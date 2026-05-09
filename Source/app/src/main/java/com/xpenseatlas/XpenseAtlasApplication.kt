package com.xpenseatlas

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.xpenseatlas.service.ReportWorker
import java.util.concurrent.TimeUnit

class XpenseAtlasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleMonthlyReport()
    }

    private fun scheduleMonthlyReport() {
        // Schedule to run once every 30 days
        val reportRequest = PeriodicWorkRequestBuilder<ReportWorker>(30, TimeUnit.DAYS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "monthly_report",
            ExistingPeriodicWorkPolicy.KEEP,
            reportRequest
        )
    }
}
