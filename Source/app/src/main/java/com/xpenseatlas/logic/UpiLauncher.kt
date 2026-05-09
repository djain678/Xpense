package com.xpenseatlas.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpiLauncher {
    fun launchUpi(context: Context) {
        // Standard UPI intent to let user pick their preferred app
        val uri = Uri.parse("upi://pay")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        val chooser = Intent.createChooser(intent, "Pay with...")
        
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }
}
