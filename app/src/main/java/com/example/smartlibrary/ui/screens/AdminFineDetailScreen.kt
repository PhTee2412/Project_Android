package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
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
import com.example.smartlibrary.ui.viewmodel.AdminFineDetailViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun AdminFineDetailContent(
    viewModel: AdminFineDetailViewModel
) {
    val fine by viewModel.fine.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF3FB))
    ) {
        if (isLoading && fine == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6CB1DA))
            }
        } else if (fine == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy thông tin phiếu phạt.", color = Color.Gray)
            }
        } else {
            val currentFine = fine!!
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Box 1: Thông tin chung
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Mã Phiếu: ${currentFine.id}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF062D76),
                                        fontSize = 16.sp
                                    )
                                    StatusBadge(status = currentFine.trangThai ?: "")
                                }

                                Text(
                                    text = "Số Tiền: ${currencyFormat.format(currentFine.soTien ?: 0.0)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    fontSize = 20.sp
                                )

                                Text(text = "Nội Dung: ${currentFine.noiDung}", fontWeight = FontWeight.Medium)
                                
                                Divider(color = Color(0xFFF1F4F9))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        DetailItem(label = "ID Người Dùng", value = currentFine.userId?.id?.toString() ?: "N/A")
                                        DetailItem(label = "Tên Người Dùng", value = currentFine.tenND ?: "N/A")
                                    }
                                    if (currentFine.trangThai == "DA_THANH_TOAN") {
                                        Column(modifier = Modifier.weight(1f)) {
                                            DetailItem(
                                                label = "Ngày Thanh Toán", 
                                                value = currentFine.ngayThanhToan ?: "N/A",
                                                valueColor = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Box 2: Chi tiết vi phạm
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Chi tiết vi phạm",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Divider(color = Color(0xFFF1F4F9))

                                when (currentFine.noiDung) {
                                    "Trả sách trễ hạn" -> {
                                        val borrow = currentFine.cardId
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                DetailItem(label = "Mã Phiếu Mượn", value = "#${borrow?.id ?: "N/A"}")
                                                DetailItem(label = "Số Ngày Trễ", value = "${borrow?.soNgayTre ?: 0} ngày", valueColor = Color.Red)
                                            }
                                            Column {
                                                DetailItem(label = "Ngày Mượn", value = borrow?.getBookDate ?: "N/A")
                                                DetailItem(label = "Hạn Trả", value = borrow?.dueDate ?: "N/A")
                                            }
                                        }
                                        
                                        borrow?.borrowedBooks?.forEach { book ->
                                            AdminBookInFineCard(book)
                                        }
                                    }
                                    "Làm mất sách" -> {
                                        currentFine.cardId?.borrowedBooks?.forEach { book ->
                                            AdminBookInFineCard(book)
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F4F9))
                                                .padding(12.dp)
                                        ) {
                                            Text(text = "Ghi chú: ${currentFine.cardId?.id ?: "Không có thông tin thêm"}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer: Thanh toán
                if (currentFine.trangThai == "CHUA_THANH_TOAN") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Button(
                            onClick = { viewModel.payFine() },
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                            shape = RoundedCornerShape(25.dp),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Xác nhận Thanh Toán", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "DA_THANH_TOAN" -> Color(0xFF2E7D32) to "Đã thanh toán"
        else -> Color(0xFFE65100) to "Chưa thanh toán"
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String, valueColor: Color = Color.Black) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun AdminBookInFineCard(book: BorrowedBookBrief) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = book.image,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp, 70.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = book.name ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text(text = book.author ?: "N/A", fontSize = 12.sp, color = Color.Gray)
                Text(text = "ID: ${book.bookId}", fontSize = 11.sp, color = Color(0xFF062D76))
            }
        }
    }
}
