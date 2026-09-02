package com.jueqiao.jianghu.nav

/**
 * Route constants for Navigation Compose.
 * Mirrors the React Native expo-router route names.
 */
object Routes {
    const val Splash    = "splash"
    const val Login     = "login"
    const val Register  = "register"
    const val Forgot    = "forgot"
    const val Agreement = "agreement"
    const val Privacy   = "privacy"
    const val Home      = "home"
    const val Home1     = "home1"
    const val Xiulian   = "xiulian"
    const val Luggage   = "luggage"
    const val LuggageBadges = "luggage/badges"
    const val LuggageGrowth = "luggage/growth"
    const val LuggageEvidence = "luggage/evidence"
    const val LuggageManualsPattern = "luggage/manuals/{state}"
    fun luggageManuals(state: String?): String = "luggage/manuals/${state ?: "ALL"}"
    const val LuggageManualDetailPattern = "luggage/manual/{manualId}"
    fun luggageManualDetail(manualId: String): String = "luggage/manual/$manualId"
    const val LuggageMistakes = "luggage/mistakes"
    const val LuggageMistakeDetailPattern = "luggage/mistake/{mistakeId}"
    fun luggageMistakeDetail(mistakeId: String): String = "luggage/mistake/$mistakeId"
    const val LuggageCreations = "luggage/creations"
    const val LuggageCreationDetailPattern = "luggage/creation/{projectId}"
    fun luggageCreationDetail(projectId: String): String = "luggage/creation/$projectId"
    const val LuggagePrivacySafety = "luggage/privacy_safety"
    const val RetryTrialPattern = "luggage/retry/{mistakeId}/{trialId}/{versionId}/{sessionId}"
    fun retryTrial(mistakeId: String, trialId: String, versionId: String, sessionId: String): String =
        "luggage/retry/$mistakeId/$trialId/$versionId/$sessionId"
    const val Settings  = "settings"
    const val SettingsAccount = "settings/account"
    const val SettingsMessage = "settings/message"
    const val SettingsGeneral = "settings/general"
    const val SettingsSound = "settings/sound"
    const val SettingsBlacklist = "settings/blacklist"
    const val SettingsCollection = "settings/collection"
    const val SettingsSharing = "settings/sharing"
    const val SettingsHelp = "settings/help"
    const val SettingsAbout = "settings/about"
    const val SettingsDataRecovery = "settings/data_recovery"
    const val Challenge = "challenge"
    const val Zaowu     = "zaowu"
    const val Dahui     = "dahui"
    const val Gongfang  = "gongfang"
    const val Shengtu   = "shengtu"
    const val ShengtuProjectPattern = "shengtu/project/{projectId}"
    fun shengtuProject(projectId: String): String = "shengtu/project/$projectId"
    const val Picture   = "picture"
    const val Yaosu     = "yaosu"
    const val Chuangzuodangan = "chuangzuodangan"
    const val Chuangzuodangan2 = "chuangzuodangan2"
    const val Chuangzuodangan3 = "chuangzuodangan3"
    const val Chuangzuodangan4 = "chuangzuodangan4"
    const val Chuangzuodangan5 = "chuangzuodangan5"
    // 参数化路由:工坊里 3 个"继续创作"按钮跳过去
    const val EditWork         = "edit_work"
    const val EditWorkPattern  = "edit_work/{workId}"
    fun editWork(workId: String): String = "edit_work/$workId"
    // 参数化路由:工坊"确定"提交后,跳到聊天结果页
    const val ChatResult         = "chat_result"
    const val ChatResultPattern  = "chat_result/{query}"
    fun chatResult(query: String): String {
        // query 走 URL 编码,避免中文/特殊字符把路由破坏
        val encoded = java.net.URLEncoder.encode(query, Charsets.UTF_8.name())
        return "chat_result/$encoded"
    }
}
