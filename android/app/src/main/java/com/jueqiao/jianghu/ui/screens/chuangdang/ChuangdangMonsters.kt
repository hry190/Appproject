package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.jueqiao.jianghu.R

/**
 * 2026-09-19 §6 闯荡江湖 —— 五个敌人的形象
 *
 * **铜齿门卫已改用真实素材**(2026-09-19 §16,用户提供的 `D:\图\铜齿门卫.png`,
 * 落在 `res/drawable-nodpi/img_chuangdang_tongchimenwei.png`);其余四个仍是
 * **Compose Canvas 手绘矢量**。
 *
 * 当初全用手绘的理由(§6)是"现成怪物素材很难找到**风格匹配且授权干净**的" ——
 * 用户自己提供了风格匹配、版权自有的图,这条理由对第 1 关不再成立;
 * 其余四个没有素材,继续手绘(零素材依赖、体积可忽略、可随状态做简单动画)。
 *
 * 手绘部分的设计原则:**以线描为主、淡墨为辅**,与界面里其他水墨元素一致;
 * 每个敌人用一个可辨识的轮廓,而不是写实插画 —— 尺寸完全由调用方决定,内部按
 * `min(width, height)` 归一化,所以在任意大小下比例一致。
 *
 * 五个敌人(策划方案 §5):
 *   盾 = 铜齿门卫 —— **真实素材**(走兽 + 铜齿 + 铜环 + 铜杖)
 *   蝠 = 断目机关蝠 —— **真实素材**(机关翼 + 铜齿轮 + 铜钥匙)
 *   棋 = 棋冠石将(黑白石阶 + 棋冠)
 *   鹤 = 百声纸鹤(符纸铃铛 + 折纸鹤)
 *   枢 = 百面机枢(悬空核心 + 无数张宣称"绝不会错"的面具)
 */

/**
 * 已有**真实素材**的敌人:**五个已全部换成素材**(§16 铜齿门卫、§17 断目机关蝠、§18 棋冠石将、
 * §20 百声纸鹤、§22 百面机枢)。手绘分支保留 —— 它是未知字形的兜底,也是这些素材的来处。
 *
 * 再加素材只需在这里补一行 —— 渲染与命中效果都由 [CdMonster] 统一处理,不必再写 if 分支。
 * 素材一律走 `ContentScale.Fit`,所以**方形 / 横构图 / 竖构图都能用**:槽位由调用方给,
 * 长宽比不一致时居中留白而不是拉伸:
 *   战斗页槽位 132×96dp → 1:1 出 96×96 · 3:2 出 132×88 · 1.176:1 出 113×96 · 0.774:1 出 74×96
 *   Boss 页槽位 64dp 方形 → 0.941:1 出 60×64
 */
private val CD_MONSTER_ART: Map<String, Int> = mapOf(
    "盾" to R.drawable.img_chuangdang_tongchimenwei,
    "蝠" to R.drawable.img_chuangdang_duanmujiguanfu,
    "棋" to R.drawable.img_chuangdang_qiguanshixiang,
    "鹤" to R.drawable.img_chuangdang_baishengzhihe,
    "枢" to R.drawable.img_chuangdang_baimianjishu,
)

/**
 * 字形 → 真实素材资源;**没有素材时返回 null**,调用方走文字 / 手绘兜底。
 *
 * 2026-09-19 §23 开的这个口子:地图页的圆形关卡徽记原先画的是字形文字
 * (§16 当时留着它的理由是"只换一个会和其余四个不一致"),五个敌人都有素材后这条理由不成立,
 * 徽记也改用素材。**素材表仍然只此一份**,加素材依旧只改上面那一行。
 */
fun cdMonsterArtRes(glyph: String): Int? = CD_MONSTER_ART[glyph]

@Composable
fun CdMonster(
    glyph: String,
    /**
     * 已被打掉的心数(0~3)。
     *
     * 用于表现策划方案 §5 每关的「战斗表现」:
     *   铜齿门卫 —— 每命中一次身上多一道**爪痕**(素材版,见 [drawDamageMarks])
     *   断目机关蝠 —— 每命中一次多一圈**声波环**(素材版,见 [drawSonarRings])
     *   棋冠石将 —— 每识破一个越界判断,脚下的一块错误棋格便**崩落**
     *   百声纸鹤 —— 合理样本让纸鹤恢复判断,**逐只脱离阵形**
     */
    hitCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    // 有真实素材的敌人:素材 + 一层命中效果叠加
    //   尺寸完全交给调用方(modifier 里的 size),这里只用 ContentScale.Fit 保住比例。
    val art = CD_MONSTER_ART[glyph]
    if (art != null) {
        Box(modifier = modifier) {
            Image(
                painter = painterResource(art),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Canvas(modifier = Modifier.fillMaxSize()) { drawHitEffect(glyph, hitCount) }
        }
        return
    }

    Canvas(modifier = modifier) {
        when (glyph) {
            "棋" -> drawChessGeneral(hitCount)
            "鹤" -> drawPaperCrane(hitCount)
            else -> drawManyFaceCore()
        }
    }
}

/**
 * 有素材的敌人各自的「战斗表现」叠加层(文档 §5)。
 *
 * 手绘敌人的表现画在**同一个 Canvas** 里(棋格崩落、纸鹤离阵),因为它们本来就是整体绘制;
 * 有素材的敌人则只能"素材 + 叠加层",故效果单独成函数。
 */
private fun DrawScope.drawHitEffect(glyph: String, hitCount: Int) {
    when (glyph) {
        "盾" -> drawDamageMarks(hitCount)   // 每命中一次多一道爪痕
        "蝠" -> drawSonarRings(hitCount)    // 每命中一次多一圈声波环
        "棋" -> drawCollapsingPlatform(hitCount)  // 每命中一次脚下石台崩掉一块
        // 2026-09-19 §20:鹤复用通用受损标记。
        //   §5 对第 4 关写的是「逐只脱离阵形」—— 那需要**多只纸鹤**的画面,
        //   而用户给的素材只有**一只**纸鹤,"阵形"根本不存在。
        //   按 §16/§17 的既有做法:**不假装画没有的东西**,退回通用受损标记。
        // 2026-09-19 §24:满三心那两道贯穿痕改用半透明 —— 纸鹤身上颜色浅,
        //   不透明的粗红杠压上去像"贴了两条胶带"(用户看过真机后定的)。盾是深色走兽,
        //   同样的线不刺眼,故**只对鹤改成半透明**,不动盾那一路。
        "鹤" -> drawDamageMarks(hitCount, faintThroughMarks = true)
    }
}

// ── 配色(水墨 + 一点金属色)────────────────────────────────────────────────
private val Ink = Color(0xFF2E2A24)
private val InkMid = Color(0x882E2A24)
private val InkFaint = Color(0x332E2A24)
private val Bronze = Color(0xFFB8894A)
private val BronzeFaint = Color(0x55B8894A)
private val Paper = Color(0xFFF2E8D5)
private val Danger = Color(0xFF9B3B2E)

/**
 * 半透明的"受损"红(2026-09-19 §18)。
 *
 * 素材版敌人的命中叠加层用这个:96dp 的显示尺寸下,纯 [Danger] 会显得像贴上去的色块,
 * 降一点不透明度才读得出"损伤"而不是"装饰"。
 */
private val DamageFaint = Color(0xB39B3B2E)

/** 归一化:把 0~1 的比例坐标换算成画布坐标(以较短边为基准,居中)。 */
private fun DrawScope.px(fx: Float, fy: Float): Offset {
    val m = size.minDimension
    val ox = (size.width - m) / 2f
    val oy = (size.height - m) / 2f
    return Offset(ox + fx * m, oy + fy * m)
}

/**
 * 画一条线。
 *
 * ⚠️ [w] 是**相对画布短边的比例**,不是像素值 —— 内部会乘以 `size.minDimension`。
 * 传绝对值(如 `m * 0.026f`)会让线宽变成 m² 量级,画出一个占满屏幕的巨块。
 */
private fun DrawScope.line(a: Offset, b: Offset, color: Color = Ink, w: Float = 0.035f) =
    drawLine(color, a, b, strokeWidth = w * size.minDimension)

/**
 * 「战斗表现」的通用伤痕(2026-09-19 §16)。
 *
 * 原 Canvas 版的铜齿门卫是「齿轮外圈 + 铜盾」,策划方案 §5 对应的文案是
 * "长盾刻字逐步开裂";换成用户提供的真实素材后形象变成一只走兽,**盾不存在了** ——
 * 所以这里不再假装在画盾,改为**通用爪痕**:每打掉一颗心多一道折线
 * (左右交替,避免看着像同一道),打满三心再追加两道贯穿痕。
 *
 * 保留的是行为本身(**形象随命中数变化**),不是某一种具体画法。
 *
 * ⚠️ 调用方的画布必须**与素材对齐**(见 [CdMonster] 里同为 `fillMaxSize` 的方形槽位):
 *    本函数按 `size.minDimension` 归一化,画布偏了伤痕就会落在空处。
 *
 * @param faintThroughMarks 满三心追加的两道贯穿痕是否改用半透明 [DamageFaint]。
 *   2026-09-19 §24:纸鹤身体是**浅色的**,不透明粗红杠压上去像"贴了两条胶带";
 *   盾是深色走兽,同样的线不刺眼 —— 所以这档只在鹤那一路打开。
 */
private fun DrawScope.drawDamageMarks(hitCount: Int, faintThroughMarks: Boolean = false) {
    if (hitCount <= 0) return
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = m * 0.30f

    repeat(hitCount) { i ->
        val dir = if (i % 2 == 0) -1f else 1f
        val y0 = c.y - r * 0.34f + i * r * 0.28f
        val a = Offset(c.x + dir * r * 0.42f, y0)
        val b = Offset(c.x + dir * r * 0.06f, y0 + r * 0.16f)
        val d = Offset(c.x + dir * r * 0.28f, y0 + r * 0.36f)
        line(a, b, Danger, 0.026f)
        line(b, d, Danger, 0.026f)
    }
    // 三心打完 → 两道贯穿痕
    if (hitCount >= CD_MAX_HEARTS) {
        val tone = if (faintThroughMarks) DamageFaint else Danger
        line(Offset(c.x - r * 0.42f, c.y - r * 0.52f), Offset(c.x + r * 0.28f, c.y + r * 0.72f), tone, 0.032f)
        line(Offset(c.x + r * 0.42f, c.y - r * 0.40f), Offset(c.x - r * 0.26f, c.y + r * 0.72f), tone, 0.032f)
    }
}

/**
 * 「战斗表现」的声波环(2026-09-19 §17)。
 *
 * 策划方案 §5 对第 2 关写的是「正确流程让**声波沿指定路线反弹**,击中机关蝠弱点」。
 * 原 Canvas 版把"弱点"取在自己画的「完好的左眼」上;换成用户提供的真实素材后
 * **眼位由画师决定,代码不该去猜** —— 故改为以**素材中心为圆心**扩散:
 * 语义仍是"声波命中它",但不再依赖任何具体五官坐标。
 *
 * ⚠️ 与 [drawDamageMarks] 同理:调用方的画布必须与素材对齐(同为 `fillMaxSize`),
 *    否则圆环会落在偏处。
 */
private fun DrawScope.drawSonarRings(hitCount: Int) {
    if (hitCount <= 0) return
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    repeat(hitCount) { i ->
        drawCircle(
            Danger,
            radius = m * (0.11f + i * 0.075f),
            center = c,
            style = Stroke(width = m * 0.014f),
        )
    }
    // 命中标记:中心一颗点亮的红点
    drawCircle(Danger, radius = m * 0.022f, center = c)
}

/**
 * 「战斗表现」:脚下石台崩落(2026-09-19 §18)。
 *
 * 策划方案 §5 对第 3 关写的是「每识破一个越界判断,石将脚下的一块错误棋格便**崩落**」。
 * 与 §16/§17 不同,这次**可以照原意画** —— 因为素材里石像正站在一座**阶梯石台**上,
 * "脚下有台"是画里真实存在的;只是把"棋格"落成"石台的一块"。
 *
 * ⚠️ **必须按素材实际占位定位,不能按槽位。** 素材是 3:4 竖构图,`Fit` 进 132×96 的槽位后
 *    只占中间 72×96(左右各留 30dp)。若按槽位归一化,效果会横向偏出素材、落在空白处。
 *    石台在素材高度的 **62%~84%**、宽度的 **17%~84%**(实测自这张素材)。
 */
private fun DrawScope.drawCollapsingPlatform(hitCount: Int) {
    if (hitCount <= 0) return

    // 素材是 0.774:1 的竖构图(§21 裁掉水印后实测)—— `Fit` 进 132×96 槽位时
    //   高度铺满、宽度 = 高度 × 0.774、水平居中。
    val artH = size.height
    val artW = artH * 0.774f
    val artX = (size.width - artW) / 2f

    // 石台位置是**实测值**,不是眼估:逐行看不透明宽度 —— 腿只有 560 宽,
    //   到 y = 0.661H 处跳到 1100+,那就是石台顶边;最宽处 x 占 0.09~0.91。
    //   ⚠️ 素材被裁过(§21 去水印),旧的百分比(0.62/0.85、0.18/0.86)已**不成立** ——
    //      改素材后必须重量一次,否则效果会落在石像身上而不是石台上。
    val plateTop = artH * 0.661f
    val plateBottom = artH * 0.910f
    val plateL = artX + artW * 0.09f
    val plateR = artX + artW * 0.91f
    val plateW = plateR - plateL

    repeat(hitCount) { i ->
        // 沿台沿**自左向右**依次崩掉一块(越早崩的越靠左),铺开到整个台面
        val cx = plateL + plateW * (0.14f + 0.26f * i)
        val halfW = plateW * 0.05f
        val depth = plateTop + (plateBottom - plateTop) * 0.6f

        // 崩口:一个**实心**向下的楔形。
        //   首版画的是两条收拢的线 + 悬空方框 → 真机放大后读成"一根红棍子";
        //   第二版楔形又太大太实,像两个红锥子盖住了石像的脚 —— 故收小并降低不透明度,
        //   让它在 96dp 的显示尺寸下"看得见但不抢戏"。
        val notch = Path().apply {
            moveTo(cx - halfW, plateTop)
            lineTo(cx + halfW, plateTop)
            lineTo(cx + halfW * 0.15f, depth)
            close()
        }
        drawPath(notch, DamageFaint)

        // 坠落的小碎块:越早崩的掉得越低
        val fy = depth + artH * 0.008f + i * artH * 0.026f
        drawRect(
            DamageFaint,
            topLeft = Offset(cx - halfW * 0.45f, fy),
            size = Size(halfW * 0.9f, artH * 0.018f),
        )
    }
}

/** ③ 棋冠石将 —— 石制身躯 + 棋冠,胸前一方棋盘。 */
private fun DrawScope.drawChessGeneral(hitCount: Int) {
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val bodyTop = c.y - m * 0.10f
    val bodyW = m * 0.30f
    val bodyH = m * 0.42f

    // 石身(略带收腰的方柱)
    val body = Path().apply {
        moveTo(c.x - bodyW, bodyTop)
        lineTo(c.x + bodyW, bodyTop)
        lineTo(c.x + bodyW * 0.86f, bodyTop + bodyH)
        lineTo(c.x - bodyW * 0.86f, bodyTop + bodyH)
        close()
    }
    drawPath(body, Paper)
    drawPath(body, Ink, style = Stroke(width = m * 0.026f))

    // 胸前棋盘(呼应「棋王」)
    val gx = c.x - bodyW * 0.52f
    val gy = bodyTop + m * 0.10f
    val gs = m * 0.055f
    for (r in 0..2) {
        for (col in 0..2) {
            if ((r + col) % 2 == 0) {
                drawRect(
                    InkFaint,
                    topLeft = Offset(gx + col * gs, gy + r * gs),
                    size = Size(gs, gs),
                )
            }
        }
    }
    drawRect(Ink, topLeft = Offset(gx, gy), size = Size(gs * 3, gs * 3), style = Stroke(width = m * 0.016f))

    // 棋冠:冠底 + 两个尖角 + 顶珠
    val crownY = bodyTop - m * 0.005f
    drawRect(
        Bronze,
        topLeft = Offset(c.x - bodyW * 0.92f, crownY - m * 0.075f),
        size = Size(bodyW * 1.84f, m * 0.055f),
    )
    val crown = Path().apply {
        moveTo(c.x - bodyW * 0.62f, crownY - m * 0.075f)
        lineTo(c.x - bodyW * 0.30f, crownY - m * 0.20f)
        lineTo(c.x, crownY - m * 0.095f)
        lineTo(c.x + bodyW * 0.30f, crownY - m * 0.20f)
        lineTo(c.x + bodyW * 0.62f, crownY - m * 0.075f)
        close()
    }
    drawPath(crown, Paper)
    drawPath(crown, Bronze, style = Stroke(width = m * 0.024f))
    drawCircle(Bronze, radius = m * 0.022f, center = Offset(c.x, crownY - m * 0.235f))

    // 眼:两点冷光
    drawCircle(Ink, radius = m * 0.022f, center = Offset(c.x - m * 0.075f, bodyTop + m * 0.055f))
    drawCircle(Ink, radius = m * 0.022f, center = Offset(c.x + m * 0.075f, bodyTop + m * 0.055f))

    // 文档 §5 战斗表现:「每识破一个越界判断,石将脚下的一块错误棋格便崩落」
    //   脚下铺 3×3 石台,按命中数从左到右逐块挖空(不画的即"已崩落")。
    val plateY = bodyTop + bodyH + m * 0.03f
    val ps = m * 0.072f
    for (row in 0..2) {
        for (col in 0..2) {
            if (row * 3 + col < hitCount) continue
            val px0 = c.x + (col - 1.5f) * ps
            val py0 = plateY + row * ps * 0.42f
            drawRect(Paper, topLeft = Offset(px0, py0), size = Size(ps * 0.9f, ps * 0.36f))
            drawRect(
                InkMid,
                topLeft = Offset(px0, py0),
                size = Size(ps * 0.9f, ps * 0.36f),
                style = Stroke(width = m * 0.010f),
            )
        }
    }
}

/** ④ 百声纸鹤 —— 折纸质感的鹤,旁边挂一排声音铃铛。 */
private fun DrawScope.drawPaperCrane(hitCount: Int) {
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)

    // 折纸身体(两个三角构成)
    val body = Path().apply {
        moveTo(c.x - m * 0.02f, c.y - m * 0.02f)
        lineTo(c.x + m * 0.30f, c.y + m * 0.10f)
        lineTo(c.x - m * 0.04f, c.y + m * 0.24f)
        close()
    }
    drawPath(body, Paper)
    drawPath(body, Ink, style = Stroke(width = m * 0.026f))

    // 折线(纸感)
    line(Offset(c.x - m * 0.02f, c.y - m * 0.02f), Offset(c.x - m * 0.04f, c.y + m * 0.24f), InkMid, 0.016f)

    // 尾羽
    val tail = Path().apply {
        moveTo(c.x - m * 0.04f, c.y + m * 0.24f)
        lineTo(c.x - m * 0.30f, c.y + m * 0.34f)
        lineTo(c.x - m * 0.22f, c.y + m * 0.16f)
        close()
    }
    drawPath(tail, Paper)
    drawPath(tail, Ink, style = Stroke(width = m * 0.024f))

    // 长颈与头
    line(Offset(c.x + m * 0.05f, c.y + m * 0.01f), Offset(c.x + m * 0.06f, c.y - m * 0.24f), Ink, 0.026f)
    line(Offset(c.x + m * 0.06f, c.y - m * 0.24f), Offset(c.x + m * 0.20f, c.y - m * 0.30f), Ink, 0.026f)
    drawCircle(Paper, radius = m * 0.035f, center = Offset(c.x + m * 0.07f, c.y - m * 0.245f))
    drawCircle(Ink, radius = m * 0.035f, center = Offset(c.x + m * 0.07f, c.y - m * 0.245f), style = Stroke(width = m * 0.020f))
    drawCircle(Danger, radius = m * 0.014f, center = Offset(c.x + m * 0.075f, c.y - m * 0.252f))

    // 翅膀折面
    val wing = Path().apply {
        moveTo(c.x + m * 0.02f, c.y + m * 0.02f)
        lineTo(c.x + m * 0.04f, c.y - m * 0.14f)
        lineTo(c.x + m * 0.22f, c.y + m * 0.02f)
        close()
    }
    drawPath(wing, BronzeFaint)
    drawPath(wing, Ink, style = Stroke(width = m * 0.022f))

    // 声音铃铛(三枚,大小递减 —— 对应"只听懂一种口音")
    val bellXs = listOf(-0.36f, -0.28f, -0.20f)
    val bellR = listOf(0.034f, 0.028f, 0.022f)
    for (i in 0..2) {
        val p = Offset(c.x + bellXs[i] * m, c.y - m * 0.18f + i * m * 0.03f)
        drawCircle(Bronze, radius = m * bellR[i], center = p)
        drawCircle(Ink, radius = m * bellR[i], center = p, style = Stroke(width = m * 0.016f))
        line(Offset(p.x, p.y - m * 0.05f), Offset(p.x, p.y - m * bellR[i]), InkMid, 0.014f)
    }

    // 文档 §5 战斗表现:「合理样本让纸鹤恢复判断,逐只脱离阵形」
    //   右侧原本是三只编队的小鹤;每答对一次,最靠前的一只脱离阵形、飞到右上角变淡。
    for (i in 0..2) {
        val left = i >= hitCount                       // 仍在阵形里
        val fx = c.x + m * (if (left) 0.30f + i * 0.055f else 0.52f + i * 0.07f)
        val fy = c.y + m * (if (left) -0.02f - i * 0.05f else -0.22f - i * 0.09f)
        val s = m * (if (left) 0.050f else 0.028f)
        val body = Path().apply {
            moveTo(fx, fy)
            lineTo(fx + s, fy + s * 0.60f)
            lineTo(fx + s * 0.10f, fy + s)
            close()
        }
        if (left) drawPath(body, InkFaint)
        drawPath(body, if (left) InkMid else BronzeFaint, style = Stroke(width = m * 0.010f))
        // 翼线:脱离的那几只翅膀张开得更大
        val wingUp = if (left) s * 0.55f else s * 1.10f
        line(Offset(fx, fy), Offset(fx + s * 0.35f, fy - wingUp), if (left) InkMid else Bronze, 0.010f)
    }
}

/** ⑤ 百面机枢 —— 悬空核心 + 环列的多张面具,有的裂开。 */
private fun DrawScope.drawManyFaceCore() {
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val coreR = m * 0.16f

    // 环列面具(5 张,用短线连到核心)
    val faces = listOf(
        Triple(-0.36f, -0.16f, 0.9f),
        Triple(-0.22f, 0.28f, 1.0f),
        Triple(0.30f, 0.24f, 0.95f),
        Triple(0.38f, -0.14f, 1.0f),
        Triple(0.02f, -0.40f, 0.85f),
    )
    for ((fx, fy, fs) in faces) {
        val p = Offset(c.x + fx * m, c.y + fy * m)
        val fr = m * 0.095f * fs
        drawCircle(InkFaint, radius = fr, center = p)
        drawCircle(Ink, radius = fr, center = p, style = Stroke(width = m * 0.020f))
        // 面具的眼(两竖缝)
        drawRect(Ink, topLeft = Offset(p.x - fr * 0.42f, p.y - fr * 0.16f), size = Size(fr * 0.24f, fr * 0.42f))
        drawRect(Ink, topLeft = Offset(p.x + fr * 0.18f, p.y - fr * 0.16f), size = Size(fr * 0.24f, fr * 0.42f))
        // 嘴(一张在宣称"绝不会错")
        drawLine(InkMid, Offset(p.x - fr * 0.30f, p.y + fr * 0.42f), Offset(p.x + fr * 0.30f, p.y + fr * 0.42f), strokeWidth = m * 0.014f)
        // 引线
        drawLine(Bronze, p, c, strokeWidth = m * 0.012f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(m * 0.03f, m * 0.025f)))
    }

    // 核心
    drawCircle(Paper, radius = coreR, center = c)
    drawCircle(Bronze, radius = coreR, center = c, style = Stroke(width = m * 0.030f))
    repeat(8) { i ->
        rotate(degrees = i * 45f, pivot = c) {
            drawLine(Bronze, Offset(c.x, c.y - coreR), Offset(c.x, c.y - coreR * 0.55f), strokeWidth = m * 0.020f)
        }
    }
    drawCircle(Ink, radius = coreR * 0.30f, center = c)

    // 分歧的判断(核心两侧的矛盾箭头)
    line(Offset(c.x - coreR * 2.6f, c.y), Offset(c.x - coreR * 1.5f, c.y - coreR * 0.5f), Danger, 0.018f)
    line(Offset(c.x + coreR * 2.6f, c.y), Offset(c.x + coreR * 1.5f, c.y + coreR * 0.5f), InkMid, 0.018f)
}
