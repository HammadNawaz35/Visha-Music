package com.visha.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.visha.musicplayer.data.local.db.*
import com.visha.musicplayer.data.repository.AudioRepositoryImpl
import com.visha.musicplayer.domain.repository.AudioRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): MusicDatabase =
        Room.databaseBuilder(ctx, MusicDatabase::class.java, "visha_db")
            .fallbackToDestructiveMigration().build()

    @Provides fun provideFavoriteDao(db: MusicDatabase)      = db.favoriteDao()
    @Provides fun providePlaylistDao(db: MusicDatabase)      = db.playlistDao()
    @Provides fun provideNotificationDao(db: MusicDatabase)  = db.notificationDao()
    @Provides fun provideRecentSearchDao(db: MusicDatabase)  = db.recentSearchDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindAudioRepository(impl: AudioRepositoryImpl): AudioRepository
}
