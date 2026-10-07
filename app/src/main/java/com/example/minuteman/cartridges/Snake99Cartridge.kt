package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot
import org.json.JSONObject
import kotlin.random.Random

class Snake99Cartridge : Cartridge {
    override val id: String = "snake_99"
    override val name: String = "SNAKE 99"
    override val version: String = "1.0"
    override val author: String = "Minuteman Arcade"
    override val description: String = "Classic arcade snake. D-Pad: Turn. Eat food & bonus items."

    private data class Point(val x: Int, val y: Int)

    private val tileSize = 8
    private val cols = 20
    private val rows = 16
    private val offsetY = 16

    private var snake = mutableListOf<Point>()
    private var dir = Point(1, 0)
    private var nextDir = Point(1, 0)
    private var food = Point(10, 8)
    private var bonus: Point? = null
    private var bonusTimer = 0f
    private var score = 0
    private var highScore = 0
    private var moveInterval = 0.12f
    private var moveTimer = 0f
    private var isGameOver = false

    private fun spawnFood() {
        var attempts = 0
        while (attempts < 100) {
            val fx = Random.nextInt(cols)
            val fy = Random.nextInt(rows)
            if (snake.none { it.x == fx && it.y == fy }) {
                food = Point(fx, fy)
                break
            }
            attempts++
        }
    }

    private fun spawnBonus() {
        val bx = Random.nextInt(cols)
        val by = Random.nextInt(rows)
        if (snake.none { it.x == bx && it.y == by } && (bx != food.x || by != food.y)) {
            bonus = Point(bx, by)
            bonusTimer = 6f
        }
    }

    private fun resetGame() {
        snake = mutableListOf(Point(5, 8), Point(4, 8), Point(3, 8))
        dir = Point(1, 0)
        nextDir = Point(1, 0)
        score = 0
        moveInterval = 0.12f
        moveTimer = 0f
        bonus = null
        bonusTimer = 0f
        isGameOver = false
        spawnFood()
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

        if (input.pressed.up && dir.y == 0) nextDir = Point(0, -1)
        else if (input.pressed.down && dir.y == 0) nextDir = Point(0, 1)
        else if (input.pressed.left && dir.x == 0) nextDir = Point(-1, 0)
        else if (input.pressed.right && dir.x == 0) nextDir = Point(1, 0)

        if (bonus != null) {
            bonusTimer -= dt
            if (bonusTimer <= 0f) bonus = null
        }

        moveTimer += dt
        if (moveTimer >= moveInterval) {
            moveTimer = 0f
            dir = nextDir
            val head = Point(snake[0].x + dir.x, snake[0].y + dir.y)

            // Wall collision
            if (head.x < 0 || head.x >= cols || head.y < 0 || head.y >= rows) {
                triggerGameOver()
                return
            }

            // Self collision
            if (snake.any { it.x == head.x && it.y == head.y }) {
                triggerGameOver()
                return
            }

            snake.add(0, head)

            if (head.x == food.x && head.y == food.y) {
                score += 10
                spawnFood()
                moveInterval = (0.12f - (score / 50) * 0.01f).coerceAtLeast(0.05f)
                if (Random.nextFloat() < 0.25f && bonus == null) {
                    spawnBonus()
                }
            } else if (bonus != null && head.x == bonus!!.x && head.y == bonus!!.y) {
                score += 50
                bonus = null
            } else {
                snake.removeAt(snake.size - 1)
            }
        }
    }

    private fun triggerGameOver() {
        isGameOver = true
        if (score > highScore) {
            highScore = score
        }
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, surface.width.toFloat(), surface.height.toFloat(), 0xFF0F260F.toInt())

        // Top HUD
        surface.fillRect(0f, 0f, surface.width.toFloat(), offsetY.toFloat(), 0x5500FF33)
        surface.drawText("SCORE:$score", 4f, 11f, 0xFF00FF33.toInt(), 8f)
        surface.drawText("BEST:$highScore", 96f, 11f, 0xFF00FF33.toInt(), 8f)

        // Arena boundary
        surface.strokeRect(0f, offsetY.toFloat(), surface.width.toFloat(), (surface.height - offsetY).toFloat(), 0x4400FF33)

        // Food
        surface.fillRect(
            (food.x * tileSize + 1).toFloat(),
            (food.y * tileSize + offsetY + 1).toFloat(),
            (tileSize - 2).toFloat(),
            (tileSize - 2).toFloat(),
            0xFFFF3333.toInt()
        )

        // Bonus
        bonus?.let { b ->
            val color = if ((System.currentTimeMillis() / 120) % 2 == 0L) 0xFFFFFF00.toInt() else 0xFFFFAA00.toInt()
            surface.fillRect(
                (b.x * tileSize).toFloat(),
                (b.y * tileSize + offsetY).toFloat(),
                tileSize.toFloat(),
                tileSize.toFloat(),
                color
            )
        }

        // Snake
        snake.forEachIndexed { idx, pt ->
            val color = if (idx == 0) 0xFF00FF33.toInt() else 0xFF00AA22.toInt()
            surface.fillRect(
                (pt.x * tileSize + 1).toFloat(),
                (pt.y * tileSize + offsetY + 1).toFloat(),
                (tileSize - 2).toFloat(),
                (tileSize - 2).toFloat(),
                color
            )
        }

        // Game Over modal
        if (isGameOver) {
            surface.fillRect(20f, 45f, 120f, 65f, 0xDD000000.toInt())
            surface.strokeRect(20f, 45f, 120f, 65f, 0xFF00FF33.toInt(), 1f)
            surface.drawText("GAME OVER", 44f, 64f, 0xFFFF3333.toInt(), 10f)
            surface.drawText("FINAL: $score", 48f, 80f, 0xFF00FF33.toInt(), 8f)
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
