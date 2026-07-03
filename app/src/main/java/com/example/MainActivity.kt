package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.AnimeViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe viewmodel's isDarkMode state dynamically!
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                // Ensure RTL layout for beautiful localized Arabic layout
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            BottomNavigationBar(
                                selectedTab = currentDestinationRoute(),
                                onTabSelected = { route ->
                                    navigateToRoute(route)
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            NavigationGraph(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Handle possible Intent extras from background notifications click
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val openAnimeId = intent.getStringExtra("OPEN_ANIME_ID")
        val openProviderId = intent.getStringExtra("OPEN_PROVIDER_ID")
        val playEpisodeNum = intent.getIntExtra("PLAY_EPISODE_NUM", -1)

        if (!openAnimeId.isNullOrEmpty() && !openProviderId.isNullOrEmpty()) {
            Log.d("MainActivity", "Notification clicked! Navigating to Anime details: $openAnimeId")
            viewModel.loadAnimeDetails(openAnimeId, openProviderId)
            
            // Automatically launch watch if an episode is provided
            if (playEpisodeNum != -1) {
                // We'll let the viewmodel select the episode once loaded
                viewModel.selectEpisode(
                    com.example.data.model.AnimeEpisode(
                        id = "${openAnimeId}_$playEpisodeNum",
                        number = playEpisodeNum,
                        releaseDate = "حلقة $playEpisodeNum",
                        animeId = openAnimeId,
                        providerId = openProviderId
                    )
                )
            }
        }
    }

    // Navigation controllers references for outer functions helper
    private var navController: androidx.navigation.NavHostController? = null

    @Composable
    private fun currentDestinationRoute(): String {
        val navBackStackEntry by (navController ?: return "search").currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.route?.substringBefore("/") ?: "search"
    }

    private fun navigateToRoute(route: String) {
        navController?.navigate(route) {
            popUpTo("search") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    @Composable
    fun NavigationGraph(
        viewModel: AnimeViewModel,
        modifier: Modifier = Modifier
    ) {
        val controller = rememberNavController()
        navController = controller

        NavHost(
            navController = controller,
            startDestination = "search",
            modifier = modifier
        ) {
            composable("search") {
                MainSearchScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id, pId ->
                        controller.navigate("details/$id/$pId")
                    }
                )
            }
            
            composable(
                route = "details/{animeId}/{providerId}",
                arguments = listOf(
                    navArgument("animeId") { type = NavType.StringType },
                    navArgument("providerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("animeId") ?: ""
                val pId = backStackEntry.arguments?.getString("providerId") ?: ""
                
                AnimeDetailsScreen(
                    viewModel = viewModel,
                    animeId = id,
                    providerId = pId,
                    onBack = { controller.popBackStack() },
                    onWatchEpisode = { episode ->
                        viewModel.selectEpisode(episode)
                        controller.navigate("watch")
                    }
                )
            }

            composable("watch") {
                WatchScreen(
                    viewModel = viewModel,
                    onBackToDetails = { controller.popBackStack() }
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id, pId ->
                        controller.navigate("details/$id/$pId")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == "search" || selectedTab == "details" || selectedTab == "watch",
            onClick = { onTabSelected("search") },
            label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "بحث") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryAnime,
                indicatorColor = PrimaryAnime
            ),
            modifier = Modifier.testTag("nav_search_tab")
        )

        NavigationBarItem(
            selected = selectedTab == "favorites",
            onClick = { onTabSelected("favorites") },
            label = { Text("المفضلة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "المفضلة") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryAnime,
                indicatorColor = PrimaryAnime
            ),
            modifier = Modifier.testTag("nav_favorites_tab")
        )

        NavigationBarItem(
            selected = selectedTab == "settings",
            onClick = { onTabSelected("settings") },
            label = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "الإعدادات") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = PrimaryAnime,
                indicatorColor = PrimaryAnime
            ),
            modifier = Modifier.testTag("nav_settings_tab")
        )
    }
}
