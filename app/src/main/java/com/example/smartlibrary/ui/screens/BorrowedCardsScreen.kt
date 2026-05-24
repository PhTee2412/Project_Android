package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.smartlibrary.network.BorrowCardResponse
import com.example.smartlibrary.ui.viewmodel.BorrowedCardsViewModel
import com.example.smartlibrary.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowedCardsScreen(
    viewModel: BorrowedCardsViewModel,
    onCardClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val filteredCards by viewModel.filteredCards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBorrowCards()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phiếu mượn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFE6EAF1)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Đã yêu cầu",
                    icon = Icons.Default.ListAlt,
                    isSelected = selectedTab == "Đã yêu cầu",
                    onClick = { viewModel.onTabSelected("Đã yêu cầu") },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Đang mượn",
                    icon = Icons.Default.Book,
                    isSelected = selectedTab == "Đang mượn",
                    onClick = { viewModel.onTabSelected("Đang mượn") },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Hết hạn",
                    icon = Icons.Default.Timer,
                    isSelected = selectedTab == "Hết hạn",
                    onClick = { viewModel.onTabSelected("Hết hạn") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isLoading && filteredCards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF062D76))
                }
            } else if (filteredCards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Không có phiếu mượn nào ở trạng thái này.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCards, key = { it.id }) { card ->
                        BorrowCardItem(card = card, selectedTab = selectedTab, onCardClick = onCardClick)
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF9CE5F4) else Color(0xFFD1D5DB),
        contentColor = if (isSelected) Color(0xFF062D76) else Color(0xFF131313),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BorrowCardItem(
    card: BorrowCardResponse,
    selectedTab: String,
    onCardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(card.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ID: ${card.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = "Ngày mượn: ${formatDate(card.borrowDate)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )

                // Hiển thị ngày tương ứng theo tab, khớp với Next.js
                when (selectedTab) {
                    "Đang mượn" -> {
                        Text(
                            text = "Ngày trả dự kiến: ${formatDate(card.dueDate)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                    "Hết hạn" -> {
                        Text(
                            text = "Ngày trả: ${formatDate(card.dueDate)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                    else -> { // "Đã yêu cầu"
                        Text(
                            text = "Hạn lấy sách: ${formatDate(card.getBookDate)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }
            }

            Button(
                onClick = { onCardClick(card.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9CE5F4),
                    contentColor = Color(0xFF062D76)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xem chi tiết", fontSize = 12.sp)
            }
        }
    }
}