package com.jueqiao.jianghu.ui.screens.wushuhuan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualCodexContentTest {
    @Test
    fun firstVolumeUsesFiveLessonsInsteadOfOneLessonPerVolume() {
        assertEquals(5, volumeOneLessons.size)
        assertEquals((1..5).toList(), volumeOneLessons.map { it.pageNo })
        assertEquals(
            listOf(
                "会动未必会思",
                "机关三步诀",
                "一艺精不等于全能",
                "无样本不成招",
                "机巧也会犯错",
            ),
            volumeOneLessons.map { it.title },
        )
        assertEquals(listOf(8, 7, 6, 7, 7), volumeOneLessons.map { it.leaves.size })
    }

    @Test
    fun everyLessonHasDeepReadableCopyAndARealMigrationAction() {
        volumeOneLessons.forEach { lesson ->
            val chineseCharacters = lesson.leaves.sumOf { leaf ->
                (leaf.paragraphs + leaf.bullets + listOfNotNull(leaf.keyLine))
                    .sumOf { text -> text.count { it in '\u4e00'..'\u9fff' } }
            }
            assertTrue("${lesson.title}正文过短：$chineseCharacters", chineseCharacters >= 650)
            assertTrue(lesson.leaves.size in 6..9)
            assertEquals(ManualLeafAction.OPEN_COMIC, lesson.leaves.first().action)
            assertEquals(ManualLeafAction.USE_IN_CREATION, lesson.leaves.last().action)
        }
    }

    @Test
    fun statePermissionsNeverPretendUnseenContentIsLearned() {
        assertEquals(2, readableLeafCount("UNSEEN", 8))
        assertEquals(2, readableLeafCount("DISCOVERED", 8))
        assertEquals(8, readableLeafCount("LEARNED", 8))
        assertEquals(8, readableLeafCount("MASTERED", 8))
        assertEquals(8, readableLeafCount("TEACHING", 8))
        assertEquals(0, readableLeafCount("UNKNOWN", 8))
    }

    @Test
    fun comicRangesCoverAllFourteenPagesExactlyOnce() {
        val pages = volumeOneLessons.flatMap { it.comicPageRange.toList() }
        assertEquals((0..13).toList(), pages)
        assertEquals(pages.size, pages.distinct().size)
    }
}
