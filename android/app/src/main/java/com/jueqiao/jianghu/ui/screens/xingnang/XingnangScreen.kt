package com.jueqiao.jianghu.ui.screens.xingnang

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.data.StaticData
import com.jueqiao.jianghu.ui.theme.InkTextSecondary
import com.jueqiao.jianghu.ui.theme.YaHei

/**
 * 行囊 / Profile — static page mirroring RN xingnang.tsx.
 */
@Composable
fun XingnangScreen(onBack: () -> Unit) {
    val p = StaticData.profile
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "返回",
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "行囊",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Profile card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = p.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "入学 ${p.joinedDays} 天",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Stat(label = "作品",   value = p.works.toString())
                        Stat(label = "秘籍",   value = p.manuals.toString())
                        Stat(label = "勋章",   value = p.badges.toString())
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("勋章 / Badges")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                StaticData.badges.take(4).forEach { b ->
                    BadgeChip(b)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StaticData.badges.drop(4).forEach { b ->
                    BadgeChip(b)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("我的作品 / Works")
            StaticData.works.forEach { work ->
                WorkCard(work.title, work.format, work.status)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("我的秘籍 / Manuals")
            StaticData.manuals.forEach { manual ->
                ManualCard(manual.name, manual.state, manual.date)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("修行记录 / Practice Log")
            StaticData.practice.forEach { entry ->
                PracticeCard(entry.date, entry.title, entry.xp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "数据每晚与服务器同步；离线状态会暂存本地，恢复后上传",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                fontFamily = YaHei,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun BadgeChip(b: com.jueqiao.jianghu.data.Badge) {
    val bg = if (b.earned) Color(b.colorHex) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = b.name,
            color = if (b.earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun WorkCard(title: String, format: String, status: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "$format  ·  $status",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ManualCard(name: String, state: String, date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            ManualStateTag(state)
        }
    }
}

@Composable
private fun ManualStateTag(state: String) {
    val (bg, fg) = when (state) {
        "偶得" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "习得" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondary
        "悟得" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        else   -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = state, color = fg, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PracticeCard(date: String, title: String, xp: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = xp,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}