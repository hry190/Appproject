package com.jueqiao.jianghu.ui.screens.wushuhuan

import kotlin.math.abs

internal enum class ManualGestureAxis {
    Undecided,
    Horizontal,
    Vertical,
}

internal fun resolveManualGestureAxis(
    totalX: Float,
    totalY: Float,
    touchSlopPx: Float,
    directionBias: Float = 1.2f,
): ManualGestureAxis {
    if (abs(totalX) < touchSlopPx && abs(totalY) < touchSlopPx) {
        return ManualGestureAxis.Undecided
    }
    return when {
        abs(totalX) > abs(totalY) * directionBias -> ManualGestureAxis.Horizontal
        abs(totalY) > abs(totalX) * directionBias -> ManualGestureAxis.Vertical
        else -> ManualGestureAxis.Undecided
    }
}

internal fun shouldCommitManualPageTurn(
    distancePx: Float,
    pageWidthPx: Float,
    velocityPxPerSecond: Float,
    minimumDistancePx: Float,
    minimumVelocityPxPerSecond: Float,
    pageFractionThreshold: Float = 0.14f,
): Boolean {
    if (pageWidthPx <= 0f) return false
    return abs(distancePx) / pageWidthPx >= pageFractionThreshold ||
        abs(distancePx) >= minimumDistancePx ||
        abs(velocityPxPerSecond) >= minimumVelocityPxPerSecond
}
