package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.compose.foundation.Canvas
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

/**
 * 2026-09-19 §6 闯荡江湖 —— 五个敌人的形象
 *
 * 用 **Compose Canvas 手绘矢量**表达(不引入外部素材):
 *   · 项目是水墨江湖风格,现成怪物素材很难找到风格匹配且授权干净的;
 *   · 矢量绘制没有版权与体积负担,还能随状态做简单动画(如齿轮转动、蝠翼扇动)。
 *
 * 设计原则:**以线描为主、淡墨为辅**,与界面里其他水墨元素一致;
 * 每个敌人用一个可辨识的轮廓,而不是写实插画 —— 尺寸完全由调用方决定,内部按
 * `min(width, height)` 归一化,所以在任意大小下比例一致。
 *
 * 五个敌人(策划方案 §5):
 *   盾 = 铜齿门卫(齿轮城门 + 刻满固定规则的铜盾)
 *   蝠 = 断目机关蝠(眼部装置破损、只凭回声乱撞)
 *   棋 = 棋冠石将(黑白石阶 + 棋冠)
 *   鹤 = 百声纸鹤(符纸铃铛 + 折纸鹤)
 *   枢 = 百面机枢(悬空核心 + 无数张宣称"绝不会错"的面具)
 */
@Composable
fun CdMonster(glyph: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (glyph) {
            "蝠" -> drawBrokenBat()
            "棋" -> drawChessGeneral()
            "鹤" -> drawPaperCrane()
            "枢" -> drawManyFaceCore()
            else -> drawGateGuard()
        }
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

/** ① 铜齿门卫 —— 齿轮外圈 + 铜盾,盾上刻着固定规则的横纹。 */
private fun DrawScope.drawGateGuard() {
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = m * 0.30f

    // 齿轮外圈
    drawCircle(BronzeFaint, radius = r, center = c)
    drawCircle(Bronze, radius = r, center = c, style = Stroke(width = m * 0.028f))
    repeat(12) { i ->
        rotate(degrees = i * 30f, pivot = c) {
            drawLine(
                Bronze,
                Offset(c.x, c.y - r),
                Offset(c.x, c.y - r * 1.24f),
                strokeWidth = m * 0.045f,
            )
        }
    }

    // 铜盾(竖长的上圆下方轮廓)
    val shield = Path().apply {
        val top = c.y - r * 0.62f
        val bottom = c.y + r * 0.72f
        val halfW = r * 0.42f
        moveTo(c.x - halfW, top + r * 0.20f)
        quadraticBezierTo(c.x, top - r * 0.22f, c.x + halfW, top + r * 0.20f)
        lineTo(c.x + halfW, c.y + r * 0.18f)
        quadraticBezierTo(c.x + halfW * 0.9f, bottom, c.x, bottom + r * 0.10f)
        quadraticBezierTo(c.x - halfW * 0.9f, bottom, c.x - halfW, c.y + r * 0.18f)
        close()
    }
    drawPath(shield, Paper)
    drawPath(shield, Ink, style = Stroke(width = m * 0.026f))

    // 盾上刻痕(「刻满固定规则」的横纹)
    val lines = 4
    repeat(lines) { i ->
        val y = c.y - r * 0.32f + i * r * 0.26f
        drawLine(InkMid, Offset(c.x - r * 0.26f, y), Offset(c.x + r * 0.26f, y), strokeWidth = m * 0.014f)
    }

    // 门卫的「眼」:两个孔,表示它只会照做
    drawCircle(Ink, radius = m * 0.030f, center = Offset(c.x - r * 0.16f, c.y - r * 0.52f))
    drawCircle(Ink, radius = m * 0.030f, center = Offset(c.x + r * 0.16f, c.y - r * 0.52f))
}

/** ② 断目机关蝠 —— 一只眼完好、一只眼被打叉,翅膀不对称。 */
private fun DrawScope.drawBrokenBat() {
    val m = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f)
    val bodyR = m * 0.17f

    // 双翼(左翼完整、右翼残破)
    val leftWing = Path().apply {
        moveTo(c.x - bodyR * 0.6f, c.y - bodyR * 0.2f)
        quadraticBezierTo(c.x - m * 0.34f, c.y - m * 0.20f, c.x - m * 0.40f, c.y + m * 0.06f)
        quadraticBezierTo(c.x - m * 0.30f, c.y + m * 0.02f, c.x - m * 0.26f, c.y + m * 0.13f)
        quadraticBezierTo(c.x - m * 0.20f, c.y + m * 0.03f, c.x - m * 0.15f, c.y + m * 0.12f)
        quadraticBezierTo(c.x - m * 0.10f, c.y + m * 0.02f, c.x - bodyR * 0.6f, c.y + bodyR * 0.5f)
        close()
    }
    drawPath(leftWing, InkFaint)
    drawPath(leftWing, Ink, style = Stroke(width = m * 0.024f))

    val rightWing = Path().apply {
        moveTo(c.x + bodyR * 0.6f, c.y - bodyR * 0.2f)
        quadraticBezierTo(c.x + m * 0.30f, c.y - m * 0.16f, c.x + m * 0.38f, c.y + m * 0.12f)
        quadraticBezierTo(c.x + m * 0.28f, c.y + m * 0.06f, c.x + m * 0.24f, c.y + m * 0.16f)
        quadraticBezierTo(c.x + m * 0.18f, c.y + m * 0.08f, c.x + bodyR * 0.6f, c.y + bodyR * 0.5f)
        close()
    }
    drawPath(rightWing, InkFaint)
    drawPath(rightWing, Ink, style = Stroke(width = m * 0.024f))

    // 身体
    drawCircle(Paper, radius = bodyR, center = c)
    drawCircle(Ink, radius = bodyR, center = c, style = Stroke(width = m * 0.028f))

    // 尖耳
    line(Offset(c.x - bodyR * 0.5f, c.y - bodyR * 0.8f), Offset(c.x - bodyR * 0.75f, c.y - bodyR * 1.5f))
    line(Offset(c.x + bodyR * 0.5f, c.y - bodyR * 0.8f), Offset(c.x + bodyR * 0.75f, c.y - bodyR * 1.5f))

    // 完好的左眼
    drawCircle(Ink, radius = m * 0.028f, center = Offset(c.x - bodyR * 0.42f, c.y - bodyR * 0.1f))
    // 「断目」右眼:一个叉,表示眼部装置已损坏
    val ex = c.x + bodyR * 0.42f
    val ey = c.y - bodyR * 0.1f
    val s = m * 0.035f
    line(Offset(ex - s, ey - s), Offset(ex + s, ey + s), Danger, 0.026f)
    line(Offset(ex + s, ey - s), Offset(ex - s, ey + s), Danger, 0.026f)

    // 回声波纹(呼应「只凭回声乱撞」)
    drawArc(
        Bronze,
        startAngle = -60f, sweepAngle = 120f, useCenter = false,
        topLeft = Offset(c.x + m * 0.20f, c.y - m * 0.14f),
        size = Size(m * 0.18f, m * 0.28f),
        style = Stroke(width = m * 0.018f),
    )
    drawArc(
        BronzeFaint,
        startAngle = -60f, sweepAngle = 120f, useCenter = false,
        topLeft = Offset(c.x + m * 0.26f, c.y - m * 0.22f),
        size = Size(m * 0.28f, m * 0.44f),
        style = Stroke(width = m * 0.016f),
    )
}

/** ③ 棋冠石将 —— 石制身躯 + 棋冠,胸前一方棋盘。 */
private fun DrawScope.drawChessGeneral() {
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
}

/** ④ 百声纸鹤 —— 折纸质感的鹤,旁边挂一排声音铃铛。 */
private fun DrawScope.drawPaperCrane() {
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
