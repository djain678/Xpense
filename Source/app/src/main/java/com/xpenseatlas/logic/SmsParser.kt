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
    private val AMOUNT_REGEX = Pattern.compile("(?i)(?:rs\\.?|inr|₹|\\$|£|€|aed)\\s*([\\d,]+\\.?\\d*)")
    
    // Broad list of keywords for any transaction in Indian context
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "sent", "payment", "withdrawn", "purchased", "txn", "trf", "vpa")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "refunded", "added", "deposited", "incoming")
    
    // Only ignore if it is EXCLUSIVELY an information message with no transaction intent
    private val INFO_ONLY_KEYWORDS = listOf("otp", "pre-approved", "remind", "score", "insurance", "outstanding")

    fun parse(sms: String): ParsedTransaction? {
        val cleanSms = sms.lowercase()
        
        // 1. Hard-discard pure spam/info
        if (INFO_ONLY_KEYWORDS.any { cleanSms.contains(it) }) return null

        // 2. Extract Amount
        val matcher = AMOUNT_REGEX.matcher(sms)
        var bestAmount: Double? = null
        var bestCurrency: String? = null
        
        while (matcher.find()) {
            val curr = matcher.group(1) ?: "₹"
            val amtStr = matcher.group(2)?.replace(",", "") ?: continue
            val amt = amtStr.toDoubleOrNull() ?: continue
            
            // Heuristic: The first number that ISN'T preceded by "balance" or "bal" is the transaction
            val start = matcher.start()
            val window = cleanSms.substring(Math.max(0, start - 15), start)
            if (!window.contains("bal") && !window.contains("limit")) {
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
        
        // Smarter Logic:
        // - If it mentions "Loan", "Balance", or "Limit" but NO transaction keywords -> Info only (ignore)
        if ((cleanSms.contains("loan") || cleanSms.contains("balance") || cleanSms.contains("limit")) && !isDebit && !isCredit) {
            return null
        }

        val finalIsDebit = when {
            isCredit -> false
            isDebit -> true
            else -> true // Fallback to Debit for safety if an amount was found and it's not credit
        }

        // 4. Extract Vendor
        var vendor = "Unknown Merchant"
        val vendorPatterns = listOf(
            "(?i)at\\s+([a-z0-9.\\s&]+)", 
            "(?i)to\\s+([a-z0-9.\\s&]+)", 
            "(?i)vpa\\s+([a-z0-9@.\\s&]+)",
            "(?i)info[:\\s]+([a-z0-9.\\s&]+)",
            "(?i)towards\\s+([a-z0-9.\\s&]+)"
        )
        
        for (patternStr in vendorPatterns) {
            val vMatcher = Pattern.compile(patternStr).matcher(sms)
            if (vMatcher.find()) {
                val found = vMatcher.group(1)?.trim() ?: ""
                if (found.isNotEmpty() && found.length > 2) {
                    vendor = found.split(" on ")[0].split(" ref ")[0].split(" ref:")[0].trim()
                    break
                }
            }
        }

        // Fallback for vendor: if still unknown, try to find the sender ID (if passed) or just use the first capitalized word
        if (vendor == "Unknown Merchant") {
            val words = sms.split(" ").filter { it.length > 3 && it.any { c -> c.isLetter() } }
            vendor = words.firstOrNull { it.any { c -> c.isUpperCase() } } ?: "MANUAL"
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
