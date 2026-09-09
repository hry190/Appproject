package com.jueqiao.jianghu.ui.screens.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.ClassroomDto
import com.jueqiao.jianghu.luggage.PublicationFeedItemDto
import com.jueqiao.jianghu.ui.components.PublicationFeedCard
import com.jueqiao.jianghu.ui.theme.YaHei

@Composable
fun ChallengeScreen(
    inbox: List<PublicationFeedItemDto> = emptyList(),
    inboxLoading: Boolean = false,
    inboxError: String? = null,
    canLoadMore: Boolean = false,
    classrooms: List<ClassroomDto> = emptyList(),
    classroomLoading: Boolean = false,
    classroomMessage: String? = null,
    oneTimeJoinCode: String? = null,
    isAdult: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onCreateClassroom: (String) -> Unit = {},
    onJoinClassroom: (String) -> Unit = {},
    onDismissJoinCode: () -> Unit = {},
    onBack: () -> Unit = {},
    onOpenWendao: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenProgress: () -> Unit = {},
) {
    var classInput by rememberSaveable { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Image(
            painter = painterResource(R.drawable.img_home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Surface(color = Color(0xFFF4F0DB).copy(alpha = 0.92f), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Column(Modifier.weight(1f)) {
                        Text("书信 · 作品来信", fontFamily = YaHei, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("家长与班级创建者在这里查看已通过审核的作品", fontFamily = YaHei, style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onRefresh, enabled = !inboxLoading && !classroomLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新来信与班级")
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    ClassroomPanel(
                        isAdult = isAdult,
                        input = classInput,
                        onInputChange = { classInput = it },
                        classrooms = classrooms,
                        loading = classroomLoading,
                        message = classroomMessage,
                        joinCode = oneTimeJoinCode,
                        onSubmit = {
                            if (isAdult) onCreateClassroom(classInput) else onJoinClassroom(classInput)
                        },
                        onDismissJoinCode = onDismissJoinCode,
                    )
                }
                item { Text("最新作品", fontFamily = YaHei, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                when {
                    inboxLoading && inbox.isEmpty() -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    inboxError != null && inbox.isEmpty() -> item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(inboxError, fontFamily = YaHei)
                            Button(onClick = onRefresh) { Text("重新载入", fontFamily = YaHei) }
                        }
                    }
                    inbox.isEmpty() -> item { Text("暂无新的作品来信", fontFamily = YaHei, color = Color(0xFF526354)) }
                    else -> {
                        items(inbox, key = { it.publicationId }) { PublicationFeedCard(it) }
                        if (canLoadMore) item {
                            Button(onClick = onLoadMore, enabled = !inboxLoading, modifier = Modifier.fillMaxWidth()) {
                                Text(if (inboxLoading) "载入中…" else "查看更多", fontFamily = YaHei)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomPanel(
    isAdult: Boolean,
    input: String,
    onInputChange: (String) -> Unit,
    classrooms: List<ClassroomDto>,
    loading: Boolean,
    message: String?,
    joinCode: String?,
    onSubmit: () -> Unit,
    onDismissJoinCode: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3E5).copy(alpha = 0.95f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (isAdult) "我创建的班级" else "加入班级", fontFamily = YaHei, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = input,
                onValueChange = { onInputChange(if (isAdult) it.take(80) else it.take(12).uppercase()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(if (isAdult) "班级名称" else "8 位邀请码", fontFamily = YaHei) },
            )
            Button(
                onClick = onSubmit,
                enabled = !loading && input.trim().length >= if (isAdult) 1 else 6,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (isAdult) "创建班级" else "确认加入", fontFamily = YaHei) }
            if (loading) CircularProgressIndicator()
            message?.let { Text(it, fontFamily = YaHei, color = Color(0xFF49644A)) }
            joinCode?.let {
                Surface(color = Color(0xFFE3EFD8), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("一次性显示的邀请码", fontFamily = YaHei, fontWeight = FontWeight.Bold)
                        Text(it, fontFamily = YaHei, style = MaterialTheme.typography.headlineSmall)
                        Text("请现在交给学生；列表不会再次显示邀请码。", fontFamily = YaHei, style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onDismissJoinCode) { Text("我已保存", fontFamily = YaHei) }
                    }
                }
            }
            classrooms.forEach { classroom ->
                Text(
                    "${classroom.name} · ${if (classroom.role == "OWNER") "我创建的" else classroom.teacherNickname} · ${classroom.memberCount} 人",
                    fontFamily = YaHei,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
