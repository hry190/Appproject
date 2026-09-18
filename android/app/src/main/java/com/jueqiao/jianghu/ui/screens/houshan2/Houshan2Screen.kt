package com.jueqiao.jianghu.ui.screens.houshan2

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

// ── 后山2 → 后山3 沉浸式纵深推进(dolly-in)参数 ──────────────────────────────
// 总时长落在用户要求的 0.8~1.2s 区间;ease-in-out 用 FastOutSlowInEasing
// (cubic-bezier 0.4, 0.0, 0.2, 1.0,即标准缓入缓出)。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山3 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心,与后山3 熊猫/标签所在高度对齐
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山2 页 — 原为"后山1 页 → 点击整屏纵深推进过渡到后山3 页";⚠️ **2026-09-18 §13 起整屏点击与返回的导航均已断开**(待重设)。
 *
 * 2026-09-15 §18 重写:基于 Houshan1Screen.kt 复制,保留全部 6 朵云动画 + 4 个标签 +
 * 返回按钮,去掉熊猫 (img_shilian_panda) 和 Rectangle156 气泡及文字。
 *
 * 2026-09-15 §36 加"沉浸式过渡动画"(用户指令):点击整屏不再是硬切,而是相机穿行山间:
 *   1. 三个景深平面绕同一灭点 [FOCAL_X, FOCAL_Y] 以不同幅度放大 —
 *      背景山体 ×1.34(向用户靠近)、云雾 ×1.09 并淡出(相对后移)、标签 ×1.34 并淡出
 *      (与山体同速,不产生相对滑动;"跳动"即由此避免)
 *   2. 推进到半程 (DOLLY_HANDOFF_MS) 调 onOpenHoushan3(),后山3 由
 *      JianghuNavHost 的 enterTransition 交叉淡入 + 轻微回落(1.10 → 1.00),
 *      读作镜头减速停稳,因此全程无闪切
 *   3. 返回按钮只淡出不缩放(属 UI chrome,不应随景深放大)
 *
 * 布局:
 *   - 全屏背景图 (img_shilian_bg.png)
 *   - 6 朵云 (58/61/56/57/60/60b) —— §21 起改为**静态图层**(用户指令"老云保留为静态、去掉动画")
 *   - §21 动画云元素 6 个(竖排,间隔 60~69dp,前 5 个 ×0.75):ACI58 y=8 · FCB左下 y=178.5 · ACI60 y=330.5
 *     · FCB中下 y=477.5 · ACI62 y=620 · **ACI57 y=754.5(100×90,§21b 实图云 PNG)**
 *     第 6 个底边 844.5dp,距导航栏上沿 857dp 留 12.5dp
 *   - 标签1 图像 (X=-13, Y=570, W=106, H=188) + 文字"识机真决" + 文字"炼"
 *     ⚠️ **2026-09-18 §12 取消跳转** —— 原为滚动到第一卷-1(`onOpenVolume1`),现为死区(仅消费事件)
 *   - 标签2 图像 (X=168, Y=345, W=74, H=131) + 文字"拆招心法" + 文字"炼"
 *     ⚠️ **2026-09-18 §12 取消跳转** —— 原为滚动到第二卷-1(`onOpenVolume2Part1`),现为死区
 *   - 标签3 图像 (X=113, Y=322, W=50, H=88) + 文字"万象谱" + 文字"炼"
 *     ⚠️ **2026-09-18 §12 取消跳转** —— 原为滚动到第三卷-1(`onOpenVolume3Part1`),现为死区
 *   - 标签4 图像 (X=151, Y=248, W=30, H=53.5) + 文字"寻径迷踪步" + 文字"炼"
 *     ⚠️ **2026-09-18 §12 取消跳转** —— 原为滚动到第四卷-1(`onOpenVolume4Part1`),现为死区
 *
 * ⚠️ **2026-09-18 §12 用户指令:"取消所有标签的跳转,我要重新设置"** —— 后山2 的 4 个标签跳转**已全部取消**。
 * ✅ **2026-09-18 §14 部分恢复** —— 仅 **Y 值最大的标签**(页面上最下方那个)恢复跳转到"其文本对应的卷的第一页";
 *    其余标签**仍为死区**,等用户继续设置。
 *    → 标签现在是**死区**:`.clickable(..., onClick = {})` 仅消费点击事件(防止冒泡触发 dolly),**不跳转**。
 *    → `Houshan2Actions` 的 4 个 `onOpenVolume*` 字段**保留**(等用户给新映射表后重设;届时只需恢复
 *      本文件的 `onClick` + `JianghuNavHost` 的回调两处)。
 *    → **取消前的映射(留档,供重设参考)**:识机真决 → 卷1 · 拆招心法 → 卷2 · 万象谱 → 卷3 · 寻径迷踪步 → 卷4
 *    → 4 个标签写法统一为:`​.clickable(enabled = !isTransitioning, onClick = {})`
 *   - 左上角返回按钮 (Return.png, X=30, Y=60, W=18, H=18)
 *
 * 删除元素(§18,部分保留):
 *   - Rectangle156.png 气泡 + 文字"御剑穿行云雾群山..." — §18 决定,不复制(后山 2 是过场页,不应有信息气泡)
 *
 * 2026-09-17 §22 反转 §18:重新加回熊猫 (img_shilian_panda),沿用 §21 的"上下浮 ±10/4s + 呼吸缩放 0.95~1.05/3s"动画。
 * 放在景深平面 3(与 4 个标签同层),推进时与标签同速缩放 ×1.34 + 一起淡出(详见函数体内 pandaTransition 处注释)。
 */
/**
 * 后山2 页所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 * 详见 §5(SESSION-LOG-2026-09-18)真机验证根因:多 lambda 签名 → 1 个 data class,bug 触发条件消失。
 */
data class Houshan2Actions(
    val onBack: () -> Unit = {},
    val onOpenHoushan3: () -> Unit = {},
    val onOpenVolume1: () -> Unit = {},         // 识机真决 → 第一卷-1 (§22)
    val onOpenVolume2Part1: () -> Unit = {},    // 拆招心法 → 第二卷-1 (§28)
    val onOpenVolume3Part1: () -> Unit = {},    // 万象谱     → 第三卷-1 (§29)
    val onOpenVolume4Part1: () -> Unit = {},    // 寻径迷踪步 → 第四卷-1 (§30)
)

@Composable
fun Houshan2Screen(
    actions: Houshan2Actions = Houshan2Actions(),
) {
    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 过渡期间禁用返回手势,避免动画途中被中断而露出半程画面
    BackHandler(enabled = !isTransitioning) { }   // 2026-09-18 §13 断开导航(待重设)

    // 2026-09-17 §22:按用户指令反转 §18,重新加回熊猫 (img_shilian_panda),沿用 §21 的
    // "上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s" 动画参数;放在景深平面 3(与 4 个标签同层),
    // 推进时与标签同速缩放 ×1.34 + 一起淡出,语义上"前景角色随镜头前移后退出画面"。
    // Rectangle156 气泡仍不复制 —— §18 决定保留,理由:后山 2 是过场页,不应有信息气泡。
    // §21 起 6 朵老云本想去动画,§21f 按用户指令"不考虑间距了"又重新加回动画。

    // ── 6 个动画云元素的进度:每个元素独立周期(与后山1/后山3 完全同一套)────
    // §21k 用户反馈"最顶部的云速度太快了" → #1 ACI58 周期 7s → 10.7s;
    // §21l 用户复反馈"速度还是快了,速度调成一半" → 再减半 → **21.4s**
    //   (114.3 → 74.8 → **37.4 dp/s**;现在它是全页最慢,最快的是 #5 ACI62 的 88.9)
    // 当前六个周期(上→下):21.4 / 11 / 8 / 10 / 9 / 13 s
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 6 朵老云的动画(与后山1/后山3 同一套;用户指令"把 6 朵静态老云的动画也做出来")──
    // 间距约束已放弃;复用 §21c 的 CloudMotion 三模式(替代当初的 rememberCloudFloat 随机游走)。
    // §21i 用户指令"老云的移动速度要向其他的云朵一致" → 周期按"平均速度对齐"重算:
    //   目标 = 竖排栈 6 个的平均速度 **75.9 dp/s**(栈内 52~114);
    //   摆动模式 平均速度 = 4A/T,单向模式 = (屏宽+元素宽)/T。
    //   结果:58 4.7s→76.6、61 9.7s→77.1、56 3.7s→75.7、57 8.2s→75.4、60 4.9s→73.5、60b 10.3s→72.6 dp/s
    // 白色脉冲关闭(老云是画好的水彩云,不再叠白光)。
    val o58Progress = rememberCloudProgress(4_700, "old58")
    val o61Progress = rememberCloudProgress(9_700, "old61")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o57Progress = rememberCloudProgress(8_200, "old57")
    val o60Progress = rememberCloudProgress(4_900, "old60")
    val o60bProgress = rememberCloudProgress(10_300, "old60b")

    // ── §22 熊猫动画(沿用后山1 §21:Scale 0.95~1.05 / 3s, Y ±10 dp / 4s, RepeatMode.Reverse)──
    // 后山2 是过场页(整屏点击 dolly-in → 后山3;§13 起导航已断开),用户要求保留熊猫,放在景深平面 3(与4 个标签同层)
    // —— 推进时与标签同速缩放 (×1.34) + 同速淡出,语义上"前景角色随镜头前移后退出画面",最自然
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

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (§36) ──────────────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 2026-09-18 §13:整屏点击的导航已断开(dolly 代码保留为模板,未被调用)。原动作 = 推进到半程后交给导航 → 后山3
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
                actions.onOpenHoushan3()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),   // 2026-09-18 §13 断开导航(待重设)
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
            // 全屏背景图 (后山页背景.png)
            Image(
                painter = painterResource(R.drawable.img_shilian_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 内容层(避开系统导航条)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
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
                // 云雾层(程序化水墨云海,持续循环)— 属大气中层 (§37)
                // 放在景深平面 2 之内:过渡推进时与云雾一起"相对后移 + 淡出",层次一致
                HoushanMistLayer()

                // ══ §21 动画云元素竖排(6 个)══════════════════════════════════════
                // 与后山1/后山3 完全同一套 y 序列、同一套间隔(69/62/66/60/61)、同一套 ×0.75 尺寸,
                // 满足"三页效果统一"。用户指令:间隔 60~90dp 定死一组;最底部(导航栏上方)必须有
                // 一个动画素材 → 第 6 个 = ACI57(§21b 按用户指令从 FocusCloudBand 改为实图云 PNG),
                // 底边 844.5dp,距 857dp 留 12.5dp。
                // amplitudeY 收到 8~10dp(原 42/36dp 会把 60~69dp 的间隔上下吃光);amplitudeX 仍 200dp。
                // 放在景深平面 2 内 → §36 过渡推进时随云雾一起 scaleX/Y + 淡出,层次一致,不会"留在屏上不动"。
                // §21c/§21d 横向模式分配 —— **按"观感可见性"分配**(与后山1/后山3 一致):
                //   三朵 ACI 的 PNG 是 250/255/237 的近白/纯白,浅底上几乎不可见;
                //   两条 FocusCloudBand 是冷青 #A9C3C0,浅底上对比明显 → 把**看得见的**放进单向组。
                //   #1 ACI58 摆动(7s) · #2 FCB左下 **单向→右**(11s) · #3 ACI60 **单向→右**(8s)
                //   #4 FCB中下 **单向←左**(10s) · #5 ACI62 摆动(9s) · #6 ACI57 摆动(13s,底部,永远在屏上)
                // §21d/§21e:3 个单向 = **2 个向右(FCB左下 + ACI60)+ 1 个向左(FCB中下)**,
                //   保证两个方向各有一个看得见的元素。
                // 单向模式忽略 xOffset / amplitudeX,故两者传 0f。
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -27f, yOffset = 8f,
                    widthDp = 180f, heightDp = 101.5f,
                    progress = c58Progress, phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                    // §21h:近白素材(250/0% 带色)叠浅底看不见 → 冷青 tint 提对比
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
                // 第 6 个 = 最底部素材(§21b):用户指令从 FocusCloudBand 换成 AnimatedCloudImage(实图云 PNG)。
                // 底边 844.5dp,距导航栏上沿(857dp)留 12.5dp。选 cloud_57 而非扁长的 cloud_60:
                // cloud_60 实测纯白 (255,255,255),叠在近纯白的底部背景上像素差为 0(等于没加);
                // cloud_57 实测 MeanLum 221 / 72% 像素带色调 → 真正看得见(详见 §21 素材亮度表)。
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_57),
                    contentDescription = "云朵57",
                    xOffset = 146f, yOffset = 754.5f,
                    widthDp = 100f, heightDp = 90f,
                    progress = c57Progress, phase = 0.71f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f, amplitudeY = 10f,
                    baseAlpha = 0.50f, alphaAmp = 0.25f,
                    // §21m:按各朵**所在竖带的实测背景亮度**决定 tint。ACI57 在浅底部(234),
                    //   素材 cloud_57 亮度 221 → 对比度只有 6.3;Modulate 冷青后约 162 → 对比度 35.8
                    tint = CloudTintCool,
                )

                // ══ §21f 6 朵老云(重新动画;间距约束已放弃)══════════════════════
                // 位置/尺寸沿用原值(注意 60b 是 **Y=770**,用户真机调过的值,§21 曾被我误改成 760,本次修回);
                // alpha 恢复老动画区间 0.75±0.25;云 56 按 §7 用户指令恒为 1f。
                // 仍在景深平面 2 内 → dolly 过渡时随云雾一起缩放淡出,层次不变。
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

                // 云朵 60b — **Y=770**(用户真机调过的值;§21 误改为 760,§21f 修回)
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

            // ── 景深平面 3:标签(与山体同速推进 + 淡出 → 不相对滑动)──────────
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
                // 熊猫图像 (img_shilian_panda, X=184, Y=621, W=210, H=192) — 上下浮 ±10 / 4s + 呼吸缩放 0.95~1.05 / 3s (§21,§22 复制到后山2)
                // 放在景深平面 3 内 4 个标签之前 → 推进时与标签同速缩放 ×1.34 + 一起淡出
                // (后山1 没有 dolly,所以原版没这层行为;后山2 是过场,推进中熊猫自然前移+退场,层次与标签一致)
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

                // "标签1" 图像 (未标题-1-恢复的-恢复的 4.png, X=-13, Y=570, W=106, H=188) — 点击跳转第一卷-1 (§22)
                Box(
                    modifier = Modifier
                        .offset(x = -13.dp, y = 570.dp)
                        .size(width = 106.dp, height = 188.dp)
                        .clickable(enabled = !isTransitioning, onClick = actions.onOpenVolume1)   /* 2026-09-18 §14 恢复跳转(其余标签仍待设置) */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签1",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    // "识机真决" 竖排文字
                    Text(
                        text = "识\n机\n真\n决",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp),
                        modifier = Modifier
                            .offset(x = 46.dp, y = 54.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    // "炼" 文字
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 48.dp, y = 27.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签2" 图像 (X=168, Y=345, W=74, H=131) — 点击跳转第二卷-1 (§28)
                //   §28 前它是"死区"(.clickable(指示器=null, onClick = {}) 仅消费事件、
                //   阻止冒泡到整屏 dolly);§28 起改为真实跳转,clickable 写法与标签1 对齐
                //   (enabled = !isTransitioning 防止过渡动画途中误触)。
                Box(
                    modifier = Modifier
                        .offset(x = 168.dp, y = 345.dp)
                        .size(width = 74.dp, height = 131.dp)
                        .clickable(enabled = !isTransitioning, onClick = {})   /* 2026-09-18 §12 取消跳转(待重设) */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签2",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "拆\n招\n心\n法",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 34.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                        modifier = Modifier
                            .offset(x = 32.dp, y = 17.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }

                // "标签3" 图像 (X=113, Y=322, W=50, H=88) — 点击跳转第三卷-1 (§29)
                //   同 §28 标签2:从"死区"改为真实跳转,clickable 写法和标签1/2 对齐
                //   (enabled = !isTransitioning 防 dolly 途中误触;有 ripple 反馈)。
                Box(
                    modifier = Modifier
                        .offset(x = 113.dp, y = 322.dp)
                        .size(width = 50.dp, height = 88.dp)
                        .clickable(enabled = !isTransitioning, onClick = {})   /* 2026-09-18 §12 取消跳转(待重设) */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签3",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "万\n象\n谱",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp),
                        modifier = Modifier
                            .offset(x = 20.5.dp, y = 25.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                        modifier = Modifier
                            .offset(x = 23.dp, y = 14.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像 (X=151, Y=248, W=30, H=53.5) — 点击跳转第四卷-1 (§30)
                //   同 §28/§29:从"死区"改为真实跳转。
                //   ⚠️ §30 是**最后一个**从死区转活区的标签 → 本文件已无
                //      `indication = null` 的消费型 clickable,故 import
                //      `MutableInteractionSource` 一并移除(见文件头部 import 区)。
                Box(
                    modifier = Modifier
                        .offset(x = 151.dp, y = 248.dp)
                        .size(width = 30.dp, height = 53.5.dp)
                        .clickable(enabled = !isTransitioning, onClick = {})   /* 2026-09-18 §12 取消跳转(待重设) */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签4",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "寻\n径\n迷\n踪\n步",
                        color = Color.Black,
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
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
            }

            // 左上角返回按钮 (Return.png, X=30, Y=60, W=18, H=18)— ⚠️ 2026-09-18 §13 起导航已断开(原为回到后山1 页)
            // 只淡出不缩放:UI chrome 不参与景深,否则会随山体放大而"跳动"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .graphicsLayer { alpha = chromeFade }
                    .clickable(enabled = !isTransitioning, onClick = {})   /* 2026-09-18 §13 断开导航(待重设) */,
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
