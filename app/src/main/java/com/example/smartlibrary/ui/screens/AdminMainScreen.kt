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
import com.example.smartlibrary.ui.viewmodel.*

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
            // Cập nhật danh sách các route hiển thị BottomBar, bao gồm admin_fine_list
            val mainRoutes = listOf("admin_dashboard", "admin_books", "admin_users", "admin_borrow_list", "admin_fine_list", "admin_settings")
            if (currentRoute in mainRoutes) {
                AdminBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> adminNavController.navigate(route) }
                )
            }
        }
    ) { innerPadding ->
        // Sử dụng innerPadding để nội dung không bị đè bởi TopBar/BottomBar
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
                onEditBookClick = { bookId -> adminNavController.navigate("admin_edit_book/$bookId") },
                onCategoryManageClick = { adminNavController.navigate("admin_categories") }
            )
        }
        
        // --- Category Management Routes ---
        composable("admin_categories") {
            val catViewModel: CategoryListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CategoryListViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            CategoryListScreen(
                viewModel = catViewModel,
                onNavigate = { route -> adminNavController.navigate(route) }
            )
        }
        composable("admin_add_category") {
            val addCatViewModel: AddCategoryViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AddCategoryViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AddCategoryScreen(
                viewModel = addCatViewModel,
                onSaved = { adminNavController.popBackStack() }
            )
        }
        composable(
            route = "admin_edit_category_parent/{parentId}",
            arguments = listOf(navArgument("parentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getString("parentId") ?: return@composable
            val editParentVM: EditCategoryParentViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EditCategoryParentViewModel(RetrofitClient.apiService, parentId) as T
                    }
                }
            )
            EditCategoryParentScreen(
                viewModel = editParentVM,
                onNavigateToChild = { childId -> adminNavController.navigate("admin_edit_category_child/$childId") },
                onBack = { adminNavController.popBackStack() }
            )
        }
        composable(
            route = "admin_edit_category_child/{childId}",
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            val editChildVM: EditCategoryChildViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EditCategoryChildViewModel(RetrofitClient.apiService, childId) as T
                    }
                }
            )
            EditCategoryChildScreen(
                viewModel = editChildVM,
                onBack = { adminNavController.popBackStack() }
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

        // --- User Management Routes ---
        composable("admin_users") {
            val userListVM: AdminUserListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminUserListViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminUserListScreen(
                viewModel = userListVM,
                onNavigate = { route -> adminNavController.navigate(route) }
            )
        }
        composable("admin_add_user") {
            val addUserVM: AdminAddUserViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminAddUserViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminAddUserScreen(
                viewModel = addUserVM,
                onUserCreated = { adminNavController.popBackStack() }
            )
        }
        composable(
            route = "admin_edit_user/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            val editUserVM: AdminEditUserViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminEditUserViewModel(RetrofitClient.apiService, userId) as T
                    }
                }
            )
            AdminEditUserScreen(
                viewModel = editUserVM,
                userId = userId,
                onUserUpdated = { adminNavController.popBackStack() }
            )
        }

        // --- Borrow Management Routes ---
        composable("admin_borrow_list") {
            val viewModel: BorrowListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return BorrowListViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            BorrowListScreen(viewModel = viewModel, onNavigate = { route -> adminNavController.navigate(route) })
        }
        composable("admin_add_borrow") {
            val viewModel: AddBorrowViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AddBorrowViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AddBorrowScreen(viewModel = viewModel, onSaved = { adminNavController.popBackStack() })
        }
        composable(
            route = "admin_borrow_detail/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            val viewModel: BorrowDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return BorrowDetailViewModel(cardId, RetrofitClient.apiService) as T
                    }
                }
            )
            BorrowDetailScreen(viewModel = viewModel, onDeleted = { adminNavController.popBackStack() })
        }

        // --- Fine Management Routes ---
        composable("admin_fine_list") {
            val viewModel: FineListViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return FineListViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            FineListContent(viewModel = viewModel, onNavigate = { route -> adminNavController.navigate(route) })
        }
        composable("admin_add_fine") {
            val viewModel: AddFineViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AddFineViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AddFineScreen(viewModel = viewModel, onSaved = { adminNavController.popBackStack() })
        }
        composable(
            route = "admin_fine_detail/{fineId}",
            arguments = listOf(navArgument("fineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fineId = backStackEntry.arguments?.getString("fineId") ?: return@composable
            val viewModel: AdminFineDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminFineDetailViewModel(RetrofitClient.apiService, fineId) as T
                    }
                }
            )
            AdminFineDetailContent(viewModel = viewModel)
        }

        composable("admin_settings") {
            val viewModel: AdminSettingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AdminSettingsViewModel(RetrofitClient.apiService) as T
                    }
                }
            )
            AdminSettingsContent(viewModel = viewModel)
        }
        composable("admin_scan") { PlaceholderContent("Quét sách") }
    }
}

@Composable
private fun PlaceholderContent(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Màn hình $name", style = MaterialTheme.typography.headlineMedium)
    }
}
