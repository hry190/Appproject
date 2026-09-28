package com.jueqiao.jianghu.luggage

/** Read every server page before reporting a complete archive. Keep server ordering. */
internal suspend fun collectCreationProjects(
    loadPage: suspend (String?) -> CreationProjectListDto,
): List<CreationProjectDto> {
    val projects = linkedMapOf<String, CreationProjectDto>()
    val visitedCursors = mutableSetOf<String>()
    var cursor: String? = null
    do {
        val page = loadPage(cursor)
        page.items.forEach { projects.putIfAbsent(it.id, it) }
        cursor = page.nextCursor?.takeIf { it.isNotBlank() }
        check(cursor == null || visitedCursors.add(cursor)) { "作品列表分页异常，请重新加载" }
    } while (cursor != null)
    return projects.values.toList()
}
