package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot
import org.json.JSONObject
import kotlin.math.hypot
import kotlin.random.Random

class PocketJumpCartridge : Cartridge {
    override val id: String = "pocket_jump"
    override val name: String = "POCKET JUMP"
    override val version: String = "1.0"
    override val author: String = "Minuteman Arcade"
    override val description: String = "Vertical platform hopper. D-Pad: Tilt, A: Super Boost."

    private enum class PlatformType { NORMAL, MOVING, CRUMBLY, SPRING }

    private data class Platform(
        var x: Float,
        var y: Float,
        val w: Float,
        val h: Float,
        val type: PlatformType,
        var vx: Float = 0f,
        var broken: Boolean = false
    )

    private data class Star(
        val x: Float,
        val y: Float,
        var collected: Boolean = false
    )

    private var px = 80f
    private var py = 100f
    private var pvx = 0f
    private var pvy = 0f
    private var cameraY = 0f
    private var score = 0
    private var highScore = 0
    private var superJumps = 1
    private var isGameOver = false

    private val platforms = mutableListOf<Platform>()
    private val stars = mutableListOf<Star>()

    private fun generateInitialPlatforms() {
        platforms.clear()
        stars.clear()
        platforms.add(Platform(60f, 125f, 40f, 5f, PlatformType.NORMAL))
        platforms.add(Platform(30f, 95f, 32f, 5f, PlatformType.NORMAL))
        platforms.add(Platform(90f, 65f, 32f, 5f, PlatformType.SPRING))
        platforms.add(Platform(40f, 35f, 32f, 5f, PlatformType.MOVING, vx = 25f))
        platforms.add(Platform(80f, 5f, 32f, 5f, PlatformType.NORMAL))
        spawnHigherPlatforms(-300f)
    }

    private fun spawnHigherPlatforms(topY: Float) {
        var currentY = if (platforms.isNotEmpty()) platforms.last().y else 0f
        while (currentY > topY - 144f) {
            currentY -= 26f + Random.nextFloat() * 16f
            val rx = 10f + Random.nextFloat() * 100f
            val roll = Random.nextFloat()
            val type = when {
                roll < 0.25f -> PlatformType.MOVING
                roll < 0.45f -> PlatformType.CRUMBLY
                roll < 0.60f -> PlatformType.SPRING
                else -> PlatformType.NORMAL
            }
            val vx = if (type == PlatformType.MOVING) {
                (if (Random.nextBoolean()) 1f else -1f) * (20f + Random.nextFloat() * 30f)
            } else 0f

            platforms.add(Platform(rx, currentY, 30f, 5f, type, vx))

            if (Random.nextFloat() < 0.3f) {
                stars.add(Star(rx + 12f, currentY - 10f))
            }
        }
    }

    private fun resetGame() {
        px = 80f
        py = 100f
        pvx = 0f
        pvy = -180f
        cameraY = 0f
        score = 0
        superJumps = 1
        isGameOver = false
        generateInitialPlatforms()
    }

    override fun init(surface: CartridgeSurface) {
        resetGame()
        val saved = surface.load()
        if (saved != null) {
            try {
                val json = JSONObject(saved)
                highScore = json.optInt("highScore", 0)
            } catch (_: Exception) {}
        }
    }

    override fun update(input: InputSnapshot, dt: Float) {
        if (isGameOver) {
            if (input.pressed.a || input.pressed.start) {
                resetGame()
            }
            return
        }

        // Tilt controls
        val moveAccel = 350f
        if (input.held.left) pvx -= moveAccel * dt
        else if (input.held.right) pvx += moveAccel * dt
        else pvx *= 0.85f

        pvx = pvx.coerceIn(-120f, 120f)
        px += pvx * dt

        // Wrap around screen
        if (px < -6f) px = 166f
        else if (px > 166f) px = -6f

        // Super boost jump
        if (input.pressed.a && superJumps > 0) {
            superJumps--
            pvy = -340f
        }

        // Gravity
        pvy += 450f * dt
        py += pvy * dt

        // Camera scroll
        if (py < cameraY + 70f) {
            val diff = (cameraY + 70f) - py
            cameraY -= diff
            score = score.coerceAtLeast((-cameraY).toInt())
            spawnHigherPlatforms(cameraY)
        }

        // Moving platforms
        platforms.forEach { pl ->
            if (pl.type == PlatformType.MOVING && pl.vx != 0f) {
                pl.x += pl.vx * dt
                if (pl.x < 10f || pl.x + pl.w > 150f) {
                    pl.vx *= -1f
                }
            }
        }

        // Platform bounce (only when falling)
        if (pvy > 0f) {
            for (pl in platforms) {
                if (pl.broken) continue
                if (px + 6f > pl.x && px - 6f < pl.x + pl.w && py + 6f >= pl.y && py <= pl.y + 8f) {
                    when (pl.type) {
                        PlatformType.SPRING -> pvy = -320f
                        PlatformType.CRUMBLY -> {
                            pl.broken = true
                            pvy = -180f
                        }
                        else -> pvy = -210f
                    }
                    break
                }
            }
        }

        // Star collection
        stars.forEach { s ->
            if (!s.collected && hypot(s.x - px, s.y - py) < 12f) {
                s.collected = true
                score += 25
            }
        }

        // Clean offscreen
        platforms.removeAll { it.y > cameraY + 180f }
        stars.removeAll { it.y > cameraY + 180f }

        // Fall check
        if (py > cameraY + 144f) {
            isGameOver = true
            if (score > highScore) {
                highScore = score
            }
        }
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, surface.width.toFloat(), surface.height.toFloat(), 0xFF0F241A.toInt())

        // Platforms
        platforms.forEach { pl ->
            val screenY = pl.y - cameraY
            if (screenY >= -10f && screenY <= surface.height + 10f && !pl.broken) {
                val color = when (pl.type) {
                    PlatformType.NORMAL -> 0xFF00FF33.toInt()
                    PlatformType.MOVING -> 0xFF00DDFF.toInt()
                    PlatformType.SPRING -> 0xFFFFFF00.toInt()
                    PlatformType.CRUMBLY -> 0xFFAA6644.toInt()
                }
                surface.fillRect(pl.x, screenY, pl.w, pl.h, color)
                if (pl.type == PlatformType.SPRING) {
                    surface.fillRect(pl.x + pl.w / 2 - 3, screenY - 3, 6f, 3f, 0xFFFF5500.toInt())
                }
            }
        }

        // Stars
        stars.forEach { s ->
            if (!s.collected) {
                val screenY = s.y - cameraY
                if (screenY >= -10f && screenY <= surface.height + 10f) {
                    val color = if ((System.currentTimeMillis() / 120) % 2 == 0L) 0xFFFFFF00.toInt() else 0xFFFFFFAA.toInt()
                    surface.fillRect(s.x - 2f, screenY - 2f, 5f, 5f, color)
                }
            }
        }

        // Player
        val screenPlayerY = py - cameraY
        surface.fillRect(px - 5f, screenPlayerY - 6f, 10f, 10f, 0xFFFFFFFF.toInt())
        surface.fillRect(px - 3f, screenPlayerY - 4f, 6f, 6f, 0xFF00FF33.toInt())
        val eyeX = if (pvx >= 0) px else px - 3f
        surface.fillRect(eyeX, screenPlayerY - 4f, 2f, 2f, 0xFF000000.toInt())

        // Top HUD
        surface.fillRect(0f, 0f, surface.width.toFloat(), 12f, 0x88000000.toInt())
        surface.drawText("ALT:${score}m", 4f, 10f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("BEST:${highScore}m", 64f, 10f, 0xFF00FF33.toInt(), 8f)
        val boostColor = if (superJumps > 0) 0xFFFFFF00.toInt() else 0xFF555555.toInt()
        surface.drawText("BOOST:$superJumps", 118f, 10f, boostColor, 8f)

        // Game Over
        if (isGameOver) {
            surface.fillRect(20f, 45f, 120f, 65f, 0xDD000000.toInt())
            surface.strokeRect(20f, 45f, 120f, 65f, 0xFF00FF33.toInt(), 1f)
            surface.drawText("SPLATTED!", 48f, 64f, 0xFFFF3333.toInt(), 10f)
            surface.drawText("ALTITUDE: ${score}m", 38f, 80f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("PRESS A / START", 34f, 96f, 0xFFFFFF00.toInt(), 8f)
        }
    }

    override fun saveState(): String {
        val json = JSONObject().apply {
            put("highScore", highScore)
            put("lastScore", score)
        }
        return json.toString()
    }
}
