package com.jueqiao.jianghu.luggage

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreationToolCallModelsTest {
    @Test
    fun proposedCoachCallAllowsNullResultFields() {
        val raw = """
            {
              "id":"51093a93-321c-4f99-be4d-8d23f2b27691",
              "project_id":"bec8bb8a-e7f5-49e4-9290-211173a405a2",
              "creation_version_id":"f0b84d6d-add7-4259-98ae-16356eaafa8a",
              "kind":"COACH_REVIEW",
              "status":"PROPOSED",
              "input_snapshot":{"prompt":"请检查我的主题是否清楚","version_number":2},
              "prompt_summary":"请检查我的主题是否清楚",
              "effect_summary":"仅分析当前文字草稿并返回修改建议，不生成图片、不修改作品",
              "external_data_shared":false,
              "output_snapshot":null,
              "executor_ref":null,
              "row_version":1,
              "proposed_at":"2026-09-10T07:40:57.640728Z",
              "expires_at":"2026-09-10T08:10:57.640728Z",
              "decided_at":null,
              "completed_at":null
            }
        """.trimIndent()

        val call = Gson().fromJson(raw, CreationToolCallDto::class.java)

        assertEquals("PROPOSED", call.status)
        assertEquals("请检查我的主题是否清楚", call.inputSnapshot["prompt"].asString)
        assertNull(call.outputSnapshot)
        assertNull(call.executorRef)
        assertEquals(1, call.rowVersion)
    }
}
