package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.smartlibrary.network.FineResponse
import com.example.smartlibrary.ui.viewmodel.FineListViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun FineListContent(
    viewModel: FineListViewModel,
    onNavigate: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val paginatedFines by viewModel.paginatedFines.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val message by viewModel.message.collectAsState()
    
    val focusManager = LocalFocusManager.current
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Tabs and Search
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TabItem(
                            text = "Chưa thanh toán",
                            isActive = currentTab == "CHUA_THANH_TOAN",
                            onClick = { viewModel.onTabChange("CHUA_THANH_TOAN") },
                            modifier = Modifier.weight(1f)
                        )
                        TabItem(
                            text = "Đã thanh toán",
                            isActive = currentTab == "DA_THANH_TOAN",
                            onClick = { viewModel.onTabChange("DA_THANH_TOAN") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Search Bar
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
                        
                        Button(
                            onClick = { focusManager.clearFocus() },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // List
            if (isLoading && paginatedFines.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6CB1DA))
                }
            } else if (paginatedFines.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Không tìm thấy phiếu phạt nào.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(paginatedFines, key = { it.id }) { fine ->
                        FineListItem(
                            fine = fine,
                            onClickDetail = { onNavigate("admin_fine_detail/${fine.id}") }
                        )
                    }
                }

                // Pagination
                if (totalPages > 1) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentPage > 1) viewModel.onPageChange(currentPage - 1) },
                                enabled = currentPage > 1
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = if (currentPage > 1) Color(0xFF062D76) else Color.Gray)
                            }
                            
                            Text(
                                text = "Trang $currentPage / $totalPages",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF062D76)
                            )

                            IconButton(
                                onClick = { if (currentPage < totalPages) viewModel.onPageChange(currentPage + 1) },
                                enabled = currentPage < totalPages
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (currentPage < totalPages) Color(0xFF062D76) else Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // FAB
        if (currentTab == "CHUA_THANH_TOAN") {
            FloatingActionButton(
                onClick = { onNavigate("admin_add_fine") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Color(0xFF6CB1DA),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm phiếu phạt")
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun TabItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF6CB1DA) else Color(0xFF9CE5F4),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = if (text.contains("Chưa")) Icons.Default.Timer else Icons.Default.AttachMoney,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FineListItem(
    fine: FineResponse,
    onClickDetail: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mã Phiếu: ${fine.id}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF062D76),
                    fontSize = 14.sp
                )
                Text(
                    text = "User ID: ${fine.userId}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Số Tiền: ${currencyFormat.format(fine.soTien ?: 0.0)}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Nội Dung: ${fine.noiDung}",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }
            
            Button(
                onClick = onClickDetail,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Chi tiết", fontSize = 12.sp)
            }
        }
    }
}
