package com.example.smartlibrary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.QrCodeScanner
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
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
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
                route = "admin_borrow_list",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_borrow_list") }
            )
            AdminBottomBarItem(
                icon = Icons.Filled.Warning,
                label = "Phiếu phạt",
                route = "admin_fine_list",
                currentRoute = currentRoute,
                onClick = { onNavigate("admin_fine_list") }
            )
            AdminBottomBarItem(
                icon = Icons.Filled.QrCodeScanner,
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
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isActive) Color(0xFF062D76) else Color.Gray
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) Color(0xFF062D76) else Color.Gray
        )
    }
}
