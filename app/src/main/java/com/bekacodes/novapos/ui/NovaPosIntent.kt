package com.bekacodes.novapos.ui

sealed interface NovaPosIntent {
    data class UpdateSearchQuery(val query: String) : NovaPosIntent
    data class SelectCategory(val category: MenuCategoryFilter) : NovaPosIntent
    data class TapMenuItem(val item: PosMenuItem, val customModifier: String? = null) : NovaPosIntent
    data class DecrementOrRemoveLine(val lineId: String) : NovaPosIntent
    data object HoldOrResetOrder : NovaPosIntent
    data class SelectTipPercent(val percent: Int) : NovaPosIntent
    data class AdjustSplitWays(val delta: Int) : NovaPosIntent
    data object ToggleOnlineSafMode : NovaPosIntent
    data object SyncSafQueueNow : NovaPosIntent
    data object TriggerSoftPosTap : NovaPosIntent
    data object PrintEscPosReceipt : NovaPosIntent
    data class UpdateAiCommandText(val text: String) : NovaPosIntent
    data object ExecuteAiCommand : NovaPosIntent
    data object DismissAiBanner : NovaPosIntent
}