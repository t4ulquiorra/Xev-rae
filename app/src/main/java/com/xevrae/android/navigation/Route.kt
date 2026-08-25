package com.xevrae.android.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Search : Route
    @Serializable data object Library : Route
    @Serializable data object Settings : Route
    @Serializable data object Login : Route
    @Serializable data object NowPlaying : Route
    @Serializable data class Album(val browseId: String) : Route
    @Serializable data class Artist(val channelId: String) : Route
    @Serializable data class Playlist(val id: String) : Route
    @Serializable data class LocalPlaylist(val id: Long) : Route
    @Serializable data object Mood : Route
    @Serializable data object RecentlySongs : Route
    @Serializable data object Analytics : Route
    @Serializable data object Notification : Route
    @Serializable data object Podcast : Route
    @Serializable data class MoreAlbums(val browseId: String) : Route
    @Serializable data object SpotifyLogin : Route
    @Serializable data object DiscordLogin : Route
    @Serializable data object LibraryDynamicPlaylist : Route
    @Serializable data object FullscreenPlayer : Route
}
