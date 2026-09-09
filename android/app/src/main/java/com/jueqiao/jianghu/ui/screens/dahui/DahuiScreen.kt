package com.jueqiao.jianghu.ui.screens.dahui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.PublicationFeedItemDto
import com.jueqiao.jianghu.ui.components.PublicationFeedCard
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun DahuiScreen(
    items: List<PublicationFeedItemDto> = emptyList(),
    loading: Boolean = false,
    error: String? = null,
    canLoadMore: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onOpenArena: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Image(
            painter = painterResource(R.drawable.img_dahui_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Surface(color = Color(0xFFF4F0DB).copy(alpha = 0.9f), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "江湖大会 · 知行流",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = YaHei, fontWeight = FontWeight.Bold),
                            color = Color(0xFF264C33),
                        )
                        Text("仅展示审核通过且仍在发布中的作品", fontFamily = YaHei, style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = onOpenArena) {
                        Text("演武场", fontFamily = YaHei)
                    }
                    IconButton(onClick = onRefresh, enabled = !loading) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新知行流")
                    }
                }
            }
            when {
                loading && items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null && items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(error, fontFamily = YaHei)
                        Button(onClick = onRefresh) { Text("重新载入", fontFamily = YaHei) }
                    }
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("进入演武场浏览知行流作品", fontFamily = YaHei, color = Color(0xFF425846))
                        Button(onClick = onOpenArena) { Text("进入演武场", fontFamily = YaHei) }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(items, key = { it.publicationId }) { PublicationFeedCard(it) }
                    if (canLoadMore) {
                        item {
                            Button(onClick = onLoadMore, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                                Text(if (loading) "载入中…" else "查看更多", fontFamily = YaHei)
                            }
                        }
                    }
                    error?.let { message -> item { Text(message, fontFamily = YaHei, color = MaterialTheme.colorScheme.error) } }
                }
            }
        }
    }
}
