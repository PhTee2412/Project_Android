package com.example.smartlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartlibrary.network.RetrofitClient
import com.example.smartlibrary.ui.components.AppBottomBar
import com.example.smartlibrary.ui.components.AppHeader
import com.example.smartlibrary.ui.screens.HomeScreen
import com.example.smartlibrary.ui.screens.SearchScreen
import com.example.smartlibrary.ui.screens.BookDetailScreen
import com.example.smartlibrary.ui.screens.CategoriesScreen
import com.example.smartlibrary.ui.screens.AboutScreen
import com.example.smartlibrary.ui.screens.NewsScreen
import com.example.smartlibrary.ui.screens.NewsDetailScreen
import com.example.smartlibrary.ui.screens.CartScreen
import com.example.smartlibrary.ui.screens.NotificationScreen
import com.example.smartlibrary.ui.theme.SmartLibraryTheme
import com.example.smartlibrary.ui.viewmodel.MainViewModel
import com.example.smartlibrary.ui.viewmodel.BookDetailViewModel
import com.example.smartlibrary.ui.viewmodel.CategoriesViewModel
import com.example.smartlibrary.ui.viewmodel.NewsViewModel
import com.example.smartlibrary.ui.viewmodel.CartViewModel
import com.example.smartlibrary.ui.viewmodel.NotificationViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(RetrofitClient.apiService) as T
                    }
                }
            )

            var isDarkMode by remember { mutableStateOf(false) }

            SmartLibraryTheme(darkTheme = isDarkMode) {
                MainApp(mainViewModel)
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // NewsViewModel dùng chung cho NewsScreen và NewsDetailScreen
    val newsViewModel: NewsViewModel = viewModel()

    // Chỉ hiện Header và BottomBar ở các màn hình chính
    val showBars = currentRoute != "search" && 
                   currentRoute?.startsWith("book_detail") != true &&
                   currentRoute?.startsWith("news_detail") != true

    Scaffold(
        topBar = {
            if (showBars) {
                AppHeader(
                    isLoggedIn = isLoggedIn,
                    cartCount = cartCount,
                    notificationCount = unreadNotifications,
                    onSearchClick = { navController.navigate("search") },
                    onCartClick = { navController.navigate("cart") },
                    onNotificationClick = { navController.navigate("notifications") },
                    onProfileClick = { },
                    onLoginClick = { viewModel.toggleLogin() },
                    onChatClick = { viewModel.showChatBot() }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                AppBottomBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(if (showBars) innerPadding else PaddingValues(0.dp))
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onBookClick = { bookId -> navController.navigate("book_detail/$bookId") },
                    onChatBotClick = { }
                )
            }
            composable("categories") { 
                val categoriesViewModel: CategoriesViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CategoriesViewModel(RetrofitClient.apiService) as T
                        }
                    }
                )
                CategoriesScreen(
                    viewModel = categoriesViewModel,
                    onBookClick = { bookId -> navController.navigate("book_detail/$bookId") }
                )
            }
            composable("about") { AboutScreen(navController = navController) }
            composable("news") { 
                NewsScreen(
                    viewModel = newsViewModel,
                    onNewsClick = { news ->
                        newsViewModel.selectNews(news)
                        navController.navigate("news_detail")
                    }
                )
            }
            composable("news_detail") {
                NewsDetailScreen(
                    viewModel = newsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("cart") {
                val cartViewModel: CartViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CartViewModel(
                                apiService = RetrofitClient.apiService,
                                onCartCountChanged = { count -> viewModel.setCartCount(count) }
                            ) as T
                        }
                    }
                )
                CartScreen(
                    viewModel = cartViewModel,
                    onBookClick = { bookId -> navController.navigate("book_detail/$bookId") }
                )
            }
            composable("notifications") {
                val notificationViewModel: NotificationViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return NotificationViewModel(
                                apiService = RetrofitClient.apiService,
                                onUnreadCountChanged = { count -> viewModel.setUnreadNotificationCount(count) }
                            ) as T
                        }
                    }
                )
                NotificationScreen(
                    viewModel = notificationViewModel,
                    userId = "user123" // Mock userId
                )
            }
            composable("search") {
                val searchQuery by viewModel.searchQuery.collectAsState()
                SearchScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchBooks(it) },
                    searchResults = searchResults,
                    isLoading = isLoading,
                    onSearch = { viewModel.searchBooks(it) },
                    onBack = { navController.popBackStack() },
                    onBookClick = { bookId -> navController.navigate("book_detail/$bookId") }
                )
            }
            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                val detailViewModel: BookDetailViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return BookDetailViewModel(RetrofitClient.apiService, bookId) as T
                        }
                    }
                )
                BookDetailScreen(
                    viewModel = detailViewModel,
                    isLoggedIn = isLoggedIn,
                    onLoginRequired = { viewModel.toggleLogin() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Màn hình $name", style = MaterialTheme.typography.headlineMedium)
    }
}
