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
import com.example.smartlibrary.ui.viewmodel.AddCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    viewModel: AddCategoryViewModel,
    onSaved: () -> Unit
) {
    val parentName by viewModel.parentName.collectAsState()
    val childNames by viewModel.childNames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onSaved()
        }
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
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                        Text("Lưu danh mục", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Thêm danh mục mới",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF062D76)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Danh mục cha
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
                        placeholder = { Text("Nhập tên danh mục cha", fontSize = 14.sp) },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Danh mục con
            Text(
                text = "Danh mục con",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF062D76)
            )
            Spacer(modifier = Modifier.height(8.dp))

            childNames.forEachIndexed { index, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.onChildNameChange(index, it) },
                        placeholder = { Text("Tên danh mục con ${index + 1}", fontSize = 14.sp) },
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
                        onClick = { viewModel.removeChild(index) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.addChild() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF062D76))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thêm danh mục con", color = Color(0xFF062D76), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
