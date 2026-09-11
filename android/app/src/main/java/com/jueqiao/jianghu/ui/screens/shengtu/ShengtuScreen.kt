package com.jueqiao.jianghu.ui.screens.shengtu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.luggage.CreationConversationDto
import com.jueqiao.jianghu.luggage.CreationConversationMessageDto
import com.jueqiao.jianghu.luggage.ImageGenerationJobDto
import com.jueqiao.jianghu.ui.components.CreationWorkspaceTopBar
import com.jueqiao.jianghu.ui.screens.settings.CreationDraftStore
import com.jueqiao.jianghu.ui.theme.YaHei

private val Ink = Color(0xFF3F5542)
private val MutedInk = Color(0xFF70806F)
private val CoachPaper = Color(0xFFF8F3E5).copy(alpha = .97f)
private val StudentGreen = Color(0xFFE3EFD4).copy(alpha = .97f)
private val ActionGreen = Color(0xFF7EA67D)
private val ActionDarkGreen = Color(0xFF5F8763)

/** 同一页完成交流、生成、确认与保存；服务端业务门禁不会暴露给孩子。 */
@Composable
fun ShengtuScreen(
    onBack: () -> Unit = {},
    projectId: String,
    projectTitle: String? = null,
    conversation: CreationConversationDto? = null,
    activeGeneration: ImageGenerationJobDto? = null,
    loading: Boolean = false,
    busy: Boolean = false,
    statusMessage: String? = null,
    errorMessage: String? = null,
    generationBusy: Boolean = false,
    generationMessage: String? = null,
    onSendMessage: (String) -> Unit = {},
    onAcceptSuggestion: (CreationConversationMessageDto) -> Unit = {},
    onSaveDraftAndGenerate: () -> Unit = {},
    onSaveWork: () -> Unit = {},
    onRetryGeneration: (ImageGenerationJobDto) -> Unit = {},
    onOpenChuangzuodangan: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    BackHandler {
        focusManager.clearFocus()
        keyboardController?.hide()
        onBack()
    }

    val context = LocalContext.current
    val draftStore = remember(context) { CreationDraftStore(context) }
    val draftKey = remember(projectId) { "creation_conversation_input_$projectId" }
    var input by rememberSaveable(projectId) { mutableStateOf(draftStore.read(draftKey)) }
    var submittedText by rememberSaveable(projectId) { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val messages = conversation?.messages.orEmpty()
    val resultReady = conversation?.status == "RESULT_READY" || conversation?.status == "SAVED"
    val generating = generationBusy || conversation?.status == "GENERATING"
    val canChat = conversation?.status in setOf(
        "DIALOGUE",
        "GENERATION_FAILED",
        "RESULT_READY",
        "SAVED",
    )
    val latestPendingSuggestion = messages.lastOrNull {
        it.role == "COACH" && it.kind == "SUGGESTION" && it.decision == "PENDING"
    }
    val pendingStudentText = submittedText?.takeUnless { pending ->
        messages.lastOrNull { it.role == "STUDENT" }?.content == pending
    }
    val resultUrl = activeGeneration?.outputAsset?.originalUrl

    LaunchedEffect(
        messages.size,
        resultUrl,
        resultReady,
        generating,
        pendingStudentText,
        statusMessage,
        errorMessage,
        generationMessage,
        activeGeneration?.retryable,
    ) {
        var itemCount = messages.size
        if (loading && messages.isEmpty()) itemCount += 1
        if (generating || (!resultReady && !generationMessage.isNullOrBlank())) itemCount += 1
        if (resultReady && !resultUrl.isNullOrBlank()) itemCount += 1
        if (pendingStudentText != null) itemCount += 1
        if (!statusMessage.isNullOrBlank() && (busy || !resultReady)) itemCount += 1
        if (!errorMessage.isNullOrBlank()) itemCount += 1
        if (conversation?.status == "GENERATION_FAILED" && activeGeneration?.retryable == true) {
            itemCount += 1
        }
        if (itemCount > 0) {
            // Wait for the result/status item to enter the lazy list before scrolling to it.
            withFrameNanos { }
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LaunchedEffect(messages.size, busy, errorMessage) {
        val pending = submittedText
        val latestStudentText = messages.lastOrNull { it.role == "STUDENT" }?.content
        if (pending != null && latestStudentText == pending) {
            draftStore.clear(draftKey)
            input = ""
            submittedText = null
            focusManager.clearFocus()
            keyboardController?.hide()
        } else if (pending != null && !busy && !errorMessage.isNullOrBlank()) {
            input = pending
            draftStore.saveIfEnabled(draftKey, pending)
            submittedText = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.img_shengtu_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding(),
        ) {
            CreationWorkspaceTopBar(onBack = onBack, onOpenArchive = onOpenChuangzuodangan)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shengtu_group212),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 30.dp, top = 31.dp, end = 30.dp, bottom = 27.dp),
                ) {
                    projectTitle?.takeIf(String::isNotBlank)?.let { title ->
                        Text(
                            text = title,
                            color = Ink,
                            fontFamily = YaHei,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 5.dp, bottom = 7.dp),
                            maxLines = 1,
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (loading && messages.isEmpty()) {
                            item(key = "loading") { CoachBubble("正在找回我们的创作对话……") }
                        }
                        messages.forEach { message ->
                            item(key = message.id) {
                                ConversationBubble(
                                    message = message,
                                    showAccept = message.id == latestPendingSuggestion?.id &&
                                        canChat && !busy && !generating,
                                    onAccept = { onAcceptSuggestion(message) },
                                )
                            }
                        }
                        if (generating) {
                            item(key = "generating") {
                                GenerationBubble(activeGeneration?.progressPercent, generationMessage)
                            }
                        } else if (!resultReady && !generationMessage.isNullOrBlank()) {
                            item(key = "generation-message") { CoachBubble(generationMessage) }
                        }
                        if (resultReady && !resultUrl.isNullOrBlank()) {
                            item(key = "result") {
                                ResultPreview(url = resultUrl)
                            }
                        }
                        pendingStudentText?.let { text ->
                            item(key = "pending-student-message") {
                                PendingStudentBubble(text)
                            }
                        }
                        if (!statusMessage.isNullOrBlank() && (busy || !resultReady)) {
                            item(key = "status-message") { CoachBubble(statusMessage) }
                        }
                        if (!errorMessage.isNullOrBlank()) {
                            item(key = "conversation-error") {
                                CoachBubble("刚才的话还没有送到，已经帮你保留了。$errorMessage")
                            }
                        }
                        if (conversation?.status == "GENERATION_FAILED" && activeGeneration?.retryable == true) {
                            item(key = "retry") {
                                CoachBubble(
                                    text = activeGeneration.errorSummary
                                        ?: "这次没有创作成功，我们可以再试一次。",
                                    actionLabel = "再试一次",
                                    actionDescription = "重新尝试生成作品",
                                    onAction = { onRetryGeneration(activeGeneration) },
                                )
                            }
                        }
                    }

                    if (canChat && !generating) {
                        if (resultReady) {
                            Text(
                                text = "还想调整？直接告诉教练哪里需要修改",
                                color = MutedInk,
                                fontFamily = YaHei,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 8.dp, bottom = 5.dp),
                            )
                        }
                        ConversationInput(
                            value = input,
                            enabled = !busy,
                            placeholder = if (resultReady) {
                                "告诉教练哪里需要修改"
                            } else {
                                "继续和教练商量"
                            },
                            onValueChange = {
                                input = it
                                draftStore.saveIfEnabled(draftKey, it)
                            },
                            onSend = {
                                val outgoing = input.trim()
                                if (outgoing.length >= 2) {
                                    submittedText = outgoing
                                    input = ""
                                    draftStore.clear(draftKey)
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onSendMessage(outgoing)
                                }
                            },
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.img_shengtu_untitled41),
                    contentDescription = "熊猫教练",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-21).dp, y = (-63).dp)
                        .size(92.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            PrimaryCreationAction(
                status = conversation?.status,
                enabled = conversation != null && !busy && !generating,
                onSaveDraftAndGenerate = onSaveDraftAndGenerate,
                onSaveWork = onSaveWork,
                onOpenArchive = onOpenChuangzuodangan,
            )
        }
    }
}

@Composable
private fun PendingStudentBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            color = StudentGreen,
            shape = RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB4C9A8)),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .semantics { contentDescription = "正在发送的学生消息" },
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)) {
                Text(
                    text = text,
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                )
                Text(
                    text = "正在送给教练……",
                    color = MutedInk,
                    fontFamily = YaHei,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ConversationBubble(
    message: CreationConversationMessageDto,
    showAccept: Boolean,
    onAccept: () -> Unit,
) {
    val fromStudent = message.role == "STUDENT"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromStudent) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (fromStudent) StudentGreen else CoachPaper,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (fromStudent) 18.dp else 5.dp,
                bottomEnd = if (fromStudent) 5.dp else 18.dp,
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (fromStudent) Color(0xFFB4C9A8) else Color(0xFFD8D2BD),
            ),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)) {
                Text(
                    text = message.content,
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                )
                if (showAccept) {
                    TextButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(48.dp)
                            .semantics { contentDescription = "采纳教练建议并继续" },
                    ) {
                        Text(
                            text = "采纳并继续",
                            color = ActionDarkGreen,
                            fontFamily = YaHei,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else if (message.decision == "ACCEPTED") {
                    Text(
                        text = "已采纳",
                        color = MutedInk,
                        fontFamily = YaHei,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachBubble(
    text: String,
    actionLabel: String? = null,
    actionDescription: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = CoachPaper,
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8D2BD)),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)) {
                Text(
                    text = text,
                    color = Ink,
                    fontFamily = YaHei,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                )
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(48.dp)
                            .semantics { contentDescription = actionDescription ?: actionLabel },
                    ) {
                        Text(actionLabel, color = ActionDarkGreen, fontFamily = YaHei)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerationBubble(progress: Int?, message: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = CoachPaper,
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8D2BD)),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = ActionGreen,
                )
                Column {
                    Text(
                        text = message?.takeIf(String::isNotBlank)
                            ?: "我正在把商量好的想法画出来……",
                        color = Ink,
                        fontFamily = YaHei,
                        fontSize = 14.sp,
                    )
                    progress?.takeIf { it > 0 }?.let {
                        Text("已经完成 $it%", color = MutedInk, fontFamily = YaHei, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultPreview(url: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CoachBubble("作品做好啦！看看是不是你想要的样子。")
        AsyncImage(
            model = url,
            contentDescription = "本次创作的作品预览",
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFFD1D6BF), RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun ConversationInput(
    value: String,
    enabled: Boolean,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(end = 72.dp)
            .background(StudentGreen, RoundedCornerShape(28.dp))
            .border(1.dp, Color(0xFFB6CAA9), RoundedCornerShape(28.dp))
            .padding(start = 18.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isBlank()) {
                Text(placeholder, color = MutedInk, fontFamily = YaHei, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(500)) },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (enabled && value.trim().length >= 2) onSend()
                    },
                ),
                textStyle = TextStyle(color = Ink, fontFamily = YaHei, fontSize = 15.sp),
                cursorBrush = SolidColor(ActionGreen),
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = "给熊猫教练发送修改想法"
                },
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (enabled && value.trim().length >= 2) ActionGreen else Color(0xFFB6C5AE))
                .semantics {
                    role = Role.Button
                    contentDescription = "发送消息"
                }
                .clickable(enabled = enabled && value.trim().length >= 2, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Text("↑", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PrimaryCreationAction(
    status: String?,
    enabled: Boolean,
    onSaveDraftAndGenerate: () -> Unit,
    onSaveWork: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    val (label, action) = when (status) {
        "RESULT_READY" -> "保存作品" to onSaveWork
        "SAVED" -> "查看创作档案" to onOpenArchive
        "GENERATING" -> "正在创作……" to {}
        "GENERATION_FAILED" -> "保存草稿并重新创作" to onSaveDraftAndGenerate
        else -> "保存上传草稿" to onSaveDraftAndGenerate
    }
    Button(
        onClick = action,
        enabled = enabled && status != "GENERATING",
        colors = ButtonDefaults.buttonColors(
            containerColor = ActionGreen,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFAABCA3),
            disabledContentColor = Color.White.copy(alpha = .82f),
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 14.dp, bottom = 16.dp)
            .height(56.dp)
            .semantics { contentDescription = label },
    ) {
        Text(
            text = label,
            fontFamily = YaHei,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
        )
    }
}
