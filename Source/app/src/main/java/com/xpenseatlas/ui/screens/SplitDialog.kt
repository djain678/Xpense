package com.xpenseatlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xpenseatlas.ui.theme.*

@Composable
fun SplitDialog(
    amount: Double,
    onDismiss: () -> Unit
) {
    var personCount by remember { mutableStateOf(2) }
    val splitAmount = amount / personCount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Split Bill", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Total: ₹${String.format("%.2f", amount)}", color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (personCount > 1) personCount-- }) {
                        Text("-", color = AccentTeal, fontSize = 24.sp)
                    }
                    Text("$personCount People", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White, fontSize = 18.sp)
                    IconButton(onClick = { personCount++ }) {
                        Text("+", color = AccentTeal, fontSize = 24.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("Each Pays", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text("₹${String.format("%.2f", splitAmount)}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Done")
                }
            }
        }
    }
}
