package com.example.smartlibrary.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartlibrary.network.CategoryResponse
import com.example.smartlibrary.ui.viewmodel.CategoryListViewModel

@Composable
fun CategoryListScreen(
    viewModel: CategoryListViewModel,
    onNavigate: (String) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Tự động tải lại dữ liệu khi màn hình được hiển thị (đảm bảo cập nhật mới nhất)
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FD),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("admin_add_category") },
                containerColor = Color(0xFF9CE5F4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Giảm khoảng cách phía trên để tiêu đề sát header hơn
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 Danh mục sách",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF062D76)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading && categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6CB1DA))
                }
            } else if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có danh mục nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { category ->
                        CategoryParentItem(
                            category = category,
                            onParentClick = { onNavigate("admin_edit_category_parent/${category.id}") },
                            onChildClick = { childId -> onNavigate("admin_edit_category_child/$childId") },
                            onDeleteChild = { childId -> viewModel.deleteChild(childId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun CategoryParentItem(
    category: CategoryResponse,
    onParentClick: () -> Unit,
    onChildClick: (String) -> Unit,
    onDeleteChild: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF9CE5F4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF062D76)
                    )
                }
                
                IconButton(onClick = onParentClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa cha", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 28.dp, top = 8.dp)) {
                    category.children?.forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${child.name}",
                                fontSize = 15.sp,
                                color = Color(0xFF555555),
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(onClick = { onChildClick(child.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa con", tint = Color(0xFF6CB1DA), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteChild(child.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa con", tint = Color(0xFFD66766), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    if (category.children.isNullOrEmpty()) {
                        Text("Không có danh mục con", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
