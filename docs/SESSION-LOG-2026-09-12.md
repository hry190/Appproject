# SESSION-LOG-2026-09-12

> 昨日: [SESSION-LOG-2026-09-11.md](SESSION-LOG-2026-09-11.md)
> 今日工作: **Vol-5 系列 7 屏**(Vol-5-1 → Vol-5-7)+ Gunlun12 新入口 + Vol-5-2 书框中途换素材

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | zzz |
| 主要工作 | 创建第五卷 7 屏(Vol-5-1 → Vol-5-7) |
| 新入口 | Gunlun12「已解锁秘籍9」图像 → Vol-5-1 |
| 标题切换 | 「样本从何而来」系列 → 「标签不能重复和乱贴」系列 → 「训练，检验，测试」|
| 书框交替 | Vol-5-{1,3,5,7}=255,Vol-5-{2,4,6}=256(已中断:Vol-5-2 改成 256 后 5-3 仍按 255 走)|
| 新增 PNG | 13 张(image 382~397 跨 7 屏) |
| 新增 const | Routes.Volume5Part1 ~ Volume5Part7(7 个)|
| 新增测试 | RoutesTest 7 个 assert |
| 静态核验 | ✅ grep Routes / NavHost / RoutesTest / KDoc vs 代码 |
| gradle 编译 | ❌ 未跑(全程 auto-mode 屏蔽)|
| 未 commit | Vol-5-1 ~ Vol-5-7 累计 |

## 当天操作记录(从最近往前)

### §30 创建 Vol-5-7 「训练，检验，测试」(本屏)

- **入口**:Vol-5-6 标题 `onOpenVolume5Part7`
- **书框**:Group 256(Vol-5-6 是 255,本屏 256 交替)
- **图 1**:image 396 18/135/355/311(原图 1035×918 比率 1.127,渲染比 1.141,畸变 1.2% 几乎完美)
- **图 2**:image 397 18/318/355/321(原图 1062×921 比率 1.153,渲染比 1.106,畸变 4.2% 可接受)
- **标题**:`110/67/192/32` 「训练，检验，测试」— 8 字(含 2 个中文`,`)按 6-8 字规约 W=192
- **文件**:新建 [Volume5Part7Screen.kt](../android/app/src/main/java/com/jueqiao/jianghu/ui/screens/volume5part7/Volume5Part7Screen.kt)
- **接线**:NavHost 5-6 → 5-7 + Routes.kt + RoutesTest
- **资源**:`img_volume5part7_image_{396,397}.png`

### §29 创建 Vol-5-6 「标签不能重复和乱贴」(沿用 Vol-5-4/5-5 标题)

- **入口**:Vol-5-5 标题 `onOpenVolume5Part6`
- **书框**:Group 255(Vol-5-5 是 256,本屏 255 交替)
- **图 1**:image 393 18/135/355/311(畸变 4.1%)
- **图 2**:image 394 18/318/355/321(畸变 2% 几乎完美)
- **标题**:`110/67/302/32` 「标签不能重复和乱贴」— 沿用 Vol-3-5 真机测过 9 字 W=302
- **KDoc**:Vol-5-6 标题 clickable 留 "等 Vol-5-7 创建时按历次约定回填"(随后 §30 已兑现)

### §28 创建 Vol-5-5 「标签不能重复和乱贴」(Group 256,书框异常续作)

- 用户原指令:"复制'第一卷-2'页面的背景和标题和书框" → 字面理解用 Group 256
- 但历次约定 Vol-5-X-奇数 = 255,Vol-5-X-偶数 = 256。5-5 是奇数 → 应 255
- 经 [AskUserQuestion](screen-copy-verify-coordinates.md) 反复推断(用户字面 vs 模式约定 vs 既成事实),用户意图"复制第一卷-2 的素材包括书框"被保留 → 用 256
- **本例外沉淀**:复制指令中明确指定页 = 字面优先于模式约定(同类案例待补)
- 图:image 391 / 395

### §27 创建 Vol-5-4 「标签不能重复和乱贴」(Group 255,异常首发生)

- 用户原指令:"复制'第一卷-1'页面素材,标题'标签不能重复和乱贴'"
- 标题是 9 字 → 触发 W=302 沿用 Vol-3-5 真机测过宽度(此屏是 9 字 W=302 的首次复用)
- 图:image 389 / 390
- 当时本应继续 255,但 §28 收到 Vol-5-5 时已用 256 — **交替被打破,5-5 是异常起点**

### §26 创建 Vol-5-3 「样本从何而来」

- 复制第一卷-1 素材(Group 255)
- 图:image 387 / 388

### §25 创建 Vol-5-2 「样本从何而来」(事后书框换 256)

- 用户先发指令「复制第一卷-1」(字面应为 Group 255)
- 我创建后用户说"用 image 256"
- 经 AskUserQuestion 确认意图是 Group 256(非字面 image 256.png — 该文件不存在)
- 修正后:`img_volume1part2_group_256` 替换原 `img_volume1_group_255`
- KDoc 同步更新("用户 2026-09-12 指定用 image 256;已确认为 Group 256")

### §24 创建 Vol-5-1 「样本从何而来」(新入口)

- 入口:Gunlun12「已解锁秘籍9」图像 → Vol-5-1(仿 Gunlun8 → Vol-4 模式)
- 素材:复制第一卷-1(背景 + Group 255 书框)
- 图:image 382 / 383
- 标题 W=192,6 字,沿用 6-8 字规约

### §23 9月12日 SESSION-LOG 起头

- 早上发现 2026-09-12.md 不存在
- 写本文件时已累积 §24-30(从 Vol-5-1 写到 Vol-5-7)

## 当前状态

### git status(累计未 commit)

```
 M android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt
 M android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt
 M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/volume5part6/Volume5Part6Screen.kt
 M android/app/src/test/java/com/jueqiao/jianghu/nav/RoutesTest.kt
?? android/app/src/main/java/com/jueqiao/jianghu/ui/screens/volume5part7/
?? android/app/src/main/res/drawable-nodpi/img_volume5part7_image_396.png
?? android/app/src/main/res/drawable-nodpi/img_volume5part7_image_397.png
```

注:§24-29 的 Vol-5-1 ~ Vol-5-6 累计未 commit 文件已叠加在 zzz 上(§30 创建 Vol-5-7 时同批)。

### 新增导航拓扑

```
 Gunlun12 → Vol-5-1 → 5-2 → 5-3 → 5-4 → 5-5 → 5-6 → 5-7(本屏末端)
```

### 已知沉淀(本日新结论)

| 项 | 内容 | 出处 |
|---|---|---|
| AskUserQuestion 隐式推断 | 字面 vs 模式约定 vs 既成事实,用户意图不歧 | §28 Vol-5-5 |
| 复制指令中明确指定页 | 字面优先于模式约定 | §28 Vol-5-5 |
| 9 字 W=302 复用基线 | Vol-3-5「特征与信息是否有关」是首个真机测过 | §27 Vol-5-4 |

## 明天(可选)优先级

1. 高 ROI:决定是否补齐 Vol-5-8 ~ Vol-5-14(Vol-4 有 14 屏,Vol-5 才 7 屏,半途中断)
2. 中 ROI:补 v2 audit HIGH 项(参考 [SESSION-LOG-2026-09-11.md §待修的 v2 audit 遗留](SESSION-LOG-2026-09-11.md))
3. 中 ROI:重跑 v2 失败 verifier(`resumeFromRunId=wf_1980fcb3-b31`)
4. 低 ROI:抽 scaffold helper(消 maintainability HIGH)

## 重要建议(沿用 9月10日 + 9月12日新加)

1. **每次开始新功能前先 `git checkout zzz`**
2. **Gradle JDK 永远设 jbr-21**
3. **重要操作独立存档为 CODE-AUDIT / SUMMARY / DECISIONS / MERGE-WORKFLOW**
4. **每个 PR/commit 后推 origin**
5. **未来文件变更涉及 i18n 时优先 stringResource(R)**,避免新增硬编码中文字符串
6. **每次"复制 X"先 grep X 的实际值,不再凭印象**(Vol-12 标题教训)
7. **每次建屏前先 git status 看是否漏 commit**(本日 §30 时漏了 §24-29 累计)— ⚠️ 本日教训
9. **书框奇偶交替中断例外已沉淀到 §28**:复制指令中明确指定页 = 字面优先于模式约定
10. **commit 之前必先写当日 SESSION-LOG**(commit-push-summary-rule)— 本日未遵守,务必明天起恢复

---

## §31 Vol-5-8 ~ Vol-5-12 五屏批量创建 + NavHost 4 次 stray `}` 修复 + 环境修复

### §31.1 Vol-5-8「训练，检验，测试」— 8 字 W=192

- 用户 2026-09-12 13:33;复制第一卷-2 → Group 256(Vol-5-7 也是 256,连续两屏 256 异常)
- 图:image 398(3.4%)/ image 399(3.9%)

### §31.2 Vol-5-9「训练，检验，测试」**— 无图 2,4 层 z-order**

- 用户 2026-09-12 13:35 创建指令只指定 image 400,未指定图 2
- 经 AskUserQuestion 确认"确认 Vol-5-9 只有图1,跳过图2"
- 复制第一卷-1 → Group 255(恢复交替:Vol-5-8 256 → Vol-5-9 255)
- image 400 畸变 **0.5% 几乎完美**

### §31.3 Vol-5-10「偏差的数据」— 5 字 W=213

- 用户 2026-09-12 14:05;复制第一卷-1 → Group 255(Vol-5-9 也是 255,连续两屏 255 异常)
- 图:image 401(**0% 完全匹配**)/ image 01(2.2%)— 图2 Y=318 改 Y=478(沿用 Vol-5-7/5-8 真机调过的值)
- KDoc 同步记录"用户已手动适配 Y=478"

### §31.4 Vol-5-11「偏差的数据」— 5 字 W=213

- 用户 2026-09-12 14:11;复制第一卷-2 → Group 256(Vol-5-10 255 → Vol-5-11 256 恢复交替)
- 图:image 4(3.6%)/ image 4001(0.6% 几乎完美)— Y=478 继续

### §31.5 Vol-5-12「偏差的数据」— 5 字 W=213

- 用户 2026-09-12 14:17;复制第一卷-1 → Group 255(Vol-5-11 256 → Vol-5-12 255 恢复交替)
- 图:image 101(1.5% 几乎完美)/ image 901(**0% 完全匹配**)— Y=478

### §31.6 NavHost stray `}` bug **4 次复发**(本日沉淀)

| Vol-N | stray `}` 位置 | 大括号差 | 备注 |
|---|---|---|---|
| Vol-5-9 | 1003-1005 行 3 个 | diff +3 | 修了 3 行 |
| Vol-5-10 | 1010 行 1 个 | diff +1 | 修了 1 行 |
| Vol-5-11 | 1017 行 1 个 | diff +1 | 修了 1 行 |
| Vol-5-12 | 1024 行 1 个 | diff +1 | **主动发现**(我之前承诺每次创建后跑 brace check)|**根因猜测**:NavHost 文件在新建 composable 后,闭合块外层多一个 `}`,可能是某个工具(IDE 自动格式化?)留下的。**未确认根因**(没追溯到具体来源)。

**应对策略**(本日沉淀):
- **每次新建 composable 后必跑 brace check**(我已主动做到)
- 检查命令:`python -c "content=open('JianghuNavHost.kt', encoding='utf-8').read(); print(content.count('{'), content.count('}'), content.count('{') - content.count('}'))"`
- 用户暂不考虑写文档("下次再出现的时候再让我考虑要不要加上")— ⚠️ 4 次同模式,但文档待办

### §31.7 AUTH_BASE_URL + adb reverse 修复(本日诊断 → 用户处理)

- 用户报"暂时无法连接江湖驿站"
- 诊断:`AUTH_BASE_URL = http://10.0.2.2:8010/`(模拟器专属 alias)
- 真机 `21908b7a` 用 `10.0.2.2` 不解析
- curl 测试:127.0.0.1:8010 返回 200 + 完整 token pair ✓(Docker jianghu-dev-api-1 0.0.0.0:8010->8000/tcp 健康)
- 修复(我做):
  1. `adb reverse tcp:8010 tcp:8010`(同时保留 tcp:8081)
  2. 改 [android/app/build.gradle.kts](android/app/build.gradle.kts) 三处 `10.0.2.2` → `127.0.0.1`(line 14/56/63)
- 用户后续:`export JAVA_HOME=C:/Users/28784/.jdks/jbr-21.0.11` + `./gradlew :app:assembleDebug` + `adb install -r` + 登录 13800138000 / Test1234!

### §31.8 当前状态(commit 前)

```
 M android/app/build.gradle.kts
 M android/app/src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt   (含 4 次 stray `}` 修复)
 M android/app/src/main/java/com/jueqiao/jianghu/nav/Routes.kt
 M android/app/src/main/java/com/jueqiao/jianghu/ui/screens/volume5part{1..7}/  (用户真机手动调整 Y=318→478)
 M android/app/src/test/java/com/jueqiao/jianghu/nav/RoutesTest.kt
?? volume5part8/ volume5part9/ volume5part10/ volume5part11/ volume5part12/  (5 个新目录)
?? img_volume5part{8,9,10,11,12}_*.png  (8 张 PNG)
```

### §31.9 累计未 commit 量统计

- Vol-5-8/9/10/11/12 五屏完整创建
- 8 张 PNG (image 398/399/400/401/01/4/4001/101/901 = 实际 9 张?查一下)
- 1 次 stray `}` 修复
- 1 处 build.gradle.kts URL 修改

### §31.10 本日沉淀新结论(添加到 §121 后的"重要建议")

11. **NavHost stray `}` 是高频 bug(本日 4 次),每次新增 composable 后必跑 brace check** — 已形成肌肉记忆
12. **`adb reverse tcp:8010` 需手动建**(模拟器 10.0.2.2 在真机不解析)
13. **gradle 不能在 Claude 自动模式下跑**(全程静态核验)— 用户自己 `./gradlew :app:assembleDebug`