package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.ui.components.BookCard
import com.example.smartlibrary.ui.viewmodel.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBookClick: (String) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val children by viewModel.categoryChildren.collectAsState()
    val books by viewModel.books.collectAsState()
    val selectedParentId by viewModel.selectedParentId.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // chỉ padding hai bên, bỏ padding dọc
    ) {
        // 1. Dropdown chọn danh mục cha
        Text(
            text = "Danh mục chính",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedParentName = categories.find { it.id == selectedParentId }?.name ?: "Tất cả"
            OutlinedTextField(
                value = selectedParentName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Chọn danh mục") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tất cả") },
                    onClick = {
                        viewModel.selectParentCategory(null)
                        expanded = false
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(category.name)
                                if (category.soLuongDanhMuc != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${category.soLuongDanhMuc})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.selectParentCategory(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "ALL" to "Tất cả sách",
                "NEWEST" to "Mới nhất",
                "MOST_BORROWED" to "Mượn nhiều"
            )
            filters.forEach { (key, label) ->
                val isSelected = activeFilter == key
                Button(
                    onClick = { viewModel.setFilter(key) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF30c9e8) else Color(0xFF9ce5f4),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Danh mục con (nếu có) – hiển thị dạng chip có thể cuộn ngang
        if (children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip "Tất cả" để bỏ chọn danh mục con
                FilterChip(
                    selected = selectedChildId == null,
                    onClick = { viewModel.selectChildCategory(null) },
                    label = { Text("Tất cả") },
                    shape = RoundedCornerShape(20.dp)
                )
                children.forEach { child ->
                    FilterChip(
                        selected = selectedChildId == child.id,
                        onClick = { viewModel.selectChildCategory(child.id) },
                        label = { Text(child.name) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Tiêu đề
        val currentTitle = when {
            selectedChildId != null -> children.find { it.id == selectedChildId }?.name ?: ""
            selectedParentId != null -> {
                val parentName = categories.find { it.id == selectedParentId }?.name ?: ""
                "Tất cả sách của $parentName"
            }
            else -> "Tất cả sách"
        }

        Text(
            text = currentTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 5. Danh sách sách (không contentPadding dư thừa)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = Color(0xFF30C9E8))
                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                books.isEmpty() -> Text("Không có sách nào.", color = Color.Gray)
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    // Bỏ contentPadding để sát với đáy
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(books) { book ->
                        BookCard(book = book, onClick = onBookClick)
                    }
                }
            }
        }
    }
}