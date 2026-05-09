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
import com.xpenseatlas.logic.ExportHelper
import com.xpenseatlas.logic.SmsScanner
import com.xpenseatlas.logic.UpiLauncher
import com.xpenseatlas.ui.screens.DashboardScreen
import com.xpenseatlas.ui.screens.ReportDialog
import com.xpenseatlas.ui.theme.XpenseAtlasTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MainActivity : FragmentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isUnlocked by mutableStateOf(false)
        showBiometricPrompt { isUnlocked = true }

        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Note: On Android 11+ (R), this might need to be requested separately, 
            // but adding it here ensures it's requested on Q and R properly where possible.
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        permissionLauncher.launch(permissions.toTypedArray())

        setContent {
            XpenseAtlasTheme {
                val db = AppDatabase.getDatabase(this)
                val coroutineScope = rememberCoroutineScope()

                // ── Month selection (start of month millis) ──────────────
                var selectedMonthStart by remember {
                    mutableLongStateOf(
                        Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    )
                }

                val selectedMonthEnd = remember(selectedMonthStart) {
                    Calendar.getInstance().apply {
                        timeInMillis = selectedMonthStart
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                }

                // ── Profile / UI state ───────────────────────────────────
                var isBusinessMode by remember { mutableStateOf(false) }
                var isShadowMode   by remember { mutableStateOf(false) }
                var showReport     by remember { mutableStateOf(false) }
                var searchQuery    by remember { mutableStateOf("") }

                // ── Past SMS scan state ──────────────────────────────────
                var isScanningPast by remember { mutableStateOf(false) }
                var scanProgress   by remember { mutableStateOf(0 to 0) }

                // ── Database flows ───────────────────────────────────────
                val monthlyDebits by db.transactionDao()
                    .getMonthlyDebits(selectedMonthStart, selectedMonthEnd, isBusinessMode)
                    .collectAsState(initial = 0.0)

                val monthlyCredits by db.transactionDao()
                    .getMonthlyCredits(selectedMonthStart, selectedMonthEnd, isBusinessMode)
                    .collectAsState(initial = 0.0)

                val monthTransactions by db.transactionDao()
                    .getTransactionsForMonth(selectedMonthStart, selectedMonthEnd, isBusinessMode)
                    .collectAsState(initial = emptyList())

                // For report dialog
                val categoryTotals by db.transactionDao()
                    .getSpendingByCategory(selectedMonthStart, selectedMonthEnd)
                    .collectAsState(initial = emptyList())
                val topMerchants by db.transactionDao()
                    .getTopMerchants(selectedMonthStart, selectedMonthEnd)
                    .collectAsState(initial = emptyList())

                // ── Month navigation helpers ─────────────────────────────
                fun changeMonth(delta: Int) {
                    selectedMonthStart = Calendar.getInstance().apply {
                        timeInMillis = selectedMonthStart
                        add(Calendar.MONTH, delta)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }

                fun scanPastSms(isFullSync: Boolean) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            isScanningPast = true
                            scanProgress = 0 to 0
                            
                            // 1. Fetch all transactions that have GPS data
                            val allExistingList = db.transactionDao().getTransactionsForPeriod(0).first()
                            val gpsCache = allExistingList.filter { it.latitude != null && it.longitude != null }
                                .associate { it.rawSms to (it.latitude to it.longitude) }

                            // 2. Clear data based on sync type
                            var startScanMillis: Long? = null
                            var endScanMillis: Long? = null
                            if (isFullSync) {
                                db.transactionDao().clearAllTransactions()
                            } else {
                                db.transactionDao().clearTransactionsForPeriod(selectedMonthStart, selectedMonthEnd)
                                startScanMillis = selectedMonthStart
                                endScanMillis = selectedMonthEnd
                            }

                            // 3. Scan Inbox
                            val found = SmsScanner.scanAllInbox(
                                context = this@MainActivity,
                                startMillis = startScanMillis,
                                endMillis = endScanMillis,
                                progressCallback = { cur, total -> scanProgress = cur to total }
                            )

                            // 4. Apply GPS Cache and Insert
                            if (found.isNotEmpty()) {
                                val merged = found.map { tx ->
                                    gpsCache[tx.rawSms]?.let { (lat, lng) ->
                                        tx.copy(latitude = lat, longitude = lng)
                                    } ?: tx
                                }
                                db.transactionDao().insertTransactions(merged)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isScanningPast = false
                            scanProgress = 0 to 0
                        }
                    }
                }

                if (isUnlocked) {
                    DashboardScreen(
                        monthlyDebits       = monthlyDebits ?: 0.0,
                        monthlyCredits      = monthlyCredits ?: 0.0,
                        monthTransactions   = monthTransactions,
                        selectedMonthStart  = selectedMonthStart,
                        onPrevMonth         = { changeMonth(-1) },
                        onNextMonth         = { changeMonth(+1) },
                        onMonthPicked       = { selectedMonthStart = it },
                        searchQuery         = searchQuery,
                        onSearchChanged     = { searchQuery = it },
                        isBusinessMode      = isBusinessMode,
                        onToggleBusinessMode = { isBusinessMode = it },
                        isShadowMode        = isShadowMode,
                        onToggleShadowMode  = { isShadowMode = !isShadowMode },
                        onShowReport        = { showReport = true },
                        onExport            = {
                            ExportHelper.exportToCsv(this@MainActivity, monthTransactions.map { it.transaction })
                        },
                        onExportDebug       = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val report = SmsScanner.generateDebugReport(this@MainActivity)
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    ExportHelper.exportDebugTxt(this@MainActivity, report)
                                }
                            }
                        },
                        onLaunchUpi         = { UpiLauncher.launchUpi(this@MainActivity) },
                        onSyncCurrentMonth  = { scanPastSms(isFullSync = false) },
                        onSyncAllTime       = { scanPastSms(isFullSync = true) },
                        onBlockVendor       = { vendor ->
                            val settings = com.xpenseatlas.logic.SettingsManager(this@MainActivity)
                            settings.addToBlocklist(vendor)
                            coroutineScope.launch(Dispatchers.IO) {
                                db.transactionDao().deleteTransactionsByVendor(vendor)
                            }
                        },
                        isScanningPast      = isScanningPast,
                        scanProgress        = scanProgress
                    )

                    if (showReport) {
                        ReportDialog(
                            categoryTotals = categoryTotals,
                            topMerchants   = topMerchants,
                            monthTransactions = monthTransactions,
                            selectedMonth  = Calendar.getInstance().apply { timeInMillis = selectedMonthStart },
                            onMonthChanged = { selectedMonthStart = it.timeInMillis },
                            onDismiss      = { showReport = false }
                        )
                    }
                }
            }
        }
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

        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vault Lock")
            .setSubtitle("Authenticate to enter the Atlas")
            .setNegativeButtonText("Exit")
            .build()
            .also { biometricPrompt.authenticate(it) }
    }
}
