package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class KnightCartridge : Cartridge {
    override val id: String = "knight"
    override val name: String = "KNIGHT (PROTO)"
    override val version: String = "0.3"
    override val author: String = "Minuteman Metroidvania"
    override val description: String = "Metroidvania adventure. D-Pad: Move, A: Jump, B: Sword Combo."

    private val tileSize = 16
    private val cols = 10
    private val rows = 9
    private val halfWidth = 5f
    private val bodyH = 16f
    private val gravity = 700f
    private val jumpPower = 270f
    private val runSpeed = 60f
    private val maxFall = 320f

    private data class Enemy(
        var x: Float,
        var y: Float,
        var dir: Float,
        var hp: Int,
        var stun: Float,
        val kind: String, // "slime" or "bat"
        var animT: Float,
        val baseY: Float
    )

    private data class Pickup(val x: Float, val y: Float, val type: String, val id: String)
    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Int)

    private var px = 32f
    private var py = 128f
    private var pvx = 0f
    private var pvy = 0f
    private var face = 1f
    private var ground = false
    private var jumping = false
    private var coyote = 0f
    private var buffer = 0f
    private var airJumps = 0
    private var hasDoubleJump = false
    private var hp = 3
    private val maxHp = 3
    private var coins = 0
    private var keys = 0
    private var inv = 0f
    private var atk = 0f
    private var atkV = 0 // 0 stab, 1 up, 2 forward, 3 down
    private var atkCd = 0f
    private var currentRoom = "0,0"
    private var rx = 0
    private var ry = 0
    private var bannerMsg = ""
    private var bannerTime = 0f

    private val staticRooms = mapOf(
        "0,0" to listOf(
            "##########",
            "#........#",
            "#........#",
            "#........#",
            "#..ooo...#",
            "#..###...#",
            "..........",
            "..........",
            "##########"
        ),
        "1,0" to listOf(
            "####..####",
            "#........#",
            "#........#",
            "#..####..#",
            "#........#",
            "#........#",
            "..........",
            ".1...E....",
            "##########"
        ),
        "2,0" to listOf(
            "##########",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            ".........#",
            ".......D.L",
            "##########"
        ),
        "3,0" to listOf(
            "##########",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            ".G.B.Y.oo.",
            "##########"
        ),
        "1,-1" to listOf(
            "##########",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#........#",
            "#.E...KH.#",
            "####..####"
        )
    )

    private val collectedItems = mutableSetOf<String>()
    private val enemies = mutableListOf<Enemy>()
    private val pickups = mutableListOf<Pickup>()
    private val particles = mutableListOf<Particle>()

    private fun getRoomLines(rx: Int, ry: Int): List<String> {
        val key = "$rx,$ry"
        return staticRooms[key] ?: listOf(
            "##########",
            "#........#",
            "#........#",
            "#...##...#",
            "#........#",
            "#........#",
            "..........",
            "....E.....",
            "##########"
        )
    }

    private fun isSolid(tx: Int, ty: Int): Boolean {
        if (tx !in 0 until cols || ty !in 0 until rows) return false
        val lines = getRoomLines(rx, ry)
        val ch = lines[ty][tx]
        if (ch == '#') return true
        if (ch == 'L' && !collectedItems.contains("$currentRoom:L")) return true
        return false
    }

    private fun hits(x: Float, y: Float): Boolean {
        val x0 = ((x - halfWidth) / tileSize).toInt()
        val x1 = ((x + halfWidth - 0.01f) / tileSize).toInt()
        val y0 = ((y - bodyH) / tileSize).toInt()
        val y1 = ((y - 0.01f) / tileSize).toInt()
        for (ty in y0..y1) {
            for (tx in x0..x1) {
                if (isSolid(tx, ty)) return true
            }
        }
        return false
    }

    private fun moveX(dx: Float) {
        val sign = if (dx > 0) 1f else -1f
        var dist = abs(dx)
        while (dist > 0) {
            val step = dist.coerceAtMost(1f)
            if (hits(px + sign * step, py)) {
                pvx = 0f
                return
            }
            px += sign * step
            dist -= step
        }
    }

    private fun moveY(dy: Float) {
        ground = false
        val sign = if (dy > 0) 1f else -1f
        var dist = abs(dy)
        while (dist > 0) {
            val step = dist.coerceAtMost(1f)
            if (hits(px, py + sign * step)) {
                if (sign > 0) {
                    ground = true
                }
                pvy = 0f
                return
            }
            py += sign * step
            dist -= step
        }
    }

    private fun enterRoom(newRoom: String) {
        currentRoom = newRoom
        val parts = newRoom.split(",").map { it.toIntOrNull() ?: 0 }
        rx = parts[0]
        ry = parts[1]
        enemies.clear()
        pickups.clear()

        val lines = getRoomLines(rx, ry)
        lines.forEachIndexed { ty, row ->
            row.forEachIndexed { tx, c ->
                val id = "$currentRoom:$tx,$ty"
                when (c) {
                    'E' -> {
                        val isFlying = ty < 4
                        enemies.add(
                            Enemy(
                                x = (tx * tileSize + 8).toFloat(),
                                y = ((ty + 1) * tileSize).toFloat(),
                                dir = -1f,
                                hp = 2,
                                stun = 0f,
                                kind = if (isFlying) "bat" else "slime",
                                animT = Random.nextFloat() * 2f,
                                baseY = ((ty + 1) * tileSize).toFloat()
                            )
                        )
                    }
                    'o', 'K', 'D', 'H', 'G', 'B', 'Y', '1' -> {
                        if (!collectedItems.contains(id)) {
                            pickups.add(Pickup((tx * tileSize + 8).toFloat(), (ty * tileSize + 8).toFloat(), c.toString(), id))
                        }
                    }
                }
            }
        }

        bannerMsg = "DUNGEON [$rx,$ry]"
        bannerTime = 1.8f
    }

    private fun respawn() {
        px = 32f
        py = 128f
        pvx = 0f
        pvy = 0f
        hp = maxHp
        inv = 0f
        atk = 0f
        enterRoom("0,0")
    }

    override fun init(surface: CartridgeSurface) {
        collectedItems.clear()
        hasDoubleJump = false
        coins = 0
        keys = 0
        respawn()
    }

    override fun update(input: InputSnapshot, dt: Float) {
        coyote -= dt
        buffer -= dt
        inv -= dt
        atk -= dt
        atkCd -= dt
        bannerTime -= dt

        // Horizontal input
        var sx = 0f
        if (input.held.left) sx = -1f
        else if (input.held.right) sx = 1f

        pvx = sx * runSpeed
        if (sx > 0.1f) face = 1f
        else if (sx < -0.1f) face = -1f

        // Attack
        if (input.pressed.b && atkCd <= 0f) {
            val sy = if (input.held.up) -1f else if (input.held.down) 1f else 0f
            atkV = if (sy < -0.5f) 1 else if (sy > 0.5f) 3 else if (abs(sx) > 0.3f) 2 else 0
            atk = 0.2f
            atkCd = 0.35f
        }

        // Jump
        if (input.pressed.a) buffer = 0.1f

        if (buffer > 0f) {
            if (coyote > 0f) {
                pvy = -jumpPower
                coyote = 0f
                buffer = 0f
                jumping = true
            } else if (airJumps > 0) {
                pvy = -jumpPower
                airJumps--
                buffer = 0f
                jumping = true
            }
        }

        if (jumping && !input.held.a && pvy < -jumpPower * 0.4f) {
            pvy = -jumpPower * 0.4f
        }
        if (pvy >= 0f) jumping = false

        pvy = (pvy + gravity * dt).coerceAtMost(maxFall)

        moveX(pvx * dt)
        moveY(pvy * dt)

        if (ground) {
            coyote = 0.08f
            airJumps = if (hasDoubleJump) 1 else 0
        }

        // Screen boundary room switching
        if (px < 0f) {
            px += 160f
            enterRoom("${rx - 1},$ry")
        } else if (px >= 160f) {
            px -= 160f
            enterRoom("${rx + 1},$ry")
        }
        if (py < 0f) {
            py += 144f
            enterRoom("$rx,${ry - 1}")
        } else if (py > 144f) {
            py -= 144f
            enterRoom("$rx,${ry + 1}")
        }

        // Enemies patrol
        enemies.forEach { e ->
            if (e.hp <= 0) return@forEach
            e.animT += dt
            if (e.stun > 0f) {
                e.stun -= dt
                return@forEach
            }

            if (e.kind == "slime") {
                val nx = e.x + e.dir * 22f * dt
                val ahead = nx + e.dir * 7f
                val wall = isSolid((ahead / tileSize).toInt(), ((e.y - 1) / tileSize).toInt())
                val floorAhead = isSolid((ahead / tileSize).toInt(), ((e.y + 1) / tileSize).toInt())
                if (wall || !floorAhead || ahead < 4f || ahead > 156f) {
                    e.dir = -e.dir
                } else {
                    e.x = nx
                }
            } else {
                e.x += e.dir * 30f * dt
                e.y = e.baseY + sin(e.animT * 4f) * 6f
                if (e.x < 8f || e.x > 152f || isSolid((e.x / tileSize).toInt(), (e.y / tileSize).toInt())) {
                    e.dir = -e.dir
                }
            }
        }

        // Sword hit
        if (atk > 0f) {
            val swordBoxX = if (face > 0) px + 4f else px - 16f
            val swordBoxY = py - 12f
            enemies.forEach { e ->
                if (e.hp > 0 && abs(e.x - swordBoxX) < 14f && abs(e.y - swordBoxY) < 14f) {
                    e.hp--
                    e.stun = 0.25f
                    e.x += face * 8f
                    if (e.hp <= 0 && Random.nextFloat() < 0.6f) {
                        pickups.add(Pickup(e.x, e.y - 6f, "o", "drop_${System.currentTimeMillis()}"))
                    }
                }
            }
        }

        // Player damage
        if (inv <= 0f) {
            enemies.find { it.hp > 0 && abs(it.x - px) < 10f && abs(it.y - py) < 12f }?.let { foe ->
                hp--
                inv = 1.0f
                pvx = (if (px < foe.x) -1f else 1f) * 110f
                pvy = -140f
                if (hp <= 0) {
                    respawn()
                }
            }
        }

        // Pickups
        pickups.removeAll { pk ->
            if (abs(pk.x - px) < 10f && abs(pk.y - (py - 8f)) < 12f) {
                collectedItems.add(pk.id)
                when (pk.type) {
                    "o" -> coins++
                    "K" -> {
                        keys++
                        bannerMsg = "OBTAINED KEY"
                        bannerTime = 1.5f
                    }
                    "D" -> {
                        hasDoubleJump = true
                        bannerMsg = "DOUBLE JUMP UNLOCKED"
                        bannerTime = 2.0f
                    }
                    else -> coins += 2
                }
                true
            } else false
        }
    }

    override fun draw(surface: CartridgeSurface) {
        // Backdrop
        surface.fillRect(0f, 0f, 160f, 144f, 0xFF0B0F14.toInt())

        // Geometry
        val lines = getRoomLines(rx, ry)
        lines.forEachIndexed { ty, row ->
            row.forEachIndexed { tx, c ->
                val qx = (tx * tileSize).toFloat()
                val qy = (ty * tileSize).toFloat()
                if (c == '#') {
                    surface.fillRect(qx, qy, tileSize.toFloat(), tileSize.toFloat(), 0xFF1C2B1F.toInt())
                    surface.strokeRect(qx + 1f, qy + 1f, (tileSize - 2).toFloat(), (tileSize - 2).toFloat(), 0x4400FF33)
                } else if (c == 'L' && !collectedItems.contains("$currentRoom:L")) {
                    surface.fillRect(qx + 4f, qy + 2f, 8f, 12f, 0xFFB8860B.toInt())
                    surface.fillRect(qx + 7f, qy + 6f, 2f, 4f, 0xFF000000.toInt())
                }
            }
        }

        // Pickups
        pickups.forEach { pk ->
            val color = when (pk.type) {
                "o" -> 0xFFFFD700.toInt()
                "K" -> 0xFFFFAA00.toInt()
                "D" -> 0xFF4DEEEA.toInt()
                else -> 0xFFFF44AA.toInt()
            }
            surface.drawCircle(pk.x, pk.y, 3.5f, color, true)
        }

        // Enemies
        enemies.forEach { e ->
            if (e.hp <= 0) return@forEach
            val color = if (e.stun > 0f) 0xFFFFFFFF.toInt() else if (e.kind == "slime") 0xFFB347D4.toInt() else 0xFF473063.toInt()
            surface.fillRect(e.x - 5f, e.y - 8f, 10f, 8f, color)
            surface.fillRect(e.x - 2f, e.y - 6f, 2f, 2f, 0xFFFF2244.toInt())
        }

        // Knight Player
        if (inv <= 0f || (System.currentTimeMillis() / 60) % 2 == 0L) {
            surface.fillRect(px - halfWidth, py - bodyH, halfWidth * 2, bodyH, 0xFFDDDDDD.toInt())
            surface.fillRect(px - 3f, py - 14f, 6f, 5f, 0xFF00B0FF.toInt())
            surface.fillRect(if (face > 0) px + 1f else px - 3f, py - 12f, 2f, 2f, 0xFF000000.toInt())
        }

        // Sword Slash
        if (atk > 0f) {
            val sx = if (face > 0) px + 6f else px - 18f
            surface.fillRect(sx, py - 10f, 12f, 4f, 0xFFFFFFFF.toInt())
            surface.fillRect(sx + 2f, py - 9f, 8f, 2f, 0xFF66CCFF.toInt())
        }

        // Hearts HUD
        for (i in 0 until maxHp) {
            val color = if (i < hp) 0xFFFF2244.toInt() else 0xFF330808.toInt()
            surface.fillRect((4 + i * 9).toFloat(), 4f, 7f, 6f, color)
        }

        // Coins & Keys HUD
        surface.drawText("$$coins", 130f, 10f, 0xFFFFD700.toInt(), 8f)
        if (keys > 0) {
            surface.drawText("KEY:$keys", 85f, 10f, 0xFFFFAA00.toInt(), 8f)
        }

        // Banner
        if (bannerTime > 0f) {
            val tw = surface.measureText(bannerMsg, 8f)
            surface.fillRect(80f - tw / 2f - 4f, 24f, tw + 8f, 12f, 0xDD000000.toInt())
            surface.strokeRect(80f - tw / 2f - 4f, 24f, tw + 8f, 12f, 0xFF4DEEEA.toInt(), 1f)
            surface.drawText(bannerMsg, 80f - tw / 2f, 33f, 0xFFFFFFFF.toInt(), 8f)
        }
    }
}
