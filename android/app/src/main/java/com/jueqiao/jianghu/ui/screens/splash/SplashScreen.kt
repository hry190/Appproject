package com.jueqiao.jianghu.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R

/**
 * Splash screen — full-bleed panda launch image; tap anywhere → onTap.
 * Mirrors RN splash.tsx: Image cover bg, italic "点击进入" hint at bottom.
 */
@Composable
fun SplashScreen(onTap: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onTap,
            )
            .semantics { contentDescription = "点击进入登录页" }
            .testTag("splashRoot"),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painter        = painterResource(R.drawable.img_panda_launch),
            contentDescription = null,
            modifier       = Modifier.fillMaxSize(),
            contentScale   = ContentScale.Crop,
        )
        Text(
            text       = "点击进入",
            style      = MaterialTheme.typography.bodyMedium,
            color      = MaterialTheme.colorScheme.onBackground,
            modifier   = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 48.dp)
                .padding(horizontal = 24.dp),
        )
    }
}