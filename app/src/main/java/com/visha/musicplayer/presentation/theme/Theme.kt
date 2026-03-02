package com.visha.musicplayer.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand ─────────────────────────────────────────────────────────────────────
val VishaOrange      = Color(0xFFFF6B35)
val VishaOrangeLight = Color(0xFFFF8C5A)
val VishaPink        = Color(0xFFFF2D78)

// Purple Glossy (default)
val PurpleDeep       = Color(0xFF1A0A2E)
val PurpleMid        = Color(0xFF2D1254)
val PurpleCard       = Color(0xFF3A1866)
val PurpleElevated   = Color(0xFF4F2490)
val PurpleAccent     = Color(0xFF9C27B0)
val PurpleAccentLight= Color(0xFFCE93D8)

// Navy
val NavyDeep         = Color(0xFF0A1628)
val NavyMid          = Color(0xFF0D1F3C)
val NavyCard         = Color(0xFF1A2F52)
val NavyElevated     = Color(0xFF1F3660)

// AMOLED
val AmoledBg         = Color.Black
val AmoledCard       = Color(0xFF0D0D0D)
val AmoledElevated   = Color(0xFF181818)

// Light
val LightBg          = Color(0xFFF2F2F7)
val LightSurface     = Color(0xFFFFFFFF)
val LightCard        = Color(0xFFEEEEF6)

// Text
val TextWhite        = Color(0xFFFFFFFF)
val TextGray         = Color(0xFFB0BEC5)
val TextMuted        = Color(0xFF607D8B)

// Glass
val GlassWhite10     = Color(0x1AFFFFFF)
val GlassWhite20     = Color(0x33FFFFFF)
val GlassBorder      = Color(0x40FFFFFF)
val GlassNavy        = Color(0x990A1628)

// ── Color Presets ─────────────────────────────────────────────────────────────
data class ColorPreset(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val gradient: Brush,
    val bgGradient: Brush
)

val colorPresets = listOf(
    ColorPreset(
        "Neon Purple", PurpleAccent, PurpleAccentLight,
        Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))),
        Brush.verticalGradient(listOf(PurpleDeep, PurpleMid, Color(0xFF1E0740), PurpleDeep))
    ),
    ColorPreset(
        "Visha Orange", VishaOrange, VishaOrangeLight,
        Brush.linearGradient(listOf(VishaOrange, VishaOrangeLight)),
        Brush.verticalGradient(listOf(Color(0xFF1A0800), Color(0xFF2D1200), Color(0xFF1A0600), NavyDeep))
    ),
    ColorPreset(
        "Ocean Blue", Color(0xFF2196F3), Color(0xFF64B5F6),
        Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
        Brush.verticalGradient(listOf(Color(0xFF040E28), NavyDeep, Color(0xFF071830)))
    ),
    ColorPreset(
        "Mint Teal", Color(0xFF00BCD4), Color(0xFF4DD0E1),
        Brush.linearGradient(listOf(Color(0xFF00838F), Color(0xFF26C6DA))),
        Brush.verticalGradient(listOf(Color(0xFF00121A), Color(0xFF001E28), Color(0xFF001218)))
    ),
    ColorPreset(
        "Rose Gold", Color(0xFFE91E63), Color(0xFFF48FB1),
        Brush.linearGradient(listOf(Color(0xFFC2185B), Color(0xFFEC407A))),
        Brush.verticalGradient(listOf(Color(0xFF1A0010), Color(0xFF2D0020), Color(0xFF150010)))
    ),
)

// ── CompositionLocals ─────────────────────────────────────────────────────────
val LocalAppColors        = staticCompositionLocalOf { AppColors() }
val LocalGlossyBackground = staticCompositionLocalOf<Brush> {
    Brush.verticalGradient(listOf(PurpleDeep, PurpleMid, Color(0xFF1E0740), PurpleDeep))
}
val LocalSelectedPreset   = staticCompositionLocalOf { colorPresets[0] }

// ── AppColors ─────────────────────────────────────────────────────────────────
data class AppColors(
    val primary: Color       = PurpleAccent,
    val primaryLight: Color  = PurpleAccentLight,
    val background: Color    = PurpleDeep,
    val surface: Color       = PurpleMid,
    val card: Color          = PurpleCard,
    val elevated: Color      = PurpleElevated,
    val textPrimary: Color   = TextWhite,
    val textSecondary: Color = TextGray,
    val textMuted: Color     = TextMuted,
    val glassLight: Color    = GlassWhite10,
    val glassMedium: Color   = GlassWhite20,
    val border: Color        = GlassBorder,
    val isLight: Boolean     = false
)

enum class AppThemeMode { NAVY, AMOLED, LIGHT }

fun buildAppColors(mode: AppThemeMode, preset: ColorPreset): AppColors = when (mode) {
    AppThemeMode.NAVY   -> AppColors(
        primary = preset.primary, primaryLight = preset.secondary,
        background = NavyDeep, surface = NavyMid,
        card = NavyCard, elevated = NavyElevated,
        textPrimary = TextWhite, textSecondary = TextGray,
        border = GlassBorder, isLight = false
    )
    AppThemeMode.AMOLED -> AppColors(
        primary = preset.primary, primaryLight = preset.secondary,
        background = AmoledBg, surface = Color(0xFF080808),
        card = AmoledCard, elevated = AmoledElevated,
        textPrimary = TextWhite, textSecondary = TextGray,
        border = Color(0x28FFFFFF), isLight = false
    )
    AppThemeMode.LIGHT  -> AppColors(
        primary = preset.primary, primaryLight = preset.secondary,
        background = LightBg, surface = LightSurface,
        card = LightCard, elevated = Color(0xFFE0E0EA),
        textPrimary = Color(0xFF0D0D1A), textSecondary = Color(0xFF455A64),
        textMuted = Color(0xFF90A4AE), border = Color(0x28000000),
        isLight = true
    )
}

// Returns the correct full-screen gradient for mode+preset combination
fun buildGlossyBackground(
    mode: AppThemeMode,
    preset: ColorPreset,
    customBgUri: String
): Brush {
    if (customBgUri.isNotBlank()) return preset.bgGradient
    return when (mode) {
        AppThemeMode.NAVY   -> Brush.verticalGradient(
            listOf(NavyDeep, Color(0xFF0F2040), NavyMid, Color(0xFF0A1830))
        )
        AppThemeMode.AMOLED -> Brush.verticalGradient(
            listOf(Color.Black, Color(0xFF06000E), Color(0xFF020008))
        )
        AppThemeMode.LIGHT  -> Brush.verticalGradient(
            listOf(Color(0xFFF7F0FF), LightBg, Color(0xFFEDE7F6))
        )
    }
}

val MusicTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.Black,    fontSize = 36.sp, letterSpacing = (-1).sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.ExtraBold,fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 15.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, letterSpacing = 0.5.sp)
)

@Composable
fun VishaPlayerTheme(
    themeMode: AppThemeMode = AppThemeMode.NAVY,
    selectedPreset: ColorPreset = colorPresets[0],
    customBgUri: String = "",
    content: @Composable () -> Unit
) {
    val appColors = remember(themeMode, selectedPreset) { buildAppColors(themeMode, selectedPreset) }
    val glossyBg  = remember(themeMode, selectedPreset, customBgUri) {
        buildGlossyBackground(themeMode, selectedPreset, customBgUri)
    }
    val cs = if (appColors.isLight) lightColorScheme(
        primary = appColors.primary, onPrimary = Color.White,
        primaryContainer = appColors.primaryLight.copy(alpha = 0.2f),
        background = appColors.background, onBackground = appColors.textPrimary,
        surface = appColors.surface, onSurface = appColors.textPrimary,
        surfaceVariant = appColors.card, onSurfaceVariant = appColors.textSecondary,
        secondaryContainer = appColors.card, onSecondaryContainer = appColors.textPrimary
    ) else darkColorScheme(
        primary = appColors.primary, onPrimary = Color.White,
        primaryContainer = appColors.primary.copy(alpha = 0.2f),
        background = appColors.background, onBackground = appColors.textPrimary,
        surface = appColors.surface, onSurface = appColors.textPrimary,
        surfaceVariant = appColors.card, onSurfaceVariant = appColors.textSecondary,
        secondaryContainer = appColors.card, onSecondaryContainer = appColors.textPrimary
    )
    CompositionLocalProvider(
        LocalAppColors        provides appColors,
        LocalGlossyBackground provides glossyBg,
        LocalSelectedPreset   provides selectedPreset
    ) {
        MaterialTheme(colorScheme = cs, typography = MusicTypography, content = content)
    }
}