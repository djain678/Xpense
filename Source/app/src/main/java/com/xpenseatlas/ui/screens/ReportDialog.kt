package com.xpenseatlas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xpenseatlas.data.CategoryTotal
import com.xpenseatlas.data.MerchantTotal
import com.xpenseatlas.data.TransactionWithMemory
import com.xpenseatlas.ui.theme.*
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ReportDialog(
    categoryTotals: List<CategoryTotal>,
    topMerchants: List<MerchantTotal>,
    monthTransactions: List<TransactionWithMemory>,
    selectedMonth: Calendar,
    onMonthChanged: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.time)
    
    // State to track which category is expanded
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ForestBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "FINANCIAL ATLAS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = GlowGreen,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                monthName.uppercase(),
                                color = PixelGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val next = (selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                    onMonthChanged(next)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = MossGreen)
                            }
                            IconButton(
                                onClick = {
                                    val next = (selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                    onMonthChanged(next)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = MossGreen)
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PixelCream)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Top Categories Section
                    item {
                        Text(
                            "TOP CATEGORIES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MossGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val maxAmount = categoryTotals.maxOfOrNull { it.total } ?: 1.0

                    items(categoryTotals) { cat ->
                        val isExpanded = expandedCategory == cat.category
                        val categoryTxs = monthTransactions.filter { 
                            it.transaction.category == cat.category && it.transaction.isDebit 
                        }

                        CategoryAccordionItem(
                            category = cat,
                            maxAmount = maxAmount,
                            isExpanded = isExpanded,
                            transactions = categoryTxs,
                            onToggle = { 
                                expandedCategory = if (isExpanded) null else cat.category 
                            }
                        )
                    }

                    // Top Merchants Section
                    if (topMerchants.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "FREQUENT MERCHANTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MossGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ForestDark,
                                border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    topMerchants.forEachIndexed { index, merchant ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                merchant.vendor, 
                                                color = PixelCream,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                "₹${String.format("%,.0f", merchant.total)}", 
                                                fontWeight = FontWeight.ExtraBold, 
                                                color = PixelRed,
                                                fontSize = 14.sp
                                            )
                                        }
                                        if (index < topMerchants.size - 1) {
                                            HorizontalDivider(color = MossGreen.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAccordionItem(
    category: CategoryTotal,
    maxAmount: Double,
    isExpanded: Boolean,
    transactions: List<TransactionWithMemory>,
    onToggle: () -> Unit
) {
    val progress = (category.total / maxAmount).toFloat()
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Surface(
        color = if (isExpanded) MossGreen.copy(alpha = 0.05f) else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.category, 
                        color = PixelCream, 
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp),
                        color = PixelRed,
                        trackColor = ForestDark,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${String.format("%,.0f", category.total)}",
                        fontWeight = FontWeight.Black,
                        color = PixelCream,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MossGreen,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            // Expanded Transactions List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (transactions.isEmpty()) {
                        Text(
                            "No detailed transactions found.",
                            color = PixelGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        transactions.forEach { txItem ->
                            val tx = txItem.transaction
                            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.timestamp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        tx.vendor,
                                        color = PixelCream.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        dateStr,
                                        color = PixelGray,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    "₹${String.format("%,.0f", tx.amount)}",
                                    color = PixelRed.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
