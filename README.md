# Minuteman Virtual Gamepad (Android)

Mobile retro 8-bit handheld console, virtual gamepad, LCD screen shell, and cartridge runner written in Kotlin with Jetpack Compose.

## Features

- **Retro 8-bit Shell OS**: Boot splash screen, Main Menu, Cartridge browser, Diagnostics, and Settings.
- **Built-in Cartridge Library**:
  - **Knight (Proto)**: Metroidvania action adventure with procedural dungeons, sword combos, animated monsters, and relics.
  - **Snake 99**: Classic arcade snake with speed escalations, fruit, and bonus pickups.
  - **Pocket Jump**: Vertical doodle platform hopper with springs, moving platforms, stars, and super boosts.
  - **Star Patrol**: Space shoot-em-up with starfield, laser blasts, space drones, cruisers, and powerups.
  - **Tiny Rogue**: Dungeon crawler RPG with pots, chests, keys, skeleton warriors, and multiple floor depths.
  - **Walk Test**: Walking cycle physics demonstrator.
  - **Template Cart**: Workshop boilerplate ready for custom game creation.
- **Tactile Virtual Gamepad**:
  - Directional Cross D-Pad with sliding touch and analog tilt support.
  - Tactile Action Buttons (A, B, X, Y) with responsive press animations.
  - Angled SELECT & START buttons (Press START + SELECT to return to Shell).
  - L and R shoulder buttons.
  - Haptic feedback integration (`android.permission.VIBRATE`).
- **Retro 160x144 LCD Viewport**:
  - Crisp nearest-neighbor pixel rendering.
  - CRT scanlines overlay toggle.
  - 5 customizable console color palettes (DMG Classic, Pocket Mono, Cyber Neon, Amber CRT, Virtual Red).
- **Audio Synthesizer Engine**:
  - Procedural 8-bit square, triangle, sawtooth, and noise wave audio synthesizer.
  - Sound effects for menu navigation, lasers, jumps, hits, coins, and powerups.
- **Memory Card System (VMS-64)**:
  - Save slot storage and auto-checkpointing.
  - Reversible snapshots (undo save states).
  - Backup export and import.
- **Cartridge Workshop**: Custom cartridge burner to create new retro games on device.
- **Developer Toolkit**: Real-time FPS monitoring, frame delta timing, active game stats, pause and single-frame advance.

## Testing & Installing on Your Phone

You can install and test Minuteman on your phone using either method:

### Method 1: Android Native APK (Automated via GitHub Actions)
1. Push these changes to your GitHub repository (`main` branch).
2. Go to your repository on GitHub and click the **Actions** tab.
3. Select the **Build Android APK** workflow run.
4. Once completed, scroll to the bottom **Artifacts** section and download **Minuteman-Android-APK**.
5. Transfer or open the downloaded `app-debug.apk` on your Android phone to install!

### Method 2: Install directly as a Progressive Web App (PWA)
1. Open the app link on your phone in Chrome (Android) or Safari (iOS).
2. On Android:
   - Tap the green **Install App** button inside the console header, or tap Chrome menu (`⋮`) -> **Install app** / **Add to Home screen**.
3. On iOS (iPhone / iPad):
   - Tap Safari's **Share** button (`⎋`) -> tap **Add to Home Screen**.
4. Minuteman will be installed as a full-screen, standalone app on your home screen with offline caching, haptics, and retro audio!
