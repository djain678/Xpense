package com.xpenseatlas.logic

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("xpenseatlas_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BLOCKLIST = "blocklist_keywords"
    }

    fun getBlocklist(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKLIST, emptySet()) ?: emptySet()
    }

    fun addToBlocklist(keyword: String) {
        val current = getBlocklist().toMutableSet()
        current.add(keyword.lowercase().trim())
        prefs.edit().putStringSet(KEY_BLOCKLIST, current).apply()
    }

    fun removeFromBlocklist(keyword: String) {
        val current = getBlocklist().toMutableSet()
        current.remove(keyword.lowercase().trim())
        prefs.edit().putStringSet(KEY_BLOCKLIST, current).apply()
    }
}
