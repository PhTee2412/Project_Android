package com.example.smartlibrary.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun AboutScreen(navController: androidx.navigation.NavController? = null) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Dữ liệu tính năng
    val features = remember {
        listOf(
            FeatureItem("Tra cứu sách", "Tìm kiếm nhanh chóng theo tên, tác giả, thể loại.", Icons.Default.Search),
            FeatureItem("Mượn/Trả", "Quản lý mượn, trả và gia hạn sách dễ dàng.", Icons.Default.Book),
            FeatureItem("Người dùng", "Quản lý sinh viên, giảng viên và thủ thư.", Icons.Default.People),
            FeatureItem("Thống kê", "Tự động thống kê số lượng sách, lượt mượn.", Icons.Default.BarChart),
            FeatureItem("Chat AI", "Hỗ trợ chat trực tuyến với AI các thắc mắc.", Icons.AutoMirrored.Filled.Chat),
            FeatureItem("Bảo mật", "Dữ liệu an toàn trên máy chủ bảo mật.", Icons.Default.Storage),
            FeatureItem("Hiện đại", "Giao diện trực quan, đa nền tảng.", Icons.Default.Star),
            FeatureItem("Thông báo", "Gửi thông báo khi sắp đến hạn trả sách.", Icons.Default.Notifications)
        )
    }

    // Dữ liệu thành viên
    val teamMembers = remember {
        listOf(
            TeamMember("Lê Thị Phương Thảo", "23521468", "https://i.pinimg.com/736x/bc/fd/9a/bcfd9a2d158eb0f2c36bf5b1126c0bfc.jpg"),
            TeamMember("Nguyễn Gia Bảo", "23520120", "https://i.pinimg.com/736x/bc/fd/9a/bcfd9a2d158eb0f2c36bf5b1126c0bfc.jpg")
        )
    }

    // Slider cho ảnh giới thiệu
    val introImages = remember {
        listOf(
            "https://i.pinimg.com/736x/bc/fd/9a/bcfd9a2d158eb0f2c36bf5b1126c0bfc.jpg",
            "https://i.pinimg.com/736x/bc/fd/9a/bcfd9a2d158eb0f2c36bf5b1126c0bfc.jpg",
            "https://i.pinimg.com/736x/bc/fd/9a/bcfd9a2d158eb0f2c36bf5b1126c0bfc.jpg"
        )
    }
    val introPagerState = rememberPagerState(pageCount = { introImages.size })
    LaunchedEffect(introPagerState) {
        while (true) {
            delay(3000)
            if (introImages.isNotEmpty()) {
                val nextPage = (introPagerState.currentPage + 1) % introImages.size
                introPagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Slider cho thành viên
    val teamPagerState = rememberPagerState(pageCount = { teamMembers.size })
    LaunchedEffect(teamPagerState) {
        while (true) {
            delay(3000)
            if (teamMembers.isNotEmpty()) {
                val nextPage = (teamPagerState.currentPage + 1) % teamMembers.size
                teamPagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Hàm kiểm tra item có nằm trong viewport không
    fun isVisible(index: Int): Boolean {
        val layoutInfo = listState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        return visibleItems.any { it.index == index }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7FF)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- 1. Hero Section ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Biến trạng thái cho hiệu ứng gõ chữ
                var helloText by remember { mutableStateOf("") }
                var brandText by remember { mutableStateOf("") }
                val fullHelloText = "Hello, we are"
                val fullBrandText = "8ooK !"

                LaunchedEffect(Unit) {
                    // Gõ dòng thứ nhất
                    fullHelloText.forEachIndexed { index, _ ->
                        helloText = fullHelloText.substring(0, index + 1)
                        delay(100)
                    }
                    // Gõ dòng thứ hai
                    fullBrandText.forEachIndexed { index, _ ->
                        brandText = fullBrandText.substring(0, index + 1)
                        delay(150)
                    }
                }

                Text(
                    text = helloText,
                    color = Color(0xFFEC4899),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = brandText,
                    color = Color(0xFF047857),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Hệ thống quản lý thư viện thông minh giúp tra cứu, mượn trả và quản lý sách hiệu quả.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(
                    onClick = { navController?.navigate("home") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66D7EE)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Khám phá ngay", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        // --- 2. Slider ảnh giới thiệu ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = introPagerState,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF30C9E8), CircleShape)
                ) { page ->
                    AsyncImage(
                        model = introImages[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // --- 3. Tech badges ---
        item {
            val techs = listOf(
                Triple("Jetpack Compose", Icons.Default.Code, Color(0xFF6B7280)),
                Triple("Spring Boot", Icons.Default.Build, Color(0xFFEC4899)),
                Triple("Supabase", Icons.Default.Storage, Color(0xFF14B8A6))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                techs.forEach { (name, icon, color) ->
                    Surface(
                        modifier = Modifier,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- 4. Tính năng NỔI BẬT ---
        item {
            AnimatedSectionTitle("Tính năng", "NỔI BẬT")
        }
        itemsIndexed(features) { index, feature ->
            val isItemVisible = remember { mutableStateOf(false) }
            LaunchedEffect(listState) {
                snapshotFlow { isVisible(index) }
                    .collect { visible ->
                        if (visible) isItemVisible.value = true
                    }
            }
            AnimatedVisibility(
                visible = isItemVisible.value,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            feature.icon,
                            null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                feature.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                feature.description,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // --- 5. Nhóm phát triển ---
        item {
            AnimatedSectionTitle("Nhóm", "PHÁT TRIỂN")
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = teamPagerState,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFF30C9E8), CircleShape)
                    ) { page ->
                        AsyncImage(
                            model = teamMembers[page].imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    if (teamMembers.isNotEmpty()) {
                        val current = teamMembers[teamPagerState.currentPage]
                        Text(
                            current.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0E7490),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "MSSV: ${current.mssv}",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- 6. Liên hệ ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "LIÊN HỆ",
                    color = Color(0xFFEC4899),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { Toast.makeText(context, "23521468@uit.edu.vn", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFF97A97C)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE7E1D7))
                ) {
                    Icon(Icons.Default.Email, null, tint = Color(0xFF53624E))
                    Spacer(Modifier.width(8.dp))
                    Text("Gửi Email", color = Color(0xFF53624E))
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AnimatedSectionTitle(t1: String, t2: String) {
    val visible = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        visible.animateTo(1f, animationSpec = tween(800))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .graphicsLayer { alpha = visible.value }
    ) {
        Text(
            t1,
            color = Color(0xFF047857),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            t2,
            color = Color(0xFFEC4899),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

data class FeatureItem(val title: String, val description: String, val icon: ImageVector)
data class TeamMember(val name: String, val mssv: String, val imageUrl: String)
