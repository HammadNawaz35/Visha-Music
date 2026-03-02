package com.visha.musicplayer.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visha.musicplayer.domain.model.PlayerState
import com.visha.musicplayer.presentation.theme.*

// ─── GlassSurface ─────────────────────────────────────────────────────────
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glassBg: Color = GlassWhite10,
    borderColor: Color = GlassBorder,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(
                    glassBg,
                    colors.card.copy(alpha = 0.6f)
                )), shape
            )
            .border(0.5.dp, borderColor, shape),
        content = content
    )
}

// ─── Vinyl Decorative Background Element ─────────────────────────────────
@Composable
fun VinylDecoration(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.elevated.copy(alpha = 0.3f))
            .border(1.dp, colors.primary.copy(alpha = 0.1f), CircleShape)
    )
}

// ─── MiniPlayer ──────────────────────────────────────────────────────────
@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    AnimatedVisibility(
        visible = playerState.currentTrack != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            glassBg = GlassNavy
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(8.dp))
                            .background(colors.elevated)
                    ) {
                        AsyncImage(
                            model = playerState.currentTrack?.albumArtUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = playerState.currentTrack?.title ?: "",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                        )
                        Text(
                            text = playerState.currentTrack?.artist ?: "",
                            color = colors.primary,
                            fontSize = 12.sp, maxLines = 1
                        )
                    }
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null, tint = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(Icons.Default.SkipNext, null, tint = colors.textPrimary)
                    }
                }
                if (playerState.duration > 0) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (playerState.progress.toFloat() / playerState.duration).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                        color = colors.primary,
                        trackColor = colors.elevated
                    )
                }
            }
        }
    }
}

// ─── Time helpers ──────────────────────────────────────────────────────────
fun Long.toTimeString(): String {
    val s = this / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
