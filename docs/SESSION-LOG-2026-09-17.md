# SESSION-LOG-2026-09-17

> 昨日: [SESSION-LOG-2026-09-16.md](SESSION-LOG-2026-09-16.md)(3294 行,§1~§21s)
> 今日工作: **09-17 开始 — 工作区扫描 + SESSION-LOG 建档 + 后山 2 加回熊猫(反转 §18)**
> 状态: 09-16 已 push 到 `58ae39a`;09-17 开工时工作区只有 **1 M**(`Houshan2Screen.kt`,§22 改动,A 模式不 commit)

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | `zzz` |
| 最近 commit | `58ae39a` chore(infra): 删除静默失效的 adb-reverse.ps1 + ONBOARDING §3.4 补验证步骤(§21s) |
| 远端 HEAD | `58ae39a`(已同步) |
| Tracked 文件总数 | 1238 |
| 工作区未 commit | 1 M(`Houshan2Screen.kt`,§22 待 commit) |
| 09-16 SESSION-LOG | 3294 行(完整) |
| 09-17 SESSION-LOG | **本文件**(新建,刚开工) |

> ⚠️ 上表是 **09-17 开工时**的快照。后续工作会让"未 commit"那一行的内容变化,**别再当成"最新状态"使用**。
> 看真实状态:`git log --oneline -1` + `git status -sb`(本文档不追记 commit 号,避免又变成过期快照)。

---

## 🎯 今日 TL;DR(09-17)

**一句话**:上午把后山 1 的熊猫动画复制到后山 2(反转 §18)并修了 `adb reverse` 的坑;下午**新建后山 4 页 + 给后山 3 加 dolly 推进**;晚上查清"为什么还是要登录"其实是**网络断**(token 一直都在),**新建后山 5 页 + 后山 4 补 dolly**,最后修了 Docker Desktop 的 **compose 插件缺失 + 8010 端口保留冲突**。

### 做了什么

| 段 | 主题 | 结果 |
|---|---|---|
| **§22** | **后山 2 加回熊猫(反转 §18)** | 沿用后山 1 §21 同款动画;放景深平面 3(与 4 标签同层),推进时同速缩放 + 一起淡出 |
| §23 | ADB 重置 + USB reverse 映射修复 | 真机登录从"无法连接江湖驿站"恢复 |
| **§24** | **新建后山 4 页 + 后山 3 加 dolly 推进 + 标签文本改名** | 4 标签 callback 占位待配;dolly 参数复制自后山 2 §36;反转 §21o |
| **§24b** | **标签行间距收紧 + image 高度增加 + "炼"字号跨页对齐** | 修好"百炼识物诀"的"诀"被裁;Houshan1/2 的"炼"统一 6.sp |
| **§25** | **后山 1 标签"识机真决"去掉跳转** | 删 clickable(不是留空 callback);后山 2 同名标签**未动** |
| **§26** | **"为什么还是要登录"根因诊断** | **不是登录态丢失,是网络断**;token 一直在 prefs 里 |
| **§27** | **后山 1 改为"点击屏幕任意位置"跳后山 2** | 外层 Box 加整屏 clickable;气泡降级为纯视觉提示 |
| **§28** | **后山 2 标签"拆招心法"跳转第二卷-1** | 标签2 从"死区"变"活区";顺手清零 4 处 `§X` 占位 |
| **§29** | **后山 2 标签"万象谱"跳转第三卷-1** | 标签3 从"死区"变"活区";沿用 §28 模式,零摩擦复用 |
| **§30** | **后山 2 标签"寻径迷踪步"跳转第四卷-1** | 标签4 转活区 → **后山 2 的 4 个标签全部可点**;顺手删 `MutableInteractionSource` import |
| **§31** | **后山 3 三个标签配置跳转目标** | 拆招心法→卷2 / 万象谱→卷3 / 寻径迷踪步→卷4;沿用后山2 的"文字→卷"映射表 |
| **§32** | **后山 4 四个标签配置跳转目标** | 万象谱→卷3 / 寻径迷踪步→卷4 / 百炼识物诀→卷5 / 分门辨类掌→卷6;占位名 `onOpenTagN` 改语义名 → **后山 1/2/3/4 标签配置全部完工** |
| **§33** | **新建后山 5 页 + 后山 4 补 dolly** | 后山5 复用后山3 素材/动画(终点页);后山4 从"终点"改造成"过场"(dolly → 后山5)→ **推进链变成 1→2→3→4→5** |
| **§34** | **Docker Desktop:compose 插件缺失 + 8010 端口保留冲突** | `cliPluginsExtraDirs` 持久修复;8010 靠重启脱离保留段;顺带修真 `adb reverse` |

### 只读这三条也够

1. **§26 最有价值:误导性 UI 比崩溃更糟** —— "网络不通"和"未登录"在 app 里**无法区分**,都跳登录页。
   - **根因**:`restoreSession()` 只在 **401** 时清 token;**网络异常不清**,但也返回 `false` → 跳登录页
   - **决定性证据**:同一枚 token,只切网络 → 步骤 4(断网)跳登录页 / 步骤 5(通网)进主页
   - **短期规避**:`adb reverse --list` 确认非空 → **杀 app 重开**(⚠️ **不用真的重新登录**)
   - **长期建议**:A 网络异常留在 Splash 给重试(3~5 行)/ B 自动重试 / C 三态返回
2. **§18 的"后山 2 不要熊猫"决定被反转**(`Houshan2Screen.kt`):理由是用户重新要求保留。
   - 熊猫位置 **X=184, Y=621, W=210, H=192** —— 与后山 1 完全一致
   - 动画参数 **Scale 0.95~1.05 / 3s + Y ±10dp / 4s, RepeatMode.Reverse** —— 与后山 1 §21 同款
   - **景深平面选择**:放在 plane 3(标签同层)而非 plane 2(云雾同层) —— 推进时与 4 标签一起 ×1.34 缩放 + 比云雾更早淡出
   - **Rectangle156 气泡仍不复制**:§18 "过场页不应有信息气泡"的判断仍然成立
3. **USB reverse 映射 = 真坑,踩过两次**(§23 + §26):任何一次 USB 松脱 / `adb kill-server` / ADB 重启都会清空 `adb reverse` 列表。
   - 报错信息:"**暂时无法连接江湖驿站**" / "**登录页突然出现**" —— 两者根源相同
   - **一行修复**:`adb reverse tcp:8010 tcp:8010`
   - **仍未根治**:每次 USB 抖动都要手动 reverse;候选方案见 §23

### 状态

- 今日 **0 个 commit**(一切还在 A 模式等你说"commit")
- 本地与 `origin/zzz` 一致(`58ae39a`)
- ADB 已恢复 + `adb reverse` 已重建;真机**已登录并停在主页**
- **工作区(截至 §26)**:5 M + 1 新目录

  | 文件 | 归属 |
  |---|---|
  | `nav/JianghuNavHost.kt` | §24 后山4 路由 + §25 移除 onOpenVolume1 |
  | `nav/Routes.kt` | §24 `Shilian4` |
  | `ui/screens/houshan1/Houshan1Screen.kt` | §24b 炼字号/位置 + §25 标签1 去跳转 |
  | `ui/screens/houshan2/Houshan2Screen.kt` | §22 熊猫 + §24b 炼字号 4→6 |
  | `ui/screens/houshan3/Houshan3Screen.kt` | §24 dolly 重构 |
  | `ui/screens/houshan4/`(新)| §24 新页面 |

---

## §22 后山 2 加回熊猫(反转 §18)(2026-09-17 上午)— A 模式不 commit

**用户指令**:"把后山 1 页面的熊猫动画复制到后山 2 页面"

### 为什么这次改动"有故事"

§18(2026-09-15)你**明确指令**:"重写后山 2 时**去掉熊猫和气泡**",理由是后山 2 只是过场页(整屏点击 → 推进 → 后山 3 出现)。

`Houshan2Screen.kt` 开工前的现状:

| 项 | 后山 1 | 后山 2(开工前)| 后山 3 |
|---|---|---|---|
| 熊猫图片 `img_shilian_panda` | ✅ 有动画 | ❌ **无**(§18 删) | ✅ 有动画 |
| 矩形气泡 `Rectangle156` | ✅ 有 | ❌ 无(§18 删) | ❌ 无 |

`Houshan2Screen.kt:106` 开工前的注释:**"熊猫已按 §18 去掉;§21 起 6 朵老云也去掉动画,改为静态图层"**。

### 我准备这样改(动手前先对齐)

| 项 | 决定 | 理由 |
|---|---|---|
| **位置** | X=184, Y=621, W=210, H=192 | 与后山 1 完全相同 |
| **动画参数** | Scale 0.95~1.05 / 3s,Y ±10dp / 4s,Reverse | 与后山 1 §21 同款 |
| **景深平面(后山 2 特有)** | 放在 **景深平面 3**(与 4 个标签同层) | 熊猫是前景角色,与标签同速推进、一起淡出最自然;放在平面 2 会和云雾一起 ×1.09 但只有云雾是"相对后移"的语义,语义不对;放在平面外最差(不动不淡,推进时显得"贴屏") |
| **z-序** | 平面 3 内,4 个标签**之前**绘制 | 与后山 1 一致(熊猫在标签之下,标签永远在熊猫之上) |
| **不复制气泡** | Rectangle156 + "御剑穿行..." 文字**不加** | §18 删的理由("过场页不要信息气泡")仍然成立 |
| **导入** | 加 5 个 `animation.core.*` | 当前 Houshan2 没 import animateFloat / infiniteRepeatable / rememberInfiniteTransition / LinearEasing / RepeatMode |
| **注释** | ① 把"删除元素"里的熊猫那行去掉;② 改 line 106 的"已按 §18 去掉"注释;③ 顶部 KDoc 加一行"§X 重新加回熊猫,沿用 §21 动画参数" |  |

### 一个我替你拍板的细节(可推翻)

后山 1 的熊猫**完全静止在 Y=621**(0.95~1.05 缩放 + ±10 上下浮),**没有景深概念**。

后山 2 的熊猫会被 `DOLLY_LABEL_SCALE = 0.34`(×1.34)+ `labelFade`(1.4× 比云雾更快淡出)作用。

→ 推进时的视觉:**熊猫会跟着放大、并比云雾先消失**。这是后山 2 过场动画的"应有的层次感"——前景角色在镜头拉近时退场。

如果你不希望这样,告诉我,我把熊猫移出 plane 3(→ 整屏外的 Box,推进时**完全不动**)。

### 实际改动(5 处)

```diff
 import androidx.compose.animation.core.Animatable
 import androidx.compose.animation.core.FastOutSlowInEasing
+import androidx.compose.animation.core.LinearEasing
+import androidx.compose.animation.core.RepeatMode
+import androidx.compose.animation.core.animateFloat
+import androidx.compose.animation.core.infiniteRepeatable
+import androidx.compose.animation.core.rememberInfiniteTransition
 import androidx.compose.animation.core.tween

-// 熊猫已按 §18 去掉;§21 起 6 朵老云也去掉动画,改为静态图层
+// 2026-09-17 §22:按用户指令反转 §18,重新加回熊猫 (img_shilian_panda),沿用 §21 的
+// "上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s" 动画参数;放在景深平面 3(与 4 个标签同层),
+// 推进时与标签同速缩放 ×1.34 + 一起淡出,语义上"前景角色随镜头前移后退出画面"。
+// Rectangle156 气泡仍不复制 —— §18 决定保留,理由:后山 2 是过场页,不应有信息气泡。
+// §21 起 6 朵老云本想去动画,§21f 按用户指令"不考虑间距了"又重新加回动画。

 val o60bProgress = rememberCloudProgress(10_300, "old60b")

+// ── §22 熊猫动画(沿用后山1 §21:Scale 0.95~1.05 / 3s, Y ±10 dp / 4s, RepeatMode.Reverse)──
+// 后山2 是过场页(整屏点击 dolly-in → 后山3),用户要求保留熊猫,放在景深平面 3(与4 个标签同层)
+// —— 推进时与标签同速缩放 (×1.34) + 同速淡出,语义上"前景角色随镜头前移后退出画面",最自然
+val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
+val pandaScale by pandaTransition.animateFloat(
+    initialValue = 0.95f,
+    targetValue = 1.05f,
+    animationSpec = infiniteRepeatable(
+        animation = tween(durationMillis = 3000, easing = LinearEasing),
+        repeatMode = RepeatMode.Reverse,
+    ),
+    label = "pandaScale",
+)
+val pandaDy by pandaTransition.animateFloat(
+    initialValue = -10f,
+    targetValue = 10f,
+    animationSpec = infiniteRepeatable(
+        animation = tween(durationMillis = 4000, easing = LinearEasing),
+        repeatMode = RepeatMode.Reverse,
+    ),
+    label = "pandaDy",
+)
```

```diff
 // 景深平面 3:标签(与山体同速推进 + 淡出 → 不相对滑动)
 Box(
     modifier = ...
     graphicsLayer { ... labelScale + labelFade ... },
 ) {
+    // 熊猫图像 (img_shilian_panda, X=184, Y=621, W=210, H=192) — 上下浮 ±10 / 4s + 呼吸缩放 0.95~1.05 / 3s (§21,§22 复制到后山2)
+    // 放在景深平面 3 内 4 个标签之前 → 推进时与标签同速缩放 ×1.34 + 一起淡出
+    // (后山1 没有 dolly,所以原版没这层行为;后山2 是过场,推进中熊猫自然前移+退场,层次与标签一致)
+    Image(
+        painter = painterResource(R.drawable.img_shilian_panda),
+        contentDescription = "熊猫",
+        modifier = Modifier
+            .offset(x = 184.dp, y = (621f + pandaDy).dp)
+            .size(width = 210.dp, height = 192.dp)
+            .graphicsLayer(
+                scaleX = pandaScale,
+                scaleY = pandaScale,
+            ),
+        contentScale = ContentScale.FillBounds,
+    )

     // "标签1" 图像 (X=-13, Y=570, ...) — 点击跳转第一卷-1 (§22)
```

```diff
-* 删除元素:
- *   - 熊猫 (img_shilian_panda) — 用户指令 §18
- *   - Rectangle156.png 气泡 + 文字"御剑穿行云雾群山..." — 用户指令 §18
+* 删除元素(§18,部分保留):
+*   - Rectangle156.png 气泡 + 文字"御剑穿行云雾群山..." — §18 决定,不复制(后山 2 是过场页,不应有信息气泡)
+*
+* 2026-09-17 §22 反转 §18:重新加回熊猫 (img_shilian_panda),沿用 §21 的"上下浮 ±10/4s + 呼吸缩放 0.95~1.05/3s"动画。
+* 放在景深平面 3(与 4 个标签同层),推进时与标签同速缩放 ×1.34 + 一起淡出(详见函数体内 pandaTransition 处注释)。
```

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 20s
```

`compileDebugKotlin` 实际跑了(不是缓存命中),说明这次改动确实动了文件。

### 真机验收(待做)

- **未做** — 用户没指示 `assembleDebug` + `adb install`,§22 仍是 A 模式不 commit
- **预期要看的**:
  1. 后山 2 进入时,熊猫在 X=184, Y=621 处做"上下浮 + 呼吸缩放" —— 与后山 1 节拍应一致(同 Reverse + 同周期)
  2. 整屏点击进入 dolly-in 过渡时,熊猫**随 4 标签一起 ×1.34 缩放 + 比云雾更快淡出** —— 视觉上"前景角色前移后退场"
  3. 推进到后山 3 后,熊猫**不应再出现在后山 3**(这是另一张页面自己的 `pandaDy = ...,1.405 dp` 单独动画)
- **可能踩坑**:后山 2 是过场页,如果发现熊猫在推进过程中"太抢戏"挡住 4 标签点击,可以调:
  - 选 A:把 `DOLLY_LABEL_SCALE` 调小(0.34 → 0.20),所有标签 + 熊猫都推得少
  - 选 B:把熊猫从 plane 3 移到 plane 外,推进时**完全不动**(但推进结束时它会比 4 标签小,可能更突兀)

### git 状态(A 模式)

- `M Houshan2Screen.kt`(+5 import,顶部 KDoc 改,line 106 注释改,pandaTransition 块 22 行,panda Image 18 行,KDoc 各处)
- 等用户说"commit"再走 commit + push

### 沉淀(§22)

- **"反转旧决定"是大事,值得写专门一段** —— §18 删熊猫的理由(过场页)今天不成立了(用户重新要求),但**部分保留**(气泡),所以 KDoc 不能只改一处,要明确标"反转 §18:加回熊猫,但 Rectangle156 仍不复制"。**这种"半反转"在 git log 里很难看出来,注释是唯一的留痕**
- **景深平面 = 复合动画的"次元"** —— 后山 2 的 panda 与后山 1 的 panda 行为**不一样**:推进时后山 2 的会缩放 + 淡出(plane 3 的副作用),后山 1 的不会(没有 dolly)。**同一段代码 + 不同父级 = 不同运行时行为**。如果将来后山 3 也想加 dolly,panda 会自动跟随 plane 3 / plane 2 的选择
- **"加 import"这件事没看 IDE 警告** —— Kotlin 编译器其实会建议"未使用 import 警告";但**反过来"使用了未导入的符号"是硬错误**(直接编译失败)。所以加 import 是**必然安全**的——如果旧代码引用了它而没 import,根本编不过
- **kdoc "§X" 占位是个小技巧** —— 我在写代码时还不知道今天的日志编号会是几(§22 还是 §23 起),先放 `§X`,等你建 SESSION-LOG 再回填。**避免我"提前决定编号"导致和最终日志对不上**

---

## §23 ADB 重置 + USB reverse 映射修复(2026-09-17 上午)— 不是代码改动

> 注:本节**不是代码工作**,但踩坑够深,**值得沉淀**,所以写进 SESSION-LOG 而不是只在 chat 里。

### 时间线

| 时间 | 事件 |
|---|---|
| 你: "重设 adb" | 我执行 `adb kill-server` + `adb start-server`,设备恢复 `device` 状态 |
| 你: "我刚刚 USB 松了" | 我**错**答"当前连接状态正常 ✅" —— 只看了 `adb devices`,没看屏幕 |
| 你: "你确定没问题吗,但是我还是需要登录账号" | 我才去看屏幕 —— app 在登录页(`com.jueqiao.jianghu/.MainActivity` 内的登录 Compose 屏) |
| 你: "帮我看看有没有可以使用的账号" | 找到 `13800138000` / `Test1234!`(API 实测可登,返回 200 + next_action=ENTER_APP) |
| 你: "登录账号时显示'暂时无法连接江湖驿站'" | 真坑暴露:**app 网络不通** |
| 排查 | `android/local.properties:11-12` 注释写着"真机经 adb reverse 走 127.0.0.1:8010 才能到后端" |
| 验证 | `adb reverse --list` 返回空 —— **映射丢了** |
| 修复 | `adb reverse tcp:8010 tcp:8010` 重建 → `adb shell curl http://127.0.0.1:8010/...` 收到 API 响应(网络通了) |

### 为什么 `adb reverse` 映射会丢

任何一次以下操作都会清空 `adb reverse` 列表:
- **USB 物理断开**(包括松脱)
- **`adb kill-server`**(这就是我前面"重设 adb"做的事 —— 顺手杀了 reverse)
- **ADB 服务重启**
- **设备重启**

而项目对 reverse 的依赖写在 `local.properties` 注释里,**没有任何自动化** —— 没 IDE hook,没 build 后脚本,没 USB 插上时的 daemon。

### 报错识别(以后秒判)

| 现象 | 层 | 修法 |
|---|---|---|
| `adb devices` 显示 `offline` | adb 链路 | `adb kill-server` + `adb start-server` |
| `adb devices` 显示 `unauthorized` | adb 授权 | 手机弹框点"允许 USB 调试" |
| `adb devices` 显示 `device` 但 app 报"无法连接 xxx" | app 网络 | `adb reverse --list` 检查,空了重建 |
| `adb reverse` 重建后 app 仍报网络错 | 端口 / 后端 | `adb shell curl 127.0.0.1:8010/v1/health` 验证后端是否在跑 |

**关键**:**`adb devices` 显示 `device` ≠ app 网络可达** —— 这是今天踩过的最痛一课。

### 仍未根治(候选方案)

| 方案 | 代价 | 评估 |
|---|---|---|
| **A. 加 IDE External Tool**:Build 完后自动 `adb -d reverse tcp:8010 tcp:8010` | 0 改动代码 | 最便宜,只在 IDE 用户受益 |
| **B. 改用 LAN IP**:`AUTH_BASE_URL=http://192.168.x.x:8010/` + `network_security_config` 例外 | 0 改动代码;但每次开发机 IP 变要改 | 不依赖 USB,稳;但要走明文 HTTP 局域网 |
| **C. 加 Makefile 目标 / PowerShell 脚本** `scripts/setup-reverse.ps1` | 1 个文件 | 每次手动跑一次 |
| **D. 接受现状**:反正 USB 一直插着,1 行命令记忆成本很低 | 0 | 不动;但今天又被坑一次 |

**当前状态**:**接受现状**(方案 D)。等下一次被坑再说。

### git 状态

- **无文件改动** —— §23 全是 adb 命令 + 调试,不涉及代码

### 沉淀(§23)

- **"我看到 X 没问题"是个危险句式** —— 当我说"设备在线,没问题"时,**只验证了 adb 链路层,没验证 app 网络层**。**两层独立**,adb 通了不代表 app 通了。**正确句式**:"adb 链路正常;但 app 网络是另一个层,需要单独验证"
- **诊断要从用户报错倒推** —— 用户的报错是"app 显示无法连接",**这是 app 网络层的现象**,不是 adb 层。**诊断方向要对齐报错层级**,否则会像我一样错答"没问题"
- **真机 + reverse 是 dev 环境的脆弱点** —— `local.properties` 注释是**有人已经踩过**的标记。读仓库时,**注释里"踩坑 / 注意 / 务必"字眼附近必读**,往往是血泪史
- **`adb reverse --list` 应该成为 dev 自检的标配**(像 `git status` 一样随手敲一次)—— 它**几乎零成本**,但能在 app 报网络错前就发现映射丢了

---

## §24 新建后山 4 页 + 后山 3 加 dolly 推进 + 标签文本改名(2026-09-17 下午)— A 模式不 commit

**用户指令**(连续 4 条):
1. "先取消后山3到'未完待续'页面的跳转方式,我要创建'后山4'页面,当点击除了标签以外的位置,可以跳转到后山4页面,后山4页面复用后山2页面的素材和动画"
2. "后山4页面的标签可以点击,但我还没有设置好可以跳转的页面"
3. "把后山2页面到后山3页面的那种山峰拉近的动画也要在后山3页面跳转到后山4页面的时候出现"
4. "将标签1的文本'识机真决'改成'万象谱',将标签2的文本'拆招心法'改成'寻径迷踪步',将标签3的文本'万象谱'改成'百炼识物诀',将标签4的文本'寻径迷踪步'改成'分门辨类掌',继承原本的字体字号和位置信息"

### 4 个文件的改动

| 文件 | 改动 |
|---|---|
| **`Houshan4Screen.kt`**(新建)| 复用后山 2 全部素材/动画;**去掉** dolly(终点页);整屏 clickable = noop;4 标签带命名 callback 占位 |
| **`Houshan3Screen.kt`**(重写 ~520 行)| 加 dolly 三景深平面;整屏 click 从"直接跳转"改为 `startDollyIn()`;**反转 §21o** 跳转目标 |
| **`Routes.kt`** | 加 `Shilian4 = "shilian4"` |
| **`JianghuNavHost.kt`** | 加 import;Shilian3 加 `exitTransition`;新增 Shilian4 composable + `enterTransition` |

### 关键设计:后山 4 复用后山 2,但**不复制 dolly**

| 复用(与后山 2 完全一致)| 改造 |
|---|---|
| 背景图 `img_shilian_bg.png` | ❌ **去掉 dolly 全套**(Animatable / scope / easing)|
| 云雾层 `HoushanMistLayer()`(默认变体,**非** Houshan3 变体)| ❌ 整屏 clickable = **noop**(终点页语义)|
| 6 朵 ACI 动画云(58/60/62 + 2 FCB + 57),周期 21.4/11/8/10/9/13 s | ❌ 去掉 `onOpenHoushan3` 回调 |
| 6 朵老云动画(58/61/56/57/60/60b)| ❌ **不加** Rectangle156 气泡(§18 决定保留)|
| 4 标签 + 熊猫(§22 同款动画)| ✅ 4 标签 clickable + **4 个命名 callback 占位** |

**为什么后山 4 不能有 dolly**:它是**新的终点页**。若照抄后山 2 的 dolly,整屏点击会 dolly 到后山 3 —— 形成循环倒退。

### 后山 4 的 4 个 callback 占位(用户指令 2 的落地)

```kotlin
fun Houshan4Screen(
    onBack: () -> Unit = {},
    onOpenTag1: () -> Unit = {},   // 万象谱 — TODO 用户配置跳转目标
    onOpenTag2: () -> Unit = {},   // 寻径迷踪步 — TODO
    onOpenTag3: () -> Unit = {},   // 百炼识物诀 — TODO
    onOpenTag4: () -> Unit = {},   // 分门辨类掌 — TODO
)
```

**设计意图**:用户"还没有设置好可以跳转的页面" → **4 个 callback 默认空函数**,`NavHost` 里传 `{}`。
以后填目标时**只改 `JianghuNavHost.kt` 一处**(把 `{}` 换成 `{ navController.navigate(Routes.XXX) }`),**不必碰 `Houshan4Screen.kt`**。

### 后山 3 的 dolly 重构(用户指令 3)

**参数直接复制后山 2 §36**(不抽共享常量 —— 只 2 个文件复用,抽出来反而扩散):

```kotlin
private const val DOLLY_DURATION_MS = 1050      // 总时长(0.8~1.2s 区间内)
private const val DOLLY_HANDOFF_MS = 560L       // 半程交控制权给导航
private const val DOLLY_BG_SCALE = 0.34f        // 山体 1.00 → 1.34(向用户靠近)
private const val DOLLY_CLOUD_SCALE = 0.09f     // 云雾 1.00 → 1.09(相对后移)
private const val DOLLY_LABEL_SCALE = 0.34f     // 标签与山体同速,避免相对滑动
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f
```

**结构重构**(这是本次 60% 的工作量):把原来"背景 / 雾+ACI+老云 / 熊猫+标签+返回按钮"的扁平结构,改成 3 个景深平面:

| 平面 | 内容 | 变换 |
|---|---|---|
| **1 背景** | `img_shilian2_bg` | `scaleX/Y = bgScale`,origin = focal |
| **2 云雾** | `HoushanMistLayer` + 5 ACI + 5 老云 | `scaleX/Y = cloudScale` + `alpha = cloudFade` |
| **3 标签** | 熊猫 + 3 标签 | `scaleX/Y = labelScale` + `alpha = labelFade` |
| **chrome** | 返回按钮 | 只 `alpha = chromeFade`(**不缩放**,否则 UI 会"跳动")|

**淡化速率递增**:`cloudFade = 1-p` < `labelFade = 1-1.4p` < `chromeFade = 1-1.8p`
→ 云雾最后消失,视线留给山体;UI chrome 最先消失。

**其他配套改动**:
- `BackHandler(enabled = true)` → **`enabled = !isTransitioning`**(过渡期间禁返回,避免半程被中断)
- 整屏 `.clickable(onClick = onOpenHoushan4)` → **`.clickable { startDollyIn() }`**
- 3 个标签的 `clickable {}` 保留(消费事件,阻止冒泡触发 dolly)

### NavHost 过渡接线

| composable | 改动 |
|---|---|
| `Shilian3` | 加 `exitTransition`:`targetState == Shilian4` → `fadeOut(520ms, LinearEasing)`;否则 300ms `FastOutSlowInEasing` |
| `Shilian4`(新)| `enterTransition` = `scaleIn(1.10→1.00, 760ms)` + `fadeIn(640ms, delay 120ms)` —— **与 Shilian3 同款**,读作"镜头减速停稳" |

**完整时间轴**(后山 3 → 后山 4):

| 时刻 | 事件 |
|---|---|
| 0ms | 点击空白 → `dolly.animateTo(1f)` 启动 |
| 0ms | 三平面开始缩放 / 淡出 |
| 560ms | `onOpenHoushan4()` → 导航切换;Shilian3 开始 520ms 淡出;Shilian4 开始 760ms scaleIn |
| 1050ms | dolly 完成 |
| 1080ms | exit fadeOut 完成 |
| 1320ms | enter scaleIn 完成 |

### 标签文本改名(用户指令 4)

| 标签 | 位置/字号/字体(不变)| 原文本 | 新文本 |
|---|---|---|---|
| 1 | X=-13 Y=570 / 14sp / Black / YaHei | `识\n机\n真\n决` | **`万\n象\n谱`** |
| 2 | X=168 Y=345 / 12sp | `拆\n招\n心\n法` | **`寻\n径\n迷\n踪\n步`** |
| 3 | X=113 Y=322 / 10sp | `万\n象\n谱` | **`百\n炼\n识\n物\n诀`** |
| 4 | X=151 Y=248 / 4sp | `寻\n径\n迷\n踪\n步` | **`分\n门\n辨\n类\n掌`** |

> `炼` 文字**未变**(字号 / 颜色 / 位置保持)。

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 19s
```

### 沉淀(§24)

- **"复用素材"≠"复用行为"**(新)—— 用户说"后山 4 复用后山 2 的素材和动画",但后山 2 的整屏点击是 **dolly 推进到下一页**;若照抄,后山 4 点一下会"倒退"回后山 3。**要区分"资产层复用"(图 / 动画参数)和"行为层复用"(导航语义)** —— 前者照抄,后者必须按新页面的角色(终点页)重新设计
- **callback 占位是"用户还没想好"的最优解**(新)—— 用户明说"还没设置好可以跳转的页面"。若我把 4 个 callback 写死在 `Houshan4Screen` 里,以后每加一个目标都要改 2 个文件;**改成 4 个命名参数 + 默认空函数后,以后只改 NavHost 一处**
- **dolly 参数重复而不抽常量,是有意的**(新)—— 只有 2 个文件用,**抽常量要新开文件 + 加 import + 三处引用**,抽象成本 > 重复成本(2×7 行)。**"重复 3 次再抽象"这条经验法则在这里成立**
- **景深平面决定"复用时会不会带副作用"**(新)—— §22 把熊猫放进后山 2 的 plane 3,于是它自动获得"推进时缩放 + 淡出";若放 plane 2,就会跟着云雾走。**同一个子组件,父级不同 → 运行时行为不同**
- **过渡期间禁返回是必要而非可选**(新)—— `BackHandler(enabled = !isTransitioning)`。若不这样,用户在半程按返回会露出"推了一半"的画面,比不禁用更糟

---

## §24b 标签行间距收紧 + image 高度增加 + "炼"字号跨页对齐(2026-09-17 下午)— A 模式不 commit

**用户指令**(2 条):
1. "在后山4页面中,将四个标签的文本的上下行间距缩小,还可以让四个标签的图像的高度增加以容纳文本,让文本完整显示出来"
2. "在后山4页面,文本'万象谱'的行间距可以增大一些"

### 问题(用户截图证明)

**后山 4 标签3"百炼识物诀"的"诀"字被裁掉**。

**根因**:
- Text 的 `size(width = 12.dp, height = 60.dp)` 按 3 行分配
- 但 Compose 中文默认行高 ≈ `fontSize × 1.5` = 10sp × 1.5 = **15dp/行** × 5 行 = **75dp** > 60dp
- → 最后一行溢出被裁

**标签4 更严重**:Text `size(height = 60.dp)` **本身就大于父 Box 的 `H=53.5dp`**(结构上就溢出)。

### 修法:双管齐下(用户提的两条都做)

**① 收紧行间距** —— 4 个 Text 的 `style` 加 `lineHeight = fontSize`(一字一行紧排):

| 标签 | fontSize | lineHeight 旧(默认) | lineHeight 新 |
|---|---|---|---|
| 1 | 14.sp | ~21sp | **14.sp** |
| 2 | 12.sp | ~18sp | **12.sp** |
| 3 | 10.sp | ~15sp | **10.sp** |
| 4 | 4.sp | ~6sp | **4.sp** |

**② 增加 image 高度** —— 4 个父 Box 的 H:

| 标签 | 旧 H | 新 H | Δ |
|---|---|---|---|
| 1 | 188 | **210** | +22 |
| 2 | 131 | **150** | +19 |
| 3 | 88 | **105** | +17 |
| 4 | 53.5 | **70** | +16.5 |

**③ 标签1"万象谱"单独放大行间距**(用户指令 2):`lineHeight 14.sp → 18.sp`(每行 +4sp,3 行共 +12sp)

### 视觉副作用(已知并接受)

`Image(fillMaxSize, ContentScale.FillBounds)` 会把 image **纵向拉伸** → 4 个标签看起来比后山 1/2 略瘦长,"炼"徽章略被拉高。

### "炼"字号跨页不一致的修复(顺带)

用户随后在外面自己改了 `Houshan1` 标签3 的"炼":`字号 4 → 6`、位置 `(22,12) → (23,14)`,但**只改了 Houshan1**。

我核对注释时发现:
1. **`Houshan1Screen.kt:53`(KDoc)+ `:433`(inline)注释仍写旧值** `X=22, Y=12, 字号 4` → **已修正**为 `X=23, Y=14, 字号 6`
2. **Houshan1 `6.sp` vs Houshan2 `4.sp` 跨页不一致** → 用户选 **A(两页对齐)**,于是把 `Houshan2` 也改成 `6.sp`

**§21"三页效果统一"原则恢复**:两页的标签3"炼"现在完全一致(X=23 / Y=14 / 6.sp / 10×14 / #385816)。

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 11s / 10s / 9s(多次)
```

### 沉淀(§24b)

- **中文竖排文本的"行高陷阱"**(新)—— Compose 默认 `lineHeight ≈ fontSize × 1.5`,而竖排文字(用 `\n` 分隔)的可用高度往往按"`fontSize × 行数`"估的 → **5 行 10sp 需要 75dp,不是 50dp**。**给竖排中文定 `size` 前,先算 `fontSize × 1.5 × 行数`**
- **"加高度"和"压行距"要一起做,不能只做一个**(新)—— 只压行距 → 字挤在一起;只加高度 → 木牌被拉得老长。**两个旋钮同时调,才能在"字完整"和"比例正常"之间取平衡**
- **用户自己改过的值,注释最容易漂移**(新)—— 用户在外面把 `4.sp→6.sp`、`(22,12)→(23,14)`,但**注释不会跟着变**。**用户说"我改了数据,查一下注释"时,优先查 KDoc + inline 两处**(这次两处都漂了)
- **"跨页不一致"要主动上报而不是自己决定**(新)—— 我发现 Houshan1/2 的"炼"字号不同,但**没有直接改**,而是给出 A/B/C 三个选项让用户拍板。**因为那是设计决策,不是 bug 修复**
- **`§24b` 这种子编号是有效的**(新)—— 行间距调整是 §24 的**后续微调**,单独编号会打乱"§24 = 后山4 主体"的语义。**用 `b` 后缀既保持主线清晰,又能在代码注释里精确定位**

---

## §25 后山 1 标签"识机真决"去掉跳转(2026-09-17 下午)— A 模式不 commit

**用户指令**:"在'后山1'页面,点击标签'识机真决'改成无法跳转"

**这是 §22 的又一次反转**:§22 时该标签跳转第一卷-1,现在要求"无法跳转"。

> ⚠️ **本节被 §27 部分覆盖 —— 读之前先看这条**
> §27(同日晚些)用户要求"跳转到后山2 的方式改成点击屏幕任意位置" → 外层 Box 加了**整屏 clickable**。
> 因此:**标签1 自身确实"无 clickable"(本节结论仍成立,不再跳第一卷-1),
> 但它的区域会被整屏 clickable 接管 → 点击标签1 实际跳的是"后山2"**。
> 完整行为对照见 [§27](#27-后山-1-改为点击屏幕任意位置跳后山-2)。

### 改动(2 个文件)

| 文件 | 改动 |
|---|---|
| **`Houshan1Screen.kt`** | ① 函数签名**删除** `onOpenVolume1` 参数;② 标签1 的 Box **删除** `.clickable(onClick = onOpenVolume1)`;③ KDoc 顶部加 §25 说明 |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian)` 里**删除** `onOpenVolume1 = { navController.navigate(Routes.Volume1) }` |

### 关键决定:删 clickable 而不是留空 callback

**Houshan3/4 的标签用 `.clickable {}`(空 callback)消费事件**,是为了**阻止冒泡到外层整屏 clickable**。
**Houshan1 外层没有整屏 clickable** → 不需要消费事件 → **直接删掉 clickable 更干净**:

| 方案 | 结果 |
|---|---|
| 留 `.clickable {}`(空)| 点击有 ripple 但无响应 → **看起来像坏了** |
| **删 clickable**(本次采用)| 点击完全无响应、无 ripple → **与同页标签 2/3/4 行为一致** |

### 未受影响(特意没动)

| 位置 | 状态 |
|---|---|
| **Houshan2** 的"识机真决"标签 | ✅ **仍跳转**第一卷-1(`JianghuNavHost.kt:577` 传参未动)|
| Houshan1 的气泡"御剑穿行..." | ✅ 仍跳后山 2 |
| Houshan1 返回按钮 | ✅ 仍 `onBack` |

> ⚠️ 注意:**后山 1 和 后山 2 的"识机真决"现在行为不同了**(1 不跳 / 2 跳)。这是用户只要求改后山 1 的结果,已在 `Houshan1Screen.kt:68` 注释里留痕并写明恢复步骤。

### 留给以后的恢复路径(已写进代码注释)

1. `Houshan1Screen` 签名加回 `onOpenVolume1: () -> Unit = {}`
2. 标签1 Box 的 modifier 加回 `.clickable(onClick = onOpenVolume1)`
3. `JianghuNavHost` 的 `composable(Routes.Shilian)` 里传回 `onOpenVolume1 = { navController.navigate(Routes.Volume1) }`

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 30s
```

### 沉淀(§25)

- **"删 clickable" vs "留空 callback",取决于有没有外层 clickable**(新)—— 有外层 → 必须留(消费事件防冒泡);无外层 → 删掉更干净(避免"有 ripple 但无响应"的假交互)。**同一个"不可跳转"的需求,在两种结构下正确实现不同**
- **改动要写"恢复步骤"注释**(新)—— 符号删除后,`git log` 只能看到"这行没了",看不出"原来是什么"。**在删除处留下"若要恢复,需改哪 3 处",比只写"§25 删除"有用得多**
- **用户只点名改一个页面时,不要顺手改"看起来一样"的另一个**(新)—— 后山 1/2 都有"识机真决"标签,但用户只说后山 1。**顺手改后山 2 会破坏用户的设计意图**(也可能他下一步正要单独处理)

---

## §26 "为什么我还是需要登录" —— 根因是网络断,不是登录态丢失(2026-09-17 晚)

**用户问题**:"为什么我还是需要登录"(在重复点了 3 次"重设 adb"之后,app 仍停在登录页)

### 现象与初判

| 观察 | 值 | 初判 |
|---|---|---|
| `auth_session.xml` | **存在且有内容**(refresh 密文 + IV)| **token 没丢!** |
| 该文件 mtime | 18:59 | 不久前写过 |
| app 进程 | PID 21817,已运行 52 分钟(≈19:01 启动)| 启动过,却停在登录页 |
| `adb devices` | `device` 就绪 | adb 链路正常 |
| 设备侧 `curl 127.0.0.1:8010` | 404(有响应)| 网络当时可达 |

→ **token 在,但 app 仍跳登录页** = `restoreSession()` 失败。

### 排查路上我自己犯的一个错

第一轮我测 "refresh 接口" 时用了 **`/v1/auth/refresh`** → 404,一度误判为**客户端 / 服务端路径不一致**。

| 端 | 实际路径 |
|---|---|
| 客户端 `AuthApi.kt:71` | `/v1/auth/token/refresh` |
| 服务端 `auth.py:89` | `@router.post("/token/refresh")`(router prefix = `/v1/auth`)|

**两者一致** —— 是我**自己把测试 URL 拼错了**。用正确路径重测 → `✅ refresh OK`、`✅ /v1/auth/me OK`。

### 决定性实验(5 步对照)

| 步骤 | 操作 | token 状态 | 结果 |
|---|---|---|---|
| 1 | 清空 token + 冷启动 | 无 | → 登录页 ✅ **正确** |
| 2 | 通过 UI 登录(`adb shell input`)| **保存** `Ztn98...` | 进入 app |
| 3 | 重启 app(**网络通**)| **轮换**为 `FJX6Bc...` | → **主页** ✅ **不用登录** |
| 4 | **移除 `adb reverse`** + 重启 | **未清**(仍是 `FJX6Bc...`)| → **登录页** ❌ |
| 5 | **恢复 `adb reverse`** + 重启 | **轮换**为 `APCF9...` | → **主页** ✅ **不用登录** |

**步骤 4 vs 5 是唯一变量对照**:同一枚 token,**只有网络通/断不同** → 结论无歧义。

### 根因

`AuthRepository.restoreSession()`(`AuthRepository.kt:23-33`):

```kotlin
val refreshToken = tokenStore.readRefreshToken() ?: return false   // 有 token,继续
return try {
    saveTokens(api.refresh(refreshToken))     // ← 网络断在这里抛异常
    _currentUser.value = api.currentUser(requireAccessToken())
    true
} catch (error: AuthApiException) {
    if (error.statusCode == 401) clearSession()   // ← 只有 401 才清 token
    false
}
```

**网络异常(IOException)不是 `AuthApiException`**,因此:

1. **token 不会被清** —— 文件原样保留 ✓ 已实测验证
2. 异常冒泡到 `AuthViewModel.bootstrap` 的 `catch (error: Exception)` → `onComplete(false)`
3. `false` → 导航到 `Routes.Login`(`JianghuNavHost.kt:365`)

**为什么今天反复触发**:

```
点"重设 adb" → adb kill-server → adb reverse 映射被清空(§23 的坑)
            → app 启动时连不上 127.0.0.1:8010
            → bootstrap 失败 → 显示登录页
            → 以为登录态丢了,其实 token 好好躺在 prefs 里
```

今天点了 **3 次**"重设 adb",每次都清 reverse。第 2/3 次我顺手重建了,但**在这期间启动 app 的那几次**仍会撞上。

### 解决办法

**短期(本次实际用的)**:

1. `adb reverse --list` 确认非空(空则 `adb reverse tcp:8010 tcp:8010`)
2. **杀掉 app 重开** —— ⚠️ **不需要真的重新登录**(token 还在)
3. 若仍不行,才走 UI 登录

**验证命令(3 行)**:

```powershell
adb reverse --list     # 应为 UsbFfs tcp:8010 tcp:8010
adb shell run-as com.jueqiao.jianghu cat /data/data/com.jueqiao.jianghu/shared_prefs/auth_session.xml
                       # 有 ciphertext = token 还在 → 不用重登
adb shell am force-stop com.jueqiao.jianghu    # 杀掉重开
```

**长期(这是真实产品缺陷,建议修)**:

| 方案 | 改动 | 效果 |
|---|---|---|
| **A. 最小** | `restoreSession()` 区分异常类型:网络异常时**留在 Splash** 并给"网络异常,点击重试" | 3~5 行 |
| **B. 中等** | `bootstrap` 自动重试(2~3 次,指数退避) | ~10 行 |
| **C. 完整** | 三态返回 `Authenticated / Unauthenticated / NetworkError`,Splash 按三态分支 | 改接口,~30 行 |

**为什么这是缺陷**:**"网络不通"和"未登录"在 UI 上无法区分** —— 用户会以为凭据失效,白输一次密码。

| 场景 | 应该 | 实际 |
|---|---|---|
| token 有效 + 网络通 | 进主页 | ✓ 进主页 |
| **token 有效 + 网络断** | **提示网络异常 / 留在 Splash** | ❌ **跳登录页,误导用户** |
| token 失效(401)| 跳登录页 | ✓ 跳登录页 |

### git 状态(§26)

- **无文件改动** —— §26 全是诊断 + 实测,不涉及代码

### 沉淀(§26)

- **"文件存在且有内容"是比"我以为"强一万倍的证据**(新)—— 我一开始也倾向"登录态丢了",但 `cat auth_session.xml` 直接显示密文还在。**诊断第一步永远是:找到承载状态的持久化介质,直接读它**,而不是从 UI 现象反推
- **唯一变量对照实验**(新)—— 步骤 4/5 只改网络一个变量、同一 token,结果从"登录页"变"主页"。**这种 A/B 对照比十段推理都有说服力**。以后遇到"到底是不是 X 导致的",就设计一个只改 X 的对照
- **"我测出来 404" ≠ "代码错了"**(新)—— 我把 `/v1/auth/token/refresh` 敲成 `/v1/auth/refresh`,得出"客户端服务端路径不一致"的错误结论。**负面测试结果要先自证测试本身**,尤其是手敲 URL / 手拼参数时
- **误导性 UI 比崩溃更糟**(新)—— 崩溃至少明确告诉你"出错了";**"网络断 → 跳登录页"让用户以为凭据失效**。产品设计上,**"失败原因不可区分"比"失败"本身更严重**
- **短期 workaround 必须写明"(不需要真的重新登录)"**(新)—— 若只写"重建 reverse",用户下次仍会以为要重登。**把这句话写进解决办法**,能省掉每次 30 秒的无效操作

---

## §27 后山 1 改为"点击屏幕任意位置"跳后山 2(2026-09-17 晚)— A 模式不 commit

**用户指令**:"后山1页面跳转到后山2页面的方式改成点击屏幕任意位置"

### 改动(1 个文件,4 处)

| 位置 | 改动 |
|---|---|
| **外层 Box**(`Houshan1Screen.kt:146`)| 加 `.clickable { onOpenHoushan2() }` —— **整屏任意位置**可点 |
| **气泡 Box** | **移除** `.clickable(onClick = onOpenHoushan2)` —— 现在只是视觉提示 |
| **KDoc 顶部** | 改为"**点击屏幕任意位置**跳转后山2(§27)" |
| **KDoc 气泡行** | 加"仅视觉提示(**无 clickable**)—— 点击职责已上移到整屏" |

**NatHost 无需改动** —— `onOpenHoushan2` 回调早已接好(`JianghuNavHost.kt:556`)。

### 点击行为对照

| 点击位置 | 之前 | 现在 |
|---|---|---|
| 气泡"御剑穿行..." | → 后山2 | → 后山2 |
| **4 个标签** | ❌ 无响应 | **→ 后山2** |
| **熊猫 / 云 / 背景空白** | ❌ 无响应 | **→ 后山2** |
| **左上角返回按钮** | → 修炼页 | → 修炼页(自带 clickable **消费事件**,不会误跳)|

### ⚠️ 与 §25 的语义交叠(已向用户确认,选 A)

用户的两次指令在"标签"上**结论相反**:

| 指令 | 用户原话 | 对标签的处理 |
|---|---|---|
| **§25**(今天下午)| "点击**标签**'识机真决'改成**无法跳转**" | 标签 = **不能跳** |
| **§27**(本次)| "跳转到后山2页面的方式改成**点击屏幕任意位置**" | 标签 = **也跳**(任意位置包含标签)|

**旁证:用户对后山 3 的措辞不同** —— "当点击**除了标签以外**的位置,可以跳转到后山4页面"(**明确排除标签**)。
→ 说明后山 1 说"任意位置"、后山 3 说"除了标签"是**刻意的差异**。

**给出 3 个选项让用户拍板,用户选 A**:

| 选项 | 结果 |
|---|---|
| **A ✅ 采用** | 保持现状 —— 后山1 点标签 → 跳后山2(与后山2/3 的"标签死区"不同,但符合"任意位置"字面)|
| B | 4 个标签改为 `.clickable {}` 死区 —— 与后山2/3 统一,但违背"任意位置" |
| C | 只把标签1 设死区 —— 满足 §25 字面,其余标签照跳 |

### 注释重构(用户:"为了方便可以把 §25 和 §27 修改一下,为了以后读起来比较清楚")

**第一版注释的问题**:在标签1 和气泡处各写一段"§25 说 X,但 §27 说 Y" —— 读者**要读两处 § 才能推出"点标签1 到底会怎样"**,而且两段措辞不一致(一处说"无响应",一处说"跳后山2")。

**重构原则**:**"当前行为"放最上面作为唯一权威,"演变史"另起一块只讲一次,其余位置只写一行指针。**

**新结构**(`Houshan1Screen.kt` KDoc 顶部):

```
【点击行为 · 当前】  ← 一张 4 行表,直接回答"点哪里会怎样"
  | 屏幕任意位置    | → 跳后山2 |
  | 4 个标签        | → 跳后山2 |
  | 气泡            | → 跳后山2 |
  | 返回按钮        | → 回修炼页 |
  ⚠️ 后山1 标签是"活区",后山2/3 是"死区" —— 用户有意为之,别"统一"掉

【点击交互演变史】  ← §22 → §25 → §27 三行,讲清"为什么长这样"
  ⇒ 净结果:标签1 自身无 clickable(§25 成立) 但整屏接管(§27) → 实际跳后山2
  ⇒ "无 clickable"与"会跳后山2"**同时为真,不矛盾**
```

**各处的 4 条 inline 注释全部改成一行指针**:

| 位置 | 新注释 |
|---|---|
| 布局清单 · 标签1 | `点击行为见顶部【点击行为】表 —— 自身无 clickable,由整屏接管(§25 + §27)` |
| 布局清单 · 气泡 | `**仅视觉提示,无 clickable**(§27)—— 点击职责已上移到整屏` |
| 外层 Box | `§27:整屏 clickable —— 点任意位置跳后山2(取代 §22 起"只有气泡可点")` |
| 标签1 Box | `§25:自身无 clickable...但 §27 起整屏接管本区域 → 点击它实际跳后山2` |
| 气泡 Box | `§27:clickable 已移除 —— 仅视觉提示(§22 起它曾是唯一可点区)` |

**函数签名处的"恢复步骤"也补了一条警告**:

```
⚠️ 只做 ② 是无效的:整屏 clickable 仍会接管,表现仍是跳后山2。
```

**日志侧**:§25 节顶部加了指向 §27 的警示块(读者看 §25 时立刻知道结论被 §27 部分覆盖)。

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 11s / 4s / 9s
```

### git 状态(§27)

- `M Houshan1Screen.kt`(与 §24b / §25 同一文件,累积待 commit)

### 沉淀(§27)

- **"任意位置"和"除了标签以外"是两种不同需求,不要互相套用**(新)—— 用户对后山 1 说"任意位置"、对后山 3 说"除了标签",**措辞差异即需求差异**。我**没有擅自把后山 1 也做成"标签死区"**(那样看起来更"一致"),而是**按字面实现 + 明确上报差异让用户拍板**
- **注释要写"实际可观察行为",不能只写"做了什么操作"**(新)—— §25 删除 clickable 是**真事**,但 §27 之后"该区域无响应"这个**结论就错了**。**只写"我删了 clickable"会误导读者**;必须补一句"现在由外层接管 → 实际行为是跳后山2"
- **"同一个文件被多个 § 反复改"时,注释要学会引用链**(新)—— `Houshan1Screen.kt` 今天被 §21(熊猫动画)、§24b(炼字号)、§25(去跳转)、§27(整屏跳转)改过 4 次。**在每个改动点标注它被哪些后续 § 覆盖/修正**,比每次重写整段注释更省且信息更全
- **给"看起来不一致"的设计留出确认环节**(新)—— 后山1 标签会跳、后山2/3 标签是死区,**读代码的人第一反应是"这是 bug 吧"**。**这种"有意的跨页不一致"必须在上报里显式说明**,否则以后会被当成缺陷"修掉"
- **"当前行为优先"的注释结构**(新)—— 当一个区域被 §22 → §25 → §27 **层层叠加修改**后,
  "每处各写一段历史"会让读者**必须心算三段才能得出当前行为**。正确结构:
  **①顶部一张"当前行为"表(唯一权威) → ②一块"演变史"讲清为什么 → ③各处只写一行指针**。
  **判据**:如果读者需要同时看两处以上才能回答"点这里会怎样",就该重构了
- **注释重构也要写进日志**(新)—— 这次改动**只动注释、零代码语义**,但它是用户明确要求的"为了以后读起来清楚"的**可交付物**。**只记录功能改动、不记录"可读性改动"**会让日志与代码的注释状态对不上(以后看到日志里的旧注释片段会以为代码没改)

---

## §28 后山 2 标签"拆招心法"跳转第二卷-1(2026-09-17 晚)— A 模式不 commit

**用户指令**:"点击'后山2'页面的标签'拆招心法'可以跳转到'第二卷-1'页面"

### 改动(2 个文件,3 处)

| 文件 | 改动 |
|---|---|
| **`Houshan2Screen.kt`** | ① 签名加 `onOpenVolume2Part1: () -> Unit = {}`;② 标签2 的 clickable 从"死区"改为**真实跳转**;③ KDoc 布局清单补每个标签的可点状态 |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian2)` 里加 `onOpenVolume2Part1 = { navController.navigate(Routes.Volume2Part1) }` |

### 核心改动:标签2 从"死区"变成"活区"

**§28 前**(只有消费事件的空 clickable):

```kotlin
.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§19)
)
```

**§28 后**(与标签1 写法完全对齐):

```kotlin
.clickable(enabled = !isTransitioning, onClick = onOpenVolume2Part1)
```

**为什么改成标签1 的写法**:
- `enabled = !isTransitioning` —— 防止 dolly 过渡动画途中误触跳转(标签1 早就是这个模式)
- 去掉 `indication = null` —— 死区不要 ripple(避免"看起来能点却不响应"),**活区应该有 ripple**(点击反馈)
- 去掉 `interactionSource` —— 那是为了配 `indication = null` 才需要;标签3/4 仍在用,故 import 保留

### 后山 2 的标签现在是什么状态

| 标签 | 文字 | 状态 | 目标 |
|---|---|---|---|
| **1** | 识机真决 | **活区** | 第一卷-1(§22)|
| **2** | 拆招心法 | **活区**(§28 新)| **第二卷-1** |
| 3 | 万象谱 | 死区 | 未配置 |
| 4 | 寻径迷踪步 | 死区 | 未配置 |

> ⚠️ 后山 2 的标签现在是**混合状态**(2 活 + 2 死)。这**不是缺陷**,而是"逐个配置跳转目标"的中间态 —— 已写进 KDoc 布局清单,避免以后被误判为 bug。

### 三页标签状态总览(截至 §28)

| 页面 | 整屏点击 | 标签行为 |
|---|---|---|
| **后山1** | → 后山2 | **全部活区** → 后山2(§27)|
| **后山2** | dolly → 后山3 | 标签1 → 第一卷-1;标签2 → **第二卷-1**;标签3/4 死区 |
| **后山3** | dolly → 后山4 | 全部死区 |
| **后山4** | noop(终点)| 4 个 clickable 但目标待配 |

### 顺手修:4 处 `§X` 占位符残留

核对注释时发现 `Houshan2Screen.kt` 里还有 **4 处 `§X`**(§22 写代码时日志编号未定,先放占位):

| 行 | 原文 | 改为 |
|---|---|---|
| KDoc | `2026-09-17 §X 反转 §18` | `§22 反转 §18` |
| 函数体 | `// 2026-09-17 §X:按用户指令反转 §18` | `§22:` |
| 函数体 | `// ── §X 熊猫动画` | `── §22 熊猫动画` |
| 单元格 | `上下浮 ±10/4s + 呼吸缩放 0.95~1.05/3s (§21,§X 复制到后山2)` | `(§21,§22 复制到后山2)` |

**全项目 `§X` 已清零**(grep 验证 0 匹配)。

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 9s / 3s
```

### git 状态(§28)

- `M Houshan2Screen.kt`(§22 + §24b + §28 累积)
- `M JianghuNavHost.kt`(§24 + §25 + §28 累积)

### 沉淀(§28)

- **"死区 → 活区"不只是加一个回调,还要改 clickable 的写法**(新)—— 死区用 `indication = null`(无 ripple)+ 空 onClick;活区要 `indication` 默认(有 ripple)+ `enabled = !isTransitioning`。**只把 `{}` 换成回调、保留 `indication = null`,会得到一个"能跳但点起来没反馈"的按钮**
- **"混合状态"必须写进注释,否则会被当成 bug**(新)—— 后山 2 的标签 2 活 2 死,任何一个新读者都会问"为什么不一样"。**在 KDoc 布局清单里逐个标签标注可点状态**,比只写一句"标签可点击"信息密度高得多
- **占位符 `§X` 是双刃剑**(新)—— 写代码时日志编号未定,用 `§X` 占位**避免瞎猜编号**(好);但**日志建好后必须回填**,否则注释永久漂移(坏)。**这次是自己踩的:§22 写完代码到补日志之间插了 §23~§27,`§X` 就留在原地了**
  - 规避法:**建 SESSION-LOG 时顺手 `grep "§X"` 全项目扫一遍**,清零再收工
- **`MutableInteractionSource` 的 import 要等所有使用点都改完才能删**(新)—— 标签2 改成简单 clickable 后,标签3/4 仍在用 `MutableInteractionSource`,**import 不能跟着删**。**删 import 前先 grep 全文件确认零引用**

---

## §29 后山 2 标签"万象谱"跳转第三卷-1(2026-09-17 晚)— A 模式不 commit

**用户指令**:"后山2页面的标签'万象谱'点击时可以跳转到'第三卷-1'页面"

### 改动(2 个文件,3 处)—— 完全沿用 §28 的模式

| 文件 | 改动 |
|---|---|
| **`Houshan2Screen.kt`** | ① 签名加 `onOpenVolume3Part1: () -> Unit = {}`;② 标签3 的 clickable 从"死区"改为**真实跳转**;③ KDoc 布局清单同步 |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian2)` 里加 `onOpenVolume3Part1 = { navController.navigate(Routes.Volume3Part1) }` |

**clickable 写法**(与 §28 标签2 一致,也与标签1 对齐):

```kotlin
.clickable(enabled = !isTransitioning, onClick = onOpenVolume3Part1)
```

### 后山 2 标签现状(3 活 + 1 死)

| 标签 | 文字 | 状态 | 目标 |
|---|---|---|---|
| 1 | 识机真决 | 活区 | 第一卷-1(§22)|
| 2 | 拆招心法 | 活区 | 第二卷-1(§28)|
| **3** | **万象谱** | **活区**(§29 新)| **第三卷-1** |
| 4 | 寻径迷踪步 | **死区** | 未配置 |

> 只剩标签4 待配。KDoc 布局清单逐个标签标注了状态,新读者不会疑惑"为什么 3 个能点 1 个不能"。

### "滚动式配置"的节奏观察

后山 2 的标签跳转目标**不是一次配齐,而是逐条下达**(§22 → §28 → §29),每次一句"点击 X 可跳转 Y":

| 段 | 配置的标签 | 目标 |
|---|---|---|
| §22 | 标签1 识机真决 | 第一卷-1 |
| §28 | 标签2 拆招心法 | 第二卷-1 |
| §29 | 标签3 万象谱 | **第三卷-1** |
| (待)| 标签4 寻径迷踪步 | ? |

→ **§28 沉淀里"混合状态必须写进注释"这条,在 §29 又用了一次**:每次改完都同步 KDoc 清单,读者任何时候看到的都是准确的"谁活谁死"。

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 7s
```

### git 状态(§29)

- `M Houshan2Screen.kt`(§22 + §24b + §28 + §29 累积)
- `M JianghuNavHost.kt`(§24 + §25 + §28 + §29 累积)

### 沉淀(§29)

- **重复到第 3 次时,模式已经稳定,可以直接套用而不必重新设计**(新)—— §28 定的"死区→活区"三件套(加参数 / 改 clickable 写法 / NavHost 接线 + KDoc 同步)在 §29 **零摩擦复用**。**这反过来说明 §28 当时把"为什么这样写"记进沉淀是对的** —— 如果不记,§29 我又得重新推一遍 `indication` / `enabled` 的取舍
- **"逐条配置"场景下,KDoc 清单比段落描述更抗漂移**(新)—— 如果 KDoc 只写"标签可点击跳转",配置到第 3 个时读者根本无法判断哪个标签是什么状态。**表格化(标签 × 状态 × 目标)在增量修改场景下维护成本最低** —— 每次只改一格
- **不必为"还剩 1 个未配"做特殊处理**(新)—— 剩下标签4 是死区是**正常中间态**,不需要临时提示、不需要 TODO 输出噪音。**KDoc 里写一句"未配置"就够**;等到用户下一条指令再改一格即可

---

## §30 后山 2 标签"寻径迷踪步"跳转第四卷-1(2026-09-17 晚)— A 模式不 commit

**用户指令**:"点击'后山2'页面的标签'寻径迷踪步'时可以跳转到'第四卷-1'页面"

### 改动(2 个文件,4 处)

| 文件 | 改动 |
|---|---|
| **`Houshan2Screen.kt`** | ① 签名加 `onOpenVolume4Part1`;② 标签4 的 clickable 从"死区"改为**真实跳转**;③ KDoc 布局清单 + 新增"4 标签全部可点"总结块;④ **删除 `MutableInteractionSource` import** |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian2)` 里加 `onOpenVolume4Part1 = { navController.navigate(Routes.Volume4Part1) }` |

### 后山 2 标签配置完成(4/4 全部活区)

| 标签 | 文字 | 目标 | 配置于 |
|---|---|---|---|
| 1 | 识机真决 | 第一卷-1 | §22 |
| 2 | 拆招心法 | 第二卷-1 | §28 |
| 3 | 万象谱 | 第三卷-1 | §29 |
| **4** | **寻径迷踪步** | **第四卷-1** | **§30** |

**4 个标签的 clickable 写法现在完全统一**:

```kotlin
.clickable(enabled = !isTransitioning, onClick = onOpenVolumeX)
```

### 顺手删 import:`MutableInteractionSource`

§28/§29 的沉淀里都提过"删 import 前先 grep 全文件确认零引用" —— **§30 正好用上**:

| 步骤 | 结果 |
|---|---|
| 改完标签4 后 grep `MutableInteractionSource` | 只剩 **import 行 + 一处注释**,**零实际使用** |
| 删除 import | ✅ 保留注释里的说明(解释"为什么删除")|
| 编译验证 | ✅ `BUILD SUCCESSFUL`(若漏删,会有 unused import 警告;若误删,会编译失败)|

> 为什么 §28/§29 时**不能**删:标签3/4 当时还是"死区",仍在用 `MutableInteractionSource` + `indication = null` 组合。
> **只有最后一个死区消失,这个 import 才真正变成死代码。**

### 一个观察:四次重复让"模式"沉淀成"模板"

| 段 | 死区→活区 的第 N 次 | 摩擦 |
|---|---|---|
| §28 | 第 1 次 | 需要重新推导 `indication` / `enabled` 的取舍 |
| §29 | 第 2 次 | 直接套用,但仍需核对 KDoc |
| **§30** | **第 3 次** | **零摩擦** —— 4 处改动全在预期内,只多了一步"删 import" |

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 6s
```

### git 状态(§30)

- `M Houshan2Screen.kt`(§22 + §24b + §28 + §29 + §30 累积)
- `M JianghuNavHost.kt`(§24 + §25 + §28 + §29 + §30 累积)

### 沉淀(§30)

- **"最后一个使用点消失"才该删 import,而不是"某次改动后看起来不用了"**(新)—— §28 时标签2 改成简单 clickable,**看起来** `MutableInteractionSource` 可以删了,但标签3/4 还在用他。**"某处不用了"不等于"全文件不用了"** —— 判据永远是 `grep 全文件 = 0 实际引用`
- **注释里保留"为什么删"比保留"删了什么"更有用**(新)—— 我在标签4 的注释里写了"§30 是最后一个死区 → 本文件已无消费型 clickable,故 import 一并移除"。**这样以后若有人想加回死区标签,会立刻知道需要重新加 import**
- **同一模式重复 3 次后,应该主动把它固化成"模板"**(新)—— 死区→活区的 4 步(加参数 / 改 clickable / NavHost 接线 / KDoc 同步)在 §30 已零摩擦。**下次再有类似需求(比如后山3/4 的标签配置),可以直接照 §28~§30 的写法做,不必重新设计**
- **"配置完成"是值得显式记录的里程碑**(新)—— §30 之后后山 2 的 4 标签**全部可点**,这是一个**阶段性完工**。若只记"又配了一个标签",以后回看会不知道"配完没有"。**KDoc 里加一个"✅ 4 个标签全部可点击"的总结块**,让完成状态一眼可见

---

## §31 后山 3 三个标签配置跳转目标(2026-09-17 晚)— A 模式不 commit

**用户指令**:"后山3页面的三个标签也要可以跳转文本对应的页面"

### 目标映射:沿用后山2 的"文字 → 卷"表

用户说"**文本对应的页面**" —— 即复用 §28~§30 在后山2 建立的那张映射表,**同一段文字跳到同一卷**:

| 标签 | 文字 | 目标 | 后山2 里配置于 |
|---|---|---|---|
| 标签2 | 拆招心法 | **第二卷-1** | §28 |
| 标签3 | 万象谱 | **第三卷-1** | §29 |
| 标签4 | 寻径迷踪步 | **第四卷-1** | §30 |

> 后山3 没有"识机真决"标签(§21n 之前就没复制过来),故只有 3 个目标。

### 改动(2 个文件,5 处)

| 文件 | 改动 |
|---|---|
| **`Houshan3Screen.kt`** | ① 签名加 3 个参数;② 3 个标签的 clickable 从"死区"改为**真实跳转**;③ KDoc 布局清单 + 新增"✅ 3 标签全部可点击"总结块;④ 内容层注释更新;⑤ **删除 `MutableInteractionSource` import** |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian3)` 里加 3 行接线(带行尾注释标明是哪个标签)|

**3 个标签的 clickable 写法**(与后山2 完全统一):

```kotlin
.clickable(enabled = !isTransitioning, onClick = onOpenVolumeX)
```

### ⚠️ 关键:`enabled = !isTransitioning` 在这里比后山2 更重要

后山3 **有 dolly-in 过渡动画**(整屏空白点击 → dolly → 后山4),因此存在一个后山2 没有的风险:

> 若用户在 **dolly 推进途中**点到标签,可能触发"导航到卷X-1"与"dolly 到后山4"的竞争。
> `enabled = !isTransitioning` 正是为此 —— **过渡期间 3 个标签的 clickable 全部失效**。

**行为对照**(§31 后):

| 点击位置 | 结果 |
|---|---|
| 标签2 拆招心法 | → 第二卷-1 |
| 标签3 万象谱 | → 第三卷-1 |
| 标签4 寻径迷踪步 | → 第四卷-1 |
| 其余任意空白 | → dolly-in 推进 → 后山4 |
| 返回按钮 | → 后山2 |
| (dolly 进行中)点任何标签 | ❌ 无效(`enabled = false`)|

### 顺手删 import:`MutableInteractionSource`

后山3 的 3 个标签是 `MutableInteractionSource` 的**全部使用者** —— 3 个都改成简单 clickable 后:

| 步骤 | 结果 |
|---|---|
| grep `MutableInteractionSource` | 只剩 **import 行**,零实际使用 |
| 删除 import | ✅ |
| 编译验证 | ✅ `BUILD SUCCESSFUL`(15s)|

> 这是**今天第二次**用同一条沉淀(§30 删后山2 的 import)。**"最后一个使用点消失才删 import"** 已成稳定动作。

### 后山1/2/3 标签状态总览(§31 后)

| 页面 | 整屏点击 | 标签 | 标签跳转 |
|---|---|---|---|
| **后山1** | → 后山2 | 4 个 | 全部 → 后山2(§27,标签是"活区"但目标统一)|
| **后山2** | dolly → 后山3 | 4 个 | 卷1 / 卷2 / 卷3 / 卷4(§22 §28 §29 §30)|
| **后山3** | dolly → 后山4 | 3 个 | **卷2 / 卷3 / 卷4**(§31)|
| **后山4** | noop(终点)| 4 个 | 待配 |

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 15s
```

### git 状态(§31)

- `M Houshan3Screen.kt`
- `M JianghuNavHost.kt`

### 沉淀(§31)

- **"文本对应的页面"= 复用已有的映射表,而不是重新问一遍**(新)—— 用户说"跳转**文本对应的**页面",而不是"跳到某某页面"。**这意味着映射关系已经建立过了**(§28~§30 在后山2),我只需**复用同一张表**。**如果我把"拆招心法"在后山3 配到别的卷,就是违反了"对应"二字**
- **同一交互在不同页面风险不同:有 dolly 的页面必须防误触**(新)—— `enabled = !isTransitioning` 在后山2 是"锦上添花"(防过渡中乱点),但在后山3 是**必需**(否则会与 dolly 竞争)。**照抄写法时要知道它在新环境里解决的是什么问题**,而不是机械复制
- **"删 import"这条沉淀今天用了两次,已固化成标准动作**(新)—— §30(后山2)、§31(后山3)都是:改完最后一个使用点 → grep 确认零引用 → 删 import → 编译验证。**当一条沉淀能连续复用两次,说明它值得写进"项目级约定"而不是单次经验**
- **多页面配置同类目标时,"映射表"应该集中记录**(新)—— 现在后山2(4 个)和后山3(3 个)的标签目标都只散落在各自文件的 KDoc 里。**若以后要加"后山4 的标签",得翻三个文件才知道"万象谱"对应哪一卷**。候选改法:在 `docs/ONBOARDING.md` 里加一张"后山页标签 → 卷"总表,但**当前只有 7 条、还不够乱,先不加**(重复 3 次再抽象)

---

## §32 后山 4 四个标签配置跳转目标 + 占位参数名改语义名(2026-09-17 晚)— A 模式不 commit

**用户指令**:"在后山4页面,标签'万象谱'对应卷三,'寻径迷踪步'对应卷四,'百炼识物诀'对应卷五,'分门辨类掌'对应'卷六',点击时可以跳转到对应的卷的第一页"

### 目标映射

| 标签 | 文字 | 目标 | callback |
|---|---|---|---|
| 1 | 万象谱 | **第三卷-1** | `onOpenVolume3Part1` |
| 2 | 寻径迷踪步 | **第四卷-1** | `onOpenVolume4Part1` |
| 3 | 百炼识物诀 | **第五卷-1** | `onOpenVolume5Part1` |
| 4 | 分门辨类掌 | **第六卷-1** | `onOpenVolume6Part1` |

### 关键观察:映射规则是"**文字 → 卷**",不是"位置 → 卷"

后山 4 的标签文字是 §24 **改过**的(识机真决→万象谱 / 拆招心法→寻径迷踪步 / 万象谱→百炼识物诀 / 寻径迷踪步→分门辨类掌),所以**不能照抄后山2/3 的位置映射**:

| 位置 | 后山2 文字 → 目标 | 后山4 文字 → 目标 | 一致? |
|---|---|---|---|
| 1 | 识机真决 → 卷1 | **万象谱 → 卷3** | 文字不同,但"万象谱→卷3"这条规则一致 ✓ |
| 2 | 拆招心法 → 卷2 | **寻径迷踪步 → 卷4** | "寻径迷踪步→卷4"一致 ✓ |
| 3 | 万象谱 → 卷3 | **百炼识物诀 → 卷5** | 新文字(后山2 没有) |
| 4 | 寻径迷踪步 → 卷4 | **分门辨类掌 → 卷6** | 新文字 |

→ **用户给的映射与 §28~§31 建立的"文字→卷"表完全自洽**(同一段文字在任何页面都去同一卷)。
→ 这也解释了 §31 沉淀里"用户说'跳转**文本对应的**页面'"的措辞 —— **规则一直锚在文字上**。

### 改动(2 个文件,6 处)

| 文件 | 改动 |
|---|---|
| **`Houshan4Screen.kt`** | ① 4 个参数从占位名 `onOpenTagN` **改成语义名**;② 4 处 `onClick` 同步;③ 4 处标签注释 + KDoc 布局清单补目标;④ KDoc 的 §24 占位说明块改写为 §32 已配置 |
| **`JianghuNavHost.kt`** | `composable(Routes.Shilian4)` 的 4 个 `{}` 换成 `navigate(Routes.VolumeXPart1)` |

### 一个超出原计划的决定:参数改名

**§24 当时代码注释明确写了**:

> 一旦加好目标,在 composable(Routes.Shilian4) 里把对应 callback 改成 navigate(...) 即可,**不必再改本文件**

**§32 我改了这个文件**(参数名 `onOpenTagN` → `onOpenVolume3Part1` 等)。理由:

| 维度 | 保留 `onOpenTagN` | 改成语义名(采用)|
|---|---|---|
| 改动量 | 只改 NavHost(符合 §24 承诺)| 改 2 个文件 |
| 可读性 | `onOpenTag1` 完全看不出目标,须翻 NavHost | 名字即映射 |
| 跨页一致性 | 与后山2/3 的 `onOpenVolumeXPart1` **不一致** | **一致** |
| §31 沉淀的"映射散落"问题 | 加剧 | **缓解**(名字自带映射)|

→ **§24 的"不必再改本文件"是"目标未知时"的便利承诺,不是永久约束**。目标确定后,**占位名完成使命,应换成语义名**。

### 后山 4 的 clickable 没有 `!isTransitioning`(与后山2/3 不同)

| 页面 | 整屏点击 | 标签 clickable |
|---|---|---|
| 后山2 | dolly → 后山3 | `.clickable(enabled = !isTransitioning, ...)` |
| 后山3 | dolly → 后山4 | `.clickable(enabled = !isTransitioning, ...)` |
| **后山4** | **noop(终点页)** | **`.clickable(onClick = onOpenXXX)`** —— 无过渡,不需要门槛 |

> 后山4 是终点页,整屏点击是 noop、没有 dolly 过渡 → 不存在"过渡中误触"的竞争 → **不加 `enabled = !isTransitioning`**。
> (该门槛在后山2 是"锦上添花",在后山3 是"必需",在后山4 是"无意义")

### 后山 1/2/3/4 标签配置总览(§32 后 — **全部配置完成**)

| 页面 | 标签数 | 跳转目标 |
|---|---|---|
| 后山1 | 4 | 全部 → 后山2(§27;标签是"活区"但目标统一,不是"文字→卷")|
| 后山2 | 4 | 识机真决→卷1 · 拆招心法→卷2 · 万象谱→卷3 · 寻径迷踪步→卷4 |
| 后山3 | 3 | 拆招心法→卷2 · 万象谱→卷3 · 寻径迷踪步→卷4 |
| **后山4** | **4** | **万象谱→卷3 · 寻径迷踪步→卷4 · 百炼识物诀→卷5 · 分门辨类掌→卷6** |

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 14s
```
`grep onOpenTag` 全项目 = 只剩 3 处注释里的历史说明,**零代码引用**。

### git 状态(§32)

- `M Houshan4Screen.kt`
- `M JianghuNavHost.kt`

### 沉淀(§32)

- **映射规则要锚在"内容"上,不是"位置"上**(新)—— 后山4 的标签文字是 §24 改过的,若按"第 1 个标签→卷1"的**位置规则**实现,4 个目标全错。**用户说"标签'万象谱'对应卷三"是在说内容,不是序号** —— 遇到"文字→X"的表述,永远按文字匹配
- **占位名的生命周期:目标确定即结束**(新)—— §24 用 `onOpenTagN` 是因为"目标未知",它能避免瞎猜。**但目标一旦确定,占位名就从"方便"变成"障碍"**(看不出去哪)。**判据:当 param 的注释需要写"// 万象谱"才能看懂时,就该改名了**
- **"不改本文件"这类承诺要标注前提条件**(新)—— §24 写"不必再改本文件"时,隐含前提是"参数名保持不变"。**§32 违背了字面承诺但符合意图**(目标配置本身不受影响)。**写这类承诺时应写明"只要不改参数名"**,否则后续变更会造成"注释与事实不符"
- **`enabled = !isTransitioning` 不是通用写法,而是"有过渡才需要"**(新)—— 这个门槛今天先后出现在后山2/3/4 的讨论里。**它是 dolly 的伴生需求**:没有过渡的页面(后山4 终点)加了它反而多余。**照抄写法前先问"这个页面有过渡吗"**
- **"全部配置完成"值得显式记录**(新)—— §32 之后**后山 1/2/3/4 所有标签的跳转目标都已配置**(14 个)。**这是今天导航工作的完工点**,日志里应有明确标记,否则以后回看不知道"还有没有没配的"

---

## §33 新建后山5 页 + 后山4 补 dolly(2026-09-17 晚)— A 模式不 commit

**用户指令**:"创建后山5页面,那种山峰拉近的动画需要在'后山4'页面跳转到'后山5'页面时出现,后山5复用后山3页面的素材和动画,点击除了'后山4'页面的除了标签以外的位置,可以跳转到后山5页面"

拆成 4 件事:
1. 新建 **后山5 页**(复用后山3 的素材和动画)
2. **后山4 补上 dolly-in**(山峰拉近),在 后山4 → 后山5 时播放
3. 后山4 的整屏点击从 **noop** 改为 **dolly 推进**
4. 后山4 的标签**不参与**整屏跳转(标签仍是各自跳卷)

### 改动(4 个文件)

| 文件 | 改动 |
|---|---|
| **`Houshan5Screen.kt`**(新建)| 复用后山3 的素材/动画;当前是**终点页**(整屏 noop,无 dolly)|
| **`Houshan4Screen.kt`**(重写 ~600 行)| 从"终点页"改造成"过场页":加 dolly 三景深平面 + `onOpenHoushan5` 参数 + 标签加 `enabled` 门槛 |
| **`Routes.kt`** | 加 `Shilian5 = "shilian5"` |
| **`JianghuNavHost.kt`** | 加 import;Shilian4 加 `exitTransition`(→Shilian5 时 520ms 淡出);新增 Shilian5 composable(enterTransition 同 Shilian3/4)+ 接线 |

### 后山5 与后山3 的关系

**复用(逐项一致)**:

| 项 | 值 |
|---|---|
| 背景图 | `img_shilian2_bg`(后山3 那张"试炼转换.png")|
| 云雾层 | `HoushanMistLayer(variant = HoushanMistVariant.Houshan3)` |
| 5 朵 ACI | 58 / FCB左下 / 60 / FCB中下 / 62(周期 21.4/11/8/10/9 s)|
| 5 朵老云 | old / 56 / 58 / 57 / 5(§21f 同款)|
| 熊猫 | `img_shilian2_recovered_8`,X=118 Y=405 W=181 H=96 + 上下浮/呼吸缩放 |
| 3 个标签 | 万象谱(X=43,Y=390)· 寻径迷踪步(X=105,Y=295)· 拆招心法(X=124,Y=521)|
| 返回按钮 | Return.png,X=30 Y=60 W=18 H=18 |

**改造(与后山3 不同)**:

| 项 | 后山3 | 后山5 |
|---|---|---|
| dolly 过渡 | ✅ 有(→后山4)| ❌ **无**(当前是终点页)|
| 整屏点击 | `startDollyIn()` | **noop** |
| `isTransitioning` / 三景深平面 | ✅ | ❌ |
| 标签 `enabled = !isTransitioning` | ✅ 必需 | ❌ 无过渡 → 不需要 |
| 标签跳转目标 | 卷2/卷3/卷4(§31 已配)| **未配置**(见下)|

### 后山5 的标签:沿用 §24 对后山4 的做法

用户只说"复用**素材和动画**"(未提标签跳转)→ 3 个 callback **默认 noop**,但**参数名已取语义名**:

```kotlin
onOpenVolume2Part1: () -> Unit = {},  // (预留)拆招心法   → 第二卷-1?
onOpenVolume3Part1: () -> Unit = {},  // (预留)万象谱     → 第三卷-1?
onOpenVolume4Part1: () -> Unit = {},  // (预留)寻径迷踪步 → 第四卷-1?
```

NavHost 里也写了"若日后要接,参照后山3 §31 的映射"的三行注释 —— **接的时候只改 NavHost 一处**。

### ⚠️ 重要观察:**"终点页"是暂时的**

后山4 在 §24~§32 期间是**终点页**(整屏 noop、无 dolly、标签无门槛)。§33 因为要在它后面接后山5,必须**同时改三件事**:

| # | 改动 | 目的 |
|---|---|---|
| 1 | 加 **dolly 三景深平面** | 播放"山峰拉近" |
| 2 | 整屏 clickable:`noop` → `startDollyIn()` | 触发推进 |
| 3 | 标签加 `enabled = !isTransitioning` | 防"跳卷"与"dolly"竞争 |

**后山3 在 §24 经历过完全一样的三件事**(它原本跳"未完待续",也是终点)。

→ **结论:每次在后面接新页面,原终点都要做这三件事**。这不是偶然 —— 建议以后新增页面时**直接按"过场页"的完整结构写**(dolly + 门槛都先加上),只在"确实是最后一页"时才退回 noop。**代价是多写 ~60 行 dolly 代码,收益是省掉一次返工**。
→ 但反过来:**后山5 现在按"终点页"写是合理的**,因为不知道会不会接后山6;过早加 dolly 就是空转。

### 点击行为(后山4,§33 后)

| 点击位置 | 结果 |
|---|---|
| 4 个标签 | → 各自对应的卷的第一页(§32)|
| **其余任意位置**(空白/云/熊猫)| → **dolly-in → 后山5** |
| 返回按钮 | → 后山3 |
| (dolly 进行中)点任何标签 | ❌ 无效 |

### 完整推进链(今日新增两段)

```
后山1 ──整屏点击──→ 后山2 ──dolly──→ 后山3 ──dolly──→ 后山4 ──dolly──→ 后山5(终点)
  │                    │                │                │
  └标签→后山2          ├标签→卷1        ├标签→卷2        ├标签→卷3
                       ├标签→卷2        ├标签→卷3        ├标签→卷4
                       ├标签→卷3        └标签→卷4        ├标签→卷5
                       └标签→卷4                         └标签→卷6
```

**四段"整屏推进"的机制现已全部统一**:
- 后山1 → 后山2:直接 `onOpenHoushan2()`(无 dolly,§27)
- 后山2 → 后山3:dolly 三景深平面(§36)
- 后山3 → 后山4:dolly(§24)
- **后山4 → 后山5:dolly(§33)**

### 编译

```
.\gradlew.bat compileDebugKotlin  →  BUILD SUCCESSFUL in 16s / 3s
```

### git 状态(§33)

- `?? Houshan5Screen.kt`(新)
- `M Houshan4Screen.kt`
- `M Routes.kt`
- `M JianghuNavHost.kt`

### 沉淀(§33)

- **"终点页"是暂时状态,接新页时要改三件事**(新)—— §24(后山3)、§33(后山4)各经历一次:加 dolly + 改整屏 click + 给标签加门槛。**这是"页面链"的固有代价** —— 写新页面时若**有把握还会往后接**,直接按过场页写;若不确定,按终点页写、以后返工
- **"复用素材和动画"≠"复用行为",这条 §24 的沉淀在 §33 再次生效**(新)—— 后山5 复用后山3 的素材/动画,但**没有**复用后山3 的 dolly(因为后山5 是终点)。**同一份视觉资产,在不同位置(N=终点 vs N=过场)需要不同的交互结构**
- **给"未配置"预留语义名 + NavHost 注释,比留 TODO 更有效**(新)—— 后山5 的 3 个 callback 虽是 noop,但参数名已是 `onOpenVolume3Part1` 等,且 NavHost 里写好了"要接就照后山3 §31 的映射"三行示例。**下次接的时候是"改 3 行"而不是"先搞清该叫什么名"**
- **"最后一个终点页"的判断应该显式写进注释**(新)—— 后山5 的 KDoc 里写明"当前是终点页",并注明"若日后接后山6,这里改成 `startDollyIn()` 并补三景深平面"。**让未来的自己知道"这里不是永久 noop"**
- **四段推进链的机制统一,是今天导航工作的真正收口**(新)—— 后山1→2 用直接跳转(无 dolly),后山2→3 / 3→4 / 4→5 用同款 dolly。**在日志里画出完整链条图**,比分散在 4 个文件里更容易看出"哪段用什么机制"

---

## §34 Docker Desktop:compose 插件缺失 + 8010 端口保留冲突(2026-09-17 深夜)— 不是代码改动

> 本节是**环境类踩坑**,与 §23(adb reverse)/ §26(登录)同性质。
> 涉及 3 个独立问题 + 1 个工具坑,全部有实测证据。

### 问题 1:`docker compose` 子命令不存在 → Docker Desktop 报 "unknown flag: --project-name"

**用户报错**(Docker Desktop UI):

```
Cannot start Docker Compose application. Reason: compose [start] exit status 125.
unknown flag: --project-name
Usage: docker [OPTIONS] COMMAND [ARG...]
```

**诊断链**:

| 检查 | 结果 |
|---|---|
| `docker --version` | `29.7.2` ✅ CLI 在 |
| **`docker compose version`** | ❌ **`unknown command: docker compose`** |
| `docker-compose --version` | `v5.4.0` ✅ 独立版在 |
| `docker info` → Plugins | ❌ **只有引擎的 Volume/Network/Log,没有 compose** |
| `~/.docker/cli-plugins/` | ❌ **空目录** |

**根因**:

```
Docker Desktop 的正常行为:在 ~/.docker/cli-plugins/ 建【符号链接】指向 resources\cli-plugins\*
        ↓
Windows 建符号链接【需要开发者模式或管理员权限】
        ↓
链接创建失败 → 目录留空
        ↓
docker CLI 找不到 docker-compose.exe 这个插件 → `docker compose` 不是合法子命令
        ↓
Docker Desktop 调 `docker compose ... --project-name X ... start` 时,
参数落到 docker 顶层解析 → "unknown flag: --project-name",exit 125
```

> **报错信息误导性极强**:看着像"compose 参数写错了",实际是"compose 插件压根没装"。
> 判据见 §34 沉淀 —— **看 Usage 行是谁打印的**。

**❌ 第一次修复尝试(失败)**:

```powershell
# 把安装目录里的 docker-compose.exe 复制到用户插件目录
Copy-Item "...\DockerDesktop\resources\bin\docker-compose.exe" `
          "$env:USERPROFILE\.docker\cli-plugins\docker-compose.exe"
```

当场有效(`docker compose version` 通过、`docker info` 列出 compose)——
**但用户重启机器后失效**:文件被删除,目录又空了。

**验证**(重启后):

| 检查 | 结果 |
|---|---|
| `~/.docker/cli-plugins/docker-compose.exe` | ❌ **文件不在了** |
| `~/.docker/cli-plugins` 目录 | ❌ 空 |
| `docker compose version` | ❌ `unknown command` |

→ **结论:该目录被 Docker Desktop 管理/清理,复制进去不是持久修法。**

**✅ 持久修复(采用)**:

发现 Docker Desktop **自己的插件目录里全套插件都在**:

```
C:\Users\28784\AppData\Local\Programs\DockerDesktop\resources\cli-plugins\
    docker-compose.exe   (50 MB)
    docker-buildx.exe · docker-ai.exe · docker-debug.exe · docker-scout.exe
    docker-mcp.exe · docker-model.exe · docker-pass.exe · docker-desktop.exe ...
```

**改 `~/.docker/config.json`,加 `cliPluginsExtraDirs` 指向它**:

```json
{
  "auths": {},
  "cliPluginsExtraDirs": [
    "C:/Users/28784/AppData/Local/Programs/DockerDesktop/resources/cli-plugins"
  ],
  "credsStore": "desktop",
  "currentContext": "desktop-linux",
  "plugins": { ... },
  "features": { "hooks": "true" }
}
```

**验证**:

| 检查 | 结果 |
|---|---|
| `docker compose version` | ✅ `Docker Compose version v5.4.0` |
| `docker info` → Plugins | ✅ `compose: ... Path: ...\resources\cli-plugins\docker-compose.exe` |

**为什么这次持久**:
- 插件**直接从 Docker Desktop 自己的安装目录加载**(它不会删自己的文件)
- **不再往会被清空的 `~/.docker/cli-plugins` 复制**
- `config.json` 是持久文件

**⚠️ 踩坑过程中的工具坑:PowerShell 写 JSON 会带 BOM**

第一次用 `Set-Content -Encoding utf8` 写 config.json → **docker 报**:

```
WARNING: Error parsing config file (C:\Users\28784\.docker\config.json):
invalid character 'ï' looking for beginning of value
docker: unknown command: docker compose
```

`ï` = BOM(`EF BB BF`)被当 latin1 解读。**Go 的 JSON 解析器不接受 BOM** → 整个 config 失效 → compose 又不见了。

**正确写法**(无 BOM):

```powershell
[System.IO.File]::WriteAllText($path, $json, (New-Object System.Text.UTF8Encoding($false)))
```

**验证方法**:读前 3 字节,应为 `7B 0D 0A`(`{`+CRLF),**不是** `EF BB BF`。

**备份**:`~/.docker/config.json.bak-20260917-222524`

---

### 问题 2:8010 被 Windows 保留端口占用 → `bind: WSAEACCES`

问题 1 修好后,同一操作的报错**推进到更后一步**:

```
Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:8010 -> 127.0.0.1:0:
listen tcp 0.0.0.0:8010: bind: An attempt was made to access a socket
in a way forbidden by its access permissions.
```

> ⚠️ **注意这不是"端口被占用"** —— 被占用的报错是 `Only one usage of each socket address...`。
> `WSAEACCES`(Windows 10013)= **端口在系统的"排除端口范围"里**。

**诊断**:

```powershell
netsh int ipv4 show excludedportrange protocol=tcp
```

```
Start Port    End Port
    1623        1722
    7456        7555
    ...
    7956        8055      ← 8010 在这里!
    8056        8155
   50000       50059   *
```

**8010 落在 `7956-8055`**(Hyper-V/WSL 预留的动态端口段)→ 任何进程 `bind(8010)` 都失败。

**与 Docker 无关的证据**(直接 bind 测试,不经过 Docker):

| 端口 | 可绑? |
|---|---|
| 8000 / **8010** / 8050(都在 7956-8055)| ❌ 全部 `WSAEACCES` |
| 8200 / 18010(不在任何范围)| ✅ 可绑 |

**🔬 决定性实验:两类"排除"的行为完全不同**

列表里 `50000-50059` 带 `*`(=`Administered port exclusions`),而 `7956-8055` 不带。实测:

| 类别 | 示例 | 可绑? |
|---|---|---|
| **管理员排除**(带 `*`)| 50000 / 50001 / 50059 | ✅ **全都能绑!** |
| **Hyper-V 预留**(不带 `*`)| 8000 / 8010 / 8050 | ❌ 全不可绑 |

→ **「管理员排除」只禁止系统自动分配,应用仍可显式绑定;「Hyper-V 预留」是真占用。**
→ 所以正解是:**把 8010 登记成"管理员排除",Hyper-V 就不会再抢,而 Docker 仍能绑。**

**备选(改端口)为什么不用**:

8010 在本仓库有 **103 处引用**(`infra/docker-compose.yml` / `android/local.properties` / `build.gradle.kts` / `services/api/app/core/config.py` / `storage.py` / `DEV-SETUP.md` / `ONBOARDING.md` / 一堆 SESSION-LOG)。
改端口会让文档与实现大面积脱节。**所以优先保住 8010。**

(不过有个巧办法:compose 改 8200 + `adb reverse tcp:8010 tcp:8200` → **App 不用改**。留档备用。)

**✅ 实际结果:重启机器后自动解决**

用户重启后重新查:

| | 重启前 | 重启后 |
|---|---|---|
| 保留范围 | 1623-1722, **7456-8155**, 50000-50059 | 1516-1615, 12438-13342, 50000-50059 |
| **8010** | ❌ 不可绑 | ✅ **可以绑定** |

Windows 每次启动**重新随机分配**保留范围 —— 这次 8010 恰好被放出来了。

**⚠️ 会复发** —— 某次重启后可能又被圈进去。**届时的管理员修法**(方案有效性已由上面的实验证明,但**本次未实际执行**,因为重启已解决):

```powershell
net stop winnat
netsh int ipv4 add excludedportrange protocol=tcp startport=8010 numberofports=1 store=persistent
net start winnat
```

---

### 问题 3:`adb reverse` 被重启清空(§23 老坑复发)

重启后 `adb reverse --list` 为空 → 补:

```powershell
adb reverse tcp:8010 tcp:8010      # → UsbFfs tcp:8010 tcp:8010
```

**验证**:`adb shell curl http://127.0.0.1:8010/docs` → **200** ✓

→ **§23 的"重启/USB 抖动会清空 reverse"在 §34 再次应验**,今天是**第二次**踩。§23 里列的 4 个候选根治方案仍未落地。

---

### 最终状态(全部验证通过)

| 项 | 状态 |
|---|---|
| `docker compose` | ✅ v5.4.0(**持久修复**)|
| compose 栈 | ✅ **6 容器全起**,`api` healthy |
| API 8010 | ✅ `http://127.0.0.1:8010/docs` → **200**;登录接口实测成功(`138****8000`)|
| `adb reverse` | ✅ `UsbFfs tcp:8010 tcp:8010`;设备侧 curl → **200** |
| 端口保留冲突 | ✅ 重启后消失(**需留意复发**)|

**容器明细**:

| 容器 | 状态 | 端口 |
|---|---|---|
| `jianghu-dev-api-1` | Up (healthy) | **0.0.0.0:8010→8000** |
| `jianghu-dev-worker-1` | Up | 8000/tcp |
| `jianghu-dev-postgres-1` | Up (healthy) | 55432→5432 |
| `jianghu-dev-minio-1` | Up (healthy) | 19000/19001 |
| `jianghu-dev-redis-1` | Up (healthy) | 16379→6379 |
| `jianghu-dev-clamav-1` | Up | 3310 / 7357 |

### git 状态(§34)

- **无代码改动** —— 全部是环境诊断与配置修复
- 改动的**非仓库文件**:`~/.docker/config.json`(加 `cliPluginsExtraDirs`)

### 沉淀(§34)

- **看报错里的 Usage 行判断"是谁在报错"**(新)—— `unknown flag: --project-name` 配 `Usage: docker [OPTIONS] COMMAND` 时,**打印者不是 compose 而是 docker 顶层 CLI** → 说明 **compose 插件根本没加载**,而不是"参数位置写错了"。**如果只看 flag 名去调参数顺序,会白折腾一整天**
- **"复制到系统目录"修法要先确认该目录的归属权**(新)—— 我第一版把 `docker-compose.exe` 复制进 `~/.docker/cli-plugins`,当场有效、**重启即失效**(该目录由 Docker Desktop 管理并清理)。**判据:这个目录是"应用程序管理的"还是"用户的"?** 前者不要往里塞东西,应该**改配置让它去别处找**
- **同一个报错修好后会"推进"到下一个错误,别以为是没修好**(新)—— 修好 compose 插件后,报错从 `exit 125 unknown flag` 变成 `exit 1 ports are not available`。**错误变化 = 修复生效的证据**,要继续往后排
- **`WSAEACCES` ≠ 端口被占用**(新)—— Windows 上 bind 失败有两种截然不同的原因:
  - `Only one usage of each socket address` = **真被占用**
  - `An attempt was made to access a socket in a way forbidden by its access permissions` = **落在系统保留端口范围**
  后者用 `netsh int ipv4 show excludedportrange protocol=tcp` 查,**与 Docker 无关**
- **"管理员排除"和"Hyper-V 预留"必须区分**(新)—— 列表里带 `*` 的是管理员排除(**可绑定**),不带的是 Hyper-V 预留(**不可绑定**)。**实测数据见上表**。所以"把端口加进排除列表"这个听起来自相矛盾的操作,**实际是有效修法**
- **PowerShell 写 JSON 给外部程序读,必须显式去 BOM**(新)—— `Set-Content -Encoding utf8` 在 pwsh 下**仍可能带 BOM**;Go 的 JSON 解析器报 `invalid character 'ï'` 就是 BOM 被当 latin1。**正确写法**:`[System.IO.File]::WriteAllText($p, $s, (New-Object System.Text.UTF8Encoding($false)))`,并**读前 3 字节自检**
- **"重启一下试试"在这类问题上真的是有效手段**(新)—— 端口保留范围**每次启动随机重排**,重启让 8010 脱离了保留段。**但要知道它是"碰运气"而非"根治"** —— 所以本节仍给出管理员修法备用
- **100+ 处引用的端口不要为了绕一个环境问题而改**(新)—— 改端口的"文档漂移成本"远高于"用管理员权限修系统配置"。**先量化影响面再决定改哪边**;同时留了"改 compose + 改 adb reverse、App 不动"这种巧办法作为退路

---

## 沉淀(新)

- **"反转 §18"≠"全盘否定 §18"**(新)— §22 加回熊猫但保留"不复制 Rectangle156 气泡"的决定 —— 这就是 §18 **部分成立 + 部分反转**。写代码 / 注释时,**显式标注哪些子决定保留、哪些反转**,比单纯写"反转 §18"信息密度高 10 倍。否则半年后回来看,**不知道 §18 当时还说了什么、为什么今天改了**
- **多层栈的诊断要看对应层**(新)— 今天有 3 层(adb 链路 / USB reverse / app 网络),我第一反应只看了最浅的 adb 链路,**用户问"为什么还要登录"才意识到要看 app 层**。**诊断问题的标准动作**:把报错现象先**分层归类**(adb 层 / app 网络层 / app 业务层),再分别检查。**报错现象出现在哪一层,就从哪一层开始查**,不要从最容易查的开始
- **"我看不到的,你要告诉我"(新)— 我看得到 + 检查截图** 这是今天唯一的合规体验。今天做的对的事:**用户说"还需要登录账号"后,主动 `adb shell screencap` 拿截图确认**,而不是继续猜。**截图 = 唯一可信的"屏上是什么"信息源**,其它(adb 状态、logcat、UI hierarchy)都是间接的
- **"git status -sb"是 dev 的脉搏**(新)— 开工第一件事查这个;不光是看"有什么未 commit",更重要的是看**分支 + 远端是否同步**。今天发现 `zzz` 已经同步到 `58ae39a`,说明上次 push 没问题,**这是个让心情轻松的小确认**
- **本节"§23 不是代码工作"的元教训**(新)— 我犹豫过"非代码工作要不要写进 SESSION-LOG"。**结论:踩坑 + 沉淀够分量就写**。dev 工作 = 写代码 + 调试环境 + 学经验,**SESSION-LOG 是后者唯一的留痕**。如果只记代码改动,半年后回看"为什么我们的 dev 流程这么依赖 adb reverse"会找不到答案