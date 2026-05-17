package com.example.smartlibrary.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.ui.viewmodel.ProfileViewModel
import com.example.smartlibrary.util.formatDate
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val userInfo by viewModel.userInfo.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val otp by viewModel.otp.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Launcher chọn ảnh
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadAvatar(File(it.path ?: "avatar.jpg"))
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (isOtpSent) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOtp() },
            title = { Text("Xác thực OTP") },
            text = {
                Column {
                    Text("Vui lòng nhập mã OTP 6 chữ số đã được gửi đến email của bạn để xác nhận thay đổi.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
                        label = { Text("Mã OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.verifyOtp() },
                    enabled = otp.length == 6 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Xác nhận")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOtp() }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading && userInfo == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F8)) // Xanh nhạt
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            if (userInfo?.avatarUrl != null) {
                                AsyncImage(
                                    model = userInfo?.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userInfo?.fullName ?: "Chưa cập nhật",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userInfo?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { if (isEditing) viewModel.updateProfile() else viewModel.toggleEdit() },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9CE5F4)
                            )
                        ) {
                            Text(if (isEditing) "Lưu" else "Sửa")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Thông tin cá nhân
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Thông tin cá nhân",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color(0xFF1976D2),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoItem("Họ và Tên", userInfo?.fullName ?: "", isEditing) {
                            viewModel.onUserInfoChange(userInfo!!.copy(fullName = it))
                        }
                        
                        InfoItem("MSSV", userInfo?.studentId ?: "Chưa cập nhật", false) {}

                        InfoItem("Email", userInfo?.email ?: "Chưa cập nhật", isEditing) {
                            viewModel.onUserInfoChange(userInfo!!.copy(email = it))
                        }

                        // DatePicker cho Ngày sinh
                        if (isEditing) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text("Ngày Sinh", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                OutlinedTextField(
                                    value = userInfo?.birthdate ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                                    viewModel.onUserInfoChange(userInfo!!.copy(birthdate = date))
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }) {
                                            Icon(Icons.Default.CalendarMonth, null)
                                        }
                                    }
                                )
                            }
                        } else {
                            InfoItem("Ngày Sinh", formatDate(userInfo?.birthdate, "Chưa cập nhật"), false) {}
                        }

                        InfoItem("Số Điện Thoại", userInfo?.phone ?: "", isEditing) {
                            viewModel.onUserInfoChange(userInfo!!.copy(phone = it))
                        }

                        InfoItem("Ngày Tham Gia", formatDate(userInfo?.joinDate, "Chưa cập nhật"), false) {}

                        // Upload Avatar Section
                        if (isEditing) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Upload ảnh đại diện", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { launcher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Text("Choose File")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                
                if (isLoading && userInfo != null && !isOtpSent) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.Black.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = value.ifBlank { "Chưa cập nhật" },
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}
