package com.jueqiao.jianghu.ui.screens.gongfang

import com.jueqiao.jianghu.luggage.CreationProjectDto
import com.jueqiao.jianghu.luggage.CreationMethodDraftDto

/** A small, UI-only projection used by the creation desk's resume panel. */
data class CreationResumeItem(
    val projectId: String,
    val title: String,
    val stageLabel: String,
    val stageIndex: Int,
    val statusLabel: String,
    val updatedAt: String,
)

data class CreationManualOption(
    val id: String,
    val title: String,
    val stateLabel: String,
)

data class CreationMethodPlan(
    val name: String,
    val goal: String,
    val audience: List<String>,
    val format: String,
    val steps: List<String>,
    val resourceLinks: List<String>,
    val sourceAssetIds: List<String>,
    val manualPageIds: List<String>,
    val recommendedMediaType: String,
)

fun CreationMethodDraftDto.toCreationMethodPlan() = CreationMethodPlan(
    name = name,
    goal = goal,
    audience = audience,
    format = format,
    steps = steps,
    resourceLinks = resourceLinks,
    sourceAssetIds = sourceAssetIds,
    manualPageIds = manualPageIds,
    recommendedMediaType = recommendedMediaType,
)

fun CreationMethodPlan.toCreationMethodDraftDto() = CreationMethodDraftDto(
    name = name,
    goal = goal,
    audience = audience,
    format = format,
    steps = steps,
    resourceLinks = resourceLinks,
    sourceAssetIds = sourceAssetIds,
    manualPageIds = manualPageIds,
    recommendedMediaType = recommendedMediaType,
)

fun CreationProjectDto.toCreationResumeItem(): CreationResumeItem? {
    if (status != "ACTIVE") return null

    val (stageLabel, stageIndex) = when (currentStage) {
        "IDEATION" -> "构思" to 0
        "DRAFT" -> "草图/脚本" to 1
        "PRODUCTION" -> "制作" to 2
        "TEST" -> "测试" to 3
        "SEAL" -> "说明/封卷" to 4
        else -> return null
    }
    return CreationResumeItem(
        projectId = id,
        title = title,
        stageLabel = stageLabel,
        stageIndex = stageIndex,
        statusLabel = when (displayStatus) {
            "PENDING_CHECK", "PENDING_HUMAN_REVIEW" -> "老师正在看"
            "PUBLISHED" -> "已展示"
            "RETURNED" -> "再改一改"
            "WITHDRAWN" -> "已撤回"
            "RESTRICTED" -> "暂不可展示"
            else -> "继续创作"
        },
        updatedAt = updatedAt.take(10).ifBlank { "最近更新" },
    )
}
