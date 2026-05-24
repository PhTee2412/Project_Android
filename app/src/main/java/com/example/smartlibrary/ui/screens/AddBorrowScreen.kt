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
            // User Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ID Người Dùng", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = userIdInput,
                            onValueChange = { viewModel.onUserIdInputChange(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Nhập ID...") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.findUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF062D76)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Tìm")
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Tên Người Dùng", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedUser != null) Color(0xFFE8F5E9) else Color(0xFFE0E0E0))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = selectedUser?.username ?: "Chưa chọn người dùng",
                            color = if (selectedUser != null) Color(0xFF2E7D32) else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book Selection
            Text("Chọn Sách", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { isBookDropdownOpen = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
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
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF062D76))
                        }
                    }

                    DropdownMenu(
                        expanded = isBookDropdownOpen,
                        onDismissRequest = { isBookDropdownOpen = false },
                        modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 300.dp)
                    ) {
                        bookList.forEach { book ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${book.maSach} - ${book.tenSach}", fontWeight = FontWeight.Bold)
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
                
                Spacer(Modifier.width(8.dp))
                
                IconButton(
                    onClick = { viewModel.addBook() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF062D76))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm vào danh sách", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Borrow List
            Text(
                text = "Danh sách sách mượn (${borrowList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF062D76),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(borrowList) { book ->
                    BorrowedBookGridItem(book = book, onRemove = { viewModel.removeBook(book) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Button
            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF062D76)),
                shape = RoundedCornerShape(28.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hoàn Tất", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.hinhAnh?.firstOrNull() ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp, 90.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.tenSach, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(book.tenTacGia ?: "N/A", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Text("ID: ${book.maSach}", fontSize = 11.sp, color = Color.Gray)
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp).align(Alignment.End)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
