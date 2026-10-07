package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class StarPatrolCartridge : Cartridge {
    override val id: String = "star_patrol"
    override val name: String = "STAR PATROL"
    override val version: String = "1.0"
    override val author: String = "Minuteman Arcade"
    override val description: String = "Space shooter. A: Laser, B: Shield Boost, D-Pad: Fly."

    private data class Star(var x: Float, var y: Float, val s: Float, val speed: Float)
    private data class Laser(var x: Float, var y: Float, val vy: Float)
    private enum class EnemyType { DRONE, ROCK, CRUISER }
    private data class Enemy(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var hp: Int,
        val type: EnemyType,
        val r: Float
    )
    private data class Particle(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float)
    private enum class PowerupType { SHIELD, GUN }
    private data class Powerup(var x: Float, var y: Float, val vy: Float, val type: PowerupType)

    private var playerX = 80f
    private var playerY = 120f
    private val playerSpeed = 75f
    private var shield = 100f
    private var score = 0
    private var highScore = 0
    private var lives = 3
    private var wave = 1
    private var isGameOver = false
    private var shootTimer = 0f
    private var spawnTimer = 0f
    private var invulnTimer = 0f
    private var gunLevel = 1

    private val stars = mutableListOf<Star>()
    private val lasers = mutableListOf<Laser>()
    private val enemies = mutableListOf<Enemy>()
    private val particles = mutableListOf<Particle>()
    private val powerups = mutableListOf<Powerup>()

    private fun resetGame() {
        playerX = 80f
        playerY = 120f
        shield = 100f
        score = 0
        lives = 3
        wave = 1
        isGameOver = false
        lasers.clear()
        enemies.clear()
        particles.clear()
        powerups.clear()
        gunLevel = 1
        invulnTimer = 2f
    }

    private fun spawnExplosion(x: Float, y: Float, count: Int = 8) {
        for (i in 0 until count) {
            val angle = (2 * PI * i / count + (Random.nextFloat() - 0.5f)).toFloat()
            val spd = 20f + Random.nextFloat() * 40f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    life = 0.3f + Random.nextFloat() * 0.3f
                )
            )
        }
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

        stars.clear()
        for (i in 0 until 35) {
            stars.add(
                Star(
                    x = Random.nextFloat() * surface.width,
                    y = Random.nextFloat() * surface.height,
                    s = if (Random.nextFloat() > 0.7f) 2f else 1f,
                    speed = 15f + Random.nextFloat() * 35f
                )
            )
        }
    }

    override fun update(input: InputSnapshot, dt: Float) {
        // Stars
        stars.forEach { s ->
            s.y += s.speed * dt
            if (s.y > 144f) {
                s.y = 0f
                s.x = Random.nextFloat() * 160f
            }
        }

        if (isGameOver) {
            if (input.pressed.a || input.pressed.start) {
                resetGame()
            }
            return
        }

        var currentSpeed = playerSpeed
        if (input.held.b && shield > 5f) {
            currentSpeed = playerSpeed * 1.5f
            shield -= 15f * dt
        } else {
            shield = (shield + 4f * dt).coerceAtMost(100f)
        }

        if (input.held.left) playerX -= currentSpeed * dt
        if (input.held.right) playerX += currentSpeed * dt
        if (input.held.up) playerY -= currentSpeed * dt
        if (input.held.down) playerY += currentSpeed * dt

        playerX = playerX.coerceIn(8f, 152f)
        playerY = playerY.coerceIn(14f, 136f)

        if (invulnTimer > 0f) invulnTimer -= dt

        // Laser shoot
        shootTimer -= dt
        if ((input.held.a || input.pressed.a) && shootTimer <= 0f) {
            shootTimer = if (gunLevel == 1) 0.22f else 0.14f
            if (gunLevel == 1) {
                lasers.add(Laser(playerX, playerY - 6f, -150f))
            } else {
                lasers.add(Laser(playerX - 4f, playerY - 6f, -160f))
                lasers.add(Laser(playerX + 4f, playerY - 6f, -160f))
            }
        }

        // Move lasers
        lasers.removeAll { l ->
            l.y += l.vy * dt
            l.y < -4f
        }

        // Spawner
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawnTimer = (2.0f - wave * 0.15f).coerceAtLeast(0.6f)
            val roll = Random.nextFloat()
            if (roll < 0.5f) {
                enemies.add(
                    Enemy(
                        x = 16f + Random.nextFloat() * 128f,
                        y = -10f,
                        vx = (Random.nextFloat() - 0.5f) * 40f,
                        vy = 35f + Random.nextFloat() * 30f,
                        hp = 1,
                        type = EnemyType.DRONE,
                        r = 5f
                    )
                )
            } else if (roll < 0.8f) {
                enemies.add(
                    Enemy(
                        x = 16f + Random.nextFloat() * 128f,
                        y = -12f,
                        vx = (Random.nextFloat() - 0.5f) * 20f,
                        vy = 20f + Random.nextFloat() * 25f,
                        hp = 3,
                        type = EnemyType.ROCK,
                        r = 7f
                    )
                )
            } else {
                enemies.add(
                    Enemy(
                        x = 20f + Random.nextFloat() * 120f,
                        y = -14f,
                        vx = (Random.nextFloat() - 0.5f) * 50f,
                        vy = 25f,
                        hp = 5,
                        type = EnemyType.CRUISER,
                        r = 8f
                    )
                )
            }
        }

        // Enemies update
        val enemiesIter = enemies.iterator()
        while (enemiesIter.hasNext()) {
            val en = enemiesIter.next()
            en.x += en.vx * dt
            en.y += en.vy * dt
            if (en.x < en.r || en.x > 160f - en.r) en.vx *= -1f

            // Collision with player
            if (hypot(en.x - playerX, en.y - playerY) < en.r + 5f && invulnTimer <= 0f) {
                spawnExplosion(en.x, en.y, 8)
                enemiesIter.remove()
                if (shield > 30f) {
                    shield -= 30f
                    invulnTimer = 0.5f
                } else {
                    lives--
                    shield = 100f
                    invulnTimer = 1.8f
                    gunLevel = 1
                    if (lives <= 0) {
                        isGameOver = true
                        if (score > highScore) highScore = score
                    }
                }
                continue
            }

            // Collision with lasers
            var enemyDead = false
            val laserIter = lasers.iterator()
            while (laserIter.hasNext()) {
                val lz = laserIter.next()
                if (hypot(en.x - lz.x, en.y - lz.y) < en.r + 3f) {
                    laserIter.remove()
                    en.hp--
                    if (en.hp <= 0) {
                        enemyDead = true
                        break
                    }
                }
            }

            if (enemyDead) {
                spawnExplosion(en.x, en.y, if (en.type == EnemyType.CRUISER) 14 else 7)
                enemiesIter.remove()
                score += when (en.type) {
                    EnemyType.CRUISER -> 150
                    EnemyType.ROCK -> 50
                    EnemyType.DRONE -> 30
                }
                if (Random.nextFloat() < 0.2f) {
                    powerups.add(
                        Powerup(
                            x = en.x,
                            y = en.y,
                            vy = 30f,
                            type = if (Random.nextBoolean()) PowerupType.SHIELD else PowerupType.GUN
                        )
                    )
                }
                continue
            }

            if (en.y > 154f) {
                enemiesIter.remove()
            }
        }

        // Powerups
        powerups.removeAll { pw ->
            pw.y += pw.vy * dt
            if (hypot(pw.x - playerX, pw.y - playerY) < 10f) {
                if (pw.type == PowerupType.SHIELD) {
                    shield = (shield + 50f).coerceAtMost(100f)
                } else {
                    gunLevel = 2
                }
                score += 25
                true
            } else pw.y > 150f
        }

        // Particles
        particles.removeAll { p ->
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.life <= 0f
        }

        wave = 1 + score / 500
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, surface.width.toFloat(), surface.height.toFloat(), 0xFF0A1A0A.toInt())

        // Stars
        stars.forEach { s ->
            surface.fillRect(s.x, s.y, s.s, s.s, 0x5500FF33)
        }

        // Powerups
        powerups.forEach { pw ->
            val color = if (pw.type == PowerupType.SHIELD) 0xFF00AAFF.toInt() else 0xFFFFAA00.toInt()
            surface.fillRect(pw.x - 3f, pw.y - 3f, 6f, 6f, color)
            surface.drawText(if (pw.type == PowerupType.SHIELD) "S" else "P", pw.x - 2f, pw.y + 2f, 0xFF000000.toInt(), 6f)
        }

        // Lasers
        lasers.forEach { l ->
            surface.fillRect(l.x - 1f, l.y - 3f, 2f, 6f, 0xFF00FF33.toInt())
        }

        // Enemies
        enemies.forEach { en ->
            when (en.type) {
                EnemyType.DRONE -> {
                    surface.fillRect(en.x - 4f, en.y - 3f, 8f, 6f, 0xFFFF5555.toInt())
                    surface.fillRect(en.x - 1f, en.y - 1f, 2f, 2f, 0xFFFFFFFF.toInt())
                }
                EnemyType.ROCK -> {
                    surface.drawCircle(en.x, en.y, en.r, 0xFF88AA88.toInt(), true)
                }
                EnemyType.CRUISER -> {
                    surface.fillRect(en.x - 7f, en.y - 4f, 14f, 8f, 0xFFDD33FF.toInt())
                    surface.fillRect(en.x - 3f, en.y - 2f, 6f, 4f, 0xFFFF0000.toInt())
                }
            }
        }

        // Player Ship
        if (invulnTimer <= 0f || (System.currentTimeMillis() / 80) % 2 == 0L) {
            surface.fillRect(playerX - 5f, playerY - 3f, 10f, 8f, 0xFF00FF33.toInt())
            surface.fillRect(playerX - 2f, playerY - 6f, 4f, 4f, 0xFF00FF33.toInt())
            surface.fillRect(playerX - 1f, playerY - 2f, 2f, 3f, 0xFF004411.toInt())
            // Flame
            val flameColor = if (Random.nextBoolean()) 0xFFFFAA00.toInt() else 0xFFFF4444.toInt()
            surface.fillRect(playerX - 2f, playerY + 5f, 4f, 3f, flameColor)
        }

        // Particles
        particles.forEach { p ->
            val color = if (p.life > 0.3f) 0xFFFFFF00.toInt() else 0xFFFF5500.toInt()
            surface.fillRect(p.x, p.y, 2f, 2f, color)
        }

        // Top HUD
        surface.fillRect(0f, 0f, surface.width.toFloat(), 11f, 0xAA0F300F.toInt())
        surface.drawText("S:$score", 2f, 9f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("W:$wave", 68f, 9f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("L:$lives", 105f, 9f, 0xFF00FF33.toInt(), 8f)

        // Shield bar
        surface.fillRect(130f, 3f, 26f, 5f, 0x5500FF33)
        val shieldColor = if (shield > 30f) 0xFF00FF33.toInt() else 0xFFFF3333.toInt()
        surface.fillRect(130f, 3f, (shield / 100f * 26f).coerceAtLeast(0f), 5f, shieldColor)

        // Game Over
        if (isGameOver) {
            surface.fillRect(20f, 35f, 120f, 75f, 0xCC000000.toInt())
            surface.strokeRect(20f, 35f, 120f, 75f, 0xFF00FF33.toInt(), 2f)
            surface.drawText("MISSION FAILED", 34f, 52f, 0xFFFF3333.toInt(), 10f)
            surface.drawText("SCORE: $score", 38f, 68f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("BEST:  $highScore", 38f, 80f, 0xFF00FF33.toInt(), 8f)
            surface.drawText("PRESS A / START", 34f, 98f, 0xFFFFFF00.toInt(), 8f)
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
