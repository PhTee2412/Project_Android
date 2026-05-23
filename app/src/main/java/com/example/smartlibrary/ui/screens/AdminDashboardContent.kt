package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.ui.viewmodel.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(viewModel: AdminDashboardViewModel) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val books by viewModel.books.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeSection by viewModel.activeSection.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortByBorrowCount by viewModel.sortByBorrowCount.collectAsState()

    val displayedBooks = remember(activeSection, books, dashboardData, currentPage) {
        if (activeSection == "danhSach") {
            viewModel.getPaginatedBooks()
        } else {
            dashboardData?.booksToRestock ?: emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FD))
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Biểu đồ 1: Thống kê tổng quan
            if (dashboardData != null) {
                DashboardCard(title = "Thống kê tổng quan") {
                    val stats = listOf(
                        "Đầu sách" to dashboardData!!.totalBooks,
                        "Tổng SL" to dashboardData!!.totalBookQuantity,
                        "Sách mới" to dashboardData!!.newBooksThisMonth,
                        "Đang mượn" to dashboardData!!.borrowedBooksThisMonth
                    )
                    ModernBarChart(
                        stats = stats.map { it.first to it.second.toFloat() },
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Biểu đồ 2: Thống kê theo tuần
                DashboardCard(title = "Lượt mượn sách theo tuần trong tháng") {
                    val monthly = dashboardData!!.monthlyStats
                    if (monthly.isNotEmpty()) {
                        val weekLabels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")
                        val borrowData = monthly.take(4).mapIndexed { index, stat ->
                            (weekLabels.getOrNull(index) ?: "T${index+1}") to stat.borrowedBooks.toFloat()
                        }
                        ModernBarChart(
                            stats = borrowData,
                            colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có dữ liệu thống kê", color = Color.Gray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section Tìm kiếm & Lọc hiện đại
        item {
            SearchAndFilterSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                searchMode = searchMode,
                onSearchModeChange = { viewModel.setSearchMode(it) },
                onSearchClick = { viewModel.searchBooks() },
                sortByBorrow = sortByBorrowCount,
                onSortChange = { viewModel.setSortByBorrowCount(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    modifier = Modifier.weight(1f),
                    text = "Tất cả sách",
                    isActive = activeSection == "danhSach",
                    onClick = { viewModel.setActiveSection("danhSach") }
                )
                TabButton(
                    modifier = Modifier.weight(1f),
                    text = "Cần bổ sung",
                    badge = dashboardData?.booksToRestock?.size ?: 0,
                    isActive = activeSection == "restock",
                    onClick = { viewModel.setActiveSection("restock") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Danh sách sách
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6CB1DA))
                }
            }
        } else {
            if (displayedBooks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy kết quả nào", color = Color.Gray)
                    }
                }
            } else {
                items(displayedBooks, key = { it.maSach }) { book ->
                    ModernBookCard(book = book)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (activeSection == "danhSach" && totalPages > 1) {
                    item {
                        PaginationSection(currentPage, totalPages, viewModel)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchMode: String,
    onSearchModeChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    sortByBorrow: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tìm kiếm sách...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF6CB1DA))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF6CB1DA)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = if (searchMode == "year") KeyboardType.Number else KeyboardType.Text)
            )

            Button(
                onClick = onSearchClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA))
            ) {
                Icon(Icons.Default.Search, contentDescription = "Tìm")
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                val modes = listOf(
                    "all" to "Tất cả",
                    "title" to "Tên sách",
                    "author" to "Tác giả",
                    "category" to "Thể loại",
                    "publisher" to "NXB",
                    "year" to "Năm"
                )
                modes.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        leadingIcon = { if(searchMode == value) Icon(Icons.AutoMirrored.Filled.Sort, null, tint = Color(0xFF6CB1DA)) },
                        onClick = {
                            onSearchModeChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }

        FilterChip(
            modifier = Modifier.padding(top = 8.dp),
            selected = sortByBorrow,
            onClick = { onSortChange(!sortByBorrow) },
            label = { Text("Sắp xếp theo lượt mượn", fontSize = 12.sp) },
            leadingIcon = { if (sortByBorrow) Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(16.dp)) },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF6CB1DA).copy(alpha = 0.1f),
                selectedLabelColor = Color(0xFF6CB1DA),
                selectedLeadingIconColor = Color(0xFF6CB1DA)
            )
        )
    }
}

@Composable
fun TabButton(
    modifier: Modifier = Modifier,
    text: String,
    badge: Int = 0,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(45.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF6CB1DA) else Color(0xFFE0E7F5),
            contentColor = if (isActive) Color.White else Color(0xFF6CB1DA)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (badge > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isActive) Color.White.copy(alpha = 0.3f) else Color.White,
                    shape = CircleShape
                ) {
                    Text(
                        "$badge",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color(0xFF6CB1DA)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBookCard(book: BookResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = book.hinhAnh?.firstOrNull() ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.tenSach, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(book.tenTacGia ?: "Không rõ tác giả", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                        Text("SL: ${book.tongSoLuong}", modifier = Modifier.padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0)) {
                        Text("Mượn: ${book.soLuongMuon}", modifier = Modifier.padding(4.dp))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (book.trangThai) {
                    "CON_SAN" -> Color(0xFF4CAF50)
                    "DA_HET" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (book.trangThai) {
                            "CON_SAN" -> "Còn sẵn"
                            "DA_HET" -> "Hết sách"
                            else -> "Đã xóa"
                        },
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBarChart(stats: List<Pair<String, Float>>, colors: List<Color>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)) {
        val maxVal = (stats.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
        val barCount = stats.size
        val barWidth = size.width / (barCount * 2f)
        val barSpacing = barWidth

        // Grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = size.height - (i * (size.height / gridLines))
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        stats.forEachIndexed { index, (label, value) ->
            val barHeight = (value / maxVal) * size.height * 0.8f
            val xOffset = index * (barWidth + barSpacing) + barSpacing / 2

            // Bar with Gradient
            drawRoundRect(
                brush = Brush.verticalGradient(colors),
                topLeft = Offset(xOffset, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Label and Value
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(label, xOffset + barWidth / 2, size.height + 35f, paint)

                paint.color = colors.last().toArgb()
                paint.isFakeBoldText = true
                drawText(value.toInt().toString(), xOffset + barWidth / 2, size.height - barHeight - 10f, paint)
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun PaginationSection(currentPage: Int, totalPages: Int, viewModel: AdminDashboardViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { viewModel.setPage(currentPage - 1) },
            enabled = currentPage > 1,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6CB1DA))
        ) { Text("Trước") }

        Text(
            "$currentPage / $totalPages",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6CB1DA)
        )

        OutlinedButton(
            onClick = { viewModel.setPage(currentPage + 1) },
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6CB1DA))
        ) { Text("Sau") }
    }
}