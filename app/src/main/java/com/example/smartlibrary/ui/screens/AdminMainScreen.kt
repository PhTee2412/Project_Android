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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartlibrary.network.RetrofitClient
import com.example.smartlibrary.ui.components.AdminBottomBar
import com.example.smartlibrary.ui.components.AdminHeader
import com.example.smartlibrary.ui.viewmodel.AdminAddBookViewModel
import com.example.smartlibrary.ui.viewmodel.AdminBookDetailViewModel
import com.example.smartlibrary.ui.viewmodel.AdminBooksViewModel
import com.example.smartlibrary.ui.viewmodel.AdminDashboardViewModel
import com.example.smartlibrary.ui.viewmodel.AdminEditBookViewModel

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
            // Chỉ hiển thị BottomBar ở các màn hình chính
            val mainRoutes = listOf("admin_dashboard", "admin_books", "admin_users", "admin_borrow_fines", "admin_fines", "admin_settings")
            if (currentRoute in mainRoutes) {
                AdminBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> adminNavController.navigate(route) }
                )
            }
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
            AdminDashboardContent(
                viewModel = dashboardViewModel,
                onBookClick = { bookId -> adminNavController.navigate("admin_book_detail/$bookId") }
            )
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
            AdminBooksContent(
                viewModel = booksViewModel,
                onBookClick = { bookId -> adminNavController.navigate("admin_book_detail/$bookId") },
                onAddBookClick = { adminNavController.navigate("admin_add_book") },
                onEditBookClick = { bookId -> adminNavController.navigate("admin_edit_book/$bookId") }
            )
        }
        composable(
            route = "admin_book_detail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            val detailViewModel: AdminBookDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminBookDetailViewModel(RetrofitClient.apiService, bookId) as T
                    }
                }
            )
            AdminBookDetailContent(
                viewModel = detailViewModel,
                onBack = { adminNavController.popBackStack() }
            )
        }
        composable("admin_add_book") {
            val addBookViewModel: AdminAddBookViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminAddBookViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminAddBookContent(
                viewModel = addBookViewModel,
                onBookAdded = { bookId -> 
                    adminNavController.navigate("admin_book_detail/$bookId") {
                        popUpTo("admin_add_book") { inclusive = true }
                    }
                },
                onBack = { adminNavController.popBackStack() }
            )
        }
        composable(
            route = "admin_edit_book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            val editViewModel: AdminEditBookViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminEditBookViewModel(RetrofitClient.apiService, bookId) as T
                    }
                }
            )
            AdminEditBookContent(
                viewModel = editViewModel,
                onBookUpdated = { id -> 
                    adminNavController.navigate("admin_book_detail/$id") {
                        popUpTo("admin_edit_book/{bookId}") { inclusive = true }
                    }
                },
                onBack = { adminNavController.popBackStack() }
            )
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
