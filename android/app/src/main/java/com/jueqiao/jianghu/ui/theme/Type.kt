package com.jueqiao.jianghu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R

/**
 * Default to Noto Sans SC (OFL, redistributable). If user has local YaHei, replace
 * with R.font.font_yahei in [YaHei] below.
 */
val YaHei = FontFamily(
    Font(R.font.noto_sans_sc_regular, FontWeight.Normal),
    Font(R.font.noto_sans_sc_regular, FontWeight.Medium),
    Font(R.font.noto_sans_sc_bold, FontWeight.Bold),
)

val JianghuTypography = Typography(
    displayLarge   = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold,    fontSize = 48.sp, lineHeight = 52.sp),
    displayMedium  = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold,    fontSize = 36.sp, lineHeight = 40.sp),
    headlineLarge  = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold,    fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.SemiBold,fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.SemiBold,fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.SemiBold,fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.SemiBold,fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge      = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = YaHei, fontWeight = FontWeight.Bold,    fontSize = 10.sp, lineHeight = 14.sp),
)