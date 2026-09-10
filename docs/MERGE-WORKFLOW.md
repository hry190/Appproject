# 分支合并工作流 — Appproject

> **给 AI 助手 + 新人的标准 SOP。** 每次要把一个分支合并到 main / zzz,**严格按 4 步走**,不要跳过侦察和评估直接合并。
>
> 来源:2026-09-10 合并 `feature/creation-contest-demo` 时的教训(没有冲突但实际有 1 个文件 auto-merge,git 不报错不警告)。

---

## 0. 何时用这份 SOP

满足以下任一条件时启动:
- 收到"推 main"、"合并到 main"、"merge zzz"、"把 X 分支合过来"等指令
- 自己想把当前的 zzz 工作合并到 main
- 看到 `origin/*` 上有新的远端分支,想知道它在干什么

**绝对不要跳过任何步骤直接合并** — 即使分支看起来"只是几行改动",因为:
- git 的 "Automatic merge went well" **不等于** "没有冲突"
- auto-merge 是 git 的 **默认行为**,成功后不报错、不警告、不在 UI 突出显示
- 只有**真正冲突**才会显示 `<<<<<<<` markers
- 即使没有 conflict,合并结果也可能不符合预期(逻辑错误 / 误删功能)

---

## 4 步流程

### Step 1. 侦察 — 了解分支内容(必须)

**目标**:理解这个分支做了什么、动了哪些文件、有没有特殊 commit。

**命令**:
```bash
cd d:/Appproject
git fetch origin                    # 先抓远端,确保看的是最新

# 1.1 看分支头(只属于这个分支的 commits)
git log --oneline <base-branch>..origin/<branch>     # 默认风格

# 1.2 看 commit 详情(选 1-2 个最近的大 commit 看 message + diff stat)
git show --stat <commit-sha>

# 1.3 看改了哪些文件 + 文件级 stats
git diff <base-branch>..origin/<branch> --stat

# 1.4 如果是创建/新功能分支,看 README 或 docs 是否有说明
# 例: docs/creation-demo-acceptance.md 是 feature/creation-contest-demo 的设计说明
```

**注意**:
- "分支领先多少 commit" 用 `git log --oneline <base>..HEAD | wc -l`
- "领先多少文件" 用 `git diff --stat <base>..HEAD | tail -1`
- "删除了哪些 zzz 上有的文件" 用 `git diff --stat | grep -E "^[^A]"`

**这一步的产出**:1 段 ~5 行的中文摘要,放进最终报告。

---

### Step 2. 评估 — 查看是否有冲突(必须)

**目标**:在 `--no-commit` 之前,**真实判断**会不会冲突,以及冲突严重程度。

**步骤**:

```bash
# 2.1 找出合并基点(merge-base)
git merge-base <base-branch> origin/<branch>

# 2.2 找出两边都改过的文件
# zzz 改了哪些 + 分支改了哪些 → 取交集
common=$(comm -12 \
  <(git diff --name-only <merge-base>..<base-branch> | sort) \
  <(git diff --name-only <merge-base>..origin/<branch> | sort))
echo "两边都改的文件:"
echo "$common"
```

**判定**:
- **空交集** → 完全没有重叠改动,**理论上 0 conflict**
- **交集里 KDoc / build.gradle** → 大概率自动合并 OK
- **交集里有 NavHost / 路由注册** → 大概率自动合并 OK,但要检查合并后路由是否齐全
- **交集里有 Composable 的 Modifier 链** → 容易真实冲突,需手动解决
- **交集里有 Routes.kt / RoutesTest.kt** → **必须**用 grep 验证合并后所有路由都还在

**这一步的产出**:潜在冲突文件列表 + 严重程度评级(低/中/高)。

---

### Step 3. 建议 — 给用户明确推荐(必须)

**目标**:把 Step 1 + Step 2 的发现整理成可决策的报告,**等用户拍板**。

**报告格式**(必须包含):

```
## 分支:<branch-name>
## 领先 commit 数:N
## 改动文件数:M(+A / -D)
## 与 zzz 重叠文件:<list>
## 冲突风险:低 / 中 / 高(说明原因)
## 关键发现:<1-2 句话突出重点>
## 推荐:立即合并 / 修复后合并 / 暂缓合并(说明)
## 风险点:<可能丢失的东西 / 兼容性问题>
```

**注意**:
- 一定要给"推荐" — 不要说"你自己看吧"
- 如果是高风险,推荐"暂缓"并指出需要先做什么
- 如果有边界条件(比如会删除 zzz 上的某些文件),必须明确列出

---

### Step 4. 合并 — 用户同意后才执行(必须)

**用户明确说"合并"或"推 main"后**,按以下步骤:

```bash
# 4.1 试合并(不提交,留余地)
git merge origin/<branch> --no-commit --no-ff --strategy=recursive

# 4.2 立刻验证编译 + 测试
export JAVA_HOME="C:/Users/28784/.jdks/jbr-21.0.11"
cd android
./gradlew.bat compileDebugKotlin
./gradlew.bat testDebugUnitTest

# 4.3 检查 auto-merge 结果(关键!)
# git 输出 "Auto-merging X" 表示 X 被自动合并
# 即使没有 conflict 标记,也要验证 X 的内容是合并后的正确版本
grep -nE '^(<<<<<<<|=======|>>>>>>>)' <X>    # 必须为空
cat <X> | head -50                              # 肉眼检查 50 行
git diff --cached <X>                            # 看 staged 改动是不是预期

# 4.4 通过 → commit + 推
git commit -m "merge: <branch-name> into <target>

<summary>

Verified:
- compileDebugKotlin ✓
- testDebugUnitTest ✓
- 0 conflicts
- <key files manually inspected>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
Co-Authored-By: Claude Code <noreply@anthropic.com>"

# 4.5 推远端
git push origin <branch>

# 4.6 如果是合并到 main,完成 fast-forward / merge main → 推 main → 同步 zzz 到 main
git checkout main
git merge <branch> --no-ff --strategy=recursive -m "merge <branch> ..."
git push origin main
git checkout <branch>
git reset --hard main
git push --force origin <branch>
```

---

### 中途发现真冲突 --abort 退回

如果 Step 4.3 发现:
- 任何文件有 `<<<<<<<` 标记
- compileDebugKotlin 失败
- testDebugUnitTest 失败
- 合并后路由 / 关键功能 丢失

**立即撤回**:
```bash
cd /d/Appproject
git merge --abort
# zzz 回到合并前状态(clean)
```

然后告诉用户:
- 冲突在哪
- 为什么冲突(各自的改动)
- 是否要手动解决 / 用 `git merge -X theirs` / `git merge -X ours` / rebase / cherry-pick 替代方案

---

## Windows bash git 已知 bug(2026-09-09 发现)

`git merge --no-ff` 或 `git merge --ff-only` 在 Windows git bash 下会被错误解析为策略名 `theirs`,导致:

```
Could not find merge strategy 'theirs'.
Available strategies are: octopus ours recursive resolve subtree.
```

**修复**:必须显式指定 `--strategy=recursive`:
```bash
# ❌ 报错
git merge zzz --no-ff

# ✅ 成功
git merge zzz --no-ff --strategy=recursive
```

`git reset --hard main`(fast-forward 替代方案)不受这个 bug 影响。

---

## 关键教训(2026-09-10 creation-contest-demo 合并)

1. **"Automatic merge went well" 不等于 "no conflict"** — git 输出这句话意味着它**自动成功合并**,不是"两边都没改"
2. **"Auto-merging X" 是关键信号** — 这个文件被两边都改了,git 自动选了合并方式
3. **必查 .orig 备份文件** — `find . -name '*.orig'` 在仓库根查找,如果没有 = git 没产生 conflict
4. **必查 conflict markers** — `grep -nE '^(<<<<<<<|=======|>>>>>>>)' <files>` 必须为空
5. **必查合并基点** — `git merge-base` 决定哪些文件双方都有
6. **删文件 ≠ 冲突** — 如果分支从 X 删了 PNG 但 zzz 上有,**git 自动接受删除**(除非 zzz 也改了那个 PNG)
7. **汇总报告给用户前** — 列出关键文件(如 Routes.kt、NavHost.kt、屏幕组件)的两边改动 + auto-merge 结果

---

## 检查清单(复制这个)

```
□ Step 1 — 侦察
  □ git fetch origin
  □ git log --oneline <base>..origin/<branch> — 看 commits
  □ git diff --stat <base>..origin/<branch> — 看文件范围
  □ 看分支 README / docs 文件(如 docs/creation-demo-acceptance.md)

□ Step 2 — 评估
  □ git merge-base 找共同祖先
  □ comm -12 找两边都改的文件
  □ 评级风险:低/中/高

□ Step 3 — 建议
  □ 给用户写 1 段中文摘要(领先 N commit、M 文件、冲突风险、关键发现)
  □ 给"推荐"(立即合并 / 修复后合并 / 暂缓)

□ Step 4 — 用户同意后才执行
  □ git merge ... --no-commit --no-ff --strategy=recursive
  □ compileDebugKotlin ✓
  □ testDebugUnitTest ✓
  □ grep conflict markers → 0
  □ git status → "all conflicts fixed"
  □ git commit + push

□ 中途发现问题
  □ git merge --abort
  □ 告诉用户冲突在哪、为什么、替代方案

□ 合并后
  □ 完整推送链:target 分支 push → main push → 其他分支 reset + push
  □ git log --graph --oneline -8 验证拓扑合理
```

---

## 完整例子:2026-09-10 合并 feature/creation-contest-demo

```bash
# Step 1
git fetch origin
git log --oneline main..origin/feature/creation-contest-demo
# → 6 commits: chore/feat(android)/feat(api)/test/merge/docs

git diff --stat main..origin/feature/creation-contest-demo
# → 52 files changed, +5251/-1456

# Step 2
git merge-base zzz origin/feature/creation-contest-demo
# → 1ee4549 (之前合并的 base)

comm -12 <(git diff --name-only 1ee4549..zzz) \
         <(git diff --name-only 1ee4549..origin/feature/creation-contest-demo)
# → JianghuNavHost.kt(只有这个两边都改)

# Step 3 — 给用户的报告
# 推荐:低风险,可合并。
# - 创建大赛功能完整
# - 与 zzz 重叠只有 NavHost.kt(路由注册表)
# - NavHost 修改在不同 region(zzz 加 Volume1Part6 vs 分支加创建大赛屏),auto-merge OK
# - 注意:分支基于 1ee4549,但 zzz 已包含 Volume1Part6(4d0359a + 271af88)
#   → 合并会自动保留 zzz 新增的 Volume1Part6 PNGs 和 routes

# Step 4 — 用户同意后
git merge origin/feature/creation-contest-demo --no-commit --no-ff --strategy=recursive
# → "Auto-merging JianghuNavHost.kt" + "Automatic merge went well"

./gradlew.bat compileDebugKotlin  # → BUILD SUCCESSFUL

grep -nE '^(<<<<<<<|=======|>>>>>>>)' JianghuNavHost.kt  # → 0 个 marker

# 提交 + 推送
git commit -m "merge: feature/creation-contest-demo into zzz ..."
git push origin zzz
git checkout main
git merge zzz --no-ff --strategy=recursive -m "merge zzz into main: ..."
git push origin main
git checkout zzz
git reset --hard main
git push --force origin zzz
```
