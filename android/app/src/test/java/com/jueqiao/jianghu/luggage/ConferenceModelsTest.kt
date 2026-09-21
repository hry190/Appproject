package com.jueqiao.jianghu.luggage

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceModelsTest {
    private val gson = Gson()

    @Test
    fun parsesConferenceWorkLearningEvidence() {
        val work = gson.fromJson(
            """
            {
              "publication_id":"pub-1",
              "project_id":"project-1",
              "creation_version_id":"version-2",
              "title":"机巧作品",
              "description":"说明",
              "media_type":"ILLUSTRATION",
              "author_nickname":"同门",
              "is_owner":false,
              "version_number":2,
              "published_at":"2026-09-07T00:00:00Z",
              "preview_url":null,
              "ai_assisted":true,
              "learning_summary":"学会了结构设计",
              "related_manuals":[{"manual_page_id":"manual-1","page_no":3,"title":"榫卯"}],
              "learning_card":{"method_summary":"先拆后合","unresolved_questions":[]},
              "provenance":{"human_contribution_summary":"构思与修改","ai_assistance_used":true,"ai_contribution_summary":"草图辅助","aigc_label_declared":true,"source_count":1}
            }
            """.trimIndent(),
            ConferenceWorkDto::class.java,
        )

        assertEquals(2, work.versionNumber)
        assertEquals("榫卯", work.relatedManuals.single().title)
        assertEquals("先拆后合", work.learningCard?.methodSummary)
        assertTrue(work.provenance?.aiAssistanceUsed == true)
    }

    @Test
    fun parsesConferenceMatchAndLetterDeepLink() {
        val match = gson.fromJson(
            """
            {
              "match_id":"match-1",
              "manual_page_id":"manual-1",
              "status":"ACTIVE",
              "questions":[{"id":"question-1","position":1,"kind":"CORE_LOGIC","prompt":"为什么？"}],
              "my_answers":[],
              "my_progress":{"answered":0,"total":3,"complete":false},
              "opponent_progress":{"answered":1,"total":3,"complete":false},
              "matched_at":"2026-09-07T00:00:00Z"
            }
            """.trimIndent(),
            ConferenceMatchDetailDto::class.java,
        )
        val letter = gson.fromJson(
            """
            {"id":"letter-1","category":"MATCH","title":"已匹配","body":"进入切磋","action_type":"CONFERENCE_MATCH","action_id":"match-1","navigation_target":"CONFERENCE_MATCH","navigation_id":"match-1","is_read":false,"read_at":null,"created_at":"2026-09-07T00:00:00Z"}
            """.trimIndent(),
            ConferenceLetterDto::class.java,
        )

        assertEquals("question-1", match.questions.single().id)
        assertEquals("match-1", letter.actionId)
        assertEquals("CONFERENCE_MATCH", letter.actionType)
        assertEquals("CONFERENCE_MATCH", letter.navigationTarget)
        assertEquals("match-1", letter.navigationId)
    }

    @Test
    fun parsesConferenceMatchRecordPageWithoutOpponentIdentity() {
        val page = gson.fromJson(
            """
            {
              "items":[{
                "match_id":"match-1",
                "manual_id":"manual-1",
                "manual_title":"机关三步诀",
                "manual_page_no":2,
                "status":"ENDED",
                "outcome":"WIN",
                "my_score":91,
                "opponent_score":84,
                "anonymous_opponent":{"alias":"竹影同门","avatar_key":"PANDA_BAMBOO","age_band_label":"同龄","stage_label":"同阶段"},
                "judgment_status":"COMPLETED",
                "reflection_status":"PENDING",
                "created_at":"2026-09-07T00:00:00Z",
                "ended_at":"2026-09-07T00:08:00Z"
              }],
              "summary":{"total":3,"wins":1,"ties":1,"pending_reflections":2},
              "page":1,
              "limit":20,
              "total":1,
              "has_more":false
            }
            """.trimIndent(),
            ConferenceMatchRecordListDto::class.java,
        )

        assertEquals("match-1", page.items.single().matchId)
        assertEquals("竹影同门", page.items.single().anonymousOpponent.alias)
        assertEquals(2, page.summary.pendingReflections)
        assertEquals(false, page.hasMore)
    }

    @Test
    fun parsesConferenceCapabilitySwitch() {
        val capabilities = gson.fromJson(
            """
            {"profile":true,"manual_catalog":true,"manual_favorites":true,"luggage_snapshot":"LIVE_MEDIA_REVIEW","learning_progress":true,"mistakes":true,"creations":true,"media_uploads":true,"conference":false}
            """.trimIndent(),
            CapabilitiesDto::class.java,
        )

        assertEquals(false, capabilities.conference)
    }
}
