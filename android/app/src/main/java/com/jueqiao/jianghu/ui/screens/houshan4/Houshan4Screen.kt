package com.jueqiao.jianghu.ui.screens.houshan4

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
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 后山4 → 后山5 沉浸式纵深推进(dolly-in)参数 (§33,沿用后山2 §36 / 后山3 §24 的同款参数)──
// 总时长落在 0.8~1.2s 区间;ease-in-out 用 FastOutSlowInEasing(标准缓入缓出)。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山5 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山4 页 — 后山3 页 dolly 推进而来;点击"返回"回到后山3;
 * **点击标签以外任意位置 → dolly 推进到后山5**(§33)。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【§33 交互变更】从"终点页"变成"过场页"
 *
 *   §24~§32 期间:后山4 是**终点页** —— 整屏 clickable = noop,无 dolly,
 *                 标签 clickable 不需要 `enabled = !isTransitioning` 门槛。
 *   §33 起:用户要求"点击标签以外的位置跳转到后山5" + "山峰拉近动画要出现"
 *           → 后山4 补上 **dolly-in 三景深平面**(与后山2 §36 / 后山3 §24 同款),
 *             整屏 clickable 改为 `startDollyIn()`,标签加回 `enabled = !isTransitioning`。
 *
 *   ⚠️ 这条演进说明**"终点页"是暂时的** —— 每次在后面接新页面,原终点都要"补 dolly +
 *      改整屏 click + 给标签加门槛"三件事。后山3 §24、后山4 §33 各经历过一次。
 * ══════════════════════════════════════════════════════════════════════════
 * 【点击行为 · 当前】
 *
 *   | 点击位置                  | 结果                          |
 *   |--------------------------|-------------------------------|
 *   | 4 个标签                  | → 各自对应的卷的第一页(见下表)|
 *   | 其余任意位置(空白/云/熊猫)| → **dolly-in 推进 → 后山5**   |
 *   | 左上角返回按钮            | → 后山3                       |
 *   | (dolly 进行中)点任何标签   | ❌ 无效(`enabled = false`)    |
 *
 *   4 个标签的跳转目标(§32 配置,注意与后山2/3 的映射表**不同**):
 *     万象谱 → 第三卷-1 · 寻径迷踪步 → 第四卷-1 · 百炼识物诀 → 第五卷-1 · 分门辨类掌 → 第六卷-1
 *     ⚠️ 后山4 的标签文字是 §24 改过的,不能照抄后山2 的位置映射 —— 映射锚在"文字"上。
 * ══════════════════════════════════════════════════════════════════════════
 *
 * 复用(与后山 1/2 同款素材):
 *   - 全屏背景图 img_shilian_bg.png
 *   - 云雾层 HoushanMistLayer()(默认变体,**不**用 Houshan3 变体)
 *   - 6 朵 ACI 动画云(58/60/62 + 2 FocusCloudBand + 57)+ 6 朵老云(58/61/56/57/60/60b)
 *   - 熊猫 img_shilian_panda,X=184 Y=621 W=210 H=192(§22 同款动画)
 *   - 返回按钮 Return.png,X=30 Y=60 W=18 H=18
 *   - **不加** Rectangle156 气泡(§18 "过场页不应有信息气泡")
 *
 * 布局:
 *   - 标签1 万象谱     X=-13, Y=570, W=106, H=210(§24b)
 *   - 标签2 寻径迷踪步 X=168, Y=345, W=74,  H=150(§24b)
 *   - 标签3 百炼识物诀 X=113, Y=322, W=50,  H=105(§24b)
 *   - 标签4 分门辨类掌 X=151, Y=248, W=30,  H=70(§24b)
 */
/**
 * 后山4 页所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 * 详见 §5(SESSION-LOG-2026-09-18)真机验证根因:多 lambda 签名 → 1 个 data class,bug 触发条件消失。
 */
data class Houshan4Actions(
    val onBack: () -> Unit = {},
    val onOpenHoushan5: () -> Unit = {},
    val onOpenVolume3Part1: () -> Unit = {},  // 标签1 万象谱     → 第三卷-1 (§32)
    val onOpenVolume4Part1: () -> Unit = {},  // 标签2 寻径迷踪步 → 第四卷-1 (§32)
    val onOpenVolume5Part1: () -> Unit = {},  // 标签3 百炼识物诀 → 第五卷-1 (§32)
    val onOpenVolume6Part1: () -> Unit = {},  // 标签4 分门辨类掌 → 第六卷-1 (§32)
)

@Composable
fun Houshan4Screen(
    actions: Houshan4Actions = Houshan4Actions(),
) {
    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 过渡期间禁用返回手势,避免动画途中被中断而露出半程画面
    BackHandler(enabled = !isTransitioning) { actions.onBack() }

    // 熊猫上下浮 + 呼吸缩放(沿用 §22 后山 2 / §21 后山 1 的同款动画参数)
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

    // ── 6 个动画云元素的进度:与后山 1/2 完全同一套(周期 21.4/11/8/10/9/13 s)──
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(与后山 1/2 同一套;§21f 用户指令"不考虑间距了")──
    val o58Progress = rememberCloudProgress(4_700, "old58")
    val o61Progress = rememberCloudProgress(9_700, "old61")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o57Progress = rememberCloudProgress(8_200, "old57")
    val o60Progress = rememberCloudProgress(4_900, "old60")
    val o60bProgress = rememberCloudProgress(10_300, "old60b")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (§33) ──────────────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 整屏点击:启动纵深推进,并在半程把控制权交给导航(后山5 交叉淡入)
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
                actions.onOpenHoushan5()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // §33:整屏 clickable 触发 dolly-in(取代 §24~§32 的 noop 终点语义)
            .clickable { startDollyIn() },
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
            // 全屏背景图(后山页背景.png,与后山 1/2 同款)
            Image(
                painter = painterResource(R.drawable.img_shilian_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 内容层(避开系统导航条)— 4 个标签 Box 自带 clickable 消费事件,点击标签不会冒泡触发 dolly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
            // 把云雾层 + 6 朵 ACI + 6 朵老云全部包进一个 graphicsLayer(scale + alpha),
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
                // 云雾层(程序化水墨云海,持续循环)— 用默认变体(Houshan1/2 同款)
                // 后山 3/5 用的是 Houshan3 变体(因为它们的背景图是另一张画作);后山 4 用默认变体
                HoushanMistLayer()

                // ══ §21 动画云元素竖排(6 个)════════════════════════════════════════
                // 与后山 1/2 完全同一套 y 序列、间隔(69/62/66/60/61)、×0.75 尺寸
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -27f, yOffset = 8f,
                    widthDp = 180f, heightDp = 101.5f,
                    progress = c58Progress, phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                    tint = CloudTintCool,
                )
                FocusCloudBand(
                    xOffset = 0f, yOffset = 178.5f,
                    widthDp = 180f, heightDp = 90f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.30f,
                    periodMs = 11_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60",
                    xOffset = 0f, yOffset = 330.5f,
                    widthDp = 210f, heightDp = 81f,
                    progress = c60Progress, phase = 0.35f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 8f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                )
                FocusCloudBand(
                    xOffset = 0f, yOffset = 477.5f,
                    widthDp = 240f, heightDp = 82.5f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.30f,
                    periodMs = 10_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan3_cloud_62),
                    contentDescription = "云朵62",
                    xOffset = 83f, yOffset = 620f,
                    widthDp = 180f, heightDp = 73.5f,
                    progress = c62Progress, phase = 0.70f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f, amplitudeY = 8f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_57),
                    contentDescription = "云朵57",
                    xOffset = 146f, yOffset = 754.5f,
                    widthDp = 100f, heightDp = 90f,
                    progress = c57Progress, phase = 0.71f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                    tint = CloudTintCool,
                )

                // ══ §21f 6 朵老云(与后山 1/2 同一套)════════════════════════════════
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -70f, yOffset = 320f,
                    widthDp = 455f, heightDp = 259f,
                    progress = o58Progress, phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 90f, amplitudeY = 14f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_61),
                    contentDescription = "云朵61",
                    xOffset = 0f, yOffset = 304f,
                    widthDp = 355f, heightDp = 213f,
                    progress = o61Progress, phase = 0.18f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_56),
                    contentDescription = "云朵56",
                    xOffset = 196f, yOffset = 595f,
                    widthDp = 335f, heightDp = 297f,
                    progress = o56Progress, phase = 0.42f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 70f, amplitudeY = 16f,
                    baseAlpha = 1f, alphaAmp = 0f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_57),
                    contentDescription = "云朵57",
                    xOffset = 0f, yOffset = 570f,
                    widthDp = 225f, heightDp = 191f,
                    progress = o57Progress, phase = 0.63f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60",
                    xOffset = -21f, yOffset = 570f,
                    widthDp = 355f, heightDp = 137f,
                    progress = o60Progress, phase = 0.27f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 90f, amplitudeY = 10f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60b",
                    xOffset = 0f, yOffset = 770f,
                    widthDp = 355f, heightDp = 137f,
                    progress = o60bProgress, phase = 0.81f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 10f,
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
                // 熊猫图像(沿用 §22 后山 2 / §21 后山 1 的同款)— X=184, Y=621, W=210, H=192
                Image(
                    painter = painterResource(R.drawable.img_shilian_panda),
                    contentDescription = "熊猫",
                    modifier = Modifier
                        .offset(x = 184.dp, y = (621f + pandaDy).dp)
                        .size(width = 210.dp, height = 192.dp)
                        .graphicsLayer(
                            scaleX = pandaScale,
                            scaleY = pandaScale,
                        ),
                    contentScale = ContentScale.FillBounds,
                )

                // "标签1" 图像(万象谱,X=-13, Y=570, W=106, H=210 §24b)— §32:点击 → **第三卷-1**
                //   §33:加 `enabled = !isTransitioning` 门槛(后山4 现在有 dolly,防过渡途中误触)
                Box(
                    modifier = Modifier
                        .offset(x = -13.dp, y = 570.dp)
                        .size(width = 106.dp, height = 210.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenVolume3Part1,
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签1",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "万\n象\n谱",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 24.sp),
                        modifier = Modifier
                            .offset(x = 46.dp, y = 68.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 48.dp, y = 30.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签2" 图像(寻径迷踪步,X=168, Y=345, W=74, H=150 §24b) — §32:点击 → **第四卷-1**
                Box(
                    modifier = Modifier
                        .offset(x = 168.dp, y = 345.dp)
                        .size(width = 74.dp, height = 150.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenVolume4Part1,
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
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp, lineHeight = 15.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 40.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 20.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签3" 图像(百炼识物诀,X=113, Y=322, W=50, H=105 §24b) — §32:点击 → **第五卷-1**
                Box(
                    modifier = Modifier
                        .offset(x = 113.dp, y = 322.dp)
                        .size(width = 50.dp, height = 105.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenVolume5Part1,
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
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 10.sp),
                        modifier = Modifier
                            .offset(x = 20.5.dp, y = 27.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                        modifier = Modifier
                            .offset(x = 23.dp, y = 18.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像(分门辨类掌,X=151, Y=248, W=30, H=70 §24b) — §32:点击 → **第六卷-1**
                Box(
                    modifier = Modifier
                        .offset(x = 151.dp, y = 248.dp)
                        .size(width = 30.dp, height = 70.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = actions.onOpenVolume6Part1,
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
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp, lineHeight = 6.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 23.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 11.dp)
                            .size(width = 10.dp, height = 14.dp),
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
                    .clickable(enabled = !isTransitioning, onClick = actions.onBack),
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
