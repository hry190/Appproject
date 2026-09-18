package com.jueqiao.jianghu.ui.screens.houshan5

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.AnimatedCloudImage
import com.jueqiao.jianghu.ui.components.CloudMotion
import com.jueqiao.jianghu.ui.components.CloudTintCool
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.components.HoushanMistVariant
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 后山5 → 后山6 沉浸式纵深推进(dolly-in)参数 (2026-09-18 §4,沿用后山2 §36 / 后山3 §24 / 后山4 §33 的同款参数)──
// 总时长落在 0.8~1.2s 区间;ease-in-out 用 FastOutSlowInEasing(标准缓入缓出)。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山6 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山5 页的所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 *
 * 为什么用 data class 而不是 5 个独立 lambda 参数:
 *   §3 实测:多 lambda 签名(`onBack, onA, onB, onC, onD` 都是 `() -> Unit`)的 composable,
 *           slot table 在某种条件下把第 1 个 slot 记成 null 而非 `Composer.Empty`,
 *           被 Compose 当成"有效值"复用 → clickable.onClick = null → invoke() 时 NPE 闪退。
 *   方向 B(包成 data class):参数从"5 个 lambda"变成"1 个非 lambda",slot 结构根本不同,
 *                           bug 触发条件消失,理论上根治。
 *   字段命名沿用 §34 的"目标卷号"语义 —— 不绑定标签文案,文案再改只动 NavHost 一处。
 */
data class Houshan5Actions(
    val onBack: () -> Unit = {},
    // ── 2026-09-18 §15 新规则:点击"非本页 Y 最大"的标签 → 跳到"该文本为 Y 最大标签"的那个页面 ──
    val onOpenHoushan6: () -> Unit = {},   // 百炼识物诀 → 后山6(百炼识物诀在后山6 是 Y 最大标签)
    val onOpenHoushan7: () -> Unit = {},   // 分门辨类掌 → 后山7(分门辨类掌在后山7 是 Y 最大标签)
    // ── 卷跳转:仅"本页 Y 最大标签"使用 ──
    val onOpenVolume4Part1: () -> Unit = {},  // 寻径迷踪步(本页 Y 最大,Y=521)→ 第四卷-1
    val onOpenVolume5Part1: () -> Unit = {},
    val onOpenVolume6Part1: () -> Unit = {},
)

/**
 * 后山5 页 — 后山4 页 dolly 推进而来;点击"返回"按钮回到后山4;
 * **整屏点击 / 返回键的导航已断开**(2026-09-18 §13,待重设)—— 原为"点击标签以外任意位置 → dolly 推进到后山6"。
 *
 * 2026-09-17 §33 新建:用户指令"创建后山5页面...后山5复用后山3页面的素材和动画"。
 * 2026-09-18 §4 升级:从"终点页"变成"过场页" —— 补上 dolly-in 三景深平面
 *                    (参数沿革详见本文件顶部的 2026-09-18 §4 注释行),
 *                    整屏点击改为 startDollyIn(),标签加回 isTransitioning 门槛。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【2026-09-18 §4 交互变更】从"终点页"变成"过场页"
 *
 *   §33~§34 期间:后山5 是**终点页** —— 整屏 clickable = noop,无 dolly,
 *                标签 clickable 不需要 `enabled = !isTransitioning` 门槛。
 *   2026-09-18 §4 起:用户要求"点击标签以外的位置跳转到后山6" + "山峰拉近动画要出现"
 *           → 后山5 补上 **dolly-in 三景深平面**(与后山2 §36 / 后山3 §24 / 后山4 §33 同款),
 *             整屏 clickable 改为 `startDollyIn()`,标签加回 `enabled = !isTransitioning`。
 *
 *   ⚠️ 这条演进说明**"终点页"是暂时的** —— 每次在后面接新页面,原终点都要"补 dolly +
 *      改整屏 click + 给标签加门槛"三件事。后山3 §24、后山4 §33、后山5 2026-09-18 §4 各经历过一次。
 * ══════════════════════════════════════════════════════════════════════════
 * 【点击行为 · 当前】
 *
 *   | 点击位置                  | 结果                          |
 *   |--------------------------|-------------------------------|
 *   | 3 个标签                  | → 仅 **Y 最大**标签可跳转(§14);其余 ❌ 待设置|
 *   | 其余任意位置(空白/云/熊猫)| → ❌ **已取消**(2026-09-18 §13,待重设)|
 *   | 左上角返回按钮 / 系统返回键 | → ❌ **已取消**(2026-09-18 §13,待重设)|
 *   | (dolly 进行中)点任何位置   | —(导航已断开,dolly 不会被触发)|
 *
 * ⚠️ **2026-09-18 §12 用户指令:"取消所有标签的跳转,我要重新设置"** —— 下列跳转**已全部取消**;
 * ✅ **2026-09-18 §14 部分恢复** —— 仅 **Y 值最大的标签**(页面上最下方那个)恢复跳转到"其文本对应的卷的第一页";
 *    其余标签**仍为死区**,等用户继续设置。
 *    标签现在是**死区**(`.clickable(..., onClick = {})` 仅消费点击事件,不跳转)。**取消前的映射留档如下(供重设参考)**:
 *   3 个标签的跳转目标(§34 接好,沿用 §31 的"文字→卷"映射):
 *     分门辨类掌 → 第六卷-1 · 百炼识物诀 → 第五卷-1 · 寻径迷踪步 → 第四卷-1
 *     ⚠️ 后山5 的标签文字是 §34 改过的,不能照搬后山3 的位置映射 —— 映射锚在"文字"上。
 * ══════════════════════════════════════════════════════════════════════════
 *
 * 复用(与后山 3 同款素材):
 *   - 全屏背景图 img_shilian2_bg.png
 *   - 云雾层 HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
 *   - 5 朵 ACI 动画云(58 / FCB左下 / 60 / FCB中下 / 62)+ 5 朵老云(old / 56 / 58 / 57 / 5)
 *   - 熊猫 img_shilian2_recovered_8,X=118 Y=405 W=181 H=96
 *   - 3 个标签    分门辨类掌 / 百炼识物诀 / 寻径迷踪步(位置与后山3 完全一致,文案已重命名 §34)
 *
 * 布局(与后山3 一致):
 *   - 全屏背景图(img_shilian2_bg.png)
 *   - 5 朵 ACI 动画云 + 5 朵老云
 *   - 熊猫图像(X=118, Y=405, W=181, H=96)
 *   - 标签3 百炼识物诀(X=43, Y=390, W=51, H=91)—— 字号 10sp,行间距 9sp,5×9=45dp < 容器 60dp ✓
 *   - 标签4 分门辨类掌(X=105, Y=295, W=30, H=53.5)—— 字号 4sp,行间距 6sp,5×6=30dp < 容器 60dp ✓
 *   - 标签2 寻径迷踪步(X=124, Y=521, W=96, H=170)—— 字号 14sp,行间距 14sp,5×14=70dp < 容器 80dp ✓
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
@Composable
fun Houshan5Screen(
    actions: Houshan5Actions = Houshan5Actions(),
) {
    // §3 + 2026-09-18 §4:用 data class 包成 1 个参数,bug 触发条件(slot 0 = lambda)消失,无需 safeXxx 兜底
    // (旧版的 if (xxx == null) ({}) else xxx 5 行兜底已删)

    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 过渡期间禁用返回手势,避免动画途中被中断而露出半程画面
    BackHandler(enabled = !isTransitioning) { actions.onBack() }   // 2026-09-18 §15 恢复:返回上一页

    // 熊猫上下浮 + 呼吸缩放(与后山1/2/3 同款参数)
    val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
    val pandaScale by pandaTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaScale",
    )
    val pandaDy by pandaTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaDy",
    )

    // §21 5 朵 ACI 动画云进度(与后山3 同一套周期)
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")

    // §21f 5 朵老云动画进度(与后山3 同一套)
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (2026-09-18 §4,沿用 §33 后山 4 的同款) ──────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 整屏点击:启动纵深推进,并在半程把控制权交给导航(后山6 交叉淡入)
    val startDollyIn: () -> Unit = {
        if (!isTransitioning) {
            isTransitioning = true
            scope.launch {
                dolly.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = DOLLY_DURATION_MS, easing = FastOutSlowInEasing),
                )
            }
            scope.launch {
                delay(DOLLY_HANDOFF_MS)
                actions.onOpenHoushan6()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 2026-09-18 §4:整屏 clickable 触发 dolly-in(取代 §33~§34 的 noop 终点语义)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),   // 2026-09-18 §13 断开导航(待重设)
    ) {
        // ── 景深平面 1:背景山体(推进最多 → "向用户靠近")────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    transformOrigin = focal
                },
        ) {
            // 全屏背景图(试炼转换.png —— 与后山3 同一张)
            Image(
                painter = painterResource(R.drawable.img_shilian2_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // 云雾层(程序化水墨云海,持续循环)— 用 Houshan3 变体(与后山3 同)
            HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
        }

        // 内容层(避开系统导航条)— 标签 Box 自带 clickable 消费事件,点击标签不会冒泡触发 dolly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
            // 把 5 朵 ACI + 5 朵老云全部包进一个 graphicsLayer(scale + alpha),
            // dolly 时一起 ×1.09 放大 + 一起淡出,层次一致,不会"留在屏上不动"。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cloudScale
                        scaleY = cloudScale
                        transformOrigin = focal
                        alpha = cloudFade
                    },
            ) {
                // ══ §21 动画云元素竖排(5 个,与后山3 完全同一套)════════════════
                // y 序列(顶边):ACI58 8 · FCB左下 178.5 · ACI60 330.5 · FCB中下 477.5 · ACI62 620
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -60f,
                    yOffset = 8f,
                    widthDp = 180f,
                    heightDp = 101.5f,
                    progress = c58Progress,
                    phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                    tint = CloudTintCool,
                )
                FocusCloudBand(
                    xOffset = 0f,
                    yOffset = 178.5f,
                    widthDp = 180f,
                    heightDp = 90f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.30f,
                    periodMs = 11_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60",
                    xOffset = 0f,
                    yOffset = 330.5f,
                    widthDp = 210f,
                    heightDp = 81f,
                    progress = c60Progress,
                    phase = 0.35f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f,
                    amplitudeY = 8f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                )
                FocusCloudBand(
                    xOffset = 0f,
                    yOffset = 477.5f,
                    widthDp = 240f,
                    heightDp = 82.5f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.30f,
                    periodMs = 10_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan3_cloud_62),
                    contentDescription = "云朵62",
                    xOffset = 50f,
                    yOffset = 620f,
                    widthDp = 180f,
                    heightDp = 73.5f,
                    progress = c62Progress,
                    phase = 0.70f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f,
                    amplitudeY = 8f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                )

                // ══ §21f 5 朵老云(与后山3 完全同一套)════════════════════════════
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_old),
                    contentDescription = "旧云朵",
                    xOffset = -46f, yOffset = 476f,
                    widthDp = 331f, heightDp = 92f,
                    progress = oOldProgress, phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 80f, amplitudeY = 10f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_56),
                    contentDescription = "云朵56",
                    xOffset = 263f, yOffset = 755f,
                    widthDp = 335f, heightDp = 297f,
                    progress = o56Progress, phase = 0.42f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 70f, amplitudeY = 16f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = 0f, yOffset = 170f,
                    widthDp = 331f, heightDp = 92f,
                    progress = o58Progress, phase = 0.18f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 10f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_57),
                    contentDescription = "云朵57",
                    xOffset = 0f, yOffset = 500f,
                    widthDp = 225f, heightDp = 191f,
                    progress = o57Progress, phase = 0.63f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_5),
                    contentDescription = "云朵5",
                    xOffset = 0f, yOffset = 308f,
                    widthDp = 225f, heightDp = 191f,
                    progress = o5Progress, phase = 0.81f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )
            }

            // ── 景深平面 3:标签 + 熊猫(与山体同速推进 + 淡出 → 不相对滑动)──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = labelScale
                        scaleY = labelScale
                        transformOrigin = focal
                        alpha = labelFade
                    },
            ) {
                // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 与后山3 同
                Image(
                    painter = painterResource(R.drawable.img_shilian2_recovered_8),
                    contentDescription = "熊猫",
                    modifier = Modifier
                        .offset(x = 118.dp, y = (405f + pandaDy).dp)
                        .size(width = 181.dp, height = 96.dp)
                        .graphicsLayer(
                            scaleX = pandaScale,
                            scaleY = pandaScale,
                        ),
                    contentScale = ContentScale.FillBounds,
                )

                // "标签3" 图像(百炼识物诀,X=43, Y=390, W=51, H=91)— §34 已接 → 第五卷-1
                //   2026-09-18 §4:加 `enabled = !isTransitioning` 门槛(后山5 现在有 dolly,防过渡途中误触)
                Box(
                    modifier = Modifier
                        .offset(x = 43.dp, y = 390.dp)
                        .size(width = 51.dp, height = 91.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenHoushan6,   // 2026-09-18 §15 跳到"该文本为 Y 最大标签"的页面
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签3",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "百\n炼\n识\n物\n诀",
                        color = Color.Black,
                        // §34 用户指令:行间距 9sp(略小于字号 10sp,字符紧凑);5×9=45dp,容器 60dp 留 15dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 9.sp),
                        modifier = Modifier
                            .offset(x = 20.5.dp, y = 25.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                        modifier = Modifier
                            .offset(x = 22.dp, y = 12.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像(分门辨类掌,X=105, Y=295, W=30, H=53.5)— §34 已接 → 第六卷-1
                //   文案 5→5 字,字号 4sp 不变
                //   2026-09-18 §4:加 `enabled = !isTransitioning` 门槛
                Box(
                    modifier = Modifier
                        .offset(x = 105.dp, y = 295.dp)
                        .size(width = 30.dp, height = 53.5.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenHoushan7,   // 2026-09-18 §15 跳到"该文本为 Y 最大标签"的页面
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签4",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "分\n门\n辨\n类\n掌",
                        color = Color.Black,
                        // §34 用户指令:行间距 6sp(略大于字号 4sp,适度撑开);5×6=30dp,容器 60dp 留 30dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp, lineHeight = 6.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 14.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 7.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签2" 图像(寻径迷踪步,X=124, Y=521, W=96, H=170)— §34 已接 → 第四卷-1
                //   2026-09-18 §4:加 `enabled = !isTransitioning` 门槛
                Box(
                    modifier = Modifier
                        .offset(x = 124.dp, y = 521.dp)
                        .size(width = 96.dp, height = 170.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenVolume4Part1,   // 2026-09-18 §14 恢复跳转(其余标签仍待设置)
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签2",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "寻\n径\n迷\n踪\n步",
                        color = Color.Black,
                        // §34 用户指令:行间距 14sp(等于字号,字符紧凑);5×14=70dp,容器 80dp 有 10dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 14.sp),
                        modifier = Modifier
                            .offset(x = 43.dp, y = 48.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 43.dp, y = 25.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }
            }

            // ── UI chrome:左上角返回按钮 ─────────────────────────────────────
            // 只淡出不缩放:UI chrome 不参与景深,否则会随山体放大而"跳动"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .graphicsLayer { alpha = chromeFade }
                    .clickable(enabled = !isTransitioning, onClick = actions.onBack)   /* 2026-09-18 §15 恢复:返回上一页 */,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
