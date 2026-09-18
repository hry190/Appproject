package com.jueqiao.jianghu.ui.screens.houshan10

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

// ── 后山10 → 后山11 沉浸式纵深推进(dolly-in)参数 (2026-09-19 §3 **已启用**)──
// 触发点:点击本页「正心守道录」标签(用户指令:"点击任意页面的'正心守道录'标签")。
// 参数沿用后山2 §2026-09-16.36 首创,经后山3 §24 / 后山4 §33 / 后山5 §4 / 后山6 §9 /
//   后山7 §10 / 后山8 §11 / 后山9 §2 演进的同款。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由下一页交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山10 页 — 后山9 页 dolly 推进而来;点击"返回"按钮回到后山9;整屏点击 noop;
 * 「正心守道录」标签触发 dolly 推进到后山11(2026-09-19 §3),「听言解意篇」标签跳卷9。
 *
 * 2026-09-19 §2 新建:用户指令"创建后山10页面...后山10复用后山8页面的素材和动画" +
 *   "把标签'千层观心镜'改成'听言解意篇'、把'赏罚驭灵诀'改成'正心守道录'、
 *    删掉原有的'听言解意篇'标签和'正心守道录'标签" + "应用 Y 最大标签跳对应卷规则"。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【与后山8 的关系】**素材 / 动画 100% 复用,但标签从 4 个减到 2 个**
 *
 *   复用(与后山8 一致):
 *     - 全屏背景图 img_shilian_bg.png
 *     - 云雾层 HoushanMistLayer()(默认变体,**不**用 Houshan3 变体 —— 与后山8 同)
 *     - 6 朵 ACI 动画云(58 / FCB左下 / 60 / FCB中下 / 62 / 57)+ 6 朵老云(58 / 61 / 56 / 57 / 60 / 60b)
 *     - 熊猫 img_shilian_panda,X=184 Y=621 W=210 H=192(同款动画)
 *
 *   改造(与后山8 不同)—— **标签 4 → 2**:
 *     | 位置 | 后山8 原文本 | 后山10 新文本 | Y | 说明 |
 *     |---|---|---|---|---|
 *     | 标签1(X=-13)| 千层观心镜 | **听言解意篇** | **570** | 改名;**成为本页 Y 最大** |
 *     | 标签2(X=168)| 赏罚驭灵诀 | **正心守道录** | 345 | 改名 |
 *     | ~~标签3(X=113)~~ | ~~听言解意篇~~ | **已删除** | — | 用户指令删除 |
 *     | ~~标签4(X=151)~~ | ~~正心守道录~~ | **已删除** | — | 用户指令删除 |
 *
 *     - ❌ 整屏点击是**死区**(noop)—— 触发点是标签,不是整屏
 *     - ✅ dolly 已启用(2026-09-19 §3):点「正心守道录」→ dolly 推进到后山11
 * ══════════════════════════════════════════════════════════════════════════
 * 【点击行为 · 当前】
 *
 *   | 点击位置                  | 结果                          |
 *   |--------------------------|-------------------------------|
 *   | 标签1 **听言解意篇**(Y=570)| → **第九卷-1**(本页 Y 最大,按 §15 规则)|
 *   | 标签2 正心守道录(Y=345)   | → **dolly 推进 → 后山11**(2026-09-19 §3 改动,原为死区)|
 *   | 其余任意位置(空白/云/熊猫)| → ❌ noop(整屏点击取消)|
 *   | 左上角返回按钮 / 系统返回键 | → 后山9                       |
 * ══════════════════════════════════════════════════════════════════════════
 * 【本页在导航体系里的角色】—— 2026-09-19 §2 起,§3 修订
 *
 *   后山10 是 **「听言解意篇」的 Y 最大页面**(全链最大):
 *     - 听言解意篇:后山8=322 < 后山9=390 < **后山10=570** ✓ → 本页标签跳卷9
 *   后山10 **曾是**「正心守道录」的 Y 最大页面;2026-09-19 §3 后山11 创建后让位:
 *     - 正心守道录:后山8=248 < 后山9=295 < 后山10=345 < **后山11=521**
 *   → 因此本页的「正心守道录」不再跳卷,而是 **dolly 推进 → 后山11**(用户 §3 指令)。
 *   → 2026-09-18 §15 里用户说的"我还没有创建这几个文本标签在 Y 值最大的页面,先不要跳转,
 *     **之后提醒我**" —— 到 §3 为止,「听言解意篇」(后山10)与「正心守道录」(后山11)
 *     两个 Y 最大页面均已创建,相关死区全部接完。
 *
 * 布局(坐标与后山8 的标签1/2 完全一致):
 *   - 全屏背景图(img_shilian_bg.png)
 *   - 6 朵 ACI 动画云 + 6 朵老云
 *   - 熊猫图像(X=184, Y=621, W=210, H=192)
 *   - 标签1 听言解意篇(X=-13,  Y=570, W=106, H=210 §24b)—— 字号 14sp,行间距 16sp,5×16=80dp = 容器 80dp
 *   - 标签2 正心守道录(X=168, Y=345, W=74,  H=150 §24b)—— 字号 12sp,行间距 15sp,5×15=75dp < 容器 80dp ✓
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
/**
 * 后山10 页的所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 * (2026-09-18 §5 真机验证根因:多 lambda 签名 → 1 个 data class,bug 触发条件消失)
 */
data class Houshan10Actions(
    val onBack: () -> Unit = {},
    // ── 2026-09-19 §3:后山11 创建 —— 「正心守道录」不再是死区,改为 dolly 推进到后山11 ──
    //   用户指令:"点击任意页面的'正心守道录'标签"(§3 澄清回答)
    val onOpenHoushan11: () -> Unit = {},   // 正心守道录(本页 Y=345,非 Y 最大)→ dolly 推进到后山11
    // ── 卷跳转:按 §15 规则,仅"本页 Y 最大标签"跳卷 ──
    val onOpenVolume9Part1: () -> Unit = {},   // 听言解意篇(本页 Y 最大,Y=570)→ 第九卷-1
    //    注:后山10 只有 2 个标签(原后山8 的标签3/4 已按用户指令删除)
)

@Composable
fun Houshan10Screen(
    actions: Houshan10Actions = Houshan10Actions(),
) {
    // §3 + 2026-09-18 §4:用 data class 包成 1 个参数,bug 触发条件(slot 0 = lambda)消失,无需 safeXxx 兜底
    // (旧版的 if (xxx == null) ({}) else xxx 5 行兜底已删)

    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 2026-09-18 §11:过渡期间禁用返回手势,避免动画途中被中断而露出半程画面(同 §4 后山5)
    BackHandler(enabled = !isTransitioning) { actions.onBack() }   // 2026-09-18 §15 恢复:返回上一页

    // 熊猫上下浮 + 呼吸缩放(与后山1 §21 / 后山2 §22 同款参数;2026-09-18 §4 后山6 沿用)
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

    // ── 6 个动画云元素的进度:与后山 1/2/4 完全同一套(周期 21.4/11/8/10/9/13 s)──
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(与后山 1/2/4 同一套;§21f 用户指令"不考虑间距了")──
    val o58Progress = rememberCloudProgress(4_700, "old58")
    val o61Progress = rememberCloudProgress(9_700, "old61")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o57Progress = rememberCloudProgress(8_200, "old57")
    val o60Progress = rememberCloudProgress(4_900, "old60")
    val o60bProgress = rememberCloudProgress(10_300, "old60b")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (2026-09-18 §11,沿用 §4 后山 5 的同款) ──────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 纵深推进:启动动画,并在半程把控制权交给导航(后山11 交叉淡入)。
    //   2026-09-19 §3:从"未启用的模板"改为**启用** —— 本页「正心守道录」标签点击即触发;
    //   同时改为接收跳转目标的参数化形式(本页听言解意篇仍是直接跳卷9,不走 dolly)。
    val startDollyIn: (() -> Unit) -> Unit = { onComplete ->
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
                onComplete()   // 2026-09-19 §3:dolly 半程后推进到后山11
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 2026-09-18 §11:整屏 clickable 触发 dolly-in(取代 §10 的 noop 终点语义)
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
            // 全屏背景图(后山页背景.png —— 与直接复用源后山6 同一张;后山6 又复用自后山4)
            Image(
                painter = painterResource(R.drawable.img_shilian_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 内容层(避开系统导航条)— 标签 Box 自带 clickable 消费事件,点击标签不会冒泡触发 dolly
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

            // ══ §21 动画云元素竖排(6 个,与后山 4 同一套)══════════════════════
            // 与后山 1/2/4 完全同一套 y 序列、间隔、×0.75 尺寸
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

            // ══ §21f 6 朵老云(与后山 1/2/4 同一套)══════════════════════════════
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
            }   // 2026-09-18 §11 景深平面 2 结束

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
            // 熊猫图像(沿用 §22 后山 2 / §21 后山 1 / 2026-09-18 §4 后山 6 的同款)— X=184, Y=621, W=210, H=192
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

            // "标签1" 图像(听言解意篇,X=-13, Y=570, W=106, H=210 §24b)— 2026-09-19 §2:点击 → **第九卷-1**(本页 Y 最大)
            Box(
                modifier = Modifier
                    .offset(x = -13.dp, y = 570.dp)
                    .size(width = 106.dp, height = 210.dp)
                    .clickable(
                        enabled = !isTransitioning,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = actions.onOpenVolume9Part1,   // 2026-09-19 §2 本页 Y 最大(听言解意篇)→ 卷9
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "听\n言\n解\n意\n篇",
                    color = Color.Black,
                    style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 16.sp),
                    modifier = Modifier
                        .offset(x = 46.dp, y = 66.dp)
                        .size(width = 14.dp, height = 84.dp),
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

            // "标签2" 图像(正心守道录,X=168, Y=345, W=74, H=150 §24b) — 2026-09-19 §3:点击 → **dolly 推进到后山11**
            Box(
                modifier = Modifier
                    .offset(x = 168.dp, y = 345.dp)
                    .size(width = 74.dp, height = 150.dp)
                    .clickable(
                        enabled = !isTransitioning,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { startDollyIn(actions.onOpenHoushan11) },   // 2026-09-19 §3 dolly → 后山11(该文本在后山11 是 Y 最大)
                    ),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_recovered_4),
                    contentDescription = "标签2",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
                Text(
                    text = "正\n心\n守\n道\n录",
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

            }   // 2026-09-18 §11 景深平面 3 结束

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
