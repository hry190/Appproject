package com.jueqiao.jianghu

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.nav.JianghuNavHost
import com.jueqiao.jianghu.ui.components.NoRippleIndication
import com.jueqiao.jianghu.ui.screens.settings.SettingsPreferences
import com.jueqiao.jianghu.ui.theme.JianghuTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // 显式声明:状态栏/导航栏全透明(scrim=TRANSPARENT),浅色背景用深色图标
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
        )
        setContent {
            val preferences = remember { SettingsPreferences(applicationContext) }
            val navController = rememberNavController()
            val settings by preferences.snapshots.collectAsStateWithLifecycle(
                initialValue = preferences.readSnapshot(),
            )
            val baseDensity = LocalDensity.current
            val scaledDensity = remember(baseDensity, settings.largeText) {
                Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * if (settings.largeText) 1.12f else 1f,
                )
            }
            CompositionLocalProvider(
                LocalDensity provides scaledDensity,
                LocalIndication provides NoRippleIndication,
                LocalRippleConfiguration provides null,
            ) {
                JianghuTheme {
                    JianghuNavHost(navController = navController)
                }
            }
        }
    }
}
