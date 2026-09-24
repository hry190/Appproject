package com.jueqiao.jianghu.ui.screens.chuangzuodangan

import com.jueqiao.jianghu.luggage.CreationProjectDto

enum class ArchiveCategory(val label: String, val pickerTitle: String) {
    Graphic("图文", "选择一幅图文作品"),
    Video("视频", "选择一个视频作品"),
    Game("游戏小程序", "选择一个游戏小程序作品");

    companion object {
        /** Match the stored media type, never the title, prompt or publication category. */
        fun fromMediaType(mediaType: String): ArchiveCategory? = when (mediaType) {
            "ILLUSTRATION", "COMIC", "MIXED_MEDIA", "TEXT", "IMAGE" -> Graphic
            "VIDEO" -> Video
            // Reserved for the future editors; unknown types must not become graphic works.
            "GAME", "MINI_PROGRAM" -> Game
            else -> null
        }
    }
}

internal fun ArchiveCategory.projects(works: List<CreationProjectDto>): List<CreationProjectDto> =
    works.filter { it.status == "ACTIVE" && ArchiveCategory.fromMediaType(it.mediaType) == this }
        .distinctBy { it.id }

internal fun archivePublicationLabel(status: String): String = when (status) {
    "PUBLISHED" -> "已发布"
    "PENDING_CHECK", "PENDING_REVIEW", "SUBMITTED", "PENDING_HUMAN_REVIEW" -> "审核中"
    "WITHDRAWN" -> "已撤回"
    "RETURNED", "REJECTED" -> "请修改后再试"
    "RESTRICTED" -> "暂不可发布"
    else -> "已提交"
}

internal fun archiveCanPublish(project: CreationProjectDto, currentVersionId: String?): Boolean =
    project.status == "ACTIVE" && project.currentStage == "SEAL" && currentVersionId != null &&
        project.latestPublication?.status !in setOf(
            "PUBLISHED", "PENDING_CHECK", "PENDING_REVIEW", "SUBMITTED", "PENDING_HUMAN_REVIEW",
        ) && project.latestPublication?.creationVersionId != currentVersionId
