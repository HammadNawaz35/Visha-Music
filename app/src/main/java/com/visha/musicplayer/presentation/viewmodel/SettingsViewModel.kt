package com.visha.musicplayer.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.domain.repository.AudioRepository
import com.visha.musicplayer.presentation.theme.AppThemeMode
import com.visha.musicplayer.presentation.theme.colorPresets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AudioRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = repository.getThemeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig())

    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val notifications: StateFlow<List<AppNotification>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Cache size ─────────────────────────────────────────────────────────
    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    init { calculateCacheSize() }

    private fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = context.cacheDir.walkBottomUp()
                .filter { it.isFile }
                .sumOf { it.length() }
            _cacheSize.value = formatBytes(bytes)
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val size = _cacheSize.value
            // Clear Coil image cache
            Coil.imageLoader(context).diskCache?.clear()
            Coil.imageLoader(context).memoryCache?.clear()
            // Clear app cache dir
            context.cacheDir.walkBottomUp().forEach { if (it.isFile) it.delete() }
            _cacheSize.value = "0 B"
            _toast.emit("Cache cleared: $size")
        }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576     -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024         -> "%.1f KB".format(bytes / 1_024.0)
        else                   -> "$bytes B"
    }

    // ── Theme ──────────────────────────────────────────────────────────────
    fun saveTheme(config: ThemeConfig) {
        viewModelScope.launch(Dispatchers.IO) { repository.saveThemeConfig(config) }
    }

    // ── Profile ────────────────────────────────────────────────────────────
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) { repository.saveUserProfile(profile) }
    }

    // ── Notifications ──────────────────────────────────────────────────────
    fun markRead(id: Long) { viewModelScope.launch { repository.markNotificationRead(id) } }
    fun markAllRead() { viewModelScope.launch { repository.markAllRead() } }
    fun addDemoNotification() {
        viewModelScope.launch {
            repository.addNotification(AppNotification(
                id = 0, title = "New Release 🎵",
                description = "Fresh tracks added to your library this week.",
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
