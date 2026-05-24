package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.smartlibrary.network.BorrowedBookBrief
import com.example.smartlibrary.network.BorrowCardDetailResponse
import com.example.smartlibrary.ui.viewmodel.BorrowDetailViewModel

@Composable
fun BorrowDetailScreen(
    viewModel: BorrowDetailViewModel,
    onDeleted: () -> Unit
) {
    val detail by viewModel.borrowDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val onDeletedSuccess by viewModel.onDeleted.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(onDeletedSuccess) {
        if (onDeletedSuccess) {
            onDeleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FD))
    ) {
        if (isLoading && detail == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF062D76))
        } else if (detail != null) {
            val info = detail!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa phiếu")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoColumn(label = "ID Phiếu", value = info.id.toString(), modifier = Modifier.weight(1f))
                            InfoColumn(label = "ID Người Dùng", value = info.userId.toString(), modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoColumn(label = "Tên Người Dùng", value = info.userName ?: "N/A", modifier = Modifier.weight(1f))
                            InfoColumn(label = "Số lượng mượn", value = "${info.totalBooks ?: 0} cuốn", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoColumn(label = "Ngày mượn", value = formatDate(info.borrowDate), modifier = Modifier.weight(1f))
                            val dateLabel = if (info.dueDate != null) "Ngày trả" else "Hạn lấy sách"
                            val dateValue = info.dueDate ?: info.getBookDate
                            InfoColumn(label = dateLabel, value = formatDate(dateValue), modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Danh sách sách mượn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF062D76),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(info.bookIds ?: emptyList()) { book ->
                        BorrowedBookItem(book = book)
                    }
                }
            }
        } else {
            Text("Không tìm thấy thông tin phiếu mượn", modifier = Modifier.align(Alignment.Center))
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc chắn muốn xóa phiếu mượn này không?") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.deleteCard()
                        showDeleteDialog = false
                    }) {
                        Text("Xóa", color = Color.Red)
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
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BorrowedBookItem(book: BorrowedBookBrief) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.image ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.name ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "ID sách: ${book.bookId}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Tác giả: ${book.author ?: "N/A"}", fontSize = 14.sp)
                Text(text = "Thể loại: ${book.category ?: "N/A"}", fontSize = 14.sp)
                Text(text = "NXB: ${book.publisher ?: "N/A"}", fontSize = 14.sp)
                if (book.viTri != null) {
                    Text(text = "Vị trí tủ: ${book.viTri}", fontSize = 14.sp, color = Color(0xFF062D76))
                }
            }
        }
    }
}
