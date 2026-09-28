package com.jueqiao.jianghu.ui.screens.wushuhuan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AncientManualGestureTest {
    @Test
    fun `movement below touch slop remains undecided`() {
        assertEquals(
            ManualGestureAxis.Undecided,
            resolveManualGestureAxis(totalX = 7f, totalY = 5f, touchSlopPx = 12f),
        )
    }

    @Test
    fun `mostly horizontal diagonal movement locks page turn`() {
        assertEquals(
            ManualGestureAxis.Horizontal,
            resolveManualGestureAxis(totalX = 40f, totalY = 18f, touchSlopPx = 12f),
        )
    }

    @Test
    fun `mostly vertical movement stays with page scroll`() {
        assertEquals(
            ManualGestureAxis.Vertical,
            resolveManualGestureAxis(totalX = 14f, totalY = 36f, touchSlopPx = 12f),
        )
    }

    @Test
    fun `distance fraction commits turn`() {
        assertTrue(
            shouldCommitManualPageTurn(84f, 600f, 0f, 130f, 1800f),
        )
    }

    @Test
    fun `density based distance or velocity can commit turn`() {
        assertTrue(
            shouldCommitManualPageTurn(112f, 1200f, 0f, 110f, 1800f),
        )
        assertTrue(
            shouldCommitManualPageTurn(30f, 1200f, 1900f, 110f, 1800f),
        )
    }

    @Test
    fun `short slow drag cancels`() {
        assertFalse(
            shouldCommitManualPageTurn(48f, 1200f, 700f, 110f, 1800f),
        )
    }
}
