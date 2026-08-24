package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.jueqiao.jianghu.R

/**
 * Card frame used by login & forgot — image-based.
 * Replaced Canvas-drawn frame with "Vector 576.png" from mobile assets.
 */
@Composable
fun CardFrame(
    modifier: Modifier = Modifier,
    height: Dp,
) {
    Image(
        painter = painterResource(R.drawable.img_card_frame_bg),
        contentDescription = null,
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
        contentScale = ContentScale.FillBounds,
    )
}