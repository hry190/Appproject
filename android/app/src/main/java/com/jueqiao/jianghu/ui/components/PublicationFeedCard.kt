package com.jueqiao.jianghu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jueqiao.jianghu.luggage.PublicationFeedItemDto
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun PublicationFeedCard(item: PublicationFeedItemDto, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3E5).copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item.previewUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "${item.title}作品预览",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 260.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = YaHei, fontWeight = FontWeight.Bold),
                        color = Color(0xFF274C34),
                        modifier = Modifier.weight(1f),
                    )
                    if (item.aiAssisted) AssistChip(onClick = {}, label = { Text("AI 协作", fontFamily = YaHei) })
                }
                Text(
                    "${item.authorNickname} · ${item.publishedAt.take(16).replace('T', ' ')}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = YaHei),
                    color = Color(0xFF647064),
                )
                item.classroomName?.let { Text("投递班级：$it", fontFamily = YaHei, color = Color(0xFF466847)) }
                item.description?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = YaHei))
                }
                item.learningSummary?.takeIf(String::isNotBlank)?.let {
                    Text(
                        "学习说明：$it",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = YaHei),
                        color = Color(0xFF5C684F),
                    )
                }
            }
        }
    }
}
