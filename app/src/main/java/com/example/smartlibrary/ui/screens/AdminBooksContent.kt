package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.ui.viewmodel.AdminBooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBooksContent(
    viewModel: AdminBooksViewModel,
    onBookClick: (Long) -> Unit = {},
    onAddBookClick: () -> Unit = {},
    onEditBookClick: (Long) -> Unit = {},
    onCategoryManageClick: () -> Unit = {}
) {
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val deleteBook by viewModel.deleteBook.collectAsState()
    val message by viewModel.message.collectAsState()

    val filteredBooks = remember(books, statusFilter) {
        when (statusFilter) {
            "all" -> books
            else -> books.filter { it.trangThai == statusFilter }
        }
    }
    val totalPages = remember(filteredBooks) {
        if (filteredBooks.isEmpty()) 1 else (filteredBooks.size - 1) / 10 + 1
    }
    val paginatedBooks = remember(filteredBooks, currentPage) {
        val from = (currentPage - 1) * 10
        val to = minOf(from + 10, filteredBooks.size)
        if (from < filteredBooks.size) filteredBooks.subList(from, to) else emptyList()
    }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.loadBooksIfNeeded()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    deleteBook?.let { book ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Xác nhận xóa sách", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Bạn có chắc chắn muốn xóa cuốn sách này không?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = book.hinhAnh?.firstOrNull() ?: "",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    book.tenSach,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Mã: ${book.maSach}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Xác nhận xóa") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Hủy bỏ") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookClick,
                containerColor = Color(0xFF6CB1DA),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sách mới")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7FD))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val commonHeight = 50.dp

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(0.35f).height(commonHeight),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        color = Color.White
                    ) {
                        var searchExpanded by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { searchExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (searchMode) {
                                        "title" -> "Tên sách"
                                        "author" -> "Tác giả"
                                        "category" -> "Thể loại"
                                        "publisher" -> "NXB"
                                        "year" -> "Năm"
                                        else -> "Tất cả"
                                    },
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(expanded = searchExpanded, onDismissRequest = { searchExpanded = false }) {
                                listOf(
                                    "all" to "Tất cả",
                                    "title" to "Tên sách",
                                    "author" to "Tác giả",
                                    "category" to "Thể loại",
                                    "publisher" to "NXB",
                                    "year" to "Năm"
                                ).forEach { (v, l) ->
                                    DropdownMenuItem(
                                        text = { Text(l) },
                                        onClick = { viewModel.setSearchMode(v); searchExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(0.65f).height(commonHeight),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        color = Color.White
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Từ khóa...", fontSize = 13.sp, fontStyle = FontStyle.Italic) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (searchMode == "year") KeyboardType.Number else KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(onSearch = {
                                viewModel.searchBooks()
                                focusManager.clearFocus()
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6CB1DA),
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(0.35f).height(commonHeight),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp,
                        color = Color.White
                    ) {
                        var statusExpanded by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { statusExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (statusFilter) {
                                        "CON_SAN" -> "Còn sẵn"
                                        "DA_HET" -> "Đã hết"
                                        "DA_XOA" -> "Đã xóa"
                                        else -> "Tất cả"
                                    },
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                                listOf(
                                    "all" to "Tất cả",
                                    "CON_SAN" to "Còn sẵn",
                                    "DA_HET" to "Đã hết",
                                    "DA_XOA" to "Đã xóa"
                                ).forEach { (v, l) ->
                                    DropdownMenuItem(
                                        text = { Text(l) },
                                        onClick = { viewModel.setStatusFilter(v); statusExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.searchBooks(); focusManager.clearFocus() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                        modifier = Modifier.weight(0.25f).height(commonHeight),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tìm", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onCategoryManageClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                        modifier = Modifier.weight(0.40f).height(commonHeight),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thể loại", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6CB1DA))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (paginatedBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Không tìm thấy kết quả phù hợp", color = Color.Gray)
                            }
                        }
                    } else {
                        items(paginatedBooks, key = { it.maSach }) { book ->
                            BookAdminCard(
                                book = book,
                                onView = { onBookClick(book.maSach) },
                                onEdit = { onEditBookClick(book.maSach) },
                                onDelete = { viewModel.requestDelete(book) }
                            )
                        }

                        if (totalPages > 1) {
                            item {
                                PaginationSection(
                                    currentPage = currentPage,
                                    totalPages = totalPages,
                                    onPageChange = { viewModel.setPage(it) }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BookAdminCard(
    book: BookResponse,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = book.hinhAnh?.firstOrNull() ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F2F5)),
                    contentScale = ContentScale.Crop
                )

                val statusDotColor = when (book.trangThai) {
                    "CON_SAN" -> Color(0xFF4CAF50)
                    "DA_HET" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                        .border(1.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.tenSach,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                Text(
                    text = book.tenTacGia ?: "N/A",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng: ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = book.tongSoLuong.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6CB1DA)
                    )
                    Text(
                        text = " | Mượn: ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = book.soLuongMuon.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val (statusLabel, statusColor) = when (book.trangThai) {
                    "DA_XOA" -> "Đã xóa" to Color.Red
                    "DA_HET" -> "Đã hết" to Color(0xFF5C4033)
                    else -> "Còn sẵn" to Color(0xFF2E7D32)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái: ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                ActionButtonCompact(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Xem",
                    containerColor = Color(0xFF6CB1DA),
                    onClick = onView
                )
                Spacer(modifier = Modifier.height(6.dp))
                ActionButtonCompact(
                    icon = Icons.Default.Edit,
                    label = "Sửa",
                    containerColor = Color(0xFF6CB1DA),
                    onClick = onEdit
                )
                if (book.trangThai != "DA_XOA") {
                    Spacer(modifier = Modifier.height(6.dp))
                    ActionButtonCompact(
                        icon = Icons.Default.Delete,
                        label = "Xóa",
                        containerColor = Color(0xFFD66766),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtonCompact(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(28.dp)
            .width(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                color = Color.White,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PaginationSection(currentPage: Int, totalPages: Int, onPageChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6CB1DA)),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Default.NavigateBefore, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Trang trước", fontSize = 13.sp)
        }

        Text(
            "$currentPage / $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF6CB1DA)
        )

        OutlinedButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6CB1DA)),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text("Trang sau", fontSize = 13.sp)
            Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
