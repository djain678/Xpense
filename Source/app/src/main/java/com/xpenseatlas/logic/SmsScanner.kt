package com.xpenseatlas.logic

import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.xpenseatlas.data.Transaction
import com.xpenseatlas.logic.SettingsManager

object SmsScanner {
    fun scanAllInbox(
        context: Context, 
        startMillis: Long? = null,
        endMillis: Long? = null,
        progressCallback: (Int, Int) -> Unit
    ): List<Transaction> {
        val blocklist = SettingsManager(context).getBlocklist()
        val results = mutableListOf<Transaction>()
        
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (startMillis != null && endMillis != null) {
            selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.DATE} <= ?"
            selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        }

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE),
            selection, selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return emptyList()

        var totalCount = 0
        var processedCount = 0

        cursor.use { c ->
            totalCount = c.count
            val bodyIdx    = c.getColumnIndex(Telephony.Sms.BODY)
            val addressIdx = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val dateIdx    = c.getColumnIndex(Telephony.Sms.DATE)

            while (c.moveToNext()) {
                processedCount++
                if (processedCount % 50 == 0) progressCallback(processedCount, totalCount)

                try {
                    val body = c.getString(bodyIdx) ?: continue
                    val address = c.getString(addressIdx) ?: ""
                    
                    // Only bank/UPI alphanumeric senders
                    if (!address.any { it.isLetter() }) continue

                    val parsed = SmsParser.parse(body, blocklist)
                    if (parsed != null) {
                        results.add(
                            Transaction(
                                amount = parsed.amount,
                                vendor = parsed.vendor,
                                category = parsed.category,
                                isDebit = parsed.isDebit,
                                currency = parsed.currency,
                                timestamp = c.getLong(dateIdx),
                                latitude = null,
                                longitude = null,
                                rawSms = body
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("XpenseAtlas", "Error parsing SMS: ${e.message}")
                }
            }
        }
        Log.d("XpenseAtlas", "Scan complete. Found ${results.size} transactions.")
        return results
    }

    fun getSmartBlockSuggestions(context: Context): List<String> {
        val promoKeywords = listOf("OFFER", "LOAN", "APPLY", "DISCOUNT", "CASHBACK", "REWARDS", "INVEST", "CREDIT CARD", "JEWELS", "SALE")
        val blocklist = SettingsManager(context).getBlocklist()
        val suggestions = mutableMapOf<String, Int>()

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null, null,
            "${Telephony.Sms.DATE} DESC LIMIT 200"
        ) ?: return emptyList()

        cursor.use {
            while (it.moveToNext()) {
                val body = it.getString(0).uppercase()
                // If it's NOT a transaction but contains promo keywords
                if (SmsParser.parse(body) == null) {
                    promoKeywords.forEach { keyword ->
                        if (body.contains(keyword) && !blocklist.contains(keyword)) {
                            suggestions[keyword] = suggestions.getOrDefault(keyword, 0) + 1
                        }
                    }
                }
            }
        }

        return suggestions.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(5)
    }

    fun generateDebugReport(context: Context): String {
        val blocklist = SettingsManager(context).getBlocklist()
        val sb = java.lang.StringBuilder()
        sb.append("--- XPENSE ATLAS PARSER DEBUG REPORT ---\n")
        
        sb.append("PERMISSIONS:\n")
        val permissions = listOf(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            "android.permission.ACCESS_BACKGROUND_LOCATION"
        )
        permissions.forEach { perm ->
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED
            sb.append("- ${perm.substringAfterLast(".")}: ${if (granted) "✅ GRANTED" else "❌ DENIED"}\n")
        }
        sb.append("\n")
        
        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE),
            null, null,
            "${Telephony.Sms.DATE} DESC LIMIT 100"
        ) ?: return "Failed to read SMS."

        var checked = 0
        var parsedCount = 0
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", java.util.Locale.getDefault())

        cursor.use { c ->
            val bodyIdx    = c.getColumnIndex(Telephony.Sms.BODY)
            val addressIdx = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val dateIdx    = c.getColumnIndex(Telephony.Sms.DATE)

            while (c.moveToNext()) {
                try {
                    val body = c.getString(bodyIdx) ?: continue
                    val address = c.getString(addressIdx) ?: ""
                    val timestamp = c.getLong(dateIdx)
                    val dateStr = dateFormat.format(java.util.Date(timestamp))
                    
                    checked++
                    val parsed = SmsParser.parse(body, blocklist)
                    
                    sb.append("DATE: $dateStr\n")
                    sb.append("SENDER: $address\n")
                    sb.append("BODY: $body\n")
                    if (parsed != null) {
                        parsedCount++
                        sb.append("RESULT: [ACCEPTED] ${parsed.currency}${parsed.amount} | ${parsed.vendor} | ${if(parsed.isDebit) "DEBIT" else "CREDIT"}\n")
                    } else {
                        sb.append("RESULT: [REJECTED]\n")
                    }
                    sb.append("--------------------------------------------------\n")
                } catch (e: Exception) {
                    sb.append("RESULT: [CRASHED] ${e.message}\n")
                    sb.append("--------------------------------------------------\n")
                }
            }
        }
        sb.insert(0, "Total Checked: $checked | Successfully Parsed: $parsedCount\n\n")
        return sb.toString()
    }
}
