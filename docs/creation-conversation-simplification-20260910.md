# 作品创作简约化整改与验收报告

日期：2026-09-11  
状态：本地实现并验证，尚未提交、推送或合并 Git

## 1. 整改结论

作品创作主流程已从多阶段表单改为持续对话：学生只需在创作台说出想法，随后直接进入 AI 教练对话；学生可以采纳建议或继续提出修改，满意后保存上传草稿并生成作品；生成结果页仍保留同一个输入框，可直接发出新要求并再次生成，也可保存到个人档案。保存作品不会自动公开或投递班级。

保留了竹林、卷轴、熊猫、荷花及顶部入口的原有风格，没有新增横向复杂按钮组。页面只保留当前任务对应的一个高强调主动作。

## 2. 删除或退出主流程的旧交互

- 不再显示“创作工法”大卡片、目标、形式、下一步等确认内容。
- 不再进入“调整工法”和“进入草图/脚本”。
- 移除对学生可见的五阶段导航与草图、制作、测试、封卷连续表单。
- 不再要求学生重复填写系统已经从初始想法、附件、参考资料和对话中得到的信息。
- 后端原工法、版本、测试、来源和封卷结构仍保留，用于内部业务记录和旧数据兼容。

## 3. 新创作流程

```text
提出想法/选择参考内容
  → 直接进入教练对话
  → 教练自动分析
  → 采纳建议或直接提出新要求
  → 多轮沟通
  → 保存上传草稿
  → 后端整理摘要和私有生成要求
  → 真实生成任务与自动检查
  → 查看结果
  ├─ 满意：保存作品到个人档案
  └─ 不满意：在结果页直接输入新要求 → 继续对话 → 再次生成
```

## 4. Android 修改

- `CreationViewModel.kt`：增加开始/恢复会话、发送消息、采纳建议、保存草稿并生成、返回沟通、保存作品等状态与幂等操作。
- `LuggageApi.kt`、`LuggageModels.kt`、`LuggageRepository.kt`：增加会话 DTO、API 和档案聚合读取。
- `GongfangScreen.kt`：点击“开始”直接创建会话并进入对话页；保留初始想法、附件、秘籍和二创来源上下文；上传中使用自然中文等待提示。
- `ShengtuScreen.kt`：改为单一卷轴对话页；统一学生、教练、状态和错误气泡；只在最新待确认建议附近显示“采纳并继续”；生成结果页直接保留沟通输入框、发送按钮和唯一主动作“保存作品”，失败时保留未发送成功的文字供重试，不再要求先点“返回沟通”。
- `JianghuNavHost.kt`：新建与继续创作都进入同一个对话/结果恢复路径，不再进入旧工法页。
- `CreationHomeModels.kt`：最近作品只显示“继续沟通、等待老师、已展示”等用户状态。
- `ChuangzuodanganScreen.kt`：保留三朵荷花的图像、坐标、动效；记录内容改为真实初始想法、主动保存版本和真实教练对话；教练时间线可滚动查看，并标记已采纳或已调整；删除、撤回、申诉仍在“更多”。
- 发送按钮和荷花热区已补充无障碍名称，主要触控目标按 Android 移动端规范保持可点击尺寸。

## 5. 后端修改与新增接口

新增接口：

- `POST /v1/creation-conversations:start`
- `GET /v1/creation-projects/{project_id}/conversation`
- `POST /v1/creation-projects/{project_id}/conversation:resume`
- `POST /v1/creation-projects/{project_id}/conversation/messages`
- `POST /v1/creation-projects/{project_id}/conversation/suggestions/{message_id}:accept`
- `POST /v1/creation-projects/{project_id}/conversation:generate`
- `POST /v1/creation-projects/{project_id}/conversation:return`
- `POST /v1/creation-projects/{project_id}/conversation:save-result`

领域服务会真实执行以下业务动作：

- 校验学生身份、附件归属和秘籍学习资格。
- 保存原始想法、附件、参考内容、完整对话及建议状态。
- 生产/演示环境通过服务器端 OpenAI 兼容接口调用真实对话模型，已支持 OpenAI 和 DeepSeek 两种独立配置；图片创作已支持豆包 Seedream 和 OpenAI Images 两种外部提供方。不在 Android 端保存密钥，也不使用固定话术或本地占位图兜底，仅测试环境注入可预测的测试实现。
- 每次模型调用携带最近的真实对话、建议采纳状态、初始想法、最多三张参考图片、已学习秘籍和二创来源；模型名、系统要求和完整内部上下文不返回前端。
- 新消息会把仍待确认的旧建议标为“已调整”，只有当前有效要求进入新摘要。
- “保存上传草稿”创建不可变草稿版本，并在服务器内部整理结构化工法和私有生成要求。
- 生成任务使用幂等键；长 Android 幂等键会先转换为固定长度的内部图层编号，避免数据库长度错误。
- 生成完成后写入真实版本、自动安全/完整性测试记录和后台阶段事件；失败会回到可重试状态。
- 返回沟通保留旧消息、历史生成和已保存版本；再次生成不会覆盖历史版本。
- “保存作品”仅写入个人档案，不自动投递班级或公开。
- 对前端返回的生成摘要为自然中文，不返回服务器内部完整生成要求。

主要后端文件：

- `services/api/app/api/routes/creations.py`
- `services/api/app/domains/creations/contracts.py`
- `services/api/app/domains/creations/models.py`
- `services/api/app/domains/creations/service.py`
- `services/api/app/domains/creations/conversation_coach.py`
- `services/api/app/domains/creations/image_generation_service.py`

比赛后端需要配置：

```text
JIANGHU_CONVERSATION_COACH_PROVIDER=deepseek
JIANGHU_CONVERSATION_COACH_MODEL=deepseek-v4-flash-vision-exp
JIANGHU_DEEPSEEK_API_KEY=<仅保存在服务器的密钥>
JIANGHU_DEEPSEEK_BASE_URL=https://api.deepseek.com
JIANGHU_IMAGE_GENERATION_PROVIDER=volcengine
JIANGHU_IMAGE_GENERATION_MODEL=<比赛使用的 Seedream 接入点或模型标识>
JIANGHU_VOLCENGINE_ARK_API_KEY=<仅保存在服务器的密钥>
JIANGHU_VOLCENGINE_ARK_BASE_URL=https://ark.cn-beijing.volces.com/api/v3
JIANGHU_VOLCENGINE_IMAGE_SIZE=2K
JIANGHU_VOLCENGINE_IMAGE_WATERMARK=true
```

生产配置会在未启用真实对话提供方或缺少密钥时拒绝启动，避免现场悄悄退回预生成话术。

## 6. 数据库迁移

新增迁移：`services/api/migrations/versions/0023_creation_conversations.py`

新增两张表：

- `creation_conversations`：会话状态、原始想法、附件/秘籍、方案摘要、草稿/作品版本、当前生成任务、结果版本和乐观锁版本号。
- `creation_conversation_messages`：学生/教练消息、消息类型、采纳/替换状态、回复关系、幂等键和请求指纹。

迁移只新增表和索引，不删除旧工法、版本、测试、封卷或用户数据。部署比赛后端前仍需在目标 PostgreSQL 环境执行 `alembic upgrade head` 并备份数据库。仓库旧迁移链在 SQLite 上包含不支持的约束变更，因此本地没有用 SQLite 假装验证完整生产迁移链。

## 7. 三朵荷花的数据来源

### 原创记录

- 作品名称：`creation_projects.title`
- 最初想法：`creation_conversations.initial_idea`，保持学生原文
- 开始时间：`creation_conversations.started_at`
- 上传内容：会话附件资源 ID 对应的真实文件名
- 参考秘籍：会话秘籍 ID 对应的真实秘籍标题
- 二次创作来源：项目衍生关系对应的原作品标题

### 修改版本记录

- 只读取 `draft_version_ids` 和 `saved_version_ids` 中用户主动保存的版本
- 版本号、时间和调整摘要来自 `creation_versions`
- 不默认列出未保存、失败或废弃的生成尝试

### 创作教练记录

- 来自 `creation_conversation_messages` 的真实时间线
- 显示学生消息、教练建议、已采纳/已调整状态和最终方案摘要
- 不读取或展示完整生成要求、系统提示、模型名、执行器名或工具参数

## 8. 验证命令与结果

后端：

- `.venv\\Scripts\\python.exe -m pytest -q`：包含真实模型适配器、创作 API 与安全配置在内的全量测试通过。
- 模型适配器单元测试已验证请求地址、鉴权头、模型选择、历史消息、秘籍、二创来源和图片输入，并验证网络失败时返回可重试的自然中文错误。
- `.venv\\Scripts\\python.exe -m scripts.accept_creation_workflow`：真实 HTTP 业务链通过，结果见 `artifacts/conversation-acceptance.json`。

Android：

- `gradlew.bat :app:testDebugUnitTest :app:lintDebug`：构建成功，单元测试与 Lint 通过。
- `gradlew.bat :app:assembleAcceptance`：比赛验收 APK 构建成功。
- 模拟器 `emulator-5554` 全流程：58 项 Android 操作检查、13 项 API 检查，失败数均为 0；包含结果页直接发送第二次修改、再次生成、保存、三朵荷花、档案继续和应用重启恢复。
- 正常链路验收结果：`artifacts/conversation-ui-acceptance-r13/result.json`
- 断服专项验证：发送后立即显示学生待发送气泡；网络失败后显示自然中文错误并把原文字恢复到输入框，证据见 `artifacts/conversation-ui-acceptance-r13/14-offline-send-restored.png`。
- DeepSeek 真实端到端验证：Android 输入两轮要求，服务端实际调用 DeepSeek，数据库最终保存 2 条学生消息和 2 条中文教练回复；证据见 `artifacts/deepseek-live-r1/result.json` 和 `artifacts/deepseek-live-r1/04-deepseek-second-reply.png`。
- 中文输入专项验证：确认 Compose 输入框不限制 Unicode，并补上文本键盘类型与“发送”IME 动作；测试模拟器已启用简体中文拼音和软键盘。通过拼音实际输入“清晨荷花”，再点击键盘发送键，学生中文气泡与 DeepSeek 第二轮中文回复均正常显示；证据见 `artifacts/chinese-ime-r1/08-chinese-entered.png` 和 `artifacts/chinese-ime-r1/10-chinese-deepseek-reply.png`。
- 外部图片服务门禁验证：实时模型演示模式不再启用本地确定性占位图；未配置外部图片生成服务时明确返回“图片生成功能暂未开放”，且没有生成假作品；证据见 `artifacts/chinese-ime-r1/11-external-image-required.png`。
- 豆包 Seedream 实时接口验证：服务器真实请求已成功返回 JPEG 图片，大小 671,792 字节，分辨率 1776×2368；后端会立即下载服务商临时 URL、校验图片类型和大小，再写入项目媒体存储。证据见 `artifacts/volcengine-live-r1/seedream-result/live.jpg`。
- Android 真实业务链验证：应用能完成“提出想法→DeepSeek 首轮分析→保存上传草稿→后台调用 Seedream”。第二次生图请求被服务商以账户余额不足拒绝，后端已将该状态映射为不含模型名和开发信息的“图片创作额度不足，请联系老师”；充值或补充可用额度后需重新执行一次最终端到端验收。
- 充值后复验：通用方舟推理密钥重新通过鉴权，独立调用成功返回 679,184 字节 JPEG；Android 随后完整完成“提出想法→真实教练回复→保存上传草稿→Seedream 作品预览→保存作品”。后台任务状态为 `COMPLETED`、进度 100%，会话状态为 `SAVED`，结果版本已加入个人档案。证据见 `artifacts/volcengine-live-r3/`。
- `git diff --check`：无空白错误；仅有仓库 Windows 行尾提示。

## 9. 最终截图路径

- `artifacts/conversation-ui-acceptance-r13/01-login.png`：登录入口
- `artifacts/conversation-ui-acceptance-r13/02-home-onboarding.png`：首页与首次引导
- `artifacts/conversation-ui-acceptance-r13/creation-entry-guide.png`：作品创作入口引导
- `artifacts/conversation-ui-acceptance-r13/04-first-coach-analysis.png`：提交想法后直接进入首次教练分析
- `artifacts/conversation-ui-acceptance-r13/05-revised-dialogue.png`：采纳及提出新要求后的持续对话
- `artifacts/conversation-ui-acceptance-r13/06-first-result.png`：第一版结果及直接沟通输入框
- `artifacts/conversation-ui-acceptance-r13/07-final-result.png`：结果页发出第二次要求后的新版结果
- `artifacts/conversation-ui-acceptance-r13/08-work-saved.png`：作品保存成功
- `artifacts/conversation-ui-acceptance-r13/09-archive-original.png`：原创记录荷花
- `artifacts/conversation-ui-acceptance-r13/10-archive-versions.png`：修改版本记录荷花
- `artifacts/conversation-ui-acceptance-r13/11-archive-coach.png`：真实教练对话荷花
- `artifacts/conversation-ui-acceptance-r13/12-archive-continue.png`：从档案继续创作并恢复结果
- `artifacts/conversation-ui-acceptance-r13/creation-entry-revisit.png`：入口引导不重复播放
- `artifacts/conversation-ui-acceptance-r13/13-restart-resume.png`：应用重启后恢复同一结果与输入框
- `artifacts/conversation-ui-acceptance-r13/14-offline-send-restored.png`：服务不可用时错误可见且输入原文恢复
- `artifacts/deepseek-live-r1/04-deepseek-second-reply.png`：DeepSeek 结合上一轮内容返回第二轮中文建议
- `artifacts/chinese-ime-r1/08-chinese-entered.png`：简体中文拼音候选词成功写入应用输入框
- `artifacts/chinese-ime-r1/10-chinese-deepseek-reply.png`：键盘发送动作提交中文消息并获得 DeepSeek 真实回复
- `artifacts/chinese-ime-r1/11-external-image-required.png`：未配置外部图片服务时拒绝本地占位生成
- `artifacts/volcengine-live-r1/seedream-result/live.jpg`：豆包 Seedream 真实返回的 2K 竖图
- `artifacts/volcengine-live-r1/android/after-wait.png`：Android 真实进入 DeepSeek 首轮教练分析
- `artifacts/volcengine-live-r1/android/seedream-ui.png`：账户额度不足时生成失败并保留原对话和再次创作入口
- `artifacts/volcengine-live-r3/seedream-result/live.jpg`：充值后独立调用返回的真实 Seedream 图片
- `artifacts/volcengine-live-r3/android/03-live-dialogue.png`：Android 获得真实教练首轮分析
- `artifacts/volcengine-live-r3/android/04-seedream-preview.png`：Android 展示真实 Seedream 作品预览
- `artifacts/volcengine-live-r3/android/05-work-saved.png`：保存作品成功，主动作切换为“查看创作档案”

## 10. 尚需人工确认与风险

1. 当前本机 `services/api/.env` 已配置 DeepSeek 并完成两轮外部模型实呼；该文件已被 Git 忽略。由于本次密钥曾通过聊天发送，正式比赛前必须在 DeepSeek 控制台轮换密钥，并只把新密钥写入比赛服务器环境变量。
2. 目标比赛后端必须先应用 `0023_creation_conversations` 迁移，并确认对话模型、图片生成服务、对象存储和网络均可用；本地完整 HTTP/任务链不能代替现场网络检查。
3. 自动脚本验证了自然中文错误组件和正常链路，但没有在 r12 中主动切断模拟器网络；赛前人工验收应补一次断网、恢复网络和点击重试，并确认输入文字不会丢失。
4. 原创记录目前显示上传文件名，修改版本记录显示版本与调整摘要；受现有荷花详情固定文本区域限制，附件和历史版本缩略图尚未嵌入该页面。这不影响主流程，但与最终提示词中的“缩略图”要求仍有差距，建议在不改变荷花布局的前提下另做一次轻量预览设计确认。
5. 旧项目会通过恢复接口生成兼容会话，不删除旧记录。建议在目标环境抽取至少一个旧草稿、一个待确认生成结果和一个已提交作品做人工回归。
6. DeepSeek 只负责中文多轮沟通和生成要求整理，成品图片交给豆包 Seedream。充值后，通用方舟推理密钥已通过独立调用和 Android 完整业务链；此前提供的“套餐 API”密钥仍不适用于标准图片生成地址。比赛环境必须继续使用当前通用推理密钥，并确保余额、模型权限和网络可用。若现场改用 OpenAI Images，也只需替换服务器端图片提供方配置，不会回退到本地占位图。

## 11. Git 状态

本次修改仍保留在本地工作区，没有执行 `git commit`、`git push` 或分支合并，也没有删除用户现有数据，等待人工预览确认。
