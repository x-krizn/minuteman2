package com.example.minuteman.ui.screens

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.ConsolePalette
import com.example.minuteman.model.ShellScreen
import kotlin.math.sin

object ShellScreenRenderer {

    val MENU_ITEMS = listOf(
        Pair("HOW-TO", ShellScreen.HOWTO),
        Pair("CARTRIDGES", ShellScreen.CARTS),
        Pair("MEMORY CARD", ShellScreen.MEMORY),
        Pair("SETTINGS", ShellScreen.SETTINGS),
        Pair("DEBUGGER", ShellScreen.DEBUG),
        Pair("CREDITS", ShellScreen.CREDITS)
    )

    fun draw(
        surface: CartridgeSurface,
        screen: ShellScreen,
        palette: ConsolePalette,
        menuIndex: Int,
        cartIndex: Int,
        cartridges: List<Cartridge>,
        splashProgress: Float
    ) {
        val w = surface.width.toFloat()
        val h = surface.height.toFloat()
        val textColor = palette.screenText.toInt()
        val accentColor = palette.accent.toInt()
        val bgColor = palette.screenBg.toInt()

        surface.clear(bgColor)

        when (screen) {
            ShellScreen.SPLASH -> {
                drawSplash(surface, w, h, textColor, splashProgress)
            }
            ShellScreen.MENU -> {
                drawMenu(surface, w, h, textColor, accentColor, menuIndex)
            }
            ShellScreen.CARTS -> {
                drawCarts(surface, w, h, textColor, accentColor, cartIndex, cartridges)
            }
            ShellScreen.HOWTO -> {
                drawHowTo(surface, w, h, textColor, accentColor)
            }
            ShellScreen.MEMORY -> {
                drawMemory(surface, w, h, textColor, accentColor)
            }
            ShellScreen.SETTINGS -> {
                drawSettings(surface, w, h, textColor, accentColor, palette)
            }
            ShellScreen.DEBUG -> {
                drawDebug(surface, w, h, textColor, accentColor)
            }
            ShellScreen.CREDITS -> {
                drawCredits(surface, w, h, textColor, accentColor)
            }
            ShellScreen.EXIT -> {
                surface.drawText("SHUTTING DOWN...", 30f, 72f, textColor, 8f)
            }
        }
    }

    private fun drawSplash(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, progress: Float) {
        val yOffset = ((progress * 1.5f).coerceAtMost(1f)) * 55f
        surface.drawText("★ MINUTEMAN ★", 38f, yOffset, textColor, 9f)
        if (progress > 0.6f) {
            surface.drawText("VIRTUAL GAMEPAD OS", 32f, 72f, textColor, 7f)
            surface.drawText("PRESS A / START", 40f, 95f, 0xFFFFFF00.toInt(), 7f)
        }
        surface.drawText("v1.0.0 (ABI 1.0)", 48f, 134f, 0x7700FF33, 6f)
    }

    private fun drawMenu(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int, menuIndex: Int) {
        // Header
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("MINUTEMAN SHELL", 6f, 10f, textColor, 8f)
        surface.drawText("MAIN MENU", 100f, 10f, 0xFFFFFF00.toInt(), 7f)

        // Menu items
        MENU_ITEMS.forEachIndexed { idx, item ->
            val isSelected = idx == menuIndex
            val y = 30f + idx * 15f
            if (isSelected) {
                surface.fillRect(6f, y - 8f, w - 12f, 12f, 0x5500FF33)
                surface.drawText("▶ ${item.first}", 10f, y, 0xFFFFFFFF.toInt(), 8f)
            } else {
                surface.drawText("  ${item.first}", 10f, y, textColor, 8f)
            }
        }

        // Footer Help
        surface.fillRect(0f, h - 14f, w, 14f, 0x33000000)
        surface.drawText("UP/DOWN: SELECT   A: OPEN", 12f, h - 4f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawCarts(
        surface: CartridgeSurface,
        w: Float,
        h: Float,
        textColor: Int,
        accentColor: Int,
        cartIndex: Int,
        cartridges: List<Cartridge>
    ) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("CARTRIDGES (${cartridges.size})", 6f, 10f, textColor, 8f)

        if (cartridges.isEmpty()) {
            surface.drawText("NO CARTRIDGES FOUND", 20f, 70f, textColor, 8f)
            return
        }

        // Show max 5 carts at a time with scroll
        val startIndex = (cartIndex - 2).coerceAtLeast(0)
        val visibleCarts = cartridges.drop(startIndex).take(5)

        visibleCarts.forEachIndexed { vIdx, cart ->
            val actualIdx = startIndex + vIdx
            val isSelected = actualIdx == cartIndex
            val y = 28f + vIdx * 15f
            if (isSelected) {
                surface.fillRect(6f, y - 8f, w - 12f, 12f, 0x5500FF33)
                surface.drawText("▶ ${cart.name}", 10f, y, 0xFFFFFFFF.toInt(), 8f)
            } else {
                surface.drawText("  ${cart.name}", 10f, y, textColor, 8f)
            }
        }

        // Cart Description Pane
        val selCart = cartridges.getOrNull(cartIndex)
        if (selCart != null) {
            surface.fillRect(6f, 106f, w - 12f, 24f, 0x33000000)
            surface.strokeRect(6f, 106f, w - 12f, 24f, 0x4400FF33, 1f)
            surface.drawText(selCart.author, 10f, 115f, 0xFFFFFF00.toInt(), 6f)
            val desc = selCart.description.take(28)
            surface.drawText(desc, 10f, 124f, 0xFFEEEEEE.toInt(), 6f)
        }

        // Footer Help
        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("A: PLAY   B: BACK", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawHowTo(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("CONTROLS / HOW-TO", 6f, 10f, textColor, 8f)

        val lines = listOf(
            "D-PAD: MOVE / TURN",
            "A: ACTION / SELECT",
            "B: CANCEL / ATTACK",
            "START: PAUSE / ADVANCE",
            "START+SELECT: QUIT GAME",
            "",
            "VIRTUAL TOUCH CONTROLLER",
            "WITH HAPTIC FEEDBACK"
        )

        lines.forEachIndexed { idx, line ->
            val color = if (line.contains("START+SELECT")) 0xFFFFFF00.toInt() else textColor
            surface.drawText(line, 10f, 26f + idx * 12f, color, 7f)
        }

        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("PRESS B TO RETURN", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawMemory(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("MEMORY CARD BUS", 6f, 10f, textColor, 8f)

        surface.drawText("STATUS: ONLINE (VMS-64)", 10f, 32f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("AUTO-CHECKPOINT: ON", 10f, 46f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("SYNC: LOCAL SECURE STORE", 10f, 60f, textColor, 7f)
        surface.drawText("OPEN MEMORY MODAL IN HEADER", 10f, 80f, 0xFFFFFF00.toInt(), 7f)
        surface.drawText("TO BACKUP / UNDO SAVES", 10f, 94f, 0xFFFFFF00.toInt(), 7f)

        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("PRESS B TO RETURN", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawSettings(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int, palette: ConsolePalette) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("SYSTEM SETTINGS", 6f, 10f, textColor, 8f)

        surface.drawText("PALETTE: ${palette.name}", 10f, 34f, 0xFFFFFF00.toInt(), 7f)
        surface.drawText("SCREEN: 160 x 144 PIXELS", 10f, 48f, textColor, 7f)
        surface.drawText("SCANLINES: ENABLED", 10f, 62f, textColor, 7f)
        surface.drawText("HAPTICS: ENABLED", 10f, 76f, textColor, 7f)
        surface.drawText("AUDIO: 8-BIT STEREO TRACK", 10f, 90f, textColor, 7f)
        surface.drawText("USE TOP BAR GEAR FOR FULL", 10f, 110f, 0xFF4DEEEA.toInt(), 7f)

        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("PRESS B TO RETURN", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawDebug(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("DIAGNOSTICS & DEBUG", 6f, 10f, textColor, 8f)

        surface.drawText("CORE ENGINE: OK", 10f, 32f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("FRAME TIME: ~16.6ms", 10f, 46f, textColor, 7f)
        surface.drawText("SURFACE CANVAS: 160x144", 10f, 60f, textColor, 7f)
        surface.drawText("AUDIO CHANNELS: ACTIVE", 10f, 74f, textColor, 7f)
        surface.drawText("INTERRUPT BUS: 60 HZ", 10f, 88f, textColor, 7f)

        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("PRESS B TO RETURN", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }

    private fun drawCredits(surface: CartridgeSurface, w: Float, h: Float, textColor: Int, accentColor: Int) {
        surface.fillRect(0f, 0f, w, 14f, 0x4400FF33)
        surface.drawText("CREDITS & TRIBUTE", 6f, 10f, textColor, 8f)

        surface.drawText("MINUTEMAN VIRTUAL GAMEPAD", 10f, 30f, 0xFFFFFF00.toInt(), 7f)
        surface.drawText("PORTED TO KOTLIN + COMPOSE", 10f, 44f, 0xFF00FF33.toInt(), 7f)
        surface.drawText("INSPIRED BY RETRO HANDHELDS", 10f, 58f, textColor, 7f)
        surface.drawText("ORIGINAL REPO: x-krizn", 10f, 74f, 0xFF4DEEEA.toInt(), 7f)
        surface.drawText("GAMES BUILT-IN: 7 CARTS", 10f, 88f, textColor, 7f)
        surface.drawText("THANKS FOR PLAYING!", 26f, 110f, 0xFFFF77AA.toInt(), 8f)

        surface.fillRect(0f, h - 12f, w, 12f, 0x33000000)
        surface.drawText("PRESS B TO RETURN", 36f, h - 3f, 0xFF88AA88.toInt(), 7f)
    }
}
