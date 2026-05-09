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
    // Matches currency followed by numbers: ₹500, Rs. 500, INR 500.00
    private val AMOUNT_REGEX = Pattern.compile("(?i)(rs\\.?|inr|₹|\\$|£|€|aed)\\s*([\\d,]+\\.?\\d*)")
    
    // Broad list of keywords for any transaction in Indian context
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "sent", "payment", "withdrawn", "purchased", "txn", "trf", "vpa", "towards")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "refunded", "added", "deposited", "incoming", "reversal")
    
    // Hard-discard only absolute non-financial noise
    private val NOISE_KEYWORDS = listOf("otp", "pre-approved", "score", "insurance")

    fun parse(sms: String, customBlocklist: Set<String> = emptySet()): ParsedTransaction? {
        val cleanSms = sms.lowercase()
        
        // 1. Hard-discard noise and custom blocklist
        if (NOISE_KEYWORDS.any { cleanSms.contains(it) }) return null
        if (customBlocklist.any { cleanSms.contains(it) }) return null

        // 2. Extract Amount
        val matcher = AMOUNT_REGEX.matcher(sms)
        var bestAmount: Double? = null
        var bestCurrency: String? = null
        
        while (matcher.find()) {
            val curr = matcher.group(1) ?: "₹"
            val amtStr = matcher.group(2)?.replace(",", "") ?: continue
            val amt = amtStr.toDoubleOrNull() ?: continue
            
            // Heuristic: Prefer the number that is NOT the balance
            val start = matcher.start()
            val windowBefore = cleanSms.substring(Math.max(0, start - 15), start)
            if (!windowBefore.contains("bal") && !windowBefore.contains("limit")) {
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
        
        // If it's explicitly credit, it's credit.
        // If it's not credit but has debit keywords, it's debit.
        // If it has NO keywords but we found an amount, it's likely a transaction we should catch.
        val finalIsDebit = when {
            isCredit -> false
            isDebit -> true
            else -> true // Default to Debit if we found a currency amount in a bank-like message
        }

        // 4. Extract Vendor
        var vendor = "Unknown Merchant"
        val vendorPatterns = listOf(
            "(?i)at\\s+([a-z0-9.\\s&@]+)", 
            "(?i)to\\s+([a-z0-9.\\s&@]+)", 
            "(?i)vpa\\s+([a-z0-9@.\\s&]+)",
            "(?i)info[:\\s]+([a-z0-9.\\s&]+)",
            "(?i)towards\\s+([a-z0-9.\\s&]+)",
            "(?i)ref[:\\s]*([a-z0-9.\\s&]+)"
        )
        
        for (patternStr in vendorPatterns) {
            val vMatcher = Pattern.compile(patternStr).matcher(sms)
            if (vMatcher.find()) {
                val found = vMatcher.group(1)?.trim() ?: ""
                if (found.isNotEmpty() && found.length > 2) {
                    // Clean up: stop at "on", "ref", "dated", etc.
                    vendor = found.split(" on ")[0]
                                 .split(" ref")[0]
                                 .split(" dated")[0]
                                 .split(" avbl")[0]
                                 .trim()
                    break
                }
            }
        }

        // Fallback for vendor
        if (vendor == "Unknown Merchant") {
            val words = sms.split(" ").filter { it.length > 3 && it.any { c -> c.isLetter() } }
            vendor = words.firstOrNull { it.any { c -> c.isUpperCase() } } ?: "TRANSACTION"
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
            v.contains("uber") || v.contains("ola") || v.contains("metro") || v.contains("railway") || v.contains("petrol") || v.contains("shell") -> "Transport"
            v.contains("amazon") || v.contains("flipkart") || v.contains("myntra") || v.contains("mall") || v.contains("mart") -> "Shopping"
            v.contains("airtel") || v.contains("jio") || v.contains("vi ") || v.contains("recharge") || v.contains("bill") -> "Bills"
            v.contains("netflix") || v.contains("prime") || v.contains("hotstar") || v.contains("theatre") || v.contains("spotify") -> "Entertainment"
            v.contains("hospital") || v.contains("pharmacy") || v.contains("med") || v.contains("clinic") -> "Health"
            else -> "General"
        }
    }
}
