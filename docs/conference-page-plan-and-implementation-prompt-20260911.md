# 大会页前后端制作计划与开发提示词

## 1 范围和结论

本轮只改“作品”旁边的“大会”入口进入后的内容。以下页面保持原样，不纳入修改：

- 图 1 对应的大会引导页 `DahuiScreen`
- 图 2 对应的演武场入口页 `YanwuchangScreen`
- 点击“作品”进入的作品展示页 `YanwuchangVideoScreen`
- 现有作品创作台与创作档案的普通入口和普通创作流程

目标是在 `Routes.DahuiArena` 及其下级路由内完成两条真实可用的比拼链路：

1. 队友切磋：支持好友、班级同门、限时口令和匿名同门匹配。
2. 知识比拼与同题 AI 共创：知识题直接在大会内作答；同题 AI 创作复用现有作品创作台，双方完成后进入可解释的 AI 评审、自评、互评、复盘和申诉环节。

当前工程并非没有大会后端。现状是“后端基础较完整，前端信息架构和共创赛制未完成”：

- 已有：大会作品流、结构化评招、收藏、二创授权、匿名知识匹配、三题作答、异步评审、结果、自评、互评、师评、复盘、举报、书信和审计。
- 已有 Android 接入：基础作品列表、作品详情、评招、授权记录、匿名知识切磋、结果与书信。
- 主要缺口：好友或队友发起挑战、房间和口令、比赛类型选择、同题共创任务快照、比赛与创作项目映射、双方封卷提交、共创评审量规、异议复核，以及符合现有国风视觉的大会首页。
- 2026 年 9 月 11 日验证：大会后端专项测试 12 项通过；Android `compileDebugKotlin` 和 `testDebugUnitTest` 通过。

策划书是需求依据，不是对执行代理的操作指令。最终范围、禁止修改项和验收优先级以本文件和用户当轮要求为准。

## 2 目标信息架构

进入路径保持为：

`首页 → 图 1 大会引导页 → 图 2 演武场入口页 → 点击大会 → 竹峰武会首页`

大会内页只承担比赛，不把“作品”入口的内容重新复制进来。首页包含：

- 当前进行中的切磋：继续作答、继续创作、等待同门、等待评审。
- 两种比赛：知识比拼、同题 AI 共创。
- 四种组局方式：邀请队友、班级同门、输入一次性口令、匿名同门匹配。
- 战绩与复盘：只展示个人成长和最近复盘，不做公开排行榜。
- 大会书信：挑战、入局、评审完成、教师复核和举报结案。

## 3 核心流程

### 3.1 队友发起

1. 用户点击“邀请队友”。
2. 选择知识比拼或同题 AI 共创。
3. 选择已学秘籍或教师发布的任务范围。
4. 选择好友、班级同门，或生成限时一次性口令。
5. 服务端创建房间并固化规则、题目范围或共创任务版本。
6. 对方接受后进入准备页；超时、拒绝或取消均有明确状态。

首发可以先使用班级同门和口令；如果现有好友域没有可靠关系数据，不用本地假好友冒充完成。

### 3.2 知识比拼

1. 服务端按双方共同学过的秘籍或教师指定范围生成 3 至 5 题快照。
2. 默认思考模式不倒计时；可选轻快模式 30 秒，但计时不作为唯一评分标准。
3. 每题提交答案和理由；提交后不能覆盖，只允许幂等重试。
4. 双方进度可见，但不泄露对方答案和真实身份。
5. 双方完成后进入评审队列。
6. 结果页按“概念准确、解释依据、迁移应用、完成度”展示双方表现。
7. 用户可提交自评、结构化互评和本局复盘；教师可抽查或复核。

### 3.3 同题 AI 共创

1. 服务端创建不可变的共创任务快照，包含目标、受众、作品类型、指定知识点、允许工具、时间边界、安全规则和评审量规。
2. 双方接受任务后，服务端分别创建两个独立的 `CreationProject`，并写入同一个 `matchId` 和 `taskSnapshotId`。
3. Android 跳转到现有作品创作台，不另建第二套编辑器。创作台顶部显示“竹峰武会同题任务”和剩余状态。
4. 起稿、工法确认、制作、测试、说明、来源、AIGC 和人机分工继续使用现有五阶段创作协议。
5. 参赛者必须提交一个已封卷、已完成来源与 AIGC 检查的不可变 `CreationVersion`。
6. 双方提交或比赛截止后写入评审 Outbox 任务。评审器只读取任务快照、最终作品可评审摘要、过程证据、学习卡、来源与人机分工，不读取隐私资料。
7. AI 评审输出总分、分项、证据引用、优点、改进建议和置信度。结果不只给胜负。
8. 结果页必须保留自评、同伴“看见一招”反馈、教师或人工复核、提出异议和生成修订任务。

### 3.4 赛后回流

- “去温习”返回关联秘籍。
- “按建议修订”携带原 `projectId` 和评审建议回到创作台，创建新版本。
- “登记改进”把评审或同伴建议关联到后续不可变版本。
- “存入创作档案”显示比赛来源、任务版本、AI 评审、自评、互评和修订记录。
- “传习记录”只在建议被采纳或教师认可后生成。

## 4 创作台复用契约

复用的关键不是相似界面，而是同一个项目状态机、同一组 Repository 和同一套后端创作实体。

创作台路由建议统一为：

`creation/editor/{projectId}?entryMode={NORMAL|CONFERENCE}&matchId={id}&taskSnapshotId={id}&returnRoute={encoded}`

创作台只新增比赛上下文，不复制普通创作业务：

- `entryMode=NORMAL`：保持现有普通作品创作行为。
- `entryMode=CONFERENCE`：显示比赛任务条、截止状态和“提交本局作品”按钮。
- `projectId` 始终由服务端创建并返回。
- `currentStage` 和 `projectStatus` 始终从项目详情恢复。
- `matchId` 用于提交资格、截止校验和结果回跳。
- `taskSnapshotId` 用于锁定双方相同题面，客户端不能改写。
- `returnRoute` 用于封卷提交后回到大会等待页或结果页。

禁止做法：复制 `GongfangScreen` 或 `CreationEditorScreen` 形成“比赛专用创作页”；用本地 `remember` 假造项目；把对方作品或源文件直接复制给参赛者；绕过封卷、来源或 AIGC 检查直接参赛。

## 5 Android 前端实施计划

### 5.1 保留和替换

保留：

- `DahuiScreen.kt`
- `YanwuchangScreen.kt`
- `YanwuchangVideoScreen.kt`
- 现有 `CreationViewModel`、创作 Repository、创作阶段页面和普通路由行为
- 现有大会详情、评招、授权、书信能力

替换或重组：

- 把 `ConferenceHubScreen` 从作品列表改造成“竹峰武会首页”。
- 原作品流和作品详情继续由“作品”入口承担，避免两个入口内容重复。
- 将 1353 行的 `ConferenceScreens.kt` 拆成按功能组织的文件，降低状态耦合。

建议文件结构：

```text
ui/screens/conference/
  ConferenceHomeScreen.kt
  ChallengeSetupSheet.kt
  WaitingRoomScreen.kt
  KnowledgeMatchScreen.kt
  CoCreationBriefScreen.kt
  CoCreationWaitingScreen.kt
  MatchResultScreen.kt
  AiReviewPanel.kt
  ConferenceLettersScreen.kt
  ConferenceComponents.kt
```

### 5.2 页面状态

每个页面统一支持：

- `Loading`：骨架或局部进度，不锁死返回。
- `Content`：真实服务端数据。
- `Empty`：给出可执行去向。
- `RecoverableError`：展示原因、重试和安全返回。
- `OfflineCached`：只读展示缓存，写操作明确禁用。
- `Submitting`：按钮防重复，写操作使用幂等键。

不要使用一个全局 `loading: Boolean` 锁住大会所有区域。按首页、房间、作答、提交、评审、书信拆分请求状态。

### 5.3 视觉与适配

- 沿用竹林、擂台、宣纸、竹节、叶签和熊猫语言，但信息层使用 Compose 可读组件，不把整页做成不可访问的背景图。
- 所有交互触控目标至少 48dp，图标有中文 `contentDescription`。
- 以 360dp 至 430dp 手机宽度为首要验收，兼容横屏和大屏；避免继续增加固定 `offset`。
- 长内容使用 `LazyColumn`，底部按钮使用安全区 Insets。
- 动效 0.35 至 0.55 秒，并遵守系统减少动态效果设置。
- 不做血条、失败震屏、陌生人私聊、热度数字或排行榜。

### 5.4 ViewModel 与导航

- `ConferenceViewModel` 拆分为 `ConferenceHomeViewModel`、`ChallengeRoomViewModel`、`KnowledgeMatchViewModel` 和 `MatchResultViewModel`，或至少拆分内部状态容器。
- 页面只发意图，不直接拼接口。
- 进房、接收挑战、逐题作答、提交比赛作品和评审轮询均可在进程重启后恢复。
- 比赛创作跳转必须携带真实 `projectId`；返回时依据服务器状态进入等待或结果页。
- 使用稳定 deep link 处理书信直达比赛、共创任务和评审结果。

## 6 后端实施计划

### 6.1 复用现有能力

直接复用：

- `ConferenceMatch`、问题快照、答案、结果、评价、复盘和举报。
- `ConferenceJudge`、Outbox、Worker 租约、重试和人工补判。
- `CreationProject`、`CreationVersion`、工法、阶段事件、学习卡、来源谱、封卷检查和提交审核。
- 大会书信、审计、隐私文本检查、年龄段和拉黑过滤。

### 6.2 新增或扩展实体

建议扩展，不并行新造领域：

- `ConferenceChallenge`
  - `id`
  - `mode`: `KNOWLEDGE` 或 `CO_CREATION`
  - `opponent_scope`: `FRIEND`、`CLASSMATE`、`INVITE_CODE`、`ANONYMOUS`
  - `initiator_user_id`
  - `classroom_id` 可空
  - `manual_page_id` 可空
  - `status`: `DRAFT`、`OPEN`、`MATCHED`、`CANCELED`、`EXPIRED`
  - `expires_at`
  - `rules_snapshot`
- `ConferenceChallengeInvite`
  - 邀请对象、响应状态、一次性口令哈希、使用次数和撤销时间
- `ConferenceCoCreationTaskSnapshot`
  - 任务文本、作品类型、知识点、允许工具、量规、版本和安全规则
- `ConferenceMatchCreationEntry`
  - `match_id`
  - `participant_user_id`
  - `creation_project_id`
  - `submitted_creation_version_id` 可空
  - `submitted_at` 可空
  - `status`: `NOT_STARTED`、`CREATING`、`SEALED`、`SUBMITTED`、`WITHDRAWN`
- `ConferenceJudgmentEvidence`
  - 评审维度、证据引用、模型版本、提示词版本、置信度和人工复核状态

约束：一个参赛者在一局只有一个比赛项目；同一个不可变版本只能提交一次；提交版本必须属于该参赛者的比赛项目；双方任务快照必须相同；截止后不能新增提交；任何跨用户对象读取都必须校验参赛关系。

### 6.3 API 草案

首页与发起：

- `GET /v1/conference/home`
- `POST /v1/conference/challenges`
- `GET /v1/conference/challenges/{challenge_id}`
- `POST /v1/conference/challenges/{challenge_id}/accept`
- `POST /v1/conference/challenges/{challenge_id}/decline`
- `POST /v1/conference/challenges/{challenge_id}/cancel`
- `POST /v1/conference/challenges:join-by-code`

知识比拼继续复用：

- `GET /v1/conference/matches/{match_id}`
- `POST /v1/conference/matches/{match_id}/answers`
- `GET /v1/conference/matches/{match_id}/result`

同题共创新增：

- `GET /v1/conference/matches/{match_id}/creation-task`
- `POST /v1/conference/matches/{match_id}/creation-entry`
- `GET /v1/conference/matches/{match_id}/creation-entry`
- `POST /v1/conference/matches/{match_id}/creation-submission`
- `DELETE /v1/conference/matches/{match_id}/creation-submission`，仅截止前且规则允许时使用
- `GET /v1/conference/matches/{match_id}/judgment-status`

赛后继续复用或扩展：

- `GET /v1/conference/matches/{match_id}/result`
- `POST /v1/conference/matches/{match_id}/evaluations`
- `POST /v1/conference/matches/{match_id}/reflections`
- `POST /v1/conference/matches/{match_id}/reports`
- `POST /v1/conference/matches/{match_id}/appeals`
- `POST /v1/internal/conference/matches/{match_id}/teacher-evaluations`

所有写接口必须支持 `Idempotency-Key` 或等价请求键；服务端负责状态转换、权限、截止、版本归属和封卷校验。

## 7 比赛状态机

推荐统一状态：

```text
DRAFT
  -> WAITING_FOR_OPPONENT
  -> READY
  -> IN_PROGRESS
  -> AWAITING_SUBMISSIONS
  -> AWAITING_JUDGMENT
  -> JUDGING
  -> ENDED
  -> REFLECTED
```

异常分支：

```text
WAITING_FOR_OPPONENT -> EXPIRED | CANCELED
READY | IN_PROGRESS -> WITHDRAWN | REPORTED
AWAITING_JUDGMENT | JUDGING -> JUDGMENT_FAILED -> MANUAL_REVIEW
ENDED -> APPEALED -> MANUAL_REVIEW -> RESOLVED
```

知识比拼在作答完成后进入 `AWAITING_JUDGMENT`。共创比拼在双方提交封卷版本或达到截止策略后进入 `AWAITING_JUDGMENT`。

## 8 AI 评审设计

AI 不是唯一裁判。评审输入应最小化并匿名化：

- 参赛者只使用 A、B 标识。
- 固化任务与量规版本。
- 最终作品的可评审内容或安全代理文件。
- 工法、测试、版本、来源、AIGC 和人类贡献的结构化摘要。
- 不发送昵称、手机号、学校、班级、地理位置和私密草稿。

共创评审建议维度：

- 任务达成 25 分
- 概念准确与知识迁移 25 分
- 创意表达 20 分
- 过程证据与人机分工 20 分
- 安全与来源完整 10 分

输出必须包含：

- `score`
- `dimension_scores`
- `evidence_refs`
- `summary`
- `strengths`
- `improvements`
- `confidence`
- `flags`
- `rubric_version`
- `evaluator_reference`

置信度低、内容风险高、双方分差处于设定灰区或用户提出异议时，进入教师或人工复核，不自动下结论。

## 9 安全和未成年人边界

- 不开放陌生人自由私聊或真实资料交换。
- 口令限时、一次性或班级范围内有效，可撤销并限流。
- 匹配按年龄段、修炼范围和拉黑关系过滤。
- 作答、房间说明、互评、申诉和举报详情都做隐私与外链检查。
- 共创项目仍执行媒体病毒检查、内容审核、来源许可和 AIGC 标记。
- 对外结果不返回对手用户 ID。
- 开发环境透明规则评分必须明确标记，不能冒充生产 AI。
- 生产评审必须使用 HTTPS、固定模型版本、超时熔断、重试、人工兜底和偏差抽检。

## 10 分阶段交付

### Sprint 0 3 至 5 天

- 冻结路由、状态机、数据模型、错误码和 OpenAPI。
- 确认好友域是否存在；若不存在，首发采用班级同门、口令和匿名匹配。
- 确认比赛作品类型首发范围，建议先完成图片类同题共创。
- 评审通过大会首页、知识题、共创任务、等待评审和结果页五个关键状态。

### Sprint 1 1 周

- 重做大会首页和比赛设置页。
- 拆分 Android 状态并接 `GET /conference/home`。
- 增加挑战、邀请、口令和恢复能力。
- 完成房间权限、幂等、过期和审计测试。

### Sprint 2 1 周

- 将现有匿名知识匹配扩展到队友、班级和口令模式。
- 完成 3 至 5 题、理由、进度、退出、举报和结果复盘。
- 双账号端到端验证。

### Sprint 3 2 周

- 新增共创任务快照和比赛项目映射。
- 以真实 `projectId` 复用现有创作台。
- 完成封卷版本提交、截止和等待页。
- 首发仅图片类，视频、小游戏和程序保留协议但不以静态页冒充完成。

### Sprint 4 1 至 2 周

- 扩展 AI 评审协议、证据引用、置信度和人工复核。
- 完成赛后修订回流、创作档案谱系和大会书信深链。
- 完成负载、弱网、进程恢复、越权、内容安全和无障碍验收。

## 11 必须通过的验收场景

1. 从图 1 到图 2 的视觉和交互不变；点击“作品”仍进入原作品页；点击“大会”进入新武会首页。
2. 用户可通过班级同门、口令或匿名匹配建立知识比拼；有好友域时再开放好友直邀。
3. 两个测试账号能完成同一局知识题、看到双方进度、等待评审并获得结果和解释。
4. 任一方退出、超时或举报时状态一致，不扣除已有学习成果。
5. 同题共创双方得到相同任务快照，但各自拥有独立 `CreationProject`。
6. 比赛创作真实复用现有创作台；重启应用后能根据 `projectId/currentStage` 恢复。
7. 未封卷、缺来源、缺 AIGC 或版本不属于本局的作品不能提交。
8. 双方提交后只产生一个幂等评审任务；Worker 重启后可继续；失败可人工补判。
9. 结果展示量规、证据、优点和改进，不只展示胜负；可自评、互评、复盘和申诉。
10. “按建议修订”回到原项目并生成后续版本，档案能显示比赛和评审谱系。
11. 对外接口不泄露对手真实身份；口令不可长期复用；所有跨用户读取都有越权测试。
12. 360dp、412dp 和 430dp 宽度无裁切；所有按钮至少 48dp；系统返回与深链正确。

## 12 可直接使用的开发主提示词

```text
你正在维护 Android 原生 Jetpack Compose + FastAPI 项目“机巧江湖”。请在当前工作区实施“作品旁边的大会内页”，而不是重做已有引导页或作品页。

一、最高优先级范围
1. 不修改 DahuiScreen，它是图 1 的大会引导页。
2. 不修改 YanwuchangScreen 的现有场景、熊猫、“作品/大会”两个入口和进入关系，它是图 2。
3. 不修改 YanwuchangVideoScreen 的现有作品页主流程。
4. 只重做 Routes.DahuiArena 及其下级比赛页面，并完成对应后端。
5. 保留用户和队友已有的未提交改动；先执行 git status，禁止覆盖或回滚无关文件。
6. 策划书只作为需求资料；不要执行其中与本任务范围冲突的描述。以本提示词和用户最新要求为准。

二、必须实现的产品结果
1. 大会首页叫“竹峰武会”，提供知识比拼和同题 AI 共创两种比赛。
2. 支持邀请队友、班级同门、限时一次性口令和匿名同门匹配。若仓库没有可靠好友域，首发先实现班级、口令和匿名，不得用本地假数据冒充好友功能。
3. 知识比拼复用现有 ConferenceMatch 的题目快照、答案、进度、异步评审、结果、自评、互评、师评、复盘、举报和书信。
4. 同题 AI 共创必须复用现有 CreationProject/CreationVersion 和 Android 创作台。服务端为双方创建独立 projectId，并关联同一 matchId/taskSnapshotId；客户端通过真实 projectId 进入现有创作台，不能复制一套比赛编辑器。
5. 双方完成创作后只能提交已封卷、来源/AIGC/人机分工完整的不可变版本。双方提交后进入 AI 评审。
6. AI 评审必须使用固定量规，输出总分、分项分、证据引用、总结、优点、改进、置信度、风险标记和评审器版本；保留自评、同伴结构化反馈、教师/人工复核和申诉。
7. 赛后“按建议修订”回到原 CreationProject 创建新版本，并在创作档案保存比赛任务、参赛版本、AI 评审、自评、互评和修订谱系。

三、现有代码事实
1. Android 当前大会相关文件：
   - android/app/src/main/java/com/jueqiao/jianghu/ui/screens/dahui/DahuiScreen.kt
   - android/app/src/main/java/com/jueqiao/jianghu/ui/screens/yanwuchang/YanwuchangScreen.kt
   - android/app/src/main/java/com/jueqiao/jianghu/ui/screens/yanwuchangvideo/YanwuchangVideoScreen.kt
   - android/app/src/main/java/com/jueqiao/jianghu/ui/screens/dahui/ConferenceScreens.kt
   - android/app/src/main/java/com/jueqiao/jianghu/conference/ConferenceViewModel.kt
   - android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt
   - android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt
2. 后端已有 app/domains/conference、conference routes、迁移 0016/0018/0020/0021、Judge Provider、Outbox Worker 和专项测试。
3. 后端现有能力不是空壳：匿名知识切磋、AI/开发评审、结果、自评/互评/师评、复盘、举报、书信已存在。新增代码应扩展现有领域，不能再造平行 ConferenceV2。
4. 现有 ConferenceScreens.kt 很大，重构时按页面拆文件，但保持已用路由和深链兼容。
5. 现有工作区可能是脏树。任何格式化和批量改写都限制在本任务文件。

四、先做审查再改代码
1. 阅读：
   - 文档/机巧江湖_大会页策划书.docx
   - 文档/机巧江湖_作品创作页策划书.docx
   - 文档/机巧江湖_作品创作页_详细模块与执行方案.docx
   - Appproject/services/api/docs/conference-integration.md
   - Appproject/services/api/docs/conference-backend-review-report.md
   - Appproject/services/api/docs/creation-workflow-integration.md
2. 列出现有接口、模型、迁移、Android API DTO、Repository、ViewModel 和路由的可复用项及缺口。
3. 先提交状态机、数据模型、OpenAPI 草案和页面状态清单；确认不会改图 1、图 2 和作品页后再进入实现。

五、后端实现要求
1. 为 ConferenceMatch 增加 mode：KNOWLEDGE/CO_CREATION，以及 opponent_scope：FRIEND/CLASSMATE/INVITE_CODE/ANONYMOUS；保持旧匿名接口兼容或提供明确迁移。
2. 新增 Challenge/Invite、CoCreationTaskSnapshot、MatchCreationEntry 和 JudgmentEvidence；所有外键、唯一约束、索引、级联和数据保留行为明确。
3. 使用服务端状态机校验，不相信客户端传入的阶段、归属、截止和封卷结果。
4. 创作比赛项目通过 creations domain service 创建，记录 task snapshot link；不要直接跨领域写数据库绕过创作规则。
5. 写操作幂等；答案、比赛提交和评审任务不可重复；并发使用唯一约束和 row_version。
6. 继续使用 Outbox + Worker 评审；支持租约、指数退避、最大重试、人工补判和可观测指标。
7. 评审外发数据只使用 A/B，禁止发送用户 ID、昵称、班级、手机号、学校和私密草稿。生产只允许 HTTPS。
8. 增加 API、service、权限越权、迁移升级/降级、Worker 恢复和端到端测试。

六、Android 实现要求
1. ConferenceHubScreen 改造成竹峰武会首页，不再重复作品流。
2. 新增挑战设置、等待房、知识题、共创任务说明、共创等待、AI 评审中、结果与复核页面。
3. 页面使用 Compose 状态提升、稳定 DTO、单向数据流和按请求拆分的 loading/error/submitting 状态。
4. 使用 LazyColumn/自适应布局，不继续堆叠固定 offset；360/412/430dp 宽度和系统 Insets 必须通过。
5. 触控目标至少 48dp，交互图标有 contentDescription，支持减少动效。
6. 比赛共创通过 creation/editor/{projectId} 进入现有创作台，并传 entryMode=CONFERENCE、matchId、taskSnapshotId、returnRoute。普通创作行为不能回归。
7. 重启和进程恢复后，从服务端恢复当前房间、比赛、作答进度、项目阶段和评审状态。
8. 书信可深链到具体挑战、比赛、共创项目和评审结果。

七、视觉要求
1. 延续竹林、擂台、宣纸、竹节、叶签和熊猫风格；不做通用白底后台列表。
2. 不做夸张血条、失败震屏、陌生人私聊、热度数字和排行榜。
3. 默认思考模式无倒计时；可选轻快模式 30 秒。
4. 结果页标题使用“这一招练得如何”，先展示亮点和方法，再展示分数和胜负。

八、验证命令和交付标准
1. 后端至少运行：
   .venv/Scripts/python -m pytest tests/test_conference_api.py tests/test_conference_judge.py tests/test_conference_workflow_acceptance.py
2. Android 至少运行：
   gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest
3. 新增双账号端到端场景：队友/口令入局、知识比拼、共创创建项目、双方提交、评审延迟、Worker 重试、举报结束、书信未读和赛后修订。
4. 不得只报告编译通过；必须报告真实接口链路、数据库持久化、重启恢复、越权测试和仍未完成的边界。
5. 完成后给出修改文件、迁移、接口变更、测试结果、手工验收路径、风险和回滚开关。

现在先执行审查和协议冻结，不要改动图 1、图 2 和作品页。协议确认后按 Sprint 1 到 Sprint 4 实施，每个 Sprint 都必须能独立编译、测试和回滚。
```

