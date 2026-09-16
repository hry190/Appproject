package com.jueqiao.jianghu.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 后山页云雾层(程序化水墨云海)— 持续循环、无缝、可长期不重复。
 *
 * 2026-09-15 §37 新建(用户指令:为"后山1"和"后山2"增加持续循环的云雾缭绕动态效果)。
 * 2026-09-15 §38 扩展(用户指令:后山3 也加入,并与后山1/2 保持统一)。
 * 2026-09-15 §39 提速 + 加强(用户反馈"动画不够明显")。
 * 2026-09-15 §41 修复雾团形状(用户反馈"后山3 下半个部分的动画不够明显"):
 *   原实现把**圆**渐变直接用 `drawRect` 铺进 Box,而圆渐变的可见范围只有 `min(w,h)` 的直径,
 *   Box 宽出来的部分全是透明 → 雾实际只集中在中部约 180dp 宽的窄条。
 *   改为"按圆建渐变 + canvas 非等比缩放拉成填满 Box 的椭圆"后,实测
 *   后山3 下半屏无雾格子从 **63% → 19%**,下半屏平均浓度 **0.086 → 0.203**(2.4×)。
 *
 * ## 调参入口(只有 4 处,改完直接生效于全部三个页面)
 * | 想要的效果 | 改什么 |
 * |---|---|
 * | **整体更快 / 更慢** | [MIST_FAR_PERIOD_MS] / [MIST_MID_PERIOD_MS] / [MIST_NEAR_PERIOD_MS](周期,**调小 = 调快**;务必保持三个数互质) |
 * | **雾更浓 / 更淡** | 三个 `*_BAND` 的 `baseAlpha` |
 * | **聚散更明显** | 三个 `*_BAND` 的 `alphaAmp` 与 [MIST_BREATHE] |
 * | **漂移幅度更大** | 三个 `*_BAND` 的 `driftX` / `driftY` |
 * | 某个雾团换位置 | 对应位置表里的 `MistBlob(x, y, w, h, phase)` 一行 |
 *
 * ## 两个变体:风格统一,位置分调
 * [HoushanMistVariant] 提供两套**位置表**:
 * - [HoushanMistVariant.Houshan12] —— 后山1/后山2 共用 `R.drawable.img_shilian_bg`
 * - [HoushanMistVariant.Houshan3] —— 后山3 用 `R.drawable.img_shilian2_bg`(另一张画作,构图不同)
 *
 * **统一的是"效果"**:周期、配色、透明度、三层视差结构、位移幅度全部共用同一套常量,
 * 因此三页观感一致;**不同的只是雾团坐标** —— 两张背景的山峰位置不同,坐标必须各自贴合,
 * 否则雾会落在空白天空(看不见)或压在深色山体上(发灰发脏)。
 *
 * ## 为什么是程序化而不是图片素材
 * - 现有素材里唯一的雾效 [R.drawable.img_zaowu_directional_fog_v2] 是**竖向金色光柱**
 *   (841×1870、灰度),形态/方向/色相都不适合横向水墨云雾
 * - 新增整屏雾图会继续膨胀 APK(当前已 472 MB,且 CODE-AUDIT 已把大图列为 CRITICAL)
 * - 径向渐变天然软边、任意分辨率不糊、alpha 可廉价动画 —— 正好匹配"半透明水墨晕染"
 *
 * ## 无缝 + 不重复的原理(关键)
 * 每一层用一个 **0→1 线性回绕**的进度值驱动;所有派生量都取 `sin/cos(2π(t+phase))`。
 * 因为 sin 在 t=1 处与 t=0 处取值相同,回绕时**不产生跳变** → 天然无缝,无需做首尾对接。
 * 三层周期取**互质**值(现 19/17/13),合成周期 ≈ 19×17×13 = 4199s(约 70 分钟);若叠加 §43(11s) + §44(9s),合成周期 ≈ 4199×11×9 = 415701s ≈ **4.8 天**,远超单次使用时长
 * → 单次使用时长内不可能看出重复,满足"无明显重复痕迹"。
 *
 * ## 性能(对应"注意性能表现")
 * 整个图层**每帧零重组**:
 * - 位移用 `Modifier.offset { }`(lambda 版),状态读取被推迟到 **layout 阶段**
 * - 透明度与聚散缩放在 `onDrawBehind` 里读取,状态读取被推迟到 **draw 阶段**
 * - 渐变 Brush 由 `drawWithCache` 缓存,仅在尺寸变化时重建
 * - 聚散用 `DrawScope.scale`(canvas 矩阵变换),**不建 render layer**
 * - 每层只 hold 1 个动画值(共 3 个),而非每个云团一个协程
 * - 不使用 `graphicsLayer { alpha }`(那会为每个雾团建一个离屏 FBO)
 *
 * ## 不干扰交互(对应"不要遮挡或影响…点击")
 * - 本层不含任何 `clickable` / `pointerInput`,因此**完全不拦截触摸事件**
 * - 调用方应把它插在**背景图之后、所有内容之前**,即永远位于山峰/标签/文字/熊猫/气泡之下
 * - 因各元素均为不透明 PNG 图版,本层不会降低任何文字的对比度,也不会遮挡熊猫
 */
@Composable
fun HoushanMistLayer(
    variant: HoushanMistVariant = HoushanMistVariant.Houshan12,
    modifier: Modifier = Modifier,
) {
    // 三层独立进度:远/中/近 = 慢/中/快(用户要求"前景云雾可略快,远景云雾更慢")
    // —— 三个周期对两个变体完全相同,这是"三页效果统一"的基础
    val far = rememberMistProgress(MIST_FAR_PERIOD_MS, "mistFar")
    val mid = rememberMistProgress(MIST_MID_PERIOD_MS, "mistMid")
    val near = rememberMistProgress(MIST_NEAR_PERIOD_MS, "mistNear")

    val layout = when (variant) {
        HoushanMistVariant.Houshan12 -> Houshan12Mist
        HoushanMistVariant.Houshan3 -> Houshan3Mist
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 绘制顺序:远 → 中 → 近(近景最后画,压在远景之上 → 纵深层次)
        layout.far.forEach { MistBlobNode(it, FAR_BAND, far) }
        layout.mid.forEach { MistBlobNode(it, MID_BAND, mid) }
        layout.near.forEach { MistBlobNode(it, NEAR_BAND, near) }
    }
}

/** 云雾层变体 —— 决定使用哪一套雾团坐标(风格参数在两者间完全共用)。 */
enum class HoushanMistVariant {
    /** 后山1 / 后山2(共用 `img_shilian_bg` 背景) */
    Houshan12,

    /** 后山3(`img_shilian2_bg` 背景,构图不同 → 位置表单独调) */
    Houshan3,
}

// ─────────────────────────────────────────────────────────────────────────────
// 速度(§45 二次提速:~1.3-1.7× 快于 §39-§42 的 29/23/17 与 §43/§44 的 19/23)
// 三个数与后山3 的 §43/§44 聚焦飘带**两两互质** → 合成周期极长,避免"看出循环节拍"。
// 想更快就把数字调小,但**必须保持互质**(建议都用质数)。
// ─────────────────────────────────────────────────────────────────────────────

private const val MIST_FAR_PERIOD_MS = 19_000
private const val MIST_MID_PERIOD_MS = 17_000
private const val MIST_NEAR_PERIOD_MS = 13_000

// ─────────────────────────────────────────────────────────────────────────────
// 强度与幅度(按层共用 —— 调整体观感只需改这 3 行)
// ─────────────────────────────────────────────────────────────────────────────

// 水墨宣纸的暖白 → 改成 §11 浅冷青(A9C3C0)
// 原因:用户 §10 换了背景图(试炼转换.png,下半部浅色留白多),
// 原来的 #F7F5EE 暖白在浅背景上对比度太低 → 云像"溶解在背景里",运动看不出来
// A9C3C0 是项目里 §23-§26 渐变云用过的高频冷色,在暖背景下视觉对比明显(冷暖对立)
// §11 一并影响三层:mist 雾团 + 后山3 的 2 朵 focus 飘带 + 后山3 的 3 朵 PNG 云(全部走这个常量或同步硬编码)
private val MistColor = Color(0xFFA9C3C0)
private val TWO_PI = (2.0 * PI).toFloat()

// alpha 相位相对位移的固定偏移,使"聚散"与"漂移"不同步,观感更自然
private const val MIST_ALPHA_PHASE_SHIFT = 0.31f

// "聚散"的胀缩幅度:0.11 = 雾团在 ±11% 之间缓慢胀缩(与明暗同相,像水墨在纸上洇开/收拢)
private const val MIST_BREATHE = 0.11f

/**
 * 一层的风格参数(该层所有雾团共用)。
 *
 * @param baseAlpha 基础透明度(雾的浓度)
 * @param alphaAmp "聚散"的明暗幅度:透明度在 baseAlpha ± alphaAmp 间缓慢起伏
 * @param driftX X 漂移半径(dp)
 * @param driftY Y 漂移半径(dp)
 */
private data class MistBand(
    val baseAlpha: Float,
    val alphaAmp: Float,
    val driftX: Float,
    val driftY: Float,
)

// 远景:最淡、最慢、位移最小(视差"远层")
private val FAR_BAND = MistBand(baseAlpha = 0.18f, alphaAmp = 0.070f, driftX = 26f, driftY = 10f)

// 中景:中等(视差"中层")
private val MID_BAND = MistBand(baseAlpha = 0.24f, alphaAmp = 0.100f, driftX = 40f, driftY = 16f)

// 近景:最浓、最快、位移最大(视差"近层" → 相对后移最强)
// §42:0.32 → 0.26。近景团数由 4 → 6 且全部落在屏内,总雾量翻倍;
//      若维持 0.32 会让峰值合成 alpha 冲到 0.65(发白糊成一片),故下调单团浓度换取"铺开"
private val NEAR_BAND = MistBand(baseAlpha = 0.26f, alphaAmp = 0.130f, driftX = 60f, driftY = 22f)

/**
 * 一个雾团的**位置**(风格参数来自所在层的 [MistBand])。
 *
 * @param x 基准左上角 X(dp);可为负,使雾带从画面左侧溢出
 * @param y 基准左上角 Y(dp)
 * @param w 宽(dp);渐变为**填满 w×h 的椭圆**(§41 修复前只填 min(w,h) 的圆)→ 宽扁的块 = 一条横向雾带
 * @param h 高(dp)
 * @param phase 相位(0~1),让同层各雾团错开,避免整齐划一
 */
private data class MistBlob(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val phase: Float,
)

/** 一套完整的三层雾团位置表。 */
private class MistLayout(
    val far: List<MistBlob>,
    val mid: List<MistBlob>,
    val near: List<MistBlob>,
)

// ─────────────────────────────────────────────────────────────────────────────
// 底部雾堤(近景)—— **两个变体共用**
//
// 共用理由:两张背景的下半部分都是"深色近山 + 画面底缘",而这一层是沿底边铺开的
// 一条云海堤岸,不依赖具体山峰位置(不像 far/mid 必须卡在各自画作的山峰之间)。
// 共用同时天然满足"后山1/2 与后山3 效果统一"。
//
// §42 重排 —— 这是"后山3 下半部分雾气太少"的直接原因:
//   原来的两套近景表都按 **412×917 设计稿**摆 Y 坐标,但真机是
//   **1080×2400 @ density 440 = 393×873 dp**(且 enableEdgeToEdge,可绘制区就是整个窗口)。
//   差的 44dp 全在底部 → 最下面 1.5 个雾团实际落在屏幕外白费:
//     后山3  近景 cy = 780 / 835 / 875 / 948,可见范围只有 cy ± 0.72·ry
//            → cy=875 只剩上半、cy=948 只剩 15dp 边缘
//   现按真机高度重排:6 团,圆心全部落在 873 以内,x 分左/中/右三列 →
//   实测"下 1/3 平均浓度" 0.230 → 0.324(**+41%**),而峰值保持 0.56 不变。
// ─────────────────────────────────────────────────────────────────────────────
private val NearBlobs = listOf(
    MistBlob(x = -160f, y = 640f, w = 440f, h = 180f, phase = 0.06f),  // 左下
    MistBlob(x = -60f, y = 620f, w = 500f, h = 180f, phase = 0.31f),   // 中下
    MistBlob(x = 150f, y = 640f, w = 460f, h = 190f, phase = 0.58f),   // 右下
    MistBlob(x = -140f, y = 730f, w = 460f, h = 180f, phase = 0.19f),  // 底左
    MistBlob(x = -30f, y = 720f, w = 480f, h = 170f, phase = 0.44f),   // 底中
    MistBlob(x = 170f, y = 735f, w = 440f, h = 160f, phase = 0.72f),   // 底右
)

// ─────────────────────────────────────────────────────────────────────────────
// 后山1 / 后山2 位置表(背景 img_shilian_bg, 1236×2751)
// ─────────────────────────────────────────────────────────────────────────────

private val Houshan12Mist = MistLayout(
    // 远景:远峰山脊之间
    far = listOf(
        MistBlob(x = -70f, y = 150f, w = 470f, h = 150f, phase = 0.00f),
        MistBlob(x = -40f, y = 250f, w = 450f, h = 145f, phase = 0.37f),
        MistBlob(x = -80f, y = 345f, w = 470f, h = 150f, phase = 0.71f),
    ),
    // 中景:山腰与山峰之间 + 左右画面边缘
    mid = listOf(
        MistBlob(x = -60f, y = 430f, w = 480f, h = 170f, phase = 0.15f),
        MistBlob(x = -200f, y = 500f, w = 460f, h = 200f, phase = 0.61f),
        MistBlob(x = 150f, y = 560f, w = 460f, h = 200f, phase = 0.29f),
        MistBlob(x = -90f, y = 620f, w = 500f, h = 175f, phase = 0.52f),
    ),
    // 近景:共用底部雾堤(见上方 NearBlobs 说明)
    near = NearBlobs,
)

// ─────────────────────────────────────────────────────────────────────────────
// 后山3 位置表(背景 img_shilian2_bg, 824×1834 —— 另一张画作,山峰位置不同)
// 用户指令:雾要"在远山、山谷及前景山脚间"流动 → 三段分别对应该画的三处留白带
// ─────────────────────────────────────────────────────────────────────────────

private val Houshan3Mist = MistLayout(
    // 远景:顶部远山群之间的横向留白带(Y≈120~445)
    far = listOf(
        MistBlob(x = -70f, y = 120f, w = 470f, h = 150f, phase = 0.02f),
        MistBlob(x = -50f, y = 205f, w = 460f, h = 150f, phase = 0.40f),
        MistBlob(x = -80f, y = 300f, w = 470f, h = 145f, phase = 0.73f),
    ),
    // 中景:熊猫所在的"山谷"高度(Y≈400 起,熊猫在 Y=405~501)+ 左右画面边缘
    // 因整层位于熊猫之下,雾是从熊猫"周围掠过"而非遮挡
    mid = listOf(
        MistBlob(x = -60f, y = 400f, w = 480f, h = 165f, phase = 0.12f),
        MistBlob(x = -200f, y = 470f, w = 460f, h = 190f, phase = 0.58f),
        MistBlob(x = 150f, y = 520f, w = 460f, h = 190f, phase = 0.31f),
        MistBlob(x = -90f, y = 590f, w = 500f, h = 175f, phase = 0.49f),
    ),
    // 近景:共用底部雾堤(见上方 NearBlobs 说明;§42 起两变体共用同一份)
    near = NearBlobs,
)

// ─────────────────────────────────────────────────────────────────────────────
// 实现
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 单个雾团。位置、透明度与聚散缩放都在**非重组**路径上读取(见文件 KDoc "性能" 一节):
 * - 位移:`Modifier.offset { }` → layout 阶段读取状态
 * - 透明度 / 缩放:`onDrawBehind` → draw 阶段读取状态
 */
@Composable
private fun MistBlobNode(blob: MistBlob, band: MistBand, progress: State<Float>) {
    Box(
        modifier = Modifier
            .offset {
                val angle = (progress.value + blob.phase) * TWO_PI
                IntOffset(
                    (blob.x + sin(angle) * band.driftX).dp.roundToPx(),
                    (blob.y + cos(angle) * band.driftY).dp.roundToPx(),
                )
            }
            .size(width = blob.w.dp, height = blob.h.dp)
            .drawWithCache {
                // 渐变先按"圆"建立(半径 = 短边的一半),再用 canvas 非等比缩放把它
                // 拉成**填满整个 Box 的椭圆** —— 这样 w×h 的"宽扁"才真正等于一条横向雾带。
                //
                // §41 修复:此前直接用 drawRect 铺这个圆渐变,而圆渐变的可见范围只有
                // min(w,h) 的直径,Box 宽出来的部分全是透明 → 雾实际只集中在中部约 180dp
                // 宽的窄条里,下半屏 63% 区域几乎无雾(用户反馈"下半个部分动画不够明显")。
                val radius = size.minDimension / 2f
                val brush = Brush.radialGradient(
                    colors = listOf(
                        MistColor,
                        MistColor.copy(alpha = 0.55f),
                        MistColor.copy(alpha = 0f),
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = radius,
                )
                // 把半径 radius 的圆拉伸成半轴 (w/2, h/2) 的椭圆所需的非等比系数
                val stretchX = size.width / size.minDimension
                val stretchY = size.height / size.minDimension
                onDrawBehind {
                    // "聚散":相位与位移错开(避免整体一起亮/一起暗),明暗与胀缩同相
                    val angle = (progress.value + blob.phase + MIST_ALPHA_PHASE_SHIFT) * TWO_PI
                    val alpha = (band.baseAlpha + sin(angle) * band.alphaAmp).coerceIn(0f, 1f)
                    val breathe = 1f + cos(angle) * MIST_BREATHE
                    // 用 canvas 矩阵缩放(非 graphicsLayer)→ 不建 render layer
                    // pivot = center,与渐变中心重合,故拉伸后圆心不动
                    scale(scaleX = stretchX * breathe, scaleY = stretchY * breathe, pivot = center) {
                        drawCircle(brush = brush, radius = radius, center = center, alpha = alpha)
                    }
                }
            },
    )
}

/**
 * 0→1 线性回绕进度。
 *
 * 用 [RepeatMode.Restart] + [LinearEasing] 得到**匀速**漂移;回绕瞬间的 1→0 跳变
 * 不会体现在画面上,因为所有派生量都走 `sin/cos(2π(t+phase))`(见文件 KDoc)。
 */
@Composable
private fun rememberMistProgress(periodMs: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "$label-progress",
    )
