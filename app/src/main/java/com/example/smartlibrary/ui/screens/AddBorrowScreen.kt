package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.smartlibrary.network.BookResponse
import com.example.smartlibrary.ui.viewmodel.AddBorrowViewModel

@Composable
fun AddBorrowScreen(
    viewModel: AddBorrowViewModel,
    onSaved: () -> Unit
) {
    val userIdInput by viewModel.userIdInput.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val bookList by viewModel.bookList.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val borrowList by viewModel.borrowList.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val onSuccess by viewModel.onSuccess.collectAsState()

    var isBookDropdownOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(onSuccess) {
        if (onSuccess) {
            onSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // User Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column ID Người Dùng
                Column(modifier = Modifier.weight(0.6f)) {
                    Text("ID Người Dùng", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userIdInput,
                            onValueChange = { viewModel.onUserIdInputChange(it) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            placeholder = { Text("Nhập ID...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF9CE5F4),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Button(
                            onClick = { viewModel.findUser() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9CE5F4),
                                contentColor = Color(0xFF062D76)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Tìm", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Column Tên Người Dùng
                Column(modifier = Modifier.weight(0.4f)) {
                    Text("Tên Người Dùng", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = selectedUser?.username ?: "Chưa chọn",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (selectedUser != null) Color(0xFFE8F5E9) else Color(0xFFF0F0F0),
                            unfocusedContainerColor = if (selectedUser != null) Color(0xFFE8F5E9) else Color(0xFFF0F0F0),
                            disabledContainerColor = Color(0xFFF0F0F0),
                            focusedBorderColor = if (selectedUser != null) Color(0xFF4CAF50) else Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = if (selectedUser != null) Color(0xFF2E7D32) else Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book Selection
            Text("Chọn Sách", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { isBookDropdownOpen = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBook?.let { "${it.maSach} - ${it.tenSach}" } ?: "Nhấn để chọn sách",
                                color = if (selectedBook != null) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF062D76))
                        }
                    }

                    DropdownMenu(
                        expanded = isBookDropdownOpen,
                        onDismissRequest = { isBookDropdownOpen = false },
                        modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 300.dp).background(Color.White)
                    ) {
                        bookList.forEach { book ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${book.maSach} - ${book.tenSach}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(book.tenTacGia ?: "N/A", fontSize = 12.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    viewModel.onBookSelected(book)
                                    isBookDropdownOpen = false
                                }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { viewModel.addBook() },
                    modifier = Modifier.size(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9CE5F4),
                        contentColor = Color(0xFF062D76)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm vào danh sách")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Borrow List Header
            Text(
                text = "Danh sách sách mượn (${borrowList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF062D76),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Borrow List Content
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (borrowList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có sách nào", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(borrowList) { book ->
                            BorrowedBookGridItem(book = book, onRemove = { viewModel.removeBook(book) })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Button
            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9CE5F4),
                    contentColor = Color(0xFF062D76)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color(0xFF062D76), modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hoàn Tất", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isLoading && !isSubmitting) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF062D76))
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun BorrowedBookGridItem(book: BookResponse, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = book.hinhAnh?.firstOrNull() ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                book.tenSach,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                book.tenTacGia ?: "N/A",
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "ID: ${book.maSach}",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
