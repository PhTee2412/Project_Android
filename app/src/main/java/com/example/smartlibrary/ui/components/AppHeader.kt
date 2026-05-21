package com.example.smartlibrary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.smartlibrary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    isLoggedIn: Boolean,
    cartCount: Int,
    notificationCount: Int,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onLoginClick: () -> Unit,
    onChatClick: () -> Unit,
    onBorrowedCardsClick: () -> Unit = {},
    onFineClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onQRCodeClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.height(40.dp)
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
            }

            if (isLoggedIn) {
                IconButton(onClick = onChatClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Message,
                        contentDescription = "ChatBot",
                        tint = Color.Gray
                    )
                }

                IconButton(onClick = onCartClick) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    modifier = Modifier.offset(x = 0.dp, y = -1.dp)
                                ) { Text(cartCount.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Giỏ hàng")
                    }
                }

                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    modifier = Modifier.offset(x = 0.dp, y = -1.dp)
                                ) { Text(notificationCount.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo")
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Tài khoản")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(0.dp, 0.dp),
                        modifier = Modifier.width(180.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hồ sơ của bạn") },
                            onClick = {
                                menuExpanded = false
                                onProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Phiếu mượn") },
                            onClick = {
                                menuExpanded = false
                                onBorrowedCardsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Phiếu phạt") },
                            onClick = {
                                menuExpanded = false
                                onFineClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đổi mật khẩu") },
                            onClick = {
                                menuExpanded = false
                                onChangePasswordClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mã QR của tôi") },
                            onClick = {
                                menuExpanded = false
                                onQRCodeClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đăng xuất") },
                            onClick = {
                                menuExpanded = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            } else {
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
        ),
        // Sử dụng default windowInsets để Scaffold cha tính toán chính xác
        windowInsets = TopAppBarDefaults.windowInsets
    )
}
