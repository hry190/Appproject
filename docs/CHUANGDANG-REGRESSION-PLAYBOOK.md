# 闯荡江湖 · 真机回归 Playbook

> **创建于 2026-09-20**,起因:那套回归脚本原先入库(09-19 §25),但用户认为"辅助开发的脚本不该进产品仓库",
> 于是**脚本撤出 git、判据与踩坑留在这里**。本文是**纯文档**:讲清"用什么判据、按什么顺序、会踩什么坑",
> 脚本本体在本地 `scripts/chuangdang-regress/`(未入库)。
> 目的没变:把「我看截图觉得没问题」换成「有对照项、可复现的数字」。

## 0. 脚本在哪 / 丢了怎么取回

| 项 | 说明 |
|---|---|
| 本地位置 | `scripts/chuangdang-regress/`(17 个 `.py`)—— **不在 git**:2026-09-20 起 **`scripts/` 整个目录都不入库**,`.gitignore` 已排除 |
| 取回 | `git log --oneline -- scripts/chuangdang-regress` 找到最后一个含它的提交(如 `6714bf8`),<br>`git checkout 6714bf8 -- scripts/chuangdang-regress` |
| 为什么撤出 | 用户 2026-09-20 决定:**绑设备 / 一次性的辅助脚本不进产品仓库**(同一批还有 `capture-*.py`、`setup-reverse.ps1`、`audit-comment-drift.ps1`)。**知识留在本文,代码留在本地** —— 这也是本文存在的理由 |

## 1. 为什么需要它(而不是人眼看)

这套东西是被真实的坑逼出来的,几条判据值得先读:

| 场景 | 只靠肉眼会怎样 | 脚本怎么答 |
|---|---|---|
| 换了素材图 | "看着是同一只" | 五个模板各匹配一遍(**负对照**),正确的那张必须明显胜出 |
| 尺寸/比例 | "没变形吧" | 扫 0.90/0.95/**1.00**/1.05/1.10 × Fit,**最小值必须落在 1.00** |
| 文字被裁(§11/§12/§19) | "好像有点挤" | 连通域定位卡片 → 量墨迹到卡片边的留白,阈值 6px |
| 命中效果没了 | "好像没变" | 同一张素材**前后差分**(素材自身颜色会污染绝对值) |
| 圆形徽记切没切主体 | "看不出切" | **模型法**:源素材逐像素映射进徽记坐标,数出圆像素 |
| 降不透明度有没有生效 | "看不出差别" | 换读数:不看"红像素数",看**浓红**与平均红度(计数是不变量) |

## 2. 前置条件

| 项 | 说明 |
|---|---|
| 设备 | 真机一台(本机历史值 `21908b7a`),`adb devices` 可见 |
| 后端 | 需要登录态时先跑 `scripts/setup-reverse.ps1`(否则 app 冷启动跳登录页)|
| Python | 3.10+;依赖 `numpy` `Pillow` `scipy` |
| 分辨率 | 判据里的槽位坐标按 **1080×2400 / density 440(2.75)** 标定;换分辨率要重量一次(`verify_render.py` 会把坐标打出来)|

环境变量(都有默认值,一般不用设):

```powershell
$env:CD_SERIAL   = "21908b7a"     # 目标设备序列号
$env:CD_ADB      = "D:\Android\Sdk\platform-tools\adb.exe"
$env:CD_TEST_DIR = "$env:TEMP\cd-test"   # 产物根目录(默认在临时目录,**不落仓库**)
```

## 3. 跑一遍完整回归(顺序别换)

```powershell
# 0) 构建 + 装机 —— 装机即"重置进度"(ChuangdangStore 是进程内状态)
cd android; .\gradlew.bat assembleDebug
adb -s 21908b7a install -r app\build\outputs\apk\debug\app-debug.apk
adb -s 21908b7a shell am start -n com.jueqiao.jianghu/.MainActivity   # ★ 必须先把 app 拉起来

# 1) 素材源文件体检(不碰设备)
python scripts\chuangdang-regress\assets_check.py

# 2) 从头到尾自动过五关 + 逐帧成对采集(约 6 分钟 / 50 帧)
python scripts\chuangdang-regress\five_stage_run.py

# 3) 三层校验
python scripts\chuangdang-regress\verify_render.py      # 渲染层:负对照 + 尺寸正对照 + 像素一致性
python scripts\chuangdang-regress\verify_hits.py        # 命中效果:随命中数递增、崩落带落在石台上
python scripts\chuangdang-regress\clip2.py "$env:TEMP\cd-test\run" "$env:TEMP\cd-test\clip_report.txt"

# 4) 按需
python scripts\chuangdang-regress\verify_badges.py all4  # 地图页徽记(参数=mapcheck2 里的截图名)
python scripts\chuangdang-regress\map_shot.py start      # 先拍一张地图页
python scripts\chuangdang-regress\verify_marks.py        # 命中标记 A/B(带对照项)
python scripts\chuangdang-regress\boss_score.py          # Boss 评分规则的 Python 镜像(算分,不碰设备)
python scripts\chuangdang-regress\token_check.py         # 闯荡令余额(图标底色,文字读不出来)
python scripts\chuangdang-regress\resample_forensics.py  # 像素差溯源:滤波差异还是真的不一样
python scripts\chuangdang-regress\crop_evidence.py       # 生成人眼复核用的裁片
python scripts\chuangdang-regress\verify_monster.py 3    # 单独盯第 3 关的怪物(逐帧连拍)
python scripts\verify_fx.py                              # 交手动画·出手(进攻答对)
python scripts\verify_fx.py --takehit                    # 防御答错 → 熊猫受击
python scripts\verify_fx.py --block                      # 防御答对 → 熊猫挡一下
```

## 3.1 交手动画怎么验(2026-09-20 新增)

熊猫的出手 / 格挡 / 受击是**一次性** `Animatable`(160/110/120 ms)。110 ms 的动作
在真机上抓不到 —— 一次 `screencap` + `pull` 就要约 1.2 s。所以先把系统动画时长放大:

```powershell
adb shell settings put global animator_duration_scale 20   # 160ms → 3.2s
python scripts\verify_fx.py --block                        # 三条路各跑一次
adb shell settings put global animator_duration_scale 1    # 跑完必须还原(脚本自己也会还原)
```

**倍率就用 ×20,别贪大。** 放大倍率会**同时放大同屏的其它过渡**:实测答题后整个舞台
(熊猫 + 怪物一起)会有一次约 300ms 的淡入,×20 时 ~6s(还能容忍),**×60 时长达 18s**,
直接把格挡那 4.4s 的脉冲盖住(那次量到的"墨迹重心一动不动"就是被它盖的结果)。
另外脚本用的是 `adb exec-out screencap -p`(省掉"设备落盘+pull"),帧间隔 ~1.0~1.7s。

代码里**不要**再去读 `Settings.Global.ANIMATOR_DURATION_SCALE` 自己乘一遍时长:
Compose 的 `WindowRecomposer` 已经把这个倍率注入 `MotionDurationScale`,**所有 `tween` 都会被框架
自动缩放**(源码 `WindowRecomposer_androidKt` 读的就是 `animator_duration_scale`)。手动再乘 = 乘两遍,
生效倍率 = 倍率²(实测:系统 ×10 + 代码 ×10 → 一条 120+170ms 的动作在真机上放了 **29 秒**,
取证时被误读成"熊猫停不下来地漂")。系统倍率为 0 时框架把时长归一为 0,动作自然不播 —— 这就是无障碍要求。

**四条判据**(顺序即重要性):

| # | 判据 | 怎么量 | 通过线 |
|---|---|---|---|
| ① | 动作确实发生 | 出手:相对"回位后那一帧"的**变化区域 bbox** 右缘有没有被顶出去;格挡/受击:**模板匹配墨迹 x** + **墨迹重心 x** | 出手 14dp≈38.5px(+6% 放大)→ 实测 **+35px(12.7dp)**;格挡 +4dp=+11px → 实测 **+11px / 重心 +10.67px**;受击 −10dp=−27.5px → 实测 **−25px** |
| ② | 回位无残留 | 轨迹收尾那一帧的墨迹 x 与**匹配误差** | 应回到静息值(墨迹 x=54、误差 **1438**,与素材渲染结论里的静息基准相同)|
| ③ | **不破坏静态渲染** | 同一个界面**静止**两帧互比 | 熊猫窗口与怪物窗口都应 ≈ 0 px |
| ④ | 动画会结束 | 轨迹要**走完一个脉冲**并停在静息值上 | 稀疏 4 帧不够(看不到曲线),用 `TRACE_N=20` |

判据 ③ 是"加动画"这件事的**回归条件**:动画只在 `fx.value > 0` 时才挂
`graphicsLayer`(条件挂载而不是常挂),所以静息态与"加动画之前"逐像素一致。

⚠️ 两条量法上的坑(§8 都踩过):

1. **量受击不能用"窗口内任何变化像素的 bbox 左缘"当熊猫左缘** —— 那个 bbox 装的是背景、血条、
   滚动条的任意变化,背景一动就把左缘顶出去(一度量到 -25px 全是假的)。**用掩膜 SSD 匹配熊猫自己的墨迹。**
2. **位移要两把尺子一起用:模板匹配 x(绝对位置)+ 墨迹重心 x(对亮度免疫)。**
   同屏一旦有淡入淡出,匹配误差会被抬得很高而位置并没动;重心只对形状位置敏感。
   负对照写法:**不碰设备空转连拍 12 帧** —— 实测 `|d| = 0.00`(逐像素一模一样),
   所以"截着截着变暗了"绝不是采集噪声,得去代码/系统里找解释。

## 4. 每个脚本干什么

| 脚本 | 作用 | 关键判据 |
|---|---|---|
| `drive.py` | adb 封装:`dump`(带重试)/`shot`/`tap`/`find`;`OUT` 产物根 | 所有调用**必须 `-s <serial>`**(真机+模拟器同时在线的坑)|
| `qa.py` | 从 `ChuangdangData.kt` **解析**题库与答案(38 题)| 答案不手打 —— 错一个字脚本会静默卡死 |
| `play.py` | 自动游玩状态机(点选 / 排序 / 分类三种题型)| 先判"继续/返回地图"再判题干(结算面板也带题干)|
| `five_stage_run.py` | 全程过关 + 每帧落 `png + 同时刻 xml` + 记录当帧全部文本 | 产物 `%TEMP%\cd-test\run\`,索引 `frames.json` |
| `assets_check.py` | 素材**源文件**体检:尺寸 / alpha / 主体 bbox / 主体外碎片(水印)| 水印 = 落在主体包围盒之外的连通域碎片 |
| `verify_render.py` | 真机截图 vs 源文件:模板匹配定位 | 负对照(五张互比)+ 尺寸正对照(×Fit 扫描)+ `渲染≈a×素材+b` |
| `verify_hits.py` | 命中叠加层:红像素随命中数递增 | **差分**(同一张素材前后比),不用绝对值 |
| `clip2.py` | 文字贴边/裁剪检测(v2)| 卡片=够亮且中性;留白 < 6px 报警 |
| `verify_badges.py` | 地图页 40dp 徽记:直径 / 主体是否出圆 / 认得出是哪只 | **模型法**(源素材逐像素映射)判"切没切" |
| `verify_marks.py` | 贯穿痕 A/B,带"没改的那一路"当对照 | 降不透明度不改变"红像素数",要看**浓红**与平均红度 |
| `boss_score.py` | `cdScoreBoss` 的逐条 Python 镜像 | 先用历史面板读数验证镜像(8/100 能复现),再拿它反推能过线的草稿 |
| `boss_capture.py` | Boss 结果面板连拍 + 同时刻 dump | 中文要人手动输入(adb 输不进) |
| `token_check.py` | 闯荡令余额:图标底色采样(文字永远是"令")| 与 §6.1 规则对照(普通关连推不额外扣令)|
| `resample_forensics.py` | 像素差溯源:滤波器指纹 / 降采样收敛 / 偏差回归 / 亚像素位移 | 先排除**测量本身的误差**,再谈缺陷 |
| `crop_evidence.py` | 生成"真机裁片 / 期望模板 / 差异放大"三联图 | 数字只说哪里不对,**算不算问题要看图** |
| `verify_monster.py` | 单独一关连拍(按红色像素数事后挑帧)| 素材是实图时以看图为主 |
| `verify_fx.py`(在 `scripts\`,未入库)| 交手动画真机取证:放大倍率后**密集连拍**三条路(出手 / `--takehit` / `--block`)| 见 §3.1 四条判据;核心是 ③"静息态与加动画前逐像素一致" |

## 5. 产物在哪

全部落在 `%TEMP%\cd-test\`(**故意不落仓库**):

```
cd-test\
  run\            五关全程的 ~50 帧 png + 同时刻 xml + frames.json + run.log
  shots\          零散截图
  crops\          人眼复核用的裁片(含 A/B 并排图)
  mapcheck2\      地图页截图(verify_badges 的输入)
  boss_pass\      Boss 通过态取证
  fx\             交手动画连拍(verify_fx.py 的输出)
  assets_check.txt / render_check.txt / hits_check.txt / clip_report*.txt / resample_forensics.txt
```

## 6. ⚠️ 踩过的坑(照抄时最容易重犯)

1. **进度在进程内,装机即重置。** `ChuangdangStore` 不落库 —— "干净起点"靠重装;
   但**中途 `am force-stop` 会把刚打出来的进度清掉**(09-19 §17),要保留进度就别重启进程。
2. **跑之前必须先把 app 拉到前台。** 装完直接跑脚本 → 停在桌面 → `to_map()` 认不出界面,0 帧退出。
3. **别在"通关帧"上重新做模板匹配。** 通关态战斗区是素材+伤痕叠加,匹配会落到背景上;
   同一设备同一版式**槽位坐标是固定的**,用题目帧量出来的坐标。
4. **中文无法经 `adb shell input text` 输入**(MIUI 上 NPE;拼音输入法会吞掉注入按键)
   → Boss 的"通过态"结果面板只能靠**人手动输入**(09-19 §26)。
5. **判据本身要先被检验。** 没有正/负对照的"0 问题"没有意义(09-19 §11 的 v1 就是这么废掉的);
   数字看着对也要**去看那张图**。
6. **"工具报通过"要连它的覆盖数一起看。** 批量脚本改过文件行尾后,`audit-comment-drift.ps1`
   照样报"0 漂移",但配对从 559 掉到 165 —— 工具已经看不见了(09-20 §4)。
7. **幂等判断看"同一行",不要看固定窗口。** 「锚点后 60 字符内有 `img_` 就跳过」会误判跨行的情况(09-20 §4)。
8. **"逐像素必须为 0"往往不是可测量的判据。** 量动画回位时先写的是这条,实测不成立:
   常挂 `graphicsLayer` 的图层静止时也有 ~7.9k px 的 1px 级边缘抖动(同屏未挂的怪物是 **0**)。
   判据要改成**能量到的量**:变化区域的 bbox 有没有超出墨迹轮廓,容差取"静止两帧的噪声底"。
   → 反过来这也修了代码:图层改成**只在该动的时候挂**。
9. **别把系统的动画倍率乘第二遍。** Compose 框架已经按 `animator_duration_scale` 缩放所有动画
   (`WindowRecomposer_androidKt` → `MotionDurationScale`);代码里再乘一次,生效倍率就是倍率的平方
   (×10 → 一条 290ms 的动作真的放 29 秒)。**判据:先确认"框架有没有替你做这件事",再决定自己要不要做。**
10. **先证明"你量的那个东西就是你测的那个东西"。** 受击位移一度用"窗口内变化像素 bbox 的左缘"冒充
   熊猫左缘 —— 背景但凡有变化,量到的就是背景。换成**掩膜模板匹配熊猫墨迹**之后曲线立刻干净。
   同类:稀疏 4 帧看不出"脉冲还是斜坡",要看形状就得密集采样(20 帧)。
11. **放大倍率也会放大"同屏的其它过渡"。** 答题后整个舞台(熊猫 + 怪物一起)有一次 ~300ms 的淡入,
   ×20 时 ~6s(可容忍),**×60 时 18s** —— 直接把格挡那 4.4s 的脉冲盖住,量出"重心一动不动"的假结论。
   **判据:先用"不动的那把尺子"做负对照(空转连拍应逐像素一致),再决定倍率。**


## 7. 相关的入库文档

| 文档 | 内容 |
|---|---|
| [REGRESSION-chuangdang-5stages-20260919.md](REGRESSION-chuangdang-5stages-20260919.md) | 一次完整五关回归的**结果**(三层校验 15 项、1 处文档缺陷) |
| [SESSION-LOG-2026-09-19.md](SESSION-LOG-2026-09-19.md) §11/§23~§26 | 判据的来历:检测器重建、徽记、贯穿痕、通过态 |
| [SESSION-LOG-2026-09-20.md](SESSION-LOG-2026-09-20.md) | 本文档的由来(脚本撤出 git 的决定) |
| [DESIGN-SOURCES.md](DESIGN-SOURCES.md) | 设计稿来源对照(与本文无关,但同属"来源可查"这一类工程纪律) |
