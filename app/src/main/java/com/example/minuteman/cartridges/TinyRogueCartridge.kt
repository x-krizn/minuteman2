package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot
import org.json.JSONObject
import kotlin.random.Random

class TinyRogueCartridge : Cartridge {
    override val id: String = "tiny_rogue"
    override val name: String = "TINY ROGUE"
    override val version: String = "1.0"
    override val author: String = "Minuteman Dungeon"
    override val description: String = "Dungeon crawler RPG. A: Sword slash, D-Pad: Move."

    private val tileSize = 12
    private val cols = 13
    private val rows = 11
    private val offsetX = 2
    private val offsetY = 12

    private enum class EnemyType { SLIME, BAT, SKEL }
    private data class Enemy(var x: Int, var y: Int, val type: EnemyType, var hp: Int, var moveTimer: Float)
    private data class Particle(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float, val color: Int)
    private data class SlashBox(val x: Float, val y: Float, val w: Float, val h: Float)

    private var playerX = 6
    private var playerY = 8
    private var playerFacing = "down"
    private var slashTimer = 0f
    private var slashBox: SlashBox? = null
    private var hearts = 3
    private val maxHearts = 3
    private var keys = 1
    private var gems = 0
    private var floor = 1
    private var invulnTimer = 0f
    private var gameState = "PLAYING" // PLAYING, GAMEOVER, VICTORY

    private val map = Array(rows) { IntArray(cols) }
    private val enemies = mutableListOf<Enemy>()
    private val particles = mutableListOf<Particle>()

    private fun generateRoom(floorNum: Int) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
                    map[r][c] = 1 // Wall
                } else {
                    map[r][c] = 0 // Floor
                }
            }
        }

        // Obstacles
        val obstacleCount = 6 + (floorNum * 2).coerceAtMost(8)
        for (i in 0 until obstacleCount) {
            val rx = 2 + Random.nextInt(cols - 4)
            val ry = 2 + Random.nextInt(rows - 4)
            if (rx != 6 || ry != 8) {
                map[ry][rx] = if (Random.nextBoolean()) 1 else 2 // 1: wall, 2: pot
            }
        }

        // Chest
        val chestX = 2 + Random.nextInt(cols - 4)
        val chestY = 2 + Random.nextInt(3)
        map[chestY][chestX] = 3

        // Stairs
        map[1][cols - 2] = 5

        // Enemies
        enemies.clear()
        val count = 3 + floorNum.coerceAtMost(5)
        for (i in 0 until count) {
            val ex = 1 + Random.nextInt(cols - 2)
            val ey = 1 + Random.nextInt(rows - 5)
            if (map[ey][ex] == 0) {
                val t = when {
                    Random.nextFloat() > 0.6f -> EnemyType.SKEL
                    Random.nextFloat() > 0.4f -> EnemyType.BAT
                    else -> EnemyType.SLIME
                }
                enemies.add(Enemy(ex, ey, t, 2, Random.nextFloat() * 0.5f))
            }
        }

        playerX = 6
        playerY = rows - 2
        invulnTimer = 0.5f
    }

    private fun spawnDust(tileX: Int, tileY: Int, color: Int = 0xFFFFFF00.toInt()) {
        val cx = (tileX * tileSize + offsetX + tileSize / 2).toFloat()
        val cy = (tileY * tileSize + offsetY + tileSize / 2).toFloat()
        for (i in 0 until 6) {
            particles.add(
                Particle(
                    x = cx,
                    y = cy,
                    vx = (Random.nextFloat() - 0.5f) * 40f,
                    vy = (Random.nextFloat() - 0.5f) * 40f,
                    life = 0.3f,
                    color = color
                )
            )
        }
    }

    override fun init(surface: CartridgeSurface) {
        hearts = 3
        keys = 1
        gems = 0
        floor = 1
        gameState = "PLAYING"

        val saved = surface.load()
        if (saved != null) {
            try {
                val json = JSONObject(saved)
                gems = json.optInt("gems", 0)
                val savedFloor = json.optInt("floor", 1)
                if (savedFloor in 1..5) floor = savedFloor
                keys = json.optInt("keys", 1)
            } catch (_: Exception) {}
        }

        generateRoom(floor)
    }

    override fun update(input: InputSnapshot, dt: Float) {
        if (gameState != "PLAYING") {
            if (input.pressed.a || input.pressed.start) {
                hearts = 3
                keys = 1
                gems = 0
                floor = 1
                gameState = "PLAYING"
                generateRoom(1)
            }
            return
        }

        if (invulnTimer > 0f) invulnTimer -= dt
        if (slashTimer > 0f) {
            slashTimer -= dt
            if (slashTimer <= 0f) slashBox = null
        }

        // Movement
        var dx = 0
        var dy = 0
        if (input.pressed.left) { dx = -1; playerFacing = "left" }
        else if (input.pressed.right) { dx = 1; playerFacing = "right" }
        else if (input.pressed.up) { dy = -1; playerFacing = "up" }
        else if (input.pressed.down) { dy = 1; playerFacing = "down" }

        if (dx != 0 || dy != 0) {
            val nx = playerX + dx
            val ny = playerY + dy
            if (nx in 0 until cols && ny in 0 until rows) {
                when (map[ny][nx]) {
                    0 -> { // floor
                        playerX = nx
                        playerY = ny
                    }
                    2 -> { // pot
                        map[ny][nx] = 0
                        gems += 5
                        spawnDust(nx, ny, 0xFF00FF33.toInt())
                    }
                    3 -> { // chest
                        if (keys > 0) {
                            keys--
                            gems += 50
                            map[ny][nx] = 0
                            spawnDust(nx, ny, 0xFFFFFF00.toInt())
                        }
                    }
                    5 -> { // stairs
                        floor++
                        if (floor > 5) {
                            gameState = "VICTORY"
                        } else {
                            generateRoom(floor)
                        }
                    }
                }
            }
        }

        // Attack
        if (input.pressed.a && slashTimer <= 0f) {
            slashTimer = 0.22f
            var sx = playerX
            var sy = playerY
            when (playerFacing) {
                "left" -> sx--
                "right" -> sx++
                "up" -> sy--
                "down" -> sy++
            }
            slashBox = SlashBox(
                x = (sx * tileSize + offsetX).toFloat(),
                y = (sy * tileSize + offsetY).toFloat(),
                w = tileSize.toFloat(),
                h = tileSize.toFloat()
            )

            // Break pot
            if (sx in 0 until cols && sy in 0 until rows && map[sy][sx] == 2) {
                map[sy][sx] = 0
                gems += 5
                spawnDust(sx, sy, 0xFF00FF33.toInt())
            }

            // Damage enemies
            val iter = enemies.iterator()
            while (iter.hasNext()) {
                val en = iter.next()
                if (en.x == sx && en.y == sy) {
                    en.hp--
                    spawnDust(sx, sy, 0xFFFF3333.toInt())
                    if (en.hp <= 0) {
                        iter.remove()
                        gems += 15
                    }
                }
            }
        }

        // Enemy AI
        enemies.forEach { en ->
            en.moveTimer -= dt
            if (en.moveTimer <= 0f) {
                en.moveTimer = when (en.type) {
                    EnemyType.BAT -> 0.35f
                    EnemyType.SKEL -> 0.70f
                    EnemyType.SLIME -> 0.90f
                }
                var edx = 0
                var edy = 0
                if (Random.nextFloat() < 0.7f) {
                    if (en.x < playerX) edx = 1
                    else if (en.x > playerX) edx = -1
                    else if (en.y < playerY) edy = 1
                    else if (en.y > playerY) edy = -1
                } else {
                    val dirs = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
                    val d = dirs[Random.nextInt(dirs.size)]
                    edx = d.first
                    edy = d.second
                }

                val tx = en.x + edx
                val ty = en.y + edy

                if (tx == playerX && ty == playerY) {
                    if (invulnTimer <= 0f) {
                        hearts--
                        invulnTimer = 1.0f
                        spawnDust(playerX, playerY, 0xFFFF3333.toInt())
                        if (hearts <= 0) {
                            gameState = "GAMEOVER"
                        }
                    }
                } else if (tx > 0 && tx < cols - 1 && ty > 0 && ty < rows - 1 && map[ty][tx] == 0) {
                    en.x = tx
                    en.y = ty
                }
            }
        }

        // Particles
        particles.removeAll { p ->
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.life <= 0f
        }
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, surface.width.toFloat(), surface.height.toFloat(), 0xFF0F2010.toInt())

        // Top HUD
        surface.fillRect(0f, 0f, surface.width.toFloat(), 11f, 0xFF0A150A.toInt())
        var heartsStr = ""
        for (i in 0 until maxHearts) {
            heartsStr += if (i < hearts) "HP " else "   "
        }
        surface.drawText(heartsStr, 4f, 9f, 0xFFFF3333.toInt(), 8f)
        surface.drawText("G:$gems", 50f, 9f, 0xFFFFFF00.toInt(), 8f)
        surface.drawText("K:$keys", 95f, 9f, 0xFF00AAFF.toInt(), 8f)
        surface.drawText("F:$floor", 130f, 9f, 0xFF00FF33.toInt(), 8f)

        // Dungeon Grid
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val px = (c * tileSize + offsetX).toFloat()
                val py = (r * tileSize + offsetY).toFloat()
                when (map[r][c]) {
                    1 -> { // Wall
                        surface.fillRect(px, py, tileSize.toFloat(), tileSize.toFloat(), 0xFF1C3820.toInt())
                        surface.strokeRect(px + 1f, py + 1f, (tileSize - 2).toFloat(), (tileSize - 2).toFloat(), 0x5500FF33)
                    }
                    2 -> { // Pot
                        surface.fillRect(px + 2f, py + 3f, (tileSize - 4).toFloat(), (tileSize - 5).toFloat(), 0xFF8A5A2A.toInt())
                        surface.fillRect(px + 4f, py + 1f, (tileSize - 8).toFloat(), 2f, 0xFFFFFFBB.toInt())
                    }
                    3 -> { // Chest
                        surface.fillRect(px + 1f, py + 2f, (tileSize - 2).toFloat(), (tileSize - 4).toFloat(), 0xFFB8860B.toInt())
                        surface.fillRect(px + 5f, py + 5f, 2f, 3f, 0xFFFFFF00.toInt())
                    }
                    5 -> { // Stairs
                        surface.fillRect(px + 1f, py + 1f, (tileSize - 2).toFloat(), (tileSize - 2).toFloat(), 0xFF000000.toInt())
                        surface.fillRect(px + 3f, py + 3f, 6f, 2f, 0xFF00FF33.toInt())
                        surface.fillRect(px + 5f, py + 5f, 4f, 2f, 0xFF00FF33.toInt())
                        surface.fillRect(px + 7f, py + 7f, 2f, 2f, 0xFF00FF33.toInt())
                    }
                }
            }
        }

        // Enemies
        enemies.forEach { en ->
            val ex = (en.x * tileSize + offsetX).toFloat()
            val ey = (en.y * tileSize + offsetY).toFloat()
            when (en.type) {
                EnemyType.SLIME -> {
                    surface.drawCircle(ex + tileSize / 2f, ey + tileSize / 2f + 1f, 4f, 0xFF44FF44.toInt(), true)
                    surface.fillRect(ex + 4f, ey + 4f, 1f, 2f, 0xFF000000.toInt())
                    surface.fillRect(ex + 7f, ey + 4f, 1f, 2f, 0xFF000000.toInt())
                }
                EnemyType.BAT -> {
                    surface.fillRect(ex + 2f, ey + 4f, 8f, 4f, 0xFFDD4444.toInt())
                    surface.fillRect(ex + 5f, ey + 5f, 2f, 2f, 0xFFFFFF00.toInt())
                }
                EnemyType.SKEL -> {
                    surface.fillRect(ex + 3f, ey + 2f, 6f, 7f, 0xFFEEEEEE.toInt())
                    surface.fillRect(ex + 4f, ey + 4f, 1f, 2f, 0xFF000000.toInt())
                    surface.fillRect(ex + 7f, ey + 4f, 1f, 2f, 0xFF000000.toInt())
                }
            }
        }

        // Player
        if (invulnTimer <= 0f || (System.currentTimeMillis() / 80) % 2 == 0L) {
            val px = (playerX * tileSize + offsetX).toFloat()
            val py = (playerY * tileSize + offsetY).toFloat()
            surface.fillRect(px + 2f, py + 4f, 8f, 7f, 0xFF0088FF.toInt())
            surface.fillRect(px + 3f, py + 1f, 6f, 5f, 0xFFFFDDBB.toInt())
            val eyeColor = 0xFF000000.toInt()
            when (playerFacing) {
                "left" -> surface.fillRect(px + 3f, py + 3f, 1f, 2f, eyeColor)
                "right" -> surface.fillRect(px + 7f, py + 3f, 1f, 2f, eyeColor)
                else -> {
                    surface.fillRect(px + 4f, py + 3f, 1f, 2f, eyeColor)
                    surface.fillRect(px + 7f, py + 3f, 1f, 2f, eyeColor)
                }
            }
        }

        // Slash
        slashBox?.let { sb ->
            surface.fillRect(sb.x + 2f, sb.y + 2f, sb.w - 4f, sb.h - 4f, 0xFFFFFFFF.toInt())
            surface.strokeRect(sb.x, sb.y, sb.w, sb.h, 0xFF00AAFF.toInt(), 1f)
        }

        // Particles
        particles.forEach { p ->
            surface.fillRect(p.x, p.y, 2f, 2f, p.color)
        }

        // Modals
        if (gameState == "GAMEOVER") {
            surface.fillRect(20f, 35f, 120f, 75f, 0xDD000000.toInt())
            surface.strokeRect(20f, 35f, 120f, 75f, 0xFFFF3333.toInt(), 2f)
            surface.drawText("YOU PERISHED", 38f, 55f, 0xFFFF3333.toInt(), 10f)
            surface.drawText("GEMS: $gems", 44f, 72f, 0xFFFFFF00.toInt(), 8f)
            surface.drawText("FLOOR: B$floor", 44f, 84f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("PRESS A / START", 34f, 100f, 0xFF00FF33.toInt(), 8f)
        } else if (gameState == "VICTORY") {
            surface.fillRect(20f, 35f, 120f, 75f, 0xDD000000.toInt())
            surface.strokeRect(20f, 35f, 120f, 75f, 0xFFFFFF00.toInt(), 2f)
            surface.drawText("DUNGEON CLEARED!", 26f, 55f, 0xFFFFFF00.toInt(), 10f)
            surface.drawText("TOTAL GEMS: $gems", 36f, 74f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("YOU ARE A LEGEND!", 32f, 88f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("PRESS A / START", 34f, 102f, 0xFFFFFF00.toInt(), 8f)
        }
    }

    override fun saveState(): String {
        val json = JSONObject().apply {
            put("gems", gems)
            put("floor", floor)
            put("keys", keys)
        }
        return json.toString()
    }
}
