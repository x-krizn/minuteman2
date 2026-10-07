package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot

class TemplateCartridge : Cartridge {
    override val id: String = "template"
    override val name: String = "TEMPLATE CART"
    override val version: String = "1.0"
    override val author: String = "Workshop Base"
    override val description: String = "Clean boilerplate cartridge ready for hacking and custom games."

    private var x = 80f
    private var y = 72f
    private var vx = 40f
    private var vy = 30f
    private var bounces = 0

    override fun init(surface: CartridgeSurface) {
        x = 80f
        y = 72f
        vx = 40f
        vy = 30f
        bounces = 0
    }

    override fun update(input: InputSnapshot, dt: Float) {
        // Player manual nudge
        if (input.held.left) x -= 50f * dt
        if (input.held.right) x += 50f * dt
        if (input.held.up) y -= 50f * dt
        if (input.held.down) y += 50f * dt

        // Ball movement
        x += vx * dt
        y += vy * dt

        if (x < 10f || x > 150f) {
            vx *= -1f
            bounces++
        }
        if (y < 20f || y > 130f) {
            vy *= -1f
            bounces++
        }
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, 160f, 144f, 0xFF0D1B2A.toInt())

        surface.strokeRect(6f, 16f, 148f, 120f, 0xFF415A77.toInt(), 1f)

        surface.drawText("MINUTEMAN WORKSHOP", 10f, 12f, 0xFFE0E1DD.toInt(), 8f)
        surface.drawText("BOUNCES: $bounces", 10f, 26f, 0xFF778DA9.toInt(), 7f)

        // Bouncing box
        surface.fillRect(x - 5f, y - 5f, 10f, 10f, 0xFFFF0055.toInt())
        surface.strokeRect(x - 6f, y - 6f, 12f, 12f, 0xFFFFFFFF.toInt(), 1f)
    }
}
