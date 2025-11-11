# VoidLauncher - Quick Start Guide

Welcome to VoidLauncher! This guide will help you get started with installing, configuring, and using the launcher.

## Installation

### Method 1: Install from APK (Recommended)

1. **Download the APK**
   - Get the latest APK from the `releases/` folder in this repository
   - Transfer it to your Android device

2. **Enable installation from unknown sources**
   - Go to **Settings → Security** (or **Settings → Apps**)
   - Enable **Install unknown apps** for your file manager or browser

3. **Install the APK**
   - Open the APK file and tap **Install**
   - Wait for installation to complete

4. **Grant required permissions** (see Permissions section below)

5. **Set as default launcher**
   - Go to **Settings → Apps → Default apps → Home app**
   - Or tap **Settings** in VoidLauncher, then **Set as Default Launcher**
   - Select **Void Launcher** from the list
   - Press the home button to confirm

### Method 2: Build from Source

1. **Prerequisites**
   - Android Studio (latest version)
   - Android SDK with API 24+ (Android 7.0+)
   - Git

2. **Clone and build**
   ```bash
   git clone [repository-url]
   cd VoidLauncher
   ```

3. **Open in Android Studio**
   - Open the project folder
   - Wait for Gradle sync to complete
   - Connect your Android device or start an emulator

4. **Build and install**
   - Click **Run** (or press Shift+F10)
   - Select your device
   - The app will build and install automatically

5. **Follow steps 4-5 from Method 1** to grant permissions and set as default launcher

---

## Required Permissions

VoidLauncher requires specific permissions to function properly. You'll need to grant these manually after installation:

### 1. Query All Packages (Required)

**What it does:** Allows VoidLauncher to see and display all installed apps on your device.

**When it's needed:** Android 11 (API 30) and above

**How to grant:**
- The launcher will prompt you automatically on first launch, OR:
- Go to **Settings → Apps → Void Launcher → Permissions**
- Look for "Query all packages" or similar option
- Enable the permission

**Note:** Without this permission, most apps won't appear in the All Apps menu on Android 11+.

### 2. Usage Access / Package Usage Stats (Optional)

**What it does:** Enables app usage statistics for intelligent auto-hide features.

**When it's needed:** Only if you want to use auto-hide based on usage time or frequency

**How to grant:**
- Go to VoidLauncher **Settings → Screen Time**
- Tap **Grant Permission**
- You'll be redirected to system settings
- Find **Void Launcher** in the list
- Enable **Permit usage access**

**Note:** This permission is optional. Basic launcher functionality works without it.

### 3. Device Administrator (Optional)

**What it does:** Allows the double-tap-to-lock feature to work.

**When it's needed:** Only if you want to lock your device by double-tapping the homepage

**How to grant:**
- Double-tap anywhere on the VoidLauncher homepage
- You'll be prompted to enable device administrator
- Tap **Activate** to grant permission

**How to revoke (if needed):**
- Go to **Settings → Security → Device admin apps**
- Find **Void Launcher** and disable it
- Now you can uninstall normally if desired

---

## Initial Setup

### First Launch

1. **Homepage appears empty**
   - This is normal! VoidLauncher starts with a clean slate
   - You'll see the clock at the center and "All Apps" button at the bottom

2. **Add apps to your homepage**
   - Tap **All Apps** to see all installed apps
   - Long press any app you want on your homepage
   - Select **Add to Homepage** from the dialog
   - Repeat for 4-6 essential apps (stay minimal!)

3. **Access settings**
   - Tap **Settings** at the bottom of the All Apps menu
   - Or long press **All Apps** button on homepage

### Recommended First Settings

1. **Adjust font size**
   - Go to **Settings → Font Size**
   - Try presets: **Small**, **Medium**, **Large**, or **Extra Large**
   - Or enter a custom size (8-32sp)
   - Tap **Apply**

2. **Configure gesture shortcuts** (optional but recommended)
   - Go to **Settings → Gestures**
   - Configure swipe directions:
     - **Swipe Up**: All Apps (recommended)
     - **Swipe Down**: None or a frequently used app
     - **Swipe Left**: Your most-used app
     - **Swipe Right**: Another frequent app
   - Tap each direction and select an app or "All Apps"
   - Choose "None (Disable)" to disable a gesture

3. **Hide unwanted apps** (optional)
   - Go to **Settings → Hidden Apps**
   - Search or scroll to find system apps or apps you never use
   - Check the boxes next to apps to hide them
   - They'll disappear from the All Apps menu

---

## Features Guide

### Homepage Navigation

**What you'll see:**
```
void launcher
──────────────
      14:30
──────────────
[Your apps here]
──────────────
All Apps >    Settings
```

**Actions:**
- **Tap an app** → Launch it
- **Double tap anywhere** → Lock device (requires Device Administrator permission)
- **Long press an app** → Remove from homepage
- **Swipe up/down/left/right** → Trigger configured gesture (on clock/app area)
- **Tap "All Apps"** → Open full app list
- **Tap "Settings"** → Open settings menu

**Smart UI:** The "All Apps" button automatically hides if you configure any gesture to open All Apps.

### All Apps Menu

**What you'll see:**
```
all apps
──────────────
search...
──────────────
[Filtered app list]
──────────────
< Back    Settings
```

**Actions:**
- **Type in search bar** → Filter apps instantly (real-time search)
- **Tap an app** → Launch it
- **Long press an app** → Options dialog:
  - Add to/Remove from homepage
  - Configure intelligent auto-hide settings
  - View usage statistics (if permission granted)
- **Tap "< Back"** → Return to homepage
- **Tap "Settings"** → Open settings menu

**Performance:** Apps are preloaded on launcher startup for instant, zero-delay opening.

### Settings Overview

**Font Size**
- Choose from 4 presets or enter custom size (8-32sp)
- Changes apply immediately

**Gestures**
- Configure 4 swipe directions (up/down/left/right)
- Options: None (Disable), All Apps, or any installed app
- Includes search functionality for easy app selection

**Hidden Apps**
- Select apps to hide from All Apps menu
- Searchable interface for quick selection
- Hidden apps can still be launched via search

**Screen Time** (requires Usage Access permission)
- View app usage statistics
- Configure intelligent auto-hide rules based on:
  - Total usage time per day
  - Number of times opened
  - Time of day restrictions

**Set as Default Launcher**
- Quick link to system settings to set VoidLauncher as your home app

### Intelligent Auto-Hide Features

Configure apps to automatically hide based on your usage patterns:

1. **Long press any app** in All Apps menu
2. Select **Auto-Hide Settings**
3. Configure rules:
   - **Usage time threshold**: Hide if used more than X minutes per day
   - **Open frequency**: Hide if opened more than X times per day
   - **Time-based**: Hide during specific hours (e.g., work hours)
4. View real-time usage statistics to inform your choices

**Use case examples:**
- Hide social media apps after 30 minutes of daily usage
- Hide gaming apps during work hours (9 AM - 5 PM)
- Hide frequently-opened apps to reduce compulsive checking

---

## Tips & Best Practices

### Embrace Minimalism
- Keep only 3-6 apps on your homepage
- Hide apps you rarely use
- Use gestures for your most frequent actions

### Optimize Gestures
- **Swipe Up** → All Apps (most intuitive)
- **Swipe Left/Right** → Your top 2 most-used apps
- **Swipe Down** → Keep disabled or use for a utility app

### Reduce Phone Addiction
- Use auto-hide to limit time on distracting apps
- Keep social media off the homepage
- Rely on intentional search rather than visual browsing

### Customize Appearance
- Larger font (18-24sp) for easier reading
- Smaller font (10-14sp) for more apps visible at once
- Medium (16sp) is the default and works well for most users

---

## Troubleshooting

### Apps not appearing in All Apps menu
- **Android 11+:** Grant "Query All Packages" permission
- **Solution:** Go to Settings → Apps → Void Launcher → Permissions
- Enable package query permission

### Double-tap lock not working
- **Required:** Device Administrator permission
- **Solution:** Double tap the homepage and approve the permission request
- Or go to Settings → Security → Device admin apps → Enable Void Launcher

### Gestures not working
- **Check:** Swipe must be performed on the clock or app list area
- **Verify:** Gesture is configured in Settings → Gestures
- **Try:** Longer swipe movements for better detection

### Usage statistics not showing
- **Required:** Usage Access permission
- **Solution:** Settings → Screen Time → Grant Permission
- Enable "Permit usage access" for Void Launcher

### Can't uninstall the launcher
- **Reason:** Device Administrator is enabled
- **Solution:**
  1. Go to Settings → Security → Device admin apps
  2. Disable Void Launcher
  3. Now you can uninstall normally

### Launcher keeps resetting to default
- **Check:** VoidLauncher is set as default home app
- **Solution:** Settings → Apps → Default apps → Home app → Select Void Launcher

---

## Support

For issues, feature requests, or questions:
- Open an issue on the [GitHub repository](../../issues)
- Check existing issues for solutions
- Include your Android version and device model when reporting bugs

---

**Stay minimal. Stay focused. Embrace the void.**
