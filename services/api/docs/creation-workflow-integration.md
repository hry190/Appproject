# 创作台工法与阶段流转接入说明

本阶段把创作台首屏从静态演示接到了真实作品数据，并完成“意图分析 → 来源带入 → 工法确认 → 项目创建 → 不可变版本 → 用户确认生成/受控建议 → 测试整改 → 封卷说明 → 明确去向 → 提交检查”的闭环。工作流迁移为 `0009_creation_workflow`，来源字段迁移为 `0010_creation_sources`，工具确认与测试记录迁移为 `0011_creation_tools_tests`，封卷自查迁移为 `0012_creation_seal_check`，异步图片生成任务迁移为 `0013_image_generation_jobs`，服务端合成导出任务迁移为 `0014_creation_export_jobs`。

## 1. 当前可执行链路

1. Android 调用 `POST /v1/creation-intents:analyze`，服务端保存原始想法、已通过检查的草图 `attachment_asset_ids`、已学秘籍 `manual_page_ids` 和分析结果，并返回可编辑工法草案。
2. 用户可修改目标、形式和下一步；确认时以 `Idempotency-Key` 调用 `POST /v1/creation-projects`。
3. 项目创建请求携带 `intent_id`。一个意图只能转换成一个项目，项目可追溯到原始想法。
4. Android 调用 `PUT /v1/creation-projects/{id}/method` 保存工法第 1 版，再调用阶段接口从 `IDEATION` 进入 `DRAFT`。
5. 生图页的“保存草图/脚本”调用版本接口。每次有效保存创建不可覆盖版本；首版完成后从 `DRAFT` 进入 `PRODUCTION`。
6. 创作档案读取真实工法、当前阶段、阶段事件、版本、学习卡、来源谱和审核状态。
7. 制作阶段可提出一次创作教练调用。服务端只创建 `PROPOSED` 记录，Android 必须先展示输入快照、作用说明和数据去向；用户确认后才得到文字建议，拒绝则不执行。
8. 测试阶段把场景、结果、观察记录和发现的问题固定到当前不可变版本。问题需要逐条填写整改说明；进入 `SEAL` 前，当前版本最近一次测试必须为 `PASSED`，且该版本所有问题均已关闭。
9. 说明阶段依次保存作品说明与三项隐私自查、学习卡、来源与人机分工。已学秘籍是可选参考，不是自由创作的发布门槛。
10. 用户选择家长、教师/班级或知行流后，页面再次展示去向和锁定影响；只有点击“确认提交”才创建审核任务。仅自己保存不调用提交接口。
11. 制作阶段可先核对提示词、画幅、质量、模型和数据去向，再明确点击“确认生成”。Worker 生成图片后进入与用户上传相同的媒体安全管线；通过后才添加 `AI_GENERATED` 图层、创建新版本并生成待补充的来源谱。
12. 制作阶段可进入项目级画布编辑器。图层的位移、缩放、旋转、透明度、中心裁剪、文字字号与颜色都写入版本快照；保存只创建新版本，不覆盖旧版。
13. 任意未删除作品版本可明确创建 PNG/JPEG、1×/2× 导出任务。Worker 按版本快照合成可见图层，再经统一媒体安全管线生成限时预览与下载地址；导出不会修改作品版本。

## 2. 草图与秘籍来源

- 上传草图：`POST /v1/uploads/intents` → 对象存储 `PUT` → `POST /v1/uploads/{id}/complete` → `GET /v1/media-assets/{id}`。只有本人、用途为 `CREATION_LAYER` 且状态为 `READY` 的 JPEG/PNG/WebP 可以进入意图分析。
- 开发环境：内存素材库无法被 Android 直接访问，因此非生产且 `media_storage_provider=memory` 时可使用受登录保护的 `PUT /v1/uploads/{id}/object`；该通道校验所属用户、有效期、大小和 MIME，并在开发模式同步执行图片安全处理。生产环境不开放该通道，仍走 MinIO/S3 预签名直传与异步 Worker。
- 带入秘籍：Android 只列出 `LEARNED`、`MASTERED`、`TEACHING` 三种状态；服务端在分析与工法确认时再次核验所属用户的学习进度，未学秘籍返回 `MANUAL_NOT_LEARNED`。
- 来源会同时保存在创作意图、每一版工法和账户导出中；已被有效意图或活动作品工法引用的草图不能被删除。

## 3. 五阶段状态

- `IDEATION`：构思与工法确认。
- `DRAFT`：草图或脚本。
- `PRODUCTION`：正式制作与修订。
- `TEST`：记录真实预览/试玩结果、问题和整改，再对当前版本复测。
- `SEAL`：作品说明、来源、人机分工与发布选择。

向前只能进入相邻阶段，不能跳级；回退必须填写原因。进入 `DRAFT` 前必须已有工法，进入 `TEST` 前必须至少保存一个作品版本。进入 `SEAL` 还必须满足“最近测试通过 + 当前版本零个开放问题”。在 `SEAL` 创建新版本会自动退回 `PRODUCTION`，旧测试不会沿用，必须对新版本重新测试。所有阶段变动会写入阶段事件和作品修改日志，并使用项目 `row_version` 防止旧页面覆盖新状态。

## 4. 幂等和断点恢复

项目创建、版本保存、图片生成、图片生成重试、作品导出、工具建议、工具确认和提交审核都接受 8–64 字符的 `Idempotency-Key`。同一键和同一请求会返回原结果；同一键改用不同内容返回冲突。Android 在失败重试时保留原键，因此网络超时不会重复创建项目、版本、生成任务、导出任务、工具调用或审核任务。

## 5. 封卷资料与发布门禁

- `GET|PUT /v1/creation-versions/{id}/seal-check`：保存作品说明、学习复盘、下次改进，以及真实身份/学校、联系方式、肖像使用权限三项确认。仅 `SEAL` 阶段可写。
- `GET|PUT /v1/creation-versions/{id}/learning-card`：方法总结和未解决问题确认必填；`manual_page_ids` 可为空。
- `GET|PUT /v1/creation-versions/{id}/provenance-manifest`：至少记录本人贡献；使用 AI 时必须补充提供方、模型、动作、提示摘要、是否人工修改和 AIGC 声明；外部素材必须记录来源与授权，未知授权会阻断提交。
- `POST /v1/creation-projects/{id}/submissions`：再次核验当前版本、`SEAL` 阶段、最近测试通过、零开放问题、三份资料完整、媒体已通过安全检查和监护可见范围。成功后资料锁定并进入 `PENDING_CHECK`，不会直接公开。

发布去向只有 `GUARDIAN_ONLY`、`CLASSROOM`、`COMMUNITY`。`PRIVATE` 表示不提交、继续保存在个人创作档案中。社区去向还受监护设置约束。

## 6. Agent 边界

当前不需要自主 Agent。意图分析使用确定性的 `rules-v1`；创作教练使用确定性的 `rules-coach-v1`。图片生成是一次性、参数固定、需用户明确确认的异步工具任务，不会自行规划、连续调用工具、修改其他图层或发布作品。版本导出是完全确定性的图层渲染任务，也不使用模型或 Agent。

图片生成接口：

1. `GET /v1/creation-projects/{id}/image-generations` 返回功能开关、提供方/模型、是否外传、每日额度和最近任务。
2. `POST /v1/creation-projects/{id}/image-generations` 要求当前版本、项目修订号和 `user_confirmed_generation=true`，返回 `202 QUEUED`。
3. `GET /v1/image-generation-jobs/{id}` 用于轮询 `QUEUED → RUNNING → SAFETY_CHECK → VERSIONING → COMPLETED`。
4. `POST /v1/image-generation-jobs/{id}/retry` 只允许重试可恢复失败，最多重试配置次数；提供方/内部失败返还额度，安全策略拒绝不返还。

提示词在调用外部服务前拦截电话、邮箱、网址和证件号；外部请求只附带不可逆的短哈希用户标识。生成期间若父版本已不是当前版本，任务停止且不调用/不写入。结果必须经过哈希、病毒、真实格式、解码、像素、元数据和内容安全检查；通过后媒体用途为 `AIGC_OUTPUT`、`aigc_detected=true`。默认每日 6 次、同一作品同时只允许一个活跃任务。

开发环境默认使用 `development/local-composition-v1`，它只用于本地端到端验收，不外传提示词。生产环境禁止该提供方，可设为 `disabled`，或配置服务端 `openai` 提供方；API 密钥绝不放入 Android。

工具调用采用两步协议：

1. `POST /v1/creation-projects/{id}/tool-calls` 只保存提案和快照，状态为 `PROPOSED`，30 分钟后过期。
2. `POST /v1/creation-tool-calls/{id}/decision` 由用户明确确认或拒绝，并用工具记录自己的 `row_version` 防止重复决定。

只有以后确实需要多轮规划或多工具协作时才评估 Agent；即使替换为模型，也必须继续保留调用前确认、最小输入、外传标记、执行器版本、结果快照、幂等和审计日志。

## 7. 画布编辑与版本对比

- Android 从 `GET /v1/creation-projects/{id}/versions` 读取当前版本，并只为当前图层引用的素材换取短期签名地址。画布支持图层选择、显隐、层级调整、删除、新增文字、拖动、双指缩放/旋转、透明度与中心裁剪。
- `LayerSnapshot` 中的 `offset_x`、`offset_y`、`scale`、`rotation_degrees`、`opacity`、`crop_inset`、`font_size` 和 `text_color` 都有服务端范围校验。旧版 JSON 缺少这些字段时按无变换默认值读取，不需要数据库迁移。
- 保存继续使用 `POST /v1/creation-projects/{id}/versions`，父版本必须仍是当前版本；Android 保留同一次重试的幂等键。编辑器要求填写修改说明，服务端修改日志会记录变化图层。
- `GET /v1/creation-versions/{id}/diff` 默认与父版本比较，也可用 `base_version_id` 指定同一项目内的其他版本；结果分别列出新增、删除、修改图层及具体字段。
- 新版本会继承上一版来源谱并恢复为 `DRAFT`。如果 AI 图层被移动、缩放、旋转、裁剪、隐藏、删除或替换，对应 AI 贡献条目会自动设置 `user_modified=true`；这项事实由服务端比较快照得出，不依赖客户端自报。
- 画布编辑是本地直接操作加一次明确保存，不需要智能 Agent，也不会自行生成、发布或改写其他图层。

## 8. 当前未接入

- “上传草图”和“带入秘籍”已接入；“同门灵感”继续禁用，必须等作品授权快照、撤权处理和引用规则完成。
- 短视频、小游戏和互动程序暂时降级为图文工法，并返回 `MVP_MEDIA_FALLBACK`。
- 教师/班级创建者与已验证家长的作品收件箱已接入；知行流由大会作品流展示。三种去向都只在审核发布后可见，撤回或撤销审核结论会同步撤销投递。班级提交必须携带本人已加入且仍有效的 `target_classroom_id`。
- 审核仍是现有安全扫描与人工复核状态机；尚未新增视觉模型审核 Agent。

## 9. 服务端预览与导出

- `POST /v1/creation-versions/{id}/exports`：必须携带幂等键和 `user_confirmed_export=true`；参数为 `format=PNG|JPEG`、`output_scale=1|2`，返回 `202 QUEUED`。
- `GET /v1/creation-versions/{id}/exports`：读取该不可变版本最近 20 个导出任务；`GET /v1/creation-export-jobs/{id}` 用于轮询 `QUEUED → RENDERING → SAFETY_CHECK → COMPLETED`。
- Worker 只读取本人且状态为 `READY` 的图层素材，按 Fit、中心裁剪、位移、缩放、旋转、透明度、层级、文字字号与颜色合成。PNG 保留透明背景，JPEG 使用米白背景；最大 2500 万像素。
- 合成结果使用 `CREATION_PREVIEW` 媒体用途进入隔离区，并再次执行哈希、真实格式、病毒、解码、像素、元数据和内容安全检查。完成后返回短期签名 URL；Android 使用系统下载队列，不保存永久公开地址。
- 若版本包含 AI 图层且用户未关闭 `aigc_export_mark_enabled`，导出图右下角自动加入“AI辅助”标记。导出资产受作品引用保护，删除作品时随项目进入回收流程，并包含在账户数据导出中。
- Android 有未保存画布修改时禁用导出，避免导出的版本与屏幕草稿不一致。用户必须先保存新版本，再选择格式与清晰度。

## 10. 验证

后端测试覆盖意图持久化、草图所有权/状态、秘籍学习状态、开发上传通道、监护开关、项目与版本幂等、跨用户隔离、工法乐观锁、生成确认/隐私拦截/额度/重试/安全媒体/版本与来源谱写入、画布参数持久化、版本差异、AI 图层人工修改标记、服务端合成导出、工具提案/确认重放、测试问题整改、复测门禁、封卷资料完整性、发布锁定和账户数据导出。迁移需要通过升级到 `head`；阶段性回滚验收时回滚到目标前一迁移再升级。Android 至少执行 `:app:compileDebugKotlin` 和 `:app:assembleDebug`。
