package com.jueqiao.jianghu.ui.screens.manualreader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.screens.volume1.Volume1Screen
import com.jueqiao.jianghu.ui.screens.volume1part10.Volume1Part10Screen
import com.jueqiao.jianghu.ui.screens.volume1part11.Volume1Part11Screen
import com.jueqiao.jianghu.ui.screens.volume1part12.Volume1Part12Screen
import com.jueqiao.jianghu.ui.screens.volume1part13.Volume1Part13Screen
import com.jueqiao.jianghu.ui.screens.volume1part14.Volume1Part14Screen
import com.jueqiao.jianghu.ui.screens.volume1part2.Volume1Part2Screen
import com.jueqiao.jianghu.ui.screens.volume1part3.Volume1Part3Screen
import com.jueqiao.jianghu.ui.screens.volume1part4.Volume1Part4Screen
import com.jueqiao.jianghu.ui.screens.volume1part5.Volume1Part5Screen
import com.jueqiao.jianghu.ui.screens.volume1part6.Volume1Part6Screen
import com.jueqiao.jianghu.ui.screens.volume1part7.Volume1Part7Screen
import com.jueqiao.jianghu.ui.screens.volume1part8.Volume1Part8Screen
import com.jueqiao.jianghu.ui.screens.volume1part9.Volume1Part9Screen
import com.jueqiao.jianghu.ui.screens.volume2part1.Volume2Part1Screen
import com.jueqiao.jianghu.ui.screens.volume2part10.Volume2Part10Screen
import com.jueqiao.jianghu.ui.screens.volume2part11.Volume2Part11Screen
import com.jueqiao.jianghu.ui.screens.volume2part12.Volume2Part12Screen
import com.jueqiao.jianghu.ui.screens.volume2part13.Volume2Part13Screen
import com.jueqiao.jianghu.ui.screens.volume2part14.Volume2Part14Screen
import com.jueqiao.jianghu.ui.screens.volume2part15.Volume2Part15Screen
import com.jueqiao.jianghu.ui.screens.volume2part2.Volume2Part2Screen
import com.jueqiao.jianghu.ui.screens.volume2part3.Volume2Part3Screen
import com.jueqiao.jianghu.ui.screens.volume2part4.Volume2Part4Screen
import com.jueqiao.jianghu.ui.screens.volume2part5.Volume2Part5Screen
import com.jueqiao.jianghu.ui.screens.volume2part6.Volume2Part6Screen
import com.jueqiao.jianghu.ui.screens.volume2part7.Volume2Part7Screen
import com.jueqiao.jianghu.ui.screens.volume2part8.Volume2Part8Screen
import com.jueqiao.jianghu.ui.screens.volume2part9.Volume2Part9Screen
import com.jueqiao.jianghu.ui.screens.volume3part1.Volume3Part1Screen
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.launch

/**
 * 单册漫画阅读容器。页码是容器内部状态，翻页不再向返回栈追加路由。
 * 旧页面组件只负责复用现有图像排版；上层透明手势区统一接管轻触和左右滑动。
 */
@Composable
fun ManualReaderScreen(
    volumeNo: Int,
    lessonPageNo: Int? = null,
    isCompleting: Boolean,
    message: String?,
    onBack: () -> Unit,
    onComplete: () -> Unit,
) {
    val pageIndices = readerPageIndices(volumeNo, lessonPageNo)
    val pageCount = pageIndices.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    BackHandler(enabled = true, onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isCompleting,
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().clearAndSetSemantics {}) {
                    ReaderPageArtwork(
                        volumeNo = volumeNo,
                        pageIndex = pageIndices[page],
                        onBack = onBack,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("manual_reader_page_${page + 1}")
                        .semantics {
                            role = Role.Button
                            contentDescription = if (page == pageCount - 1) {
                                "漫画第${page + 1}页，共${pageCount}页，轻触完成阅读"
                            } else {
                                "漫画第${page + 1}页，共${pageCount}页，轻触继续，也可左右滑动"
                            }
                        }
                        .clickable(enabled = !isCompleting) {
                            if (page == pageCount - 1) {
                                onComplete()
                            } else {
                                scope.launch { pagerState.scrollToPage(page + 1) }
                            }
                        },
                )
            }
        }

        ReaderBackButton(onBack = onBack)

        Surface(
            color = Color(0xDDF7F0E2),
            contentColor = Color(0xFF294A2E),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 18.dp)
                .testTag("manual_reader_progress"),
        ) {
            Text(
                text = buildString {
                    append("${pagerState.currentPage + 1}/$pageCount")
                    append(if (pagerState.currentPage == pageCount - 1) " · 轻触完成" else " · 轻触或左右滑动")
                },
                fontFamily = YaHei,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        if (isCompleting) {
            Surface(
                color = Color(0xE8F7F0E2),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFF397D61),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        } else if (!message.isNullOrBlank()) {
            Surface(
                color = Color(0xEEF7F0E2),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 72.dp),
            ) {
                Text(
                    text = message,
                    color = Color(0xFF7B4337),
                    fontFamily = YaHei,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ReaderBackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .windowInsetsPadding(WindowInsets.statusBars)
            .offset(x = 15.dp, y = 8.dp)
            .size(48.dp)
            .testTag("manual_reader_back")
            .semantics {
                role = Role.Button
                contentDescription = "退出整册漫画"
            }
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_shilian_return),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

internal fun readerPageCount(volumeNo: Int): Int = when (volumeNo) {
    1 -> 14
    2 -> 15
    3 -> 1
    else -> 1
}

/**
 * 第一卷的漫画按五招实际内容分段。其他卷尚未完成逐招编目，继续显示整册，避免
 * 在没有审核依据时机械切分或漏掉页面。
 */
internal fun readerPageIndices(volumeNo: Int, lessonPageNo: Int?): List<Int> {
    if (volumeNo != 1) return (0 until readerPageCount(volumeNo)).toList()
    val range = when (lessonPageNo) {
        1 -> 0..2
        2 -> 3..5
        3 -> 6..7
        4 -> 8..10
        5 -> 11..13
        else -> 0 until readerPageCount(volumeNo)
    }
    return range.toList()
}

@Composable
private fun ReaderPageArtwork(volumeNo: Int, pageIndex: Int, onBack: () -> Unit) {
    val noNext: () -> Unit = {}
    when (volumeNo) {
        1 -> when (pageIndex) {
            0 -> Volume1Screen(onBack, noNext)
            1 -> Volume1Part2Screen(onBack, noNext)
            2 -> Volume1Part3Screen(onBack, noNext)
            3 -> Volume1Part4Screen(onBack, noNext)
            4 -> Volume1Part5Screen(onBack, noNext)
            5 -> Volume1Part6Screen(onBack, noNext)
            6 -> Volume1Part7Screen(onBack, noNext)
            7 -> Volume1Part8Screen(onBack, noNext)
            8 -> Volume1Part9Screen(onBack, noNext)
            9 -> Volume1Part10Screen(onBack, noNext)
            10 -> Volume1Part11Screen(onBack, noNext)
            11 -> Volume1Part12Screen(onBack, noNext)
            12 -> Volume1Part13Screen(onBack, noNext)
            else -> Volume1Part14Screen(onBack, noNext)
        }
        2 -> when (pageIndex) {
            0 -> Volume2Part1Screen(onBack, noNext)
            1 -> Volume2Part2Screen(onBack, noNext)
            2 -> Volume2Part3Screen(onBack, noNext)
            3 -> Volume2Part4Screen(onBack, noNext)
            4 -> Volume2Part5Screen(onBack, noNext)
            5 -> Volume2Part6Screen(onBack, noNext)
            6 -> Volume2Part7Screen(onBack, noNext)
            7 -> Volume2Part8Screen(onBack, noNext)
            8 -> Volume2Part9Screen(onBack, noNext)
            9 -> Volume2Part10Screen(onBack, noNext)
            10 -> Volume2Part11Screen(onBack, noNext)
            11 -> Volume2Part12Screen(onBack, noNext)
            12 -> Volume2Part13Screen(onBack, noNext)
            13 -> Volume2Part14Screen(onBack, noNext)
            else -> Volume2Part15Screen(onBack, noNext)
        }
        else -> Volume3Part1Screen(onBack)
    }
}
