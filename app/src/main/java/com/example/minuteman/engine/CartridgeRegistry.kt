package com.example.minuteman.engine

import com.example.minuteman.cartridges.KnightCartridge
import com.example.minuteman.cartridges.PocketJumpCartridge
import com.example.minuteman.cartridges.Snake99Cartridge
import com.example.minuteman.cartridges.StarPatrolCartridge
import com.example.minuteman.cartridges.TemplateCartridge
import com.example.minuteman.cartridges.TinyRogueCartridge
import com.example.minuteman.cartridges.WalkCartridge

class CartridgeRegistry {
    private val cartridges = mutableListOf<Cartridge>()

    init {
        reloadBuiltIns()
    }

    fun reloadBuiltIns() {
        cartridges.clear()
        cartridges.add(KnightCartridge())
        cartridges.add(Snake99Cartridge())
        cartridges.add(PocketJumpCartridge())
        cartridges.add(StarPatrolCartridge())
        cartridges.add(TinyRogueCartridge())
        cartridges.add(WalkCartridge())
        cartridges.add(TemplateCartridge())
    }

    fun getCartridges(): List<Cartridge> = cartridges.toList()

    fun getCartridge(id: String): Cartridge? = cartridges.find { it.id == id }

    fun register(cart: Cartridge): Boolean {
        val existingIndex = cartridges.indexOfFirst { it.id == cart.id }
        if (existingIndex >= 0) {
            cartridges[existingIndex] = cart
        } else {
            cartridges.add(cart)
        }
        return true
    }

    fun removeCustom(id: String): Boolean {
        val builtInIds = setOf("knight", "snake_99", "pocket_jump", "star_patrol", "tiny_rogue", "walk", "template")
        if (builtInIds.contains(id)) return false
        return cartridges.removeAll { it.id == id }
    }
}
