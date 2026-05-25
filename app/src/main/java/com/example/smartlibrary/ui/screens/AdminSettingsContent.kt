package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.ui.viewmodel.AdminSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsContent(viewModel: AdminSettingsViewModel) {
    val finePerDay by viewModel.finePerDay.collectAsState()
    val waitingToTake by viewModel.waitingToTake.collectAsState()
    val borrowDay by viewModel.borrowDay.collectAsState()
    val startToMail by viewModel.startToMail.collectAsState()
    val maxBorrowedBooks by viewModel.maxBorrowedBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val adminPrimary = Color(0xFF6CB1DA)
    val adminBackground = Color(0xFFF4F7FD)

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = adminBackground,
        bottomBar = {
            // Nút Hoàn tất cố định ở dưới
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = adminPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "HOÀN TẤT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingItem(
                label = "Số lượt sách được mượn tối đa",
                value = maxBorrowedBooks.toString(),
                onValueChange = { viewModel.onMaxBorrowedBooksChange(it) },
                placeholder = "Nhập số sách được mượn tối đa"
            )

            SettingItem(
                label = "Số tiền phạt/ngày (VNĐ)",
                value = finePerDay.toString(),
                onValueChange = { viewModel.onFinePerDayChange(it) },
                placeholder = "Nhập số tiền phạt"
            )

            SettingItem(
                label = "Số ngày chờ nhận sách tối đa",
                value = waitingToTake.toString(),
                onValueChange = { viewModel.onWaitingToTakeChange(it) },
                placeholder = "Nhập số ngày chờ"
            )

            SettingItem(
                label = "Số ngày mượn sách tối đa",
                value = borrowDay.toString(),
                onValueChange = { viewModel.onBorrowDayChange(it) },
                placeholder = "Nhập số ngày mượn"
            )

            SettingItem(
                label = "Số ngày bắt đầu gửi email nhắc nhở",
                value = startToMail.toString(),
                onValueChange = { viewModel.onStartToMailChange(it) },
                placeholder = "Nhập số ngày"
            )
            
            // Khoảng trống cuối để không bị che bởi bottom bar
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = if (value == "0") "" else value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF6CB1DA),
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}
