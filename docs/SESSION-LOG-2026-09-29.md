# SESSION-LOG — 2026-09-29

> 承接 [SESSION-LOG-2026-09-28.md](./SESSION-LOG-2026-09-28.md) §24(main 已 ff 到 `400508a`)。
> 本日主要工作:**后端 adb reverse 端口转发丢失的诊断与恢复**。

---

## §25 10:30 — 后端 adb reverse 丢失,跑 diag + start-reverse 恢复

**用户问题**:"看一下我的后端是不是断了"

### 背景

昨天(2026-09-28)曾因 docker daemon 没启动 + 后端容器停掉,跑过一次 `infra/diag.ps1` + `infra/start-dev.ps1` 恢复(见 09-28 §16)。今天怀疑后端又断了。

### diag 初始结果(5 项中 1 项失败)

| 检查项 | 结果 |
|---|---|
| 后端 `127.0.0.1:8010/docs` | ✅ HTTP 200 |
| **adb reverse `tcp:8010`** | ❌ **丢了**(`adb reverse --list` 为空) |
| 手机视角 → 后端 | ❌ HTTP 000(因 reverse 没设) |
| local.properties `AUTH_BASE_URL` | ✅ `http://127.0.0.1:8010/` |
| token 文件 | ✅ 存在(`2026-09-29 10:19` 新写) |

### 根因

按 [USB 重插 adb reverse 恢复] memory:
> 拔 USB / 重启手机 / 重启 adb server 都会丢这条,要重新跑。

具体触发场景未确认,但用户可能在今早(10:19 token 文件被刷新说明应用启动过)插拔过 USB 或重启过 adb。

### 修复

```bash
# 一步恢复
powershell -NoProfile -ExecutionPolicy Bypass -File infra/start-reverse.ps1 -Device 21908b7a
```

修复后输出:
```
=== 当前 reverse 映射 ===(空)

=== 设置 reverse tcp:8010 -> tcp:8010 ===
8010

=== 验证 ===
UsbFfs tcp:8010 tcp:8010
```

### 修复后 diag 全绿

| 检查项 | 结果 |
|---|---|
| 后端 | ✅ HTTP 200 |
| adb reverse | ✅ `UsbFfs tcp:8010 tcp:8010` |
| 手机 → 后端 | ✅ **HTTP 200** |
| local.properties | ✅ |
| token 文件 | ✅ |

### 教训 / 预防建议

1. **添加 alias**(可选):
   ```bash
   # ~/.bashrc 或 PowerShell $PROFILE
   alias fix-reverse='powershell -File D:/Appproject/infra/start-reverse.ps1 -Device 21908b7a'
   ```

2. **adb 监控脚本**(可选):
   - 监听 `adb devices` 输出,设备掉线重连后自动跑 reverse
   - 或用 `start-reverse.ps1` 加 systemd / Task Scheduler 定时检测

3. **.gitignore 复查**(遗留):
   - `scripts/` 整体被 `.gitignore` 排除(L54 `/scripts/`)
   - `infra/diag.ps1`、`infra/start-reverse.ps1` 在 `infra/`,**已入库**(我之前 commit `97f1c3b` 把它们一起入过 commit?—— 实际是这次顺手 commit 的,具体待查)
   - **建议确认这两个脚本在 git 里**,避免下次项目 clone 后还得手写

### 收工快照(10:30)
- 后端状态:5 项全绿
- 工作区:clean(没改任何代码)
- main / ww:`400508a` / `400508a`(跨日无变化)

---

## §26 待续

如果今日继续调试/开发,在这里按 `## §N` 格式追加。

---

## §27 11:00 — 悟书环书籍图像 6-cycle 重映射

**用户指令**:悟书环页面,6 卷书的图像与卷名不对应,按用户指定的 6-cycle 循环移位调整。

### 修改详情

**唯一改动文件**:`android/app/src/main/java/com/jueqiao/jianghu/ui/screens/wushuhuan/WushuhuanScreen.kt`(line 73-84,`bookSlots` List)

6 个 `BookSlot.imageRes` 字段循环置换:
| 卷号 | 当前 | 目标 |
|---|---|---|
| 3 | `img_wushuhuan_book_03` | **`img_wushuhuan_book_10`** |
| 4 | `img_wushuhuan_book_04` | **`img_wushuhuan_book_09`** |
| 5 | `img_wushuhuan_book_05` | **`img_wushuhuan_book_04`** |
| 8 | `img_wushuhuan_book_08` | **`img_wushuhuan_book_03`** |
| 9 | `img_wushuhuan_book_09` | **`img_wushuhuan_book_08`** |
| 10 | `img_wushuhuan_book_10` | **`img_wushuhuan_book_05`** |

跟随链(闭合 6-cycle):`3 → 10 → 5 → 4 → 9 → 8 → 3`

其他 4 卷(1, 2, 6, 7)的 imageRes 不变。
其他字段(x, y, width, height, rotation)全部不变。
所有 `BookSlot` data class、点击跳转、卷名解析都不改。

### 不动的东西
- 10 个 PNG 文件本身(`res/drawable/img_wushuhuan_book_01.png` ~ `book_10.png`)—— 只是改了代码里的引用
- `BookSlot` data class 定义
- 卷名来源:`displayVolumeTitle()` 从 `LearningBookDto.volumeTitle` 解析(后端 API 动态)
- 跳转逻辑:`onOpenReader(book.volumeNo, book.manualPageId, false)`(line 392)

### 笔误修正
用户原话"卷十←卷五《赏罚驭灵诀》"是笔误,确认后改为"卷五《百炼识物诀》"(因为卷五是《百炼识物诀》,卷八才是《赏罚驭灵诀》)。

### 验证
- ✅ 静态检查:`grep BookSlot(` 输出与映射表一致
- ✅ 编译:`compileDebugKotlin` BUILD SUCCESSFUL in 25s(只改常量,不影响类型)
- ⏳ 视觉验证:用户自行 installDebug + 截图确认

### 收工快照
- HEAD(ww):`<待回填>`
- working tree:本笔 commit 后 clean

---

## §28 11:30 — fast-forward main 到 ww(悟书环 fix 同步)

**用户指令**:"推到 main"

### 拓扑
- main HEAD:`519b60b docs(session-log): 09-28 §24`
- ww HEAD:`8ad2d8b fix(wushuhuan): 悟书环 6 卷书籍图像重映射`
- merge-base:`519b60b`(= main HEAD)
- 结论:可 `--ff-only`

### 同步的 1 笔
- `8ad2d8b` fix(wushuhuan): 悟书环 6 卷书籍图像重映射(6-cycle)

### 操作
```bash
git checkout main
git merge --ff-only ww
git push origin main
```

### 收工快照(预计)
- HEAD(main):`8ad2d8b`
- HEAD(ww):`8ad2d8b`
- origin/main:push 后同步
- working tree:clean

---

## §29 11:45 — 清理孤儿分支 origin/zzz + 本地 zzz

**用户指令**:"清理 origin/zzz"

### 验证 zzz 是孤儿
```
zzz HEAD:           63dd03d fix(gunlun1): 修炼按钮无条件跳转
merge-base zzz+main: 63dd03d  ← 完全相等
main..zzz:          空(zzz 无 main 没有的 commit)
zzz..main:          18 个 commit(main 已有 zzz 全部内容)
```

合并点:`6bca742 merge origin/feature/creation-contest-demo → ww`(09-28 已合并)
后续 `400508a`、`47eee1b`、`8ad2d8b` 等 commit 都在 zzz 之后。

**结论**:**删除安全**——zzz 没有 main 没有的独立工作。

### 操作
```bash
git push origin --delete zzz   # 删远程
git branch -D zzz               # 删本地(强制,-D 因为 zzz 是 orphan)
```

### 收工快照(预计)
- 本地分支:`main`、`ww`、`feature/authentication-foundation`(本地无)
- 远程分支:`main`、`ww`、`feature/authentication-foundation`、`feature/creation-contest-demo`(孤儿,待评估)

---

## §30 12:30 — 卷最后一页的滚轮跳转改为悟书环跳转

**用户指令**:"后山2~11页面的可以跳转的卷页面的最后一个页面的可以跳转的滚轮页可以删掉,然后将卷页面的最后一页可以跳转到wushu环"

### 用户意图澄清

通过 AskUserQuestion 确认:
1. 滚轮跳转按钮 → **改 onClick 目标为 wushu 环**(复用原按钮)
2. wushu 环入口 → 复用原按钮,**不**新增按钮

### 实施细节

**唯一修改文件**:`android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt`

9 个 Volume 最后一页的 `onOpenGunlun*N*` lambda 体改为 `navigateSingleTop(Routes.Wushuhuan)`:

| Vol | NavHost 行 | 参数 | 备注 |
|---|---|---|---|
| 1 | 1211 | onOpenGunlun6 | — |
| 2 | 1301 | onOpenGunlun10 | — |
| 4 | 1480 | onOpenGunlun8 | plan 写 1388 实际是 1480(脚本误判) |
| 5 | 1772 | onOpenGunlun12 | — |
| 6 | 1960 | onOpenGunlun14 | — |
| 7 | 2043 | onOpenGunlun16 | — |
| 8 | 1675 | onOpenGunlun11 | — |
| 9 | 1577 | onOpenGunlun9 | — |
| 10 | 1863 | onOpenGunlun13 | — |

### Vol 3 特殊情况

`Volume3Part14Screen` 在 NavHost 里是单行定义(`Volume3Part14Screen(onBack = { navController.popBackStack() })`),**没有 onOpenGunlun 参数**,原本就没有跳转按钮——无需改。

### 不动的东西

- 后山 2-11 各 Screen 的 `onOpenVolume*N*Part1` 字段全部保留
- Volume 屏幕本体(参数名 `onOpenGunlun6` 等保留)
- Gunlun 屏幕(`onOpenGunlun*N*` 在 gunlun 屏幕内部调用,**不**改)
- Wushuhuan 屏幕、Routes.kt

### 验证

- ✅ compileDebugKotlin:BUILD SUCCESSFUL in 29s
- ⏳ 视觉验证(用户自行):每卷最后一页点卷尾按钮 → 进悟书环

### 收工快照
- HEAD(ww):`<待回填>`
- working tree:本笔 commit 后 clean
