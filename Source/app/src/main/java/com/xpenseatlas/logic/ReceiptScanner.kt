package com.xpenseatlas.logic

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OCRResult(
    val amount: Double?,
    val vendor: String?
)

object ReceiptScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanReceipt(bitmap: Bitmap): OCRResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            val text = result.text
            
            // Basic logic: Find highest number (likely amount) and first capitalized word (likely vendor)
            val lines = result.textBlocks.flatMap { it.lines }.map { it.text }
            
            val amountRegex = Regex("(\\d+[.,]\\d{2})")
            val amounts = amountRegex.findAll(text).map { it.value.replace(",", ".").toDouble() }.toList()
            val maxAmount = amounts.maxOrNull()
            
            val vendor = lines.firstOrNull { it.length > 3 && it.any { c -> c.isUpperCase() } } ?: "Manual Receipt"

            OCRResult(maxAmount, vendor)
        } catch (e: Exception) {
            OCRResult(null, null)
        }
    }
}
