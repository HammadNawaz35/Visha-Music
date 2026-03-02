package com.visha.musicplayer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tracks: List<Track>,
    recentlyPlayed: List<Track>,
    userProfile: UserProfile,
    unreadCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onNotificationClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    val preset  = LocalSelectedPreset.current

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else      -> "Good Night"
        }
    }

    val pullState = rememberPullToRefreshState()

    Box(Modifier.fillMaxSize()) {
        // Full-screen theme gradient
        Box(Modifier.fillMaxSize().background(bgBrush))

        // Decorative glowing orbs
        Box(
            Modifier.size(300.dp).align(Alignment.TopEnd).offset(80.dp, (-60).dp)
                .background(
                    Brush.radialGradient(listOf(
                        colors.primary.copy(alpha = 0.15f), Color.Transparent
                    )), CircleShape
                )
        )
        Box(
            Modifier.size(180.dp).align(Alignment.BottomStart).offset((-40).dp, 60.dp)
                .background(
                    Brush.radialGradient(listOf(
                        colors.primary.copy(alpha = 0.10f), Color.Transparent
                    )), CircleShape
                )
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = onRefresh,
            state        = pullState,
            modifier     = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // ── Header ──────────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 28.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            greeting.uppercase(),
                            color = colors.primary,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            userProfile.name.ifBlank { "Music Lover" },
                            color = colors.textPrimary,
                            fontSize = 30.sp, fontWeight = FontWeight.Black,
                            lineHeight = 34.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Avatar
                        Box(
                            Modifier.size(40.dp).clip(CircleShape)
                                .background(colors.card)
                                .border(1.5.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
                            Alignment.Center
                        ) {
                            if (userProfile.profilePicUri.isNotBlank()) {
                                AsyncImage(model = userProfile.profilePicUri, contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape))
                            } else {
                                Icon(Icons.Default.Person, null, tint = colors.primary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        // Bell
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(colors.card)
                                .border(0.5.dp, colors.border, CircleShape)
                                .clickable(onClick = onNotificationClick),
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsNone, null, tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp))
                            if (unreadCount > 0) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).offset((-4).dp, 4.dp)
                                        .size(16.dp).clip(CircleShape).background(colors.primary),
                                    Alignment.Center
                                ) {
                                    Text(
                                        if (unreadCount > 9) "9+" else "$unreadCount",
                                        color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Trending Now ─────────────────────────────────────────────
                SectionHeader(
                    title    = "Trending Now",
                    subtitle = "🔥 Hot this week",
                    action   = "See All",
                    onAction = onViewAllClick
                )
                Spacer(Modifier.height(14.dp))

                val featured = tracks.firstOrNull()
                if (featured != null) {
                    // Featured wide card
                    Box(
                        Modifier.fillMaxWidth().height(210.dp)
                            .padding(horizontal = 22.dp)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        AsyncImage(model = featured.albumArtUri, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                        // Overlay gradient
                        Box(Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(Color(0xDD000000), Color(0x44000000)))
                        ))

                        // Badge
                        Surface(
                            color = colors.primary,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                        ) {
                            Text("✦ TOP PICK", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp)
                        }

                        // Track info
                        Column(
                            Modifier.align(Alignment.BottomStart).padding(16.dp)
                        ) {
                            Text(featured.title, color = TextWhite, fontSize = 20.sp,
                                fontWeight = FontWeight.Black, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(featured.artist, color = TextGray, fontSize = 13.sp)
                        }

                        // Play button
                        Box(
                            Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)
                                .size(52.dp).clip(CircleShape)
                                .background(colors.primary)
                                .clickable { onTrackClick(featured, tracks) },
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White,
                                modifier = Modifier.size(28.dp))
                        }
                    }
                } else {
                    // Skeleton while loading
                    Box(
                        Modifier.fillMaxWidth().height(210.dp).padding(horizontal = 22.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.card),
                        Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary,
                                modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Scanning your library...", color = colors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Recently Played ──────────────────────────────────────────
                val displayList = recentlyPlayed.ifEmpty { tracks.take(12) }
                if (displayList.isNotEmpty()) {
                    SectionHeader(
                        title    = "Recently Played",
                        subtitle = "🎵 Pick up where you left off",
                        action   = "View All",
                        onAction = onViewAllClick
                    )
                    Spacer(Modifier.height(14.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayList) { track ->
                            RecentCard(track, onClick = { onTrackClick(track, displayList) })
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Quick Picks (more tracks) ─────────────────────────────────
                if (tracks.size > 5) {
                    SectionHeader(
                        title    = "Quick Picks",
                        subtitle = "⚡ Jump right in",
                        action   = null,
                        onAction = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    tracks.drop(1).take(5).forEach { track ->
                        QuickPickRow(
                            track  = track,
                            accent = colors.primary,
                            onClick = { onTrackClick(track, tracks) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    action: String?,
    onAction: () -> Unit
) {
    val colors = LocalAppColors.current
    Column(Modifier.padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            Column {
                Text(subtitle, color = colors.textSecondary, fontSize = 11.sp,
                    fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(2.dp))
                Text(title, color = colors.textPrimary, fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
            }
            if (action != null) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .clickable(onClick = onAction)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(action, color = colors.primary, fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun RecentCard(track: Track, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column(Modifier.width(130.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.size(130.dp).clip(RoundedCornerShape(16.dp))
                .background(colors.card)
        ) {
            AsyncImage(model = track.albumArtUri, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            // Bottom gradient
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(50.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000))))
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(track.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(track.artist, color = colors.textSecondary, fontSize = 11.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QuickPickRow(track: Track, accent: Color, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(colors.card)) {
            AsyncImage(model = track.albumArtUri, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = colors.textSecondary, fontSize = 12.sp)
        }
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
            Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}