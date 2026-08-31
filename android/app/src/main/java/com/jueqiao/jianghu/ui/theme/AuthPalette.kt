package com.jueqiao.jianghu.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Auth-only palette (from auth/authColors.ts + register page local constants).
 * Intentionally separate from the main theme — used by login, forgot, register screens.
 */
object AuthPalette {
    val BgCream      = Color(0xFFF5E8D4)
    val InputBorder  = Color(0xFFDCCCA1)
    val LinkOlive    = Color(0xFFA7AD8E)
    val Placeholder  = Color(0xFF939393)
    val TextDark     = Color(0xFF000000)
    val ActionGray   = Color(0xFF888888)
    val ErrorRed     = Color(0xFFB8323A)
    val ErrorSoft    = Color(0xFFF4E1D8)
    val SuccessGreen = Color(0xFF617A52)
    val SuccessSoft  = Color(0xFFE4ECD9)
    val WarningSoft  = Color(0xFFF4E8C8)
    val DividerGray  = Color(0xFFC0C0C0)
    val ModeTabOlive = Color(0xFF9DA27F) // register page card bg
    val CreamBtn     = Color(0xFFF7ECDA) // register submit button
    val Olive        = Color(0xFF9DA27F) // alias for register card
}
