# 🖤 VoidLauncher

> *A launcher that disappears, so you can focus on what remains.*

---

## 1. Purpose & Philosophy

**VoidLauncher** is a custom Android home screen designed for **extreme minimalism and mental clarity**.  
It removes all visual noise, icons, and animations — replacing them with a pure black background and white monospaced text.  

The launcher acts as a **gateway**, not a playground:  
You access only a few deliberately chosen apps. Every interaction feels intentional, not habitual.

**Principle:** “If you don’t need it, you don’t see it.”

---

## 2. Functional Overview

### Main Screen
- **Visuals:**  
  - Pure black background (`#000000`)  
  - White text (`#FFFFFF`)  
  - Monospace font  
  - No icons, no animations  
- **Content:**  
  - Vertical list of essential apps (e.g., Messages, Browser, Instagram)  
  - Last entry: `All Apps >`  
- **Interactions:**  
  - Tap app → Launch it  
  - Tap `All Apps >` → Open secondary screen  
- **System UI:**  
  - Hide status/navigation bar  
  - Optional subtle haptic feedback  

### All Apps Screen
- Full list of installed apps in white text  
- Alphabetically sorted  
- Tap to launch  
- Back to return  

---

## 3. Design Principles

| Principle | Description |
|------------|--------------|
| **Void Aesthetic** | No graphics, only text on black. |
| **Intentional Use** | Only chosen apps appear. |
| **Instant Response** | Immediate reaction to touch. |
| **Silence** | No animations, sounds, or clutter. |

---

## 4. Technical Design

### Architecture
- **Language:** Kotlin  
- **SDK Target:** 24+  
- **Activities:** `MainActivity`, `AllAppsActivity`  
- **Dependencies:** None external (pure Android SDK)  

### Components
| Component | Role |
|------------|------|
| `MainActivity` | Displays curated list of essential apps |
| `AllAppsActivity` | Displays full list of installed apps |
| `PackageManager` | Fetches installed apps and launch intents |
| `Intent Filter` | Declares launcher (`MAIN` + `HOME`) |

---

## 5. App Data

- Essential apps defined as a static list (package name + readable name).  
- Future: editable via JSON or preferences.

---

## 6. User Flow

1. Unlock → VoidLauncher home appears.  
2. Tap an app → Launch it.  
3. Press Home → Back to black screen.  
4. Tap `All Apps >` → View all apps.  
5. Back → Return to main screen.  

---

## 7. Planned Features

| Feature | Status |
|----------|--------|
| Configurable visible apps | ☐ |
| Adjustable text size | ☐ |
| Long-press to reorder/hide apps | ☐ |
| Randomized app order | ☐ |
| Focus mode | ☐ |
| Hidden Settings gesture | ☐ |

---

## 8. Technical Constraints

- Works on Android 7.0+  
- No permissions, analytics, or background services  
- Offline only  
- Lightweight (<1 MB APK, <1000 LOC)  

---

## 9. Roadmap

**Phase 1 – MVP**
- Minimal 2-screen launcher  
- Hardcoded app list  

**Phase 2 – Configurable**
- Load app list from SharedPreferences or JSON  

**Phase 3 – UX Enhancements**
- Gestures, randomization, hidden settings  

**Phase 4 – Polishing**
- Handle missing apps, optimize performance  

---

## 10. Visual Concept

```
void launcher
──────────────
Instagram
Messages
Browser
YouTube
Settings
──────────────
All Apps >
```

---

## 11. License & Ethos

- Open source, non-commercial by default  
- Customizable through reduction  
- Stay under 1 MB, 1000 lines of code  
