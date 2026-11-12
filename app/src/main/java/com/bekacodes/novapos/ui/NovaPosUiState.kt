package com.bekacodes.novapos.ui

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class NovaPosUiState(
    // Top Header Bar
    val terminalHeader: String = "TERMINAL #04 · PCI-PTS VALIDATED",
    val aiCommandText: String = "Add 2 Oat Flat Whites, 1 Wagyu Burger no pickles, split 2 ways",
    val isOnline: Boolean = true,
    val safQueueCount: Int = 0,
    val usbPrinterReady: Boolean = true,
    val aiStatusBanner: String? = null,

    // Left Pane: Menu & Search
    val searchQuery: String = "",
    val selectedCategory: MenuCategoryFilter = MenuCategoryFilter.ALL,
    val menuCatalog: List<PosMenuItem> = defaultFigmaCatalog(),
    val cashierName: String = "Maya Chen / Cashier",
    val isShiftOpen: Boolean = true,

    // Center Pane: Order #4092 Ticket & Split Engine
    val orderNumber: Int = 4092,
    val tableLabel: String = "Table 12",
    val diningMode: String = "Dine in",
    val ticketLines: List<OrderTicketLine> = listOf(
        OrderTicketLine(
            id = "line_oat_flat",
            item = defaultFigmaCatalog()[0],
            quantity = 2,
            modifierText = "+ Extra Shot · included"
        ),
        OrderTicketLine(
            id = "line_wagyu",
            item = defaultFigmaCatalog()[3],
            quantity = 1,
            modifierText = "No Pickles"
        )
    ),
    val selectedTipPercent: Int = 15, // 0, 10, 15, 20
    val splitWays: Int = 2,
    val currentSplitPerson: Int = 1,
    val taxRatePercent: Double = 8.5,
    val loyaltyCreditUsd: Double = 2.18,

    // Right Pane: SoftPOS Reader & 80mm ESC/POS Receipt
    val softPosStatus: SoftPosStatus = SoftPosStatus.READY,
    val apduCommandLog: String = "> SELECT PPSE   2PAY.SYS.DDF01",
    val apduResponseLog: String = "> 9000 OK       Awaiting card proximity...",
    val lastPrintedHexSummary: String? = null
) {
    val filteredCatalog: List<PosMenuItem>
        get() = menuCatalog.filter { item ->
            val matchesCat =
                selectedCategory == MenuCategoryFilter.ALL || item.categoryFilter == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    item.name.lowercase().contains(q) ||
                    item.department.label.lowercase().contains(q) ||
                    item.barcodeSku.lowercase().contains(q)
            matchesCat && matchesSearch
        }

    val totalItemCount: Int
        get() = ticketLines.sumOf { it.quantity }

    val subtotalUsd: Double
        get() = ticketLines.sumOf { it.lineTotalUsd }

    val taxUsd: Double
        get() = ((subtotalUsd * (taxRatePercent / 100.0)) * 100.0).roundToInt() / 100.0

    val tipUsd: Double
        get() = ((subtotalUsd * (selectedTipPercent / 100.0)) * 100.0).roundToInt() / 100.0

    val effectiveLoyaltyCreditUsd: Double
        get() = if (subtotalUsd > 0.0) loyaltyCreditUsd else 0.0

    val grandTotalUsd: Double
        get() = (subtotalUsd + taxUsd + tipUsd - effectiveLoyaltyCreditUsd).coerceAtLeast(0.0)

    val perPersonSplitUsd: Double
        get() = if (splitWays <= 1) {
            grandTotalUsd
        } else {
            ((grandTotalUsd / splitWays) * 100.0).roundToInt() / 100.0
        }

    fun quantityInOrder(itemId: String): Int =
        ticketLines.filter { it.item.id == itemId }.sumOf { it.quantity }
}