package com.bekacodes.novapos.ui

fun defaultFigmaCatalog(): List<PosMenuItem> = listOf(
    PosMenuItem(
        id = "oat_flat_white",
        name = "Oat Flat\nWhite",
        department = ItemDepartmentTag.COFFEE,
        categoryFilter = MenuCategoryFilter.ESPRESSO_MATCHA,
        priceUsd = 5.50,
        prepMinutes = 3,
        stockLeft = 24,
        barcodeSku = "890101",
        defaultModifier = "+ Extra Shot · included"
    ),
    PosMenuItem(
        id = "iced_matcha",
        name = "Iced Matcha\nLatte",
        department = ItemDepartmentTag.MATCHA,
        categoryFilter = MenuCategoryFilter.ESPRESSO_MATCHA,
        priceUsd = 6.50,
        prepMinutes = 2,
        stockLeft = 18,
        barcodeSku = "890102"
    ),
    PosMenuItem(
        id = "double_espresso",
        name = "Double\nEspresso",
        department = ItemDepartmentTag.COFFEE,
        categoryFilter = MenuCategoryFilter.ESPRESSO_MATCHA,
        priceUsd = 5.50,
        prepMinutes = 2,
        stockLeft = 32,
        barcodeSku = "890103"
    ),
    PosMenuItem(
        id = "wagyu_burger",
        name = "Wagyu Smash\nBurger",
        department = ItemDepartmentTag.KITCHEN,
        categoryFilter = MenuCategoryFilter.KITCHEN_MAINS,
        priceUsd = 18.00,
        prepMinutes = 12,
        stockLeft = 8,
        barcodeSku = "890201",
        defaultModifier = "No Pickles"
    ),
    PosMenuItem(
        id = "chicken_sando",
        name = "Chicken\nSando",
        department = ItemDepartmentTag.KITCHEN,
        categoryFilter = MenuCategoryFilter.KITCHEN_MAINS,
        priceUsd = 15.00,
        prepMinutes = 8,
        stockLeft = 12,
        barcodeSku = "890202"
    ),
    PosMenuItem(
        id = "truffle_fries",
        name = "Truffle\nFries",
        department = ItemDepartmentTag.SIDES,
        categoryFilter = MenuCategoryFilter.KITCHEN_MAINS,
        priceUsd = 8.50,
        prepMinutes = 6,
        stockLeft = 16,
        barcodeSku = "890203"
    ),
    PosMenuItem(
        id = "almond_croissant",
        name = "Almond\nCroissant",
        department = ItemDepartmentTag.BAKERY,
        categoryFilter = MenuCategoryFilter.PASTRIES,
        priceUsd = 6.00,
        prepMinutes = 1,
        stockLeft = 6,
        barcodeSku = "890301"
    ),
    PosMenuItem(
        id = "cinnamon_scroll",
        name = "Cinnamon\nScroll",
        department = ItemDepartmentTag.BAKERY,
        categoryFilter = MenuCategoryFilter.PASTRIES,
        priceUsd = 5.50,
        prepMinutes = 1,
        stockLeft = 9,
        barcodeSku = "890302"
    ),
    PosMenuItem(
        id = "burrata_toast",
        name = "Burrata\nToast",
        department = ItemDepartmentTag.KITCHEN,
        categoryFilter = MenuCategoryFilter.KITCHEN_MAINS,
        priceUsd = 14.00,
        prepMinutes = 7,
        stockLeft = 4, // Low-stock amber dot in Figma
        barcodeSku = "890204"
    )
)