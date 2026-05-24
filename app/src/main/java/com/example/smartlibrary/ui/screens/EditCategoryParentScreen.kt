package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.ui.viewmodel.EditCategoryParentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryParentScreen(
    viewModel: EditCategoryParentViewModel,
    onNavigateToChild: (String) -> Unit,
    onBack: () -> Unit
) {
    val parentName by viewModel.parentName.collectAsState()
    val children by viewModel.children.collectAsState()
    val newChildName by viewModel.newChildName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            onBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục cha này và toàn bộ danh mục con của nó không?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteParent(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Xóa", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FD),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 12.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD66766)),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xóa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.updateParent() },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9CE5F4)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Chỉnh sửa danh mục cha",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF062D76)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tên danh mục cha *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { viewModel.onParentNameChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6CB1DA),
                            unfocusedBorderColor = Color(0xFFEEEEEE)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Danh mục con (${children.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF062D76)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Thêm danh mục con mới
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newChildName,
                    onValueChange = { viewModel.onNewChildNameChange(it) },
                    placeholder = { Text("Thêm danh mục con mới...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6CB1DA),
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.addChild() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF9CE5F4), contentColor = Color.White),
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            children.forEach { child ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = child.name,
                            fontSize = 15.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = { onNavigateToChild(child.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF6CB1DA))
                            }
                            IconButton(onClick = { viewModel.deleteChild(child.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFD66766))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
