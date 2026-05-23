package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartlibrary.ui.components.AdminHeader
import com.example.smartlibrary.ui.components.AdminBottomBar

@Composable
fun AdminMainScreen(navController: NavController) {
    val adminNavController = rememberNavController()
    val currentBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            AdminHeader(
                onNavigate = { route -> adminNavController.navigate(route) },
                onLogout = {
                    // Xóa session admin, quay về màn hình login user
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = {
            AdminBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route -> adminNavController.navigate(route) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AdminNavHost(adminNavController)
        }
    }
}

@Composable
fun AdminNavHost(adminNavController: NavHostController) {
    NavHost(
        navController = adminNavController,
        startDestination = "admin_dashboard"
    ) {
        composable("admin_dashboard") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Admin Dashboard - Đang phát triển")
            }
        }
        composable("admin_books") { PlaceholderContent("Quản lý sách") }
        composable("admin_users") { PlaceholderContent("Quản lý người dùng") }
        composable("admin_borrow_fines") { PlaceholderContent("Quản lý mượn/trả") }
        composable("admin_fines") { PlaceholderContent("Quản lý phiếu phạt") }
        composable("admin_settings") { PlaceholderContent("Quản lý cài đặt") }
        composable("admin_scan") { PlaceholderContent("Quét sách") }
    }
}

@Composable
private fun PlaceholderContent(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Màn hình $name", style = MaterialTheme.typography.headlineMedium)
    }
}
