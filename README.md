# 🖤 VoidLauncher

> *A launcher that disappears, so you can focus on what remains.*

A minimal Android home screen launcher designed for extreme minimalism and mental clarity. No icons, no animations, no visual noise—just pure black background with white monospaced text.

## Philosophy

VoidLauncher acts as a gateway, not a playground. You access only deliberately chosen apps, making every interaction intentional rather than habitual.

**Principle:** *"If you don't need it, you don't see it."*

## Features

- **Pure Minimal Aesthetic**: Black (#000000) background with white (#FFFFFF) monospaced text
- **Customizable Homepage**: Choose which apps appear on your main screen
- **Adjustable Font Size**: 4 size options (14sp, 16sp, 18sp, 20sp)
- **Hidden Apps**: Hide specific apps from the "All Apps" list
- **No Visual Clutter**: No icons, animations, sounds, or unnecessary graphics
- **Hidden System UI**: Status and navigation bars hidden for maximum immersion
- **Instant Response**: Immediate reaction to touch with optional haptic feedback

## Screenshots

```
void launcher
──────────────
Instagram
Messages
Browser
YouTube
Settings
──────────────
All Apps >    Settings
```

## Installation

### From Source

1. Clone this repository
2. Open in Android Studio
3. Build and run on your device (Android 7.0+)

### From APK

1. Download the latest APK from [Releases](../../releases)
2. Install on your Android device
3. Go to **Settings → Apps → Default Apps → Home App**
4. Select **Void Launcher**

## Usage

- **Main Screen**: Shows your curated list of essential apps
- **All Apps**: Tap "All Apps >" to see all installed apps
- **Settings**: Tap "Settings" to customize:
  - Select homepage apps
  - Adjust font size
  - Hide apps from All Apps list

## Technical Details

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Dependencies**: Pure Android SDK (AndroidX only)
- **Lines of Code**: ~750 LOC
- **APK Size**: ~5.7 MB (debug)

## Architecture

```
MainActivity        → Homepage with curated apps
AllAppsActivity     → Full list of installed apps
SettingsActivity    → Settings menu
AppSelectionActivity → App picker with checkboxes
PreferencesManager  → SharedPreferences storage
```

## Design Principles

| Principle | Description |
|-----------|-------------|
| **Void Aesthetic** | No graphics, only text on black |
| **Intentional Use** | Only chosen apps appear |
| **Instant Response** | Immediate reaction to touch |
| **Silence** | No animations, sounds, or clutter |

## Roadmap

**Phase 1 – MVP** ✅
- Minimal 2-screen launcher
- Customizable app list

**Phase 2 – UX Enhancements** (Planned)
- Gestures and swipe actions
- Search functionality
- App usage statistics
- Focus mode

**Phase 3 – Advanced** (Future)
- Randomized app order option
- Time-based app hiding
- Custom color schemes
- Widget support (minimal)

## Contributing

Contributions are welcome! Please keep the minimalist philosophy in mind:
- No unnecessary features
- Keep it under 1000 LOC
- Maintain the void aesthetic
- No external dependencies

## License

This project is open source and available under the MIT License.

## Credits

Created with intentionality and care for focused living.

---

*Stay minimal. Stay focused. Embrace the void.*
