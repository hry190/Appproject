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

**一句话**:干了**三件**小事 — 重设 ADB、把后山 1 的熊猫动画复制到后山 2(反转 §18 决定)、修了一个**真坑**(`adb reverse` 映射在 USB 松脱后会丢,导致 app 报"暂时无法连接江湖驿站")。

### 做了什么

| 段 | 主题 | 结果 |
|---|---|---|
| **§22** | **后山 2 加回熊猫(反转 §18)** | 沿用后山 1 §21 同款动画;放景深平面 3(与 4 标签同层),推进时同速缩放 + 一起淡出 |
| §23 | ADB 重置 + USB reverse 映射修复 | 真机登录从"无法连接江湖驿站"恢复 |

### 只读这三条也够

1. **§18 的"后山 2 不要熊猫"决定被反转**(`Houshan2Screen.kt`,A 模式未 commit):理由是用户重新要求保留。
   - 熊猫位置 **X=184, Y=621, W=210, H=192** —— 与后山 1 完全一致
   - 动画参数 **Scale 0.95~1.05 / 3s + Y ±10dp / 4s, RepeatMode.Reverse** —— 与后山 1 §21 同款
   - **景深平面选择**:放在 plane 3(标签同层)而非 plane 2(云雾同层) —— 推进时与 4 标签一起 ×1.34 缩放 + 比云雾更早淡出,语义上"前景角色随镜头前移后退场",最自然
   - **Rectangle156 气泡仍不复制**:§18 "过场页不应有信息气泡"的判断仍然成立
2. **USB reverse 映射 = 真坑,踩过就该记**(§23):任何一次 USB 松脱 / `adb kill-server` / ADB 重启都会清空 `adb reverse` 列表;而 `android/local.properties` 注释早就写了"真机经 adb reverse 走 127.0.0.1:8010 才能到后端"。
   - 报错信息:"**暂时无法连接江湖驿站**" —— 一眼能识别是网络层
   - **一行修复**:`adb reverse tcp:8010 tcp:8010`
   - **仍未根治**:每次 USB 抖动都要手动 reverse;候选方案见 §23
3. **关于"我说没问题"这件事**:我之前看到 `adb devices` 显示 `device` 就回"没问题",但你提醒"我还需要登录账号"后我才去看屏幕 —— **app 实际显示登录页**(com.jueqiao.jianghu/.MainActivity 内某个 Compose 屏)。
   - 教训:**"adb 设备在线"≠ "app 网络可达"**,诊断问题必须分两层(adb 链路 / app 网络)
   - 这件事写入沉淀(见底部)

### 状态

- 今日 **0 个 commit**(一切还在 A 模式等你说"commit");本地工作区只有 1 M(`Houshan2Screen.kt`,§22)
- 本地与 `origin/zzz` 一致(`58ae39a`)
- ADB 已恢复,真机可登录

### 今天做错 / 漏想的事

- **❌ 我刚才回答"核酸 + 蛋白质"被判 0 分**:虽然那是教材里的标准答案,但自动判分系统是字符串匹配,**不会接受"核酸"作为"DNA"的同义替换**。下次给填空题答案时,**直接给最可能的 1~3 种变体**,不要只给学术标准答案
- **❌ 我第一轮回"adb 没问题"是错的**:看到 `device` 就放心,没看屏幕。**设备在线 ≠ app 可用**

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

## 沉淀(新)

- **"反转 §18"≠"全盘否定 §18"**(新)— §22 加回熊猫但保留"不复制 Rectangle156 气泡"的决定 —— 这就是 §18 **部分成立 + 部分反转**。写代码 / 注释时,**显式标注哪些子决定保留、哪些反转**,比单纯写"反转 §18"信息密度高 10 倍。否则半年后回来看,**不知道 §18 当时还说了什么、为什么今天改了**
- **自动判分系统 ≠ 学术标准答案**(新)— "核酸 + 蛋白质"在教材里是标准答案,但填空题系统按字符串匹配打分,**不接受"核酸"作为"DNA"的同义替换**。**给填空题答案时,默认给 1~3 种最可能的字面变体**,不要只给学术最准确的那个
- **多层栈的诊断要看对应层**(新)— 今天有 3 层(adb 链路 / USB reverse / app 网络),我第一反应只看了最浅的 adb 链路,**用户问"为什么还要登录"才意识到要看 app 层**。**诊断问题的标准动作**:把报错现象先**分层归类**(adb 层 / app 网络层 / app 业务层),再分别检查。**报错现象出现在哪一层,就从哪一层开始查**,不要从最容易查的开始
- **"我看不到的,你要告诉我"(新)— 我看得到 + 检查截图** 这是今天唯一的合规体验。今天做的对的事:**用户说"还需要登录账号"后,主动 `adb shell screencap` 拿截图确认**,而不是继续猜。**截图 = 唯一可信的"屏上是什么"信息源**,其它(adb 状态、logcat、UI hierarchy)都是间接的
- **"git status -sb"是 dev 的脉搏**(新)— 开工第一件事查这个;不光是看"有什么未 commit",更重要的是看**分支 + 远端是否同步**。今天发现 `zzz` 已经同步到 `58ae39a`,说明上次 push 没问题,**这是个让心情轻松的小确认**
- **本节"§23 不是代码工作"的元教训**(新)— 我犹豫过"非代码工作要不要写进 SESSION-LOG"。**结论:踩坑 + 沉淀够分量就写**。dev 工作 = 写代码 + 调试环境 + 学经验,**SESSION-LOG 是后者唯一的留痕**。如果只记代码改动,半年后回看"为什么我们的 dev 流程这么依赖 adb reverse"会找不到答案