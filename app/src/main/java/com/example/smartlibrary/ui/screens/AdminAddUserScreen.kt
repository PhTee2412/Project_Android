package com.example.smartlibrary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.ui.viewmodel.AddUserStep
import com.example.smartlibrary.ui.viewmodel.AdminAddUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddUserScreen(
    viewModel: AdminAddUserViewModel,
    onUserCreated: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val onUserCreatedEvent by viewModel.onUserCreated.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(onUserCreatedEvent) {
        if (onUserCreatedEvent) {
            onUserCreated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF4F7FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = 0.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState == AddUserStep.OTP) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "StepTransition"
            ) { targetStep ->
                when (targetStep) {
                    AddUserStep.FORM -> UserFormStep(viewModel)
                    AddUserStep.OTP -> OtpStep(viewModel)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UserFormStep(viewModel: AdminAddUserViewModel) {
    val context = LocalContext.current
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val role by viewModel.role.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar Section
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .clickable { launcher.launch("image/*") }
                ) {
                    AsyncImage(
                        model = if (avatarUrl.isNotEmpty()) avatarUrl else "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isSubmitting && avatarUrl.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF9CE5F4)
                        )
                    }
                }
                TextButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF9CE5F4))
                    Spacer(Modifier.width(4.dp))
                    Text("Tải ảnh đại diện", color = Color(0xFF9CE5F4))
                }
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Tên người dùng") },
            modifier = Modifier.fillMaxWidth().height(62.dp),
            isError = errors.containsKey("username"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true, // Khắc phục lỗi Enter biến mất chữ
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9CE5F4),
                focusedLabelColor = Color(0xFF9CE5F4)
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().height(62.dp),
            isError = errors.containsKey("email"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true, // Khắc phục lỗi Enter biến mất chữ
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9CE5F4),
                focusedLabelColor = Color(0xFF9CE5F4)
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = { Text("Số điện thoại") },
                modifier = Modifier.weight(1f).height(62.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true, // Khắc phục lỗi Enter biến mất chữ
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9CE5F4),
                    focusedLabelColor = Color(0xFF9CE5F4)
                )
            )
            DatePickerField(
                label = "Ngày sinh",
                value = birthDate,
                onValueChange = { viewModel.onBirthDateChange(it) },
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SelectBox(
                label = "Vai trò",
                value = role,
                options = listOf("ADMIN", "USER", "STAFF"),
                onValueChange = { viewModel.onRoleChange(it) },
                modifier = Modifier.weight(1f).height(62.dp)
            )
            SelectBox(
                label = "Giới tính",
                value = gender,
                options = listOf("Nam", "Nữ", "Khác"),
                onValueChange = { viewModel.onGenderChange(it) },
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.createUser() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Tiếp tục", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun OtpStep(viewModel: AdminAddUserViewModel) {
    val otp by viewModel.otp.collectAsState()
    val tempEmail by viewModel.tempEmail.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFE3F2FD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF9CE5F4), modifier = Modifier.size(40.dp))
        }

        Text(
            "Chúng tôi đã gửi mã OTP gồm 6 chữ số đến email:\n$tempEmail",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
            label = { Text("Mã OTP") },
            modifier = Modifier.fillMaxWidth().height(62.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, letterSpacing = 8.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9CE5F4),
                focusedLabelColor = Color(0xFF9CE5F4)
            )
        )

        Button(
            onClick = { viewModel.verifyOtp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
            enabled = !isSubmitting && otp.length == 6
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Xác nhận & Tạo", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        TextButton(onClick = { viewModel.setStep(AddUserStep.FORM) }) {
            Text("Quay lại chỉnh sửa", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF9CE5F4))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9CE5F4),
                focusedLabelColor = Color(0xFF9CE5F4)
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = java.util.Date(it)
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        onValueChange(format.format(date))
                    }
                    showDialog = false
                }) { Text("Chọn", color = Color(0xFF9CE5F4)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SelectBox(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9CE5F4),
                focusedLabelColor = Color(0xFF9CE5F4)
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.45f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
