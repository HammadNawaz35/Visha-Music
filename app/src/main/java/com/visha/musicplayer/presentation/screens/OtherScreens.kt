package com.visha.musicplayer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─── Favorites Screen ─────────────────────────────────────────────────────────
@Composable
fun FavoritesScreen(
    favoriteTracks: List<Track>,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Box(Modifier.size(220.dp).align(Alignment.TopEnd).offset(50.dp, (-30).dp)
            .background(Brush.radialGradient(listOf(VishaPink.copy(alpha = 0.12f), Color.Transparent)), CircleShape))

        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(VishaPink.copy(alpha = 0.15f))
                    .border(1.dp, VishaPink.copy(alpha = 0.4f), CircleShape), Alignment.Center) {
                    Icon(Icons.Default.Favorite, null, tint = VishaPink, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("FAVORITES", color = VishaPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("My Likes", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.weight(1f))
                if (favoriteTracks.isNotEmpty()) {
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(VishaPink.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("${favoriteTracks.size} songs", color = VishaPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (favoriteTracks.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(90.dp).clip(CircleShape).background(VishaPink.copy(alpha = 0.1f))
                            .border(1.dp, VishaPink.copy(alpha = 0.2f), CircleShape), Alignment.Center) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = VishaPink, modifier = Modifier.size(44.dp))
                        }
                        Text("No favorites yet", color = colors.textSecondary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tap ♥ on any song to add it here", color = colors.textMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
                    items(favoriteTracks, key = { it.id }) { t ->
                        TrackRow(t, isFavorite = true, showFavIcon = true,
                            onClick = { onTrackClick(t, favoriteTracks) },
                            onFavorite = { onToggleFavorite(t.id) })
                        HorizontalDivider(thickness = 0.5.dp, color = colors.elevated.copy(alpha = 0.4f),
                            modifier = Modifier.padding(start = 84.dp))
                    }
                }
            }
        }
    }
}

// ─── Notifications Screen – Modern Cards ─────────────────────────────────────
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit,
    onRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    val fmt     = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                    Column {
                        Text("NOTIFICATIONS", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Inbox", color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(90.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.1f))
                            .border(1.dp, colors.primary.copy(alpha = 0.2f), CircleShape), Alignment.Center) {
                            Icon(Icons.Default.NotificationsNone, null, tint = colors.primary, modifier = Modifier.size(44.dp))
                        }
                        Text("All caught up!", color = colors.textSecondary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("No new notifications", color = colors.textMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationCard(notif, fmt) { onRead(notif.id) }
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification, fmt: SimpleDateFormat, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val isUnread = !notif.isRead

    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isUnread) colors.card.copy(alpha = 0.95f)
                else colors.surface.copy(alpha = 0.6f)
            )
            .then(
                if (isUnread) Modifier.border(0.5.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        // Colored accent bar on left if unread
        if (isUnread) {
            Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .background(colors.primary))
        }
        Row(Modifier.padding(start = if (isUnread) 16.dp else 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top) {
            // Image or icon
            Box(Modifier.size(54.dp).clip(RoundedCornerShape(14.dp))
                .background(colors.elevated), Alignment.Center) {
                if (!notif.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = notif.imageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)))
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.primary.copy(alpha = 0.1f)))
                    ), Alignment.Center) {
                        Icon(Icons.Default.MusicNote, null, tint = colors.primary, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Text(notif.title, color = colors.textPrimary,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(fmt.format(Date(notif.timestamp)), color = colors.textMuted, fontSize = 10.sp)
                        if (isUnread) Box(Modifier.size(7.dp).clip(CircleShape).background(colors.primary))
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(notif.description, color = colors.textSecondary, fontSize = 13.sp, maxLines = 3, lineHeight = 18.sp)
            }
        }
    }
}

// ─── Track Selector (Playlist multi-select) ───────────────────────────────────
@Composable
fun TrackSelectorScreen(
    playlistName: String,
    allTracks: List<Track>,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                    Column {
                        Text("ADD SONGS", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(playlistName, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selectedIds.size < allTracks.size) {
                        TextButton(onClick = onSelectAll) { Text("Select All", color = colors.primary, fontSize = 13.sp) }
                    }
                    if (selectedIds.isNotEmpty()) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(colors.primary), Alignment.Center) {
                            Text("${selectedIds.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 130.dp)) {
                itemsIndexed(allTracks) { _, track ->
                    val sel = track.id in selectedIds
                    Row(
                        Modifier.fillMaxWidth().clickable { onToggleSelect(track.id) }
                            .background(if (sel) colors.primary.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox circle
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(if (sel) colors.primary else Color.Transparent)
                                .border(1.5.dp, if (sel) colors.primary else colors.border, CircleShape),
                            Alignment.Center
                        ) {
                            if (sel) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(colors.card)) {
                            AsyncImage(model = track.albumArtUri, contentDescription = null,
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.title,
                                color = if (sel) colors.primary else colors.textPrimary,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(track.artist, color = colors.textSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Floating confirm button
        if (selectedIds.isNotEmpty()) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Default.PlaylistAdd, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Add ${selectedIds.size} Song${if (selectedIds.size > 1) "s" else ""}",
                    color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(border(w, color, shape))