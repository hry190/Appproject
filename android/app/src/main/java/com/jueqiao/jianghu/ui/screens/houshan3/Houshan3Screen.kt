package com.jueqiao.jianghu.ui.screens.houshan3

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

// 文件级常量 TWO_PI 已移到 ui/components/FocusCloudBand.kt 和 AnimatedCloudImage.kt
// 这里不再需要 TWO_PI 声明

// ── 后山3 → 后山4 沉浸式纵深推进(dolly-in)参数 (§24,沿用后山2 §36 的同款参数)───────
// 总时长落在用户要求的 0.8~1.2s 区间;ease-in-out 用 FastOutSlowInEasing
// (cubic-bezier 0.4, 0.0, 0.2, 1.0,即标准缓入缓出)。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山4 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心,与后山4 熊猫/标签所在高度对齐
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;点击标签2-4 之外的空白区域触发 dolly-in 推进到后山4 页(§24)。
 *
 * 2026-09-17 §24 新增纵深推进过渡动画(沿用后山2 §36):
 *   1. 三个景深平面绕同一灭点 [FOCAL_X, FOCAL_Y] 以不同幅度放大 —
 *      背景山体 ×1.34(向用户靠近)、云雾 ×1.09 并淡出(相对后移)、标签 ×1.34 并淡出
 *      (与山体同速,不产生相对滑动;"跳动"即由此避免)
 *   2. 推进到半程 (DOLLY_HANDOFF_MS) 调 onOpenHoushan4(),后山4 由
 *      JianghuNavHost 的 enterTransition 交叉淡入,读作镜头减速停稳,因此全程无闪切
 *   3. 返回按钮只淡出不缩放(属 UI chrome,不应随景深放大)
 *
 * §24 反转 §21o:跳转目标由"未完待续页"改为"后山4页"(用户新建)。
 *
 * 布局:
 *   - 全屏背景图(试炼转换.png — §10 替换)
 *   - 云雾层(程序化水墨云海,持续循环 — HoushanMistVariant.Houshan3 位置表)(§37/§38)
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)
 *   - 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— **上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s (§44)**
 *   - 标签2 图像(X=124, Y=521, W=96, H=170)+ 文字"拆招心法"(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14)+ 文字"炼"(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12)
 *   - 标签3 图像(X=43, Y=390, W=51, H=91)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6)
 *   - 标签4 图像(X=105, Y=295, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4)
 *   - §21 动画云元素 **5 个**(竖排,间隔 60~69dp,×0.75):ACI58 y=8 · FCB左下 y=178.5 · ACI60 y=330.5
 *     · FCB中下 y=477.5 · ACI62 y=620
 *     §21n 用户指令删掉了原本的第 6 个(A CI57,y=754.5)—— 后山1/2 仍保留,故三页不再完全一致。
 *   - §21f 老云 5 朵(old/56/58/57/5)— 与 §21 动画云元素同属景深平面 2(雾 + ACI + 老云 = 大气层)
 */
@Composable
fun Houshan3Screen(
    onBack: () -> Unit = {},
    // §24:反转 §21o —— 后山 3 不再跳转未完待续页,改为 dolly-in 推进到新建的后山 4 页
    onOpenHoushan4: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步 (§24,与后山2 §36 同款)
    val dolly = remember { Animatable(0f) }

    // 过渡期间禁用返回手势,避免动画途中被中断而露出半程画面 (§24,与后山2 §36 同款)
    BackHandler(enabled = !isTransitioning) { onBack() }

    // 熊猫上下浮 + 呼吸缩放 (§44,与后山1 §21 同款动画)
    // Scale 0.95~1.05 / 3s + Y ±10 dp / 4s,都用 LinearEasing + RepeatMode.Reverse → 来回无缝
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

    // §21 5 朵 ACI 动画云进度(§21n 删掉了第 6 个)
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")

    // §21f 5 朵老云动画进度(后山 3 多一朵 _old — 8f5a28c 基线起就静态,本次 §21f 也动画化)
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (§24,沿用后山2 §36) ──
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 整屏点击:启动纵深推进,并在半程把控制权交给导航(后山4 交叉淡入)
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
                onOpenHoushan4()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // §24:整屏 clickable 触发 dolly-in(取代原来直接 onOpenHoushan4)
            .clickable { startDollyIn() },
    ) {
        // ── 景深平面 1:背景山体(推进最多 → "向用户靠近")────────────────────
        // 单独一层 fillMaxSize 包裹,使其自身边界 = 屏幕,transformOrigin 才能
        // 表达屏幕空间灭点;缩放后仍 ContentScale.Crop 铺满,不会露边。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    transformOrigin = focal
                },
        ) {
            // 全屏背景图(试炼转换.png,§10 从"后山3 转换.png"替换;长宽比 0.449 一致,ContentScale.Crop 适配)
            Image(
                painter = painterResource(R.drawable.img_shilian2_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 内容层(避开系统导航条)— 3 个标签 Box 自带消费事件 clickable (§20),点击标签不会冒泡触发 dolly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
            // 把 §37/§38 的程序化云雾层、§21 的 5 朵 ACI 动画云、§21f 的 5 朵老云动画
            // 全部包到一个 graphicsLayer(scale = cloudScale, alpha = cloudFade) 里 → dolly 时
            // 三类云一起 ×1.09 放大 + 一起淡出,层次一致,不会"留在屏上不动"。
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
                // 云雾层(程序化水墨云海,持续循环)— 属大气中层 (§37/§38)
                // 用 Houshan3 变体:背景 img_shilian2_bg 与后山1/2 的 img_shilian_bg 是两张不同画作,
                // 山峰位置不同,故坐标表单独调(远山 / 熊猫所在山谷 / 前景山脚);周期与配色与后山1/2 完全共用
                HoushanMistLayer(variant = HoushanMistVariant.Houshan3)

                // ══ §21 动画云元素竖排(本页 **5 个**,§21n 删掉了第 6 个)════════════════
                // 与后山1/后山2 原本是**完全同一套** y 序列、间隔(69/62/66/60)、×0.75 尺寸;
                //   §21n 之后本页少一个元素,三页不再完全一致。
                // 几何:5 个元素铺满 8..693.5dp;可用高度 = 873 − 16(导航栏)= 857dp。
                // y 序列(顶边):ACI58 8 · FCB左下 178.5 · ACI60 330.5 · FCB中下 477.5 · ACI62 620
                // amplitudeY 收到 8~10dp(原 42/36dp 会把 60~69dp 的间隔上下吃光);amplitudeX 仍 200dp。
                // 放在景深平面 2 内 → §24 过渡推进时随云雾一起 scaleX/Y + 淡出,层次一致,不会"留在屏上不动"。
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
                    // §21h:近白素材(250/0% 带色)叠浅底看不见 → 冷青 tint 提对比
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
                // §21n 用户指令"删掉后山3页面最底部的云" → 此处原本是竖排栈的第 6 个 **ACI57**
                //   (img_houshan1_cloud_57,x=146 y=754.5,100×90;§21b 加入、§21m 加 tint),已删除。
                //   后山3 的最底部**仍然有动画云** —— §21f 的老云 56(y=755,335×297)就在这个位置,
                //   所以"最底部一定要有一个动画素材"这条要求仍然满足,只是不再由竖排栈提供。

                // ══ §21f 5 朵老云(重新动画;间距约束已放弃)══════════════════════════
                // 放在景深平面 2 内 → dolly 过渡时随云雾一起缩放淡出,层次不变。
                // 位置/尺寸沿用原值;alpha 恢复老动画区间 0.75±0.25;白色脉冲关闭(水彩云不叠白光)。
                // 风格与后山1/后山2 一致:大云 Oscillate 留在原地,扁长的走单向回绕(2 右 1 左)。
                // 旧云朵(8f5a28c 基线,重命名为 _old 避免命名冲突 §35)— §21f 起也动画化(Oscillate)
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
                // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 在云朵下层
                // §44 加:同后山1 §21 的"上下浮 + 呼吸缩放"动画(Y=405+pandaDy,graphicsLayer 缩放)
                // §24:放在景深平面 3 内 → 推进时与 3 个标签同速 ×1.34 缩放 + 一起淡出
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

                // "标签3" 图像(未标题-1-恢复的-恢复的 4.png,X=43, Y=390, W=51, H=91)
                Box(
                    modifier = Modifier
                        .offset(x = 43.dp, y = 390.dp)
                        .size(width = 51.dp, height = 91.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签3",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    // "万象谱" 竖排文字(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10, 黑色, YaHei)— 相对位置参照后山2 标签3
                    Text(
                        text = "万\n象\n谱",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                        modifier = Modifier
                            .offset(x = 20.5.dp, y = 25.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    // "炼" 文字(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6, 颜色 #385816, YaHei)— 相对位置参照后山2 标签3
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                        modifier = Modifier
                            .offset(x = 22.dp, y = 12.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像(未标题-1-恢复的-恢复的 4.png,X=105, Y=295, W=30, H=53.5)
                Box(
                    modifier = Modifier
                        .offset(x = 105.dp, y = 295.dp)
                        .size(width = 30.dp, height = 53.5.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签4",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    // "寻径迷踪步" 竖排文字(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4, 黑色, YaHei)— 相对位置参照后山2 标签4
                    Text(
                        text = "寻\n径\n迷\n踪\n步",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 14.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    // "炼" 文字(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4, 颜色 #385816, YaHei)— 相对位置参照后山2 标签4
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 7.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签2" 图像(未标题-1-恢复的-恢复的 4.png,X=124, Y=521, W=96, H=170)
                Box(
                    modifier = Modifier
                        .offset(x = 124.dp, y = 521.dp)
                        .size(width = 96.dp, height = 170.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},  // 消费事件,阻止冒泡到整屏 clickable (§20)
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签2",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    // "拆招心法" 竖排文字(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14, 黑色, YaHei)— 相对位置参照后山2 标签2
                    Text(
                        text = "拆\n招\n心\n法",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        modifier = Modifier
                            .offset(x = 43.dp, y = 48.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    // "炼" 文字(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12, 颜色 #385816, YaHei)— 相对位置参照后山2 标签2
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
                    .clickable(enabled = !isTransitioning, onClick = onBack),
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