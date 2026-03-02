package com.visha.musicplayer.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.presentation.theme.*
import com.visha.musicplayer.presentation.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    tracks: List<Track>,
    favoriteTracks: List<Track>,
    favoriteIds: Set<Long>,
    playlists: List<Playlist>,
    loadState: LoadState<List<Track>>,
    sortOrder: SortOrder,
    isRefreshing: Boolean,
    isSearchExpanded: Boolean,
    searchQuery: String,
    searchResults: List<Track>,
    recentSearches: List<String>,
    onRefresh: () -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQuery: (String) -> Unit,
    onSaveSearch: (String) -> Unit,
    onRemoveSearch: (String) -> Unit,
    onClearSearches: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlaylistClick: (Playlist) -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    var selectedTab    by remember { mutableIntStateOf(0) }
    val tabs           = listOf("Songs", "Albums", "Artists", "Playlists")
    var showCreateDlg  by remember { mutableStateOf(false) }
    var newName        by remember { mutableStateOf("") }
    var showSortMenu   by remember { mutableStateOf(false) }
    val keyboard       = LocalSoftwareKeyboardController.current
    val pullState      = rememberPullToRefreshState()

    // Artist detail drill-down state
    var artistDetailName   by remember { mutableStateOf<String?>(null) }
    var artistDetailTracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    // Show artist detail screen if selected
    if (artistDetailName != null) {
        ArtistDetailScreen(
            artistName = artistDetailName!!,
            tracks     = artistDetailTracks,
            favoriteIds= favoriteIds,
            onTrackClick = { track -> onTrackClick(track, artistDetailTracks) },
            onToggleFavorite = onToggleFavorite,
            onBack     = { artistDetailName = null; artistDetailTracks = emptyList() }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Box(Modifier.size(200.dp).align(Alignment.TopEnd).offset(60.dp, (-20).dp)
            .background(
                Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.1f), Color.Transparent)),
                CircleShape
            ))

        PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh,
            state = pullState, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                // ── Header ─────────────────────────────────────────────────
                Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("YOUR LIBRARY", color = colors.primary, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("Music", color = colors.textPrimary, fontSize = 28.sp,
                                fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = onToggleSearch) {
                            Box(Modifier.size(38.dp).clip(CircleShape)
                                .background(if (isSearchExpanded) colors.primary else colors.card),
                                Alignment.Center) {
                                Icon(if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    null,
                                    tint = if (isSearchExpanded) Color.White else colors.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Expandable search
                    AnimatedVisibility(isSearchExpanded, enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = searchQuery, onValueChange = onSearchQuery,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search songs, artists, albums...", color = colors.textMuted) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primary) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty())
                                        IconButton(onClick = { onSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, null, tint = colors.textSecondary)
                                        }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.card.copy(alpha = 0.8f),
                                    unfocusedContainerColor = colors.card.copy(alpha = 0.6f),
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary, cursorColor = colors.primary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    if (searchQuery.isNotBlank()) { onSaveSearch(searchQuery); keyboard?.hide() }
                                })
                            )
                            if (searchQuery.isEmpty() && recentSearches.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text("Recent", color = colors.textSecondary, fontSize = 12.sp)
                                    Text("Clear all", color = colors.primary, fontSize = 12.sp,
                                        modifier = Modifier.clickable(onClick = onClearSearches))
                                }
                                Spacer(Modifier.height(8.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()) {
                                    recentSearches.forEach { q ->
                                        InputChip(selected = false, onClick = { onSearchQuery(q) },
                                            label = { Text(q, color = colors.textPrimary, fontSize = 13.sp) },
                                            trailingIcon = {
                                                Icon(Icons.Default.Close, null, tint = colors.textSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                        .clickable { onRemoveSearch(q) })
                                            },
                                            colors = InputChipDefaults.inputChipColors(containerColor = colors.card),
                                            border = InputChipDefaults.inputChipBorder(enabled = true,
                                                selected = false, borderColor = colors.border))
                                    }
                                }
                            }
                        }
                    }
                }

                // Search results overlay
                if (isSearchExpanded && searchQuery.isNotBlank()) {
                    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
                        item {
                            Text("${searchResults.size} results for \"$searchQuery\"",
                                color = colors.textSecondary, fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                        }
                        items(searchResults, key = { it.id }) { t ->
                            TrackRow(t, isFavorite = t.id in favoriteIds, showFavIcon = true,
                                onClick = { onTrackClick(t, searchResults) },
                                onFavorite = { onToggleFavorite(t.id) })
                        }
                    }
                    return@PullToRefreshBox
                }

                // ── Tabs ──────────────────────────────────────────────────
                ScrollableTabRow(
                    selectedTabIndex = selectedTab, containerColor = Color.Transparent,
                    contentColor = colors.primary, edgePadding = 16.dp,
                    indicator = { positions ->
                        if (selectedTab < positions.size)
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(positions[selectedTab]),
                                color = colors.primary
                            )
                    }, divider = {}
                ) {
                    tabs.forEachIndexed { i, lbl ->
                        Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = {
                            Text(lbl,
                                color = if (selectedTab == i) colors.primary else colors.textSecondary,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal)
                        })
                    }
                }

                when (loadState) {
                    is LoadState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Scanning music...", color = colors.textSecondary)
                        }
                    }
                    is LoadState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Error: ${loadState.message}", color = colors.textSecondary)
                    }
                    is LoadState.Success -> when (selectedTab) {
                        0 -> SongsTab(tracks, favoriteIds, sortOrder, showSortMenu,
                            { showSortMenu = it }, onSortChange, onTrackClick, onToggleFavorite)
                        1 -> AlbumsTab(tracks, onTrackClick)
                        2 -> ArtistsTab(tracks) { name, artistTracks ->
                            artistDetailName   = name
                            artistDetailTracks = artistTracks
                        }
                        3 -> PlaylistsTab(playlists, { showCreateDlg = true }, onDeletePlaylist, onPlaylistClick)
                    }
                }
            }
        }
    }

    if (showCreateDlg) {
        AlertDialog(onDismissRequest = { showCreateDlg = false },
            containerColor = colors.card, shape = RoundedCornerShape(20.dp),
            title = { Text("New Playlist", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it },
                    label = { Text("Playlist name", color = colors.textSecondary) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary, unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary))
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) { onCreatePlaylist(newName.trim()); newName = "" }
                    showCreateDlg = false
                }) { Text("Create", color = colors.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDlg = false }) { Text("Cancel", color = colors.textSecondary) }
            })
    }
}

// ─── Artist Detail (shows songs list before playing) ─────────────────────────
@Composable
fun ArtistDetailScreen(
    artistName: String,
    tracks: List<Track>,
    favoriteIds: Set<Long>,
    onTrackClick: (Track) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onBack: () -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(Modifier.fillMaxWidth().height(190.dp)
                .background(Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.surface)))) {
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.25f))
                        .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape), Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(artistName, color = colors.textPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("${tracks.size} songs", color = colors.primary, fontSize = 13.sp)
                }
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary)
                }
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
                items(tracks, key = { it.id }) { t ->
                    TrackRow(t, isFavorite = t.id in favoriteIds, showFavIcon = false,
                        onClick = { onTrackClick(t) },
                        onFavorite = { onToggleFavorite(t.id) })
                    HorizontalDivider(thickness = 0.5.dp, color = colors.elevated.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 84.dp))
                }
            }
        }
    }
}

// ─── Songs Tab ────────────────────────────────────────────────────────────────
@Composable
fun SongsTab(
    tracks: List<Track>, favoriteIds: Set<Long>, sortOrder: SortOrder,
    showSortMenu: Boolean, onSortMenuToggle: (Boolean) -> Unit,
    onSortChange: (SortOrder) -> Unit, onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val colors = LocalAppColors.current
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("${tracks.size} songs", color = colors.textSecondary, fontSize = 12.sp)
            Box {
                Row(Modifier.clip(RoundedCornerShape(20.dp)).background(colors.card)
                    .clickable { onSortMenuToggle(true) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(when (sortOrder) {
                        SortOrder.DATE_ADDED -> "Recent"
                        SortOrder.TITLE_ASC  -> "A–Z"
                        SortOrder.TITLE_DESC -> "Z–A"
                        SortOrder.ARTIST     -> "Artist"
                        SortOrder.DURATION   -> "Duration"
                    }, color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { onSortMenuToggle(false) },
                    containerColor = colors.card) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(when (order) {
                                    SortOrder.DATE_ADDED -> "Recently Added"
                                    SortOrder.TITLE_ASC  -> "Title A–Z"
                                    SortOrder.TITLE_DESC -> "Title Z–A"
                                    SortOrder.ARTIST     -> "Artist"
                                    SortOrder.DURATION   -> "Duration"
                                }, color = if (sortOrder == order) colors.primary else colors.textPrimary)
                            },
                            onClick = { onSortChange(order); onSortMenuToggle(false) },
                            leadingIcon = {
                                if (sortOrder == order) Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
            items(tracks, key = { it.id }) { t ->
                TrackRow(t, isFavorite = t.id in favoriteIds, showFavIcon = false,
                    onClick = { onTrackClick(t, tracks) }, onFavorite = { onToggleFavorite(t.id) })
                HorizontalDivider(thickness = 0.5.dp, color = colors.elevated.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 84.dp))
            }
        }
    }
}

// ─── Track Row ────────────────────────────────────────────────────────────────
@Composable
fun TrackRow(track: Track, isFavorite: Boolean, showFavIcon: Boolean, onClick: () -> Unit, onFavorite: () -> Unit) {
    val colors = LocalAppColors.current
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(colors.card)) {
            AsyncImage(model = track.albumArtUri, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text("${track.artist} • ${track.album}", color = colors.textSecondary,
                fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (showFavIcon || isFavorite) {
            Spacer(Modifier.width(8.dp))
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                tint = if (isFavorite) VishaPink else colors.textMuted,
                modifier = Modifier.size(18.dp).clickable(onClick = onFavorite))
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = colors.card) {
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove Favorite" else "Add to Favorites", color = colors.textPrimary) },
                    onClick = { onFavorite(); menu = false },
                    leadingIcon = { Icon(if (isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite, null, tint = VishaPink) }
                )
                DropdownMenuItem(text = { Text("Add to Playlist", color = colors.textPrimary) },
                    onClick = { menu = false },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = colors.primary) })
            }
        }
    }
}

// ─── Albums Tab ───────────────────────────────────────────────────────────────
@Composable
fun AlbumsTab(tracks: List<Track>, onTrackClick: (Track, List<Track>) -> Unit) {
    val colors = LocalAppColors.current
    val albums = tracks.groupBy { it.album }
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        items(albums.entries.toList(), key = { it.key }) { (name, list) ->
            val first = list.first()
            Row(Modifier.fillMaxWidth().clickable { onTrackClick(first, list) }
                .padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(colors.card)) {
                    AsyncImage(model = first.albumArtUri, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${list.size} songs • ${first.artist}", color = colors.textSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = colors.textMuted)
            }
        }
    }
}

// ─── Artists Tab (click opens detail, not auto-play) ─────────────────────────
@Composable
fun ArtistsTab(tracks: List<Track>, onArtistClick: (String, List<Track>) -> Unit) {
    val colors  = LocalAppColors.current
    val artists = tracks.groupBy { it.artist }
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        items(artists.entries.toList(), key = { it.key }) { (artist, list) ->
            Row(Modifier.fillMaxWidth().clickable { onArtistClick(artist, list) }
                .padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(CircleShape).background(
                    Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.3f), colors.elevated))
                ), Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(artist, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${list.size} songs", color = colors.textSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = colors.textMuted)
            }
        }
    }
}

// ─── Playlists Tab ────────────────────────────────────────────────────────────
@Composable
fun PlaylistsTab(playlists: List<Playlist>, onCreate: () -> Unit, onDelete: (Long) -> Unit, onClick: (Playlist) -> Unit) {
    val colors = LocalAppColors.current
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        item {
            Row(Modifier.fillMaxWidth().clickable(onClick = onCreate).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)), Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = colors.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text("Create New Playlist", color = colors.primary,
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
        items(playlists, key = { it.id }) { pl ->
            var menuOpen by remember { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth().clickable { onClick(pl) }.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(colors.card), Alignment.Center) {
                    Icon(Icons.Default.QueueMusic, null, tint = colors.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(pl.name, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("${pl.tracks.size} songs", color = colors.textSecondary, fontSize = 12.sp)
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = colors.card) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(pl.id); menuOpen = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SuspiciousModifierThen")
private fun Modifier.border(w: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(border(w, color, shape))