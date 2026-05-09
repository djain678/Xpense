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
    // Better Regex: Look for amount that is actually being transacted
    // Matches: ₹ 500, Rs.500, INR 500, etc.
    private val AMOUNT_REGEX = Pattern.compile("(?i)(?:rs\\.?|inr|₹|\\$|£|€|aed)\\s*([\\d,]+\\.?\\d*)")
    
    // Keywords that indicate a REAL transaction occurred
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "sent", "payment", "withdrawn", "purchased")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "refunded", "added", "deposited")
    
    // Keywords that mean we should IGNORE the message (marketing, informational, loans)
    private val IGNORE_KEYWORDS = listOf(
        "available balance", "bal:", "limit", "loan", "pre-approved", "otp", 
        "outstanding", "invest", "bill due", "remind", "score", "insurance"
    )

    fun parse(sms: String): ParsedTransaction? {
        val cleanSms = sms.lowercase()
        
        // 1. Check for Ignore Keywords FIRST
        if (IGNORE_KEYWORDS.any { cleanSms.contains(it) }) {
            // Only proceed if it ALSO contains a debit/credit keyword (e.g. "loan amount credited")
            val hasAction = DEBIT_KEYWORDS.any { cleanSms.contains(it) } || 
                            CREDIT_KEYWORDS.any { cleanSms.contains(it) }
            if (!hasAction) return null
        }

        // 2. Extract Amount
        val matcher = AMOUNT_REGEX.matcher(sms)
        var bestAmount: Double? = null
        var bestCurrency: String? = null
        
        // We want the FIRST amount that appears near a transaction keyword
        while (matcher.find()) {
            val curr = matcher.group(1) ?: "₹"
            val amtStr = matcher.group(2)?.replace(",", "") ?: continue
            val amt = amtStr.toDoubleOrNull() ?: continue
            
            // Heuristic: If there are multiple numbers, the transacted amount is usually 
            // the one NOT described as "Balance" or "Limit"
            val start = matcher.start()
            val window = cleanSms.substring(Math.max(0, start - 20), start)
            if (!window.contains("bal") && !window.contains("lim")) {
                bestAmount = amt
                bestCurrency = curr
                break 
            }
            if (bestAmount == null) {
                bestAmount = amt
                bestCurrency = curr
            }
        }
        
        if (bestAmount == null || bestAmount <= 0) return null

        // 3. Determine Debit vs Credit
        val isDebit = DEBIT_KEYWORDS.any { cleanSms.contains(it) }
        val isCredit = CREDIT_KEYWORDS.any { cleanSms.contains(it) }
        
        // If it's explicitly credit, it's credit. Otherwise, if it has debit keywords, it's debit.
        // If it has NEITHER, we discard it as informational.
        val finalIsDebit = when {
            isCredit -> false
            isDebit -> true
            else -> return null // Discard informational messages with no clear action
        }

        // 4. Extract Vendor
        var vendor = "Unknown Merchant"
        val vendorPatterns = listOf(
            "(?i)at\\s+([a-z0-9.\\s&]+)", 
            "(?i)to\\s+([a-z0-9.\\s&]+)", 
            "(?i)vpa\\s+([a-z0-9@.\\s&]+)",
            "(?i)info[:\\s]+([a-z0-9.\\s&]+)"
        )
        
        for (patternStr in vendorPatterns) {
            val vMatcher = Pattern.compile(patternStr).matcher(sms)
            if (vMatcher.find()) {
                val found = vMatcher.group(1)?.trim() ?: ""
                if (found.isNotEmpty() && found.length > 2) {
                    vendor = found.split(" on ")[0].split(" ref ")[0].trim()
                    break
                }
            }
        }

        return ParsedTransaction(
            amount = bestAmount,
            currency = (bestCurrency ?: "₹").uppercase().replace("RS.", "₹").replace("RS", "₹").replace("INR", "₹"),
            vendor = vendor.uppercase(),
            isDebit = finalIsDebit,
            category = autoCategorize(vendor)
        )
    }

    private fun autoCategorize(vendor: String): String {
        val v = vendor.lowercase()
        return when {
            v.contains("swiggy") || v.contains("zomato") || v.contains("eats") || v.contains("rest") -> "Food & Dining"
            v.contains("uber") || v.contains("ola") || v.contains("metro") || v.contains("railway") || v.contains("petrol") -> "Transport"
            v.contains("amazon") || v.contains("flipkart") || v.contains("myntra") || v.contains("mall") -> "Shopping"
            v.contains("airtel") || v.contains("jio") || v.contains("vi ") || v.contains("recharge") || v.contains("bill") -> "Bills"
            v.contains("netflix") || v.contains("prime") || v.contains("hotstar") || v.contains("theatre") -> "Entertainment"
            v.contains("hospital") || v.contains("pharmacy") || v.contains("med") -> "Health"
            else -> "General"
        }
    }
}
