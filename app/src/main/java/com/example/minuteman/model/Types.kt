package com.example.minuteman.model

enum class GamepadButtonKey {
    UP, DOWN, LEFT, RIGHT,
    A, B, X, Y,
    L, R,
    START, SELECT
}

data class GamepadState(
    val up: Boolean = false,
    val down: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,
    val a: Boolean = false,
    val b: Boolean = false,
    val x: Boolean = false,
    val y: Boolean = false,
    val l: Boolean = false,
    val r: Boolean = false,
    val start: Boolean = false,
    val select: Boolean = false
) {
    operator fun get(key: GamepadButtonKey): Boolean = when (key) {
        GamepadButtonKey.UP -> up
        GamepadButtonKey.DOWN -> down
        GamepadButtonKey.LEFT -> left
        GamepadButtonKey.RIGHT -> right
        GamepadButtonKey.A -> a
        GamepadButtonKey.B -> b
        GamepadButtonKey.X -> x
        GamepadButtonKey.Y -> y
        GamepadButtonKey.L -> l
        GamepadButtonKey.R -> r
        GamepadButtonKey.START -> start
        GamepadButtonKey.SELECT -> select
    }

    fun copyWith(key: GamepadButtonKey, value: Boolean): GamepadState = when (key) {
        GamepadButtonKey.UP -> copy(up = value)
        GamepadButtonKey.DOWN -> copy(down = value)
        GamepadButtonKey.LEFT -> copy(left = value)
        GamepadButtonKey.RIGHT -> copy(right = value)
        GamepadButtonKey.A -> copy(a = value)
        GamepadButtonKey.B -> copy(b = value)
        GamepadButtonKey.X -> copy(x = value)
        GamepadButtonKey.Y -> copy(y = value)
        GamepadButtonKey.L -> copy(l = value)
        GamepadButtonKey.R -> copy(r = value)
        GamepadButtonKey.START -> copy(start = value)
        GamepadButtonKey.SELECT -> copy(select = value)
    }
}

data class AnalogStick(
    val x: Float = 0f,
    val y: Float = 0f
)

data class InputSnapshot(
    val held: GamepadState = GamepadState(),
    val pressed: GamepadState = GamepadState(),
    val released: GamepadState = GamepadState(),
    val stick: AnalogStick = AnalogStick()
)

data class ConsolePalette(
    val id: String,
    val name: String,
    val bodyBg: Long,
    val dpadBg: Long,
    val actionBtn: Long,
    val actionBtnActive: Long,
    val actionShadow: Long,
    val screenBg: Long,
    val screenText: Long,
    val screenBorder: Long,
    val activeDpad: Long,
    val accent: Long
)

enum class ShellScreen {
    SPLASH,
    MENU,
    CARTS,
    MEMORY,
    HOWTO,
    SETTINGS,
    DEBUG,
    CREDITS,
    EXIT
}

data class SaveSnapshot(
    val id: String,
    val timestamp: String,
    val sizeBytes: Long,
    val dataJson: String,
    val description: String = "Checkpoint"
)

data class MemorySlot(
    val cartId: String,
    val cartName: String,
    val updatedAt: String,
    val sizeBytes: Long,
    val dataJson: String,
    val dirty: Boolean = true,
    val snapshots: List<SaveSnapshot> = emptyList()
)

data class GitSyncConfig(
    val repoOwner: String = "",
    val repoName: String = "",
    val branch: String = "main",
    val token: String = "",
    val lastSynced: String = ""
)

data class DevToolkitStats(
    val fps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val frameHistory: List<Float> = List(30) { 16.6f },
    val frameCount: Long = 0,
    val uptimeSeconds: Long = 0,
    val isPaused: Boolean = false,
    val activeCartridgeName: String? = null,
    val heldState: GamepadState = GamepadState(),
    val lastButton: String = "",
    val memorySlotsCount: Int = 0,
    val isMuted: Boolean = false,
    val volume: Float = 0.5f
)
