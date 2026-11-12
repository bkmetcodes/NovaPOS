package com.bekacodes.novapos.ui

enum class SoftPosStatus(val badgeText: String, val activeLeds: Int) {
    READY("READY", 4),
    READING_APDU("READING ISO-DEP", 2),
    APPROVED("APPROVED · 9000 OK", 4),
    QUEUED_SAF("QUEUED IN SAF", 4)
}