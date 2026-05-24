package com.example.smartlibrary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.ui.viewmodel.AdminEditUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditUserScreen(
    viewModel: AdminEditUserViewModel,
    userId: Int,
    onUserUpdated: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val role by viewModel.role.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val onUserUpdatedEvent by viewModel.onUserUpdated.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(context, it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(onUserUpdatedEvent) {
        if (onUserUpdatedEvent) {
            onUserUpdated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF4F7FD),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF9CE5F4))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), bottom = 0.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Đã xóa tiêu đề "Chỉnh Sửa Người Dùng"

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
                        value = userId.toString(),
                        onValueChange = {},
                        label = { Text("ID") },
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        enabled = false,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {},
                        label = { Text("Tên người dùng") },
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        enabled = false,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        enabled = false,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.onPhoneChange(it) },
                            label = { Text("Số điện thoại") },
                            modifier = Modifier.weight(1f).height(62.dp),
                            shape = RoundedCornerShape(10.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.updateUser() },
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
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Lưu thay đổi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
