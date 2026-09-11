package com.jueqiao.jianghu.ui.screens.gongfang

import com.jueqiao.jianghu.luggage.CreationProjectDto

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

fun CreationProjectDto.toCreationResumeItem(): CreationResumeItem? {
    if (status != "ACTIVE") return null

    val (stageLabel, stageIndex) = when (displayStatus) {
        "PUBLISHED" -> "已经展示" to 1
        "PENDING_CHECK", "PENDING_HUMAN_REVIEW" -> "等待老师" to 1
        else -> "继续沟通" to 0
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
