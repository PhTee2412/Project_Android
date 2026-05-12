package com.example.smartlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.smartlibrary.ui.theme.SmartLibraryTheme
import com.example.smartlibrary.ui.viewmodel.MainViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(RetrofitClient.apiService) as T
                    }
                }
            )

            var isDarkMode by remember { mutableStateOf(false) }

            SmartLibraryTheme(darkTheme = isDarkMode) {
                MainApp(viewModel)
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

    val showBars = currentRoute != "search"

    Scaffold(
        topBar = {
            if (showBars) {
                AppHeader(
                    isLoggedIn = isLoggedIn,
                    cartCount = cartCount,
                    notificationCount = unreadNotifications,
                    onSearchClick = { navController.navigate("search") },
                    onCartClick = { },
                    onNotificationClick = { },
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
            modifier = Modifier.padding(innerPadding)  // Giữ lại, không bỏ
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onBookClick = { bookId -> },
                    onChatBotClick = { }
                )
            }
            composable("categories") { PlaceholderScreen("Thể loại") }
            composable("about") { PlaceholderScreen("Giới thiệu") }
            composable("news") { PlaceholderScreen("Tin sách") }
            composable("search") {
                SearchScreen(
                    searchResults = searchResults,
                    isLoading = isLoading,
                    onSearch = { viewModel.searchBooks(it) },
                    onBack = { navController.popBackStack() },
                    onBookClick = { bookId -> }
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