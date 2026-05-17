package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.ui.viewmodel.CartViewModel

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBookClick: (String) -> Unit
) {
    val cartBooks by viewModel.cartBooks.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectedIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nhóm Checkbox và Text lại gần nhau
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedIds.size == cartBooks.size && cartBooks.isNotEmpty(),
                                onCheckedChange = {
                                    if (selectedIds.size == cartBooks.size) viewModel.deselectAll()
                                    else viewModel.selectAll()
                                }
                            )
                            Text(
                                text = "Đã chọn ${selectedIds.size}/${cartBooks.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.deleteSelected() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                            ) {
                                Spacer(Modifier.width(4.dp))
                                Text("Xóa")
                            }
                            Button(
                                onClick = { viewModel.borrowSelected() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4))
                            ) {
                                Text("Mượn sách")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (cartBooks.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Giỏ sách trống, hãy thêm sách để mượn nhé 📚",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartBooks, key = { it.maSach }) { book ->
                        val isSelected = book.maSach in selectedIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookClick(book.maSach) },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)  // màu nền xanh nhạt
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Checkbox
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleSelection(book.maSach) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                // Ảnh bìa
                                AsyncImage(
                                    model = book.hinhAnh?.firstOrNull() ?: "",
                                    contentDescription = book.tenSach,
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.width(12.dp))

                                // Thông tin sách
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.tenSach,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Tác giả: ${book.tenTacGia ?: "Chưa rõ"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "NXB: ${book.nxb ?: "Chưa rõ"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    // Trạng thái
                                    val available = (book.tongSoLuong - book.soLuongMuon - book.soLuongXoa) > 0
                                    Text(
                                        text = if (available) "Còn sách" else "Hết sách",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (available) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
