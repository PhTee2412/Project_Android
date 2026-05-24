package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
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
    
    val filteredCards = viewModel.getPaginatedCards()
    val totalPages = viewModel.getTotalPages()

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
            .background(Color(0xFFF4F7FD))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tab Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("Đã yêu cầu", "Đang mượn", "Đã trả")
                tabs.forEach { tab ->
                    val isActive = selectedTab == tab
                    Button(
                        onClick = { viewModel.onTabSelected(tab) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFF6CB1DA) else Color(0xFFE0E0E0),
                            contentColor = if (isActive) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(text = tab, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                }
            }

            // Search Bar and Square Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tìm kiếm...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )

                // Nút Gửi mail / Chặn (Dạng ô vuông)
                if (selectedTab == "Đã yêu cầu" || selectedTab == "Đang mượn") {
                    Button(
                        onClick = { 
                            if (selectedTab == "Đã yêu cầu") viewModel.markExpired() 
                            else viewModel.askToReturn() 
                        },
                        modifier = Modifier.size(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == "Đã yêu cầu") Icons.Default.Block else Icons.Default.Mail,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                // Nút Tạo phiếu (Dạng ô vuông)
                Button(
                    onClick = { onNavigate("admin_add_borrow") },
                    modifier = Modifier.size(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }

            // Content
            if (isLoading && filteredCards.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF9CE5F4))
                }
            } else if (filteredCards.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Không có phiếu mượn nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards) { card ->
                        BorrowCardItem(
                            card = card,
                            selectedTab = selectedTab,
                            onClickDetail = { onNavigate("admin_borrow_detail/${card.id}") }
                        )
                    }
                }

                // Pagination
                if (totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) viewModel.onPageChange(currentPage - 1) },
                            enabled = currentPage > 1
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Trang trước")
                        }
                        
                        Text(
                            text = "Trang $currentPage / $totalPages",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { if (currentPage < totalPages) viewModel.onPageChange(currentPage + 1) },
                            enabled = currentPage < totalPages
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Trang sau")
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClickDetail() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ID: ${card.id}", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "User ID: ${card.userId}", fontSize = 14.sp, color = Color.Gray)
                }
                
                Button(
                    onClick = onClickDetail,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6CB1DA),
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Chi tiết", fontSize = 12.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)

            Text(
                text = "Ngày mượn: ${formatDate(card.borrowDate)}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            val dateLabel = when (selectedTab) {
                "Đang mượn" -> "Ngày trả dự kiến: "
                "Đã trả" -> if (card.dueDate != null) "Ngày trả: " else "Hạn lấy sách: "
                "Đã yêu cầu" -> "Hạn lấy sách: "
                else -> ""
            }

            val dateValue = when (selectedTab) {
                "Đang mượn" -> card.dueDate
                "Đã trả" -> card.dueDate ?: card.getBookDate
                "Đã yêu cầu" -> card.getBookDate
                else -> ""
            }

            if (dateLabel.isNotEmpty()) {
                Text(
                    text = "$dateLabel${formatDate(dateValue)}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}
