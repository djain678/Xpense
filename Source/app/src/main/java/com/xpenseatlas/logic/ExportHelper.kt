package com.xpenseatlas.logic

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.xpenseatlas.data.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {
    fun exportToCsv(context: Context, transactions: List<Transaction>) {
        val fileName = "XpenseAtlas_Export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        val header = "ID,Date,Vendor,Amount,Type,Category,Latitude,Longitude\n"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        val content = StringBuilder(header)
        for (tx in transactions) {
            content.append("${tx.id},")
            content.append("${dateFormat.format(Date(tx.timestamp))},")
            content.append("${tx.vendor.replace(",", " ")},")
            content.append("${tx.amount},")
            content.append("${if (tx.isDebit) "DEBIT" else "CREDIT"},")
            content.append("${tx.category},")
            content.append("${tx.latitude ?: ""},")
            content.append("${tx.longitude ?: ""}\n")
        }
        
        file.writeText(content.toString())
        shareFile(context, file)
    }

    private fun shareFile(context: Context, file: File, mimeType: String = "text/csv") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    fun exportDebugTxt(context: Context, content: String) {
        val fileName = "XpenseAtlas_Debug_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        file.writeText(content)
        shareFile(context, file, "text/plain")
    }
}
