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
