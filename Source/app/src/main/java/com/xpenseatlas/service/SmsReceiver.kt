package com.xpenseatlas.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.xpenseatlas.data.AppDatabase
import com.xpenseatlas.data.Transaction
import com.xpenseatlas.logic.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullMessage = messages.joinToString("") { it.messageBody }
            val sender = messages.firstOrNull()?.originatingAddress ?: "Unknown"
            val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

            if (sender.any { it.isLetter() }) {
                scope.launch {
                    try {
                        processSms(context, fullMessage, timestamp)
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processSms(context: Context, body: String, timestamp: Long) {
        val parsed = SmsParser.parse(body) ?: return
        
        val db = AppDatabase.getDatabase(context)
        val learnedCategory = db.transactionDao().getCategoryForVendor(parsed.vendor.uppercase())
        
        val location = LocationHelper.getCurrentLocation(context)
        
        val transaction = Transaction(
            amount = parsed.amount,
            vendor = parsed.vendor,
            category = learnedCategory ?: parsed.category,
            isDebit = parsed.isDebit,
            currency = parsed.currency,
            timestamp = timestamp,
            latitude = location?.latitude,
            longitude = location?.longitude,
            rawSms = body
        )

        db.transactionDao().insertTransaction(transaction)
        Log.d("XpenseAtlas", "Saved live transaction: ${parsed.amount} at ${parsed.vendor}")
    }
}
