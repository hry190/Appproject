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