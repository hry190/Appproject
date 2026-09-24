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
    fun conferencePrimaryTabsUseDistinctRoutes() {
        assertEquals("dahui/arena", Routes.DahuiArena)
        assertEquals("dahui/records", Routes.DahuiRecords)
        assertEquals("dahui/letters", Routes.DahuiLetters)
    }

    @Test
    fun conferenceLettersResolveToTheirSpecificDestinations() {
        assertEquals(
            "dahui/match/match-456",
            Routes.conferenceLetterDestination(
                navigationTarget = "CONFERENCE_MATCH",
                navigationId = "match-456",
                actionType = "CONFERENCE_MATCH",
                actionId = "ignored",
            ),
        )
        assertEquals(
            "dahui/work/work-789",
            Routes.conferenceLetterDestination(
                navigationTarget = "CONFERENCE_WORK",
                navigationId = "work-789",
                actionType = "CONFERENCE_REVIEW",
                actionId = "review-1",
            ),
        )
        assertEquals(
            Routes.DahuiRequests,
            Routes.conferenceLetterDestination(
                navigationTarget = "DERIVATIVE_REQUESTS",
                navigationId = null,
                actionType = "DERIVATIVE_REQUEST",
                actionId = "request-1",
            ),
        )
        assertEquals(
            null,
            Routes.conferenceLetterDestination(
                navigationTarget = null,
                navigationId = null,
                actionType = "SYSTEM",
                actionId = null,
            ),
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
    fun learningDestinationsUseOneReaderAndOneRealTrialFlow() {
        assertEquals("xiulian", Routes.Xiulian)
        assertEquals("gunlun1", Routes.Gunlun1)
        assertEquals("wushuhuan", Routes.Wushuhuan)
        assertEquals("shilian", Routes.Shilian)
        assertEquals("shilian2", Routes.Shilian2)
        assertEquals("shilian3", Routes.Shilian3)
        assertEquals("unfinished", Routes.Unfinished)
        assertEquals("gunlun12", Routes.Gunlun12)
        assertEquals("gunlun13", Routes.Gunlun13)
        assertEquals(
            "manual-reader/1/lesson%2F1?continueToTrial=true",
            Routes.manualReader(1, "lesson/1", continueToTrial = true),
        )
        assertEquals(
            "luggage/learning-trial/trial-1?returnToWushuhuan=true",
            Routes.learningTrial("trial-1", returnToWushuhuan = true),
        )
        assertEquals(
            "gongfang?sourceManualId=manual%2F1",
            Routes.gongfang("manual/1"),
        )
        assertEquals("gunlun14", Routes.Gunlun14)
        assertEquals("gunlun15", Routes.Gunlun15)
    }

    @Test
    fun backMountainRouteCarriesAnOptionalTargetLesson() {
        assertEquals("shilian", Routes.shilian(null))
        assertEquals("shilian", Routes.shilian(""))
        assertEquals("shilian?lessonId=lesson%2F1", Routes.shilian("lesson/1"))
    }

    @Test
    fun conferencePublishRouteKeepsTheCreationProjectId() {
        assertEquals(
            "creation/publish/project-123",
            Routes.conferencePublish("project-123"),
        )
    }
}
