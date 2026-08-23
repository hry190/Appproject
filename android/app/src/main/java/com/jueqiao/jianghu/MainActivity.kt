package com.jueqiao.jianghu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jueqiao.jianghu.nav.JianghuNavHost
import com.jueqiao.jianghu.ui.theme.JianghuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JianghuTheme {
                JianghuNavHost()
            }
        }
    }
}