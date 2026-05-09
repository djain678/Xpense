package com.xpenseatlas.logic

import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.xpenseatlas.data.Transaction

object SmsScanner {
    fun scanAllInbox(
        context: Context,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): List<Transaction> {
        val results = mutableListOf<Transaction>()
        val seen = mutableSetOf<String>()

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.ADDRESS),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return emptyList()

        val total = cursor.count
        var current = 0

        cursor.use { c ->
            val bodyIdx    = c.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx    = c.getColumnIndex(Telephony.Sms.DATE)
            val addressIdx = c.getColumnIndex(Telephony.Sms.ADDRESS)

            while (c.moveToNext()) {
                current++
                onProgress(current, total)

                try {
                    val body    = c.getString(bodyIdx) ?: continue
                    val date    = c.getLong(dateIdx)
                    val address = c.getString(addressIdx) ?: ""

                    // Only bank/UPI alphanumeric senders
                    if (!address.any { it.isLetter() }) continue

                    val key = "$date:${body.take(20)}"
                    if (key in seen) continue
                    seen.add(key)

                    val parsed = SmsParser.parse(body)
                    if (parsed != null) {
                        results.add(
                            Transaction(
                                amount    = parsed.amount,
                                vendor    = parsed.vendor,
                                category  = parsed.category,
                                isDebit   = parsed.isDebit,
                                currency  = parsed.currency,
                                timestamp = date,
                                latitude  = null,
                                longitude = null,
                                rawSms    = body
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("XpenseAtlas", "Failed to process SMS at index $current: ${e.message}")
                    // Continue to next message
                }
            }
        }
        Log.d("XpenseAtlas", "Scan complete. Found ${results.size} transactions.")
        return results
    }
}
