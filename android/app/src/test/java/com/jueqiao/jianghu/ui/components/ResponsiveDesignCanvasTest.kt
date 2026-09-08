package com.jueqiao.jianghu.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsiveDesignCanvasTest {
    @Test
    fun shrinksUniformlyToFitTheTighterDimension() {
        assertEquals(
            700f / 917f,
            calculateDesignCanvasScale(
                availableWidthDp = 412f,
                availableHeightDp = 700f,
            ),
            0.0001f,
        )
    }

    @Test
    fun neverUpscalesLargeWindows() {
        assertEquals(
            1f,
            calculateDesignCanvasScale(
                availableWidthDp = 840f,
                availableHeightDp = 1200f,
            ),
            0f,
        )
    }

    @Test
    fun returnsZeroForUnavailableSpace() {
        assertEquals(
            0f,
            calculateDesignCanvasScale(
                availableWidthDp = 0f,
                availableHeightDp = 917f,
            ),
            0f,
        )
    }
}
