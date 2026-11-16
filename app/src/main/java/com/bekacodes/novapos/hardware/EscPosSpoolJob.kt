package com.bekacodes.novapos.hardware

import com.bekacodes.novapos.ui.NovaPosUiState
import java.io.ByteArrayOutputStream

data class EscPosSpoolJob(
    val rawBytes: ByteArray,
    val hexPreview: String,
    val byteLength: Int
)

class EscPosEncoder {

    fun encodeReceipt(uiState: NovaPosUiState): EscPosSpoolJob {
        val buffer = ByteArrayOutputStream()

        // 1. ESC @ -> Initialize printer
        buffer.write(byteArrayOf(0x1B, 0x40))

        // 2. ESC a 1 -> Center alignment + Bold header
        buffer.write(byteArrayOf(0x1B, 0x61, 0x01))
        buffer.write(byteArrayOf(0x1B, 0x45, 0x01))
        buffer.write("N O V A P O S\n".encodeToByteArray())
        buffer.write(byteArrayOf(0x1B, 0x45, 0x00))
        buffer.write(
            "ORDER #${uiState.orderNumber} - ${uiState.tableLabel.uppercase()} - TERMINAL 04\n"
                .encodeToByteArray()
        )
        buffer.write("------------------------------------------\n".encodeToByteArray())

        // 3. ESC a 0 -> Left alignment for line items (42-char 80mm thermal width)
        buffer.write(byteArrayOf(0x1B, 0x61, 0x00))
        uiState.ticketLines.forEach { line ->
            val cleanName = line.item.name.replace("\n", " ")
            val left = "${line.quantity}x $cleanName"
            val right = String.format("%.2f", line.lineTotalUsd)
            buffer.write(formatThermalRow(left, right).encodeToByteArray())
            line.modifierText?.let { mod ->
                buffer.write("  $mod\n".encodeToByteArray())
            }
        }

        buffer.write("------------------------------------------\n".encodeToByteArray())
        buffer.write(
            formatThermalRow("Subtotal", String.format("%.2f", uiState.subtotalUsd))
                .encodeToByteArray()
        )
        buffer.write(
            formatThermalRow(
                "Tax ${uiState.taxRatePercent}% / Tip ${uiState.selectedTipPercent}%",
                "${String.format("%.2f", uiState.taxUsd)} / ${String.format("%.2f", uiState.tipUsd)}"
            ).encodeToByteArray()
        )
        if (uiState.effectiveLoyaltyCreditUsd > 0) {
            buffer.write(
                formatThermalRow(
                    "Loyalty credit",
                    "-${String.format("%.2f", uiState.effectiveLoyaltyCreditUsd)}"
                ).encodeToByteArray()
            )
        }

        // 4. Bold Totals & Split Tender Line
        buffer.write(byteArrayOf(0x1B, 0x45, 0x01))
        buffer.write(
            formatThermalRow("TOTAL USD", String.format("%.2f", uiState.grandTotalUsd))
                .encodeToByteArray()
        )
        buffer.write(
            formatThermalRow(
                "SPLIT ${uiState.currentSplitPerson} OF ${uiState.splitWays}",
                String.format("%.2f", uiState.perPersonSplitUsd)
            ).encodeToByteArray()
        )
        buffer.write(byteArrayOf(0x1B, 0x45, 0x00))

        // 5. Center Footer + GS V B 00 -> Guillotine Partial Paper Cut
        buffer.write(byteArrayOf(0x1B, 0x61, 0x01))
        buffer.write("\nThank you - A little smarter, every sale\n\n".encodeToByteArray())
        buffer.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))

        val bytes = buffer.toByteArray()
        val headHex = bytes.take(8).joinToString(" ") { "%02X".format(it) }
        val tailHex = bytes.takeLast(4).joinToString(" ") { "%02X".format(it) }

        return EscPosSpoolJob(
            rawBytes = bytes,
            hexPreview = "ESC/POS [$headHex ... $tailHex] (${bytes.size}B Cut OK)",
            byteLength = bytes.size
        )
    }

    private fun formatThermalRow(left: String, right: String, width: Int = 42): String {
        val maxLeft = (width - right.length - 1).coerceAtLeast(1)
        val clippedLeft = left.take(maxLeft)
        val spaces = " ".repeat((width - clippedLeft.length - right.length).coerceAtLeast(1))
        return "$clippedLeft$spaces$right\n"
    }
}