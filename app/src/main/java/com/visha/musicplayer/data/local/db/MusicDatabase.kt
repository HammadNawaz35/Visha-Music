package com.visha.musicplayer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.visha.musicplayer.data.local.entity.*

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        NotificationEntity::class,
        RecentSearchEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun notificationDao(): NotificationDao
    abstract fun recentSearchDao(): RecentSearchDao
}
