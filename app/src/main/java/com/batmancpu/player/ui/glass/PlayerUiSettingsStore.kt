package com.batmancpu.player.ui.glass

import android.content.Context

/** Local visual preferences only; these are not metadata or media-library settings. */
internal class PlayerUiSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun glassEnabled(): Boolean = preferences.getBoolean(GLASS_ENABLED_KEY, true)

    fun setGlassEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(GLASS_ENABLED_KEY, enabled).apply()
    }

    internal companion object {
        const val PREFERENCES_NAME = "player_interface_settings"
        const val GLASS_ENABLED_KEY = "glass_enabled"
    }
}
