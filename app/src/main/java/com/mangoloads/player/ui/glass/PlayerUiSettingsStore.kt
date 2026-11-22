package com.mangoloads.player.ui.glass

import android.content.Context

/** Local visual preferences only; these are not metadata or media-library settings. */
internal class PlayerUiSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun glassEnabled(): Boolean = preferences.getBoolean(GLASS_ENABLED_KEY, true)

    fun setGlassEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(GLASS_ENABLED_KEY, enabled).apply()
    }

    fun captionTextSize(): Float = preferences.getFloat(CAPTION_TEXT_SIZE_KEY, DEFAULT_CAPTION_TEXT_SIZE)
        .coerceIn(MIN_CAPTION_TEXT_SIZE, MAX_CAPTION_TEXT_SIZE)

    fun setCaptionTextSize(size: Float) {
        preferences.edit().putFloat(CAPTION_TEXT_SIZE_KEY, size.coerceIn(MIN_CAPTION_TEXT_SIZE, MAX_CAPTION_TEXT_SIZE)).apply()
    }

    fun captionBottomPadding(): Float = preferences.getFloat(CAPTION_BOTTOM_PADDING_KEY, DEFAULT_CAPTION_BOTTOM_PADDING)
        .coerceIn(MIN_CAPTION_BOTTOM_PADDING, MAX_CAPTION_BOTTOM_PADDING)

    fun setCaptionBottomPadding(padding: Float) {
        preferences.edit().putFloat(CAPTION_BOTTOM_PADDING_KEY, padding.coerceIn(MIN_CAPTION_BOTTOM_PADDING, MAX_CAPTION_BOTTOM_PADDING)).apply()
    }

    internal companion object {
        const val PREFERENCES_NAME = "player_interface_settings"
        const val GLASS_ENABLED_KEY = "glass_enabled"
        const val CAPTION_TEXT_SIZE_KEY = "caption_text_size"
        const val CAPTION_BOTTOM_PADDING_KEY = "caption_bottom_padding"
        const val DEFAULT_CAPTION_TEXT_SIZE = 0.0533f
        const val MIN_CAPTION_TEXT_SIZE = 0.035f
        const val MAX_CAPTION_TEXT_SIZE = 0.09f
        const val DEFAULT_CAPTION_BOTTOM_PADDING = 0.08f
        const val MIN_CAPTION_BOTTOM_PADDING = 0f
        const val MAX_CAPTION_BOTTOM_PADDING = 0.28f
    }
}
