package com.visha.musicplayer.presentation.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visha.musicplayer.domain.model.PlayerState
import com.visha.musicplayer.domain.model.RepeatMode as AppRepeatMode
import com.visha.musicplayer.domain.model.Track
import com.visha.musicplayer.presentation.components.toTimeString
import com.visha.musicplayer.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    playerState: PlayerState,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekForward10: () -> Unit,
    onSeekBack10: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onDeleteTrack: () -> Unit,
    onSeekToQueueIndex: (Int) -> Unit,     // NEW: seek to specific queue item
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors  = LocalAppColors.current
    val preset  = LocalSelectedPreset.current
    val track   = playerState.currentTrack

    // Vinyl rotation
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            rotation.animateTo(
                rotation.value + 3600f,
                infiniteRepeatable(tween(80_000, easing = LinearEasing), RepeatMode.Restart)
            )
        } else rotation.stop()
    }

    var showSpeedMenu    by remember { mutableStateOf(false) }
    var showSleepMenu    by remember { mutableStateOf(false) }
    var show3DotMenu     by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSongInfo     by remember { mutableStateOf(false) }
    var showUpNext       by remember { mutableStateOf(false) }

    // Background: blended from theme
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                colors.background,
                colors.surface,
                colors.background.copy(red = colors.background.red + 0.04f)
            ))
        )
    ) {
        // Glow behind artwork
        Box(
            Modifier.size(320.dp).align(Alignment.TopCenter).offset(y = 20.dp)
                .background(
                    Brush.radialGradient(listOf(
                        colors.primary.copy(alpha = 0.3f), Color.Transparent
                    )), CircleShape
                )
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))

            // ── Top bar ─────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(colors.card.copy(alpha = 0.6f)),
                        Alignment.Center) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", color = colors.primary, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Queue", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(colors.card.copy(alpha = 0.6f))
                        .clickable { show3DotMenu = true }, Alignment.Center) {
                        Icon(Icons.Default.MoreVert, null, tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = show3DotMenu, onDismissRequest = { show3DotMenu = false },
                        containerColor = colors.card) {
                        PlayMenuOption(Icons.Default.Info, "Song Info") { showSongInfo = true; show3DotMenu = false }
                        PlayMenuOption(Icons.Default.Share, "Share") { track?.let { shareTrack(context, it) }; show3DotMenu = false }
                        PlayMenuOption(Icons.Default.RingVolume, "Set as Ringtone") { track?.let { setAsRingtone(context, it) }; show3DotMenu = false }
                        PlayMenuOption(Icons.Default.Equalizer, "Equalizer") { openEqualizer(context); show3DotMenu = false }
                        Divider(color = colors.elevated)
                        DropdownMenuItem(
                            text = { Text("Delete Song", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showDeleteDialog = true; show3DotMenu = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Album Art – Rotating Vinyl Style ────────────────────────────
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                // Shadow glow
                Box(Modifier.size(280.dp).shadow(32.dp, CircleShape, spotColor = colors.primary))
                // Outer vinyl ring
                Box(
                    Modifier.size(260.dp).rotate(rotation.value).clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(
                                colors.elevated, colors.card, colors.background
                            ))
                        )
                        .border(1.5.dp, colors.primary.copy(alpha = 0.3f), CircleShape),
                    Alignment.Center
                ) {
                    // Grooves
                    Box(Modifier.size(220.dp).border(0.5.dp, Color.White.copy(alpha = 0.05f), CircleShape))
                    Box(Modifier.size(180.dp).border(0.5.dp, Color.White.copy(alpha = 0.04f), CircleShape))
                    Box(Modifier.size(140.dp).border(0.5.dp, Color.White.copy(alpha = 0.03f), CircleShape))
                    // Art
                    AsyncImage(model = track?.albumArtUri, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(200.dp).clip(CircleShape))
                    // Center spindle
                    Box(Modifier.size(22.dp).clip(CircleShape)
                        .background(colors.background)
                        .border(1.dp, colors.primary.copy(alpha = 0.6f), CircleShape))
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Track title + fav ────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(track?.title ?: "No Track", color = colors.textPrimary,
                        fontWeight = FontWeight.Black, fontSize = 22.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(track?.artist ?: "—", color = colors.primary,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) VishaPink else colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Seekbar ──────────────────────────────────────────────────────
            val prog = if (playerState.duration > 0)
                (playerState.progress.toFloat() / playerState.duration).coerceIn(0f, 1f) else 0f

            Slider(
                value = prog,
                onValueChange = { onSeek((it * playerState.duration).toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.elevated
                )
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(playerState.progress.toTimeString(), color = colors.textSecondary, fontSize = 12.sp)
                Text(playerState.duration.toTimeString(), color = colors.textSecondary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(14.dp))

            // ── Controls ─────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(Icons.Default.Shuffle, null,
                        tint = if (playerState.shuffleEnabled) colors.primary else colors.textMuted,
                        modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.SkipPrevious, null, tint = colors.textPrimary,
                        modifier = Modifier.size(32.dp))
                }
                // Main play button
                Box(
                    Modifier.size(68.dp).clip(CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = colors.primary)
                        .background(preset.gradient)
                        .clickable(onClick = onPlayPause),
                    Alignment.Center
                ) {
                    Icon(
                        if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.White, modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onSkipNext, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.SkipNext, null, tint = colors.textPrimary,
                        modifier = Modifier.size(32.dp))
                }
                // Repeat (replaced Lyrics)
                IconButton(onClick = onToggleRepeat) {
                    val (repIcon, repTint) = when (playerState.repeatMode) {
                        AppRepeatMode.ONE -> Icons.Default.RepeatOne to colors.primary
                        AppRepeatMode.ALL -> Icons.Default.Repeat    to colors.primary
                        AppRepeatMode.OFF -> Icons.Default.Repeat    to colors.textMuted
                    }
                    Icon(repIcon, null, tint = repTint, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Bottom bar: seek 10, speed, sleep, up next ───────────────────
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card.copy(alpha = 0.8f))
                    .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                    PlayChip(Icons.Default.Replay10, "−10s") { onSeekBack10() }
                    PlayChip(Icons.Default.Forward10, "+10s") { onSeekForward10() }

                    Box {
                        PlayChip(Icons.Default.Speed,
                            if (playerState.playbackSpeed == 1f) "Speed" else "${playerState.playbackSpeed}×"
                        ) { showSpeedMenu = true }
                        DropdownMenu(expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            containerColor = colors.card) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s}×", color = if (playerState.playbackSpeed == s) colors.primary else colors.textPrimary) },
                                    onClick = { onSetSpeed(s); showSpeedMenu = false }
                                )
                            }
                        }
                    }

                    Box {
                        PlayChip(Icons.Default.Bedtime,
                            if (playerState.sleepTimerMinutes != null) "${playerState.sleepTimerMinutes}m" else "Sleep"
                        ) { showSleepMenu = true }
                        DropdownMenu(expanded = showSleepMenu,
                            onDismissRequest = { showSleepMenu = false },
                            containerColor = colors.card) {
                            listOf(10 to "10 min", 20 to "20 min", 30 to "30 min", 60 to "1 hour")
                                .forEach { (m, l) ->
                                    DropdownMenuItem(text = { Text(l, color = colors.textPrimary) },
                                        onClick = { onSetSleepTimer(m); showSleepMenu = false })
                                }
                            if (playerState.sleepTimerMinutes != null) {
                                DropdownMenuItem(
                                    text = { Text("Cancel", color = MaterialTheme.colorScheme.error) },
                                    onClick = { onSetSleepTimer(0); showSleepMenu = false }
                                )
                            }
                        }
                    }

                    PlayChip(Icons.Default.QueueMusic, "Up Next") { showUpNext = true }
                }
            }
        }
    }

    // ── Up Next Sheet ────────────────────────────────────────────────────────
    if (showUpNext) {
        ModalBottomSheet(
            onDismissRequest = { showUpNext = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Text("Up Next", color = colors.textPrimary, fontWeight = FontWeight.Black,
                fontSize = 20.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp))
            HorizontalDivider(color = colors.elevated)
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)) {
                itemsIndexed(playerState.queue) { idx, qTrack ->
                    val isCurrent = idx == playerState.currentIndex
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                // Seek to this queue index – plays that song
                                onSeekToQueueIndex(idx)
                                showUpNext = false
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                            .background(colors.card)) {
                            AsyncImage(model = qTrack.albumArtUri, contentDescription = null,
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            if (isCurrent) {
                                Box(Modifier.fillMaxSize()
                                    .background(colors.primary.copy(alpha = 0.5f)), Alignment.Center) {
                                    Icon(Icons.Default.GraphicEq, null, tint = Color.White,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(qTrack.title,
                                color = if (isCurrent) colors.primary else colors.textPrimary,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(qTrack.artist, color = colors.textSecondary, fontSize = 12.sp)
                        }
                        Text(
                            "${idx + 1}", color = if (isCurrent) colors.primary else colors.textMuted,
                            fontSize = 12.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Song Info Dialog ─────────────────────────────────────────────────────
    if (showSongInfo && track != null) {
        AlertDialog(
            onDismissRequest = { showSongInfo = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Song Info", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Title" to track.title, "Artist" to track.artist,
                        "Album" to track.album, "Duration" to track.duration.toTimeString())
                        .forEach { (k, v) ->
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(k, color = colors.textSecondary, fontSize = 13.sp)
                                Text(v, color = colors.textPrimary, fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.widthIn(max = 190.dp),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                }
            },
            confirmButton = { TextButton(onClick = { showSongInfo = false }) { Text("Close", color = colors.primary) } }
        )
    }

    // ── Safe Delete Dialog ───────────────────────────────────────────────────
    if (showDeleteDialog && track != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Song", color = colors.textPrimary) },
            text = { Text("Remove \"${track.title}\" from your device?\nThis cannot be undone.",
                color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    // Call delete; navigation back is handled by the caller via deleteEvent
                    onDeleteTrack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

// ── Small chip button ──────────────────────────────────────────────────────────
@Composable
private fun PlayChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = colors.textMuted, fontSize = 9.sp, letterSpacing = 0.3.sp)
    }
}

@Composable
private fun PlayMenuOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    DropdownMenuItem(
        text = { Text(label, color = colors.textPrimary) },
        leadingIcon = { Icon(icon, null, tint = colors.primary) },
        onClick = onClick
    )
}

// ── Intent helpers ─────────────────────────────────────────────────────────────
private fun setAsRingtone(ctx: Context, track: Track) {
    try {
        if (Settings.System.canWrite(ctx)) {
            android.media.RingtoneManager.setActualDefaultRingtoneUri(
                ctx, android.media.RingtoneManager.TYPE_RINGTONE, track.uri)
            Toast.makeText(ctx, "Set as ringtone", Toast.LENGTH_SHORT).show()
        } else {
            ctx.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:${ctx.packageName}")))
        }
    } catch (e: Exception) { Toast.makeText(ctx, "Cannot set ringtone", Toast.LENGTH_SHORT).show() }
}

private fun shareTrack(ctx: Context, track: Track) {
    ctx.startActivity(Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"; putExtra(Intent.EXTRA_STREAM, track.uri)
            putExtra(Intent.EXTRA_TEXT, "${track.title} by ${track.artist}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share Song"))
}

// Fixed equalizer: use AudioEffect intent with session ID
private fun openEqualizer(ctx: Context) {
    // Try AudioEffect intent first (works on most devices)
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioManager.ERROR) // 0 = global session
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        ctx.startActivity(intent)
    } catch (e1: Exception) {
        // Fallback: generic media action
        try {
            ctx.startActivity(Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e2: Exception) {
            Toast.makeText(ctx, "No equalizer app found on this device", Toast.LENGTH_LONG).show()
        }
    }
}