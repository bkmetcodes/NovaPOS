package com.bekacodes.novapos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Exact Color Palette from Figma Design
val PosBg = Color(0xFF070A10)
val PosPaneBg = Color(0xFF0C111C)
val PosCardBg = Color(0xFF111826)
val PosCardSelectedBg = Color(0xFF102033)
val PosBorder = Color(0xFF1C2638)
val PosCyan = Color(0xFF22D3EE)
val PosCyanBg = Color(0xFF0E293C)
val PosIndigo = Color(0xFF6366F1)
val PosIndigoBorder = Color(0xFF4F46E5)
val PosEmerald = Color(0xFF10B981)
val PosEmeraldTapBg = Color(0xFF071F1B)
val PosAmber = Color(0xFFF59E0B)
val PosLavender = Color(0xFFA78BFA)
val PosInk = Color(0xFFF8FAFC)
val PosSecondary = Color(0xFF94A3B8)
val PosMuted = Color(0xFF64748B)
val ReceiptPaper = Color(0xFFF4F3EF)
val ReceiptInk = Color(0xFF1E293B)

@Composable
fun NovaPosScreen(
    uiState: NovaPosUiState,
    onIntent: (NovaPosIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = PosBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP TERMINAL TELEMETRY & AI COMMAND HEADER
            TopPosHeader(
                uiState = uiState,
                onIntent = onIntent
            )

            AnimatedVisibility(visible = uiState.aiStatusBanner != null) {
                uiState.aiStatusBanner?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PosIndigo.copy(alpha = 0.2f))
                            .clickable { onIntent(NovaPosIntent.DismissAiBanner) }
                            .padding(horizontal = 18.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✦ $msg", color = PosInk, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("✕", color = PosSecondary, fontSize = 11.sp)
                    }
                }
            }

            // 2. MAIN 3-COLUMN WIDESCREEN REGISTER STAGE
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LEFT COLUMN (42%): Menu, Search, Categories, 3x3 Grid & Shift Footer
                LeftMenuColumn(
                    uiState = uiState,
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                )

                // CENTER COLUMN (30%): Order #4092 Ticket, Tips, Split Bill & Totals
                CenterOrderTicketColumn(
                    uiState = uiState,
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                )

                // RIGHT COLUMN (28%): SoftPOS Reader & 80mm ESC/POS Thermal Receipt
                RightSoftPosAndReceiptColumn(
                    uiState = uiState,
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun TopPosHeader(
    uiState: NovaPosUiState,
    onIntent: (NovaPosIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(PosBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand + Terminal Validation Subtitle
            Column(modifier = Modifier.widthIn(min = 148.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NovaPOS",
                        color = PosInk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("✦", color = PosInk, fontSize = 14.sp)
                }
                Text(
                    text = uiState.terminalHeader,
                    color = PosMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Center Glowing Indigo AI Cashier Command Input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0E1324))
                    .border(1.dp, PosIndigoBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✦", color = Color(0xFF818CF8), fontSize = 13.sp)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.aiCommandText.isEmpty()) {
                        Text(
                            text = "Add 2 Oat Flat Whites, 1 Wagyu Burger no pickles, split 2 ways",
                            color = PosSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = uiState.aiCommandText,
                        onValueChange = { onIntent(NovaPosIntent.UpdateAiCommandText(it)) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = PosInk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(PosCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A2238))
                        .clickable { onIntent(NovaPosIntent.ExecuteAiCommand) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("↵", color = PosSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Right Module 1: ONLINE / SAF QUEUE (0) · SYNCED ↗
            Row(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (uiState.isOnline) PosEmeraldTapBg else Color(0xFF281C0B))
                    .border(
                        width = 1.dp,
                        color = if (uiState.isOnline) PosEmerald.copy(alpha = 0.45f) else PosAmber.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onIntent(NovaPosIntent.ToggleOnlineConnectivity) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isOnline) PosEmerald else PosAmber)
                        )
                        Text(
                            text = if (uiState.isOnline) "ONLINE" else "OFFLINE",
                            color = if (uiState.isOnline) PosEmerald else PosAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "SAF QUEUE (${uiState.safQueueCount})",
                        color = PosAmber,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (uiState.safQueueCount > 0 && uiState.isOnline) "SYNC NOW ↻" else "SYNCED ↗",
                    color = PosEmerald,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = uiState.safQueueCount > 0) {
                        onIntent(NovaPosIntent.SyncSafQueueNow)
                    }
                )
            }

            // Right Module 2: ESC/POS USB · READY
            Column(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PosPaneBg)
                    .border(1.dp, PosBorder, RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ESC/POS USB",
                    color = PosInk,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(PosCyan)
                    )
                    Text(
                        text = if (uiState.usbPrinterReady) "READY" else "SPOOLING",
                        color = PosCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        HorizontalDivider(color = PosBorder)
    }
}

@Composable
private fun LeftMenuColumn(
    uiState: NovaPosUiState,
    onIntent: (NovaPosIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Menu Title Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Menu",
                color = PosInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.filteredCatalog.size} items available",
                color = PosSecondary,
                fontSize = 10.sp
            )
        }

        // Search Items or Scan Barcode Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PosPaneBg)
                .border(1.dp, PosBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🔍", fontSize = 11.sp, color = PosMuted)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (uiState.searchQuery.isEmpty()) {
                    Text(
                        text = "Search items or scan barcode",
                        color = PosMuted,
                        fontSize = 11.sp
                    )
                }
                BasicTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onIntent(NovaPosIntent.UpdateSearchQuery(it)) },
                    singleLine = true,
                    textStyle = TextStyle(color = PosInk, fontSize = 11.sp),
                    cursorBrush = SolidColor(PosCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PosCardBg)
                    .border(1.dp, PosBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("⌘ K", color = PosSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // 4 Category Tabs (All Items, Espresso & Matcha, Kitchen Mains, Pastries)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MenuCategoryFilter.entries.forEach { category ->
                val selected = uiState.selectedCategory == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) PosCyanBg else PosPaneBg)
                        .border(
                            width = 1.dp,
                            color = if (selected) PosCyan else PosBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onIntent(NovaPosIntent.SelectCategory(category)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.label,
                        color = if (selected) PosCyan else PosSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3x3 Product Card Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.filteredCatalog, key = { it.id }) { item ->
                val qtyInOrder = uiState.quantityInOrder(item.id)
                MenuProductTile(
                    item = item,
                    qtyInOrder = qtyInOrder,
                    onClick = { onIntent(NovaPosIntent.TapMenuItem(item)) }
                )
            }
        }

        // Bottom Shift & Cashier Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PosPaneBg)
                .border(1.dp, PosBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PosEmerald)
                    )
                    Text(
                        text = "SHIFT OPEN",
                        color = PosEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = uiState.cashierName,
                    color = PosSecondary,
                    fontSize = 10.sp
                )
            }

            Text(
                text = "Hold order",
                color = PosCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onIntent(NovaPosIntent.HoldOrResetOrder) }
            )
        }
    }
}

@Composable
private fun MenuProductTile(
    item: PosMenuItem,
    qtyInOrder: Int,
    onClick: () -> Unit
) {
    val isSelected = qtyInOrder > 0
    val deptColor = when (item.department) {
        ItemDepartmentTag.COFFEE, ItemDepartmentTag.MATCHA -> PosCyan
        ItemDepartmentTag.KITCHEN -> PosAmber
        ItemDepartmentTag.SIDES -> Color(0xFF60A5FA)
        ItemDepartmentTag.BAKERY -> PosLavender
    }
    val isLowStock = item.stockLeft <= 5

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PosCardSelectedBg else PosPaneBg,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PosCyan else PosBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Department Tag + Quantity Badge (or '+' button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.department.label,
                    color = deptColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(PosCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = qtyInOrder.toString(),
                            color = PosBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(PosCardBg)
                            .border(1.dp, PosBorder, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = PosSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Item Name & Price
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = item.name,
                    color = PosInk,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = "$${String.format("%.2f", item.priceUsd)}",
                    color = PosInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Bottom Prep Time Pill & Stock Left Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PosCardBg)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.prepMinutes} MIN",
                        color = PosMuted,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isLowStock) PosAmber else PosEmerald)
                    )
                    Text(
                        text = "${item.stockLeft} left",
                        color = if (isLowStock) PosAmber else PosEmerald,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterOrderTicketColumn(
    uiState: NovaPosUiState,
    onIntent: (NovaPosIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PosPaneBg,
        border = BorderStroke(1.dp, PosBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Order #4092 Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order #${uiState.orderNumber}",
                        color = PosInk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(PosEmeraldTapBg)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(PosEmerald)
                        )
                        Text("LIVE", color = PosEmerald, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(uiState.tableLabel, color = PosSecondary, fontSize = 10.5.sp)
                    Text(
                        text = "${uiState.totalItemCount} items · ${uiState.diningMode}",
                        color = PosSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            HorizontalDivider(color = PosBorder, modifier = Modifier.padding(vertical = 6.dp))

            // 2. Ticket Line Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.ticketLines, key = { it.id }) { line ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntent(NovaPosIntent.DecrementOrRemoveLine(line.id)) },
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PosCardBg)
                                        .border(1.dp, PosBorder, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${line.quantity}x",
                                        color = PosInk,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = line.item.name.replace("\n", " "),
                                        color = PosInk,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (line.quantity > 1) {
                                        Text(
                                            text = "$${String.format("%.2f", line.item.priceUsd)} each",
                                            color = PosMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "$${String.format("%.2f", line.lineTotalUsd)}",
                                color = PosInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        line.modifierText?.let { mod ->
                            Box(
                                modifier = Modifier
                                    .padding(start = 32.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(PosCyanBg)
                                    .border(1.dp, PosCyan.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = mod,
                                    color = PosCyan,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Add a note / Edit items row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Add a note", color = PosMuted, fontSize = 10.sp)
                Text(
                    text = "Edit items ↗",
                    color = PosCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = PosBorder, modifier = Modifier.padding(vertical = 4.dp))

            // 3. Add a Tip Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Add a tip", color = PosInk, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("On subtotal", color = PosMuted, fontSize = 9.5.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 10, 15, 20).forEach { pct ->
                        val selected = uiState.selectedTipPercent == pct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) PosCyanBg else PosCardBg)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) PosCyan else PosBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onIntent(NovaPosIntent.SelectTipPercent(pct)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (pct == 0) "No Tip" else "$pct%",
                                color = if (selected) PosCyan else PosSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 4. SPLIT BILL Box (Split: 2 Ways · $16.82 / person)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(PosCardSelectedBg)
                    .border(1.dp, PosCyan.copy(alpha = 0.45f), RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "SPLIT BILL",
                        color = PosCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "Split: ${uiState.splitWays} Ways",
                        color = PosInk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${String.format("%.2f", uiState.perPersonSplitUsd)} / person",
                        color = PosCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PosCardBg)
                            .border(1.dp, PosBorder, RoundedCornerShape(6.dp))
                            .clickable { onIntent(NovaPosIntent.AdjustSplitWays(-1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("−", color = PosCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PosCardBg)
                            .border(1.dp, PosBorder, RoundedCornerShape(6.dp))
                            .clickable { onIntent(NovaPosIntent.AdjustSplitWays(1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = PosCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 5. Subtotal, Tax, Tip, Loyalty Credit & Total
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                TicketSummaryRow("Subtotal", "$${String.format("%.2f", uiState.subtotalUsd)}")
                TicketSummaryRow("Tax (${uiState.taxRatePercent}%)", "$${String.format("%.2f", uiState.taxUsd)}")
                TicketSummaryRow("Tip (${uiState.selectedTipPercent}%)", "$${String.format("%.2f", uiState.tipUsd)}")
                TicketSummaryRow(
                    label = "Loyalty credit",
                    value = "-$${String.format("%.2f", uiState.effectiveLoyaltyCreditUsd)}",
                    valueColor = PosEmerald
                )

                HorizontalDivider(color = PosBorder, modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Total",
                        color = PosInk,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${String.format("%.2f", uiState.grandTotalUsd)}",
                        color = PosInk,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERSON ${uiState.currentSplitPerson} OF ${uiState.splitWays}",
                        color = PosCyan,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$${String.format("%.2f", uiState.perPersonSplitUsd)} ready to collect",
                        color = PosSecondary,
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketSummaryRow(
    label: String,
    value: String,
    valueColor: Color = PosInk
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PosSecondary, fontSize = 10.5.sp)
        Text(value, color = valueColor, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RightSoftPosAndReceiptColumn(
    uiState: NovaPosUiState,
    onIntent: (NovaPosIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TOP CARD: SoftPOS Reader + APDU Trace
        SoftPosReaderCard(
            uiState = uiState,
            onTapCard = { onIntent(NovaPosIntent.TriggerSoftPosTap) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.50f)
        )

        // 2. BOTTOM CARD: 80mm ESC/POS Thermal Receipt & Print CTA
        ThermalReceiptCard(
            uiState = uiState,
            onPrintAndCut = { onIntent(NovaPosIntent.PrintEscPosReceipt) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.50f)
        )
    }
}

@Composable
private fun SoftPosReaderCard(
    uiState: NovaPosUiState,
    onTapCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoftPosPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RingScale"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PosPaneBg,
        border = BorderStroke(1.dp, PosBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: SoftPOS Reader + ● READY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SoftPOS Reader",
                    color = PosInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(PosEmeraldTapBg)
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(PosEmerald)
                    )
                    Text(
                        text = uiState.softPosStatus.badgeText,
                        color = PosEmerald,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Emerald Contactless Tap Zone Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PosEmeraldTapBg)
                    .border(1.dp, PosEmerald.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onTapCard),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Concentric Contactless Symbol Canvas
                    Box(
                        modifier = Modifier.size(58.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                color = PosEmerald.copy(alpha = 0.18f),
                                radius = (size.minDimension / 2f) * pulse,
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = PosEmerald.copy(alpha = 0.35f),
                                radius = size.minDimension * 0.38f,
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = PosEmerald.copy(alpha = 0.65f),
                                radius = size.minDimension * 0.27f,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        Text(
                            text = ")))",
                            color = PosEmerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text = "TAP CARD OR PHONE HERE",
                        color = PosEmerald,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", uiState.perPersonSplitUsd)}",
                        color = PosInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 4 Centered EMV Indicator LEDs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..4) {
                        val lit = i <= uiState.softPosStatus.activeLeds
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (lit) PosEmerald else PosBorder)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // APDU Trace Footer
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "APDU TRACE",
                        color = PosMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ISO 14443 A/B",
                        color = PosMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = uiState.apduCommandLog,
                    color = PosCyan,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiState.apduResponseLog,
                    color = PosEmerald,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ThermalReceiptCard(
    uiState: NovaPosUiState,
    onPrintAndCut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PosPaneBg,
        border = BorderStroke(1.dp, PosBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thermal receipt",
                    color = PosInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "80 mm · ESC/POS",
                    color = PosMuted,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // White Thermal Paper Ticket with Serrated Bottom Edge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(ReceiptPaper)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Receipt Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "N O V A P O S",
                            color = ReceiptInk,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "ORDER #${uiState.orderNumber} · ${uiState.tableLabel.uppercase()} · TERMINAL 04",
                            color = Color(0xFF64748B),
                            fontSize = 7.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Receipt Line Items
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        uiState.ticketLines.take(2).forEach { line ->
                            ReceiptRow(
                                left = "${line.quantity}x ${line.item.name.replace("\n", " ")}",
                                right = String.format("%.2f", line.lineTotalUsd)
                            )
                        }
                        val mods = uiState.ticketLines.mapNotNull { it.modifierText }
                        if (mods.isNotEmpty()) {
                            Text(
                                text = "  ${mods.joinToString(" / ").replace(" · included", "")}",
                                color = Color(0xFF64748B),
                                fontSize = 7.5.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }

                    // Receipt Totals Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        ReceiptRow("Subtotal", String.format("%.2f", uiState.subtotalUsd))
                        ReceiptRow(
                            left = "Tax ${uiState.taxRatePercent}% / Tip ${uiState.selectedTipPercent}%",
                            right = "${String.format("%.2f", uiState.taxUsd)} / ${String.format("%.2f", uiState.tipUsd)}"
                        )
                        ReceiptRow(
                            left = "Loyalty credit",
                            right = "-${String.format("%.2f", uiState.effectiveLoyaltyCreditUsd)}",
                            rightColor = Color(0xFF059669)
                        )
                        ReceiptRow(
                            left = "TOTAL USD",
                            right = String.format("%.2f", uiState.grandTotalUsd),
                            bold = true
                        )
                        ReceiptRow(
                            left = "SPLIT ${uiState.currentSplitPerson} OF ${uiState.splitWays}",
                            right = String.format("%.2f", uiState.perPersonSplitUsd),
                            bold = true
                        )
                    }

                    Text(
                        text = uiState.lastPrintedHexSummary ?: "Thank you · A little smarter, every sale",
                        color = Color(0xFF64748B),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Serrated Guillotine Bottom Teeth Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                ) {
                    val toothWidth = 8.dp.toPx()
                    val count = (size.width / toothWidth).toInt().coerceAtLeast(1)
                    val actualToothWidth = size.width / count
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        for (i in 0 until count) {
                            val x = i * actualToothWidth
                            lineTo(x + actualToothWidth / 2f, size.height)
                            lineTo(x + actualToothWidth, 0f)
                        }
                        close()
                    }
                    drawPath(path, ReceiptPaper)
                }
            }

            // Print ESC/POS & Cut Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PosCardBg)
                    .border(1.dp, PosBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onPrintAndCut),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🖨  Print ESC/POS & Cut", color = PosInk, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    left: String,
    right: String,
    bold: Boolean = false,
    rightColor: Color = ReceiptInk
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = left,
            color = ReceiptInk,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = right,
            color = rightColor,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Preview(
    name = "NovaPOS Figma Widescreen Register",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true
)
@Composable
private fun NovaPosScreenPreview() {
    MaterialTheme {
        NovaPosScreen(
            uiState = NovaPosUiState(),
            onIntent = {}
        )
    }
}