package com.xpenseatlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xpenseatlas.data.TransactionWithMemory
import com.xpenseatlas.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<TransactionWithMemory>,
    totalExpense: Double,
    totalIncome: Double,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    isBusinessMode: Boolean,
    onToggleBusinessMode: (Boolean) -> Unit,
    isShadowMode: Boolean,
    onToggleShadowMode: () -> Unit,
    onShowReport: () -> Unit,
    onExport: () -> Unit,
    onLaunchUpi: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("XpenseAtlas", fontWeight = FontWeight.ExtraBold, color = AccentTeal)
                },
                actions = {
                    IconButton(onClick = onToggleShadowMode) {
                        Icon(
                            if (isShadowMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Shadow Mode",
                            tint = AccentTeal
                        )
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Share, contentDescription = "Export", tint = AccentTeal)
                    }
                    IconButton(onClick = onShowReport) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Report", tint = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepIndigo)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onLaunchUpi,
                containerColor = AccentTeal,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = "Launch UPI")
            }
        },
        containerColor = DeepIndigo
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SummaryCard(totalExpense, totalIncome, isShadowMode)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search transactions...", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentTeal) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SurfaceDark,
                    focusedBorderColor = AccentTeal
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Personal", color = if (!isBusinessMode) AccentTeal else Color.White.copy(alpha = 0.5f))
                Switch(
                    checked = isBusinessMode,
                    onCheckedChange = onToggleBusinessMode,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonPurple,
                        checkedTrackColor = NeonPurple.copy(alpha = 0.3f)
                    )
                )
                Text("Business", color = if (isBusinessMode) NeonPurple else Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredTransactions = transactions.filter {
                val tx = it.transaction
                val profileMatch = tx.isBusiness == isBusinessMode
                profileMatch && (
                    tx.vendor.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.amount.toString().contains(searchQuery)
                )
            }

            Text(
                "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isEmpty()) "Waiting for bank SMS..." else "No results found",
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredTransactions) { item ->
                        TransactionItem(item, isShadowMode)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(expense: Double, income: Double, isShadowMode: Boolean) {
    val balance = income - expense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonPurple.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Balance", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        Text(
                            if (isShadowMode) "****" else "₹${String.format("%,.2f", balance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentTeal.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentTeal)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryItem("Expense", expense, Icons.Default.ArrowDownward, ErrorRed, isShadowMode, Modifier.weight(1f))
                    SummaryItem("Income", income, Icons.Default.ArrowUpward, SuccessGreen, isShadowMode, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    isShadowMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            Text(
                if (isShadowMode) "****" else "₹${String.format("%,.0f", amount)}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun TransactionItem(item: TransactionWithMemory, isShadowMode: Boolean) {
    val tx = item.transaction
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.vendor, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateFormat.format(Date(tx.timestamp)),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    if (item.previousAmount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Prev: ${tx.currency}${if (isShadowMode) "****" else String.format("%.0f", item.previousAmount)}",
                            fontSize = 10.sp,
                            color = AccentTeal,
                            modifier = Modifier
                                .background(AccentTeal.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                if (tx.latitude != null) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tagged with GPS", fontSize = 10.sp, color = AccentTeal)
                    }
                }
            }

            Text(
                "${if (tx.isDebit) "-" else "+"}${tx.currency}${if (isShadowMode) "****" else String.format("%.0f", tx.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (tx.isDebit) ErrorRed else SuccessGreen
            )
        }
    }
}
