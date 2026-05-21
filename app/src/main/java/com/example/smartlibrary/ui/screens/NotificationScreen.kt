package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlibrary.network.NotificationItem
import com.example.smartlibrary.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    userId: String
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadNotifications()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Sử dụng Box thay cho Scaffold để tránh lỗi padding lồng nhau
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && notifications.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (notifications.isEmpty()) {
            Text(
                text = "Bạn không có thông báo nào.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = {
                            if (!item.isRead) {
                                viewModel.markAsRead(item.id)
                            }
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val backgroundColor = if (item.isRead) Color.White else Color(0xFFE3F2FD)
    val alpha = if (item.isRead) 0.7f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.isRead) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = Color.Red)
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isRead) Color.Gray else Color(0xFF062D76)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: String): String {
    if (timestamp.isBlank()) return ""
    return try {
        val inputFormat = if (timestamp.contains("T")) {
            val pattern = if (timestamp.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
            SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = if (timestamp.endsWith("Z")) TimeZone.getTimeZone("UTC") else TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            }
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            }
        }

        val date = inputFormat.parse(timestamp.removeSuffix("Z"))
        val outputFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        timestamp
    }
}
