package com.bekacodes.novapos.domain

import com.bekacodes.novapos.ui.OrderTicketLine
import com.bekacodes.novapos.ui.PosMenuItem

data class ParsedCashierOrder(
    val lines: List<OrderTicketLine>,
    val splitWays: Int?,
    val tipPercent: Int?,
    val summaryMessage: String
)

class AiCashierAgent {

    private val itemSynonyms = mapOf(
        "oat_flat_white" to listOf("oat flat white", "oat flat whites", "flat white", "flat whites"),
        "iced_matcha" to listOf("iced matcha latte", "iced matcha", "matcha latte", "matcha"),
        "double_espresso" to listOf("double espresso", "espresso", "espressos"),
        "wagyu_burger" to listOf("wagyu smash burger", "wagyu burger", "smash burger", "burger", "wagyu"),
        "chicken_sando" to listOf("chicken sando", "sando", "chicken sandwich"),
        "truffle_fries" to listOf("truffle fries", "fries"),
        "almond_croissant" to listOf("almond croissant", "croissant", "croissants"),
        "cinnamon_scroll" to listOf("cinnamon scroll", "scroll", "cinnamon roll"),
        "burrata_toast" to listOf("burrata toast", "burrata", "toast")
    )

    /**
     * Parses natural-language cashier prompts such as:
     * - "Add 2 Oat Flat Whites, 1 Wagyu Burger no pickles, split 2 ways"
     * - "3 Iced Matcha, 2 Truffle Fries extra crispy, tip 20%, split 3 ways"
     */
    fun parseCashierCommand(
        rawCommand: String,
        catalog: List<PosMenuItem>
    ): ParsedCashierOrder {
        val clean = rawCommand.trim()
        val lower = clean.lowercase()

        // 1. Extract optional Split Bill count ("split 2 ways" / "split by 3")
        val parsedSplit = Regex("""split\s+(?:by\s+)?(\d+)""")
            .find(lower)
            ?.groupValues?.get(1)
            ?.toIntOrNull()
            ?.coerceIn(1, 8)

        // 2. Extract optional Tip % ("tip 20%" / "15% tip")
        val parsedTip = Regex("""(?:tip\s*(\d{1,2})\s*%|(\d{1,2})\s*%\s*tip)""")
            .find(lower)
            ?.let { match ->
                (match.groupValues[1].ifEmpty { match.groupValues[2] }).toIntOrNull()
            }

        // 3. Split clauses by comma or "and" to parse individual line items
        val clauses = clean.split(Regex("""\s*,\s*|\s+and\s+""", RegexOption.IGNORE_CASE))
        val parsedLines = mutableListOf<OrderTicketLine>()

        clauses.forEach { clause ->
            val clauseLower = clause.lowercase().trim()
            if (clauseLower.startsWith("split") || clauseLower.contains("tip")) return@forEach

            val matchedItem = catalog.firstOrNull { menuItem ->
                val keywords = itemSynonyms[menuItem.id]
                    ?: listOf(menuItem.name.replace("\n", " ").lowercase())
                keywords.any { kw -> clauseLower.contains(kw) }
            } ?: return@forEach

            val qty = Regex("""\b(\d+)\s*x?\b""")
                .find(clauseLower)
                ?.groupValues?.get(1)
                ?.toIntOrNull()
                ?.coerceIn(1, 20) ?: 1

            val detectedModifier = extractModifierFromClause(clauseLower, matchedItem)

            parsedLines.add(
                OrderTicketLine(
                    id = "line_${matchedItem.id}",
                    item = matchedItem,
                    quantity = qty,
                    modifierText = detectedModifier
                )
            )
        }

        val itemSummary = if (parsedLines.isNotEmpty()) {
            parsedLines.joinToString(", ") { "${it.quantity}x ${it.item.name.replace("\n", " ")}" }
        } else {
            "Updated ticket settings"
        }

        val splitSuffix = parsedSplit?.let { " · Split $it ways" } ?: ""
        return ParsedCashierOrder(
            lines = parsedLines,
            splitWays = parsedSplit,
            tipPercent = parsedTip,
            summaryMessage = "AI Cashier: $itemSummary$splitSuffix"
        )
    }

    private fun extractModifierFromClause(clauseLower: String, item: PosMenuItem): String? {
        val modifierPatterns = listOf(
            Regex("""(no\s+[a-z]+)""") to { m: String ->
                m.split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
            },
            Regex("""(extra\s+[a-z]+)""") to { m: String ->
                "+ " + m.split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
            },
            Regex("""(without\s+[a-z]+)""") to { m: String ->
                m.replace("without", "No").split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
            }
        )

        for ((regex, formatter) in modifierPatterns) {
            val match = regex.find(clauseLower)
            if (match != null) {
                return formatter(match.groupValues[1])
            }
        }
        return item.defaultModifier
    }
}