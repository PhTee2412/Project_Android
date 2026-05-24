package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.network.ChildBookResponse
import com.example.smartlibrary.ui.viewmodel.AdminBookDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookDetailContent(
    viewModel: AdminBookDetailViewModel,
    onBack: () -> Unit = {}
) {
    val book by viewModel.book.collectAsState()
    val childBooks by viewModel.childBooks.collectAsState()
    val filteredChildBooks by viewModel.filteredChildBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val actionLoading by viewModel.actionLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val deleteTarget by viewModel.deleteTarget.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Logic tính toán: Còn sẵn = Tổng - Đang mượn (BORROWED)
    // Không trừ thêm 'Đã xóa' theo yêu cầu mới nhất của bạn.
    val currentBorrowedCount = remember(childBooks) {
        childBooks.count { it.status == "BORROWED" }
    }
    
    // Nếu sách đã xóa (DA_XOA) thì mặc định còn sẵn là 0.
    val availableCountDisplay = if (book?.trangThai == "DA_XOA") {
        0
    } else {
        ((book?.tongSoLuong ?: 0) - currentBorrowedCount).coerceAtLeast(0)
    }

    // Dialog xác nhận xóa bản sao
    deleteTarget?.let { child ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Xác nhận xóa bản sao", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa bản sao ID: ${child.id}?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFEFF3FB)
    ) { paddingValues ->
        if (isLoading && book == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF062D76))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Ô tìm kiếm
                item(span = { GridItemSpan(2) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Tìm ID hoặc Barcode...", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9CE5F4),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Button(
                            onClick = { /* ViewModel filters automatically */ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm", tint = Color.White)
                        }
                    }
                }

                // 2. Card 1: Thông tin chính (Ảnh bên trái, Info cơ bản bên phải)
                item(span = { GridItemSpan(2) }) {
                    book?.let { AdminBookMainInfoCard(it) }
                }

                // 3. Card 2: Thống kê & Thể loại (2 cột)
                item(span = { GridItemSpan(2) }) {
                    book?.let { AdminBookStatsCard(it, availableCountDisplay) }
                }

                // 4. Nút Thêm bản sao mới
                item(span = { GridItemSpan(2) }) {
                    Button(
                        onClick = { viewModel.addChildBook() },
                        enabled = !actionLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (actionLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm bản sao mới", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. Tiêu đề danh sách bản sao
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Danh sách bản sao (${childBooks.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF062D76),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (filteredChildBooks.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text(if (searchQuery.isNotEmpty()) "Không tìm thấy kết quả" else "Chưa có bản sao nào", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredChildBooks, key = { it.id }) { child ->
                        AdminChildBookCard(
                            child = child,
                            onDelete = { viewModel.requestDelete(child) },
                            onDownload = { viewModel.downloadBarcode(context, child.barcode ?: "") }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AdminBookMainInfoCard(book: BookResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.hinhAnh?.firstOrNull() ?: "",
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailLabelRow("ID:", book.maSach.toString())
                DetailLabelRow("Tên sách:", book.tenSach, isBold = true, color = Color(0xFF062D76))
                DetailLabelRow("Tác giả:", book.tenTacGia ?: "N/A")
                DetailLabelRow("Nhà xuất bản:", book.nxb ?: "N/A")
                DetailLabelRow("Năm sản xuất:", book.nam?.toString() ?: "N/A")
            }
        }
    }
}

@Composable
fun AdminBookStatsCard(book: BookResponse, available: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailLabelVertical("Thể loại chính:", book.categoryParentName ?: "N/A")
                DetailLabelVertical("Thể loại phụ:", book.categoryChildName ?: "N/A")
                DetailLabelVertical("Tổng số lượng:", book.tongSoLuong.toString(), color = Color(0xFF062D76))
            }
            Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailLabelVertical("Còn sẵn:", available.toString(), color = Color(0xFF2ECC71))
                DetailLabelVertical("Đã mượn:", book.soLuongMuon.toString(), color = Color(0xFFF1C40F))
                DetailLabelVertical("Đã xóa:", book.soLuongXoa.toString(), color = Color(0xFFE74C3C))
            }
        }
    }
}

@Composable
fun DetailLabelRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DetailLabelVertical(label: String, value: String, color: Color = Color.Black) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AdminChildBookCard(
    child: ChildBookResponse,
    onDelete: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ID: ${child.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    val (icon, color) = when (child.status) {
                        "AVAILABLE", "CON_SAN" -> Icons.Default.CheckCircle to Color(0xFF2ECC71)
                        "BORROWED" -> Icons.Default.Schedule to Color(0xFFF1C40F)
                        else -> Icons.Default.Cancel to Color(0xFFE74C3C)
                    }
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE74C3C), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F3F5), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            
            if (!child.barcode.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = "https://barcodeapi.org/api/128/${child.barcode}",
                        contentDescription = "Barcode",
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(child.barcode, fontSize = 9.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color(0xFFEBEDF0))
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tải về", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
