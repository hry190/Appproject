package com.jueqiao.jianghu.ui.screens.manualreader

import org.junit.Assert.assertEquals
import org.junit.Test

class ManualReaderScreenTest {
    @Test
    fun allExistingVolumesUseTheirCompletePageCount() {
        assertEquals(14, readerPageCount(1))
        assertEquals(15, readerPageCount(2))
        assertEquals(1, readerPageCount(3))
    }

    @Test
    fun unknownVolumeFallsBackToSinglePageContainer() {
        assertEquals(1, readerPageCount(99))
    }

    @Test
    fun firstVolumeComicPagesAreGroupedByTheFiveActualLessons() {
        assertEquals(listOf(0, 1, 2), readerPageIndices(1, 1))
        assertEquals(listOf(3, 4, 5), readerPageIndices(1, 2))
        assertEquals(listOf(6, 7), readerPageIndices(1, 3))
        assertEquals(listOf(8, 9, 10), readerPageIndices(1, 4))
        assertEquals(listOf(11, 12, 13), readerPageIndices(1, 5))
    }

    @Test
    fun otherVolumesAndUnknownLessonsKeepTheWholeReader() {
        assertEquals((0 until 14).toList(), readerPageIndices(1, null))
        assertEquals((0 until 15).toList(), readerPageIndices(2, 1))
        assertEquals(listOf(0), readerPageIndices(3, 1))
    }
}
