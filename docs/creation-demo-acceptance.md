# 作品创作竞赛演示验收报告

验收日期：2026-09-10

结论：作品创作主链路已达到可复跑的比赛演示状态。`demo` 与 `acceptance` 为独立安装包，均固定连接比赛后端；三个全新学生账号均从 Android 可见界面完成业务门禁，没有直接修改数据库或调用接口跳过学生阶段。

两份策划书只作为功能与视觉参考，本文和验收脚本以实际代码、实际接口及用户提出的六项修改要求为准。

## 已完成的修改

1. 增加 `demo`、`acceptance` 两个独立构建类型，包名分别为 `com.jueqiao.jianghu.demo`、`com.jueqiao.jianghu.acceptance`，后端地址固定为 `http://10.0.2.2:8011/`。启动脚本会先检查 `http://127.0.0.1:8011/healthz`，服务不可用时中止或自动拉起一次性比赛服务。
2. 所有面向学生的阶段、规格、质量、提交状态和工具说明均使用自然中文。三轮无障碍树扫描未出现 `DRAFT`、`SQUARE`、`MEDIUM`、`development`、模型名或执行器名。
3. 阶段主动作已统一：草图为“保存并继续”，制作和测试为“下一步”，封卷四项全部完成后才显示“提交作品”。
4. 新版创作档案只保留“继续创作”主动作与“更多”次动作；撤回、申诉、删除均收进“更多”。继续创作只新建一个版本，并进入制作阶段。
5. 制作弹窗按“生成画面、教练确认、画布整理、完成制作”逐项展示；封卷按“作品说明、学习回顾、来源说明、隐私与投递”四步展示。每页只处理当前任务，并保留一个明确主按钮。
6. 图片生成、教练调用、测试问题、封卷、班级投递、审核结果、撤回、申诉和删除均已连接正式业务接口。图片下载使用有时效签名的媒体地址，错误签名会被拒绝。
7. 荷花记录热区、作品选择、发送给创作教练、协议勾选等交互已补充无障碍名称，不改变原有画面和布局。
8. Android 请求增加连接复用、短保活及一次安全 GET 重试；比赛服务启用 SQLite WAL、busy timeout 与连接预检，降低连续演示时的 EOF 和锁库风险。

## 真实验收路径

完整作品由界面依次完成：

> 登录 → 加入班级 → 提出创意 → 调整并确认工法 → 保存草图版本 → 生成图片并查看真实图片 → 确认调用创作教练 → 在画布做有效修改并保存新版本 → 记录测试问题 → 处理问题 → 复测通过 → 四步封卷 → 班级投递 → 审核发布 → 从档案继续创作 → 撤回

第二作品用于验证精简路径：图片生成、教练建议和画布编辑可以跳过，但测试、封卷和投递门禁不能跳过。作品被审核退回后，从档案“更多”提交申诉；脚本验证不能重复申诉，并在应用重启后再次读取“申诉处理中”。最后从“更多”删除两个测试作品并验证空档案。

HTTP 只用于：注册全新学生与教师、创建班级、只读断言、下载签名图片，以及模拟审核员作出发布/退回决定。所有学生拥有的阶段变更都来自 Android 可见控件。

## 三轮正式结果

| 证据目录 | 构建包 | 全新学生账号 | UI 检查 | API 复核 | 截图 |
|---|---|---:|---:|---:|---:|
| `device-demo-20260910-172930` | `com.jueqiao.jianghu.demo` | `13932570782` | 210 | 21 | 31 |
| `device-demo-20260910-175834` | `com.jueqiao.jianghu.demo` | `13934314083` | 210 | 21 | 31 |
| `device-acceptance-20260910-181439` | `com.jueqiao.jianghu.acceptance` | `13935279535` | 211 | 21 | 31 |

第三轮比前两轮多一项“按无障碍名称勾选用户协议”检查。每个目录都含 `result.json`、31 张 PNG 截图及对应页面 XML。关键截图包括 `generation-preview-*`、`coach-confirm-*`、`test-needs-revision-*`、`seal-delivery-*`、`archive-main-selected.png`、`appeal-pending-more.png`、`appeal-pending-after-restart.png` 和 `archive-empty.png`。

## 自动化与构建结果

- 后端：109 项 pytest 全部通过。
- Android：`:app:testDemoUnitTest`、`:app:testAcceptanceUnitTest` 全部通过。
- 构建：`:app:assembleDemo`、`:app:assembleAcceptance` 全部成功。
- 验收脚本与比赛服务脚本通过 Python 语法检查。
- `git diff --check` 通过；仅有仓库既有的 LF/CRLF 提示，没有补丁空白错误。
- 三轮证据中的 UI 与 API 结果均为 `pass`，日志中没有本应用的 EOF、SQLite busy/locked 或崩溃。

最新 APK：

- `android/app/build/outputs/apk/demo/app-demo.apk`

  SHA-256：`B488084CF61FA0B56D0F79B83F292AB975186B358EC94D275EE3257CB6AE19E4`
- `android/app/build/outputs/apk/acceptance/app-acceptance.apk`

  SHA-256：`B999261A4C3B9FC79C5EA12EC70706C2F17FC3A0BD7A5A7672EB28668A73B812`

## 比赛现场复演

在项目根目录执行：

```powershell
.\infra\run-demo-acceptance.ps1 -Mode demo -Device emulator-5554
```

脚本会检查/启动比赛后端、构建 APK、安装并清空应用数据、生成全新账号，然后从登录页开始完整操作并保存证据。复跑独立验收包：

```powershell
.\infra\run-demo-acceptance.ps1 -Mode acceptance -Device emulator-5554
```

证据统一写入 `artifacts/creation-acceptance/`。如果比赛后端已由人工启动，可加 `-SkipApiStart`；正式验收不要使用 `-SkipWorkflowCheck` 或 `-SkipDeviceFlow`。

## 演示边界与现场建议

- 一次性比赛服务的图片 worker 会消费正式生成任务，并返回可下载的确定性图片，因此断网环境也能稳定演示；它不代表线上第三方生成模型的画质或耗时。
- 审核员发布/退回由内部审核接口模拟，因为学生端不应拥有审核权限；学生看到的检查中、已发布、需要修改和申诉处理中均来自后端真实状态。
- 完整路径步骤较长是业务门禁本身造成的。现场主讲建议演示完整作品；第二作品的精简路径用于回答“图片、教练和画布是否必须重复操作”。
- 正式上场前仍应使用比赛当天的电脑和模拟器运行一次完整命令，确认 8011 端口、磁盘空间和设备连接没有被其他软件占用。
