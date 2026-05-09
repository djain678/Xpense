package com.xpenseatlas.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.xpenseatlas.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.*

class ReportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        
        val totalSpent = db.transactionDao().getTotalExpenses().first() ?: 0.0
        
        if (totalSpent > 0) {
            showNotification(totalSpent)
        }
        
        return Result.success()
    }

    private fun showNotification(amount: Double) {
        val channelId = "reports"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Monthly Reports", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Monthly Xpense Report")
            .setContentText("You spent ₹${String.format("%.0f", amount)} this month. Tap to see your full Atlas.")
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(101, notification)
    }
}
