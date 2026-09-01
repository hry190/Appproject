package com.jueqiao.jianghu.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class SettingsSnapshot(
    val messageEnabled: Boolean = true,
    val learningReminder: Boolean = true,
    val workUpdates: Boolean = true,
    val serviceMessages: Boolean = true,
    val quietHours: Boolean = false,
    val autoSave: Boolean = true,
    val wifiOnly: Boolean = true,
    val hapticFeedback: Boolean = true,
    val largeText: Boolean = false,
    val soundEnabled: Boolean = true,
    val musicVolume: Float = 0.65f,
    val effectVolume: Float = 0.8f,
)

/**
 * Local source of truth for settings that must work while the device is offline.
 *
 * The server synchronization layer uses [isSyncDirty] to decide whether local
 * changes should be uploaded or a fresh cloud snapshot can be applied safely.
 */
class SettingsPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE,
    )

    val snapshots: Flow<SettingsSnapshot> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSnapshot())
        }
        trySend(readSnapshot())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun readSnapshot(): SettingsSnapshot = SettingsSnapshot(
        messageEnabled = getBoolean(Keys.MessageEnabled, true),
        learningReminder = getBoolean(Keys.LearningReminder, true),
        workUpdates = getBoolean(Keys.WorkUpdates, true),
        serviceMessages = getBoolean(Keys.ServiceMessages, true),
        quietHours = getBoolean(Keys.QuietHours, false),
        autoSave = getBoolean(Keys.AutoSave, true),
        wifiOnly = getBoolean(Keys.WifiOnly, true),
        hapticFeedback = getBoolean(Keys.HapticFeedback, true),
        largeText = getBoolean(Keys.LargeText, false),
        soundEnabled = getBoolean(Keys.SoundEnabled, true),
        musicVolume = getFloat(Keys.MusicVolume, 0.65f),
        effectVolume = getFloat(Keys.EffectVolume, 0.8f),
    )

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean, markDirty: Boolean = true) {
        preferences.edit()
            .putBoolean(key, value)
            .putBoolean(Keys.SyncDirty, markDirty || isSyncDirty())
            .apply()
    }

    fun getFloat(key: String, defaultValue: Float): Float =
        preferences.getFloat(key, defaultValue)

    fun putFloat(key: String, value: Float, markDirty: Boolean = true) {
        preferences.edit()
            .putFloat(key, value.coerceIn(0f, 1f))
            .putBoolean(Keys.SyncDirty, markDirty || isSyncDirty())
            .apply()
    }

    fun replaceFromCloud(snapshot: SettingsSnapshot) {
        preferences.edit()
            .putSnapshot(snapshot)
            .putBoolean(Keys.SyncDirty, false)
            .apply()
    }

    fun markSynced() {
        preferences.edit().putBoolean(Keys.SyncDirty, false).apply()
    }

    fun isSyncDirty(): Boolean = preferences.getBoolean(Keys.SyncDirty, false)

    fun restoreDefaults() {
        preferences.edit()
            .clear()
            .putSnapshot(SettingsSnapshot())
            .putBoolean(Keys.SyncDirty, true)
            .apply()
    }

    private fun SharedPreferences.Editor.putSnapshot(snapshot: SettingsSnapshot) =
        putBoolean(Keys.MessageEnabled, snapshot.messageEnabled)
            .putBoolean(Keys.LearningReminder, snapshot.learningReminder)
            .putBoolean(Keys.WorkUpdates, snapshot.workUpdates)
            .putBoolean(Keys.ServiceMessages, snapshot.serviceMessages)
            .putBoolean(Keys.QuietHours, snapshot.quietHours)
            .putBoolean(Keys.AutoSave, snapshot.autoSave)
            .putBoolean(Keys.WifiOnly, snapshot.wifiOnly)
            .putBoolean(Keys.HapticFeedback, snapshot.hapticFeedback)
            .putBoolean(Keys.LargeText, snapshot.largeText)
            .putBoolean(Keys.SoundEnabled, snapshot.soundEnabled)
            .putFloat(Keys.MusicVolume, snapshot.musicVolume.coerceIn(0f, 1f))
            .putFloat(Keys.EffectVolume, snapshot.effectVolume.coerceIn(0f, 1f))

    object Keys {
        const val MessageEnabled = "message_enabled"
        const val LearningReminder = "learning_reminder"
        const val WorkUpdates = "work_updates"
        const val ServiceMessages = "service_messages"
        const val QuietHours = "quiet_hours"

        const val AutoSave = "auto_save"
        const val WifiOnly = "wifi_only"
        const val HapticFeedback = "haptic_feedback"
        const val LargeText = "large_text"

        const val SoundEnabled = "sound_enabled"
        const val MusicVolume = "music_volume"
        const val EffectVolume = "effect_volume"

        internal const val SyncDirty = "sync_dirty"
    }

    private companion object {
        const val FILE_NAME = "jianghu_user_settings"
    }
}
