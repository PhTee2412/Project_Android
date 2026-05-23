package com.example.smartlibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartlibrary.network.RetrofitClient
import com.example.smartlibrary.ui.components.AdminBottomBar
import com.example.smartlibrary.ui.components.AdminHeader
import com.example.smartlibrary.ui.viewmodel.AdminBooksViewModel
import com.example.smartlibrary.ui.viewmodel.AdminDashboardViewModel

@Composable
fun AdminMainScreen(
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    val adminNavController = rememberNavController()
    val currentBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            AdminHeader(
                onNavigate = { route -> adminNavController.navigate(route) },
                onLogout = {
                    onLogout()
                    navController.navigate("home") {
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
            val dashboardViewModel: AdminDashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminDashboardViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminDashboardContent(viewModel = dashboardViewModel)
        }
        composable("admin_books") {
            val booksViewModel: AdminBooksViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminBooksViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminBooksContent(viewModel = booksViewModel)
        }
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