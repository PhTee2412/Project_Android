package com.example.smartlibrary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    // Sử dụng Surface để dễ dàng điều chỉnh chiều cao (height)
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Thêm padding để tránh bị thanh điều hướng hệ thống che
            .height(65.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminBottomBarItem(
                icon = Icons.Filled.Dashboard,
                label = "Dashboard",
                route = "admin_dashboard",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_dashboard") }
            )
            AdminBottomBarItem(
                icon = Icons.Filled.SwapHoriz,
                label = "Mượn/Trả",
                route = "admin_borrow_fines",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_borrow_fines") }
            )
            AdminBottomBarItem(
                icon = Icons.Filled.Warning,
                label = "Phiếu phạt",
                route = "admin_fines",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_fines") }
            )
            AdminBottomBarItem(
                icon = Icons.Filled.QrCode2,
                label = "Quét sách",
                route = "admin_scan",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_scan") }
            )
        }
    }
}

@Composable
private fun AdminBottomBarItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    onClick: () -> Unit
) {
    val isActive = currentRoute == route
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(4.dp) // Giảm padding để thu gọn khoảng cách
            .clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp), // Kích thước icon
            tint = if (isActive) Color(0xFF6CB1DA) else Color.Gray
        )
        Text(
            text = label,
            fontSize = 10.sp, // Giảm size chữ một chút cho gọn
            color = if (isActive) Color(0xFF6CB1DA) else Color.Gray
        )
    }
}
