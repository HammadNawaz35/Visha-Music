package com.visha.musicplayer.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("visha_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object K {
        val THEME_MODE      = stringPreferencesKey("theme_mode")
        val ACCENT_INDEX    = intPreferencesKey("accent_index")
        val GLASS_ENABLED   = booleanPreferencesKey("glass_enabled")
        val CUSTOM_BG       = stringPreferencesKey("custom_bg")
        val RECENTLY_PLAYED = stringPreferencesKey("recently_played")
        val PROFILE_NAME    = stringPreferencesKey("profile_name")
        val PROFILE_BIO     = stringPreferencesKey("profile_bio")
        val PROFILE_PIC     = stringPreferencesKey("profile_pic")
        val PLAYBACK_SPEED  = floatPreferencesKey("playback_speed")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[K.THEME_MODE] ?: "NAVY"
    }

    val accentIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[K.ACCENT_INDEX] ?: 0
    }

    val glassEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[K.GLASS_ENABLED] ?: true
    }

    val customBg: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[K.CUSTOM_BG] ?: ""
    }

    val profileName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[K.PROFILE_NAME] ?: ""
    }

    val profileBio: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[K.PROFILE_BIO] ?: ""
    }

    val profilePic: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[K.PROFILE_PIC] ?: ""
    }

    val playbackSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[K.PLAYBACK_SPEED] ?: 1f
    }

    val recentlyPlayedIds: Flow<List<Long>> = context.dataStore.data.map { prefs ->
        prefs[K.RECENTLY_PLAYED]
            ?.split(",")
            ?.mapNotNull { s -> s.toLongOrNull() }
            ?: emptyList()
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[K.THEME_MODE] = mode }
    }

    suspend fun setAccentIndex(idx: Int) {
        context.dataStore.edit { it[K.ACCENT_INDEX] = idx }
    }

    suspend fun setGlassEnabled(v: Boolean) {
        context.dataStore.edit { it[K.GLASS_ENABLED] = v }
    }

    suspend fun setCustomBg(uri: String) {
        context.dataStore.edit { it[K.CUSTOM_BG] = uri }
    }

    suspend fun setPlaybackSpeed(s: Float) {
        context.dataStore.edit { it[K.PLAYBACK_SPEED] = s }
    }

    suspend fun saveProfile(name: String, bio: String, pic: String) {
        context.dataStore.edit { prefs ->
            prefs[K.PROFILE_NAME] = name
            prefs[K.PROFILE_BIO]  = bio
            prefs[K.PROFILE_PIC]  = pic
        }
    }

    suspend fun addRecentlyPlayed(trackId: Long) {
        context.dataStore.edit { prefs ->
            val list = prefs[K.RECENTLY_PLAYED]
                ?.split(",")
                ?.mapNotNull { s -> s.toLongOrNull() }
                ?.toMutableList() ?: mutableListOf()
            list.remove(trackId)
            list.add(0, trackId)
            prefs[K.RECENTLY_PLAYED] = list.take(50).joinToString(",")
        }
    }
}