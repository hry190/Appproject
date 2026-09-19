package com.jueqiao.jianghu.ui.screens.chuangdang

/**
 * 2026-09-19 §6 闯荡江湖 —— 数据模型 / 题库 / 规则
 *
 * 依据《机巧江湖_闯荡江湖综合策划方案_v2》:
 *   §5  五关剧情与敌人 —— 雾隐机关镇:铜齿门卫 / 断目机关蝠 / 棋冠石将 / 百声纸鹤 / 百面机枢(Boss)
 *   §3  三心攻防规则 —— 双方各 3 心;进攻答对造成伤害,答错获得一次防御机会
 *   §3.3 格挡递减 —— 本场累计答对第 1/2/3/4 次,怪物格挡率 30% / 20% / 10% / 0%
 *   §4  第五关 —— 制作任务 + 五维量表评审(本实现用**本地规则评分**,不调大模型)
 *   §6  闯荡令 —— 持有上限 3 枚,一次正式出发消耗 1 枚(本实现只做本地状态)
 *
 * 用户 2026-09-19 指令:"先把页面做出来,先不接后端" —— 故本文件仅描述纯前端模型,
 * 不涉及 Network / DB;日后接后端时可把 [ChuangdangProgress] 换成 repository。
 */

// ── 基础模型 ────────────────────────────────────────────────────────────────

/** 一道题(进攻题与防御题同构)。 */
data class CdQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    /** 结算后展示的关键解释(文档 §3.2:完整解释在回合结束后出现,不先公布答案)。 */
    val explanation: String,
)

/** 一个普通关卡(第 1~4 关)。 */
data class CdStage(
    val index: Int,
    /** 关卡名,如"城门误认"。 */
    val title: String,
    /** 敌人名,如"铜齿门卫"。 */
    val enemyName: String,
    /** 敌人占位图形(纯 Compose 绘制,避免引入新素材)。 */
    val enemyGlyph: String,
    /** 场景描述(关前剧情,文档 §5)。 */
    val scene: String,
    /** 核心知识点。 */
    val knowledge: String,
    val attack: List<CdQuestion>,
    val defense: List<CdQuestion>,
)

/** 战斗阶段状态机(文档 §3.2 的回合判定顺序)。 */
enum class CdPhase {
    /** 等待作答进攻题。 */
    Attack,
    /** 进攻答错 → 等待作答防御题。 */
    Defense,
    /** 本回合已结算,展示解释,等待玩家继续。 */
    Resolved,
    Victory,
    Defeat,
}

// ── 规则常量(文档 §3)────────────────────────────────────────────────────────

const val CD_MAX_HEARTS = 3
const val CD_TOKENS_MAX = 3
const val CD_BOSS_PASS_SCORE = 70
const val CD_BOSS_KNOWLEDGE_FLOOR = 18
const val CD_BOSS_EXPLAIN_FLOOR = 12

/** 文档 §3.3 的格挡概率表:按**本场累计答对进攻题次数**取值(防御题答对不计入)。 */
private val CD_BLOCK_RATES = listOf(0.30f, 0.20f, 0.10f, 0f)

/** 第 [correctCount] 次答对进攻题时,怪物格挡的概率(第 4 次及以后为 0)。 */
fun cdBlockRate(correctCount: Int): Float =
    CD_BLOCK_RATES.getOrElse(correctCount - 1) { 0f }

// ── 五关题库(文档 §5 的知识点)────────────────────────────────────────────────

val CD_STAGES: List<CdStage> = listOf(
    CdStage(
        index = 1,
        title = "城门误认",
        enemyName = "铜齿门卫",
        enemyGlyph = "盾",
        scene = "烟雾笼罩镇口,巨大齿轮驱动城门,铜色盾牌刻满固定规则。" +
            "它宣称「我每日都能自动开门,自然已经学会了世间万事!」——但行动记录显示,它每次都是同一套动作。",
        knowledge = "固定规则与机器学习的区别",
        attack = listOf(
            CdQuestion(
                prompt = "门卫每次开门都是同一套动作,不随情况变化。这最可能来自?",
                options = listOf(
                    "从大量开门样本里总结出的规律",
                    "工匠预先写死的一套固定规则",
                    "它偷偷观察别处的门卫学来的",
                ),
                correctIndex = 1,
                explanation = "每次完全一致、不随情况变化,正是「固定规则」的特征;从样本学来的行为会随输入而变。",
            ),
            CdQuestion(
                prompt = "下列哪种表现,最能说明一个系统**不是**单纯执行固定规则?",
                options = listOf(
                    "每天在同一时刻开门",
                    "遇到没见过的门锁会先试探、再调整做法",
                    "按图纸依次转动齿轮",
                ),
                correctIndex = 1,
                explanation = "面对没见过的输入还能调整做法,才说明行为来自样本学习,而不是写死的规则。",
            ),
            CdQuestion(
                prompt = "门卫把「我一直在做」当成「我什么都懂」。这个推理错在哪?",
                options = listOf(
                    "它没有给出足够的样本",
                    "它把「重复执行」等同于「理解」",
                    "它的计算速度不够快",
                ),
                correctIndex = 1,
                explanation = "重复次数多不等于理解范围广 —— 这正是本关要拆穿的核心误区。",
            ),
        ),
        defense = listOf(
            CdQuestion(
                prompt = "预设规则和机器学习,最核心的区别是什么?",
                options = listOf(
                    "运行速度快慢不同",
                    "规则是人写死的,还是从样本里学出来的",
                    "是否需要用电",
                ),
                correctIndex = 1,
                explanation = "区别在于「规则从哪来」:人写死 vs 从数据中学。",
            ),
            CdQuestion(
                prompt = "一台「按下按钮就开门、别的什么都不会」的装置,属于?",
                options = listOf("固定规则", "机器学习", "两者都不是"),
                correctIndex = 0,
                explanation = "只会响应固定指令,属于典型的固定规则装置。",
            ),
        ),
    ),
    CdStage(
        index = 2,
        title = "听风失灵",
        enemyName = "断目机关蝠",
        enemyGlyph = "蝠",
        scene = "桥下回声交叠,桥上散落损坏的感知镜片。" +
            "机关蝠明明听到了求救,却朝相反方向冲去 —— 要查清它是听、是判断、还是行动时出了问题。",
        knowledge = "感知、推理、行动形成闭环,任一环节失效都会影响结果",
        attack = listOf(
            CdQuestion(
                prompt = "要定位机关蝠的故障,最合理的检查顺序是?",
                options = listOf(
                    "直接把整个机关蝠换掉",
                    "依次查:有没有听到 → 判断对不对 → 动作执行了没",
                    "只检查它的翅膀有没有坏",
                ),
                correctIndex = 1,
                explanation = "感知 → 推理 → 行动是一条闭环,顺着链路逐段排查才能定位故障点。",
            ),
            CdQuestion(
                prompt = "感知、推理、行动三者中任一环节出错,结果都会不对。这说明?",
                options = listOf(
                    "只要行动正确就没问题",
                    "三者是一条闭环,任一环失效都会让结果出错",
                    "感知最重要,其余两环可以忽略",
                ),
                correctIndex = 1,
                explanation = "闭环意味着没有哪一环可以被跳过 —— 这是本关的核心。",
            ),
            CdQuestion(
                prompt = "要区分它是「没听见」还是「听见了但判断错」,最直接的做法是?",
                options = listOf(
                    "看它最后有没有飞对方向",
                    "单独查看它的听觉输入记录",
                    "把它拆开清洗一遍",
                ),
                correctIndex = 1,
                explanation = "只看最终结果无法区分故障环节;要单独观察「感知」这一环的输入。",
            ),
        ),
        defense = listOf(
            CdQuestion(
                prompt = "机械臂抓杯子失败。下列哪一项属于「感知」环节的问题?",
                options = listOf(
                    "摄像头没识别出杯子的位置",
                    "机械臂电机没有转动",
                    "程序把抓取角度算错了",
                ),
                correctIndex = 0,
                explanation = "摄像头识别属于感知;电机属于行动,算角度属于推理。",
            ),
            CdQuestion(
                prompt = "一条完整链路上,「推理」环节最接近下面哪件事?",
                options = listOf("看见目标", "根据看到的信息做判断", "伸手去抓"),
                correctIndex = 1,
                explanation = "感知是「看到」,推理是「据此判断」,行动是「照做」。",
            ),
        ),
    ),
    CdStage(
        index = 3,
        title = "棋王越界",
        enemyName = "棋冠石将",
        enemyGlyph = "棋",
        scene = "黑白石阶围绕巨型棋盘,石将头戴棋冠。" +
            "它把下棋的胜绩当成全能证明,镇中人因此把辨药、修桥、预测天气都交给了它。",
        knowledge = "在特定任务上表现优秀,不等于具备通用能力",
        attack = listOf(
            CdQuestion(
                prompt = "石将棋艺无双,却被派去辨药、修桥、预测天气。问题出在哪?",
                options = listOf(
                    "它的计算速度不够快",
                    "把「某一领域擅长」当成了「通用能力」",
                    "它不愿意做这些工作",
                ),
                correctIndex = 1,
                explanation = "越界使用能力,是这个误区最常见的表现形式。",
            ),
            CdQuestion(
                prompt = "把一个只在下棋上训练过的系统派去认药草,最可能的结果是?",
                options = listOf(
                    "表现和下棋时一样出色",
                    "在药草任务上表现很差,因为它的能力只覆盖棋局",
                    "它会自动学会认药草",
                ),
                correctIndex = 1,
                explanation = "能力范围由训练任务决定,换个任务不会自动变强。",
            ),
            CdQuestion(
                prompt = "要判断一个 AI 能不能胜任新任务,最可靠的做法是?",
                options = listOf(
                    "看它在原来任务上有多强",
                    "看新任务是否落在它被训练覆盖的范围内",
                    "看它的名字里有没有「通用」二字",
                ),
                correctIndex = 1,
                explanation = "先划清能力边界,再谈能不能接新活。",
            ),
        ),
        defense = listOf(
            CdQuestion(
                prompt = "擅长下棋的系统,一定也能诊断疾病吗?",
                options = listOf("一定能", "不一定,能力是有适用范围的", "只要算力够就行"),
                correctIndex = 1,
                explanation = "专长不等于通用,换领域需要重新评估。",
            ),
            CdQuestion(
                prompt = "说一个模型「能力有边界」,意思是?",
                options = listOf(
                    "它在某些任务上会不可靠",
                    "它的电量有限",
                    "它不能联网",
                ),
                correctIndex = 0,
                explanation = "边界指的是适用范围,不是物理限制。",
            ),
        ),
    ),
    CdStage(
        index = 4,
        title = "偏听成阵",
        enemyName = "百声纸鹤",
        enemyGlyph = "鹤",
        scene = "符纸和声音铃铛悬挂在传信阁,纸鹤盘旋成阵。" +
            "它看似能辨百声,实际只听懂师父的一种口音 —— 不同居民的求助被当作杂音。",
        knowledge = "学习依赖样本,样本的覆盖范围影响识别表现",
        attack = listOf(
            CdQuestion(
                prompt = "纸鹤只听得懂师父一个人的口音。根因最可能是?",
                options = listOf(
                    "它的铃铛太小了",
                    "训练它的样本只覆盖了一种口音",
                    "居民说话声音太小",
                ),
                correctIndex = 1,
                explanation = "识别范围取决于样本覆盖 —— 没见过的口音自然识别不了。",
            ),
            CdQuestion(
                prompt = "要让纸鹤听懂全镇居民,最有效的做法是?",
                options = listOf(
                    "把它的音量调大",
                    "补充不同口音、不同环境的样本",
                    "让它多休息一会儿",
                ),
                correctIndex = 1,
                explanation = "扩大样本覆盖范围,才是解决识别盲区的正路。",
            ),
            CdQuestion(
                prompt = "一个系统在测试集上表现很好,上线后却频繁出错。最可能的原因是?",
                options = listOf(
                    "测试集与真实场景的样本分布不一致",
                    "服务器性能不足",
                    "用户操作方式不对",
                ),
                correctIndex = 0,
                explanation = "测试集没覆盖真实场景,就会出现「测得好、用起来差」。",
            ),
        ),
        defense = listOf(
            CdQuestion(
                prompt = "只用男声录音训练的系统,遇到女声容易出错。这属于哪类问题?",
                options = listOf("样本覆盖不足", "算力不足", "程序写错了"),
                correctIndex = 0,
                explanation = "训练样本没覆盖到的人群,就是识别盲区。",
            ),
            CdQuestion(
                prompt = "「样本覆盖范围」在这里指的是?",
                options = listOf(
                    "训练数据里包含了哪些情况",
                    "程序占用了多少内存",
                    "界面上能显示多少条记录",
                ),
                correctIndex = 0,
                explanation = "覆盖范围 = 训练时见过的情况有多全。",
            ),
        ),
    ),
)

/** Boss 关(第 5 关)的关卡信息,单独列出因为它不是答题型。 */
const val CD_BOSS_INDEX = 5
const val CD_BOSS_TITLE = "终局修枢"
const val CD_BOSS_ENEMY = "百面机枢"
const val CD_BOSS_GLYPH = "枢"
const val CD_BOSS_SCENE =
    "镇中心悬空的机关核心,投射着互相矛盾的判断。它不断切换面具,每张面具都宣称「我的答案绝不会错」。" +
        "击碎核心会让全镇机关停摆 —— 你需要为它设计一套可靠的运行办法,让机枢理解自己的能力边界," +
        "并在不确定时交给人来确认。"

/** Boss 制作任务(文档 §5 第五关)。 */
const val CD_BOSS_TASK =
    "为镇上的失物招领台制作一张「小机关设计卡」,帮助辨认水壶、书本等物品。" +
        "当照片模糊、物品陌生,或多种物品长得很像时,它应该如何处理?"

/** 作品说明需要覆盖的五项内容(文档 §5)。 */
val CD_BOSS_REQUIRED_POINTS = listOf(
    "能做什么,以及不能保证什么",
    "感知、判断、行动分别负责什么",
    "需要哪些有代表性的样本",
    "遇到看不清、没学过或不确定时,如何提示并交给人确认",
    "一个可能失败的例子,以及验证或修正办法",
)

/** 五维评分量表(文档 §4)。 */
val CD_BOSS_DIMENSIONS = listOf(
    CdBossDimension("知识与原理正确", 30),
    CdBossDimension("任务完成度", 25),
    CdBossDimension("个人解释与决策", 20),
    CdBossDimension("测试与修正", 15),
    CdBossDimension("表达清楚", 10),
)

data class CdBossDimension(val name: String, val maxScore: Int)

/** Boss 评审结果(本地规则评分)。 */
data class CdBossResult(
    val scores: List<Int>,
    val total: Int,
    val passed: Boolean,
    val advice: List<String>,
)

// ── 秘籍目录(2026-09-19 §6 追加:留下选择其他书本的入口)────────────────────────

/**
 * 一本秘籍对应的副本。
 *
 * 出处:策划方案 v2 §12「其他秘籍的名称与场景预留」——
 * 该节明确写着"以下只保留世界观名称、场景和 Boss 概念,**不在本期制作关卡、题目、动画或素材**";
 * §2 同时要求"本期地图只开放'雾隐机关镇'……若在界面露出,应标明'后续开放'"。
 * 因此本目录除《识机真诀》外,[available] 一律为 false,界面只作展示与占位,不可进入。
 */
data class CdManual(
    /** 秘籍名,如"识机真诀"。 */
    val name: String,
    /** 副本名称,如"雾隐机关镇"。 */
    val dungeon: String,
    /** 场景概念。 */
    val scene: String,
    /** Boss 名称。 */
    val boss: String,
    /** 本期是否开放(只有《识机真诀》为 true)。 */
    val available: Boolean,
)

/** 十本秘籍:第一本本期开放,其余九本按文档 §12 原样预留。 */
val CD_MANUALS: List<CdManual> = listOf(
    CdManual("识机真诀", "雾隐机关镇", "齿轮城门、听风桥、棋将台与传信阁", "百面机枢", available = true),
    CdManual("拆招心法", "千机工坊", "链条、流水线与多层木制机关", "千臂工匠", available = false),
    CdManual("万象谱", "万象镜林", "镜面、符纹与关系丝线交织的密林", "无相镜主", available = false),
    CdManual("寻径迷踪步", "回环迷宫", "道路旋转、石门移动的古代迷宫", "迷途阵主", available = false),
    CdManual("百炼识物诀", "百草试炼谷", "不同气候和光照下的相似药草", "千形药兽", available = false),
    CdManual("分门辨类掌", "分岔群岛", "纹理、颜色和地貌各异的群岛", "分界双生兽", available = false),
    CdManual("千层观心镜", "千层镜塔", "透镜与光路逐层连接的高塔", "叠影镜灵", available = false),
    CdManual("赏罚驭灵诀", "驭灵竞技谷", "机关赛道与奖励符阵遍布的山谷", "钻隙灵王", available = false),
    CdManual("听言解意篇", "回声书城", "文字漂浮、卷轴流动的古城", "千言书魇", available = false),
    CdManual("正心守道录", "守心城", "真伪面具、授权印记与守护结界", "伪面城主", available = false),
)
