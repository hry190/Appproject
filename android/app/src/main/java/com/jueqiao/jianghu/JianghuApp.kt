package com.jueqiao.jianghu

import android.app.Application

import com.jueqiao.jianghu.auth.AuthApi
import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.EncryptedTokenStore
import com.jueqiao.jianghu.ui.screens.settings.SettingsNotifications
import com.jueqiao.jianghu.ui.screens.settings.SettingsRepository

class JianghuApp : Application() {
    lateinit var authRepository: AuthRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    private val prefs by lazy {
        getSharedPreferences("jianghu_tutorial", MODE_PRIVATE)
    }

    var tutorialComplete: Boolean
        get() = prefs.getBoolean("tutorial_complete", false)
        set(value) = prefs.edit().putBoolean("tutorial_complete", value).apply()

    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(
            api = AuthApi(BuildConfig.AUTH_BASE_URL),
            tokenStore = EncryptedTokenStore(this),
        )
        settingsRepository = SettingsRepository(this, authRepository)
        SettingsNotifications.initialize(this)
    }
}
