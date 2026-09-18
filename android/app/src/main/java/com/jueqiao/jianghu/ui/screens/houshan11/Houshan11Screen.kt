package com.jueqiao.jianghu.ui.screens.houshan11

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

// ── 后山11 的沉浸式纵深推进(dolly-in)参数 (**当前未启用** —— 后山11 是终点页)──
// 保留这套常量作为模板:若日后要接"后山12",把某个标签的 onClick 改为 startDollyIn(...) 即可
// (同 2026-09-19 §2 后山10 的做法;那次保留了模板,这次后山11 用上了同样套路)。
// 参数沿用后山2 §2026-09-16.36 首创,经后山3 §24 / 后山4 §33 / 后山5 §4 / 后山6 §9 /
//   后山7 §10 / 后山8 §11 / 后山9 §2 / 后山10 §2 演进的同款。
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
 * 后山11 页 — 由后山8/9/10 的「正心守道录」标签 dolly 推进而来;点击"返回"按钮回到上一页;
 * 整屏点击 noop(**终点页**);点击唯一标签「正心守道录」→ 第十卷-1。
 *
 * 2026-09-19 §3 新建:用户指令"继续创建后山11页面,那种山峰拉近的动画也要在跳转到后山11页面时出现,
 *   后山11页面复用后山9页面的素材和动画,在后山11页面,把标签'赏罚驭灵诀'的文本改成'正心守道录',
 *   删掉'听言解意篇'标签和页面原有的'正心守道录'标签,并且应用之前说的Y值最大的标签可以跳转到对应的卷-1"。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【与后山9 的关系】**素材 / 动画 100% 复用,但标签从 3 个减到 1 个**
 *
 *   复用(与后山9 一致):
 *     - 全屏背景图 img_shilian2_bg.png
 *     - 云雾层 HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
 *     - 5 朵 ACI 动画云(58 / FCB左下 / 60 / FCB中下 / 62)+ 5 朵老云(old / 56 / 58 / 57 / 5)
 *     - 熊猫 img_shilian2_recovered_8,X=118 Y=405 W=181 H=96(同款动画)
 *     - dolly 三景深平面参数(同款,当前**未启用**)
 *
 *   改造(与后山9 不同)—— **标签 3 → 1**:
 *     | 位置 | 后山9 原文本 | 后山11 新文本 | Y | 说明 |
 *     |---|---|---|---|---|
 *     | 标签2(X=124)| 赏罚驭灵诀 | **正心守道录** | **521** | 改名;**成为本页 Y 最大**(也是全局 Y 最大)|
 *     | ~~标签3(X=43)~~  | ~~听言解意篇~~ | **已删除** | — | 用户指令删除 |
 *     | ~~标签4(X=105)~~ | ~~正心守道录~~ | **已删除** | — | 用户指令删除(页面原有的) |
 *
 *     - ❌ 整屏点击是**死区**(noop)—— 后山11 是终点页;dolly 代码保留为模板但**未启用**
 * ══════════════════════════════════════════════════════════════════════════
 * 【点击行为 · 当前】
 *
 *   | 点击位置                  | 结果                          |
 *   |--------------------------|-------------------------------|
 *   | 标签2 **正心守道录**(Y=521)| → **第十卷-1**(本页 Y 最大,按 §15 规则)|
 *   | 其余任意位置(空白/云/熊猫)| → ❌ noop(终点页)|
 *   | 左上角返回按钮 / 系统返回键 | → 上一页(后山8/9/10,取决于从哪进入)|
 * ══════════════════════════════════════════════════════════════════════════
 * 【本页在导航体系里的角色】—— 2026-09-19 §3 起
 *
 *   后山11 是 **「正心守道录」的 Y 最大页面**(全链最大):
 *     - 正心守道录:后山8=248 < 后山9=295 < 后山10=345 < **后山11=521** ✓
 *   → 按 §15 规则 1(本页 Y 最大标签 → 其文本对应的卷-1),本页标签跳 **第十卷-1**。
 *   → 同时按用户 2026-09-19 §3 的决定,**后山8/9/10 三页的「正心守道录」标签全部改为
 *     dolly 推进 → 后山11**(用户原话:"点击任意页面的'正心守道录'标签")。
 *   → 这条链至此闭合:「正心守道录」不再有待接的死区。
 *
 * 布局(坐标与后山9 的标签2 完全一致):
 *   - 全屏背景图(img_shilian2_bg.png)
 *   - 5 朵 ACI 动画云 + 5 朵老云
 *   - 熊猫图像(X=118, Y=405, W=181, H=96)
 *   - 标签2 正心守道录(X=124, Y=521, W=96, H=170)—— 字号 14sp,行间距 14sp,5×14=70dp < 容器 80dp ✓
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
/**
 * 后山11 页的所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 * 详见 §5(SESSION-LOG-2026-09-18)真机验证根因:多 lambda 签名 → 1 个 data class,bug 触发条件消失。
 */
data class Houshan11Actions(
    val onBack: () -> Unit = {},
    // ── 2026-09-19 §3:后山11 是终点页(整屏 noop)。按 §15 规则,仅"本页 Y 最大标签"跳卷 ──
    val onOpenVolume10Part1: () -> Unit = {},   // 正心守道录(本页 Y 最大,Y=521)→ 第十卷-1
    // 注:后山11 只有 1 个标签(原后山9 的标签3/4 已按用户指令删除)
)

@Composable
fun Houshan11Screen(
    actions: Houshan11Actions = Houshan11Actions(),
) {
    // §3 + 2026-09-18 §4:用 data class 包成 1 个参数,bug 触发条件(slot 0 = lambda)消失,无需 safeXxx 兜底
    // (旧版的 if (xxx == null) ({}) else xxx 5 行兜底已删)

    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 2026-09-18 §11:过渡期间禁用返回手势,避免动画途中被中断而露出半程画面(同 §4 后山5)
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

    // §21 5 朵 ACI 动画云进度(与直接复用源后山9 同一套周期)
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")

    // §21f 5 朵老云动画进度(与直接复用源后山9 同一套)
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (**当前未启用** —— 后山11 是终点页) ──
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 纵深推进:启动动画,并在半程把控制权交给导航(下一页交叉淡入)。
    // 2026-09-19 §3:后山11 是终点页 → **当前无调用点**,保留为模板(同后山8/10 的做法)。
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
                onComplete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 2026-09-19 §3:整屏 clickable 保持 **noop** —— 本页是终点页,唯一跳转入口是标签2。
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
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
            // 全屏背景图(试炼转换.png —— 与直接复用源后山9 同一张;沿革:后山9←后山7←后山5←后山3)
            Image(
                painter = painterResource(R.drawable.img_shilian2_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // 云雾层(程序化水墨云海,持续循环)— 用 HoushanMistVariant.Houshan3 变体(与后山9 同)
            //   注意:"Houshan3" 是变体枚举名(背景是那张水墨画作),与"复用源是后山几"无关
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
                // ══ §21 动画云元素竖排(5 个,与后山9 完全同一套)════════════════
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

                // ══ §21f 5 朵老云(与后山9 完全同一套)════════════════════════════
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
                // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 与直接复用源后山9 同
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

                // "标签2" 图像(正心守道录,X=124, Y=521, W=96, H=170)
                //   2026-09-19 §3:位置沿用后山9 的「赏罚驭灵诀」槽位,文本按用户指令改为「正心守道录」;
                //   它是本页唯一标签,也是"正心守道录"全链 Y 最大(521)→ 点击跳第十卷-1。
                //   字号/行间距沿用原槽位的 14sp/14sp(5×14=70dp < 容器 80dp ✓),字数同为 5 字,无需调整。
                Box(
                    modifier = Modifier
                        .offset(x = 124.dp, y = 521.dp)
                        .size(width = 96.dp, height = 170.dp)
                        .clickable(
                            enabled = !isTransitioning,
                            onClick = actions.onOpenVolume10Part1,   // 2026-09-19 §3 本页 Y 最大(正心守道录)→ 卷10
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
                        // 行间距 14sp(等于字号,字符紧凑);5×14=70dp,容器 80dp 有 10dp 余量
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
