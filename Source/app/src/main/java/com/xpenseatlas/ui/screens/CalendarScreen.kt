package com.xpenseatlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xpenseatlas.data.TransactionWithMemory
import com.xpenseatlas.ui.theme.*
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    transactions: List<TransactionWithMemory>,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
    
    // Group transactions by day
    val dailySpend = transactions.groupBy { 
        val cal = Calendar.getInstance().apply { timeInMillis = it.transaction.timestamp }
        cal.get(Calendar.DAY_OF_MONTH)
    }.mapValues { it.value.sumOf { tx -> tx.transaction.amount } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, -1)
                            currentMonth = newMonth
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = AccentTeal)
                        }
                        
                        Text(monthName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                        
                        IconButton(onClick = { 
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentTeal)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Weekday Labels
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Empty cells before first day
                        items(firstDayOfWeek) { Box(modifier = Modifier.aspectRatio(1f)) }
                        
                        items(daysInMonth) { day ->
                            val dayNum = day + 1
                            val spend = dailySpend[dayNum] ?: 0.0
                            
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (spend > 0) NeonPurple.copy(alpha = (spend / 5000.0).coerceIn(0.1, 0.8).toFloat()) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        color = if (spend > 0) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontWeight = if (spend > 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (spend > 0) {
                                        Text(
                                            text = "₹${spend.toInt()}",
                                            fontSize = 8.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text("Close", color = Color.Black)
                    }
                }
            }
        }
    }
}
