package com.xpenseatlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xpenseatlas.data.CategoryTotal
import com.xpenseatlas.data.MerchantTotal
import com.xpenseatlas.ui.theme.*
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

@Composable
fun ReportDialog(
    categoryTotals: List<CategoryTotal>,
    topMerchants: List<MerchantTotal>,
    selectedMonth: Calendar,
    onMonthChanged: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.time)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepIndigo),
            color = DeepIndigo
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Financial Atlas",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentTeal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                monthName,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                            IconButton(onClick = {
                                val next = (selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                onMonthChanged(next)
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                val next = (selectedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                onMonthChanged(next)
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    item {
                        Text("Top Categories", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(12.dp))
                        categoryTotals.forEach { cat ->
                            CategoryProgressItem(cat.category, cat.total, categoryTotals.firstOrNull()?.total ?: 1.0)
                        }
                    }

                    item {
                        Text("Top Merchants", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(12.dp))
                        topMerchants.forEach { merchant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(merchant.vendor, color = Color.White)
                                Text("₹${String.format("%.0f", merchant.total)}", fontWeight = FontWeight.Bold, color = AccentTeal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgressItem(name: String, amount: Double, maxAmount: Double) {
    val progress = (amount / maxAmount).toFloat()
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = Color.White, fontSize = 14.sp)
            Text("₹${String.format("%.0f", amount)}", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(SurfaceDark, RoundedCornerShape(4.dp)),
            color = NeonPurple,
            trackColor = SurfaceDark
        )
    }
}
