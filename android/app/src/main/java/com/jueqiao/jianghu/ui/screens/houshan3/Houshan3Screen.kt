package com.jueqiao.jianghu.ui.screens.houshan3

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.AnimatedCloudImage
import com.jueqiao.jianghu.ui.components.CloudMotion
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.components.HoushanMistVariant
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei

// 文件级常量 TWO_PI 已移到 ui/components/FocusCloudBand.kt 和 AnimatedCloudImage.kt
// 这里不再需要 TWO_PI 声明

/**
 * 后山3 页 — 后山2 页 → 点击"返回"按钮回到后山2;点击标签2-4 之外的空白区域跳转未完待续页。
 *
 * 布局:
 *   - 全屏背景图(试炼转换.png — §10 替换)
 *   - 云雾层(程序化水墨云海,持续循环 — HoushanMistVariant.Houshan3 位置表)(§37/§38)
 *   - 返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 屏幕空白点击无效
 *   - 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— **上下浮 ±10dp / 4s + 呼吸缩放 0.95~1.05 / 3s (§44)**
 *   - 标签2 图像(X=124, Y=521, W=96, H=170)+ 文字"拆招心法"(父 Box 内 X=43, Y=48, W=14, H=80, 字号 14)+ 文字"炼"(父 Box 内 X=43, Y=25, W=12, H=16, 字号 12)
 *   - 标签3 图像(X=43, Y=390, W=51, H=91)+ 文字"万象谱"(父 Box 内 X=20.5, Y=25, W=12, H=60, 字号 10)+ 文字"炼"(父 Box 内 X=22, Y=12, W=10, H=14, 字号 6)
 *   - 标签4 图像(X=105, Y=295, W=30, H=53.5)+ 文字"寻径迷踪步"(父 Box 内 X=13.5, Y=14, W=12, H=60, 字号 4)+ 文字"炼"(父 Box 内 X=13.5, Y=7, W=10, H=14, 字号 4)
 *   - 旧云朵(Ellipse 58.png,X=-46, Y=476, W=331, H=92)
 *   - §21 老云全部静态化(用户指令"保留为静态图层、去掉动画"):img_shilian3_cloud_old(本已静态)、
 *     cloud_56(X=263, Y=755, 335×297)、cloud_58(X=78, Y=170, 331×92)、cloud_57(X=-25, Y=500, 225×191)、
 *     cloud_5(X=187, Y=308, 225×191)—— 去掉位置/透明度随机飘动 + 去掉 tint 色彩循环,alpha 固定 0.75
 *   - §21 动画云元素 6 个(竖排,间隔 60~69dp,前 5 个 ×0.75):ACI58 y=8 · FCB左下 y=178.5 · ACI60 y=330.5
 *     · FCB中下 y=477.5 · ACI62 y=620 · **ACI57 y=754.5(100×90,§21b 实图云 PNG)**
 *     第 6 个底边 844.5dp,距导航栏上沿 857dp 留 12.5dp
 */
@Composable
fun Houshan3Screen(
    onBack: () -> Unit = {},
    onOpenUnfinished: () -> Unit = {},
) {
    BackHandler(enabled = true) { onBack() }

    // §21:老云(56/58/57/5)已按用户指令"保留为静态图层、去掉动画"静态化,
    // 故 §23-§26 的 A9C3C0 ↔ 白 tintProgress 循环与 §28 起的 rememberCloudFloat 随机飘动全部移除

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

    // 拆招心法下方三朵云动画 (§6)— 用 Ellipse 58/60/62 三张素材
    // 资源:58/60 复用现有 img_houshan1_cloud_58/60(同一张图,fit 版本 — 见 09-15 §4);
    // 62 是新导入:D:\图\Ellipse 62.png → drawable-nodpi/img_houshan3_cloud_62.png
    // §21j 已把"0→1 线性回绕进度"抽成共享 helper `rememberCloudProgress`;
    // 每朵云各自持有一个独立周期的进度,再用 phase 错开(不再共用 1 个 rememberInfiniteTransition)。
    // §21k 用户反馈"最顶部的云速度太快了" → #1 ACI58 周期 7s → 10.7s;
    // §21l 用户复反馈"速度还是快了,速度调成一半" → 再减半 → **21.4s**
    //   (114.3 → 74.8 → **37.4 dp/s**;现在它是全页最慢,最快的是 #5 ACI62 的 88.9)
    // 当前六个周期(上→下):21.4 / 11 / 8 / 10 / 9 / 13 s,互不整除
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")
    val c57Progress = rememberCloudProgress(13_000, "cloud57")

    // ── §21f 老云的动画(用户指令"把静态老云的动画也做出来,不考虑间距了")──
    // 后山3 有 5 朵老云(比后山1/2 多一朵 img_shilian3_cloud_old —— 它从 8f5a28c 基线起就是静态的,
    // 本次也一并动画化)。间距约束已放弃;复用 §21c 的 CloudMotion 三模式。
    // §21i 用户指令"老云的移动速度要向其他的云朵一致" → 周期按"平均速度对齐"重算:
    //   目标 = 竖排栈 6 个的平均速度 **75.9 dp/s**(栈内 52~114);
    //   摆动模式 平均速度 = 4A/T,单向模式 = (屏宽+元素宽)/T。
    //   结果:old 4.3s→74.4、56 3.7s→75.7、58 9.5s→76.2、57 8.1s→76.3、5 7.9s→78.2 dp/s。
    // 白色脉冲关闭;§23-§26 的 tint 色彩循环仍不恢复(§21b 起已移除)。
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 全屏背景图(试炼转换.png,§10 从"后山3 转换.png"替换;长宽比 0.449 一致,ContentScale.Crop 适配)
        Image(
            painter = painterResource(R.drawable.img_shilian2_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 云雾层(程序化水墨云海,持续循环)— 背景之上、所有内容之下 (§37/§38)
        // 用 Houshan3 变体:背景 img_shilian2_bg 与后山1/2 的 img_shilian_bg 是两张不同画作,
        // 山峰位置不同,故坐标表单独调(远山 / 熊猫所在山谷 / 前景山脚);周期与配色与后山1/2 完全共用
        // 本层无 clickable/pointerInput → 不拦截触摸,故不影响整屏跳转与标签点击;
        // 且整层位于熊猫、标签之下,不会遮挡御剑飞行的视觉焦点
        HoushanMistLayer(variant = HoushanMistVariant.Houshan3)

        // ══ §21 动画云元素竖排(6 个)════════════════════════════════════════════
        // 用户指令:云朵素材"每两个间隔随机在 60~90dp",避免素材太密集;定死一组,不做每帧随机。
        //          最底部(导航栏上方)必须有一个动画素材 → 第 6 个即新增的底部 FocusCloudBand。
        // 与后山1/后山2 **完全同一套** y 序列、间隔(69/62/66/60/61)、×0.75 尺寸 → 三页效果统一。
        // 几何:6 个元素铺满 8..844.5dp;可用高度 = 873 − 16(导航栏)= 857dp → 间隔落在 60~90 内。
        // y 序列(顶边):ACI58 8 · FCB左下 178.5 · ACI60 330.5 · FCB中下 477.5 · ACI62 620 · ACI57 754.5
        //   第 6 个底边 844.5dp,距导航栏上沿 857dp 留 12.5dp。
        // amplitudeY 收到 8~10dp(原 42/36dp 会把 60~69dp 的间隔上下吃光);amplitudeX 仍 200dp。
        // 绘制顺序 = 背景 → 雾层 → 这 6 个 → 内容层(旧云/熊猫/标签),不遮挡任何可点区域。
        // §21c/§21d 横向模式分配 —— **按"观感可见性"分配**(与后山1/后山2 完全一致):
        //   三朵 ACI 的 PNG 是 250/255/237 的近白/纯白,浅底上几乎不可见;
        //   两条 FocusCloudBand 是冷青 #A9C3C0,浅底上对比明显 → 把**看得见的**放进单向组。
        //   #1 ACI58 摆动(7s) · #2 FCB左下 **单向→右**(11s) · #3 ACI60 **单向→右**(8s)
        //   #4 FCB中下 **单向←左**(10s) · #5 ACI62 摆动(9s) · #6 ACI57 摆动(13s,底部,永远在屏上)
        // §21d/§21e:3 个单向 = **2 个向右(FCB左下 + ACI60)+ 1 个向左(FCB中下)**,
        //   保证两个方向各有一个看得见的元素。
        // 单向模式忽略 xOffset / amplitudeX,故传 0f。
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
        // 第 6 个 = 最底部素材(§21b):用户指令从 FocusCloudBand 换成 AnimatedCloudImage(实图云 PNG)。
        // 底边 844.5dp,距导航栏上沿(857dp)留 12.5dp → 满足"最底部必须有一个动画素材"。
        // 选 img_houshan1_cloud_57 而非扁长的 cloud_60:cloud_60 实测纯白 (255,255,255),
        //   叠在近纯白的底部背景上像素差为 0(等于没加);cloud_57 实测 MeanLum 221 / 72% 带色调。
        AnimatedCloudImage(
            painter = painterResource(R.drawable.img_houshan1_cloud_57),
            contentDescription = "云朵57",
            xOffset = 146f,
            yOffset = 754.5f,
            widthDp = 100f,
            heightDp = 90f,
            progress = c57Progress,
            phase = 0.71f,
            motion = CloudMotion.Oscillate,
            amplitudeX = 200f,
            amplitudeY = 10f,
            baseAlpha = 0.50f,
            alphaAmp = 0.25f,
        )

        // 内容层(避开系统导航条)— 整屏 clickable,但 3 个标签 Box 自带消费事件 clickable (§20),点击标签不会冒泡触发跳转
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .clickable(onClick = onOpenUnfinished),
        ) {
            // ══ §21f 5 朵老云(重新动画;间距约束已放弃)══════════════════════════
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

            // 云朵 56 (X=263, Y=755, W=335, H=297, 最高)— Oscillate 留在原地
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

            // 云朵 58 (X=78, Y=170, W=331, H=92, 扁长)— 单向 → 右
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

            // 云朵 57 (X=-25, Y=500, W=225, H=191)— 单向 → 右
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

            // 云朵 5 (X=187, Y=308, W=225, H=191)— 单向 ← 左
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

            // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 在云朵下层
            // §44 加:同后山1 §21 的"上下浮 + 呼吸缩放"动画(Y=405+pandaDy,graphicsLayer 缩放)
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

            // 左上角返回按钮(Return.png,X=30, Y=60, W=18, H=18,复制自后山2 页)— 点击回到后山2 页
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .clickable(onClick = onBack),
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

