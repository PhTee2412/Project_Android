package com.example.smartlibrary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smartlibrary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHeader(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.height(40.dp)
            )
        },
        actions = {
            IconButton(onClick = { onNavigate("admin_users") }) {
                Icon(Icons.Filled.Person, contentDescription = "Người dùng")
            }
            IconButton(onClick = { onNavigate("admin_books") }) {
                Icon(Icons.Filled.Book, contentDescription = "Sách")
            }
            IconButton(onClick = { onNavigate("admin_settings") }) {
                Icon(Icons.Filled.Settings, contentDescription = "Cài đặt")
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Filled.Logout, contentDescription = "Đăng xuất")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}
