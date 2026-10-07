package com.example.minuteman.engine

import com.example.minuteman.model.InputSnapshot

interface Cartridge {
    val id: String
    val name: String
    val version: String get() = "1.0"
    val author: String get() = "Minuteman"
    val description: String get() = ""

    fun init(surface: CartridgeSurface)
    fun update(input: InputSnapshot, dt: Float)
    fun draw(surface: CartridgeSurface)
    fun destroy() {}
    fun saveState(): String? = null
    fun loadState(dataJson: String) {}
}
