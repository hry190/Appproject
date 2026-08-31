package com.jueqiao.jianghu

import android.app.Application

import com.jueqiao.jianghu.auth.AuthApi
import com.jueqiao.jianghu.auth.AuthRepository
import com.jueqiao.jianghu.auth.EncryptedTokenStore

class JianghuApp : Application() {
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(
            api = AuthApi(BuildConfig.AUTH_BASE_URL),
            tokenStore = EncryptedTokenStore(this),
        )
    }
}
