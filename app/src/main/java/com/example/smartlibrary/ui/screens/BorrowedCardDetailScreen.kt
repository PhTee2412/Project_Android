package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BorrowedBookBrief
import com.example.smartlibrary.ui.viewmodel.BorrowedCardsViewModel
import com.example.smartlibrary.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowedCardDetailScreen(
    cardId: Int,
    viewModel: BorrowedCardsViewModel,
    onBack: () -> Unit
) {
    val cardDetail by viewModel.cardDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDetailLoaded by viewModel.isDetailLoaded.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        viewModel.loadCardDetail(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phiếu mượn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFE6EAF1)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF062D76))
            }
        } else if (isDetailLoaded && cardDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy thông tin phiếu mượn.", color = Color.Red)
            }
        } else if (cardDetail != null) {
            val detail = cardDetail!!
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Cột 1: ID Phiếu, Ngày mượn, Hạn lấy sách
                            // Sử dụng weight nhỏ (0.35) vì dữ liệu cột này ngắn (số và ngày)
                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                InfoItem(label = "ID Phiếu", value = detail.id.toString())
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoItem(label = "Ngày mượn", value = formatDate(detail.borrowDate))
                                Spacer(modifier = Modifier.height(12.dp))
                                val label = if (detail.dueDate != null) "Ngày trả sách" else "Hạn lấy sách"
                                val value = detail.dueDate ?: detail.getBookDate
                                InfoItem(label = label, value = formatDate(value))
                            }

                            // Cột 2: ID Người dùng, Tên người dùng, Số lượng mượn
                            // Chiếm weight lớn hơn (0.65) để đẩy lề trái của cột này xích qua trái
                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                InfoItem(label = "ID Người Dùng", value = detail.userId.toString())
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoItem(label = "Tên Người Dùng", value = detail.userName ?: "N/A")
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoItem(label = "Số lượng mượn", value = detail.totalBooks?.toString() ?: "0")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Danh sách sách mượn",
                        color = Color(0xFF062D76),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Books List
                items(detail.bookIds ?: emptyList()) { book ->
                    BorrowedBookItem(book = book)
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa phiếu mượn này không?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCard(cardId) {
                                onBack()
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BorrowedBookItem(book: BorrowedBookBrief) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.image,
                contentDescription = book.name,
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.name ?: "Không có tên", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Tác giả: ${book.author ?: "N/A"}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = "Thể loại: ${book.category ?: "N/A"}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = "NXB: ${book.publisher ?: "N/A"}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = "Lượt mượn: ${book.borrowCount ?: 0}", fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}
