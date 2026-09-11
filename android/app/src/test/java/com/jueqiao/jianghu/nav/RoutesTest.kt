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
        assertEquals("gunlun14", Routes.Gunlun14)
        assertEquals("gunlun15", Routes.Gunlun15)
    }
}
