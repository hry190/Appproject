package com.jueqiao.jianghu.ui.screens.wushuhuan

import androidx.annotation.DrawableRes
import com.jueqiao.jianghu.R

/**
 * Versioned presentation copy for the first manual volume.
 *
 * This is editorial content only. Unlocks, rewards and learning evidence still come from the
 * existing learning APIs; nothing in this file is allowed to advance a learner's state.
 */
internal data class ManualLessonContent(
    val pageNo: Int,
    val title: String,
    val coreLogic: String,
    val comicPageRange: IntRange,
    val leaves: List<ManualKnowledgeLeaf>,
)

internal data class ManualKnowledgeLeaf(
    val section: String,
    val title: String,
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList(),
    val keyLine: String? = null,
    @DrawableRes val comicImages: List<Int> = emptyList(),
    val action: ManualLeafAction? = null,
)

internal enum class ManualLeafAction {
    OPEN_COMIC,
    OPEN_TRIAL,
    USE_IN_CREATION,
}

internal val volumeOneLessons: List<ManualLessonContent> = listOf(
    ManualLessonContent(
        pageNo = 1,
        title = "会动未必会思",
        coreLogic = "自动规则与机器学习的区别",
        comicPageRange = 0..2,
        leaves = listOf(
            ManualKnowledgeLeaf(
                section = "一  漫画回顾",
                title = "先猜一猜",
                paragraphs = listOf(
                    "自动门看见人会打开，扫地机关会自己绕开桌腿。它们都会行动，可这是否说明它们真的在学习？",
                    "先留意两件事：是谁决定了它们下一步做什么；经历新的事情之后，它们原来的判断方法会不会改变。",
                ),
                bullets = listOf(
                    "甲  只要会自己行动，就一定是在学习",
                    "乙  会行动不等于会学习，还要看它会不会从样本和反馈中改变判断",
                ),
                keyLine = "先记住现在的想法，再去漫画和试炼中找证据。",
                comicImages = listOf(
                    R.drawable.img_volume1_image_233,
                    R.drawable.img_volume1_image_230,
                ),
                action = ManualLeafAction.OPEN_COMIC,
            ),
            ManualKnowledgeLeaf(
                section = "二  因果揭示",
                title = "关键不在会不会动",
                paragraphs = listOf(
                    "自动装置可以动作很快、步骤很多，甚至看起来像在作决定，但它仍可能只是执行人提前写好的条件。",
                    "机器学习系统的不同之处，是它会利用许多样本形成判断方法，并可能根据新样本或反馈调整之后的判断。",
                ),
                bullets = listOf(
                    "固定规则：条件由人明确写好，遇到同样条件就执行同样动作",
                    "机器学习：从样本中寻找规律，再用规律判断没有见过的新情况",
                    "两者可以同时存在于同一个装置中",
                ),
                keyLine = "会自动，只说明它会按机制行动；会学习，要看它是否会从样本和反馈中调整判断。",
                action = ManualLeafAction.OPEN_TRIAL,
            ),
            ManualKnowledgeLeaf(
                section = "三  规则机关",
                title = "什么是固定规则",
                paragraphs = listOf(
                    "固定规则像一张事先写好的门规。人先写出条件和动作，装置接收信息后检查条件；条件满足，就执行规定动作。",
                    "一次新的经历通常不会自己改写门规。除非有人修改程序或参数，下次遇到相同条件，它仍会按原来的方法处理。",
                ),
                bullets = listOf(
                    "感知信息  →  检查人写好的条件  →  执行动作",
                    "到点响起的上课铃：时间到了就响",
                    "烟雾报警器：浓度超过设定值就报警",
                    "温控风扇：温度超过门槛就启动",
                    "感应门：传感器检测到人就开门",
                ),
                keyLine = "动作复杂不等于会学习，先找一找它背后有没有固定条件。",
            ),
            ManualKnowledgeLeaf(
                section = "四  样本学习",
                title = "机器怎样从经验中学",
                paragraphs = listOf(
                    "样本是机器用来寻找规律的例子。图片识别会看到许多带有名称的图片；输入法会记录在特定上下文中，人们最后选择了哪个词。",
                    "系统比较这些例子，调整内部参数，形成一种判断方法。之后遇到新情况，它给出预测；新的反馈还可能继续影响后面的判断。",
                ),
                bullets = listOf(
                    "收集样本  →  寻找规律  →  形成判断方法",
                    "面对新情况  →  给出预测  →  根据反馈继续调整",
                    "输入法可根据长期选择调整候选词",
                    "清扫设备可根据多次记录优化常走路线",
                ),
                keyLine = "会学习也不代表它像人一样理解、思考或拥有情感。",
            ),
            ManualKnowledgeLeaf(
                section = "五  四问辨别",
                title = "看见智能装置时问什么",
                paragraphs = listOf(
                    "不要只看外表像不像人，也不要只看动作是否漂亮。按顺序问下面四个问题，通常就能找到判断依据。",
                ),
                bullets = listOf(
                    "它接收了什么信息？例如时间、距离、图片或声音",
                    "它根据什么决定动作？例如明确门槛，还是从大量例子得到的规律",
                    "决定方法是谁给出的？是人直接写好，还是训练过程调整出来",
                    "新样本和反馈会不会改变它之后的判断？如果不会，更可能只是固定规则",
                ),
                keyLine = "先找输入、再找判断依据，最后看经验能否改变以后。",
            ),
            ManualKnowledgeLeaf(
                section = "六  易混边界",
                title = "这些现象都不能单独作证",
                paragraphs = listOf(
                    "日常说法常把“自动”“聪明”“会学习”混在一起。判断时需要把看见的现象和背后的机制分开。",
                ),
                bullets = listOf(
                    "会说话，不等于会学习；可能只是在播放预设语句",
                    "动作复杂，不等于使用了机器学习；复杂流程也能由规则组成",
                    "软件更新，不等于机器自己学会；更新可能来自开发者",
                    "使用机器学习，不等于拥有人类思维或情感",
                    "同一系统可以让规则负责安全底线，让学习模型负责判断",
                ),
                keyLine = "是否智能不能只看效果，还要追问效果是怎样产生的。",
            ),
            ManualKnowledgeLeaf(
                section = "七  检查理解",
                title = "给三个机关分一分",
                paragraphs = listOf(
                    "先独立判断，再查看解释。遇到资料不足的情况，可以回答“目前不能确定”，而不是勉强猜成绝对结论。",
                ),
                bullets = listOf(
                    "固定时间响铃：时间条件由人写好，属于固定规则",
                    "输入法长期调整候选词：使用历史样本改变之后的排序，更像学习系统",
                    "感应到障碍就停止的小车：只凭这一现象无法证明会学习，也可能只是距离门槛",
                ),
                keyLine = "有证据时再下结论；证据不足，也是一个合理答案。",
                action = ManualLeafAction.OPEN_TRIAL,
            ),
            ManualKnowledgeLeaf(
                section = "八  知行迁移",
                title = "设计失物寻回机关兽",
                paragraphs = listOf(
                    "设想一只帮助校园寻找失物的机关兽。先定义它能看见或读取什么，再区分哪些动作适合固定规则，哪些判断需要从样本中学习。",
                    "最后检查错误的代价。涉及认领、身份或重要物品时，机器只能提供线索，必须让老师或失主再次确认。",
                ),
                bullets = listOf(
                    "它能感知哪些信息，又有哪些信息不该收集",
                    "哪些安全动作必须写成固定规则",
                    "哪些识别能力需要多样、合适的样本",
                    "它最可能在什么情况下认错",
                    "哪些决定必须交给人复核",
                ),
                keyLine = "作品要写清能力边界：它会什么、不会什么、错了由谁确认。",
                action = ManualLeafAction.USE_IN_CREATION,
            ),
        ),
    ),
    ManualLessonContent(
        pageNo = 2,
        title = "机关三步诀",
        coreLogic = "感知 推理 行动闭环",
        comicPageRange = 3..5,
        leaves = listOf(
            ManualKnowledgeLeaf(
                section = "一  漫画回顾",
                title = "听见求救却撞了墙",
                paragraphs = listOf(
                    "机关兽听见呼救便冲过去，却撞在墙上。问题未必是它“不聪明”，可能是听错了方向、判断错了路线，也可能是腿脚没有按命令行动。",
                    "观察漫画时，把每个现象放回感知、推理、行动中的一环。",
                ),
                comicImages = listOf(
                    R.drawable.img_volume1part4_image_237,
                    R.drawable.img_volume1part4_image_236,
                ),
                keyLine = "先判断错误发生在哪一环，再决定怎样修。",
                action = ManualLeafAction.OPEN_COMIC,
            ),
            ManualKnowledgeLeaf(
                section = "二  三步闭环",
                title = "从环境到动作",
                paragraphs = listOf(
                    "一个能与环境互动的系统，通常要不断完成三件事：接收信息、根据目标作判断、把判断变成动作。动作改变环境后，又会产生新的信息。",
                ),
                bullets = listOf(
                    "感知：摄像头、麦克风、距离或温度传感器收集信息",
                    "推理：规则或模型根据输入与目标选择下一步",
                    "行动：电机、屏幕、声音或消息真正影响环境",
                    "行动后的结果会成为下一轮感知的新输入",
                ),
                keyLine = "感知  →  推理  →  行动  →  新的感知。",
            ),
            ManualKnowledgeLeaf(
                section = "三  感知之误",
                title = "看错与听错",
                paragraphs = listOf(
                    "感知环节负责把现实转成系统能处理的信息。镜头被遮挡、光线太暗、声音太吵、传感器位置不合适，都会让后面的判断建立在错误输入上。",
                ),
                bullets = listOf(
                    "先检查传感器是否真的收到信号",
                    "比较原始输入与现实是否一致",
                    "改变光线、距离和噪声，观察错误是否消失",
                    "不要一看到错误结果就立刻责怪模型",
                ),
                keyLine = "输入不可靠，再精巧的判断也可能得出错误结果。",
            ),
            ManualKnowledgeLeaf(
                section = "四  推理之误",
                title = "信息收到却想错一步",
                paragraphs = listOf(
                    "感知正确并不保证判断正确。规则可能漏写条件，模型可能没有见过类似样本，目标也可能设置得太单一。",
                    "排查时要把输入保存下来，用同样输入重复判断，观察系统为何选择这一步。",
                ),
                bullets = listOf(
                    "规则是否覆盖当前情况",
                    "训练样本是否包含相似场景",
                    "目标是否只追求速度而忽略安全",
                    "判断结果是否带有不确定性",
                ),
                keyLine = "知道系统根据什么判断，才能修正判断。",
            ),
            ManualKnowledgeLeaf(
                section = "五  行动之误",
                title = "想对了也可能做不到",
                paragraphs = listOf(
                    "系统可能已经选对动作，但执行部件卡住、电量不足、网络中断，或现实环境不允许完成。此时继续修改判断方法并不能解决问题。",
                ),
                bullets = listOf(
                    "确认命令是否已经发出",
                    "确认执行部件是否收到并完成命令",
                    "为失败动作准备停止、提醒和人工接管",
                    "执行结果要重新反馈给判断环节",
                ),
                keyLine = "判断正确不等于任务完成，还要验证动作是否真的发生。",
            ),
            ManualKnowledgeLeaf(
                section = "六  排查顺序",
                title = "从证据最清楚处开始",
                paragraphs = listOf(
                    "面对失败，先重现现象，再沿着三步链逐项检查。一次只改变一个条件，才能知道哪项修改真正起作用。",
                ),
                bullets = listOf(
                    "记录当时的环境与输入",
                    "检查感知结果是否正确",
                    "检查判断依据与输出命令",
                    "检查行动是否完成并返回结果",
                    "修正后用原场景和新场景各验证一次",
                ),
                keyLine = "不要同时乱改三环，否则即使成功，也不知道为什么成功。",
            ),
            ManualKnowledgeLeaf(
                section = "七  知行迁移",
                title = "设计一盏护眼灯",
                paragraphs = listOf(
                    "为阅读桌设计一盏会随环境调节的护眼灯。写清它感知什么、怎样判断、如何行动，以及每一环失败时怎样安全处理。",
                ),
                bullets = listOf(
                    "感知：环境亮度、是否有人、阅读距离",
                    "推理：何时调亮、调暗或提醒休息",
                    "行动：改变亮度、发出温和提醒",
                    "安全：传感器异常时保持舒适亮度，不突然熄灭",
                ),
                keyLine = "好的设计会同时说明正常闭环与失败退路。",
                action = ManualLeafAction.USE_IN_CREATION,
            ),
        ),
    ),
    ManualLessonContent(
        pageNo = 3,
        title = "一艺精不等于全能",
        coreLogic = "专用AI与通用能力的差别",
        comicPageRange = 6..7,
        leaves = listOf(
            ManualKnowledgeLeaf(
                section = "一  漫画回顾",
                title = "棋王为何不识药草",
                paragraphs = listOf(
                    "棋王机关能算出厉害的棋步，却分不清药草。它在一个任务上的优秀表现，来自特定目标、数据和训练方法，不能自动搬到完全不同的任务。",
                ),
                comicImages = listOf(
                    R.drawable.img_volume1part7_image_243,
                    R.drawable.img_volume1part8_image_246,
                ),
                keyLine = "先问它擅长的任务是什么，再谈它有多聪明。",
                action = ManualLeafAction.OPEN_COMIC,
            ),
            ManualKnowledgeLeaf(
                section = "二  任务边界",
                title = "能力总有适用范围",
                paragraphs = listOf(
                    "专用AI通常为一个明确目标训练，例如识别某类图片、推荐候选词或计算棋步。输入形式、目标和评价方式一变，原能力就可能失效。",
                    "例如会下棋的系统接收棋盘位置并输出落子；整理书包却要识别物品、理解课程安排，还要判断哪些东西属于谁。两项任务的输入、目标和检验方法都不同，不能只靠同一项专长直接完成。",
                ),
                bullets = listOf(
                    "训练时解决的是什么问题",
                    "允许输入哪些信息",
                    "输出代表什么",
                    "在哪些环境和人群中验证过",
                ),
                keyLine = "能力说明必须连同任务和条件一起阅读。",
            ),
            ManualKnowledgeLeaf(
                section = "三  专长从何来",
                title = "精通一艺的原因",
                paragraphs = listOf(
                    "一个围棋系统能在棋盘上表现出色，是因为规则稳定、目标明确，还有大量棋局可供训练和比较。它并没有因此学会识药、扫地或理解同伴的心情。",
                ),
                bullets = listOf(
                    "清楚的任务定义让系统知道要优化什么",
                    "相关样本让系统学习这个任务中的规律",
                    "专门评价让开发者反复改进同一项能力",
                    "这些条件不会自动产生其他能力",
                ),
                keyLine = "专长可以很强，但专长仍然是专长。",
            ),
            ManualKnowledgeLeaf(
                section = "四  过度推断",
                title = "别被一次精彩表现骗过",
                paragraphs = listOf(
                    "人容易把流畅回答、漂亮动作或一次成功想成全面理解。更稳妥的方法是用新任务、陌生场景和边界案例继续验证。",
                ),
                bullets = listOf(
                    "会写故事，不代表事实一定准确",
                    "会认常见照片，不代表能认夜间或遮挡图片",
                    "会执行口令，不代表理解口令背后的意图",
                    "一次成功不能代替多种情境的验证",
                ),
                keyLine = "评价能力要看一组证据，而不是一个惊艳瞬间。",
            ),
            ManualKnowledgeLeaf(
                section = "五  检查理解",
                title = "为能力画出边界",
                paragraphs = listOf(
                    "给一个“会识花”的系统写说明牌。不要只写“能识花”，还要说明它看过哪些花、适合什么照片、遇到哪些情况会不可靠。",
                ),
                bullets = listOf(
                    "我会：识别训练中覆盖的常见花朵图片",
                    "我不会：判断花朵是否有毒或适合食用",
                    "我可能出错：光线太暗、花被遮挡、品种没见过",
                    "需要人确认：涉及安全和健康的决定",
                ),
                keyLine = "会什么与不会什么同样重要。",
            ),
            ManualKnowledgeLeaf(
                section = "六  知行迁移",
                title = "制作能力边界牌",
                paragraphs = listOf(
                    "选择一个校园AI工具，为它制作“我会／我不会”说明。用具体任务和场景描述，避免“什么都懂”“绝不会错”等绝对说法。",
                ),
                bullets = listOf(
                    "写出一个已经验证的擅长任务",
                    "写出两个超出范围的任务",
                    "写出一种可能让它失效的环境",
                    "写出需要人负责的最终决定",
                ),
                keyLine = "清楚的边界能帮助人正确使用工具。",
                action = ManualLeafAction.USE_IN_CREATION,
            ),
        ),
    ),
    ManualLessonContent(
        pageNo = 4,
        title = "无样本不成招",
        coreLogic = "学习依赖数据与经验",
        comicPageRange = 8..10,
        leaves = listOf(
            ManualKnowledgeLeaf(
                section = "一  漫画回顾",
                title = "灵鹤为何听不懂",
                paragraphs = listOf(
                    "传声灵鹤只听过师父一种口音，换成其他弟子说话就频频认错。它并不是故意偏心，而是过去的样本没有覆盖真实世界的差异。",
                ),
                comicImages = listOf(
                    R.drawable.img_volume1part9_image_250,
                    R.drawable.img_volume1part10_image_253,
                ),
                keyLine = "系统能学到什么，受到它见过哪些例子的限制。",
                action = ManualLeafAction.OPEN_COMIC,
            ),
            ManualKnowledgeLeaf(
                section = "二  样本是什么",
                title = "给机器看的例子",
                paragraphs = listOf(
                    "样本是用来发现规律的具体记录，可以是一张图片、一段声音、一行文字或一次操作结果。只有与任务相关并带有清楚含义的样本，才能帮助系统学习。",
                ),
                bullets = listOf(
                    "识别花朵：不同花朵的图片与正确名称",
                    "识别语音：声音片段与对应文字",
                    "推荐图书：阅读选择与之后的反馈",
                    "数量多不代表一定好，内容和来源也很重要",
                ),
                keyLine = "先说明任务，再判断什么样的记录才算有效样本。",
            ),
            ManualKnowledgeLeaf(
                section = "三  数量与覆盖",
                title = "见得多还要见得广",
                paragraphs = listOf(
                    "如果一千张照片都在同一地点、同一光线下拍摄，它们虽然很多，仍可能无法代表其他环境。覆盖是指样本包含任务中会遇到的重要差异。",
                ),
                bullets = listOf(
                    "不同光线、角度、距离和遮挡",
                    "不同设备产生的画面或声音",
                    "目标用户真实会遇到的表达方式",
                    "少见但后果严重的边界情况",
                ),
                keyLine = "样本数量回答“有多少”，覆盖范围回答“见过哪些情况”。",
            ),
            ManualKnowledgeLeaf(
                section = "四  平衡与标注",
                title = "样本不能偏食",
                paragraphs = listOf(
                    "某一类样本远多于其他类别时，系统可能总猜数量最多的那一类。标注如果含糊或彼此矛盾，系统也会学到混乱的对应关系。",
                ),
                bullets = listOf(
                    "检查每一类是否都有足够例子",
                    "为标签写清楚可以执行的定义",
                    "多人标注出现分歧时先讨论规则",
                    "发现明显错误样本要修正并记录版本",
                ),
                keyLine = "平衡让各类都有被看见的机会，一致标注让例子的含义稳定。",
            ),
            ManualKnowledgeLeaf(
                section = "五  练功与试功",
                title = "训练集不能冒充考试",
                paragraphs = listOf(
                    "训练样本用来调整方法，验证样本帮助选择方案，测试样本用于最后检查陌生情况。若反复偷看测试答案，结果只说明记住了试卷，不能说明真正学会。",
                ),
                bullets = listOf(
                    "训练集：练习并调整参数",
                    "验证集：比较方案并发现过拟合",
                    "测试集：最后一次公平检查",
                    "同一条记录不能同时充当训练和最终测试证据",
                ),
                keyLine = "真正的本领，要用没见过的例子检验。",
            ),
            ManualKnowledgeLeaf(
                section = "六  安全采样",
                title = "够用即可 少取为安",
                paragraphs = listOf(
                    "为校园任务收集样本时，只记录完成任务所需的信息。能用物品类别解决的问题，就不收集姓名、人脸、班级和精确位置。",
                ),
                bullets = listOf(
                    "先写明收集目的和最少字段",
                    "避免真实姓名、联系方式和精确位置",
                    "展示作品前检查校服、门牌和人脸",
                    "样本用完后按规则保存或删除",
                ),
                keyLine = "样本更丰富不等于个人信息收得更多。",
            ),
            ManualKnowledgeLeaf(
                section = "七  知行迁移",
                title = "拟一份声音样本计划",
                paragraphs = listOf(
                    "为只能听懂一种口音的传声灵鹤改进样本。列出要覆盖的语速、音量和环境噪声，并说明如何避免收集学生身份。",
                ),
                bullets = listOf(
                    "覆盖不同语速和自然表达方式",
                    "加入安静与普通环境噪声",
                    "只保存任务需要的声音内容",
                    "用匿名编号代替姓名和班级",
                ),
                keyLine = "好样本既要有代表性，也要守住隐私边界。",
                action = ManualLeafAction.USE_IN_CREATION,
            ),
        ),
    ),
    ManualLessonContent(
        pageNo = 5,
        title = "机巧也会犯错",
        coreLogic = "输出是带有不确定性的判断",
        comicPageRange = 11..13,
        leaves = listOf(
            ManualKnowledgeLeaf(
                section = "一  漫画回顾",
                title = "七成把握能否定案",
                paragraphs = listOf(
                    "验毒针说有七成把握，众人却把它当成确定结论。模型输出常是根据已见样本作出的可能性判断，不能自动代替事实和责任。",
                ),
                comicImages = listOf(
                    R.drawable.img_volume1part12_image_270,
                    R.drawable.img_volume1part13_image_267,
                ),
                keyLine = "看见结果时，也要看把握有多大、错了会怎样。",
                action = ManualLeafAction.OPEN_COMIC,
            ),
            ManualKnowledgeLeaf(
                section = "二  不确定性",
                title = "判断不是保证",
                paragraphs = listOf(
                    "模型根据过去的样本估计当前情况最像哪一类。样本有限、环境变化或输入模糊时，判断会更不稳定。分数较高也不等于绝对正确。",
                ),
                bullets = listOf(
                    "置信度表示模型对自己判断的相对把握",
                    "它不是事情真实发生的百分比保证",
                    "不同模型的分数不能随意直接比较",
                    "重要决定要结合证据与人工复核",
                ),
                keyLine = "把模型结果看作线索，而不是不可质疑的判决。",
            ),
            ManualKnowledgeLeaf(
                section = "三  两种错法",
                title = "误报与漏报",
                paragraphs = listOf(
                    "系统把正常情况判成异常，叫误报；把真正异常漏掉，叫漏报。两种错误无法总是同时降到零，哪一种更危险取决于具体任务。",
                ),
                bullets = listOf(
                    "误报：普通物品被错判为危险物",
                    "漏报：真正危险物没有被发现",
                    "作业提醒误报会造成打扰",
                    "安全检查漏报可能造成更严重后果",
                ),
                keyLine = "讨论准确率之前，先说清最担心哪一种错误。",
            ),
            ManualKnowledgeLeaf(
                section = "四  门槛权衡",
                title = "门槛一动 错法不同",
                paragraphs = listOf(
                    "把报警门槛调低，系统更容易报告异常，漏报可能减少，误报却会增加；门槛调高时通常相反。没有一个门槛适合所有场景。",
                ),
                bullets = listOf(
                    "低风险推荐可以容许少量不准确",
                    "医疗、安全等高风险任务需要更严格流程",
                    "门槛应结合错误后果、复核成本和使用对象",
                    "调整后要用新数据重新验证",
                ),
                keyLine = "门槛不是越高或越低越好，而要与风险相配。",
            ),
            ManualKnowledgeLeaf(
                section = "五  人来把关",
                title = "高风险决定留给人",
                paragraphs = listOf(
                    "当结果会影响健康、安全、权益或重要机会时，系统不应独自作最终决定。它可以筛选和提醒，但人要查看原始证据、听取解释并承担责任。",
                ),
                bullets = listOf(
                    "低把握结果自动转交人工",
                    "保留原始信息供复核，而非只显示一个分数",
                    "允许用户提出异议和补充信息",
                    "系统异常时能够停止并采用安全方案",
                ),
                keyLine = "机器提供判断，人负责核验、决定和解释。",
            ),
            ManualKnowledgeLeaf(
                section = "六  检查理解",
                title = "不同场景如何把关",
                paragraphs = listOf(
                    "比较三种任务：推荐一本课外书、判断操场是否积水、判断某人是否携带危险物。它们容许的错误、复核方式和责任并不相同。",
                ),
                bullets = listOf(
                    "推荐错书：后果较轻，可让学生自行更换",
                    "积水提醒：可结合传感器和现场查看",
                    "危险判断：不能只凭模型结果处罚某人",
                    "风险越高，越需要证据、解释与人工确认",
                ),
                keyLine = "同一个准确率，在不同任务里可能代表完全不同的风险。",
            ),
            ManualKnowledgeLeaf(
                section = "七  知行迁移",
                title = "为作品设计安全退路",
                paragraphs = listOf(
                    "选择一个会判断的机关作品，写出它可能出现的误报和漏报，再为低把握、传感器异常和人工不同意设计退路。",
                ),
                bullets = listOf(
                    "何时只提醒，不自动执行",
                    "何时暂停并请求人确认",
                    "怎样让人看到判断依据",
                    "错误发生后怎样纠正并避免重复伤害",
                ),
                keyLine = "可靠的作品不假装永不出错，而是准备好出错后的处理办法。",
                action = ManualLeafAction.USE_IN_CREATION,
            ),
        ),
    ),
)

internal fun volumeOneLesson(pageNo: Int): ManualLessonContent? =
    volumeOneLessons.firstOrNull { it.pageNo == pageNo }

internal fun readableLeafCount(state: String, totalLeaves: Int): Int = when (state) {
    "TEACHING", "MASTERED", "LEARNED" -> totalLeaves
    "DISCOVERED" -> minOf(2, totalLeaves)
    // 未闻可完成“先猜—揭示”的概念引入，但深入讲解仍由真实习得状态控制。
    "UNSEEN" -> minOf(2, totalLeaves)
    else -> 0
}
