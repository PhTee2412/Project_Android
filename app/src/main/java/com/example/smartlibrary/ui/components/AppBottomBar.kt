package com.example.smartlibrary.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        val items = listOf(
            Triple("Trang chủ", Icons.Default.Home, "home"),
            Triple("Thể loại", Icons.Default.Book, "categories"),
            Triple("Giới thiệu", Icons.Default.Info, "about"),
            Triple("Tin sách", Icons.Default.Article, "news")
        )

        items.forEach { (name, icon, route) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = name) },
                label = { Text(name, fontSize = 10.sp) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }
    }
}
