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
        assertEquals("unfinished", Routes.Unfinished)
        assertEquals("pending-unlock", Routes.PendingUnlock)
        assertEquals("gunlun12", Routes.Gunlun12)
        assertEquals("gunlun13", Routes.Gunlun13)
        assertEquals("gunlun14", Routes.Gunlun14)
        assertEquals("gunlun15", Routes.Gunlun15)
    }
}
