package com.bekacodes.novapos.ui

import com.bekacodes.novapos.data.NovaPosDao
import com.bekacodes.novapos.data.SafTransactionEntity
import com.bekacodes.novapos.domain.AiCashierAgent
import com.bekacodes.novapos.hardware.EscPosEncoder
import com.bekacodes.novapos.hardware.EscPosSpoolJob
import com.bekacodes.novapos.hardware.nfc.EmvApduResult
import com.bekacodes.novapos.hardware.nfc.SoftPosNfcReader
import com.bekacodes.novapos.hardware.printer.UsbEscPosPrinterTransport
import com.bekacodes.novapos.hardware.printer.UsbPrinterResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NovaPosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeNovaPosDao
    private lateinit var aiCashier: AiCashierAgent
    private lateinit var escPosEncoder: EscPosEncoder
    private lateinit var fakeNfcReader: FakeSoftPosNfcReader
    private lateinit var fakePrinterTransport: FakeUsbEscPosPrinterTransport

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeNovaPosDao()
        aiCashier = AiCashierAgent()
        escPosEncoder = EscPosEncoder()
        fakeNfcReader = FakeSoftPosNfcReader()
        fakePrinterTransport = FakeUsbEscPosPrinterTransport()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `EscPosEncoder emits valid ESC init header and GS guillotine partial-cut bytes`() {
        val job = escPosEncoder.encodeReceipt(NovaPosUiState())
        val bytes = job.rawBytes

        assertTrue(bytes.size > 60)
        // Header: ESC @ (0x1B 0x40)
        assertEquals(0x1B.toByte(), bytes[0])
        assertEquals(0x40.toByte(), bytes[1])

        // Trailer: GS V B 0 (0x1D 0x56 0x42 0x00)
        assertEquals(0x1D.toByte(), bytes[bytes.size - 4])
        assertEquals(0x56.toByte(), bytes[bytes.size - 3])
        assertEquals(0x42.toByte(), bytes[bytes.size - 2])
        assertEquals(0x00.toByte(), bytes[bytes.size - 1])

        val decodedText = bytes.decodeToString()
        assertTrue(decodedText.contains("ORDER #4092"))
        assertTrue(decodedText.contains("33.64"))
        assertTrue(decodedText.contains("16.82"))
    }

    @Test
    fun `AiCashierAgent parses multi-item order, modifiers, tip percentage, and split ways`() {
        val parsed = aiCashier.parseCashierCommand(
            rawCommand = "3 Iced Matcha, 2 Truffle Fries extra crispy, tip 20%, split 3 ways",
            catalog = defaultFigmaCatalog()
        )

        assertEquals(2, parsed.lines.size)
        assertEquals("iced_matcha", parsed.lines[0].item.id)
        assertEquals(3, parsed.lines[0].quantity)
        assertEquals("truffle_fries", parsed.lines[1].item.id)
        assertEquals(2, parsed.lines[1].quantity)
        assertEquals("+ Extra Crispy", parsed.lines[1].modifierText)
        assertEquals(20, parsed.tipPercent)
        assertEquals(3, parsed.splitWays)
    }

    @Test
    fun `offline SoftPOS taps queue into Room SAF ledger and flush idempotently when back online`() =
        runTest(testDispatcher) {
            val viewModel = NovaPosViewModel(
                dao = fakeDao,
                aiCashier = aiCashier,
                escPosEncoder = escPosEncoder,
                nfcReader = fakeNfcReader,
                usbPrinterTransport = fakePrinterTransport,
                workManager = null
            )
            runCurrent()

            // Initial default order matches $33.64 total split 2 ways ($16.82 / person)
            assertEquals(33.64, viewModel.uiState.value.grandTotalUsd, 0.01)
            assertEquals(16.82, viewModel.uiState.value.perPersonSplitUsd, 0.01)

            // 1. Switch terminal into OFFLINE SAF mode
            viewModel.onIntent(NovaPosIntent.ToggleOnlineSafMode)
            runCurrent()
            assertFalse(viewModel.uiState.value.isOnline)

            // 2. Person 1 taps card while offline
            viewModel.onIntent(NovaPosIntent.TriggerSoftPosTap)
            advanceTimeBy(300L)
            runCurrent()

            val stateAfterSplit1 = viewModel.uiState.value
            assertEquals(SoftPosStatus.QUEUED_SAF, stateAfterSplit1.softPosStatus)
            assertEquals(1, stateAfterSplit1.safQueueCount)
            assertEquals(2, stateAfterSplit1.currentSplitPerson) // Advanced to Person 2 of 2

            // Stock should not decrement until final split completes
            val flatWhiteStockBefore =
                stateAfterSplit1.menuCatalog.first { it.id == "oat_flat_white" }.stockLeft
            assertEquals(24, flatWhiteStockBefore)

            // 3. Person 2 taps card while offline -> Completes order and decrements stock (24 -> 22)
            viewModel.onIntent(NovaPosIntent.TriggerSoftPosTap)
            advanceTimeBy(300L)
            runCurrent()

            val stateAfterSplit2 = viewModel.uiState.value
            assertEquals(2, stateAfterSplit2.safQueueCount)
            assertEquals(1, stateAfterSplit2.currentSplitPerson)
            val flatWhiteStockAfter =
                stateAfterSplit2.menuCatalog.first { it.id == "oat_flat_white" }.stockLeft
            assertEquals(22, flatWhiteStockAfter)

            // 4. Restore ONLINE connectivity -> Auto-flushes both SAF transactions
            viewModel.onIntent(NovaPosIntent.ToggleOnlineSafMode)
            runCurrent()

            assertTrue(viewModel.uiState.value.isOnline)
            assertEquals(0, viewModel.uiState.value.safQueueCount)
        }

    @Test
    fun `PrintEscPosReceipt dispatches to UsbEscPosPrinterTransport`() = runTest(testDispatcher) {
        val viewModel = NovaPosViewModel(
            dao = fakeDao,
            aiCashier = aiCashier,
            escPosEncoder = escPosEncoder,
            nfcReader = fakeNfcReader,
            usbPrinterTransport = fakePrinterTransport,
            workManager = null
        )

        viewModel.onIntent(NovaPosIntent.PrintEscPosReceipt)
        runCurrent()

        assertTrue(viewModel.uiState.value.aiStatusBanner?.contains("Transferred 250 bytes") == true)
    }

    @Test
    fun `live ISO-DEP NFC tap updates APDU log and approves transaction`() = runTest(testDispatcher) {
        val viewModel = NovaPosViewModel(
            dao = fakeDao,
            aiCashier = aiCashier,
            escPosEncoder = escPosEncoder,
            nfcReader = fakeNfcReader,
            usbPrinterTransport = fakePrinterTransport,
            workManager = null
        )
        runCurrent()

        fakeNfcReader.emitTapResult(
            EmvApduResult(
                commandHex = "> SELECT PPSE (00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00)",
                responseHex = "> 9000 OK - Contactless EMV (6F 1E 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 90 00)",
                isApproved = true,
                arqcHex = "9F2604A1B2C3D4",
                isRealNfcCard = true
            )
        )
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(SoftPosStatus.APPROVED, state.softPosStatus)
        assertTrue(state.apduCommandLog.contains("SELECT PPSE"))
        assertTrue(state.apduResponseLog.contains("9000 OK"))
        assertTrue(state.aiStatusBanner?.contains("NFC Contactless Card Approved") == true)
    }
}

private class FakeSoftPosNfcReader : SoftPosNfcReader(null) {
    private val _flow = MutableSharedFlow<EmvApduResult>(extraBufferCapacity = 64)
    override val apduResults: SharedFlow<EmvApduResult> = _flow.asSharedFlow()

    suspend fun emitTapResult(result: EmvApduResult) {
        _flow.emit(result)
    }
}

private class FakeUsbEscPosPrinterTransport : UsbEscPosPrinterTransport(null) {
    override suspend fun printReceipt(job: EscPosSpoolJob): UsbPrinterResult {
        return UsbPrinterResult(
            success = true,
            message = "Transferred 250 bytes to 80mm ESC/POS USB Printer"
        )
    }
}

private class FakeNovaPosDao : NovaPosDao {
    private val transactionsFlow = MutableStateFlow<List<SafTransactionEntity>>(emptyList())

    override fun observePendingSafCount(): Flow<Int> =
        transactionsFlow.map { list -> list.count { !it.isSynced } }

    override suspend fun getPendingSafTransactions(): List<SafTransactionEntity> =
        transactionsFlow.value.filter { !it.isSynced }

    override suspend fun insertTransactionIdempotent(tx: SafTransactionEntity): Long {
        if (transactionsFlow.value.any { it.idempotencyKey == tx.idempotencyKey }) return -1L
        transactionsFlow.value = listOf(tx) + transactionsFlow.value
        return 1L
    }

    override suspend fun markAllPendingAsSynced(): Int {
        val pendingCount = transactionsFlow.value.count { !it.isSynced }
        transactionsFlow.value = transactionsFlow.value.map { it.copy(isSynced = true) }
        return pendingCount
    }

    override fun observeRecentTransactions(): Flow<List<SafTransactionEntity>> = transactionsFlow
}