# MERGE-PLAN: `origin/feature/creation-contest-demo` → `zzz` (2026-09-28)

> **本方案是 [docs/MERGE-WORKFLOW.md](MERGE-WORKFLOW.md) 4 步 SOP 的具体应用**,但合并规模**远超 SOP 设计**:
> SOP 09-10 案例(52 文件 / 1 conflict)→ 本次(603 文件 / 36 conflict,大 12 倍)。
>
> 用户已拍板指令(2026-09-28):
> 1. 合并 `origin/feature/creation-contest-demo` 到 `zzz`
> 2. 同名文件按 cai(`-X theirs`)
> 3. **`.gitignore` 按 zzz 版本**(用户原话)
> 4. **`android/app/build.gradle.kts` 按 zzz 版本**(改回 USB 真机地址)
>
> 状态:**方案就绪,待用户最终确认执行**。

---

## 1. 背景与目标

### 1.1 为什么合并

- `origin/feature/creation-contest-demo` 悬空 **5 天**(09-24 推最后 1 笔后停摆)
- 作者:**`Zqw66666666 <3468938747@qq.com>`**(cai),不是用户本人 (`hry190`)
- 改动巨大:**603 文件, +53,319 / -12,526**(净增 ~4 万行)
- 内容:创作 / 大会 / 演武场完整功能(Android + Backend)
- 共同祖先 `833d3b4` 比 zzz 当前祖先 `dc3e61d` 还老 → cai 那 2 笔的 diff 跨越了主分支 239 笔中间工作

### 1.2 zzz 相对祖先的改动(**冲突复杂度评估的关键**)

zzz HEAD `2757c48` 相对共同祖先 `dc3e61d` **只改了 5 个文件**:

| 文件 | 改动 | 类型 |
|---|---|---|
| `.gitignore` | +1 行(`**/uvicorn.*.log`) | M |
| `docs/SESSION-LOG-2026-09-28.md` | +277 行 | A |
| `docs/TROUBLESHOOTING.md` | +294 行 | A |
| `infra/diag.ps1` | +60 行 | A |
| `infra/start-reverse.ps1` | +45 行 | A |

> **zzz 完全没动核心代码**(Android Kotlin + Python backend)。这意味着 36 个 conflict 文件 zzz 都没改,只是因为中间走了 239 笔 main 的 commit,文件版本比 cai 旧,**冲突本质是"采纳 cai 还是 main"**。

---

## 2. 侦察报告(Step 1 - 已完成)

### 2.1 分支拓扑(2026-09-28 收工快照)

```
origin/HEAD → origin/main
origin/main                        = ca2f606 (09-22 merge)
origin/zzz                         = dc3e61d (09-22)
zzz (本地,当前)                    = 2757c48 (今天 +3 commit)
origin/feature/creation-contest-demo = b5d2c8c (09-24)
```

**共同祖先**:`833d3b4 docs(session-log): complete §17 with merge result`

### 2.2 数字

| 维度 | 值 |
|---|---|
| cai 领先 zzz | 2 commit |
| zzz 领先 cai | 241 commit |
| cai vs zzz 文件差异 | **603 文件** |
| cai vs zzz 净增行 | **+53,319 / -12,526** |

### 2.3 关键 commit 摘要

**`b5d2c8c feat: sync Android creation flows and backend updates`** (09-24, cai):

| 文件 | 变更 | 风险等级 |
|---|---|---|
| `JianghuNavHost.kt` | **779 行变更** | 🔴 导航中心 |
| `AuthApi.kt` / `AuthModels.kt` | 减 23 行 | 🟡 鉴权 API |
| `AuthRepository.kt` / `AuthViewModel.kt` | 核心鉴权路径 | 🔴 |
| `LuggageApi.kt` / `LuggageModels.kt` / `LuggageRepository.kt` | 内容 API | 🟡 |
| `LuggageViewModel.kt` | 256 行变更 | 🟡 |
| `CreationViewModel.kt` | 225 行变更 | 🟡 |
| `DistributionViewModel.kt` | **删除 201 行** | 🟡 删功能 |
| `Routes.kt` | 65 行变更 | 🟡 路由表 |
| `services/api/tests/test_creation_growth.py` | **删除 212 行** | 🟡 删测试 |
| `services/api/tests/test_creation_archive_api.py` | **删除 60 行** | 🟡 |

---

## 3. 冲突评估(Step 2 - 已完成)

### 3.1 3-way merge 模拟结果

通过 `git merge-tree $(git merge-base) zzz origin/feature/...` 输出统计:

| 状态 | 文件数 | 说明 |
|---|---|---|
| **changed in both**(真 conflict marker)| **36** | 必须手动选 ours/theirs |
| **merged**(auto success)| **51** | git 自动合并,需肉眼检查 |
| **zzz 独有**(cai 没有,自动保留)| **5** | .gitignore 改 + 4 新文件 |

### 3.2 36 个 conflict 文件(完整清单)

**Android 核心(7)**:
- `android/app/build.gradle.kts` ⚠️ **AUTH_BASE_URL 默认值冲突**
- `android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt`
- `android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt`
- `android/app/src/main/java/com/jueqiao/jianghu/creation/CreationViewModel.kt`
- `android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageApi.kt`
- `android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageModels.kt`
- `android/app/src/main/java/com/jueqiao/jianghu/luggage/LuggageRepository.kt`

**Android UI Screen(13)**:
- `chuangzuodangan/ChuangzuodanganScreen.kt`
- `dahui/ConferenceScreens.kt`
- `gongfang/{CreationHomeModels,GongfangScreen}.kt`
- `gunlun1/Gunlun1Screen.kt`
- `home/{Home1Screen,HomeWindBackground}.kt`
- `houshan1/2/3/Houshan*Screen.kt`
- `learning2/Learning2Screen.kt`
- `login/LoginScreen.kt`
- `shengtu/ShengtuScreen.kt`
- `yanwuchangvideomy/YanwuchangVideoMyScreen.kt`

**Android Tests(1)**:
- `android/app/src/test/java/com/jueqiao/jianghu/nav/RoutesTest.kt`

**Backend Python(10)**:
- `services/api/.env.example` ⚠️
- `services/api/app/api/routes/creations.py`
- `services/api/app/core/config.py`
- `services/api/app/domains/conference/service.py`
- `services/api/app/domains/creations/{contracts,image_generation,image_generation_service,service}.py`
- `services/api/scripts/{accept_conference_workflow,accept_creation_workflow,android_creation_acceptance}.py`

**Backend Tests(3)**:
- `services/api/tests/test_conference_api.py`
- `services/api/tests/test_creations_api.py`
- `services/api/tests/test_media_moderation_privacy.py`

### 3.3 风险评估

| 风险点 | 等级 | 说明 |
|---|---|---|
| `android/app/build.gradle.kts` AUTH_BASE_URL | 🔴 高 | zzz=`127.0.0.1`(USB 真机),cai=`10.0.2.2`(模拟器);`theirs` 后真机调试坏 |
| `services/api/.env.example` | 🟡 中 | cai 改后,本地 `.env` 里 `JIANGHU_ALLOWED_HOSTS` 等可能缺 key → App 报 "暂时无法连接"(参 [TROUBLESHOOTING §2-⑤](TROUBLESHOOTING.md)) |
| 34 个其他 conflict 代码 | 🟡 中 | cai 整体重写,可能覆盖 zzz 上的小优化;但 zzz 没动核心代码,所以无 zzz 工作丢失 |
| `.gitignore` | 🟢 低 | **不在** 36 冲突列表(只有 zzz 改了,cai 没改)→ 自动 merged,但用户明确指令按 zzz,所以强制覆盖一次 |
| `JianghuNavHost.kt` 779 行变更 | 🟡 中 | 导航结构改动,影响所有页面跳转;合并后需验证 5+ 页能正常跳转 |
| `DistributionViewModel.kt` 删 201 行 | 🟡 中 | 删功能;如果 zzz 有依赖会编译报错 |

---

## 4. 用户决策记录(Step 3)

### 4.1 用户拍板指令

| # | 指令 | 来源 |
|---|---|---|
| 1 | 合并 `origin/feature/creation-contest-demo` 到 `zzz` | 用户 |
| 2 | 同名文件按 cai 版本(`-X theirs`) | 用户 |
| 3 | `.gitignore` 按 zzz 版本(我们今天的 54 行) | 用户原话:"按我的ignore" |
| 4 | `android/app/build.gradle.kts` 按 zzz 版本(改回 `127.0.0.1`) | 隐含(否则 USB 真机坏) |

### 4.2 偏离标准 SOP 的地方

[docs/MERGE-WORKFLOW.md](MERGE-WORKFLOW.md) Step 4 推荐:
```bash
git merge origin/<branch> --no-commit --no-ff --strategy=recursive
# 然后手动 grep markers + 逐个处理
```

**本次采用**:
```bash
git merge origin/<branch> --no-ff -X theirs --strategy=recursive
```

**优势**:36 个 conflict 自动按 cai 版本,无需手动逐个处理。

**代价**:
1. `build.gradle.kts` 会被自动改成 cai 版本(`10.0.2.2`),需 `sed` 改回
2. SOP「自动合并必须手动检查」的安全网被削弱,必须靠 §5.5「编译验证」兜底
3. 不能逐文件确认 ours/theirs 选择是否符合用户意图

**为什么这次值得偏离**:36 个 conflict 比 SOP 设计场景(09-10 案例 1 个)大 36 倍,**手动逐个处理不现实**。

---

## 5. 执行步骤(Step 4 - 待用户确认)

### 5.1 前置检查

```bash
cd d:/Appproject

# 工作区干净
git status -sb    # 必须 clean(或只有今天的 4 untracked,实际已 commit)

# JDK 已设(memory rule)
export JAVA_HOME=C:/Users/28784/.jdks/jbr-21.0.11
$JAVA_HOME/bin/java -version    # → 21.0.11

# 确认当前在 zzz
git rev-parse --abbrev-ref HEAD    # → zzz
```

### 5.2 触发合并(全部 `-X theirs`)

```bash
git merge origin/feature/creation-contest-demo \
  --no-ff \
  -X theirs \
  --strategy=recursive \
  -m "merge origin/feature/creation-contest-demo → zzz

按用户指令执行:
- 36 个 conflict 全部按 cai(theirs)合并
- 事后单独覆盖 .gitignore(zzz 版本,保护 uvicorn / scripts / ui_*.xml 规则)
- 事后 sed 改回 android/app/build.gradle.kts 的 AUTH_BASE_URL=127.0.0.1
- termsVersion 保留 cai 的 2026-09-r2

合并规模:603 文件, +53,319 / -12,526(详 §3.1)
36 conflict 全部走 theirs,详见 docs/MERGE-PLAN-feature-creation-contest-demo-2026-09-28.md

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

> ⚠️ **Windows bash git 已知 bug**:即便用 `-X theirs`,**也必须加 `--strategy=recursive`**,否则 git 会把 flag 解析错(参考 [MERGE-WORKFLOW.md §「Windows bash git 已知 bug」](MERGE-WORKFLOW.md))

### 5.3 强制覆盖 2 个特殊文件

```bash
# .gitignore 恢复 zzz 版本(HEAD~1 = 合并前的 zzz HEAD = 2757c48)
git checkout HEAD~1 -- .gitignore
git diff HEAD~1 -- .gitignore    # 应为空(说明 HEAD~1 的 .gitignore 跟新 working tree 一致)

# build.gradle.kts:改回 USB 真机地址(保留 cai 的 termsVersion)
sed -i 's|http://10.0.2.2:8010/|http://127.0.0.1:8010/|' android/app/build.gradle.kts
git diff HEAD~1 -- android/app/build.gradle.kts    # 应只显示 +1 / -1 行(地址那一行)
```

### 5.4 amend 到 merge commit

```bash
git add android/app/build.gradle.kts .gitignore
git commit --amend --no-edit
git log --oneline -1    # 确认是 merge commit (含 "merge origin/feature/...")
```

### 5.5 编译验证(**必须,不通过就 abort**)

```bash
# Android 编译
cd android
./gradlew.bat compileDebugKotlin    # 预期:BUILD SUCCESSFUL,否则 abort
./gradlew.bat testDebugUnitTest    # 预期:全过,否则 abort

# 后端编译 + 测试
cd ../services/api
pytest tests/ -x    # 预期:全过(或已知失败列表)
```

### 5.6 合并后完整性检查

```bash
cd d:/Appproject

# 0 conflict markers(整个仓库)
grep -rnE '^(<<<<<<<|=======|>>>>>>>)' . --include="*.kt" --include="*.py" --include="*.kts" --include="*.md" 2>/dev/null
# 预期:空输出

# .gitignore 包含我们今天加的关键规则
grep -E "uvicorn|scripts|ui_\*\.xml" .gitignore
# 预期:3 行匹配(**/uvicorn.*.log, /scripts/, ui_*.xml)

# build.gradle.kts 包含 127.0.0.1 + cai 的 termsVersion
grep -E "127.0.0.1|10.0.2.2|2026-09-r2|2026-08" android/app/build.gradle.kts
# 预期:有 127.0.0.1, 有 2026-09-r2, **没有** 10.0.2.2 或 2026-08

# zzz 上独有的 5 个文件都还在
test -f docs/SESSION-LOG-2026-09-28.md && echo "SESSION-LOG ✓"
test -f docs/TROUBLESHOOTING.md && echo "TROUBLESHOOTING ✓"
test -f infra/diag.ps1 && echo "diag.ps1 ✓"
test -f infra/start-reverse.ps1 && echo "start-reverse.ps1 ✓"

# 没漏 .orig 备份文件(MERGE-WORKFLOW.md 教训)
find . -name "*.orig" -not -path "./.git/*"
# 预期:空
```

### 5.7 推送(可选,用户决定)

```bash
# A. 推到 zzz
git push origin zzz

# B. 同步 main
git checkout main
git merge zzz --no-ff --strategy=recursive -m "merge zzz into main: ..."
git push origin main

# C. zzz 跟 main 对齐
git checkout zzz
git reset --hard main
git push --force origin zzz
```

---

## 6. 风险应对决策树

### 6.1 `compileDebugKotlin` 失败

**症状**:Kotlin 编译报红

**立即回滚**:
```bash
cd d:/Appproject
git merge --abort
git status -sb    # → clean
```

**报告给用户**:
- 完整错误信息
- 失败的具体文件 + 行号
- 失败原因分析(API 不匹配 / import 缺失 / 语法错误)
- 替代方案:
  - **方案 1**:cherry-pick `b5d2c8c` 单笔(只取 09-24 那 5 万行)
  - **方案 2**:拆分合并,先合一部分
  - **方案 3**:让 cai 自己处理冲突再合

### 6.2 `testDebugUnitTest` 失败

**回滚**:同 §6.1

**额外排查**:
- 失败的测试是否在 cai 改动的文件里(`grep` test 文件 → cai diff)
- 如果是 zzz 上的旧测试引用了 cai 删除的 API → 改测试或保留旧 API

### 6.3 `pytest` 失败

**回滚**:同 §6.1

**额外排查**:
- 删测试是否合理(`test_creation_growth.py` 212 行删 → 是 cleanup 还是回退?)
- backend config 是否要更新(`JIANGHU_ALLOWED_HOSTS` 等)

### 6.4 App 启动后 "暂时无法连接江湖驿站"

**原因**:合并后 `services/api/.env` 里关键 key 没跟上 `services/api/.env.example` 的变化

**修法**(参 [TROUBLESHOOTING.md §2-⑤](TROUBLESHOOTING.md)):
```bash
diff services/api/.env.example services/api/.env
# 把 .env.example 新增的 key 加到 .env
```

**注意**:本场景下**不需要 abort**,这是配置问题不是代码问题。

### 6.5 真机装新 APK 闪退

**症状**:`adb install -r` 成功,App 启动即闪退

**回滚**:
```bash
git merge --abort
git status -sb    # → clean

# 装旧版(merge 前的 zzz HEAD = 2757c48)
git checkout 2757c48 -- android/
cd android && ./gradlew.bat assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**报告**:logcat 完整 stacktrace

---

## 7. 验证检查清单

```
□ Step 5.1 前置检查
  □ git status clean
  □ JDK = jbr-21.0.11
  □ 当前在 zzz 分支

□ Step 5.2 触发合并
  □ --no-ff -X theirs --strategy=recursive
  □ 显式 commit message
  □ 输出 "Merge made by the recursive strategy"

□ Step 5.3 强制覆盖
  □ .gitignore ← HEAD~1 版本
  □ build.gradle.kts ← sed 改 127.0.0.1

□ Step 5.4 amend
  □ git commit --amend --no-edit
  □ git log -1 → merge commit 含 2 个修正

□ Step 5.5 编译验证(任一失败 → abort)
  □ compileDebugKotlin → BUILD SUCCESSFUL
  □ testDebugUnitTest → 全过(或已知失败)
  □ pytest → 全过(或已知失败)

□ Step 5.6 合并后完整性检查
  □ grep conflict markers → 0
  □ .gitignore 包含 uvicorn / scripts / ui_*.xml
  □ build.gradle.kts 包含 127.0.0.1 + 2026-09-r2
  □ 5 个 zzz 独有文件都在
  □ find . -name "*.orig" → 空

□ Step 5.7 推送(可选)
  □ git push origin zzz
  □ git checkout main && git merge zzz && git push origin main
  □ git checkout zzz && git reset --hard main && git push --force origin zzz
```

---

## 8. 沉淀(为下次类似合并)

1. **`-X theirs` 在大合并的代价**:必须配合「事后修正白名单文件」(本例 `.gitignore` + `build.gradle.kts`)才安全。下次类似合并,考虑**直接列白名单 ours**(用 `git checkout --ours <file>`)而非「黑名单 theirs」,减少 sed/手工覆盖环节
2. **36 conflict 超出 SOP §5 设计**:SOP 应该补充"conflict > N 时必须 cherry-pick 单笔 / 拆分 / 重新评估"的硬性规则。SOP 当前最大案例才 1 个 conflict
3. **zzz 落后 main 太多是根本原因**:241 commit 的差距导致 cai 那 2 笔的 diff 跨越了主分支 239 笔中间版本。建议:
   - 定时把 main 合回 zzz
   - 或放弃 zzz 单独 branch(直接用 main 长期分支)
4. **`AuthRepository` / `AuthViewModel` 改动要警惕**:鉴权路径核心,合并后必须真机对照登录流程(参 [TROUBLESHOOTING.md §3](TROUBLESHOOTING.md))
5. **`routes.kt` 合并后必查 grep**:SOP §5 提醒,合并后要 `grep -E "Routes\.\w+" JianghuNavHost.kt` 确认所有路由都被注册(避免 cai 删的路由让某些页 404)

---

## 9. 关联文档

- [docs/MERGE-WORKFLOW.md](MERGE-WORKFLOW.md) —— 项目合并 SOP(4 步流程)
- [docs/SESSION-LOG-2026-09-28.md](SESSION-LOG-2026-09-28.md) —— 今日日志(本合并是其中一项)
- [docs/TROUBLESHOOTING.md](TROUBLESHOOTING.md) —— §2-⑤ 后端白名单 / §3 splash vs 登录页
- `SESSION-LOG-2026-09-10` —— 上次合并 `feature/creation-contest-demo` 经验(52 文件 / 1 conflict)
- [docs/REGRESSION-chuangdang-5stages-20260919.md](REGRESSION-chuangdang-5stages-20260919.md) —— 闯荡江湖 5 关回归判据(合并后真机对照用)
- [docs/CHUANGDANG-REGRESSION-PLAYBOOK.md](CHUANGDANG-REGRESSION-PLAYBOOK.md) —— 闯荡江湖回归操作手册

---

**最后更新**:2026-09-28(草稿,待用户确认执行)