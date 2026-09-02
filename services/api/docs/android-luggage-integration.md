# Android 行囊接入实现说明

基线日期：2026-09-01  
当前状态：聚合首屏、全部详情入口和作品项目修订链路已接入 `Appproject/android`。

## 1. 现有工程适配结论

Android 已使用 Compose、ViewModel、StateFlow、OkHttp、Gson、Coil，并由
`AuthRepository` 负责访问令牌刷新。`LuggageScreen.kt` 已有资料、成长、秘籍、错题、
作品和底部入口的静态布局，适合在页面状态准备好后逐区替换数据。

实现没有新建第二套令牌存储，也没有让 `LuggageApi` 直接读取 `EncryptedTokenStore`：

1. `LuggageApi` 负责带 `If-None-Match` 的请求和领域 DTO 解析。
2. `AuthRepository.withAccessToken` 复用唯一的 401 刷新/重放路径。
3. `LuggageRepository` 保存进程内最近成功快照和 ETag，并在账号变化时清除。
4. `LuggageViewModel` 由导航层共享注入，驱动首屏和详情页状态。

以后若更多领域都需要认证请求，再把 `AuthApi` 中的 OkHttp/Gson/错误解析提取为共享
`AuthenticatedApiClient`；本次不要为一个页面提前大范围重构。

## 2. DTO 文件清单

建议新增 `com.jueqiao.jianghu.luggage.LuggageModels.kt`。继续沿用工程现有做法，所有
snake_case 字段用 `@SerializedName` 显式映射。根模型：

```kotlin
data class LuggageResponseDto(
    val data: LuggageDataDto,
    val meta: LuggageMetaDto,
)

data class LuggageMetaDto(
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("snapshot_version") val snapshotVersion: Long,
    val etag: String,
)
```

`LuggageDataDto` 必须包含：

- `profile`：`nickname`、`avatar`、`age_band`、`class_label`、`anonymous_id`、
  `current_title`、`badges`。
- `stats`：`week.practice_count`、`lifetime_practice_count`、
  `distinct_trials_passed`、`evidence.wisdom/craft/chivalry`。
- `manuals`：`total`、`obtained`、`counts_by_state`、`items`、`empty_reason`、
  `detail_url`。
- `mistakes`：`pending_count`、`items`、`empty_reason`、`detail_url`。
- `creations`：`counts_by_status`、`items`、`empty_reason`、`detail_url`。
- `privacy`：`guardian_controls_active`、`pending_appeal_count`、
  `privacy_settings_url`。

枚举先按字符串接收并在 Repository 映射为 sealed UI 类型，避免后端增加兼容状态时 Gson
直接解析失败。秘籍状态固定显示：`UNSEEN/未闻`、`DISCOVERED/偶得`、
`LEARNED/习得`、`MASTERED/悟得`、`TEACHING/传习`。

图片 DTO 只保存 `asset_id`、`url`、`expires_at`。数据库身份是 `asset_id`，签名 URL
只能交给 Coil 临时加载，不能写入长期偏好或 Room 主键。

## 3. HTTP 与 ETag

建议让 API 层返回：

```kotlin
sealed interface LuggageHttpResult {
    data class Fresh(val body: LuggageResponseDto, val etag: String) : LuggageHttpResult
    data object NotModified : LuggageHttpResult
}
```

请求规则：

- 路径：`GET /v1/me/luggage`。
- 请求头：`Authorization: Bearer ...`；存在本地 ETag 时增加 `If-None-Match`。
- 200：解析 JSON，优先保存响应头 ETag，并校验它与 `meta.etag` 一致。
- 304：不解析空 body，继续使用 Repository 最近成功快照。
- 401：沿用 `AuthRepository.authorized` 刷新一次；再次 401 转登录页。
- 其他错误：沿用 `AuthApiException`，保留 request ID 便于排查。

进程内缓存即可；第一版不要把包含班级、私有作品和短时 URL 的完整行囊快照明文写入
SharedPreferences。若后续需要离线展示，应使用加密数据库，并在退出登录、切换账户或
数据删除时清空。

## 4. Repository 和 ViewModel

`LuggageRepository.refresh(force: Boolean)`：

- 普通刷新携带最近 ETag；下拉强制刷新可不带 ETag。
- 200 时原子替换快照和 ETag。
- 304 时返回现有快照；若本地快照意外为空，则无 ETag 重试一次。
- 登出时执行 `clear()`，防止下一账号看到上一账号的私人数据。

推荐页面状态：

```kotlin
sealed interface LuggageUiState {
    data object Loading : LuggageUiState
    data class Content(val model: LuggageUiModel, val refreshing: Boolean) : LuggageUiState
    data class Error(val message: String, val canRetry: Boolean) : LuggageUiState
}
```

区块空状态不要提升成全页错误：

- `NO_OBTAINED_MANUALS`：显示“完成一次修炼，就会在这里遇见第一本秘籍”。
- `NO_MISTAKES`：显示“暂时没有待巩固的错题”。
- `NO_CREATIONS`：显示“还没有作品，去造物坊试试吧”。
- `PENDING_CHECK/PENDING_HUMAN_REVIEW`：正常内容状态，展示“检查中/待人工复核”。
- `RETURNED`：展示 `return_reason` 和修订入口，不能归为网络错误。

已有内容刷新失败时保留旧内容并显示非阻断提示；首次加载失败才显示全页错误。Compose
重组键使用秘籍 ID、错题 ID、作品 project ID，不使用列表位置。

## 5. 已完成的页面连接

当前按以下顺序完成并可独立验收：

1. 资料与“本周修炼/累计修行/通关试炼”，删除所有静态昵称和“签到”含义。
2. 三类成长证据服务端摘要；“查看成长记录”连接本周证据，“学习证据”连接全部证据。
3. 最近秘籍和五态计数；筛选、搜索、十卷和分页跳转到 `/v1/manuals`。
4. 最近错题可进入详情查看原答案、错误原因、关联秘籍、重练记录和巩固状态；“再试一次”调用响应中的 `retry_url`。
5. 最近作品、缩略图、审核状态、退回原因和继续修订。
6. 隐私设置、数据导出/删除申请、申诉、撤回、学习卡与来源谱入口。
7. “继续创作”携带作品项目 ID，恢复最近文字层或项目独立草稿；完整创作页完成前只暂存本机草稿，绝不创建服务端作品版本。

首屏始终只调用一次行囊聚合；“查看全部”和详情页面进入后再请求各自分页接口。不要在
进入行囊时并发调用资料、统计、秘籍、错题和作品五个接口。

## 6. 后续按钮对应接口

- 我的秘籍/搜索/筛选：`GET /v1/manuals`。
- 全部学习证据：`GET /v1/me/learning-evidence`；单本解释继续使用
  `GET /v1/manuals/{id}/evidence` 与 `/learning-history`。
- 我的错题：`GET /v1/mistakes`、`GET /v1/mistakes/{id}`。
- 再试一次：`POST /v1/mistakes/{id}/retry-sessions`，随后提交试炼 Attempt。
- 我的作品/修订：`GET /v1/me/creation-projects` 与版本相关接口。
- 上传：intent -> 对象存储 PUT -> complete -> 轮询媒体资产状态。
- 审核/申诉/撤回：Publication 与 Moderation 接口。
- 隐私与安全：`GET|PATCH /v1/me/privacy-settings` 和账户数据权利接口。

是否展示入口先读取 `GET /v1/meta/capabilities`。暂时没有按钮的能力可以隐藏，但不要用
静态假数据代替服务端事实。

## 7. Android 验收清单

- 200、304、首次网络错误、已有内容刷新错误均有测试。
- 令牌过期只刷新一次，并重放原行囊请求。
- 登出/切换账号清除快照和 ETag。
- 三个区块空状态与作品 `RETURNED` 状态可单独截图验收。
- Coil 图片过期或 403 时刷新行囊，不永久重试旧 URL。
- TalkBack 能读出秘籍状态、作品审核状态和按钮用途。
- 页面不显示公开总榜、粉丝数、人气排名或连续签到惩罚。
- Android 默认 API 地址与 Docker 对外端口统一；当前 Docker 是 `8010`，模拟器应配置
  `http://10.0.2.2:8010/`。
- 本地媒体预签名地址需要把 Compose 的 `MINIO_PUBLIC_ENDPOINT` 配置为
  `10.0.2.2:19000`；APK 不应收到容器内部的 `minio:9000`。

2026-09-01/02 本地验收：Kotlin 编译和 debug APK 打包成功；模拟器验证聚合 200/304、
令牌 401 刷新重放、勋章空状态、成长证据空状态、秘籍搜索/状态/卷筛选及秘籍详情。
错题空状态也已通过真实接口验证。新建测试作品后，项目版本接口成功创建 V1、以 V1 为父版
创建 V2，并读取到最新文字层；测试作品已删除。这是后端版本接口的独立契约验证；当前行囊仅传递
项目上下文、暂存本机草稿，不由占位创作页写入版本。所有详情页保留首次错误、陈旧内容非阻断提示
和可直接点击的“重新加载”。

## 8. 本地真实接口验收（2026-09-02）

在 Docker 本地服务健康、使用一次性测试账号的前提下完成以下验收；每轮结束后均按唯一设备标记
精确删除测试用户、会话和关联审计记录。

- 空状态：`GET /v1/me/luggage` 返回 200，秘籍总数为 50，秘籍、错题、作品分别返回
  `NO_OBTAINED_MANUALS`、`NO_MISTAKES`、`NO_CREATIONS`；勋章、学习证据、秘籍、错题、作品和隐私
  详情接口均返回 200。
- 缓存：使用服务端原样返回的弱 ETag 重新请求行囊，返回 304。客户端必须原样保存和发送 ETag，不能
  只保留引号内的标签值。
- 有数据状态：一次失败试炼与一次正确试炼后，行囊返回本周修炼 2 次、通关试炼 1 个、习得秘籍 1 本、
  待巩固错题 1 条、悟性证据 1 条。
- 错题重练：错题详情返回原答案；重练会话与重练提交均返回 201，提交后详情返回“重练中”、1 条重练记录
  及服务端计算的下次巩固时间。延迟后再次通过才会转为“已巩固”。

这份结果只验证真实 HTTP 契约和服务端投影；Android 页面继续以同一接口展示加载、空数据和网络错误，
不需要为尚未制作的详情页面添加模拟数据。
