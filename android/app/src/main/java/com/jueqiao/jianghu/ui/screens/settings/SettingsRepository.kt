package com.jueqiao.jianghu.ui.screens.settings

import android.content.Context
import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.BlacklistEntryDto
import com.jueqiao.jianghu.auth.FeedbackResponseDto
import com.jueqiao.jianghu.auth.SessionDto
import com.jueqiao.jianghu.auth.UserSettingsDto
import com.jueqiao.jianghu.auth.UserSettingsPatchDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsRepository(
    context: Context,
    private val authRepository: AuthRepository,
) {
    private val appContext = context.applicationContext
    private val preferences = SettingsPreferences(appContext)
    private val syncMutex = Mutex()

    val snapshots: Flow<SettingsSnapshot> = preferences.snapshots
    val currentUser = authRepository.currentUser

    fun currentSnapshot(): SettingsSnapshot = preferences.readSnapshot()

    suspend fun sync() = syncMutex.withLock {
        if (!authRepository.hasStoredSession()) return@withLock
        if (preferences.isSyncDirty()) {
            authRepository.updateSettings(preferences.readSnapshot().toPatch())
            preferences.markSynced()
        } else {
            val remote = authRepository.getSettings().toSnapshot()
            preferences.replaceFromCloud(remote)
        }
        SettingsNotifications.apply(appContext, preferences.readSnapshot())
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        preferences.putBoolean(key, value)
        SettingsNotifications.apply(appContext, preferences.readSnapshot())
        uploadLatest()
    }

    suspend fun setFloat(key: String, value: Float) {
        preferences.putFloat(key, value)
        uploadLatest()
    }

    suspend fun restoreDefaults() {
        preferences.restoreDefaults()
        SettingsNotifications.apply(appContext, preferences.readSnapshot())
        uploadLatest()
    }

    suspend fun submitFeedback(message: String): FeedbackResponseDto =
        authRepository.submitFeedback(message)

    suspend fun getBlacklist(): List<BlacklistEntryDto> =
        authRepository.getBlacklist()

    suspend fun removeFromBlacklist(userId: String) {
        authRepository.removeFromBlacklist(userId)
    }

    suspend fun getSessions(): List<SessionDto> = authRepository.getSessions()

    suspend fun refreshCurrentUser() = authRepository.refreshCurrentUser()

    private suspend fun uploadLatest() = syncMutex.withLock {
        if (!authRepository.hasStoredSession()) return@withLock
        authRepository.updateSettings(preferences.readSnapshot().toPatch())
        preferences.markSynced()
    }
}

private fun UserSettingsDto.toSnapshot(): SettingsSnapshot = SettingsSnapshot(
    messageEnabled = messageEnabled,
    learningReminder = learningReminder,
    workUpdates = workUpdates,
    serviceMessages = serviceMessages,
    quietHours = quietHours,
    autoSave = autoSave,
    wifiOnly = wifiOnly,
    hapticFeedback = hapticFeedback,
    largeText = largeText,
    soundEnabled = soundEnabled,
    musicVolume = musicVolume,
    effectVolume = effectVolume,
)

private fun SettingsSnapshot.toPatch(): UserSettingsPatchDto = UserSettingsPatchDto(
    messageEnabled = messageEnabled,
    learningReminder = learningReminder,
    workUpdates = workUpdates,
    serviceMessages = serviceMessages,
    quietHours = quietHours,
    autoSave = autoSave,
    wifiOnly = wifiOnly,
    hapticFeedback = hapticFeedback,
    largeText = largeText,
    soundEnabled = soundEnabled,
    musicVolume = musicVolume,
    effectVolume = effectVolume,
)
