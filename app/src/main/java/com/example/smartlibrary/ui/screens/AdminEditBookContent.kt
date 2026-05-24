package com.example.smartlibrary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartlibrary.ui.viewmodel.AdminEditBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditBookContent(
    viewModel: AdminEditBookViewModel,
    onBookUpdated: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val message by viewModel.message.collectAsState()

    val bookName by viewModel.bookName.collectAsState()
    val author by viewModel.author.collectAsState()
    val publisher by viewModel.publisher.collectAsState()
    val year by viewModel.year.collectAsState()
    val quantityAdded by viewModel.quantityAdded.collectAsState()
    val description by viewModel.description.collectAsState()
    val weight by viewModel.weight.collectAsState()
    val price by viewModel.price.collectAsState()
    val status by viewModel.status.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val isDeleted = status == "DA_XOA"

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageUriChange(uri)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6CB1DA))
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { viewModel.submit(context, onBookUpdated) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CB1DA)),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDeleted) "Phục Hồi Sách" else "Hoàn Tất Chỉnh Sửa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF4F7FD))
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                if (isDeleted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sách này đang bị xóa. Chỉ có thể cập nhật số lượng để phục hồi.",
                                color = Color(0xFF856404),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Tên sách
                AdminTextField(
                    label = "Tên Sách",
                    value = bookName,
                    onValueChange = viewModel::onBookNameChange,
                    placeholder = "Nhập tên sách đầy đủ",
                    enabled = !isDeleted
                )

                // Tác giả
                AdminTextField(
                    label = "Tên Tác Giả",
                    value = author,
                    onValueChange = viewModel::onAuthorChange,
                    placeholder = "Nhập tên tác giả",
                    enabled = !isDeleted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminTextField(
                        label = "Năm XB",
                        value = year,
                        onValueChange = viewModel::onYearChange,
                        placeholder = "Năm",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        enabled = !isDeleted
                    )
                    AdminTextField(
                        label = "Nhà Xuất Bản",
                        value = publisher,
                        onValueChange = viewModel::onPublisherChange,
                        placeholder = "Tên NXB",
                        modifier = Modifier.weight(1.5f),
                        enabled = !isDeleted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminTextField(
                        label = "Số lượng thêm",
                        value = quantityAdded,
                        onValueChange = viewModel::onQuantityAddedChange,
                        placeholder = "Số lượng",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )

                    // Dropdown Thể loại chính
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Thể Loại Chính *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 2.dp,
                                color = if (isDeleted) Color(0xFFEEEEEE) else Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(enabled = !isDeleted) { expanded = true }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedCategory?.name ?: "Chọn Thể Loại",
                                        color = if (selectedCategory == null) Color.Gray else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.onCategorySelected(category)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Dropdown Thể loại phụ
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Thể Loại Phụ *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    val subCategories = selectedCategory?.children ?: emptyList()

                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp,
                            color = if (selectedCategory == null || isDeleted) Color(0xFFEEEEEE) else Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(enabled = selectedCategory != null && !isDeleted) { expanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedSubCategory?.name ?: "Chọn Thể Loại Phụ",
                                    color = if (selectedSubCategory == null) Color.Gray else Color.Black,
                                    fontSize = 14.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        if (subCategories.isNotEmpty()) {
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                subCategories.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            viewModel.onSubCategorySelected(sub)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminTextField(
                        label = "Trọng Lượng (gram)",
                        value = weight,
                        onValueChange = viewModel::onWeightChange,
                        placeholder = "Trọng lượng",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        enabled = !isDeleted
                    )
                    AdminTextField(
                        label = "Đơn Giá (VND)",
                        value = price,
                        onValueChange = viewModel::onPriceChange,
                        placeholder = "Đơn giá",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        enabled = !isDeleted
                    )
                }

                // Mô tả
                AdminTextField(
                    label = "Mô Tả",
                    value = description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Nhập mô tả chi tiết về sách...",
                    singleLine = false,
                    minLines = 4,
                    enabled = !isDeleted
                )

                // Hình ảnh
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Hình ảnh bìa sách", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.size(width = 200.dp, height = 280.dp),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val displayImage = imageUri ?: imageUrl
                            if (displayImage != null) {
                                AsyncImage(
                                    model = displayImage,
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Không có hình ảnh", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isDeleted) {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(42.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thay đổi ảnh bìa", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text("$label *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(50.dp) else Modifier.wrapContentHeight()),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            color = if (enabled) Color.White else Color(0xFFEEEEEE)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF6CB1DA),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF6CB1DA),
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    disabledTextColor = Color.DarkGray
                ),
                singleLine = singleLine,
                minLines = minLines,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                enabled = enabled
            )
        }
    }
}
