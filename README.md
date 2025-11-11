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
- **Double Tap to Lock**: Double tap anywhere on homepage to instantly lock your device
- **Gesture Navigation**: Swipe up/down/left/right to launch apps or open All Apps menu
- **Smart App Management**: Long press to add/remove apps from homepage
- **Universal Search**: Real-time search in All Apps, gesture selection, and hidden apps menus
- **Instant Performance**: Preloaded app cache ensures zero-delay menu opening
- **Adaptive UI**: "All Apps" button auto-hides when gesture is configured for cleaner interface
- **No Visual Clutter**: No icons, animations, sounds, or unnecessary graphics
- **Hidden System UI**: Status and navigation bars hidden for maximum immersion

### Customization
- **Adjustable Font Size**: 4 presets + custom size input (8-32sp)
- **Gesture Shortcuts**: Configure 4 swipe directions to launch apps or open All Apps menu
- **Hidden Apps**: Hide specific apps with searchable selection interface
- **Flexible Homepage**: Add/remove apps via intuitive long press
- **Smart Auto-Hide**: "All Apps" button automatically hides when assigned to a gesture
- **Intelligent Auto-Hide**: Automatically hide apps from All Apps menu based on usage time, open frequency, or time of day

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

1. Download the latest APK from the `releases/` folder in this repository
2. Install on your Android device
3. Grant "Query All Packages" permission (required for Android 11+)
4. Go to **Settings → Set as Default Launcher** (or use system settings)
5. Select **Void Launcher** as your home app

See [QUICKSTART.md](QUICKSTART.md) for detailed installation and setup instructions.

## Usage

### Homepage
- **Tap app**: Launch the app
- **Double tap anywhere**: Lock the device instantly
- **Long press app**: Remove from homepage
- **Swipe up/down/left/right**: Launch configured gesture app or open All Apps (on clock/app list area)
- **Clock**: Displays current time (updates automatically)
- **All Apps button**: Automatically hides if any gesture is set to open All Apps

### All Apps Menu
- **Search bar**: Type to filter apps in real-time (instant results)
- **Tap app**: Launch the app
- **Long press app**: Opens dialog to add/remove from homepage, configure auto-hide options, and visualize usage data
- **Preloaded apps**: Opens instantly with zero delay
- **Intelligent auto-hide**: Apps can be automatically hidden based on excessive usage time, open frequency, or during specified hours

### Settings
- **Font Size**: Choose from presets or enter custom size (8-32sp)
- **Gestures**: Configure swipe shortcuts for 4 directions
  - Choose "None (Disable)" to disable a gesture
  - Choose "All Apps" to open All Apps menu with that gesture
  - Choose any app to launch it with that gesture
- **Hidden Apps**: Show apps hidden from All Apps menu (searchable)
- **Set as Default Launcher**: Open system settings to set as default

## Technical Details

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Dependencies**: AndroidX, Kotlin Coroutines, Lifecycle Runtime KTX
- **Permissions**: `QUERY_ALL_PACKAGES` (for Android 11+ package visibility)
- **Architecture**: MVVM-like with SharedPreferences persistence
- **Performance**: Background app preloading with coroutines for instant UI response
- **Caching**: Thread-safe singleton app cache for zero-delay All Apps opening

## Architecture

```
MainActivity                → Homepage with clock, apps, and gesture detection
                              - Preloads apps on startup for instant All Apps access
                              - Auto-hides "All Apps" button when gesture is configured
AllAppsActivity             → Full app list with instant display and search
                              - Uses cached app data for zero-delay opening
SettingsActivity            → Settings menu
GesturesActivity            → Gesture configuration screen with "All Apps" option
AppSelectionActivity        → App picker for hidden apps (with search)
GestureAppSelectionActivity → App picker for gesture shortcuts (with search)
                              - Includes "None (Disable)" and "All Apps" options
PreferencesManager          → SharedPreferences storage for all settings
AppCache                    → Thread-safe singleton for preloaded app list
AppAdapter                  → RecyclerView adapter for app lists
SelectableAppAdapter        → RecyclerView adapter with checkboxes for selection
```

## Design Principles

| Principle | Description |
|-----------|-------------|
| **Void Aesthetic** | No graphics, only text on black |
| **Intentional Use** | Only chosen apps appear |
| **Instant Response** | Zero-delay app loading with background preloading |
| **Adaptive UI** | Interface elements hide when no longer needed |
| **Silence** | No animations, sounds, or clutter |

## Roadmap

**Phase 1 – MVP** ✅
- Minimal 2-screen launcher
- Customizable app list
- Hidden apps functionality

**Phase 2 – UX Enhancements** ✅
- Gesture navigation (swipe shortcuts)
- Gesture-based "All Apps" access with smart button auto-hide
- Universal search in All Apps, gestures, and hidden apps menus
- Performance optimization with app preloading and caching
- Long press to add/remove homepage apps
- Digital clock on homepage
- Custom font size input

**Phase 3 – Advanced** (WIP)
- App usage statistics
- Intelligent auto-hide based on usage time, frequency, and time of day
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
