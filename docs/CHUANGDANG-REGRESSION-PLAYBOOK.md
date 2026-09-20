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
```

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

## 5. 产物在哪

全部落在 `%TEMP%\cd-test\`(**故意不落仓库**):

```
cd-test\
  run\            五关全程的 ~50 帧 png + 同时刻 xml + frames.json + run.log
  shots\          零散截图
  crops\          人眼复核用的裁片(含 A/B 并排图)
  mapcheck2\      地图页截图(verify_badges 的输入)
  boss_pass\      Boss 通过态取证
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

## 7. 相关的入库文档

| 文档 | 内容 |
|---|---|
| [REGRESSION-chuangdang-5stages-20260919.md](REGRESSION-chuangdang-5stages-20260919.md) | 一次完整五关回归的**结果**(三层校验 15 项、1 处文档缺陷) |
| [SESSION-LOG-2026-09-19.md](SESSION-LOG-2026-09-19.md) §11/§23~§26 | 判据的来历:检测器重建、徽记、贯穿痕、通过态 |
| [SESSION-LOG-2026-09-20.md](SESSION-LOG-2026-09-20.md) | 本文档的由来(脚本撤出 git 的决定) |
| [DESIGN-SOURCES.md](DESIGN-SOURCES.md) | 设计稿来源对照(与本文无关,但同属"来源可查"这一类工程纪律) |
