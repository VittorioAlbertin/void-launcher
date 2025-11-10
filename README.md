# 🖤 VoidLauncher

> *A launcher that disappears, so you can focus on what remains.*

A minimal Android home screen launcher designed for extreme minimalism and mental clarity. No icons, no animations, no visual noise—just pure black background with white monospaced text.

## Philosophy

VoidLauncher acts as a gateway, not a playground. You access only deliberately chosen apps, making every interaction intentional rather than habitual.

**Principle:** *"If you don't need it, you don't see it."*

## Features

### Core Experience
- **Pure Minimal Aesthetic**: Black (#000000) background with white (#FFFFFF) monospaced text
- **Digital Clock**: Centered clock on homepage for time awareness
- **Gesture Navigation**: Swipe up/down/left/right to launch frequently used apps
- **Smart App Management**: Long press to add/remove apps from homepage
- **Search Functionality**: Real-time search in All Apps menu
- **No Visual Clutter**: No icons, animations, sounds, or unnecessary graphics
- **Hidden System UI**: Status and navigation bars hidden for maximum immersion

### Customization
- **Adjustable Font Size**: 4 presets + custom size input (8-32sp)
- **Gesture Shortcuts**: Configure 4 swipe directions to launch any app
- **Hidden Apps**: Hide specific apps from the "All Apps" list
- **Flexible Homepage**: Add/remove apps via intuitive long press

## Screenshots

```
void launcher
──────────────
      14:30
──────────────
Instagram
Messages
Browser
YouTube
Settings
──────────────
All Apps >    Settings
```

**All Apps with Search:**
```
all apps
──────────────
search...
──────────────
[Filtered app list]
```

## Installation

### From Source

1. Clone this repository
2. Open in Android Studio
3. Build and run on your device (Android 7.0+)

### From APK

1. Download the latest APK from [Releases](../../releases)
2. Install on your Android device
3. Grant "Query All Packages" permission (required for Android 11+)
4. Go to **Settings → Set as Default Launcher** (or use system settings)
5. Select **Void Launcher** as your home app

## Usage

### Homepage
- **Tap app**: Launch the app
- **Long press app**: Remove from homepage
- **Swipe up/down/left/right**: Launch configured gesture app (on clock/app list area)
- **Clock**: Displays current time (updates automatically)

### All Apps Menu
- **Search bar**: Type to filter apps in real-time
- **Tap app**: Launch the app
- **Long press app**: Add to homepage

### Settings
- **Font Size**: Choose from presets or enter custom size (8-32sp)
- **Gestures**: Configure swipe shortcuts for 4 directions
- **Hidden Apps**: Select apps to hide from All Apps menu
- **Set as Default Launcher**: Open system settings to set as default

## Technical Details

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Dependencies**: AndroidX only (Material Components for dialogs)
- **Permissions**: `QUERY_ALL_PACKAGES` (for Android 11+ package visibility)
- **Architecture**: MVVM-like with SharedPreferences persistence

## Architecture

```
MainActivity                → Homepage with clock, apps, and gesture detection
AllAppsActivity             → Full app list with search functionality
SettingsActivity            → Settings menu
GesturesActivity            → Gesture configuration screen
AppSelectionActivity        → App picker for hidden apps
GestureAppSelectionActivity → App picker for gesture shortcuts
PreferencesManager          → SharedPreferences storage
AppAdapter                  → RecyclerView adapter for app lists
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
- Hidden apps functionality

**Phase 2 – UX Enhancements** ✅
- Gesture navigation (swipe shortcuts)
- Search functionality in All Apps
- Long press to add/remove homepage apps
- Digital clock on homepage
- Custom font size input

**Phase 3 – Advanced** (Future)
- App usage statistics
- Focus mode / time-based app hiding
- Randomized app order option
- Custom color schemes
- Minimal widget support

## Contributing

Contributions are welcome! Please keep the minimalist philosophy in mind:
- No unnecessary features - every feature must serve the core purpose
- Maintain the void aesthetic - black, white, monospace only
- Keep dependencies minimal - AndroidX only
- Code should be clean and well-documented
- Test on multiple Android versions (API 24+)

## License

This project is open source and available under the MIT License.

## Credits

Created with intentionality and care for focused living.

---

*Stay minimal. Stay focused. Embrace the void.*
