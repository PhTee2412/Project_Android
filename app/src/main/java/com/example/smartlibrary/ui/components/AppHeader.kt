package com.example.smartlibrary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    isLoggedIn: Boolean,
    cartCount: Int,
    notificationCount: Int,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Màu nền toàn bộ vùng header (bao gồm status bar)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "BOOK",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            },
            actions = {
                // Icon Tìm kiếm
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                }

                if (isLoggedIn) {
                    // Icon Chat
                    IconButton(onClick = onChatClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Message,
                            contentDescription = "ChatBot",
                            tint = Color.Gray
                        )
                    }

                    // Giỏ hàng
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge { Text(cartCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Giỏ hàng")
                        }
                    }

                    // Thông báo
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge { Text(notificationCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onNotificationClick) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo")
                        }
                    }

                    // Profile
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Tài khoản")
                    }
                } else {
                    TextButton(onClick = onLoginClick) {
                        Text(
                            text = "Đăng nhập / Đăng ký",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent, // nền trong suốt để lộ màu của Box bên ngoài
                titleContentColor = Color.Black,
            ),
            modifier = Modifier
                .statusBarsPadding() // Đẩy nội dung xuống dưới thanh trạng thái
                .height(56.dp)
        )
    }
}