package com.example.smartlibrary.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.ui.viewmodel.UserQRCodeViewModel
import qrcode.QRCode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserQRCodeScreen(
    viewModel: UserQRCodeViewModel,
    onBack: () -> Unit
) {
    val userInfo by viewModel.userInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mã QR của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF062D76))
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "Đã có lỗi xảy ra", color = Color.Red)
            }
        } else {
            userInfo?.let { user ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sử dụng mã này để mượn/trả sách tại thư viện",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // User Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Thông tin cá nhân",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(label = "Họ tên:", value = user.fullname)
                            InfoRow(label = "Email:", value = user.email)
                            InfoRow(label = "ID:", value = user.id)
                            InfoRow(label = "Username:", value = user.username)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // QR Code
                    val qrBitmap = remember(user.id) {
                        try {
                            val qrImage = QRCode(user.id).render().nativeImage() as Bitmap
                            // Tạo bitmap mới với white background
                            val bitmapWithBg = Bitmap.createBitmap(qrImage.width, qrImage.height, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmapWithBg)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            canvas.drawBitmap(qrImage, 0f, 0f, null)
                            bitmapWithBg
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Không thể tạo QR", color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Download Button
                    Button(
                        onClick = {
                            qrBitmap?.let {
                                saveImageToGallery(context, it, "QR_${user.username}.png")
                            } ?: Toast.makeText(context, "Không có mã QR để tải", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tải xuống mã QR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Instructions Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hướng dẫn sử dụng:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InstructionLine("Xuất trình mã QR này cho thủ thư khi mượn sách")
                            InstructionLine("Mã QR sẽ được quét để xác nhận danh tính của bạn")
                            InstructionLine("Bạn có thể tải về mã QR để tiện sử dụng")
                            InstructionLine("Không chia sẻ mã QR này với người khác")
                        }
                    }
                }
            }
        }
    }
}

private fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        try {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            Toast.makeText(context, "Đã lưu mã QR vào thư viện ảnh", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi tải xuống: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    } ?: run {
        Toast.makeText(context, "Không thể tạo file để lưu", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(100.dp),
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun InstructionLine(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "• ", color = Color(0xFF0D47A1))
        Text(text = text, fontSize = 13.sp, color = Color(0xFF0D47A1))
    }
}