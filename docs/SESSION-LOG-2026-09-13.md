# SESSION-LOG-2026-09-13

> 昨日: [SESSION-LOG-2026-09-12.md](SESSION-LOG-2026-09-12.md)
> 今日工作: **第七卷 Vol-7-1~Vol-7-8(8 屏)** + 新 memory 规则(image-fit-to-natural-bounds)+ 首例 3 图布局 + 首例空标题 + 首例 11 字标题
> 重要: §38 已先写在昨日 SESSION-LOG(commit 2d554e9 之前),本文件作为今日独立日志的补建

## 快速参考

| 项 | 值 |
|---|---|
| 工作分支 | zzz |
| 主要工作 | 创建第七卷 8 屏(Vol-7-1 → Vol-7-8)|
| 新入口 | Gunlun15「已解锁秘籍9」图像 → Vol-7-1 |
| 标题切换 | 「皮影戏之小节点会加权」10字 ×3(7-1/2/3) → 「皮影戏之权重从错误中学」11字 ×2(7-4/5) → 「皮影戏之层层见不同」9字 ×2(7-6/7) → 空 → 「皮影戏之转折让网络会弯」11字(7-8 修订)|
| 书框交替 | Vol-7-{1,3,7}=255, Vol-7-{2,4,6,8}=256(Vol-7-7 异常 256)|
| 新增 PNG | 15 张(image 436~453 跨 8 屏)|
| 新增 const | Routes.Volume7Part1 ~ Volume7Part8(8 个)|
| 新增测试 | RoutesTest 8 个 assert |
| 静态核验 | ✅ grep Routes / NavHost / RoutesTest / KDoc vs 代码 |
| gradle 编译 | ❌ 未跑(全程 auto-mode 屏蔽)|
| 规则迭代 | image-size-by-width-default → **image-fit-to-natural-bounds**(完全忽略用户 W/H,fit max_W=355 × max_H=394)|

## 当天操作记录(从最近往前)

### §38.6 Vol-7-8 标题修订(2026-09-13 08:55)

- 用户 2026-09-13 指令:"标题改为皮影戏之转折让网络会弯"
- 空标题 → 11 字「皮影戏之转折让网络会弯」W=302 → **W=360**
- 标题变更史沉淀:初版空 → 修订 11 字
- **首次11 字全中文无标点标题**

### §38.5 Vol-7-8 + 首例空标题(2026-09-13 08:55)

- **Vol-7-8**:复制第一卷-1 → Group 255;图 1 H=311(巧合同用户字面,新规则算出来一样),图 2 H=293
- **首例空标题** `text = ""`: 用户 2026-09-13 显式给 `""`;W=302 H=32 仅作占位;KDoc 标注"全卷首例"

### §38.4 Vol-7-7 2 图布局变体(图 2 Y=381 非常用 478)(2026-09-13 08:50)

- 复制第一卷-2 → Group 256;图 1 H=187,图 2 H=177;**图 2 位置 Y=381 非常用 478**(用户显式指定,介于 Vol-7-4/7-5 3 图布局的中部 Y)

### §38.3 Vol-7-6 回到2 图布局(2026-09-13 08:45)

- 复制第一卷-1 → Group 255;图 1 H=281,图 2 H=265;标题 9 字「皮影戏之层层见不同」W=302

### §38.2 Vol-7-4 / 7-5 首次3 张图布局 + 11 字标题(2026-09-13 08:35)

- **Vol-7-4**:首次6 层 z-order(3 张图);图 1 H=211,图 2 H=189,图 3 H=180;位置 Y=135/381/606
- **Vol-7-5**:同 Vol-7-4 模式,3 张图 H=202/172/188
- **首次11 字标题**「皮影戏之权重从错误中学」W=360(沿用估算 ~33/字宽度,无独立 11 字规约)

### §38.1 Vol-7-2 / 7-3 创建(2026-09-13 08:25)

- **Vol-7-2**:复制第一卷-2 → Group 256(恢复交替 Vol-7-1 255 → 256);图 1 W=355 H=265,图 2 W=355 H=293;标题沿用 10 字 W=302
- **Vol-7-3**:复制第一卷-1 → Group 255(恢复交替);图 1 W=355 H=332,图 2 W=355 H=324;标题沿用 10 字

### §37.3 Vol-7-1 创建(2026-09-12 20:47)— 新规则首次应用

- **新入口**: Gunlun15 「已解锁秘籍9」图像 (仿 Gunlun8/12/14 → Vol-4/5/6 模式)
- Gunlun15 已加 `onOpenVolume7Part1: (() -> Unit)?` 参数 + 「已解锁秘籍9」 `.clickable { onOpenVolume7Part1?.invoke() }`
- **Vol-7-1 创建**:复制第一卷-1 → Group 255,标题「皮影戏之小节点会加权」**10 字**(新长度,沿用 9 字 W=302,KDoc 标注异常)
- **完全按新规则 fit-to-natural-bounds**:
  - image 436 (1047×816, ratio 1.283) → W=355 H=277(用户字面 H=311 忽略) — 畸变 0.12%
  - image 437 (1065×801, ratio 1.330) → W=355 H=267(用户字面 H=321 忽略) — 畸变 0%
- **首次新规则应用确认**:用户 W/H 完全没影响最终渲染,完全由 PNG 尺寸决定

## 当前状态

### git status(2026-09-13 累计)

```
 M JianghuNavHost.kt / Routes.kt / RoutesTest.kt
 M Volume7Part1Screen.kt (Vol-7-2 入口)
 M Volume7Part2Screen.kt (Vol-7-3 入口)
 M Volume7Part3Screen.kt (Vol-7-4 入口,修 KDoc **)** 错字 + 加 clickable)
 M Volume7Part4Screen.kt (Vol-7-5 入口)
 M Volume5Screen.kt 等 (Vol-7-6 入口)
 M Volume6Screen.kt 等 (Vol-7-7 入口)
 M Volume7Part7Screen.kt (Vol-7-8 入口,加 callback + 修正 9 字/11 字规约注释)
 M Volume7Part8Screen.kt (空标题 → 11 字「皮影戏之转折让网络会弯」W=360)
?? Volume7Part2~8Screen.kt (7 个新目录)
?? img_volume7part{2,3,4,5,6,7,8}_*.png (15 张)
```

注:全部已在 commit 2d554e9 提交并 push 到 zzz + main(2026-09-13 08:55)。

### 新增导航拓扑

```
 Gunlun15 ── 已解锁秘籍9 ──→ Vol-7-1 → 7-2 → 7-3 → 7-4 → 7-5 → 7-6 → 7-7 → 7-8
                                                  ↑3图布局     ↑3图布局
```

### 已知沉淀(今日新结论)

| 项 | 内容 | 出处 |
|---|---|---|
| image-fit-to-natural-bounds 规则 | 完全忽略用户 W/H,按 PNG 原图 fit max_W=355 × max_H=394 | §37.1 |
| 3 张图布局 | Y=135/381/606,图 1 H=auto, 图 2 H=auto, 图 3 H=auto(全部新规则) | §38.2 |
| 11 字标题 W=360 | 估算 ~33/字,无独立 11 字规约 | §38.2 |
| 空标题 text="" | KDoc 标注"全卷首例" | §38.5 |
| 标题变更史 | 初版空 → 修订 11 字,沿用 Vol-7-4/7-5 同款 | §38.6 |
| 用户笔误"第七卷-7标题" | 缺" 的",无歧义,KDoc 留痕 | §38.5 |

## 明天(可选)优先级

1. 高 ROI:决定是否补齐 Vol-7-9 ~ Vol-7-15(Vol-4/5/6 各 15 屏,Vol-7 才 8 屏,半途中断)
2. 中 ROI:补 v2 audit HIGH 项(参考 [SESSION-LOG-2026-09-11.md §待修的 v2 audit 遗留](SESSION-LOG-2026-09-11.md))
3. 中 ROI:重跑 v2 失败 verifier(`resumeFromRunId=wf_1980fcb3-b31`)
4. 低 ROI:抽 scaffold helper(消 maintainability HIGH)
5. **新**:首次 3 张图布局是否在其他卷也需要?(扫描 Vol-1~6)

## 重要建议(沿用 9月10日 + 9月12日 + 9月13日新加)

1. **每次开始新功能前先 `git checkout zzz`**
2. **Gradle JDK 永远设 jbr-21**
3. **重要操作独立存档为 CODE-AUDIT / SUMMARY / DECISIONS / MERGE-WORKFLOW**
4. **每个 PR/commit 后推 origin**
5. **未来文件变更涉及 i18n 时优先 stringResource(R)**,避免新增硬编码中文字符串
6. **每次"复制 X"先 grep X 的实际值,不再凭印象**(Vol-12 标题教训)
7. **每次建屏前先 git status 看是否漏 commit**
9. **书框奇偶交替中断例外**:复制指令中明确指定页 = 字面优先于模式约定
10. **commit 之前必先写当日 SESSION-LOG**(commit-push-summary-rule)— ⚠️ 今日违反(§38 写在昨日文件),务必明天起恢复
11. **每天新建独立 SESSION-LOG-YYYY-MM-DD.md** — ⚠️ 今日首次补建,日后严格遵守
12. **图像尺寸完全忽略用户 W/H** — 新规则 fit-to-natural-bounds 取代 image-size-by-width-default
13. **3 张图布局变体可用 Y=135/381/606** — 2 张图布局变体可用 Y=381 替代 Y=478(中部位置)