package com.xpenseatlas.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import android.util.Log

object LocationHelper {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        return try {
            // 1. Try to get last location first if it's "fresh" (within 5 minutes)
            val lastLoc = fusedLocationClient.lastLocation.await()
            if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 5 * 60 * 1000) {
                Log.d("XpenseAtlas", "Using fresh cached location")
                return lastLoc
            }

            // 2. Request fresh location with a 8s timeout (BroadcastReceiver has 10s total)
            Log.d("XpenseAtlas", "Requesting high-accuracy GPS...")
            val freshLoc = withTimeoutOrNull(8000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            }
            
            freshLoc ?: lastLoc
        } catch (e: Exception) {
            Log.e("XpenseAtlas", "GPS Error: ${e.message}")
            try {
                fusedLocationClient.lastLocation.await()
            } catch (e2: Exception) {
                null
            }
        }
    }
}
