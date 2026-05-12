package com.example.smartlibrary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartlibrary.data.model.Book
import kotlinx.coroutines.delay

@Composable
fun RecommendCarousel(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group books into pages of 4
    val pageSize = 4
    val pages = books.chunked(pageSize)

    if (pages.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Auto-scroll logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pages[page].forEach { book ->
                Box(modifier = Modifier.weight(1f)) {
                    BookRecommendCard(book = book, onClick = onBookClick)
                }
            }
            // Fill remaining space if less than 4 items
            repeat(pageSize - pages[page].size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
