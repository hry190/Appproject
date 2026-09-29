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
