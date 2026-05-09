package com.xpenseatlas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xpenseatlas.data.TransactionWithMemory
import com.xpenseatlas.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    monthlyDebits: Double,
    monthlyCredits: Double,
    monthTransactions: List<TransactionWithMemory>,
    selectedMonthStart: Long,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthPicked: (Long) -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    isBusinessMode: Boolean,
    onToggleBusinessMode: (Boolean) -> Unit,
    isShadowMode: Boolean,
    onToggleShadowMode: () -> Unit,
    onShowReport: () -> Unit,
    onExport: () -> Unit,
    onLaunchUpi: () -> Unit,
    onScanPastSms: () -> Unit,
    isScanningPast: Boolean,
    scanProgress: Pair<Int, Int>
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    val filtered = monthTransactions.filter {
        val tx = it.transaction
        tx.vendor.contains(searchQuery, ignoreCase = true) ||
        tx.category.contains(searchQuery, ignoreCase = true) ||
        tx.amount.toString().contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "XPENSE ATLAS",
                        fontWeight = FontWeight.Black,
                        color = GlowGreen,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onToggleShadowMode) {
                        Icon(
                            if (isShadowMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Shadow",
                            tint = GlowGreen
                        )
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Share, contentDescription = "Export", tint = GlowGreen)
                    }
                    IconButton(onClick = onShowReport) {
                        Icon(Icons.Default.BarChart, contentDescription = "Report", tint = GlowGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onLaunchUpi,
                containerColor = MossGreen,
                contentColor = ForestBlack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = "UPI")
            }
        },
        containerColor = ForestBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(ForestBlack, ForestDark))
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 12.dp)
            ) {
                // Monthly Spend Card
                item {
                    MonthlySpendCard(
                        debits = monthlyDebits,
                        credits = monthlyCredits,
                        selectedMonthStart = selectedMonthStart,
                        isShadowMode = isShadowMode,
                        onPrevMonth = onPrevMonth,
                        onNextMonth = onNextMonth,
                        onPickMonth = { showMonthPicker = true }
                    )
                }

                // Profile Toggle
                item {
                    PixelSegmentedControl(
                        isBusinessMode = isBusinessMode,
                        onToggle = onToggleBusinessMode
                    )
                }

                // Search
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search Transactions...",
                                color = PixelGray,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = GlowGreen)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MossGreen.copy(alpha = 0.3f),
                            focusedBorderColor = GlowGreen,
                            focusedTextColor = PixelCream,
                            unfocusedTextColor = PixelCream,
                            cursorColor = GlowGreen
                        ),
                        singleLine = true
                    )
                }

                // Scan Past SMS
                item {
                    ScanPastSmsButton(
                        isScanning = isScanningPast,
                        progress = scanProgress,
                        onClick = onScanPastSms
                    )
                }

                // Section header
                item {
                    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(Date(selectedMonthStart)).uppercase()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "TRANSACTIONS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PixelGray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Divider(color = MossGreen.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                        Text(
                            monthLabel,
                            fontSize = 10.sp,
                            color = MossGreen,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (searchQuery.isEmpty()) "No transactions found for this month"
                                else "No results matching '$searchQuery'",
                                color = PixelGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filtered, key = { it.transaction.id }) { item ->
                        TransactionItem(item = item, isShadowMode = isShadowMode)
                    }
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            currentStart = selectedMonthStart,
            onMonthSelected = { millis ->
                onMonthPicked(millis)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

// ─── Monthly Spend Card ──────────────────────────────────────────────────────

@Composable
fun MonthlySpendCard(
    debits: Double,
    credits: Double,
    selectedMonthStart: Long,
    isShadowMode: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPickMonth: () -> Unit
) {
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        .format(Date(selectedMonthStart)).uppercase()
    val isCurrentMonth = run {
        val now = Calendar.getInstance()
        val sel = Calendar.getInstance().apply { timeInMillis = selectedMonthStart }
        now.get(Calendar.YEAR) == sel.get(Calendar.YEAR) &&
        now.get(Calendar.MONTH) == sel.get(Calendar.MONTH)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = ForestDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Month Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = GlowGreen)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onPickMonth() }.weight(1f)
                ) {
                    Text(
                        monthLabel,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isCurrentMonth) GlowGreen else PixelCream,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("TAP TO PICK MONTH", fontSize = 9.sp, color = MossGreen, letterSpacing = 1.sp)
                }
                IconButton(
                    onClick = onNextMonth,
                    enabled = !isCurrentMonth
                ) {
                    Icon(
                        Icons.Default.ChevronRight, 
                        contentDescription = "Next", 
                        tint = if (!isCurrentMonth) GlowGreen else PixelGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Spend Number
            Text(
                "MONTHLY EXPENDITURE",
                fontSize = 11.sp,
                color = PixelGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                if (isShadowMode) "₹ * * * * *"
                else "₹${String.format("%,.0f", debits)}",
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                color = PixelRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Debit / Credit split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpendChip(
                    label = "SPENT",
                    amount = debits,
                    color = PixelRed,
                    isShadowMode = isShadowMode,
                    modifier = Modifier.weight(1f)
                )
                SpendChip(
                    label = "RECEIVED",
                    amount = credits,
                    color = GlowGreen,
                    isShadowMode = isShadowMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SpendChip(
    label: String,
    amount: Double,
    color: Color,
    isShadowMode: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (isShadowMode) "₹ ****"
                else "₹${String.format("%,.0f", amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = PixelCream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Profile Toggle ──────────────────────────────────────────────────────────

@Composable
fun PixelSegmentedControl(isBusinessMode: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ForestDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf(false to "PERSONAL", true to "BUSINESS").forEach { (isBiz, label) ->
                val selected = isBusinessMode == isBiz
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) MossGreen.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggle(isBiz) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) GlowGreen else PixelGray
                    )
                }
            }
        }
    }
}

// ─── Scan Past SMS Button ────────────────────────────────────────────────────

@Composable
fun ScanPastSmsButton(
    isScanning: Boolean,
    progress: Pair<Int, Int>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isScanning, onClick = onClick),
        color = PixelYellow.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PixelYellow.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoMode,
                    contentDescription = null,
                    tint = PixelYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isScanning) "SCANNING INBOX..." else "RESTORE HISTORY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PixelYellow
                    )
                    Text(
                        if (isScanning) "Searching for past bank messages" else "Auto-scan past months for expenses",
                        fontSize = 10.sp,
                        color = PixelGray
                    )
                }
            }
            if (isScanning && progress.second > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.first.toFloat() / progress.second.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = PixelYellow,
                    trackColor = ForestMid,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    "Processed ${progress.first} of ${progress.second} messages",
                    fontSize = 9.sp,
                    color = PixelGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ─── Transaction Item ────────────────────────────────────────────────────────

@Composable
fun TransactionItem(item: TransactionWithMemory, isShadowMode: Boolean) {
    val tx = item.transaction
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val amountColor = if (tx.isDebit) PixelRed else GlowGreen

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ForestDark.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon (Dynamic color based on debit/credit)
            Surface(
                modifier = Modifier.size(44.dp),
                color = amountColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (tx.isDebit) Icons.Default.ShoppingCart else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.vendor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PixelCream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateFormat.format(Date(tx.timestamp)),
                        fontSize = 11.sp,
                        color = PixelGray
                    )
                    if (item.previousAmount != null) {
                        Text(
                            " · PREV: ${tx.currency}${if (isShadowMode) "***" else String.format("%.0f", item.previousAmount)}",
                            fontSize = 10.sp,
                            color = MossGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount
            Text(
                "${if (tx.isDebit) "-" else "+"}${tx.currency}${
                    if (isShadowMode) "****" else String.format("%,.0f", tx.amount)
                }",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = amountColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

// ─── Month Picker Dialog ─────────────────────────────────────────────────────

@Composable
fun MonthPickerDialog(
    currentStart: Long,
    onMonthSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val currentCal = Calendar.getInstance().apply { timeInMillis = currentStart }
    var displayYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val months = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.3f)),
            color = ForestDark
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "SELECT MONTH",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = GlowGreen,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Year nav
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = GlowGreen)
                    }
                    Text(
                        "$displayYear",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PixelCream
                    )
                    IconButton(
                        onClick = { if (displayYear < currentYear) displayYear++ },
                        enabled = displayYear < currentYear
                    ) {
                        Icon(
                            Icons.Default.ChevronRight, 
                            contentDescription = null, 
                            tint = if (displayYear < currentYear) GlowGreen else PixelGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(months) { idx, month ->
                        val isFuture = displayYear == currentYear &&
                                idx > Calendar.getInstance().get(Calendar.MONTH)
                        val isSelected = displayYear == currentCal.get(Calendar.YEAR) &&
                                idx == currentCal.get(Calendar.MONTH)
                        
                        Surface(
                            modifier = Modifier
                                .clickable(enabled = !isFuture) {
                                    val newStart = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, displayYear)
                                        set(Calendar.MONTH, idx)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    onMonthSelected(newStart)
                                },
                            color = if (isSelected) GlowGreen.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) GlowGreen else MossGreen.copy(alpha = 0.1f))
                        ) {
                            Box(modifier = Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    month,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = when {
                                        isSelected -> GlowGreen
                                        isFuture   -> PixelGray.copy(alpha = 0.3f)
                                        else       -> PixelCream
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = PixelGray)
                }
            }
        }
    }
}
