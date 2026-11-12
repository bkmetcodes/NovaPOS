package com.bekacodes.novapos.ui

import androidx.compose.runtime.Immutable

@Immutable
data class PosMenuItem(
    val id: String,
    val name: String,
    val department: ItemDepartmentTag,
    val categoryFilter: MenuCategoryFilter,
    val priceUsd: Double,
    val prepMinutes: Int,
    val stockLeft: Int,
    val barcodeSku: String,
    val defaultModifier: String? = null
)