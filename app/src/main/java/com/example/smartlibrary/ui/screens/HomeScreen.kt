package com.example.smartlibrary.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.data.model.Book
import com.example.smartlibrary.ui.components.BookCard
import com.example.smartlibrary.ui.components.BookRecommendCard
import com.example.smartlibrary.ui.components.ServiceHoursCard
import com.example.smartlibrary.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onBookClick: (String) -> Unit,
    onChatBotClick: () -> Unit
) {
    val allBooks by viewModel.allBooks.collectAsState()
    val suggestedBooks by viewModel.suggestedBooks.collectAsState()
    val sidebarBooks by viewModel.sidebarBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val displayedCount by viewModel.displayedCount.collectAsState()
    val isChatBotVisible by viewModel.isChatBotVisible.collectAsState()

    val displayedBooks = allBooks.take(displayedCount)
    val hasNextPage = displayedCount < allBooks.size

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && allBooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF30C9E8))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- PHẦN SLIDER + SÁCH NGẪU NHIÊN ---
                item(span = { GridItemSpan(2) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SliderSection()
                        Text(
                            text = "Khám phá ngẫu nhiên",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        SidebarBooksSection(sidebarBooks, onBookClick)
                    }
                }

                // --- PHẦN "CÓ THỂ BẠN SẼ THÍCH" ---
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF9CE5F4), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Có thể bạn sẽ thích", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (suggestedBooks.isEmpty()) {
                            Text("Đang cập nhật sách gợi ý...", color = Color.Gray)
                        } else {
                            val virtualPageCount = 10000
                            val initialPage = (virtualPageCount / 2) - ((virtualPageCount / 2) % suggestedBooks.size)
                            val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { virtualPageCount })

                            LaunchedEffect(suggestedBooks) {
                                while (true) {
                                    delay(4000)
                                    if (pagerState.pageCount > 0) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                pageSize = PageSize.Fixed(155.dp),
                                contentPadding = PaddingValues(horizontal = 9.dp),
                                pageSpacing = 10.dp,
                                beyondViewportPageCount = 2
                            ) { page ->
                                val actualIndex = page % suggestedBooks.size
                                BookRecommendCard(book = suggestedBooks[actualIndex], onClick = onBookClick)
                            }
                        }
                    }
                }

                // --- PHẦN "SÁCH HAY NÈ" ---
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF9CE5F4), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Sách hay nè", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                displayedBooks.filterIndexed { i, _ -> i % 2 == 0 }.forEach { book ->
                                    BookCard(book = book, onClick = onBookClick)
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                displayedBooks.filterIndexed { i, _ -> i % 2 == 1 }.forEach { book ->
                                    BookCard(book = book, onClick = onBookClick)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadMoreBooks() },
                            enabled = hasNextPage,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF062D76),
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (hasNextPage) "Xem thêm sách" else "Đã tải toàn bộ sách")
                        }
                    }
                }
            }
        }

        // FAB + nút ẩn chat
        if (isLoggedIn && isChatBotVisible) {
            FloatingActionButton(
                onClick = onChatBotClick,
                containerColor = Color(0xFF30C9E8),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp)
            ) {
                Box {
                    Icon(Icons.AutoMirrored.Outlined.Message, contentDescription = "ChatBot")
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .size(18.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .clickable { viewModel.setChatBotVisibility(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SliderSection() {
    val images = listOf(
        "https://mintbook.com/blog/wp-content/uploads/Must-Have-Digital-Library-Tools-1.jpeg.webp",
        "https://static.vecteezy.com/system/resources/thumbnails/027/196/314/small/concept-of-electronic-library-online-bookstore-ebook-online-library-people-reading-books-with-digital-library-service-users-learn-with-book-archives-illustration-for-web-design-vector.jpg",
        "https://img.freepik.com/free-vector/online-library-app-reading-banner_33099-1733.jpg",
        "https://img.freepik.com/free-vector/audio-books-isometric-composition-with-character-female-librarian-with-book-shelves-inside-smartphone-screen-frame-vector-illustration_1284-80591.jpg"
    )

    val pagerState = rememberPagerState(pageCount = { images.size + 1 })

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (pagerState.pageCount > 0) {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                ServiceHoursCard()
            } else {
                AsyncImage(
                    model = images[page - 1],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun SidebarBooksSection(books: List<Book>, onBookClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        books.forEach { book ->
            AnimatedContent(
                targetState = book,
                transitionSpec = {
                    fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { -it }
                },
                label = "SidebarBook"
            ) { targetBook ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onBookClick(targetBook.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = targetBook.imageSrc,
                            contentDescription = targetBook.title,
                            modifier = Modifier
                                .width(85.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = targetBook.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = targetBook.author ?: "Chưa rõ",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = Color(0xFFE1F0FF),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${targetBook.borrowCount} lượt xem",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007BFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}