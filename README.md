# Xev-rae

**Xev-rae** is a FOSS (Free and Open Source Software) YouTube Music client for Android, rebuilt with modern Android architecture.

## ✨ Features

- 🎵 Stream music from YouTube Music — free, ad-free
- 🎨 Material Design 3 with dynamic colors
- 📱 Background playback with MediaSession
- 🎤 Synced lyrics from multiple providers
- 🎧 DJ-style crossfade transitions
- 🤖 AI-powered song suggestions
- 🎭 Spotify Canvas support
- 📊 Local listening analytics
- 🔒 Privacy-first: FOSS variant with zero tracking

## 🏗️ Architecture

- **Clean Architecture** with multi-module setup
- **Hilt** for dependency injection
- **Jetpack Compose** for UI
- **Media3 (ExoPlayer)** for media playback
- **Room** for local database
- **Ktor** for networking
- **MVVM** pattern with StateFlow

## 📦 Build Variants

| Variant | Description |
|---------|-------------|
| `foss` | No tracking, no crash reporting |
| `full` | Includes Sentry crash reporting |

## 🔨 Building

Builds are automated via GitHub Actions. To build locally:

```bash
# FOSS Release
./gradlew assembleFossRelease

# Full Release  
./gradlew assembleFullRelease
```

## 📄 License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- [InnerTune](https://github.com/z-huang/InnerTune) — YouTube Music data extraction inspiration
- [SponsorBlock](https://sponsor.ajay.app/) — Sponsor skip functionality
- [LRCLIB](https://lrclib.net/) — Lyrics provider
