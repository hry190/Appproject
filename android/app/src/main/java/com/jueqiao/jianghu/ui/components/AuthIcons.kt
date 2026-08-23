package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.jueqiao.jianghu.R

/**
 * Wrappers around the auth icon vector assets.
 * Replaces RN auth/AuthIcons.tsx.
 */

@Composable
fun PhoneIcon(modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(
        painter = painterResource(R.drawable.ic_phone),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
fun KeyIcon(modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(
        painter = painterResource(R.drawable.ic_key),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
fun PeopleSafeIcon(modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(
        painter = painterResource(R.drawable.ic_people_safe),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}