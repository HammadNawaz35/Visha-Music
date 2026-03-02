package com.visha.musicplayer.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visha.musicplayer.presentation.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val bgGradient: Brush,
    val accentColor: Color
)

private val pages = listOf(
    OnboardingPage(
        "Welcome to Visha", "YOUR MUSIC UNIVERSE",
        "Experience your music library like never before — beautiful, immersive, and always in sync with your vibe.",
        Icons.Default.MusicNote,
        Brush.verticalGradient(listOf(PurpleDeep, PurpleMid, Color(0xFF200845))),
        PurpleAccent
    ),
    OnboardingPage(
        "Stunning Visuals", "IMMERSIVE EXPERIENCE",
        "Rotating vinyl art, glossy glassmorphism, and dynamic gradient themes that follow your mood.",
        Icons.Default.Palette,
        Brush.verticalGradient(listOf(Color(0xFF040E28), NavyDeep, Color(0xFF071830))),
        Color(0xFF42A5F5)
    ),
    OnboardingPage(
        "Everything Built In", "PERFECTLY ORGANIZED",
        "Smart playlists, favorites, sleep timers, playback speed, notifications, and full offline support.",
        Icons.Default.LibraryMusic,
        Brush.verticalGradient(listOf(Color(0xFF1A0010), Color(0xFF2D0020), Color(0xFF150015))),
        Color(0xFFEC407A)
    )
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val scope      = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { idx ->
            OnboardingPageContent(page = pages[idx])
        }

        // Dot indicators
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { idx ->
                val selected = pagerState.currentPage == idx
                val accent   = pages[idx].accentColor
                val w by animateDpAsState(if (selected) 28.dp else 8.dp, label = "dot")
                Box(Modifier.height(8.dp).width(w).clip(CircleShape)
                    .background(if (selected) accent else Color.White.copy(alpha = 0.3f)))
            }
        }

        // CTA buttons
        val isLast = pagerState.currentPage == pages.size - 1
        val accent = pages[pagerState.currentPage].accentColor

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isLast) onDone()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    if (isLast) "Let's Go!" else "Continue",
                    color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    if (isLast) Icons.Default.RocketLaunch else Icons.Default.ArrowForward,
                    null, tint = Color.White, modifier = Modifier.size(20.dp)
                )
            }
            if (!isLast) {
                TextButton(onClick = onDone) {
                    Text("Skip for now", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val inf = rememberInfiniteTransition(label = "ob")
    val rot by inf.animateFloat(0f, 360f,
        infiniteRepeatable(tween(22_000, easing = LinearEasing), RepeatMode.Restart), label = "rot")
    val pulse by inf.animateFloat(0.92f, 1.09f,
        infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "pulse")

    Box(Modifier.fillMaxSize().background(page.bgGradient)) {

        // Background decoration rings
        Box(Modifier.size(340.dp).align(Alignment.TopCenter).offset(y = (-40).dp)
            .rotate(rot).border(1.dp, page.accentColor.copy(alpha = 0.25f), CircleShape))
        Box(Modifier.size(240.dp).align(Alignment.TopCenter).offset(y = 10.dp)
            .rotate(-rot * 0.7f).border(0.6.dp, page.accentColor.copy(alpha = 0.15f), CircleShape))
        Box(Modifier.size(160.dp).align(Alignment.TopCenter).offset(y = 50.dp)
            .background(page.accentColor.copy(alpha = 0.07f), CircleShape))

        Column(
            Modifier.fillMaxSize().padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing icon
            Box(
                Modifier.size(140.dp).scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                // Glow halo
                Box(Modifier.size(130.dp).background(
                    Brush.radialGradient(listOf(
                        page.accentColor.copy(alpha = 0.4f), Color.Transparent
                    )), CircleShape
                ))
                // Card
                Box(
                    Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(listOf(
                                page.accentColor.copy(alpha = 0.7f),
                                page.accentColor.copy(alpha = 0.25f)
                            ))
                        )
                        .border(1.5.dp,
                            Brush.linearGradient(listOf(
                                page.accentColor, page.accentColor.copy(alpha = 0.2f)
                            )),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
            }

            Spacer(Modifier.height(52.dp))

            Text(page.subtitle, color = page.accentColor, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 3.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(12.dp))

            Text(page.title, color = TextWhite, fontSize = 34.sp,
                fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 42.sp)

            Spacer(Modifier.height(18.dp))

            Text(page.description, color = TextGray, fontSize = 15.sp,
                textAlign = TextAlign.Center, lineHeight = 24.sp)

            Spacer(Modifier.height(120.dp))
        }
    }
}
