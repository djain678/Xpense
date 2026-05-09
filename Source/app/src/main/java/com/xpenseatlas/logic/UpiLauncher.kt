package com.xpenseatlas.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpiLauncher {
    fun launchUpi(context: Context) {
        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
        val pm = context.packageManager
        val upiActivities = pm.queryIntentActivities(upiIntent, 0)
        
        if (upiActivities.isEmpty()) {
            Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
            return
        }

        // Filter to unique package names to avoid duplicates
        val upiPackages = upiActivities.map { it.activityInfo.packageName }.distinct()
        
        if (upiPackages.size == 1) {
            // Only one app, open it directly
            val launchIntent = pm.getLaunchIntentForPackage(upiPackages[0])
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        } else {
            // Multiple apps, show a chooser for their main launcher activities
            val intents = upiPackages.mapNotNull { pkg ->
                pm.getLaunchIntentForPackage(pkg)?.apply {
                    // Set label for the chooser
                    val appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0))
                    putExtra(Intent.EXTRA_TITLE, appLabel)
                }
            }
            
            if (intents.isNotEmpty()) {
                val chooser = Intent.createChooser(intents[0], "Open UPI App")
                // Add the rest as initial intents
                if (intents.size > 1) {
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
                }
                context.startActivity(chooser)
            }
        }
    }
}
