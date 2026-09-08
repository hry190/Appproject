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
}
