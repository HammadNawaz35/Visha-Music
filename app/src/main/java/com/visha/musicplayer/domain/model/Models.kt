package com.visha.musicplayer.domain.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: Uri?,
    val uri: Uri,
    val duration: Long,
    val dateAdded: Long = 0L,
    val isFavorite: Boolean = false
)

data class Playlist(
    val id: Long,
    val name: String,
    val tracks: List<Track> = emptyList()
)

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val playbackSpeed: Float = 1f,
    val sleepTimerMinutes: Int? = null
)

enum class RepeatMode { OFF, ONE, ALL }

// Loading state wrapper
sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error(val message: String) : LoadState<Nothing>()
}

data class AppNotification(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class UserProfile(
    val name: String = "",
    val bio: String = "",
    val profilePicUri: String = ""
)

data class ThemeConfig(
    val themeMode: String = "NAVY",         // NAVY, AMOLED, LIGHT
    val accentPresetIndex: Int = 0,
    val glassmorphismEnabled: Boolean = true,
    val customBackgroundUri: String = ""
)
