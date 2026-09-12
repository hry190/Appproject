package com.jueqiao.jianghu.nav

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {
    @Test
    fun conferenceWorkRouteKeepsPublicationId() {
        assertEquals(
            "dahui/work/publication-123",
            Routes.dahuiWork("publication-123"),
        )
    }

    @Test
    fun conferenceMatchRouteKeepsMatchId() {
        assertEquals(
            "dahui/match/match-456",
            Routes.dahuiMatch("match-456"),
        )
    }

    @Test
    fun yanwuchangDestinationsStayUnderTheirOwnRouteNamespace() {
        assertEquals("yanwuchang", Routes.Yanwuchang)
        assertEquals("yanwuchang/video", Routes.YanwuchangVideo)
        assertEquals("yanwuchang/video/comments", Routes.YanwuchangVideoComment)
        assertEquals(
            "yanwuchang/video/comments/expanded",
            Routes.YanwuchangVideoCommentExpanded,
        )
    }

    @Test
    fun learningDestinationsIncludeWheelTrialAndBackMountainFlow() {
        assertEquals("xiulian", Routes.Xiulian)
        assertEquals("gunlun1", Routes.Gunlun1)
        assertEquals("shilian", Routes.Shilian)
        assertEquals("shilian2", Routes.Shilian2)
        assertEquals("shilian3", Routes.Shilian3)
        assertEquals("houshan", Routes.Houshan)
        assertEquals("learning2", Routes.Learning2)
        assertEquals("learning3", Routes.Learning3)
        assertEquals("learning4", Routes.Learning4)
        assertEquals("unfinished", Routes.Unfinished)
        assertEquals("pending-unlock", Routes.PendingUnlock)
        assertEquals("gunlun12", Routes.Gunlun12)
        assertEquals("gunlun13", Routes.Gunlun13)
        assertEquals("volume1", Routes.Volume1)
        assertEquals("volume1-2", Routes.Volume1Part2)
        assertEquals("volume1-3", Routes.Volume1Part3)
        assertEquals("volume1-4", Routes.Volume1Part4)
        assertEquals("volume1-5", Routes.Volume1Part5)
        assertEquals("volume1-6", Routes.Volume1Part6)
        assertEquals("volume1-7", Routes.Volume1Part7)
        assertEquals("volume1-8", Routes.Volume1Part8)
        assertEquals("volume1-9", Routes.Volume1Part9)
        assertEquals("volume1-10", Routes.Volume1Part10)
        assertEquals("volume1-11", Routes.Volume1Part11)
        assertEquals("volume1-12", Routes.Volume1Part12)
        assertEquals("volume1-13", Routes.Volume1Part13)
        assertEquals("volume1-14", Routes.Volume1Part14)
        assertEquals("volume2-1", Routes.Volume2Part1)
        assertEquals("volume2-2", Routes.Volume2Part2)
        assertEquals("volume2-3", Routes.Volume2Part3)
        assertEquals("volume2-4", Routes.Volume2Part4)
        assertEquals("volume2-5", Routes.Volume2Part5)
        assertEquals("volume2-6", Routes.Volume2Part6)
        assertEquals("volume2-7", Routes.Volume2Part7)
        assertEquals("volume2-8", Routes.Volume2Part8)
        assertEquals("volume2-9", Routes.Volume2Part9)
        assertEquals("volume2-10", Routes.Volume2Part10)
        assertEquals("volume2-11", Routes.Volume2Part11)
        assertEquals("volume2-12", Routes.Volume2Part12)
        assertEquals("volume2-13", Routes.Volume2Part13)
        assertEquals("volume2-14", Routes.Volume2Part14)
        assertEquals("volume2-15", Routes.Volume2Part15)
        assertEquals("volume3-1", Routes.Volume3Part1)
        assertEquals("volume3-2", Routes.Volume3Part2)
        assertEquals("volume3-3", Routes.Volume3Part3)
        assertEquals("volume3-4", Routes.Volume3Part4)
        assertEquals("volume3-5", Routes.Volume3Part5)
        assertEquals("volume3-6", Routes.Volume3Part6)
        assertEquals("volume3-7", Routes.Volume3Part7)
        assertEquals("volume3-8", Routes.Volume3Part8)
        assertEquals("volume3-9", Routes.Volume3Part9)
        assertEquals("volume3-10", Routes.Volume3Part10)
        assertEquals("volume3-11", Routes.Volume3Part11)
        assertEquals("volume3-12", Routes.Volume3Part12)
        assertEquals("volume3-13", Routes.Volume3Part13)
        assertEquals("volume3-14", Routes.Volume3Part14)
        assertEquals("volume4-1", Routes.Volume4Part1)
        assertEquals("volume4-2", Routes.Volume4Part2)
        assertEquals("volume4-3", Routes.Volume4Part3)
        assertEquals("volume4-4", Routes.Volume4Part4)
        assertEquals("volume4-5", Routes.Volume4Part5)
        assertEquals("volume4-6", Routes.Volume4Part6)
        assertEquals("volume4-7", Routes.Volume4Part7)
        assertEquals("volume4-8", Routes.Volume4Part8)
        assertEquals("volume4-9", Routes.Volume4Part9)
        assertEquals("volume4-10", Routes.Volume4Part10)
        assertEquals("volume4-11", Routes.Volume4Part11)
        assertEquals("volume4-12", Routes.Volume4Part12)
        assertEquals("volume4-13", Routes.Volume4Part13)
        assertEquals("volume4-14", Routes.Volume4Part14)
        assertEquals("gunlun14", Routes.Gunlun14)
        assertEquals("gunlun15", Routes.Gunlun15)
    }
}
