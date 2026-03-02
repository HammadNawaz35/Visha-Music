package com.visha.musicplayer.data.repository

import com.visha.musicplayer.data.local.datastore.AppPreferences
import com.visha.musicplayer.data.local.db.*
import com.visha.musicplayer.data.local.entity.*
import com.visha.musicplayer.data.mediastore.DeleteResult
import com.visha.musicplayer.data.mediastore.MediaStoreHelper
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.domain.repository.AudioRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val media: MediaStoreHelper,
    private val favDao: FavoriteDao,
    private val playlistDao: PlaylistDao,
    private val notifDao: NotificationDao,
    private val searchDao: RecentSearchDao,
    private val prefs: AppPreferences
) : AudioRepository {

    override fun getTracksFlow(): Flow<LoadState<List<Track>>> = media.getAllTracksFlow()

    override suspend fun getAllTracks(): List<Track> = media.getAllTracks()

    override suspend fun deleteTrack(id: Long): DeleteResult {
        favDao.removeFavorite(id)
        return media.deleteTrack(id)
    }

    // ── Favorites ──────────────────────────────────────────────────────────────
    override fun getFavoriteTracks(): Flow<List<Track>> =
        favDao.getAllFavorites().map { favs ->
            val ids = favs.map { it.trackId }.toSet()
            media.getAllTracks().filter { it.id in ids }.map { it.copy(isFavorite = true) }
        }

    override fun getFavoriteIds(): Flow<Set<Long>> =
        favDao.getFavoriteIds().map { it.toSet() }

    override suspend fun addFavorite(id: Long) = favDao.addFavorite(FavoriteEntity(id))

    override suspend fun removeFavorite(id: Long) = favDao.removeFavorite(id)

    override suspend fun isFavorite(id: Long): Boolean = favDao.isFavorite(id) > 0

    // ── Playlists ──────────────────────────────────────────────────────────────
    // FIX: only resolve tracks that are stored in playlist_tracks;
    // map by position order so the correct (and ONLY selected) songs appear.
    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { entities ->
            val allTracks = media.getAllTracks().associateBy { it.id }
            entities.map { entity ->
                val orderedIds = playlistDao.getTrackIdsForPlaylist(entity.id)   // already ordered by position
                Playlist(
                    id     = entity.id,
                    name   = entity.name,
                    tracks = orderedIds.mapNotNull { trackId -> allTracks[trackId] }
                )
            }
        }

    override suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name))

    override suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    // FIX: deduplicate before inserting — prevents duplicate rows if user taps twice
    override suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) {
        val existing = playlistDao.getTrackIdsForPlaylist(playlistId).toSet()
        val newIds   = trackIds.filter { it !in existing }
        val startPos = existing.size
        val entities = newIds.mapIndexed { i, tid ->
            PlaylistTrackEntity(playlistId = playlistId, trackId = tid, position = startPos + i)
        }
        if (entities.isNotEmpty()) playlistDao.addTracksToPlaylist(entities)
    }

    override suspend fun removeTrackFromPlaylist(pid: Long, tid: Long) =
        playlistDao.removeTrackFromPlaylist(pid, tid)

    override suspend fun getPlaylistTracks(pid: Long): List<Track> {
        val ids = playlistDao.getTrackIdsForPlaylist(pid)
        val map = media.getAllTracks().associateBy { it.id }
        return ids.mapNotNull { map[it] }
    }

    // ── Recently Played ────────────────────────────────────────────────────────
    override fun getRecentlyPlayedIds(): Flow<List<Long>> = prefs.recentlyPlayedIds

    override suspend fun addToRecentlyPlayed(id: Long) = prefs.addRecentlyPlayed(id)

    // ── Recent Searches ────────────────────────────────────────────────────────
    override fun getRecentSearches(): Flow<List<String>> =
        searchDao.getAll().map { it.map { s -> s.query } }

    override suspend fun addRecentSearch(q: String) = searchDao.insert(RecentSearchEntity(q))

    override suspend fun removeRecentSearch(q: String) = searchDao.delete(q)

    override suspend fun clearRecentSearches() = searchDao.clearAll()

    // ── Theme ──────────────────────────────────────────────────────────────────
    override fun getThemeConfig(): Flow<ThemeConfig> = combine(
        prefs.themeMode, prefs.accentIndex, prefs.glassEnabled, prefs.customBg
    ) { mode, idx, glass, bg -> ThemeConfig(mode, idx, glass, bg) }

    override suspend fun saveThemeConfig(config: ThemeConfig) {
        prefs.setThemeMode(config.themeMode)
        prefs.setAccentIndex(config.accentPresetIndex)
        prefs.setGlassEnabled(config.glassmorphismEnabled)
        prefs.setCustomBg(config.customBackgroundUri)
    }

    // ── Profile ────────────────────────────────────────────────────────────────
    override fun getUserProfile(): Flow<UserProfile> = combine(
        prefs.profileName, prefs.profileBio, prefs.profilePic
    ) { name, bio, pic -> UserProfile(name, bio, pic) }

    override suspend fun saveUserProfile(profile: UserProfile): Unit {
        prefs.saveProfile(profile.name, profile.bio, profile.profilePicUri)
    }

    // ── Notifications ──────────────────────────────────────────────────────────
    override fun getNotifications(): Flow<List<AppNotification>> =
        notifDao.getAll().map { list ->
            list.map { n ->
                AppNotification(n.id, n.title, n.description, n.imageUrl, n.timestamp, n.isRead)
            }
        }

    override suspend fun addNotification(n: AppNotification) {
        notifDao.insert(NotificationEntity(
            title = n.title, description = n.description,
            imageUrl = n.imageUrl, timestamp = n.timestamp
        ))
    }

    override suspend fun markNotificationRead(id: Long) = notifDao.markRead(id)

    override suspend fun markAllRead() = notifDao.markAllRead()

    override fun getUnreadCount(): Flow<Int> = notifDao.getUnreadCount()
}