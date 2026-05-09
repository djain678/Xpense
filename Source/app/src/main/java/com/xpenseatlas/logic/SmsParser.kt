package com.xpenseatlas.logic

import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Double,
    val currency: String,
    val vendor: String,
    val isDebit: Boolean,
    val category: String
)

object SmsParser {
    // Regex for money (captures symbols like Rs, ₹, INR, $, £, €, AED)
    private val AMOUNT_REGEX = Pattern.compile("(?i)(Rs\\.|Rs|INR|₹|\\$|£|€|AED)\\s*([\\d,]+\\.?\\d*)")
    
    // Keywords for Debit vs Credit
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "sent to", "withdrawn")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "refunded", "added")

    fun parse(sms: String): ParsedTransaction? {
        val cleanSms = sms.lowercase()
        
        // 1. Extract Amount and Currency
        val matcher = AMOUNT_REGEX.matcher(sms)
        if (!matcher.find()) return null
        
        val currency = matcher.group(1) ?: "₹"
        val amountStr = matcher.group(2)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        
        // 2. Determine Debit vs Credit
        val isDebit = DEBIT_KEYWORDS.any { cleanSms.contains(it) }
        val isCredit = CREDIT_KEYWORDS.any { cleanSms.contains(it) }
        
        // If it's neither or both (rare), assume debit as it's an expense tracker
        val finalIsDebit = if (isDebit) true else !isCredit

        // 3. Extract Vendor (Basic logic: find string after 'at' or 'to')
        var vendor = "Unknown Merchant"
        val vendorPatterns = listOf("at\\s+([a-z0-9\\s&]+)", "to\\s+([a-z0-9\\s&]+)", "info[:\\s]+([a-z0-9\\s&]+)")
        
        for (patternStr in vendorPatterns) {
            val vMatcher = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE).matcher(sms)
            if (vMatcher.find()) {
                vendor = vMatcher.group(1)?.trim() ?: "Unknown"
                break
            }
        }

        // 4. Basic Categorization
        val category = autoCategorize(vendor)

        return ParsedTransaction(
            amount = amount,
            currency = currency.uppercase().replace("RS.", "₹").replace("RS", "₹").replace("INR", "₹"),
            vendor = vendor.uppercase(),
            isDebit = finalIsDebit,
            category = category
        )
    }

    private fun autoCategorize(vendor: String): String {
        val v = vendor.lowercase()
        return when {
            v.contains("swiggy") || v.contains("zomato") || v.contains("eats") -> "Food & Dining"
            v.contains("uber") || v.contains("ola") || v.contains("metro") || v.contains("railway") -> "Transport"
            v.contains("amazon") || v.contains("flipkart") || v.contains("myntra") -> "Shopping"
            v.contains("airtel") || v.contains("jio") || v.contains("vodafone") || v.contains("recharge") -> "Bills"
            v.contains("netflix") || v.contains("prime") || v.contains("hotstar") -> "Entertainment"
            else -> "General"
        }
    }
}
