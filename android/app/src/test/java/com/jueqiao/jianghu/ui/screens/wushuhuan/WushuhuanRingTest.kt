package com.jueqiao.jianghu.ui.screens.wushuhuan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WushuhuanRingTest {
    @Test
    fun firstVolumeUsesShortestPathAcrossEndOfRing() {
        assertEquals(10f, nearestRingTarget(currentPosition = 9.4f, volumeIndex = 0))
    }

    @Test
    fun tenthVolumeUsesShortestPathAcrossStartOfRing() {
        assertEquals(-1f, nearestRingTarget(currentPosition = 0.2f, volumeIndex = 9))
    }

    @Test
    fun centeredVolumeDoesNotMove() {
        assertEquals(3f, nearestRingTarget(currentPosition = 3f, volumeIndex = 3))
    }

    @Test
    fun volumeNormalizationHandlesBothDirections() {
        assertEquals(10, volumeForRingPosition(-1f))
        assertEquals(1, volumeForRingPosition(10f))
        assertEquals(2, volumeForRingPosition(11f))
    }

    @Test
    fun centeredBookIsFullyVisibleRegardlessOfUnlockState() {
        assertEquals(1f, bookVisualAlpha(distanceFromCenter = 0f, unseen = false))
        assertEquals(1f, bookVisualAlpha(distanceFromCenter = 0f, unseen = true))
    }

    @Test
    fun booksFadeProgressivelyWithDistance() {
        val alphas = (0..5).map { distance ->
            bookVisualAlpha(distanceFromCenter = distance.toFloat(), unseen = false)
        }
        alphas.zipWithNext().forEach { (nearer, farther) ->
            assertTrue("alpha should decrease with distance", nearer > farther)
        }
    }

    @Test
    fun unseenStateUsesSubtleSaturationInsteadOfHidingCenteredBook() {
        assertEquals(0.78f, bookVisualSaturation(unseen = true))
        assertEquals(1f, bookVisualSaturation(unseen = false))
        assertTrue(
            bookVisualAlpha(distanceFromCenter = 2f, unseen = true) <
                bookVisualAlpha(distanceFromCenter = 2f, unseen = false),
        )
    }
}
