package com.jueqiao.jianghu.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.LearningStatsDto
import com.jueqiao.jianghu.luggage.LuggageCreationDto
import com.jueqiao.jianghu.luggage.LuggageCreationSectionDto
import com.jueqiao.jianghu.luggage.LuggageManualDto
import com.jueqiao.jianghu.luggage.LuggageManualSectionDto
import com.jueqiao.jianghu.luggage.LuggageMistakeSectionDto
import com.jueqiao.jianghu.luggage.LuggageProfileDto
import com.jueqiao.jianghu.luggage.LuggageResponseDto
import com.jueqiao.jianghu.luggage.LuggageUiState
import com.jueqiao.jianghu.ui.components.QuickActionItem
import com.jueqiao.jianghu.ui.components.SettingsPaperSurface
import com.jueqiao.jianghu.ui.theme.YaHei
import coil.compose.AsyncImage

private val Ink = Color(0xFF27251F)
private val MutedInk = Color(0xFF625E53)
private val Paper = Color(0xFFF4EAD6)
private val PaperPanel = Color(0xFFF7F0E2)
private val PaperPanelStrong = Color(0xFFEFE3CC)
private val Sage = Color(0xFF65775E)
private val SageSoft = Color(0xFFD6DDC9)
private val SageWash = Color(0xFFDDE2D1)
private val PaperBorder = Color(0xFF9B7E55).copy(alpha = 0.58f)

private val HeaderStyle = TextStyle(
    fontFamily = YaHei,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    color = Ink,
)
private val BodyStyle = TextStyle(fontFamily = YaHei, fontSize = 12.sp, color = Ink)
private val CaptionStyle = TextStyle(fontFamily = YaHei, fontSize = 11.sp, color = MutedInk)

/**
 * 行囊页。
 *
 * 页面使用纵向内容流而不是固定坐标；当内容高于可视区域时，整张纸卡可以滚动。
 * 主纸面与设置页共用同一套浅色古籍纸张纹理和轻毛边轮廓。
 */
@Composable
fun LuggageScreen(
    uiState: LuggageUiState,
    onBack: () -> Unit = {},
    onOpenZaowu: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenBadges: () -> Unit = {},
    onOpenGrowth: () -> Unit = {},
    onOpenManuals: (String?) -> Unit = {},
    onOpenManual: (String) -> Unit = {},
    onOpenMistakes: () -> Unit = {},
    onRetryMistake: (String) -> Unit = {},
    onOpenCreations: () -> Unit = {},
    onOpenCreation: (String) -> Unit = {},
    onContinueCreation: (String) -> Unit = {},
    onOpenEvidence: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var manualFilter by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = 71.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickActionItem(R.drawable.img_icon_works, "作品", onOpenZaowu)
                QuickActionItem(R.drawable.img_icon_progress, "进度", onOpenGrowth)
                QuickActionItem(R.drawable.img_icon_task, "任务", {})
                QuickActionItem(R.drawable.img_icon_settings, "设置", onOpenPrivacy)
            }

            SettingsPaperSurface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, bottom = 12.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LuggageHeader(onBack)
                    when (uiState) {
                        LuggageUiState.Loading -> LuggageLoading()
                        is LuggageUiState.Error -> LuggageError(
                            message = uiState.message,
                            requestId = uiState.requestId,
                            onRetry = onRefresh,
                        )
                        is LuggageUiState.Content -> {
                            LuggageContent(
                                snapshot = uiState.snapshot,
                                manualFilter = manualFilter,
                                onManualFilterChange = { manualFilter = it },
                                onOpenBadges = onOpenBadges,
                                onOpenGrowth = onOpenGrowth,
                                onOpenManuals = onOpenManuals,
                                onOpenManual = onOpenManual,
                                onOpenMistakes = onOpenMistakes,
                                onRetryMistake = onRetryMistake,
                                onOpenCreations = onOpenCreations,
                                onOpenCreation = onOpenCreation,
                                onContinueCreation = onContinueCreation,
                                onOpenZaowu = onOpenZaowu,
                                onOpenEvidence = onOpenEvidence,
                                onOpenPrivacy = onOpenPrivacy,
                            )
                            if (uiState.refreshing) Text("正在更新行囊…", style = CaptionStyle)
                            uiState.notice?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(it, style = CaptionStyle, modifier = Modifier.weight(1f))
                                    SmallActionButton("重试", onRefresh)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun LuggageContent(
    snapshot: LuggageResponseDto,
    manualFilter: String?,
    onManualFilterChange: (String?) -> Unit,
    onOpenBadges: () -> Unit,
    onOpenGrowth: () -> Unit,
    onOpenManuals: (String?) -> Unit,
    onOpenManual: (String) -> Unit,
    onOpenMistakes: () -> Unit,
    onRetryMistake: (String) -> Unit,
    onOpenCreations: () -> Unit,
    onOpenCreation: (String) -> Unit,
    onContinueCreation: (String) -> Unit,
    onOpenZaowu: () -> Unit,
    onOpenEvidence: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    UserInfo(snapshot.data.profile, onOpenBadges)
    PracticeStats(snapshot.data.stats)
    WeeklyGrowthCard(snapshot.data.stats, onOpenGrowth)
    ManualsCard(
        section = snapshot.data.manuals,
        selectedState = manualFilter,
        onSelectState = onManualFilterChange,
        onViewAll = { onOpenManuals(manualFilter) },
        onOpenManual = onOpenManual,
    )
    MistakesCard(
        section = snapshot.data.mistakes,
        onViewAll = onOpenMistakes,
        onRetry = onRetryMistake,
    )
    WorksCard(
        section = snapshot.data.creations,
        onViewAll = onOpenCreations,
        onOpenWork = onOpenCreation,
        onContinue = onContinueCreation,
        onCreate = onOpenZaowu,
    )
    FooterLinks(onOpenEvidence, onOpenPrivacy)
}

@Composable
private fun LuggageLoading() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = Sage, modifier = Modifier.size(32.dp))
        Text("正在整理你的行囊…", style = BodyStyle)
    }
}

@Composable
private fun LuggageError(message: String, requestId: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 58.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(message, style = BodyStyle, textAlign = TextAlign.Center)
        requestId?.let { Text("请求编号：$it", style = CaptionStyle) }
        SmallActionButton("重新加载", onRetry)
    }
}

@Composable
private fun LuggageHeader(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(38.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "关闭",
                modifier = Modifier.size(17.dp),
            )
        }
        Text(
            text = "行   囊",
            fontFamily = YaHei,
            fontSize = 25.sp,
            color = Ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun UserInfo(profile: LuggageProfileDto, onOpenBadges: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(Color(0xFFA9CBC6))
                .border(1.5.dp, Sage.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (profile.avatar != null) {
                AsyncImage(
                    model = profile.avatar.url,
                    contentDescription = "用户头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.img_auth_panda_face),
                    contentDescription = "默认用户头像",
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = profile.nickname,
                    fontFamily = YaHei,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
                profile.classLabel?.let { Text(text = it, style = BodyStyle) }
            }
            Text(text = "ID：${profile.anonymousId}", style = CaptionStyle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(PaperPanelStrong)
                        .border(1.dp, PaperBorder, RoundedCornerShape(5.dp))
                        .padding(horizontal = 14.dp, vertical = 3.dp),
                ) {
                    Text(text = profile.currentTitle?.name ?: "尚未获得称号", style = BodyStyle)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenBadges)
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "勋章 ${profile.badges.size}", style = CaptionStyle)
                    Image(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = "查看勋章",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeStats(stats: LearningStatsDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PaperPanel.copy(alpha = 0.72f))
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatItem("本周修炼", "${stats.week.practiceCount}次", Modifier.weight(1f))
        VerticalRule(30.dp)
        StatItem("累计修行", "${stats.lifetimePracticeDays}天", Modifier.weight(1f))
        VerticalRule(30.dp)
        StatItem("通关试炼", "${stats.distinctTrialsPassed}个", Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = CaptionStyle)
        Text(
            text = value,
            fontFamily = YaHei,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
    }
}

@Composable
private fun WeeklyGrowthCard(stats: LearningStatsDto, onOpenProgress: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(PaperPanel)
            .border(1.dp, PaperBorder, RoundedCornerShape(11.dp))
            .clickable(onClick = onOpenProgress)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "本周成长", style = HeaderStyle)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "查看成长记录", style = CaptionStyle)
                Image(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            GrowthItem("悟", "悟性", stats.evidence.wisdom.displaySummary, Modifier.weight(1f))
            VerticalRule(54.dp)
            GrowthItem("匠", "匠心", stats.evidence.craft.displaySummary, Modifier.weight(1f))
            VerticalRule(54.dp)
            GrowthItem("侠", "侠义", stats.evidence.chivalry.displaySummary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GrowthItem(
    seal: String,
    category: String,
    result: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            GrowthSeal(seal)
            Text(text = category, style = CaptionStyle)
        }
        Text(
            text = result,
            fontFamily = YaHei,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 仿参考稿的旧印章：双层印圈、略不均匀的印泥边缘与纸色缺口。
 * 使用矢量绘制保证不同密度屏幕上“悟、匠、侠”仍然清晰。
 */
@Composable
private fun GrowthSeal(text: String) {
    Box(
        modifier = Modifier.size(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val paper = PaperPanel

            // 外圈故意使用不同透明度，模拟重复落印形成的斑驳双圈。
            drawCircle(
                color = Sage.copy(alpha = 0.45f),
                radius = radius * 0.98f,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawCircle(
                color = Sage.copy(alpha = 0.92f),
                radius = radius * 0.86f,
            )
            drawCircle(
                color = paper.copy(alpha = 0.70f),
                radius = radius * 0.72f,
                style = Stroke(width = 0.8.dp.toPx()),
            )

            // 固定位置的浅色缺口让印章有轻微旧印泥质感，不影响文字辨识。
            val flecks = listOf(
                0.26f to 0.19f,
                0.69f to 0.16f,
                0.83f to 0.35f,
                0.79f to 0.73f,
                0.57f to 0.86f,
                0.23f to 0.76f,
                0.15f to 0.48f,
            )
            flecks.forEachIndexed { index, (x, y) ->
                drawCircle(
                    color = paper.copy(alpha = 0.42f),
                    radius = (if (index % 2 == 0) 0.9.dp else 0.6.dp).toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
                )
            }
        }
        Text(
            text = text,
            fontFamily = YaHei,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PaperPanel,
        )
    }
}

@Composable
private fun ManualsCard(
    section: LuggageManualSectionDto,
    selectedState: String?,
    onSelectState: (String?) -> Unit,
    onViewAll: () -> Unit,
    onOpenManual: (String) -> Unit,
) {
    val visibleItems = section.items.filter { selectedState == null || it.state == selectedState }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperPanel.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        SectionHeader("我的秘籍  ${section.obtained}/${section.total}", "查看全部", onViewAll)
        ManualFilters(selectedState, onSelectState)
        if (section.emptyReason == "NO_OBTAINED_MANUALS") {
            EmptySection("完成一次修炼，就会在这里遇见第一本秘籍")
        } else if (visibleItems.isEmpty()) {
            EmptySection("最近没有${manualStateLabel(selectedState)}状态的秘籍")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                visibleItems.take(3).forEach { item ->
                    BookItem(
                        item = item,
                        onClick = { onOpenManual(item.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat((3 - visibleItems.take(3).size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ManualFilters(selectedState: String?, onSelect: (String?) -> Unit) {
    val filters = listOf(
        null to "全部",
        "DISCOVERED" to "偶得",
        "LEARNED" to "习得",
        "MASTERED" to "悟得",
        "TEACHING" to "传习",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        filters.forEach { (state, label) ->
            val selected = state == selectedState
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSelect(state) }
                    .padding(horizontal = 3.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                    color = if (selected) Sage else Ink,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .width(if (selected) 32.dp else 0.dp)
                        .height(2.dp)
                        .background(Sage),
                )
            }
        }
    }
}

@Composable
private fun BookItem(
    item: LuggageManualDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Image(
            painter = painterResource(manualCover(item.styleNo)),
            contentDescription = item.title,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = "《${item.title}》",
            fontFamily = YaHei,
            fontSize = 10.sp,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        StatePill(item.stateLabel)
    }
}

private fun manualCover(styleNo: Int): Int = when (styleNo) {
    1 -> R.drawable.img_book_1
    2 -> R.drawable.img_book_2
    3 -> R.drawable.img_book_3
    4 -> R.drawable.img_book_1
    else -> R.drawable.img_book_3
}

private fun manualStateLabel(state: String?): String = when (state) {
    "DISCOVERED" -> "偶得"
    "LEARNED" -> "习得"
    "MASTERED" -> "悟得"
    "TEACHING" -> "传习"
    else -> "所选"
}

@Composable
private fun MistakesCard(
    section: LuggageMistakeSectionDto,
    onViewAll: () -> Unit,
    onRetry: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperPanel.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        SectionHeader("我的错题  ${section.pendingCount}", null, onViewAll)
        val item = section.items.firstOrNull()
        if (item == null) {
            EmptySection("暂时没有待巩固的错题")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_age_scroll),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    text = item.knowledgePoint,
                    style = BodyStyle,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatePill(mistakeStatusLabel(item.status))
                item.retryUrl?.let { SmallActionButton("再试一次", { onRetry(item.id) }) }
            }
        }
    }
}

@Composable
private fun WorksCard(
    section: LuggageCreationSectionDto,
    onViewAll: () -> Unit,
    onOpenWork: (String) -> Unit,
    onContinue: (String) -> Unit,
    onCreate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperPanel.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader("我的作品  ${section.total}", "查看全部", onViewAll)
        if (section.items.isEmpty()) {
            EmptySection("还没有作品，去造物坊试试吧")
            SmallActionButton("开始创作", onCreate, Modifier.align(Alignment.End))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                section.items.take(2).forEachIndexed { index, item ->
                    WorkItem(
                        item = item,
                        fallbackImage = if (index == 0) R.drawable.img_work_1 else R.drawable.img_work_2,
                        onClick = { onOpenWork(item.projectId) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (section.items.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            val revisable = section.items.firstOrNull { it.canRevise }
            if (revisable != null) {
                ContinueCreationStrip(
                    title = revisable.title,
                    onClick = { onContinue(revisable.projectId) },
                )
            }
        }
    }
}

@Composable
private fun WorkItem(
    item: LuggageCreationDto,
    fallbackImage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(7.dp))
            .background(PaperPanelStrong)
            .clickable(onClick = onClick),
    ) {
        if (item.thumbnail != null) {
            AsyncImage(
                model = item.thumbnail.url,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(fallbackImage),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(SageSoft.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(text = creationStatusLabel(item.displayStatus), style = CaptionStyle.copy(color = Sage))
        }
    }
}

@Composable
private fun ContinueCreationStrip(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(SageWash),
    ) {
        Image(
            painter = painterResource(R.drawable.img_luggage_panda),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 5.dp)
                .size(width = 62.dp, height = 66.dp),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.FillWidth,
        )
        Text(
            text = "继续完善《$title》",
            style = BodyStyle,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 72.dp, end = 94.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SmallActionButton(
            text = "继续创作",
            onClick = onClick,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
        )
    }
}

@Composable
private fun FooterLinks(onOpenEvidence: () -> Unit, onOpenPrivacy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "学习证据",
            style = CaptionStyle,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onOpenEvidence)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
        VerticalRule(18.dp)
        Text(
            text = "隐私与安全",
            style = CaptionStyle,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onOpenPrivacy)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun SectionHeader(title: String, action: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = HeaderStyle)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (action != null) Text(text = action, style = CaptionStyle)
            Image(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = action,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Text(
        text = message,
        style = CaptionStyle,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
    )
}

private fun mistakeStatusLabel(status: String): String = when (status) {
    "PRACTICING" -> "重练中"
    "CONSOLIDATED" -> "已巩固"
    else -> "待巩固"
}

private fun creationStatusLabel(status: String): String = when (status) {
    "PENDING_CHECK" -> "检查中"
    "PENDING_HUMAN_REVIEW" -> "待人工复核"
    "PUBLISHED" -> "已发布"
    "RETURNED" -> "已退回"
    "RESTRICTED" -> "受限"
    "WITHDRAWN" -> "已撤回"
    else -> "草稿"
}

@Composable
private fun StatePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SageSoft)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            fontFamily = YaHei,
            fontSize = 10.sp,
            color = Sage,
            maxLines = 1,
        )
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PaperPanel)
            .border(1.dp, PaperBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = CaptionStyle.copy(color = Ink), maxLines = 1)
    }
}

@Composable
private fun VerticalRule(height: Dp) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(height)
            .background(PaperBorder.copy(alpha = 0.35f)),
    )
}
