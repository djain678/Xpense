package com.xpenseatlas

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.xpenseatlas.data.AppDatabase
import com.xpenseatlas.data.Transaction
import com.xpenseatlas.logic.ExportHelper
import com.xpenseatlas.logic.UpiLauncher
import com.xpenseatlas.ui.screens.DashboardScreen
import com.xpenseatlas.ui.theme.XpenseAtlasTheme
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

class MainActivity : FragmentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var isUnlocked by mutableStateOf(false)

        showBiometricPrompt {
            isUnlocked = true
        }

        setContent {
            XpenseAtlasTheme {
                val db = AppDatabase.getDatabase(this)
                val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())
                val totalExpense by db.transactionDao().getTotalExpenses().collectAsState(initial = 0.0)
                val totalIncome by db.transactionDao().getTotalIncome().collectAsState(initial = 0.0)

                // Monthly Reporting State
                var showReport by remember { mutableStateOf(false) }
                var reportCalendar by remember { mutableStateOf(Calendar.getInstance()) }
                
                val startOfPeriod = (reportCalendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                
                val endOfPeriod = (reportCalendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis

                val categoryTotals by db.transactionDao().getSpendingByCategory(startOfPeriod, endOfPeriod).collectAsState(initial = emptyList())
                val topMerchants by db.transactionDao().getTopMerchants(startOfPeriod, endOfPeriod).collectAsState(initial = emptyList())

                var searchQuery by remember { mutableStateOf("") }
                var isBusinessMode by remember { mutableStateOf(false) }
                var isShadowMode by remember { mutableStateOf(false) }

                if (isUnlocked) {
                    DashboardScreen(
                        transactions = transactions,
                        totalExpense = totalExpense ?: 0.0,
                        totalIncome = totalIncome ?: 0.0,
                        searchQuery = searchQuery,
                        onSearchChanged = { searchQuery = it },
                        isBusinessMode = isBusinessMode,
                        onToggleBusinessMode = { isBusinessMode = it },
                        isShadowMode = isShadowMode,
                        onToggleShadowMode = { isShadowMode = !isShadowMode },
                        onShowReport = { showReport = true },
                        onExport = { ExportHelper.exportToCsv(this, transactions.map { it.transaction }) },
                        onLaunchUpi = { UpiLauncher.launchUpi(this) }
                    )

                    if (showReport) {
                        com.xpenseatlas.ui.screens.ReportDialog(
                            categoryTotals = categoryTotals,
                            topMerchants = topMerchants,
                            selectedMonth = reportCalendar,
                            onMonthChanged = { reportCalendar = it },
                            onDismiss = { showReport = false }
                        )
                    }
                }
            }
        }
        
        // Request essential permissions on launch
        permissionLauncher.launch(arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vault Lock")
            .setSubtitle("Authenticate to access your Atlas")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
