package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import com.google.gson.Gson
import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.luggage.CreationProjectListDto
import com.jueqiao.jianghu.luggage.PublicationDto
import com.jueqiao.jianghu.luggage.collectCreationProjects
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ArchiveModelsTest {
    private fun project(id: String, mediaType: String = "ILLUSTRATION") = CreationProjectDto(
        id = id, title = "视频与小游戏的图文设计", description = "video game", mediaType = mediaType,
        status = "ACTIVE", defaultVisibility = "PRIVATE", currentVersionNumber = 2,
        sourceIntentId = null, derivativeAuthorizationId = null, currentStage = "SEAL",
        displayStatus = "", latestPublication = null, rowVersion = 1,
        createdAt = null, updatedAt = "2026-09-12T00:00:00Z",
    )

    private fun publication(status: String, version: String = "v2") = Gson().fromJson(
        """{"id":"publication","status":"$status","creation_version_id":"$version"}""",
        PublicationDto::class.java,
    )

    @Test fun titleAndDescriptionNeverDetermineTheCategory() {
        val works = listOf(project("image"), project("comic", "COMIC"), project("mixed", "MIXED_MEDIA"))
        assertEquals(listOf("image", "comic", "mixed"), ArchiveCategory.Graphic.projects(works).map { it.id })
        assertTrue(ArchiveCategory.Video.projects(works).isEmpty())
        assertTrue(ArchiveCategory.Game.projects(works).isEmpty())
    }

    @Test fun eachCategoryContainsOnlyItsActualTypeAndUnknownTypesDoNotLeak() {
        val works = listOf(project("text", "TEXT"), project("image", "IMAGE"),
            project("video", "VIDEO"), project("game", "GAME"), project("app", "MINI_PROGRAM"),
            project("unknown", "AUDIO"), project("deleted").copy(status = "DELETED"), project("text", "TEXT"))
        assertEquals(listOf("text", "image"), ArchiveCategory.Graphic.projects(works).map { it.id })
        assertEquals(listOf("video"), ArchiveCategory.Video.projects(works).map { it.id })
        assertEquals(listOf("game", "app"), ArchiveCategory.Game.projects(works).map { it.id })
        assertNull(ArchiveCategory.fromMediaType("AUDIO"))
    }

    @Test fun previousSelectionIsNotAvailableAfterSwitchingCategoryOrDeleting() {
        val works = listOf(project("selected"), project("video", "VIDEO"))
        assertNull(ArchiveCategory.Video.projects(works).firstOrNull { it.id == "selected" })
        assertTrue(ArchiveCategory.Graphic.projects(works.filterNot { it.id == "selected" }).isEmpty())
    }

    @Test fun aSavedUnpublishedWorkCanBePublishedButMissingOrDraftVersionsCannot() {
        assertTrue(archiveCanPublish(project("p"), "v2"))
        assertFalse(archiveCanPublish(project("p"), null))
        assertFalse(archiveCanPublish(project("p").copy(currentStage = "DRAFT"), "v2"))
    }

    @Test fun publicationAndPendingReviewBlockDuplicateSubmissionsEvenAfterNewVersion() {
        listOf("PUBLISHED", "PENDING_CHECK", "PENDING_REVIEW", "SUBMITTED", "PENDING_HUMAN_REVIEW").forEach {
            val work = project("p").copy(latestPublication = publication(it))
            assertFalse(archiveCanPublish(work, "v2"))
            assertFalse(archiveCanPublish(work, "v3"))
        }
        assertEquals("已发布", archivePublicationLabel("PUBLISHED"))
        assertEquals("审核中", archivePublicationLabel("PENDING_CHECK"))
    }

    @Test fun returnedVersionMustBeRevisedBeforeResubmission() {
        val work = project("p").copy(latestPublication = publication("RETURNED"))
        assertFalse(archiveCanPublish(work, "v2"))
        assertTrue(archiveCanPublish(work, "v3"))
    }

    @Test fun archiveLoadsBeyondFiftyWorksAndRemovesPageOverlapWithoutReordering() = runBlocking {
        val calls = mutableListOf<String?>()
        val result = collectCreationProjects { cursor ->
            calls.add(cursor)
            when (cursor) {
                null -> CreationProjectListDto(121, (1..50).map { project("$it") }, "page2")
                "page2" -> CreationProjectListDto(121, (50..100).map { project("$it") }, "page3")
                "page3" -> CreationProjectListDto(121, (101..121).map { project("$it") }, null)
                else -> error("Unexpected cursor")
            }
        }
        assertEquals(listOf(null, "page2", "page3"), calls)
        assertEquals((1..121).map { "$it" }, result.map { it.id })
    }

    @Test fun laterPageFailureMustNotBeReportedAsACompleteArchive() = runBlocking {
        try {
            collectCreationProjects { cursor ->
                if (cursor == null) CreationProjectListDto(2, listOf(project("1")), "page2")
                else throw IllegalStateException("offline")
            }
            fail("Partial results must not look complete")
        } catch (expected: IllegalStateException) {
            assertEquals("offline", expected.message)
        }
    }

    @Test fun repeatedServerCursorStopsInsteadOfLoopingForever() = runBlocking {
        try {
            collectCreationProjects { CreationProjectListDto(1, listOf(project("1")), "same") }
            fail("Repeated cursor should fail")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("分页异常"))
        }
    }

    @Test fun cancelledArchiveLoadDoesNotReturnStaleResults() = runBlocking {
        try {
            collectCreationProjects { throw CancellationException("selection changed") }
            fail("Cancellation should propagate")
        } catch (expected: CancellationException) {
            assertEquals("selection changed", expected.message)
        }
    }
}
