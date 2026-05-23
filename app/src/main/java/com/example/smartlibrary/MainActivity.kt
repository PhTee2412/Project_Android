package com.example.smartlibrary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.smartlibrary.ui.screens.*
import com.example.smartlibrary.ui.theme.SmartLibraryTheme
import com.example.smartlibrary.ui.viewmodel.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.smartlibrary.data.SessionManager
import com.facebook.CallbackManager
import com.facebook.FacebookSdk

class MainActivity : ComponentActivity() {

    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize RetrofitClient with context for Authorization header
        RetrofitClient.initialize(this)

        FacebookSdk.fullyInitialize()
        callbackManager = CallbackManager.Factory.create()

        val sessionManager = SessionManager(this)

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(RetrofitClient.apiService, sessionManager) as T
                    }
                }
            )

            var isDarkMode by remember { mutableStateOf(false) }

            SmartLibraryTheme(darkTheme = isDarkMode) {
                MainApp(mainViewModel, sessionManager)
            }
        }
    }

    @Deprecated("Override for Facebook SDK compatibility")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    sessionManager: SessionManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Khởi tạo các ViewModel dùng chung (Scoped to Activity/MainApp)
    val borrowedViewModel: BorrowedCardsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BorrowedCardsViewModel(RetrofitClient.apiService, sessionManager) as T
            }
        }
    )

    val chatViewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(RetrofitClient.apiService, sessionManager) as T
            }
        }
    )

    val finesViewModel: FinesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FinesViewModel(RetrofitClient.apiService, sessionManager) as T
            }
        }
    )

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            // On logout: clear session-dependent viewmodels so UI doesn't show previous user's data
            chatViewModel.clearChat()
            borrowedViewModel.clearBorrowCards()
            finesViewModel.clearFines()
        } else {
            // On login: reload user specific data
            borrowedViewModel.loadBorrowCards()
            finesViewModel.loadFines()

        }
    }

    val newsViewModel: NewsViewModel = viewModel()

    // Cập nhật số lượng thông báo và giỏ hàng khi chuyển trang
    LaunchedEffect(currentRoute, isLoggedIn) {
        if (isLoggedIn) {
            viewModel.refreshCounts()
        }
    }

    val showBars = currentRoute != "search" &&
            currentRoute != "chat" &&
            currentRoute != "profile" &&
            currentRoute != "borrowed_cards" &&
            currentRoute != "fines" &&
            currentRoute != "change_password" &&
            currentRoute != "user_qrcode" &&
            currentRoute != "login" &&
            currentRoute?.startsWith("fine_detail") != true &&
            currentRoute?.startsWith("borrowed_card_detail") != true &&
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
                    onProfileClick = { navController.navigate("profile") },
                    onLoginClick = { navController.navigate("login") },
                    onChatClick = { navController.navigate("chat") },
                    onBorrowedCardsClick = { navController.navigate("borrowed_cards") },
                    onFineClick = { navController.navigate("fines") },
                    onChangePasswordClick = { navController.navigate("change_password") },
                    onQRCodeClick = { navController.navigate("user_qrcode") },
                    onLogoutClick = {
                        viewModel.logout()
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
                    onChatBotClick = { navController.navigate("chat") }
                )
            }
            composable("login") {
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AuthViewModel(
                                apiService = RetrofitClient.apiService,
                                sessionManager = sessionManager,
                                onLoginSuccess = {
                                    viewModel.setLoggedIn(true)
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            ) as T
                        }
                    }
                )
                AuthScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() }
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
                                sessionManager = sessionManager,
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
                                sessionManager = sessionManager,
                                onUnreadCountChanged = { count -> viewModel.setUnreadNotificationCount(count) }
                            ) as T
                        }
                    }
                )
                NotificationScreen(
                    viewModel = notificationViewModel,
                    userId = sessionManager.getUserId() ?: ""
                )
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ProfileViewModel(RetrofitClient.apiService, sessionManager) as T
                        }
                    }
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("change_password") {
                val changePasswordViewModel: ChangePasswordViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ChangePasswordViewModel(RetrofitClient.apiService, sessionManager) as T
                        }
                    }
                )
                ChangePasswordScreen(
                    viewModel = changePasswordViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("user_qrcode") {
                val qrViewModel: UserQRCodeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return UserQRCodeViewModel(RetrofitClient.apiService, sessionManager) as T
                        }
                    }
                )
                UserQRCodeScreen(
                    viewModel = qrViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("borrowed_cards") {
                BorrowedCardsScreen(
                    viewModel = borrowedViewModel, // Dùng chung instance
                    onCardClick = { cardId -> navController.navigate("borrowed_card_detail/$cardId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "borrowed_card_detail/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.IntType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getInt("cardId") ?: 0
                BorrowedCardDetailScreen(
                    cardId = cardId,
                    viewModel = borrowedViewModel, // Dùng chung instance
                    onBack = { navController.popBackStack() }
                )
            }
            composable("fines") {
                FinesScreen(
                    viewModel = finesViewModel,
                    onFineClick = { fineId -> navController.navigate("fine_detail/$fineId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "fine_detail/{fineId}",
                arguments = listOf(navArgument("fineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fineId = backStackEntry.arguments?.getString("fineId") ?: ""
                FineDetailScreen(
                    fineId = fineId,
                    viewModel = finesViewModel,
                    onBack = { navController.popBackStack() }
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
            composable("chat") {
                ChatScreen(
                    viewModel = chatViewModel,
                    onBack = { navController.popBackStack() }
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
                            return BookDetailViewModel(RetrofitClient.apiService, sessionManager, bookId) as T
                        }
                    }
                )
                BookDetailScreen(
                    viewModel = detailViewModel,
                    isLoggedIn = isLoggedIn,
                    onLoginRequired = { navController.navigate("login") },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}