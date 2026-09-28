package com.jueqiao.jianghu.ui.screens.gunlun1

import com.jueqiao.jianghu.luggage.LearningBookDto
import com.jueqiao.jianghu.luggage.LearningOverviewDto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gunlun1ManualStateTest {
    @Test
    fun emptyOrUnseenBooksHaveNoAcquiredManual() {
        assertFalse(overview().hasAcquiredManual())
        assertFalse(overview(book("UNSEEN")).hasAcquiredManual())
    }

    @Test
    fun everyExistingAcquiredStateActivatesXiulian() {
        listOf("DISCOVERED", "LEARNED", "MASTERED", "TEACHING").forEach { state ->
            assertTrue(overview(book(state)).hasAcquiredManual())
        }
    }

    @Test
    fun reviewDueKeepsUnderlyingAcquiredState() {
        assertTrue(overview(book(state = "LEARNED", reviewDue = true)).hasAcquiredManual())
    }

    private fun overview(vararg books: LearningBookDto) = LearningOverviewDto(
        recommendedLessonId = null,
        books = books.toList(),
        backMountain = null,
    )

    private fun book(state: String, reviewDue: Boolean = false) = LearningBookDto(
        manualPageId = "manual-$state",
        pageNo = 1,
        styleNo = 1,
        title = "识机真诀",
        volumeNo = 1,
        volumeTitle = "悟书环",
        state = state,
        stateLabel = state,
        evidenceCount = 0,
        reviewDue = reviewDue,
        updatedAt = null,
    )
}
