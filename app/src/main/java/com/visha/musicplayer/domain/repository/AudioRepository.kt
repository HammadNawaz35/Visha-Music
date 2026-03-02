package com.visha.musicplayer.domain.repository

import com.visha.musicplayer.data.mediastore.DeleteResult
import com.visha.musicplayer.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    // Tracks
    fun getTracksFlow(): Flow<LoadState<List<Track>>>
    suspend fun getAllTracks(): List<Track>
    suspend fun deleteTrack(trackId: Long): DeleteResult

    // Favorites
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getFavoriteIds(): Flow<Set<Long>>
    suspend fun addFavorite(id: Long)
    suspend fun removeFavorite(id: Long)
    suspend fun isFavorite(id: Long): Boolean

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(id: Long)
    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun getPlaylistTracks(playlistId: Long): List<Track>

    // Recently Played
    fun getRecentlyPlayedIds(): Flow<List<Long>>
    suspend fun addToRecentlyPlayed(trackId: Long)

    // Recent Searches
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addRecentSearch(query: String)
    suspend fun removeRecentSearch(query: String)
    suspend fun clearRecentSearches()

    // Theme / Profile
    fun getThemeConfig(): Flow<ThemeConfig>
    suspend fun saveThemeConfig(config: ThemeConfig)
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)

    // Notifications
    fun getNotifications(): Flow<List<AppNotification>>
    suspend fun addNotification(n: AppNotification)
    suspend fun markNotificationRead(id: Long)
    suspend fun markAllRead()
    fun getUnreadCount(): Flow<Int>
}
