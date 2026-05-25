package com.example.smartlibrary.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.ui.viewmodel.AdminScanViewModel
import com.example.smartlibrary.ui.viewmodel.BookWithStatus
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun AdminScanContent(viewModel: AdminScanViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showScanner by remember { mutableStateOf(false) }
    var scanType by remember { mutableStateOf("user") } // "user" or "book"

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FD))
    ) {
        if (state.selectedUser == null) {
            // Initial Screen: Find User
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF6CB1DA)
                )
                Text(
                    text = "Smart Library Admin",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6CB1DA)
                )
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = "Vui lòng nhập ID người dùng",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.userIdInput,
                    onValueChange = { viewModel.onUserIdInputChange(it) },
                    placeholder = { Text("Nhập User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6CB1DA),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.findUser() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Tìm kiếm")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.findUser() })
                )
                
                Spacer(Modifier.height(24.dp))
                Text("Hoặc", color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { 
                        scanType = "user"
                        showScanner = true 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Quét mã QR người dùng", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // User Dashboard Screen
            Column(modifier = Modifier.fillMaxSize()) {
                // User Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.selectedUser?.fullname ?: "N/A",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF062D76)
                            )
                            Text("ID: ${state.selectedUser?.id}", fontSize = 14.sp, color = Color.Gray)
                        }
                        Surface(
                            color = Color(0xFF9CE5F4).copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = state.selectedUser?.role ?: "",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color(0xFF6CB1DA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        UserInfoRow(Icons.Default.Email, state.selectedUser?.email ?: "N/A")
                        UserInfoRow(Icons.Default.Phone, state.selectedUser?.phone ?: "N/A")
                        UserInfoRow(Icons.Default.Cake, state.selectedUser?.birthdate ?: "N/A")
                    }
                }

                // Borrow Cards List (2 columns)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Left Column: Requested
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        SectionHeader(Icons.Default.PendingActions, "Đã yêu cầu")
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                            items(state.borrowCards.filter { it.status == "Đã yêu cầu" }) { card ->
                                BorrowCardItem(card) { viewModel.selectCard(card) }
                            }
                        }
                    }

                    // Right Column: Borrowing
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        SectionHeader(Icons.Default.MenuBook, "Đang mượn")
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                            // Filter out "Đã hết hạn" as requested
                            items(state.borrowCards.filter { it.status == "Đang mượn" }) { card ->
                                BorrowCardItem(card) { viewModel.selectCard(card) }
                            }
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Dialogs
        if (state.selectedCard != null) {
            BorrowDetailDialog(
                card = state.selectedCard!!,
                books = state.booksInCard,
                onDismiss = { viewModel.selectCard(null) },
                onScanClick = {
                    scanType = "book"
                    showScanner = true
                },
                onManualInput = { viewModel.onBarcodeScanned(it) },
                onSubmit = { viewModel.submitTransaction() },
                canSubmit = if (state.selectedCard?.status == "Đã yêu cầu") 
                    state.booksInCard.isNotEmpty() && state.booksInCard.all { it.checked }
                else 
                    state.booksInCard.any { it.checked }
            )
        }

        if (showScanner) {
            ScannerDialog(
                onScanSuccess = { barcode ->
                    showScanner = false
                    viewModel.onBarcodeScanned(barcode)
                },
                onDismiss = { showScanner = false },
                scanType = scanType
            )
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun UserInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF062D76))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF062D76))
    }
}

@Composable
fun BorrowCardItem(card: BorrowCardResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("ID: ${card.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Ngày: ${card.borrowDate?.take(10) ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Surface(
                color = when (card.status) {
                    "Đã yêu cầu" -> Color(0xFF6CB1DA).copy(alpha = 0.1f)
                    "Đang mượn" -> Color.Green.copy(alpha = 0.1f)
                    "Đã hết hạn" -> Color.Red.copy(alpha = 0.1f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = card.status ?: "N/A",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    color = when (card.status) {
                        "Đã yêu cầu" -> Color(0xFF6CB1DA)
                        "Đang mượn" -> Color(0xFF2E7D32)
                        "Đã hết hạn" -> Color.Red
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BorrowDetailDialog(
    card: BorrowCardResponse,
    books: List<BookWithStatus>,
    onDismiss: () -> Unit,
    onScanClick: () -> Unit,
    onManualInput: (String) -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean
) {
    var manualId by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF4F7FD)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.DarkGray)
                        }
                        Text(
                            text = "Chi tiết phiếu mượn #${card.id}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF062D76)
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Actions Card (Move to top)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualId,
                                    onValueChange = { manualId = it },
                                    placeholder = { Text("Nhập ID sách", fontSize = 14.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (manualId.isNotBlank()) {
                                            onManualInput(manualId)
                                            manualId = ""
                                        }
                                    }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6CB1DA)
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (manualId.isNotBlank()) {
                                            onManualInput(manualId)
                                            manualId = ""
                                        }
                                    },
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA))
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = onScanClick,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA))
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(12.dp))
                                Text("QUÉT BARCODE SÁCH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Books List Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Danh sách sách",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF062D76)
                            )
                        )
                        Surface(
                            color = Color(0xFF6CB1DA).copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${books.count { it.checked }}/${books.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6CB1DA)
                                )
                            )
                        }
                    }

                    // Book List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(books) { bookWithStatus ->
                            BookWithStatusItem(bookWithStatus)
                        }
                    }

                    // Bottom Finish Button
                    Button(
                        onClick = onSubmit,
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6CB1DA),
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            "HOÀN TẤT", 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 18.sp, 
                            color = if(canSubmit) Color.White else Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookWithStatusItem(item: BookWithStatus) {
    val isChecked = item.checked
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 0.dp else 2.dp),
        border = if (isChecked) BorderStroke(2.dp, Color(0xFF4CAF50)) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = item.book.hinhAnh?.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (isChecked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                    )
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.book.tenSach,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isChecked) Color(0xFF2E7D32) else Color(0xFF062D76),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tác giả: ${item.book.tenTacGia ?: "N/A"}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Vị trí: ${item.book.viTri ?: "N/A"}",
                    fontSize = 13.sp,
                    color = Color(0xFF6CB1DA),
                    fontWeight = FontWeight.Medium
                )
                if (item.childId != null) {
                    Text(
                        text = "Mã sách: ${item.childId}",
                        fontSize = 12.sp,
                        color = if (isChecked) Color(0xFF4CAF50) else Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerDialog(
    onScanSuccess: (String) -> Unit,
    onDismiss: () -> Unit,
    scanType: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = Executors.newSingleThreadExecutor()
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val scanner = BarcodeScanning.getClient(
                            BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                    if (scanType == "user") Barcode.FORMAT_QR_CODE else Barcode.FORMAT_ALL_FORMATS
                                )
                                .build()
                        )

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(scanner, imageProxy, onScanSuccess)
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("ScannerDialog", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (scanType == "user") 250.dp else 280.dp, if (scanType == "user") 250.dp else 180.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        if (scanType == "user") "Hướng Camera vào mã QR người dùng" else "Hướng Camera vào barcode sách",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onScanSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { 
                        onScanSuccess(it)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
