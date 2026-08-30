package com.jueqiao.jianghu.data

/**
 * Static data for the 4 info-tab screens (行囊/修炼/造物/武林大会).
 * Mirrors the hardcoded data from RN (tabs)/xingnang.tsx, xiulian.tsx, zaowu.tsx, dahui.tsx.
 */

data class Profile(
    val name: String,
    val joinedDays: Int,
    val works: Int,
    val manuals: Int,
    val badges: Int,
)

data class Badge(val name: String, val colorHex: Long, val earned: Boolean)

data class Work(
    val title: String,
    val format: String,
    val status: String,
    val originalPct: Float,
    val aiPct: Float,
)

data class Manual(val name: String, val state: String, val date: String)

data class PracticeEntry(val date: String, val title: String, val xp: String)

data class ManualState(
    val name: String,
    val desc: String,
    val example: String,
    val achieved: Int,
    val total: Int,
    val tone: String, // "accent" | "bamboo" | "cinnabar"
)

data class Prediction(
    val question: String,
    val type: String,
    val status: String,
)

data class Trial(
    val title: String,
    val desc: String,
    val xp: String,
    val status: String,
    val locked: Boolean,
)

data class Principle(val text: String)

data class FormatOption(val name: String, val desc: String)

data class StepItem(val name: String, val prompt: String)

data class Prompt(val text: String)

data class ConferenceWork(
    val title: String,
    val author: String,
    val format: String,
    val originalPct: Int,
    val aiPct: Int,
    val blurb: String,
    val reviewCount: Int,
)

object StaticData {
    // ───────── 行囊 ─────────
    val profile = Profile(
        name = "机巧弟子",
        joinedDays = 12,
        works = 3,
        manuals = 11,
        badges = 5,
    )

    val badges = listOf(
        Badge("初出茅庐",   0xFFD6E1D0, true),
        Badge("第一篇四格", 0xFFF2D9DB, true),
        Badge("善问者",     0xFFE8D9A8, true),
        Badge("互评达人",   0xFFD6E1D0, true),
        Badge("原创 70%+",  0xFFF2D9DB, true),
        Badge("造物首作",   0xFFE8D9A8, false),
        Badge("预言家",     0xFFD6E1D0, false),
        Badge("习得 5 招",  0xFFF2D9DB, false),
    )

    val works = listOf(
        Work("《我家乡的秋叶》", "四格漫画", "已发大会", 0.85f, 0.15f),
        Work("《神奇的树叶》",   "短视频",   "已通关",   0.72f, 0.28f),
        Work("《银杏的自述》",   "绘画",     "习得中",   0.55f, 0.45f),
    )

    val manuals = listOf(
        Manual("百炼识物诀",       "偶得",  "10月1日"),
        Manual("四格漫画入门",     "习得",  "10月3日"),
        Manual("互评达人手册",     "习得",  "10月7日"),
        Manual("原创作者守则",     "悟得",  "10月12日"),
    )

    val practice = listOf(
        PracticeEntry("10月12日", "完成第三次试炼", "+20"),
        PracticeEntry("10月10日", "发表《银杏的自述》", "+15"),
        PracticeEntry("10月7日",  "完成两篇同伴互评", "+10"),
    )

    // ───────── 修炼 ─────────
    val chapterTitle = "《百炼识物诀》第 3 章 — 树叶为什么会变色？"
    val chapterMeta  = "8 页漫画 · 3 道预测 · 2 次试炼"
    val chapterBlurb = "本章讲述：当气温下降，树叶里的叶绿素分解，类胡萝卜素和花青素逐渐显现。"

    val manualStates = listOf(
        ManualState(
            name     = "偶得",
            desc     = "在路上、书里、动画里偶然遇见，记下名字即可。",
            example  = "例：在动画里看到「类胡萝卜素」",
            achieved = 8, total = 12, tone = "accent",
        ),
        ManualState(
            name     = "习得",
            desc     = "理解原理、能向他人讲清并用于一次造物。",
            example  = "例：用「类胡萝卜素」解释银杏叶为什么变黄",
            achieved = 4, total = 12, tone = "bamboo",
        ),
        ManualState(
            name     = "悟得",
            desc     = "跨场景迁移、能与同伴讨论出新的解法。",
            example  = "例：在试炼里用它预测并验证一片陌生树叶",
            achieved = 2, total = 12, tone = "cinnabar",
        ),
    )

    val predictions = listOf(
        Prediction(
            question = "如果把一片绿叶放进冰箱一周，颜色会变吗？为什么？",
            type     = "单选",
            status   = "新",
        ),
        Prediction(
            question = "枫叶和银杏叶在秋天都变色，本质是不是同一个原因？",
            type     = "开放问答",
            status   = "进行中",
        ),
    )

    val trials = listOf(
        Trial(
            title  = "试炼：识别三种秋叶",
            desc   = "在校园里采集三种秋叶并解释它们变色的原因。",
            xp     = "+20",
            status = "可挑战",
            locked = false,
        ),
        Trial(
            title  = "试炼：漫画补全",
            desc   = "为一段四格漫画补全第三格的预测与解释。",
            xp     = "+30",
            status = "未解锁",
            locked = true,
        ),
    )

    // ───────── 造物 ─────────
    val principles = listOf(
        Principle("一次只问一个引导问题，让学生在每次回答之间停留思考。"),
        Principle("不代替学生完成全部创作——只在关键转折给出提示。"),
        Principle("记录学生原创、AI 辅助与修改过程，留下清晰的「署名留痕」。"),
    )

    val formats = listOf(
        FormatOption("绘画",   "单图、漫画分镜、四格漫画"),
        FormatOption("漫画",   "四格 / 多格，配文字对白"),
        FormatOption("动画",   "2-3 分钟短片，配旁白"),
        FormatOption("短视频", "15-60 秒解说 / 实拍"),
    )

    val steps = listOf(
        StepItem("选材", "Agent 会问：你想写什么主题？它有什么特别之处？"),
        StepItem("调研", "Agent 会问：你打算从哪里获取信息？能举一个权威来源吗？"),
        StepItem("结构", "Agent 会问：作品分几部分？每部分重点是什么？"),
        StepItem("草稿", "Agent 会问：先写下你最有把握的一段，然后我们再迭代。"),
        StepItem("署名", "Agent 会问：哪些是你原创？哪些用了 AI 辅助？"),
    )

    val recentWorks = listOf(
        Work("《我家乡的秋叶》", "四格漫画", "已发大会", 0.85f, 0.15f),
        Work("《神奇的树叶》",   "短视频",   "已通关",   0.72f, 0.28f),
    )

    // ───────── 武林大会 ─────────
    val reviewTags = listOf("有创意", "有方法", "有思考", "我学到一招")

    val prompts = listOf(
        Prompt("我最欣赏的是 …"),
        Prompt("我从中学到了 …"),
        Prompt("我建议可以 …"),
        Prompt("我想了解的是 …"),
    )

    val conferenceWorks = listOf(
        ConferenceWork(
            title = "《秋叶颜色记》", author = "李四", format = "四格漫画",
            originalPct = 80, aiPct = 20, blurb = "用四格记录了三种秋叶的颜色变化，配色观察细致。",
            reviewCount = 5,
        ),
        ConferenceWork(
            title = "《树叶为什么会变黄》", author = "王五", format = "短视频",
            originalPct = 65, aiPct = 35, blurb = "60 秒解说，引用了课本上的类胡萝卜素概念。",
            reviewCount = 3,
        ),
        ConferenceWork(
            title = "《银杏的自述》", author = "赵六", format = "绘画",
            originalPct = 70, aiPct = 30, blurb = "第一人称叙述，配色对比强烈，结尾留了一个开放问题。",
            reviewCount = 7,
        ),
    )
}