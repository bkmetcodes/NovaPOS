package com.bekacodes.novapos.ui

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

enum class MenuCategoryFilter(val label: String) {
    ALL("All Items"),
    ESPRESSO_MATCHA("Espresso & Matcha"),
    KITCHEN_MAINS("Kitchen Mains"),
    PASTRIES("Pastries")
}