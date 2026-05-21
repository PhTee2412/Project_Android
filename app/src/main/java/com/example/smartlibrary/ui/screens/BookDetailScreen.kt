package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.ui.viewmodel.BookDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onBack: () -> Unit
) {
    val book by viewModel.book.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sách") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF062D76)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = error ?: "Lỗi không xác định",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                book?.let { details ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AsyncImage(
                                model = details.hinhAnh?.firstOrNull() ?: "",
                                contentDescription = details.tenSach,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.75f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFF0F7FF))
                                    .padding(16.dp)
                            ) {
                                val isAvailable = details.trangThai == "CON_SAN"

                                Text(
                                    text = details.tenSach,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF062D76)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tác giả: ${details.tenTacGia ?: "Chưa rõ"}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Thể loại: ${details.categoryChildName ?: "—"}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "NXB: ${details.nxb ?: "—"}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Trạng thái: ${if (isAvailable) "Còn sẵn" else "Đã hết"}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (isAvailable) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Lượt mượn: ${details.soLuongMuon} lượt",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { 
                                            if (isLoggedIn) {
                                                viewModel.borrowBook(onSuccess = {})
                                            } else {
                                                onLoginRequired()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        enabled = isAvailable,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF30C9E8),
                                            disabledContainerColor = Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Mượn ngay", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }

                                    OutlinedButton(
                                        onClick = { 
                                            if (isLoggedIn) {
                                                viewModel.addToCart()
                                            } else {
                                                onLoginRequired()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        enabled = isAvailable,
                                        shape = RoundedCornerShape(24.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isAvailable) Color(0xFF062D76) else Color.LightGray
                                        )
                                    ) {
                                        Text(
                                            "Thêm vào giỏ",
                                            fontSize = 14.sp,
                                            color = if (isAvailable) Color(0xFF062D76) else Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFF0F7FF))
                                    .padding(16.dp)
                            ) {
                                SectionHeader("Thông tin chi tiết")
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailRow("Mã sách:", details.maSach.toString())
                                DetailRow("Năm XB:", details.nam?.toString() ?: "—")
                                DetailRow("Trọng lượng:", "${details.trongLuong ?: 0} g")
                                DetailRow("Đơn giá:", "${details.donGia?.toInt() ?: 0} đ")
                                DetailRow("Tổng số lượng:", details.tongSoLuong.toString())
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFF0F7FF))
                                    .padding(16.dp)
                            ) {
                                SectionHeader("Giới thiệu")
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = details.moTa ?: "Chưa có mô tả.",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = Color(0xFF9CE5F4),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF062D76)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label ",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A)
        )
    }
}
