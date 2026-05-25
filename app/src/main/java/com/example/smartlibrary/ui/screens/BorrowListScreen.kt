package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.ui.viewmodel.BorrowListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BorrowListScreen(
    viewModel: BorrowListViewModel,
    onNavigate: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val message by viewModel.message.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    
    // Quan sát dữ liệu đã được lọc và phân trang từ ViewModel
    val filteredCards by viewModel.paginatedCards.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tab Buttons (Thêm Icon vào cả 3 tab)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val tabs = listOf("Đã yêu cầu", "Đang mượn", "Đã trả")
                        tabs.forEach { tab ->
                            val isActive = selectedTab == tab
                            Button(
                                onClick = { viewModel.onTabSelected(tab) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) Color(0xFF6CB1DA) else Color(0xFF9CE5F4).copy(alpha = 0.4f),
                                    contentColor = if (isActive) Color.White else Color(0xFF6CB1DA)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = when(tab) {
                                        "Đã yêu cầu" -> Icons.Default.PendingActions
                                        "Đang mượn" -> Icons.Default.Book
                                        else -> Icons.Default.TimerOff
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = tab, 
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, 
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Search Bar (Cố định chiều cao 48dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            placeholder = { Text("Tìm kiếm ID, UserID...", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF6CB1DA)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Xóa", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F4F9),
                                unfocusedContainerColor = Color(0xFFF1F4F9),
                                focusedBorderColor = Color(0xFF6CB1DA),
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )

                        // Action Buttons
                        if (selectedTab == "Đã yêu cầu" || selectedTab == "Đang mượn") {
                            IconButton(
                                onClick = { 
                                    if (selectedTab == "Đã yêu cầu") viewModel.markExpired() 
                                    else viewModel.askToReturn() 
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Red.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == "Đã yêu cầu") Icons.Default.Block else Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onNavigate("admin_add_borrow") },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6CB1DA))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // List Content
            if (isLoading && filteredCards.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6CB1DA))
                }
            } else if (filteredCards.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History, 
                            contentDescription = null, 
                            modifier = Modifier.size(80.dp), 
                            tint = Color.LightGray.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == "Đã trả") "Chưa có phiếu trả nào." else "Không có kết quả.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards, key = { it.id }) { card ->
                        BorrowCardItem(
                            card = card,
                            selectedTab = selectedTab,
                            onClickDetail = { onNavigate("admin_borrow_detail/${card.id}") }
                        )
                    }
                }

                // Pagination
                if (totalPages > 1) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { if (currentPage > 1) viewModel.onPageChange(currentPage - 1) },
                                enabled = currentPage > 1,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Trước", fontSize = 12.sp)
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Surface(
                                color = Color(0xFFF1F4F9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Trang $currentPage / $totalPages",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6CB1DA),
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Button(
                                onClick = { if (currentPage < totalPages) viewModel.onPageChange(currentPage + 1) },
                                enabled = currentPage < totalPages,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sau", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun BorrowCardItem(
    card: BorrowCardResponse,
    selectedTab: String,
    onClickDetail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClickDetail() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Mã Phiếu: ${card.id}", fontWeight = FontWeight.Bold, color = Color(0xFF6CB1DA), fontSize = 17.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Người dùng ID: ", fontSize = 14.sp, color = Color.Gray)
                    Text(text = card.userId.toString(), fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Text(text = "Ngày mượn: ${formatDate(card.borrowDate)}", fontSize = 13.sp, color = Color.Gray)

                val statusLabel = when (selectedTab) {
                    "Đang mượn" -> "Hạn trả dự kiến: "
                    "Đã trả" -> "Ngày trả thực tế: "
                    "Đã yêu cầu" -> "Hạn lấy sách: "
                    else -> ""
                }
                val dateValue = when (selectedTab) {
                    "Đang mượn" -> card.dueDate
                    "Đã trả" -> card.dueDate ?: card.getBookDate
                    "Đã yêu cầu" -> card.getBookDate
                    else -> ""
                }

                if (statusLabel.isNotEmpty()) {
                    Text(
                        text = "$statusLabel${formatDate(dateValue)}",
                        fontSize = 13.sp,
                        color = if (selectedTab == "Đã trả") Color(0xFF2E7D32) else if (selectedTab == "Đang mượn") Color(0xFFE65100) else Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Button(
                onClick = onClickDetail,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chi tiết", fontSize = 13.sp)
            }
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val cleanDate = dateString.substringBefore("T")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val date = inputFormat.parse(cleanDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}
