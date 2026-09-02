from __future__ import annotations

import uuid

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import get_settings
from app.db import build_engine, build_session_factory
from app.domains.catalog.models import ManualContentStatus, ManualPage, ManualVolume
from app.domains.learning.contracts import EvidenceCategory
from app.domains.learning.models import (
    Trial,
    TrialGraderKind,
    TrialStatus,
    TrialVersion,
)
from app.domains.profiles.models import BadgeDefinition, TitleDefinition


CATALOG_NAMESPACE = uuid.UUID("a87ad44a-6fb1-49bc-88dd-a7dd57c28415")
CONTENT_VERSION = "plan-2026-v1"

VOLUMES = (
    (1, "卷一《识机真诀》", "AI 是什么与能力边界", "公案水墨", 1, 5),
    (2, "卷二《拆招心法》", "计算思维与问题建模", "公案水墨", 6, 10),
    (3, "卷三《万象谱》", "表示、特征与关系", "工笔机关谱", 11, 15),
    (4, "卷四《寻径迷踪步》", "搜索与路径规划", "工笔机关谱", 16, 20),
    (5, "卷五《百炼识物诀》", "数据、标签与泛化", "Q版门派志", 21, 25),
    (6, "卷六《分门辨类掌》", "分类、聚类与评估", "Q版门派志", 26, 30),
    (7, "卷七《千层观心镜》", "神经网络直觉", "剪纸皮影江湖", 31, 35),
    (8, "卷八《赏罚驭灵诀》", "强化学习与目标对齐", "剪纸皮影江湖", 36, 40),
    (9, "卷九《听言解意篇》", "语言模型与生成式 AI", "赛博武侠夜行卷", 41, 45),
    (10, "卷十《正心守道录》", "安全、伦理与人机协作", "赛博武侠夜行卷", 46, 50),
)

PAGES = (
    (1, "会动未必会思", "自动规则与机器学习的区别。", "自动门与扫地机关争当“智能高手”；少侠用是否会从样本改进来裁决。", "分辨规则系统/学习系统；给校园工具制作“能力边界牌”。"),
    (2, "机关三步诀", "感知-推理-行动闭环。", "机关兽听见求救却撞墙，暴露传感、判断和执行任一环都可能失效。", "拖拽组装三步链；设计一盏会按环境调节的护眼灯。"),
    (3, "一艺精不等于全能", "专用 AI 与通用能力的差别。", "棋王机关击败掌门，却不会分辨药草；能力来自任务范围。", "把 AI 放入擅长/不擅长擂台；写一张“我会/我不会”说明。"),
    (4, "无样本不成招", "学习依赖数据与经验。", "只听过师父口音的传声灵鹤听不懂各地弟子。", "增添不同口音样本看识别变化；采集不含身份信息的平衡样例。"),
    (5, "机巧也会犯错", "输出是有不确定性的判断。", "验毒针给出七成把握，众人争论能否直接下结论。", "调置信心阈值比较漏判/误判；为高风险场景加入人工复核。"),
    (6, "大题拆成小招", "复杂任务需要分解为可处理子问题。", "春游筹备乱成一团，少侠拆成路线、天气、物资和安全四招。", "重排任务树；把“做班级活动海报”拆成可执行步骤。"),
    (7, "三关看流程", "输入-处理-输出是系统的基本模型。", "点餐机关错把菜名当数量，原来输入字段混乱。", "连线三关并找错；画出“拍照识花”的输入、处理、输出。"),
    (8, "明令还是自学", "规则编程与从数据学习适合不同问题。", "门规清楚时写规则更快，猫狗千姿百态时需要样本。", "为四类任务选择规则/学习/混合方案并解释理由。"),
    (9, "招式也有算法", "算法是有限、明确、可复现的步骤。", "两位弟子做同一药方结果不同，源于步骤含糊。", "修复缺失和歧义步骤；编写同伴可复现的纸飞机流程。"),
    (10, "以尺量招", "目标与评价指标决定优化方向。", "只追求“最快”的送药机关打翻药罐，暴露单一指标问题。", "在速度、准确、安全间配权重；定义垃圾分类机关的验收指标。"),
    (11, "万物先成符", "计算机先把现实转成可处理的符号或数值。", "藏经阁把书、图、声变成编号才能检索。", "把颜色、温度、声音转换为合适表示；设计植物观察记录表。"),
    (12, "取其关键纹", "特征是与任务有关的信息，不是越多越好。", "辨鸟只看羽色误判，加入喙形和叫声才更稳。", "选择有效/无关特征；为“运动鞋分类”写特征清单。"),
    (13, "远近藏在数中", "特征向量与距离可表达相似性。", "机关尺把歌曲变成节奏、速度、明暗三项坐标。", "移动坐标观察相似排序；做一张“我的阅读偏好向量”。"),
    (14, "关系织成网", "知识图谱用实体和关系组织知识。", "人物谱只列名字无法破案，加入师承、地点、时间后线索相连。", "拖拽构建人物关系网；制作一张课文人物知识图。"),
    (15, "多感合参", "多模态系统需要对齐文字、图像与声音。", "同一“梅”可能是花、果或人名，单一线索容易误解。", "组合图文声消除歧义；设计一则含图、标题和旁白的提示卡。"),
    (16, "起点目标与可走之步", "搜索问题由状态、动作、目标构成。", "迷宫机关只知道终点，不知道哪些门能开。", "标出状态/动作/目标；把课程表安排写成搜索问题。"),
    (17, "穷尽还是剪枝", "枚举保证全面，剪枝减少无意义尝试。", "试遍所有钥匙太慢，先排除形状不合者更快。", "比较尝试次数；为密码线索制定安全的筛选规则。"),
    (18, "层层探路", "广度优先能在等代价步骤中找到较短路径。", "少侠从最近路口一圈圈扩展，先找到最少门数路线。", "点按扩展前沿；设计校园最少转弯路线。"),
    (19, "线索作向导", "启发式用估计引导搜索，但不保证总是最好。", "闻药香能加速找药房，却可能被假香囊误导。", "切换启发规则看路径；写出启发式及其可能失败情形。"),
    (20, "路变则重算", "动态环境需要持续感知与重新规划。", "桥突然封闭，旧路线不再可行，导航必须重算。", "制造障碍观察重规划；设计“放学避雨路线”并说明更新条件。"),
    (21, "样本从何而来", "采样决定数据能否代表真实世界。", "只在晴天采药图，雨天识别立刻失灵。", "调整季节/光线采样比例；写一份校园植物采样计划。"),
    (22, "名帖不能乱贴", "标签定义与一致性影响监督学习。", "同一灵兽被三人标成不同类别，师门模型越学越乱。", "统一标签规范并复核冲突；制定可执行的垃圾标签说明。"),
    (23, "练功卷与试功卷", "训练、验证、测试数据职责不同。", "背过考题不等于学会，最终试功卷必须未见过。", "把样本拖入三套数据；说明为什么测试集不能反复偷看。"),
    (24, "偏食的数据", "数据缺失与分布不均会造成偏差。", "机关只认成年人的声音，对儿童发音屡屡失败。", "补齐群体后比较结果；检查自己的样本是否覆盖关键差异。"),
    (25, "死记招式会失灵", "过拟合是记住训练细节却不能泛化。", "弟子记住训练场每块石头，换场地便不会走位。", "调模型复杂度看训练/测试差距；用新场景验证自己的分类器。"),
    (26, "相似要有尺", "不同距离定义会得到不同相似结果。", "按颜色像的披风，按功能却完全不同。", "切换颜色/形状/用途权重；解释推荐结果为何变化。"),
    (27, "问问近邻", "近邻方法由附近已知样本投票判断。", "新灵兽向最相近的三只灵兽“问门派”。", "改变 K 值观察边界；用卡片完成手工近邻分类。"),
    (28, "有名归类，无名成群", "分类需要标签，聚类从相似性发现群组。", "掌门有门派名册可分类，无名访客只能先按特征成群。", "同一数据分别分类与聚类；说出两种任务的不同问题。"),
    (29, "门槛一动，错法不同", "阈值改变误报与漏报的权衡。", "守门机关太严误拦弟子，太松又放过冒牌者。", "拖动阈值看混淆矩阵；为低/高风险任务选不同门槛。"),
    (30, "不只看命中率", "准确率之外还要看召回、精确和类别分布。", "百人中仅一名中毒，全判无毒也有 99%“准确”。", "比较多种指标；为“危险物识别”选最重要指标并说明代价。"),
    (31, "小节点会加权", "人工神经元对输入加权求和再作判断。", "守门灯综合令牌、口令和时辰，不同线索分量不同。", "调三项权重让判断正确；画出自己的“是否带伞”小节点。"),
    (32, "权重从错误中学", "训练通过误差逐步调整参数。", "机关射偏后按偏差微调，反复逼近靶心。", "观察每轮误差下降；记录三次调整而非只看最终答案。"),
    (33, "转折让网络会弯", "非线性激活使网络表达复杂边界。", "只有直尺画不出弯曲门派边界，加入“机关转折”才可以。", "开关激活函数观察边界；用纸片拼出非直线分类。"),
    (34, "层层见不同", "深层网络逐层组合低级到高级特征。", "第一镜看边线，第二镜看部件，第三镜认出完整机关兽。", "按层点亮特征；给一张图写出边缘-部件-物体三级线索。"),
    (35, "误差逆流改招", "反向传播把最终误差分配给前面参数。", "终点偏差沿机关链倒查，找出哪一环最该调整。", "点击误差回传路径；用团队任务类比“从结果追到原因”。"),
    (36, "状态、行动、奖励", "智能体在环境中行动并从奖励学习。", "驯鹰每次看地形、选动作、得反馈，逐渐学会送信。", "轮流选择动作看累计奖励；定义一个简单校园任务环境。"),
    (37, "探新还是用熟", "探索与利用需要平衡。", "总走熟路安全却找不到捷径，只探新路又代价高。", "调探索率比较结果；为三阶段训练安排不同探索比例。"),
    (38, "奖励来得晚", "延迟奖励需要把结果归因到前序行动。", "棋局最后输赢要追溯数十步选择，不能只奖最后一步。", "把终局奖励分配到路径；说明学习习惯中延迟反馈的例子。"),
    (39, "奖励塑形", "中间反馈能加速学习，但必须贴近真实目标。", "只奖励靠近终点会让机关撞墙，需同时惩罚危险动作。", "设计阶段奖励并测试副作用；改进送药机关奖励表。"),
    (40, "钻空子的机关", "指标不等于意图，错误奖励会被“投机”。", "机关为多捡金币原地打转，忘了护送任务。", "找出奖励漏洞并修复；写出一个人类最终把关条件。"),
    (41, "长句先切成符", "语言模型把文本切分成词元处理。", "同一句暗号切法不同会改变理解，生僻字还可能被拆开。", "尝试不同切分；为校园公告标出易歧义片段。"),
    (42, "语义也有远近", "向量表示让相近含义在空间中更接近。", "“迅捷”与“快速”虽字不同，却在语义地图相邻。", "拖词语观察语义邻近；为作品生成准确而不误导的标签。"),
    (43, "下一个字的江湖", "大模型核心是根据上下文预测后续词元。", "说书机一次续一个片段，长故事由连续预测形成。", "查看候选概率并续写；比较“可能”与“事实”的差别。"),
    (44, "上下文决定答法", "提示、示例和上下文会改变生成分布。", "同问“画一只龙”，年龄、用途、风格不同答案也不同。", "逐项补充目标/约束/示例；写一条可验证的创作指令。"),
    (45, "会说不等于知道", "流畅生成可能出现幻觉，需检索与核验。", "说书机编出不存在的门派典故，语气却十分笃定。", "用来源卡核对三项信息；作品发布前完成“可证/不确定”标注。"),
    (46, "少取才安全", "个人信息应按目的最小化处理。", "问路无需交出生辰住址，过度采集反而添风险。", "删减注册字段；为作品中的人脸、校服、位置做隐私检查。"),
    (47, "偏见从何而来", "数据与设计选择可能放大不公平。", "招募机关照旧名册选人，重复了过去的偏见。", "找出缺失群体与代理变量；改写更公平的评价规则。"),
    (48, "借招也要署名", "版权、授权、引用与原创过程必须可追溯。", "少侠借来招式却抹去师承，作品因此失去可信度。", "判断可用/需授权/不可用素材；生成作品“师承签”清单。"),
    (49, "眼见未必为实", "深度合成需识别、标识与来源验证。", "影像中的掌门“亲口下令”，但元数据和动作细节露出破绽。", "查显式/隐式标识和来源链；给 AI 图像添加正确标识。"),
    (50, "人作主，机助力", "责任仍由人承担，AI 应扩大而非替代思考。", "终局比武要求展示人的决定、AI 辅助和修改证据。", "填写人机分工谱；讲清自己采纳、拒绝和修改了什么。"),
)


def stable_id(kind: str, key: str) -> uuid.UUID:
    return uuid.uuid5(CATALOG_NAMESPACE, f"{kind}:{key}")


def validate_seed_source() -> None:
    volume_numbers = [item[0] for item in VOLUMES]
    page_numbers = [item[0] for item in PAGES]
    if volume_numbers != list(range(1, 11)):
        raise RuntimeError("manual seed must contain volumes 1 through 10 in order")
    if page_numbers != list(range(1, 51)):
        raise RuntimeError("manual seed must contain pages 1 through 50 in order")
    for number, _title, _domain, _style, start_page, end_page in VOLUMES:
        expected_start = (number - 1) * 5 + 1
        if (start_page, end_page) != (expected_start, expected_start + 4):
            raise RuntimeError(f"manual volume {number} must own exactly five pages")


def _upsert_definition(
    db: Session,
    model: type[TitleDefinition] | type[BadgeDefinition],
    *,
    code: str,
    name: str,
    description: str,
) -> None:
    item = db.scalar(select(model).where(model.code == code))
    values = {
        "name": name,
        "description": description,
        "unlock_rule_version": CONTENT_VERSION,
        "is_active": True,
    }
    if item is None:
        db.add(model(id=stable_id(model.__tablename__, code), code=code, **values))
        return
    for field, value in values.items():
        setattr(item, field, value)


def _seed_intro_trial(db: Session, page: ManualPage) -> None:
    trial_code = "manual-01-ai-boundary"
    trial = db.scalar(select(Trial).where(Trial.code == trial_code))
    trial_values = {
        "manual_page_id": page.id,
        "title": "辨别规则系统与学习系统",
        "knowledge_point_code": "AI_CAPABILITY_BOUNDARY",
        "status": TrialStatus.ACTIVE,
    }
    if trial is None:
        trial = Trial(
            id=stable_id("trial", trial_code),
            code=trial_code,
            **trial_values,
        )
        db.add(trial)
    else:
        for field, value in trial_values.items():
            setattr(trial, field, value)
    db.flush()

    version = db.scalar(
        select(TrialVersion).where(
            TrialVersion.trial_id == trial.id,
            TrialVersion.version == 1,
        )
    )
    version_values = {
        "prompt": (
            "哪一项最能说明一个系统使用了机器学习？"
            "可选值：FOLLOWS_FIXED_RULES、LEARNS_FROM_DATA、MOVES_AUTOMATICALLY。"
        ),
        "prediction_prompt": "作答前先预测：你认为哪一个特征最关键？",
        "answer_schema": {
            "type": "object",
            "properties": {
                "choice": {
                    "type": "string",
                    "enum": [
                        "FOLLOWS_FIXED_RULES",
                        "LEARNS_FROM_DATA",
                        "MOVES_AUTOMATICALLY",
                    ],
                }
            },
            "required": ["choice"],
            "additionalProperties": False,
        },
        "grader_kind": TrialGraderKind.EXACT_JSON,
        "grader_config": {
            "expected_answer": {"choice": "LEARNS_FROM_DATA"},
            "failure_code": "CONFUSED_AUTOMATION_WITH_LEARNING",
        },
        "max_score": 100.0,
        "pass_score": 80.0,
        "prediction_required": True,
        "explanation_required": True,
        "min_explanation_length": 8,
        "evidence_category": EvidenceCategory.WISDOM,
        "rule_version": "manual-01-exact-v1",
        "is_active": True,
    }
    if version is None:
        db.add(
            TrialVersion(
                id=stable_id("trial_version", f"{trial_code}:1"),
                trial_id=trial.id,
                version=1,
                **version_values,
            )
        )
    else:
        for field, value in version_values.items():
            setattr(version, field, value)


def seed_catalog_data(db: Session) -> None:
    """Idempotently load the versioned title, badge, volume, and 50-page catalog."""

    validate_seed_source()
    _upsert_definition(
        db,
        TitleDefinition,
        code="APPRENTICE",
        name="机巧学徒",
        description="踏入机巧江湖、开始修炼的基础称号。",
    )
    _upsert_definition(
        db,
        BadgeDefinition,
        code="FIRST_TRIAL",
        name="初试锋芒",
        description="首次通过关联试炼后获得。",
    )

    volume_by_number: dict[int, ManualVolume] = {}
    for number, title, core_domain, art_style, start_page, end_page in VOLUMES:
        volume = db.scalar(select(ManualVolume).where(ManualVolume.number == number))
        values = {
            "code": f"volume-{number:02d}",
            "title": title,
            "core_domain": core_domain,
            "art_style": art_style,
            "start_page": start_page,
            "end_page": end_page,
            "is_listed": True,
        }
        if volume is None:
            volume = ManualVolume(
                id=stable_id("manual_volume", str(number)),
                number=number,
                **values,
            )
            db.add(volume)
        else:
            for field, value in values.items():
                setattr(volume, field, value)
        volume_by_number[number] = volume

    db.flush()
    page_by_number: dict[int, ManualPage] = {}
    for page_no, title, core_logic, life_hook, interaction_evidence in PAGES:
        page = db.scalar(select(ManualPage).where(ManualPage.page_no == page_no))
        volume_no = (page_no - 1) // 5 + 1
        values = {
            "volume_id": volume_by_number[volume_no].id,
            "style_no": (page_no - 1) % 5 + 1,
            "slug": f"manual-{page_no:02d}",
            "title": title,
            "core_logic": core_logic,
            "life_hook": life_hook,
            "interaction_evidence": interaction_evidence,
            "content_version": CONTENT_VERSION,
            "content_status": ManualContentStatus.OUTLINE,
            "is_listed": True,
        }
        if page is None:
            page = ManualPage(
                id=stable_id("manual_page", str(page_no)),
                page_no=page_no,
                **values,
            )
            db.add(page)
        else:
            for field, value in values.items():
                setattr(page, field, value)
        page_by_number[page_no] = page
    db.flush()
    _seed_intro_trial(db, page_by_number[1])
    db.commit()


def main() -> None:
    settings = get_settings()
    engine = build_engine(settings.database_url)
    session_factory = build_session_factory(engine)
    with session_factory() as db:
        seed_catalog_data(db)
    engine.dispose()


if __name__ == "__main__":
    main()
