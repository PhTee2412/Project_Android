package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.ui.viewmodel.AddFineViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFineScreen(
    viewModel: AddFineViewModel,
    onSaved: () -> Unit
) {
    val userText by viewModel.userText.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val money by viewModel.money.collectAsState()
    val reason by viewModel.reason.collectAsState()
    val borrowList by viewModel.borrowList.collectAsState()
    val selectedBorrow by viewModel.selectedBorrow.collectAsState()
    val bookText by viewModel.bookText.collectAsState()
    val bookFound by viewModel.bookFound.collectAsState()
    val otherContent by viewModel.otherContent.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val scrollState = rememberScrollState()
    var isBorrowMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1000)
            onSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF3FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp) // Space for footer
        ) {
            // Header Space (AdminHeader is above)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Chọn User
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ID Người Dùng", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userText,
                            onValueChange = { viewModel.onUserTextChange(it) },
                            placeholder = { Text("Nhập ID...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF6CB1DA)
                            )
                        )
                        Button(
                            onClick = { viewModel.findUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Tìm")
                        }
                    }
                    
                    // Hiển thị tên user nếu tìm thấy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedUser != null) Color(0xFFE8F5E9) else Color(0xFFE0E0E0))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = selectedUser?.fullname ?: selectedUser?.username ?: "Chưa chọn người dùng",
                            fontWeight = FontWeight.Medium,
                            color = if (selectedUser != null) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                }

                // 2. Số tiền
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Số Tiền (VNĐ)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = money,
                        onValueChange = { viewModel.onMoneyChange(it) },
                        placeholder = { Text("Nhập số tiền...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF6CB1DA)
                        )
                    )
                }

                // 3. Nội dung vi phạm
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nội Dung Vi Phạm", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                    // Radio Group
                    ReasonOption(
                        label = "Trả sách trễ hạn",
                        selected = reason == "Trả sách trễ hạn",
                        onClick = { viewModel.onReasonChange("Trả sách trễ hạn") }
                    )
                    
                    if (reason == "Trả sách trễ hạn") {
                        Box(modifier = Modifier.padding(start = 32.dp).fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = isBorrowMenuExpanded,
                                onExpandedChange = { if (selectedUser != null) isBorrowMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = if (selectedBorrow != null) "Phiếu #${selectedBorrow?.id}" else "Chọn Phiếu Mượn",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBorrowMenuExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = isBorrowMenuExpanded,
                                    onDismissRequest = { isBorrowMenuExpanded = false }
                                ) {
                                    if (borrowList.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Không có phiếu mượn nào") },
                                            onClick = { isBorrowMenuExpanded = false }
                                        )
                                    } else {
                                        borrowList.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text("Phiếu #${item.id} - ${item.borrowDate}") },
                                                onClick = {
                                                    viewModel.onBorrowSelected(item)
                                                    isBorrowMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ReasonOption(
                        label = "Làm mất sách",
                        selected = reason == "Làm mất sách",
                        onClick = { viewModel.onReasonChange("Làm mất sách") }
                    )

                    if (reason == "Làm mất sách") {
                        Column(modifier = Modifier.padding(start = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = bookText,
                                    onValueChange = { viewModel.onBookTextChange(it) },
                                    placeholder = { Text("ID sách...") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Button(
                                    onClick = { viewModel.findBook() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp),
                                    enabled = !isSubmitting
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                    } else {
                                        Text("Kiểm tra")
                                    }
                                }
                            }
                            if (bookFound) {
                                Text(
                                    text = "✓ Đã xác nhận sách",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    ReasonOption(
                        label = "Khác",
                        selected = reason == "Khác",
                        onClick = { viewModel.onReasonChange("Khác") }
                    )

                    if (reason == "Khác") {
                        OutlinedTextField(
                            value = otherContent,
                            onValueChange = { viewModel.onOtherContentChange(it) },
                            placeholder = { Text("Nhập nội dung chi tiết...") },
                            modifier = Modifier.padding(start = 32.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            minLines = 3
                        )
                    }
                }
            }
        }

        // Footer Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { viewModel.submit() },
                    modifier = Modifier
                        .height(50.dp)
                        .width(160.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Hoàn Tất", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Message Snackbar simulation or simple toast
        if (message != null) {
            LaunchedEffect(message) {
                // Here you would show a snackbar
                delay(3000)
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
fun ReasonOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF062D76))
        )
        Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}
