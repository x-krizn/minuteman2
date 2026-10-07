package com.example.minuteman.cartridges

import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.model.InputSnapshot

class WalkCartridge : Cartridge {
    override val id: String = "walk"
    override val name: String = "WALK TEST"
    override val version: String = "0.1"
    override val author: String = "Minuteman Shell"
    override val description: String = "Asset loader and walk cycle test. Knight walks wherever stick points."

    private var x = 80f
    private var y = 80f
    private var t = 0f
    private var flip = false

    override fun init(surface: CartridgeSurface) {
        x = surface.width / 2f
        y = surface.height / 2f
        t = 0f
        flip = false
    }

    override fun update(input: InputSnapshot, dt: Float) {
        val sx = input.stick.x
        val sy = input.stick.y
        x += sx * 40f * dt
        y += sy * 40f * dt
        if (sx != 0f || sy != 0f) t += dt else t = 0f
        if (sx < -0.1f) flip = true
        else if (sx > 0.1f) flip = false

        x = x.coerceIn(10f, 150f)
        y = y.coerceIn(20f, 130f)
    }

    override fun draw(surface: CartridgeSurface) {
        surface.fillRect(0f, 0f, 160f, 144f, 0xFF0F300F.toInt())

        surface.drawText("WALK TEST CART", 10f, 14f, 0xFF4ADE80.toInt(), 8f)
        surface.drawText("D-PAD / ANALOG TO MOVE", 10f, 26f, 0xFF22C55E.toInt(), 7f)

        // Knight character block
        val stepAnim = if (t > 0f) ((t * 8).toInt() % 2) * 2f else 0f
        surface.fillRect(x - 6f, y - 14f, 12f, 14f, 0xFFFFFFFF.toInt())
        surface.fillRect(x - 4f, y - 10f, 8f, 6f, 0xFF00AAFF.toInt())
        val eyeX = if (flip) x - 4f else x + 2f
        surface.fillRect(eyeX, y - 12f, 2f, 2f, 0xFF000000.toInt())
        // Feet
        surface.fillRect(x - 5f, y + stepAnim, 4f, 3f, 0xFF333333.toInt())
        surface.fillRect(x + 1f, y - stepAnim, 4f, 3f, 0xFF333333.toInt())
    }
}
