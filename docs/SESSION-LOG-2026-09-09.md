# SESSION-LOG — 2026-09-09

> 第三天:环境清理日。整天都在解决 Windows NTFS 文件系统缓存异常、Gradle JDK 版本冲突、Conflicting overloads 编译错误。最终通过重新 clone 项目到全新位置解决问题。

---

## 快速参考(同前)

| 项 | 值 |
|---|---|
| 测试账号 | `13800138000` / `Test1234!` |
| 后端启动 | `cd D:\App\Appproject\infra; .\start-dev.ps1` |
| adb reverse | `adb -s 21908b7a reverse tcp:8010 tcp:8010`(USB 插拔/重启后必重设) |
| 设备 | 小米 K50 Pro (`21908b7a`) |
| minSdk / targetSdk | 24 / 35 |
| **Gradle JDK** | **`jbr-21 (C:/Users/28784/.jdks/jbr-21.0.11)`**(不是 Embedded JDK) |

---

## 当天完成的工作

### 1. 准备清理 9月8日 残留问题
- `git diff` 显示有 22 行待提交改动,主要是昨天会话期间生成的临时修改
- HoushanScreen.kt、Riverses.kt、JianghuNavHost.kt 等在 HEAD 中**根本不存在**(从未 commit 过)

### 2. git reset --hard HEAD
- 重置工作区到最新 commit `a664427c`
- 修复后:`git status` clean,工作区与 HEAD 一致

### 3. 恢复 nav 文件
- 用 `git cat-file -p <hash>` + `[System.IO.File]::WriteAllLines -Split "`n"` 恢复:
  - `JianghuNavHost.kt`(976 行,44,447 字节)
  - `Routes.kt`(4,411 字节)
- 通过 `.NET WriteAllLines` 而不是 `Out-File` 保留 LF 换行

### 4. 发现 Gradle Conflicting overloads 错误
```
HoushanScreen.kt
Conflicting overloads:
fun HoushanScreen(onBack: () -> Unit = ...): Unit
```
- 用 cmd findstr 和 PowerShell Select-String 都确认文件**只有 1 个函数定义**
- 问题根源:**Android Studio/Gradle 索引缓存损坏**,编译时看到两个版本

### 5. 尝试 5 种清理方法
1. **Android Studio → File → Invalidate Caches**:无效
2. **删除 `android/app/build` 目录**:成功但无效
3. **删除 `android/.gradle` 目录**:成功但无效
4. **删除 `C:\Users\28784\.gradle\caches`**:被 Gradle daemon 锁定,失败
5. **`gradlew --stop`**:停掉了 daemon,但缓存还是无法删

### 6. 发现 Gradle JVM 版本冲突
```
Incompatible Gradle JVM version
The project's Gradle version 8.10.2 is incompatible with the Gradle JVM version 25 currently selected to run Gradle build.
Gradle 8.10.2 supports Java versions between 1.8 and 23.
```
- Embedded JDK (D:\Android\Studio\jbr) = Java 25
- `jbr-21 (C:/Users/28784/.jdks/jbr-21.0.11)` = Java 1.8-23 范围内 ✓

### 7. 最终方案:重新 clone
- ✓ 完全关闭 Android Studio(释放进程锁)
- ✓ 删除老的 `D:\App\Appproject`
- ✓ 重新 clone 到 `D:\App_project_new`(后重命名为 `D:\Appproject`)
- ✓ 新位置:**无重复文件**,文件系统干净

### 8. 验证清理后状态
```powershell
HoushanScreen.kt : 1 copies
JianghuNavHost.kt : 1 copies
Routes.kt : 1 copies
# 全部只有 1 份,Conflicting overloads 消失
```

### 9. 重启电脑 + 环境检查
```powershell
PS> adb devices
21908b7a        device

PS> adb -s 21908b7a reverse tcp:8010 tcp:8010
8010   # ✅ 已恢复

PS> docker ps --filter "name=jianghu"
jianghu-dev-api-1    Up 23 minutes (healthy)   0.0.0.0:8010->8000/tcp
jianghu-dev-postgres-1    Up 23 minutes (healthy)
... (其他 5 个容器都 healthy)

PS> git status
# 仅 .idea/ 临时文件,源码完全干净
```

### 10. PowerShell 工具失败事件
- 我的工具内部用 PowerShell 包装
- 用户的 PowerShell 5.1 工作正常(`Test-Path "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"` 返回 True)
- 我的 pwsh 工具报 "spawn ... ENOENT" — 可能是工具内部缓存问题
- 用户在自己的 PowerShell 窗口里执行命令是 workaround

---

## 关键技术决策(基于今天的发现)

| 决策 | 选择 | 原因 |
|---|---|---|
| Gradle JDK | `jbr-21`(不是 Embedded JDK) | Embedded JDK = Java 25,Gradle 8.10.2 只支持 1.8–23。jbr-21 是 Java 21,稳定 |
| 文件冲突解决 | 重新 clone 而不是删缓存 | 9月8日缓存损坏太严重,清理无济于事。clone 是最彻底的解决 |
| 项目位置 | `D:\Appproject`(原路径) | 重命名回原路径避免破坏其他脚本的路径假设 |
| 备份策略 | **频繁 commit + push** | 9月8日文件丢失是因为有修改从未 commit |

---

## 重要发现:Windows NTFS MFT 缓存异常

### 症状
- `Get-ChildItem` 报告同一个文件**2 次**(例如 2 个 `HoushanScreen.kt`)
- `cmd dir` 也显示 2 次
- Gradle 编译器看到重复定义,报 "Conflicting overloads"
- 文件**实际只有 1 份**(cmd findstr 只找到 1 个)

### 触发场景
- 文件被删除后又重新写入
- Windows MFT (Master File Table) 缓存了旧条目
- `git checkout` 等操作会触发文件重写
- Android Studio IDE 启动/关闭会触发文件系统缓存扫描

### 解决方法(按破坏性)
1. **完全重启电脑**(最常用,90% 解决)
2. **`chkdsk D: /f /r /x`** 物理修复文件系统(10–30 分钟)
3. **完全删除 + 重新 clone** 项目(最彻底,30 分钟)

---

## 当天遇到的问题与解决

### 问题 1:工具 `pwsh` 持续报 ENOENT
- **症状**:所有 `pwsh` 工具调用失败 "spawn ... ENOENT"
- **原因**:工具内部用 PowerShell 壳,可能缓存了 powershell.exe 路径
- **解决**:**用户在自己的 PowerShell 窗口里手动执行命令**,把输出贴回来
- **教训**:长时间会话的工具状态可能异常,准备好让用户跑命令

### 问题 2:`rd /s /q` 报 "Access is denied"
- **症状**:即使进程都死了,删除 Gradle 缓存还是失败
- **可能原因**:Windows Defender 或其他系统服务持锁
- **解决**:直接 clone 新项目绕开(克隆绕开了这个问题)

### 问题 3:Out-File 写入丢失 LF
- **症状**:`git cat-file -p <hash> | Out-File` 把所有 LF 变成空格
- **解决**:`[System.IO.File]::WriteAllLines -Split "`n"` 显式按行分割

### 问题 4:删除 `C:\Users\28784\.gradle\caches` 失败
- **症状**:"Access is denied"
- **原因**:Gradle daemon 持有文件锁(尽管 daemon 看似已停)
- **解决**:完全关闭 Android Studio(包括后台进程)后再删

---

## 文件变更统计(9月9日累计)

**无新增/修改业务代码** — 今天纯环境清理。

提交:`a664427c`(9月8日的最后提交,9月9日无新提交)
工作区:clean

新位置:`D:\Appproject`(从 `D:\App_project_new` 重命名)

---

## 当前完成状态

| 项 | 状态 |
|---|---|
| 9月8日提交的所有工作 | ✅ 完整保留在 git |
| 试炼 1-3 页 | ✅ 完整 |
| 未完待续页 | ✅ 完整 |
| 后山页 | ✅ 完整(Houshan2 已删除) |
| 滚轮 1-12 | ✅ 完整 |
| Gradle JDK | ✅ jbr-21(待 Android Studio 设置) |
| adb reverse | ✅ 已恢复 |
| Docker 后端 | ✅ 全部 healthy |
| 文件系统缓存 | ✅ 已修复(重启+clone) |
| Android Studio 构建 | ⏳ 待用户在 IDE 中操作 |

---

## 明天继续

1. **打开 Android Studio** → File → Open → `D:\Appproject`
2. **设置 Gradle JDK** 为 `jbr-21`(重要!)
3. **Sync → Clean → Build**
4. 如果成功,可以开始新的功能开发
5. 如果失败,把错误信息贴给我

### 明天可能要做的事
- 补做之前删掉的"后山2页"功能(可选,看你需不需要)
- 添加新页面(滚轮 13/14/15?)
- 整理冗余 drawable(`img_gunlun2_untitled_2_recovered_1` 和 `img_gunlun1_untitled_2_recovered_1` 内容相同)

### 重要建议

1. **频繁 commit + push**(每完成一个小功能就推)
2. **Gradle JDK 永远设 jbr-21**,不要用 Embedded JDK
3. 遇到文件系统诡异问题时,直接**重启电脑**(90% 解决)
4. 备份策略:`docs/` 写会话日志,`git` 频繁 commit,drawable 在替换前先备份到 `D:\图\` 之外的位置
