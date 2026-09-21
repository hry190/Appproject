package com.jueqiao.jianghu.ui.screens.yanwuchangvideomy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.conference.ConferenceUiState
import com.jueqiao.jianghu.luggage.ConferenceWorkDto
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.ConferenceWorksNavigationBar
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay

private val MyJade = Color(0xFF315F49)
private val MyJadeDeep = Color(0xFF173A2E)
private val MyJadeSoft = Color(0xFF91B69A)
private val MyParchment = Color(0xFFF6EEDB)
private val MyGold = Color(0xFFC6A45B)

@Composable
fun YanwuchangVideoMyScreen(
    state: ConferenceUiState,
    nickname: String,
    userId: String,
    onBack: () -> Unit = {},
    onLoad: () -> Unit = {},
    onOpenWorks: () -> Unit = {},
    onOpenWork: (String) -> Unit = {},
    onOpenReviews: (String) -> Unit = {},
    onOpenBrowseRecord: () -> Unit = {},
    onOpenMyClass: () -> Unit = {},
) {
    BackHandler(enabled = true, onBack = onBack)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        onLoad()
        while (true) {
            delay(15_000)
            onLoad()
        }
    }

    val statusInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navigationContentHeight = 64.dp
    val navigationHeight = navigationContentHeight + navigationInset
    val displayedWorks = if (selectedTab == 0) {
        state.myWorks
    } else {
        state.collections.map { it.work }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEDE9DC))) {
        Image(
            painter = painterResource(R.drawable.img_yanwuchang_video_my_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(382.dp),
            contentScale = ContentScale.FillWidth,
        )
        Box(
            modifier = Modifier
                .padding(start = 18.dp, top = statusInset + 14.dp)
                .size(48.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_dahui_return),
                contentDescription = "返回",
                modifier = Modifier.size(27.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = statusInset + 82.dp,
                    bottom = navigationHeight + 10.dp,
                ),
        ) {
            ProfileHeader(
                nickname = nickname.ifBlank { "未命名" },
                userId = userId,
                workCount = state.myWorks.size,
                collectionCount = state.collections.size,
                onOpenReviews = {
                    state.myWorks.firstOrNull()?.let { onOpenReviews(it.publicationId) }
                },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("浏览记录", Icons.Outlined.History, onOpenBrowseRecord, Modifier.weight(1f))
                QuickAction("我的班级", Icons.Outlined.School, onOpenMyClass, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MyParchment.copy(alpha = 0.98f))
                    .border(
                        1.dp,
                        MyGold.copy(alpha = 0.72f),
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    ),
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(58.dp)) {
                    ShelfTab("作品", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                    ShelfTab("收藏", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(MyJadeSoft.copy(alpha = 0.4f)))
                when {
                    state.error != null && displayedWorks.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            state.error,
                            color = Color(0xFF6F6459),
                            fontFamily = YaHei,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "重新载入",
                            color = MyJadeDeep,
                            fontFamily = YaHei,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, MyGold, RoundedCornerShape(18.dp))
                                .clickable(onClick = onLoad)
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                        )
                    }
                    state.loading && displayedWorks.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MyJade)
                    }
                    displayedWorks.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (selectedTab == 0) "还没有已发布作品" else "还没有收藏作品",
                            color = Color(0xFF77837B),
                            fontFamily = YaHei,
                            fontSize = 15.sp,
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(displayedWorks, key = ConferenceWorkDto::publicationId) { work ->
                            ShelfWorkCard(work = work, onClick = { onOpenWork(work.publicationId) })
                        }
                    }
                }
            }
        }

        ConferenceWorksNavigationBar(
            selectedWorks = false,
            navigationBarInset = navigationInset,
            onOpenWorks = onOpenWorks,
            onOpenMy = {},
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ProfileHeader(
    nickname: String,
    userId: String,
    workCount: Int,
    collectionCount: Int,
    onOpenReviews: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFA5C2AB))
                .border(2.dp, MyGold, CircleShape),
        ) {
            Image(
                painter = painterResource(R.drawable.img_yanwuchang_video_my_group_165),
                contentDescription = "头像",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nickname, color = Color.White, fontFamily = YaHei, fontWeight = FontWeight.Bold, fontSize = 24.sp, maxLines = 1)
            Text(
                "ID：${userId.take(12)}",
                color = Color.White.copy(alpha = 0.78f),
                fontFamily = YaHei,
                fontSize = 11.sp,
                maxLines = 1,
            )
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                StatChip("$workCount 作品", Icons.AutoMirrored.Outlined.MenuBook)
                StatChip("$collectionCount 收藏", Icons.Outlined.StarBorder)
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(MyParchment)
                .border(1.dp, MyGold, RoundedCornerShape(22.dp))
                .clickable(enabled = workCount > 0, onClick = onOpenReviews)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = MyJade, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(5.dp))
            Text("查看点评", color = MyJadeDeep, fontFamily = YaHei, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatChip(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MyParchment.copy(alpha = 0.94f))
            .border(1.dp, MyGold.copy(alpha = 0.82f), RoundedCornerShape(18.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MyJade, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = MyJadeDeep, fontFamily = YaHei, fontSize = 11.sp)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MyParchment.copy(alpha = 0.94f))
            .border(1.dp, MyGold.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MyJade, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = MyJadeDeep, fontFamily = YaHei, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = MyJade.copy(alpha = 0.72f), modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun ShelfTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxHeight().clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(
            label,
            color = if (selected) MyJadeDeep else Color(0xFF59645E),
            fontFamily = YaHei,
            fontSize = 19.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(58.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MyGold),
            )
        }
    }
}

@Composable
private fun ShelfWorkCard(work: ConferenceWorkDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.68f))
            .border(1.dp, MyJadeSoft.copy(alpha = 0.34f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(116.dp).clip(RoundedCornerShape(11.dp))) {
            AsyncImage(
                model = work.previewUrl,
                contentDescription = work.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.img_yanwuchang_video_mechanical_butterfly),
                error = painterResource(R.drawable.img_yanwuchang_video_mechanical_butterfly),
                fallback = painterResource(R.drawable.img_yanwuchang_video_mechanical_butterfly),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, MyJadeSoft.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, "播放作品", tint = MyJade, modifier = Modifier.size(25.dp))
            }
            work.previewDurationMs?.let { durationMs ->
                Text(
                    text = "%02d:%02d".format(durationMs / 60_000, durationMs / 1_000 % 60),
                    color = Color.White,
                    fontFamily = YaHei,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                work.title,
                color = Color(0xFF1E2C25),
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ShelfChip(categoryLabel(work.conferenceCategory))
                ShelfChip(if (work.previewMimeType?.startsWith("video/") == true) "视频" else mediaLabel(work.mediaType))
                ShelfChip("已发布")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "点赞 ${work.likeCount}  ·  点评 ${work.reviewCount}  ·  收藏 ${work.collectionCount}",
                color = Color(0xFF708078),
                fontFamily = YaHei,
                fontSize = 11.sp,
                maxLines = 1,
            )
            Spacer(Modifier.height(8.dp))
            Text("已同步更新", color = Color(0xFF708078), fontFamily = YaHei, fontSize = 11.sp)
        }
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = Color(0xFF9B947F), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ShelfChip(label: String) {
    Text(
        text = label,
        color = MyJade,
        fontFamily = YaHei,
        fontSize = 10.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MyJadeSoft.copy(alpha = 0.23f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun categoryLabel(category: String?): String = when (category) {
    "ART" -> "艺术"
    "SCIENCE" -> "科学"
    "MATH" -> "数学"
    "LANGUAGE" -> "语文"
    else -> "作品"
}

private fun mediaLabel(mediaType: String): String = when (mediaType) {
    "VIDEO" -> "视频"
    "ILLUSTRATION" -> "插画"
    "COMIC" -> "漫画"
    "MIXED_MEDIA" -> "综合创作"
    else -> "创作"
}
