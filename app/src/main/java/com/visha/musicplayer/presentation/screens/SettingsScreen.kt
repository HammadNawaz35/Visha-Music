package com.visha.musicplayer.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.presentation.theme.*

@Composable
fun SettingsScreen(
    themeConfig: ThemeConfig,
    userProfile: UserProfile,
    cacheSize: String,
    onSaveTheme: (ThemeConfig) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onClearCache: () -> Unit,
    onBack: () -> Unit
) {
    var page by remember { mutableStateOf<String?>(null) }
    when (page) {
        "theme"    -> ThemeScreen(themeConfig, onSave = { onSaveTheme(it); page = null }, onBack = { page = null })
        "profile"  -> ProfileScreen(userProfile, onSave = { onSaveProfile(it); page = null }, onBack = { page = null })
        "about"    -> AboutScreen(onBack = { page = null })
        "feedback" -> FeedbackScreen(onBack = { page = null })
        else       -> SettingsMain(themeConfig, userProfile, cacheSize,
            onNavigate = { page = it }, onClearCache = onClearCache, onBack = onBack)
    }
}

@Composable
fun SettingsMain(
    themeConfig: ThemeConfig, userProfile: UserProfile, cacheSize: String,
    onNavigate: (String) -> Unit, onClearCache: () -> Unit, onBack: () -> Unit
) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Box(Modifier.size(250.dp).align(Alignment.BottomEnd).offset(60.dp, 40.dp)
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(listOf(
                    colors.primary.copy(alpha = 0.08f), Color.Transparent
                )), CircleShape
            ))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary)
                }
                Column {
                    Text("SETTINGS", color = colors.primary, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Preferences", color = colors.textPrimary, fontSize = 22.sp,
                        fontWeight = FontWeight.Black)
                }
            }

            // Profile card
            SettingsCardGroup(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onNavigate("profile") }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(64.dp).clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(2.dp, colors.primary.copy(alpha = 0.4f), CircleShape), Alignment.Center) {
                        if (userProfile.profilePicUri.isNotBlank()) {
                            AsyncImage(model = userProfile.profilePicUri, contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(userProfile.name.ifBlank { "Set your name" }, color = colors.textPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(userProfile.bio.ifBlank { "Tap to edit profile..." }, color = colors.textSecondary,
                            fontSize = 13.sp, maxLines = 1)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = colors.primary)
                }
            }

            Spacer(Modifier.height(22.dp))
            SettingsSectionLabel("APPEARANCE")
            SettingsCardGroup {
                SettingsTile(Icons.Default.Palette, "Theme & Colors",
                    colorPresets.getOrNull(themeConfig.accentPresetIndex)?.name ?: "Neon Purple") { onNavigate("theme") }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("STORAGE")
            SettingsCardGroup {
                SettingsTile(Icons.Default.CleaningServices, "Clear Cache", cacheSize) { onClearCache() }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("SUPPORT")
            SettingsCardGroup {
                SettingsTile(Icons.Default.Info, "About Visha", "Version & developer info") { onNavigate("about") }
                HorizontalDivider(thickness = 0.5.dp, color = colors.elevated)
                SettingsTile(Icons.Default.Feedback, "Send Feedback", "Suggestions or bug reports") { onNavigate("feedback") }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun ThemeScreen(current: ThemeConfig, onSave: (ThemeConfig) -> Unit, onBack: () -> Unit) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    var selectedMode      by remember { mutableStateOf(current.themeMode) }
    var selectedPresetIdx by remember { mutableIntStateOf(current.accentPresetIndex) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onSave(current.copy(customBackgroundUri = it.toString()))
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                Column {
                    Text("APPEARANCE", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Theme & Colors", color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            SettingsSectionLabel("BACKGROUND STYLE")
            SettingsCardGroup {
                listOf(
                    Triple("NAVY",  "Navy Deep", NavyDeep),
                    Triple("AMOLED","AMOLED Black", Color.Black),
                    Triple("LIGHT", "Light",     LightBg)
                ).forEachIndexed { i, (key, label, swatch) ->
                    Row(Modifier.fillMaxWidth().clickable { selectedMode = key }
                        .padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(swatch)
                            .then(if (selectedMode == key)
                                Modifier.border(2.5.dp, colors.primary, CircleShape) else Modifier))
                        Spacer(Modifier.width(14.dp))
                        Text(label, color = colors.textPrimary, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                        if (selectedMode == key)
                            Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    }
                    if (i < 2) HorizontalDivider(thickness = 0.5.dp, color = colors.elevated)
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("ACCENT COLOR")
            SettingsCardGroup {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    colorPresets.forEachIndexed { idx, preset ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedPresetIdx = idx }) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(preset.primary)
                                    .then(if (selectedPresetIdx == idx)
                                        Modifier.border(2.5.dp, Color.White, CircleShape) else Modifier),
                                Alignment.Center
                            ) {
                                if (selectedPresetIdx == idx)
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(preset.name.split(" ").first(), color = colors.textSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("CUSTOM BACKGROUND")
            SettingsCardGroup {
                Row(Modifier.fillMaxWidth().clickable { imagePicker.launch("image/*") }
                    .padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Custom Wallpaper", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Text(if (current.customBackgroundUri.isNotBlank()) "Set" else "Not set",
                            color = colors.textSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = colors.textMuted)
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { onSave(ThemeConfig(selectedMode, selectedPresetIdx, current.glassmorphismEnabled, current.customBackgroundUri)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) { Text("Apply Theme", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp) }
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun ProfileScreen(current: UserProfile, onSave: (UserProfile) -> Unit, onBack: () -> Unit) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    var name by remember { mutableStateOf(current.name) }
    var bio  by remember { mutableStateOf(current.bio) }
    var pic  by remember { mutableStateOf(current.profilePicUri) }
    val context = LocalContext.current
    val imgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pic = it.toString()
        }
    }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                Column {
                    Text("PROFILE", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Edit Profile", color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                Box(Modifier.size(110.dp).clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.15f))
                    .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
                    .clickable { imgPicker.launch("image/*") }, Alignment.Center) {
                    if (pic.isNotBlank()) {
                        AsyncImage(model = pic, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape))
                    } else {
                        Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(52.dp))
                    }
                }
                Box(Modifier.size(32.dp).align(Alignment.BottomEnd).clip(CircleShape)
                    .background(colors.primary), Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                VishaTextField(name, { name = it }, "Display Name")
                VishaTextField(bio, { bio = it }, "Bio", minLines = 3)
                Button(onClick = { onSave(UserProfile(name.trim(), bio.trim(), pic)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(18.dp)) {
                    Text("Save Profile", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                Text("About Visha", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(26.dp))
                .background(colors.primary.copy(alpha = 0.2f))
                .border(2.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(26.dp)), Alignment.Center) {
                Icon(Icons.Default.MusicNote, null, tint = colors.primary, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Visha Music", color = colors.textPrimary, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Version 2.0", color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(32.dp))
            SettingsCardGroup(Modifier.padding(horizontal = 16.dp)) {
                listOf("Developer" to "Visha Dev", "Architecture" to "MVVM + Clean Arch",
                    "Engine" to "Media3 ExoPlayer", "Build" to "Kotlin + Hilt + Room")
                    .forEachIndexed { i, (k, v) ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), Arrangement.SpaceBetween) {
                            Text(k, color = colors.textSecondary, fontSize = 14.sp)
                            Text(v, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        if (i < 3) HorizontalDivider(thickness = 0.5.dp, color = colors.elevated)
                    }
            }
        }
    }
}

@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val colors  = LocalAppColors.current
    val bgBrush = LocalGlossyBackground.current
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var isBug   by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(bgBrush))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary) }
                Text("Send Feedback", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            SettingsCardGroup(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isBug, onCheckedChange = { isBug = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary))
                        Spacer(Modifier.width(6.dp))
                        Text("This is a bug report", color = colors.textPrimary, fontSize = 14.sp)
                    }
                    VishaTextField(message, { message = it; error = "" },
                        if (isBug) "Describe the bug..." else "Your feedback...", minLines = 5)
                    if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    Button(
                        onClick = {
                            if (message.isBlank()) { error = "Please enter a message"; return@Button }
                            val i = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@vishamusic.app"))
                                putExtra(Intent.EXTRA_SUBJECT, if (isBug) "Bug Report - Visha Music" else "Feedback - Visha Music")
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            try { context.startActivity(i) } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Send via Email", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

// ─── Shared widgets ───────────────────────────────────────────────────────────
@Composable
fun VishaTextField(value: String, onValueChange: (String) -> Unit, label: String, minLines: Int = 1) {
    val colors = LocalAppColors.current
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, color = colors.textSecondary) },
        modifier = Modifier.fillMaxWidth(), minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary, unfocusedBorderColor = colors.border,
            focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary,
            focusedContainerColor = colors.card.copy(alpha = 0.6f),
            unfocusedContainerColor = colors.card.copy(alpha = 0.4f), cursorColor = colors.primary))
}

@Composable
fun SettingsSectionLabel(text: String) {
    val colors = LocalAppColors.current
    Text(text, color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
fun SettingsCardGroup(modifier: Modifier = Modifier.padding(horizontal = 16.dp), content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier.clip(RoundedCornerShape(18.dp))
            .background(colors.card.copy(alpha = 0.8f))
            .border(0.5.dp, colors.border, RoundedCornerShape(18.dp)),
        content = content
    )
}

@Composable
fun SettingsTile(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.15f)), Alignment.Center) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = colors.textMuted)
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(border(w, color, shape))