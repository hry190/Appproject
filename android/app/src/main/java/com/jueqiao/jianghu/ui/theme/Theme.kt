package com.jueqiao.jianghu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary           = InkAccent,
    onPrimary         = InkText,
    primaryContainer  = InkAccentSoft,
    onPrimaryContainer= InkText,
    secondary         = InkBamboo,
    onSecondary       = InkBgElement,
    secondaryContainer= InkAccentSoft,
    tertiary          = InkCinnabar,
    onTertiary        = InkBgElement,
    background        = InkBg,
    onBackground      = InkText,
    surface           = InkBgElement,
    onSurface         = InkText,
    surfaceVariant    = InkBgSelected,
    onSurfaceVariant  = InkTextSecondary,
    outline           = InkBorder,
    error             = InkCinnabar,
)

private val DarkColors = darkColorScheme(
    primary           = InkDarkAccent,
    onPrimary         = InkDarkText,
    primaryContainer  = InkDarkBgSelected,
    secondary         = InkDarkBamboo,
    onSecondary       = InkDarkText,
    tertiary          = InkDarkCinnabar,
    onTertiary        = InkDarkText,
    background        = InkDarkBg,
    onBackground      = InkDarkText,
    surface           = InkDarkBgElement,
    onSurface         = InkDarkText,
    surfaceVariant    = InkDarkBgSelected,
    onSurfaceVariant  = InkDarkTextSecondary,
    outline           = InkDarkBorder,
    error             = InkDarkCinnabar,
)

@Composable
fun JianghuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = JianghuTypography,
        shapes      = JianghuShapes,
        content     = content,
    )
}