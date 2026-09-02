package com.jueqiao.jianghu

import android.app.Application

import com.jueqiao.jianghu.auth.AuthApi
import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.EncryptedTokenStore
import com.jueqiao.jianghu.luggage.LuggageApi
import com.jueqiao.jianghu.luggage.LuggageRepository
import com.jueqiao.jianghu.ui.screens.settings.SettingsNotifications
import com.jueqiao.jianghu.ui.screens.settings.SettingsRepository

class JianghuApp : Application() {
    lateinit var authRepository: AuthRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var luggageRepository: LuggageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(
            api = AuthApi(BuildConfig.AUTH_BASE_URL),
            tokenStore = EncryptedTokenStore(this),
        )
        settingsRepository = SettingsRepository(this, authRepository)
        luggageRepository = LuggageRepository(
            authRepository = authRepository,
            api = LuggageApi(BuildConfig.AUTH_BASE_URL),
        )
        SettingsNotifications.initialize(this)
    }
}
