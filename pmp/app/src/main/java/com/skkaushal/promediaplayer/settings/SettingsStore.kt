package com.skkaushal.promediaplayer.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("player_settings")

class SettingsStore(private val context: Context) {
    private val dark = booleanPreferencesKey("dark_mode")
    private val autoFullscreen = booleanPreferencesKey("auto_fullscreen")
    private val speed = floatPreferencesKey("default_speed")
    val darkMode = context.store.data.map { it[dark] ?: false }
    val autoFullScreen = context.store.data.map { it[autoFullscreen] ?: true }
    val defaultSpeed = context.store.data.map { it[speed] ?: 1f }
}
