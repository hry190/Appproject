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
    const val Settings  = "settings"
    const val Challenge = "challenge"
    const val Zaowu     = "zaowu"
    const val Dahui     = "dahui"
    const val Yanwuchang = "yanwuchang"
    const val YanwuchangVideo = "yanwuchang_video"
    // 演武场视频 — 评论1 页(点击视频首页"评论"图标进入,小评论框)
    const val YanwuchangVideoComment1 = "yanwuchang_video_comment_1"
    // 演武场视频 — 评论2 页(点"放大缩小"进入,小评论框向上扩展成全屏)
    const val YanwuchangVideoComment2 = "yanwuchang_video_comment_2"
    // 演武场视频 — 顶部 4 个学科 Tab 各自独立路由(艺术/科学/数学/语文)
    const val YanwuchangVideoArt     = "yanwuchang_video_art"
    const val YanwuchangVideoScience = "yanwuchang_video_science"
    const val YanwuchangVideoMath    = "yanwuchang_video_math"
    const val YanwuchangVideoChinese = "yanwuchang_video_chinese"
    // 演武场视频 — "我的"页(点击底部导航"我的"图标进入,带返回键)
    const val YanwuchangVideoMy = "yanwuchang_video_my"
    const val Gongfang  = "gongfang"
    const val Shengtu   = "shengtu"
    const val Picture   = "picture"
    const val Yaosu     = "yaosu"
    const val Chuangzuodangan = "chuangzuodangan"
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
