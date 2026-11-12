package com.bekacodes.novapos.ui

import androidx.compose.runtime.Immutable

@Immutable
data class OrderTicketLine(
    val id: String,
    val item: PosMenuItem,
    val quantity: Int,
    val modifierText: String? = null
) {
    val lineTotalUsd: Double
        get() = item.priceUsd * quantity
}