# 🎵 Visha Music Player

![Hero Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/blob/main/assets/home.jpeg)

A production-ready, fully functional Android Music Player built with **Jetpack Compose** and **Clean Architecture**. Inspired by a Visha Player -a music & video player.

---

## 📸 Screenshots

### Home Screen
![Home Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/home_screen.png)

### Library Screen
![Library Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/library_screen.png)

### Play Screen
![Play Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/play_screen.png)

### Search Screen
![Search Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/search_screen.png)

### Settings Screen
![Settings Screen](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/settings_screen.png)

### Mini Player & Bottom Navigation
![Mini Player](https://raw.githubusercontent.com/HammadNawaz35/Visha-Music/main/assets/mini_player.png)

---

## 📁 Project Structure

```
app/src/main/java/com/musicplayer/
│
├── MusicApplication.kt                  # Hilt Application
│
├── di/
│   └── AppModule.kt                     # Hilt DI modules (DB + Repository bindings)
│
├── domain/                              # Pure Kotlin — no Android deps
│   ├── model/
│   │   └── Models.kt                    # Track, Playlist, PlayerState, RepeatMode
│   └── repository/
│       └── AudioRepository.kt           # Interface contract
│
├── data/                                # Implementation layer
│   ├── local/
│   │   ├── db/
│   │   │   ├── MusicDatabase.kt         # Room database
│   │   │   └── Daos.kt                  # FavoriteDao + PlaylistDao
│   │   ├── entity/
│   │   │   └── Entities.kt              # Room @Entity classes
│   │   └── datastore/
│   │       └── AppPreferences.kt        # DataStore for settings + recently played
│   ├── mediastore/
│   │   └── MediaStoreHelper.kt          # ContentResolver to fetch device audio
│   ├── service/
│   │   └── MusicService.kt              # Media3 MediaSessionService (background play)
│   └── repository/
│       └── AudioRepositoryImpl.kt       # Combines all data sources
│
└── presentation/
    ├── MainActivity.kt                  # Entry point, handles permissions
    ├── navigation/
    │   └── AppNavigation.kt             # NavHost + BottomNav + MiniPlayer scaffold
    ├── viewmodel/
    │   ├── PlayerViewModel.kt           # MediaController bridge, all playback commands
    │   ├── LibraryViewModel.kt          # Tracks, favorites, playlists state
    │   └── SettingsViewModel.kt         # Settings toggles via DataStore
    ├── theme/
    │   └── Theme.kt                     # Colors, Typography, MaterialTheme
    ├── components/
    │   ├── MiniPlayer.kt                # Persistent mini player above bottom nav
    │   └── GlassSurface.kt              # Glassmorphism composable
    └── screens/
        ├── HomeScreen.kt                # Good morning, trending, recently played
        ├── LibraryScreen.kt             # Tabs: Songs/Albums/Artists/Playlists
        ├── PlayScreen.kt                # Full player with vinyl animation, pink theme
        ├── SearchScreen.kt              # Search + recent chips + category grid
        └── SettingsScreen.kt            # Appearance/Audio/System toggles
```

---

## ✅ Feature Checklist

### 🎧 Playback Engine
- [x] **AndroidX Media3 / ExoPlayer** — Full media pipeline
- [x] **MediaSessionService** — Background playback with system integration
- [x] **Persistent Notification** — With play/pause/skip controls from notification shade
- [x] **Audio Focus** — Auto-pause on calls, headphone unplug (via `setHandleAudioBecomingNoisy`)
- [x] **Play / Pause / Skip Next / Skip Previous** — Full wired to `MediaController`
- [x] **Seek** — Seekbar with real-time timestamp
- [x] **Shuffle** — Toggle via `MediaController.shuffleModeEnabled`
- [x] **Repeat** — OFF → ONE → ALL cycle
- [x] **Volume Control** — Slider wired to `MediaController.volume`
- [x] **Queue** — Play any track with full queue context

### 💾 Persistence
- [x] **Room DB** — Favorites and Playlists with PlaylistTrack junction
- [x] **DataStore** — Glassmorphism toggle, Dark Mode, recently played history
- [x] **MediaStore ContentResolver** — Fetches real device audio files
- [x] **Permission handling** — API 33+ (`READ_MEDIA_AUDIO`) and legacy (`READ_EXTERNAL_STORAGE`)

### 🎨 UI / UX
- [x] **Deep dark theme** — Custom `#0D0A07` background, warm orange accents
- [x] **Glassmorphism** — Global toggle stored in DataStore, affects MiniPlayer + BottomNav
- [x] **Home Screen** — "Good Morning Alex" header, Trending Now card, Recently Played row
- [x] **Library Screen** — Tabs (Songs/Albums/Artists/Playlists), sort filter, track count
- [x] **Play Screen** — Rotating vinyl art, pink-themed FAB + seekbar, favorites heart
- [x] **Search Screen** — Search bar, dismissible recent chips, 2-column category grid
- [x] **Settings Screen** — Grouped sections with real toggles
- [x] **Mini Player** — Anchored above bottom nav, animated progress, shows current track
- [x] **Bottom Navigation** — 5 tabs, orange circle FAB for Playing tab

---

## 🚀 Setup Instructions

### 1. Requirements
- Android Studio Hedgehog or newer
- Android SDK 26+ (min), targeting SDK 35
- Kotlin 2.0.21+

### 2. Clone & Open
```bash
git clone git clone https://github.com/HammadNawaz35/Visha-Music.git
# Open in Android Studio
```

### 3. Build
```bash
./gradlew assembleDebug
```

### 4. Run
Connect a physical device (recommended — emulators don't have music files).

### 5. Grant Permissions
On first launch, accept:
- **Read Media Audio** (Android 13+) or **Read External Storage** (Android 12-)
- **Post Notifications** (for playback notification)

---

## 🏗️ Architecture Details

### Clean Architecture Layers

```
Presentation  ←  Domain  ←  Data
(ViewModels,      (Models,     (Room, DataStore,
 Screens,         Repository    MediaStore,
 Navigation)      Interface)    Media3 Service)
```



## 🎨 Theme Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `Orange` | `#FF8C00` | Primary accent, selected states |
| `Pink` | `#FF2D78` | Play screen, seekbar |
| `DarkBackground` | `#0D0A07` | Screen background |
| `DarkSurface` | `#1A1410` | Nav bar |
| `DarkCard` | `#221C15` | Cards, list items |
| `TextPrimary` | `#FFFFFF` | Main text |
| `TextSecondary` | `#B0A090` | Subtitles |

---

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2024.09.03 | UI toolkit |
| Material3 | BOM | Design system |
| Hilt | 2.52 | Dependency injection |
| Room | 2.6.1 | Local database |
| DataStore | 1.1.1 | Preferences |
| Media3 ExoPlayer | 1.4.1 | Audio playback |
| Media3 Session | 1.4.1 | Background + notification |
| Coil Compose | 2.7.0 | Album art loading |
| Navigation Compose | 2.8.2 | Screen navigation |
| KSP | 2.0.21-1.0.25 | Code generation |

---
