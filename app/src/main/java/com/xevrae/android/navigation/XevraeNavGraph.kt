package com.xevrae.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun XevraeNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home,
    ) {
        composable<Route.Home> {
            // TODO: Replace with HomeScreen
            PlaceholderScreen("Home")
        }
        composable<Route.Search> {
            PlaceholderScreen("Search")
        }
        composable<Route.Library> {
            PlaceholderScreen("Library")
        }
        composable<Route.Settings> {
            PlaceholderScreen("Settings")
        }
        composable<Route.NowPlaying> {
            PlaceholderScreen("Now Playing")
        }
        composable<Route.Album> { backStackEntry ->
            val album = backStackEntry.toRoute<Route.Album>()
            PlaceholderScreen("Album: ${album.browseId}")
        }
        composable<Route.Artist> { backStackEntry ->
            val artist = backStackEntry.toRoute<Route.Artist>()
            PlaceholderScreen("Artist: ${artist.channelId}")
        }
        composable<Route.Playlist> { backStackEntry ->
            val playlist = backStackEntry.toRoute<Route.Playlist>()
            PlaceholderScreen("Playlist: ${playlist.id}")
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name)
    }
}
