package com.example.smartlibrary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
    BottomAppBar(
        containerColor = Color.White,
        contentColor = Color(0xFF062D76)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        modifier = Modifier.padding(8.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF6CB1DA) else Color.Gray
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) Color(0xFF6CB1DA) else Color.Gray
        )
    }
}