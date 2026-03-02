package com.visha.musicplayer.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.visha.musicplayer.presentation.components.MiniPlayer
import com.visha.musicplayer.presentation.screens.*
import com.visha.musicplayer.presentation.theme.*
import com.visha.musicplayer.presentation.viewmodel.*

object Routes {
    const val ONBOARDING     = "onboarding"
    const val HOME           = "home"
    const val LIBRARY        = "library"
    const val PLAYING        = "playing"
    const val FAVORITES      = "favorites"
    const val SETTINGS       = "settings"
    const val NOTIFICATIONS  = "notifications"
    const val TRACK_SELECTOR = "track_selector/{playlistId}/{playlistName}"
    fun trackSelector(pid: Long, name: String) = "track_selector/$pid/${name.replace("/","_")}"
}

sealed class Tab(val route: String, val label: String, val icon: ImageVector, val selIcon: ImageVector) {
    object Home      : Tab(Routes.HOME,      "Home",      Icons.Outlined.Home,            Icons.Filled.Home)
    object Library   : Tab(Routes.LIBRARY,   "Library",   Icons.Outlined.LibraryMusic,    Icons.Filled.LibraryMusic)
    object Playing   : Tab(Routes.PLAYING,   "Now",       Icons.Outlined.GraphicEq,       Icons.Filled.GraphicEq)
    object Favorites : Tab(Routes.FAVORITES, "Likes",     Icons.Outlined.FavoriteBorder,  Icons.Filled.Favorite)
    object Settings  : Tab(Routes.SETTINGS,  "Settings",  Icons.Outlined.Settings,        Icons.Filled.Settings)
}

val tabs = listOf(Tab.Home, Tab.Library, Tab.Playing, Tab.Favorites, Tab.Settings)

// Persist onboarding seen state
fun isOnboardingSeen(ctx: Context): Boolean =
    ctx.getSharedPreferences("visha", Context.MODE_PRIVATE).getBoolean("onboarding_done", false)

fun markOnboardingDone(ctx: Context) =
    ctx.getSharedPreferences("visha", Context.MODE_PRIVATE).edit()
        .putBoolean("onboarding_done", true).apply()

@Composable
fun AppNavigation() {
    val playerVm   : PlayerViewModel    = hiltViewModel()
    val libraryVm  : LibraryViewModel   = hiltViewModel()
    val settingsVm : SettingsViewModel  = hiltViewModel()
    val context = LocalContext.current

    val playerState      by playerVm.state.collectAsStateWithLifecycle()
    val favoriteIds      by playerVm.favoriteIds.collectAsStateWithLifecycle()
    val loadState        by libraryVm.loadState.collectAsStateWithLifecycle()
    val sortedTracks     by libraryVm.sortedTracks.collectAsStateWithLifecycle()
    val favTracks        by libraryVm.favoriteTracks.collectAsStateWithLifecycle()
    val libFavIds        by libraryVm.favoriteIds.collectAsStateWithLifecycle()
    val playlists        by libraryVm.playlists.collectAsStateWithLifecycle()
    val recentPlayed     by libraryVm.recentlyPlayedTracks.collectAsStateWithLifecycle()
    val isRefreshing     by libraryVm.isRefreshing.collectAsStateWithLifecycle()
    val sortOrder        by libraryVm.sortOrder.collectAsStateWithLifecycle()
    val searchQuery      by libraryVm.searchQuery.collectAsStateWithLifecycle()
    val searchResults    by libraryVm.searchResults.collectAsStateWithLifecycle()
    val isSearchExpanded by libraryVm.isSearchExpanded.collectAsStateWithLifecycle()
    val recentSearches   by libraryVm.recentSearches.collectAsStateWithLifecycle()
    val selectedIds      by libraryVm.selectedTrackIds.collectAsStateWithLifecycle()
    val themeConfig      by settingsVm.themeConfig.collectAsStateWithLifecycle()
    val userProfile      by settingsVm.userProfile.collectAsStateWithLifecycle()
    val unreadCount      by settingsVm.unreadCount.collectAsStateWithLifecycle()
    val notifications    by settingsVm.notifications.collectAsStateWithLifecycle()
    val cacheSize        by settingsVm.cacheSize.collectAsStateWithLifecycle()

    val isFavorite = playerState.currentTrack?.let { it.id in favoriteIds } ?: false

    // Scoped-storage delete confirmation
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            playerState.currentTrack?.let { playerVm.onDeleteConfirmed(it.id) }
        }
    }

    LaunchedEffect(Unit) {
        playerVm.deleteEvent.collect { sender ->
            deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }
    LaunchedEffect(Unit) { playerVm.toast.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    LaunchedEffect(Unit) { settingsVm.toast.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }

    val navController = rememberNavController()
    val backStack     by navController.currentBackStackEntryAsState()
    val currentRoute  = backStack?.destination?.route

    val showBottomBar = currentRoute != Routes.ONBOARDING
            && currentRoute != Routes.PLAYING
            && currentRoute != Routes.NOTIFICATIONS
            && currentRoute?.startsWith("track_selector") == false

    val startDest = if (isOnboardingSeen(context)) Routes.HOME else Routes.ONBOARDING

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    MiniPlayer(
                        playerState = playerState,
                        onPlayPause = playerVm::playPause,
                        onSkipNext  = playerVm::skipNext,
                        onClick     = { navController.navigate(Routes.PLAYING) }
                    )
                    Spacer(Modifier.height(2.dp))
                    VishaBottomNav(
                        currentRoute = currentRoute,
                        isPlaying    = playerState.isPlaying,
                        onNavigate   = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController, startDestination = startDest,
            modifier = Modifier.padding(padding),
            enterTransition    = { fadeIn(tween(220)) },
            exitTransition     = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition  = { fadeOut(tween(160)) }
        ) {
            composable(Routes.ONBOARDING,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition  = { fadeOut(tween(300)) }
            ) {
                OnboardingScreen(onDone = {
                    markOnboardingDone(context)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }

            composable(Routes.HOME) {
                HomeScreen(
                    tracks = sortedTracks, recentlyPlayed = recentPlayed,
                    userProfile = userProfile, unreadCount = unreadCount,
                    isRefreshing = isRefreshing, onRefresh = libraryVm::refresh,
                    onTrackClick = { t, queue ->
                        playerVm.playQueue(queue, queue.indexOf(t).coerceAtLeast(0))
                        navController.navigate(Routes.PLAYING)
                    },
                    onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) },
                    onViewAllClick = {
                        navController.navigate(Routes.LIBRARY) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    }
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    tracks = sortedTracks, favoriteTracks = favTracks, favoriteIds = libFavIds,
                    playlists = playlists, loadState = loadState, sortOrder = sortOrder,
                    isRefreshing = isRefreshing, isSearchExpanded = isSearchExpanded,
                    searchQuery = searchQuery, searchResults = searchResults, recentSearches = recentSearches,
                    onRefresh = libraryVm::refresh, onSortChange = libraryVm::setSortOrder,
                    onTrackClick = { t, queue ->
                        playerVm.playQueue(queue, queue.indexOf(t).coerceAtLeast(0))
                        navController.navigate(Routes.PLAYING)
                    },
                    onToggleFavorite = libraryVm::toggleFavorite,
                    onToggleSearch = libraryVm::toggleSearch,
                    onSearchQuery = libraryVm::setSearchQuery,
                    onSaveSearch = libraryVm::saveRecentSearch,
                    onRemoveSearch = libraryVm::removeRecentSearch,
                    onClearSearches = libraryVm::clearRecentSearches,
                    onCreatePlaylist = libraryVm::createPlaylist,
                    onDeletePlaylist = libraryVm::deletePlaylist,
                    onPlaylistClick = { pl ->
                        navController.navigate(Routes.trackSelector(pl.id, pl.name))
                    }
                )
            }

            composable(
                Routes.PLAYING,
                enterTransition = { slideInVertically { it } + fadeIn() },
                exitTransition  = { slideOutVertically { it } + fadeOut() },
                popExitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                PlayScreen(
                    playerState = playerState, isFavorite = isFavorite,
                    onPlayPause = playerVm::playPause, onSkipNext = playerVm::skipNext,
                    onSkipPrevious = playerVm::skipPrevious, onSeekForward10 = playerVm::seekForward10,
                    onSeekBack10 = playerVm::seekBack10, onSeek = playerVm::seekTo,
                    onToggleShuffle = playerVm::toggleShuffle, onToggleRepeat = playerVm::toggleRepeat,
                    onToggleFavorite = { playerState.currentTrack?.let { playerVm.toggleFavorite(it.id) } },
                    onSetSpeed = playerVm::setSpeed, onSetSleepTimer = playerVm::setSleepTimer,
                    // FIX: delete calls delete; back happens automatically when track removed from queue
                    onDeleteTrack = playerVm::deleteCurrentTrack,
                    // FIX: Up Next click seeks to that index in the queue
                    onSeekToQueueIndex = { idx -> playerVm.seekToQueueIndex(idx) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    favoriteTracks = favTracks,
                    onTrackClick = { t, queue ->
                        playerVm.playQueue(queue, queue.indexOf(t).coerceAtLeast(0))
                        navController.navigate(Routes.PLAYING)
                    },
                    onToggleFavorite = libraryVm::toggleFavorite
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    themeConfig = themeConfig, userProfile = userProfile, cacheSize = cacheSize,
                    onSaveTheme = settingsVm::saveTheme, onSaveProfile = settingsVm::saveProfile,
                    onClearCache = settingsVm::clearCache, onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    notifications = notifications, onBack = { navController.popBackStack() },
                    onRead = settingsVm::markRead, onMarkAllRead = settingsVm::markAllRead
                )
            }

            composable(Routes.TRACK_SELECTOR) { entry ->
                val pid   = entry.arguments?.getString("playlistId")?.toLongOrNull() ?: return@composable
                val pName = entry.arguments?.getString("playlistName") ?: "Playlist"
                TrackSelectorScreen(
                    playlistName   = pName, allTracks = sortedTracks, selectedIds = selectedIds,
                    onToggleSelect = libraryVm::toggleTrackSelection,
                    onSelectAll    = libraryVm::selectAllTracks,
                    onConfirm      = { libraryVm.addSelectedTracksToPlaylist(pid); navController.popBackStack() },
                    onBack         = { libraryVm.clearSelection(); navController.popBackStack() }
                )
            }
        }
    }
}

// ── Visha Floating Glass Bottom Nav ──────────────────────────────────────────
@Composable
fun VishaBottomNav(currentRoute: String?, isPlaying: Boolean, onNavigate: (Tab) -> Unit) {
    val colors = LocalAppColors.current

    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(listOf(
                        colors.card.copy(alpha = 0.95f),
                        colors.elevated.copy(alpha = 0.88f)
                    ))
                )
                .border(0.5.dp, colors.border, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            Arrangement.SpaceEvenly,
            Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                if (tab is Tab.Playing) {
                    // Pulsing center button
                    val sc by rememberInfiniteTransition(label = "p").animateFloat(
                        initialValue = 1f, targetValue = if (isPlaying) 1.14f else 1f,
                        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "sc"
                    )
                    Box(
                        Modifier.size(58.dp).scale(sc).clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(colors.primaryLight, colors.primary))
                            )
                            .clickable { onNavigate(tab) },
                        Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                            null, tint = Color.White, modifier = Modifier.size(27.dp)
                        )
                    }
                } else {
                    Column(
                        Modifier.weight(1f).clickable { onNavigate(tab) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (selected) tab.selIcon else tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) colors.primary else colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        if (selected) {
                            Box(Modifier.size(4.dp).clip(CircleShape).background(colors.primary))
                        } else {
                            Text(tab.label, color = colors.textMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SuspiciousModifierThen")
private fun Modifier.border(w: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(border(w, color, shape))