package com.bekacodes.novapos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bekacodes.novapos.data.NovaPosDao
import com.bekacodes.novapos.data.SafTransactionEntity
import com.bekacodes.novapos.domain.AiCashierAgent
import com.bekacodes.novapos.hardware.EscPosEncoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class NovaPosViewModel(
    private val dao: NovaPosDao,
    private val aiCashier: AiCashierAgent = AiCashierAgent(),
    private val escPosEncoder: EscPosEncoder = EscPosEncoder()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovaPosUiState())
    val uiState: StateFlow<NovaPosUiState> = _uiState.asStateFlow()

    init {
        // Reactively observe the SQLite Store-and-Forward (SAF) unsynced transaction count
        viewModelScope.launch {
            dao.observePendingSafCount().collect { unsyncedCount ->
                _uiState.update { it.copy(safQueueCount = unsyncedCount) }
            }
        }
    }

    fun onIntent(intent: NovaPosIntent) {
        when (intent) {
            is NovaPosIntent.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
            }

            is NovaPosIntent.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = intent.category) }
            }

            is NovaPosIntent.TapMenuItem -> {
                addOrIncrementItem(intent.item, intent.customModifier)
            }

            is NovaPosIntent.DecrementOrRemoveLine -> {
                _uiState.update { state ->
                    val updated = state.ticketLines.mapNotNull { line ->
                        if (line.id != intent.lineId) {
                            line
                        } else if (line.quantity > 1) {
                            line.copy(quantity = line.quantity - 1)
                        } else {
                            null
                        }
                    }
                    state.copy(
                        ticketLines = updated,
                        currentSplitPerson = 1
                    )
                }
            }

            is NovaPosIntent.HoldOrResetOrder -> {
                _uiState.update { state ->
                    state.copy(
                        orderNumber = state.orderNumber + 1,
                        ticketLines = emptyList(),
                        currentSplitPerson = 1,
                        softPosStatus = SoftPosStatus.READY,
                        apduCommandLog = "> SELECT PPSE   2PAY.SYS.DDF01",
                        apduResponseLog = "> 9000 OK       Awaiting card proximity...",
                        aiStatusBanner = "Order #${state.orderNumber} placed on hold · Ready for Order #${state.orderNumber + 1}"
                    )
                }
            }

            is NovaPosIntent.SelectTipPercent -> {
                _uiState.update { it.copy(selectedTipPercent = intent.percent) }
            }

            is NovaPosIntent.AdjustSplitWays -> {
                _uiState.update { state ->
                    val nextSplit = (state.splitWays + intent.delta).coerceIn(1, 8)
                    state.copy(
                        splitWays = nextSplit,
                        currentSplitPerson = state.currentSplitPerson.coerceAtMost(nextSplit)
                    )
                }
            }

            is NovaPosIntent.ToggleOnlineSafMode -> {
                val nextOnline = !_uiState.value.isOnline
                _uiState.update {
                    it.copy(
                        isOnline = nextOnline,
                        aiStatusBanner = if (nextOnline) {
                            "Terminal ONLINE · Store-and-Forward (SAF) auto-sync enabled"
                        } else {
                            "Terminal OFFLINE · Card taps will queue in encrypted Room SAF ledger"
                        }
                    )
                }
                if (nextOnline && _uiState.value.safQueueCount > 0) {
                    onIntent(NovaPosIntent.SyncSafQueueNow)
                }
            }

            is NovaPosIntent.SyncSafQueueNow -> {
                viewModelScope.launch {
                    val flushedCount = dao.markAllPendingAsSynced()
                    if (flushedCount > 0) {
                        _uiState.update {
                            it.copy(
                                isOnline = true,
                                aiStatusBanner = "✓ Flushed $flushedCount offline SAF transaction(s) to acquiring host with UUID idempotency keys"
                            )
                        }
                    }
                }
            }

            is NovaPosIntent.TriggerSoftPosTap -> {
                processSoftPosEmvTap()
            }

            is NovaPosIntent.PrintEscPosReceipt -> {
                spoolEscPosReceipt()
            }

            is NovaPosIntent.UpdateAiCommandText -> {
                _uiState.update { it.copy(aiCommandText = intent.text) }
            }

            is NovaPosIntent.ExecuteAiCommand -> {
                executeAiCashierPrompt(_uiState.value.aiCommandText)
            }

            is NovaPosIntent.DismissAiBanner -> {
                _uiState.update { it.copy(aiStatusBanner = null) }
            }
        }
    }

    private fun addOrIncrementItem(item: PosMenuItem, customModifier: String?) {
        _uiState.update { state ->
            val existingIndex = state.ticketLines.indexOfFirst { it.item.id == item.id }
            val updatedLines = if (existingIndex >= 0) {
                state.ticketLines.mapIndexed { idx, line ->
                    if (idx == existingIndex) line.copy(quantity = line.quantity + 1) else line
                }
            } else {
                state.ticketLines + OrderTicketLine(
                    id = "line_${item.id}",
                    item = item,
                    quantity = 1,
                    modifierText = customModifier ?: item.defaultModifier
                )
            }

            state.copy(
                ticketLines = updatedLines,
                softPosStatus = SoftPosStatus.READY
            )
        }
    }

    private fun executeAiCashierPrompt(rawText: String) {
        if (rawText.isBlank()) return
        val current = _uiState.value
        val parsed = aiCashier.parseCashierCommand(rawText, current.menuCatalog)

        _uiState.update { state ->
            state.copy(
                ticketLines = if (parsed.lines.isNotEmpty()) parsed.lines else state.ticketLines,
                splitWays = parsed.splitWays ?: state.splitWays,
                selectedTipPercent = parsed.tipPercent ?: state.selectedTipPercent,
                currentSplitPerson = 1,
                softPosStatus = SoftPosStatus.READY,
                aiStatusBanner = parsed.summaryMessage
            )
        }
    }

    private fun processSoftPosEmvTap() {
        val snapshot = _uiState.value
        if (snapshot.ticketLines.isEmpty() || snapshot.perPersonSplitUsd <= 0.0) return

        viewModelScope.launch {
            val arqc = Random.nextBytes(4).joinToString("") { "%02X".format(it) }
            val idempotencyKey = "idem_${UUID.randomUUID()}"

            // Step 1: ISO-DEP Handshake
            _uiState.update {
                it.copy(
                    softPosStatus = SoftPosStatus.READING_APDU,
                    apduCommandLog = "> 80 A8 00 00   GET PROCESSING OPTIONS",
                    apduResponseLog = "> 80 AE 80 00   GENERATING ARQC ($arqc)..."
                )
            }
            delay(280L.milliseconds)

            // Step 2: Persist in Room SAF Queue (Synced immediately if online, queued if offline)
            val isOnlineNow = _uiState.value.isOnline
            dao.insertTransactionIdempotent(
                SafTransactionEntity(
                    idempotencyKey = idempotencyKey,
                    orderNumber = snapshot.orderNumber,
                    splitPersonIndex = snapshot.currentSplitPerson,
                    splitTotalWays = snapshot.splitWays,
                    amountChargedUsd = snapshot.perPersonSplitUsd,
                    arqcCryptogram = arqc,
                    isSynced = isOnlineNow
                )
            )

            // Step 3: Advance Split Person or Complete Order & Decrement Stock
            _uiState.update { state ->
                val isLastSplit = state.currentSplitPerson >= state.splitWays
                val updatedCatalog = if (isLastSplit) {
                    state.menuCatalog.map { catItem ->
                        val orderedQty = state.quantityInOrder(catItem.id)
                        if (orderedQty > 0) {
                            catItem.copy(
                                stockLeft = (catItem.stockLeft - orderedQty).coerceAtLeast(
                                    0
                                )
                            )
                        } else {
                            catItem
                        }
                    }
                } else {
                    state.menuCatalog
                }

                val nextStatus =
                    if (isOnlineNow) SoftPosStatus.APPROVED else SoftPosStatus.QUEUED_SAF
                val statusMsg = if (isOnlineNow) {
                    "Approved $${
                        String.format(
                            "%.2f",
                            state.perPersonSplitUsd
                        )
                    } (Person ${state.currentSplitPerson}/${state.splitWays}) · ARQC $arqc"
                } else {
                    "Queued $${
                        String.format(
                            "%.2f",
                            state.perPersonSplitUsd
                        )
                    } in Offline SAF Ledger (Idempotency: ${idempotencyKey.take(12)})"
                }

                state.copy(
                    menuCatalog = updatedCatalog,
                    currentSplitPerson = if (isLastSplit) 1 else state.currentSplitPerson + 1,
                    softPosStatus = nextStatus,
                    apduCommandLog = "> 80 AE 80 00   ARQC=$arqc AID=A0000000031010",
                    apduResponseLog = if (isOnlineNow) {
                        "> 9000 OK       AUTH APPROVED ($${
                            String.format(
                                "%.2f",
                                state.perPersonSplitUsd
                            )
                        })"
                    } else {
                        "> 9000 SAF      STORED OFFLINE (${idempotencyKey.take(12)})"
                    },
                    aiStatusBanner = statusMsg
                )
            }
        }
    }

    private fun spoolEscPosReceipt() {
        val current = _uiState.value
        val job = escPosEncoder.encodeReceipt(current)
        _uiState.update {
            it.copy(
                lastPrintedHexSummary = job.hexPreview,
                aiStatusBanner = "🖨 Spooled ${job.byteLength} bytes to 80mm ESC/POS USB endpoint (${job.hexPreview})"
            )
        }
    }

    companion object {
        fun provideFactory(dao: NovaPosDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NovaPosViewModel(dao) as T
                }
            }
    }
}