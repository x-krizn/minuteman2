package com.example.minuteman.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.minuteman.audio.RetroSoundEngine
import com.example.minuteman.engine.Cartridge
import com.example.minuteman.engine.CartridgeRegistry
import com.example.minuteman.engine.CartridgeSurface
import com.example.minuteman.memory.MemoryCardSystem
import com.example.minuteman.model.AnalogStick
import com.example.minuteman.model.ConsolePalette
import com.example.minuteman.model.DevToolkitStats
import com.example.minuteman.model.GamepadButtonKey
import com.example.minuteman.model.GamepadState
import com.example.minuteman.model.InputSnapshot
import com.example.minuteman.model.MemorySlot
import com.example.minuteman.model.Palettes
import com.example.minuteman.model.ShellScreen
import com.example.minuteman.ui.screens.ShellScreenRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConsoleViewModel(application: Application) : AndroidViewModel(application) {

    val soundEngine = RetroSoundEngine()
    val memoryCard = MemoryCardSystem(application)
    val registry = CartridgeRegistry()

    val surface = CartridgeSurface(
        width = 160,
        height = 144,
        audio = soundEngine,
        memoryCard = memoryCard
    )

    private val _palette = MutableStateFlow(Palettes.CONSOLE_PALETTES[0])
    val palette: StateFlow<ConsolePalette> = _palette.asStateFlow()

    private val _scanlines = MutableStateFlow(true)
    val scanlines: StateFlow<Boolean> = _scanlines.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _haptics = MutableStateFlow(true)
    val haptics: StateFlow<Boolean> = _haptics.asStateFlow()

    private val _currentScreen = MutableStateFlow(ShellScreen.SPLASH)
    val currentScreen: StateFlow<ShellScreen> = _currentScreen.asStateFlow()

    private val _menuIndex = MutableStateFlow(0)
    val menuIndex: StateFlow<Int> = _menuIndex.asStateFlow()

    private val _cartIndex = MutableStateFlow(0)
    val cartIndex: StateFlow<Int> = _cartIndex.asStateFlow()

    private val _activeCartridge = MutableStateFlow<Cartridge?>(null)
    val activeCartridge: StateFlow<Cartridge?> = _activeCartridge.asStateFlow()

    private val _heldState = MutableStateFlow(GamepadState())
    val heldState: StateFlow<GamepadState> = _heldState.asStateFlow()

    private val _devStats = MutableStateFlow(DevToolkitStats())
    val devStats: StateFlow<DevToolkitStats> = _devStats.asStateFlow()

    private val _isMemoryModalOpen = MutableStateFlow(false)
    val isMemoryModalOpen: StateFlow<Boolean> = _isMemoryModalOpen.asStateFlow()

    private val _isWorkshopModalOpen = MutableStateFlow(false)
    val isWorkshopModalOpen: StateFlow<Boolean> = _isWorkshopModalOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    private val _isDevToolkitOpen = MutableStateFlow(false)
    val isDevToolkitOpen: StateFlow<Boolean> = _isDevToolkitOpen.asStateFlow()

    private val _memorySlots = MutableStateFlow<List<MemorySlot>>(emptyList())
    val memorySlots: StateFlow<List<MemorySlot>> = _memorySlots.asStateFlow()

    private var prevHeld = GamepadState()
    private var stick = AnalogStick()
    private var gameLoopJob: Job? = null
    private var splashProgress = 0f
    private var isDevPaused = false
    private var stepOneFrame = false
    private var frameCount = 0L
    private val startTime = System.currentTimeMillis()

    init {
        refreshMemorySlots()
        startGameLoop()
    }

    fun setPalette(pal: ConsolePalette) {
        _palette.value = pal
    }

    fun toggleScanlines(enable: Boolean) {
        _scanlines.value = enable
    }

    fun toggleMute(mute: Boolean) {
        _muted.value = mute
        soundEngine.setMuted(mute)
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        soundEngine.setVolume(vol)
    }

    fun toggleHaptics(enable: Boolean) {
        _haptics.value = enable
    }

    fun setMemoryModalOpen(open: Boolean) {
        if (open) refreshMemorySlots()
        _isMemoryModalOpen.value = open
    }

    fun setWorkshopModalOpen(open: Boolean) {
        _isWorkshopModalOpen.value = open
    }

    fun setSettingsModalOpen(open: Boolean) {
        _isSettingsModalOpen.value = open
    }

    fun setDevToolkitOpen(open: Boolean) {
        _isDevToolkitOpen.value = open
    }

    fun refreshMemorySlots() {
        _memorySlots.value = memoryCard.getAllSlots()
    }

    fun formatMemoryCard() {
        memoryCard.formatMemoryCard()
        refreshMemorySlots()
        soundEngine.menuBack()
    }

    fun deleteMemorySlot(cartId: String) {
        memoryCard.deleteSlot(cartId)
        refreshMemorySlots()
        soundEngine.menuBack()
    }

    fun revertSnapshot(cartId: String, snapshotId: String) {
        memoryCard.revertToSnapshot(cartId, snapshotId)
        refreshMemorySlots()
        soundEngine.powerup()
    }

    fun createCustomCart(name: String, author: String) {
        val cart = object : Cartridge {
            override val id: String = "custom_${System.currentTimeMillis()}"
            override val name: String = name
            override val version: String = "1.0"
            override val author: String = author
            override val description: String = "Custom cartridge burned in workshop."

            private var bx = 80f
            private var by = 72f
            private var bvx = 50f
            private var bvy = 40f

            override fun init(surface: CartridgeSurface) {
                bx = 80f
                by = 72f
            }

            override fun update(input: InputSnapshot, dt: Float) {
                if (input.held.left) bx -= 60f * dt
                if (input.held.right) bx += 60f * dt
                if (input.held.up) by -= 60f * dt
                if (input.held.down) by += 60f * dt

                bx += bvx * dt
                by += bvy * dt

                if (bx < 10f || bx > 150f) bvx *= -1f
                if (by < 20f || by > 130f) bvy *= -1f
            }

            override fun draw(surface: CartridgeSurface) {
                surface.fillRect(0f, 0f, 160f, 144f, 0xFF112233.toInt())
                surface.drawText(name, 10f, 14f, 0xFF00FFCC.toInt(), 8f)
                surface.drawText("BY $author", 10f, 26f, 0xFFFFFFFF.toInt(), 7f)
                surface.fillRect(bx - 6f, by - 6f, 12f, 12f, 0xFFFF0055.toInt())
                surface.strokeRect(bx - 7f, by - 7f, 14f, 14f, 0xFFFFFFFF.toInt(), 1f)
            }
        }
        registry.register(cart)
        soundEngine.powerup()
    }

    fun onButtonChange(key: GamepadButtonKey, isDown: Boolean) {
        _heldState.value = _heldState.value.copyWith(key, isDown)
    }

    fun onStickChange(x: Float, y: Float) {
        stick = AnalogStick(x, y)
    }

    fun toggleDevPause() {
        isDevPaused = !isDevPaused
    }

    fun stepDevFrame() {
        stepOneFrame = true
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val deltaMs = (now - lastTime) / 1_000_000f
                lastTime = now

                val dt = 0.016f // 60 FPS fixed step

                if (!isDevPaused || stepOneFrame) {
                    stepOneFrame = false
                    frameCount++

                    val currentHeld = _heldState.value

                    // Compute pressed and released transitions
                    val pressed = GamepadState(
                        up = currentHeld.up && !prevHeld.up,
                        down = currentHeld.down && !prevHeld.down,
                        left = currentHeld.left && !prevHeld.left,
                        right = currentHeld.right && !prevHeld.right,
                        a = currentHeld.a && !prevHeld.a,
                        b = currentHeld.b && !prevHeld.b,
                        x = currentHeld.x && !prevHeld.x,
                        y = currentHeld.y && !prevHeld.y,
                        l = currentHeld.l && !prevHeld.l,
                        r = currentHeld.r && !prevHeld.r,
                        start = currentHeld.start && !prevHeld.start,
                        select = currentHeld.select && !prevHeld.select
                    )

                    val released = GamepadState(
                        up = !currentHeld.up && prevHeld.up,
                        down = !currentHeld.down && prevHeld.down,
                        left = !currentHeld.left && prevHeld.left,
                        right = !currentHeld.right && prevHeld.right,
                        a = !currentHeld.a && prevHeld.a,
                        b = !currentHeld.b && prevHeld.b,
                        x = !currentHeld.x && prevHeld.x,
                        y = !currentHeld.y && prevHeld.y,
                        l = !currentHeld.l && prevHeld.l,
                        r = !currentHeld.r && prevHeld.r,
                        start = !currentHeld.start && prevHeld.start,
                        select = !currentHeld.select && prevHeld.select
                    )

                    val input = InputSnapshot(
                        held = currentHeld,
                        pressed = pressed,
                        released = released,
                        stick = stick
                    )

                    // START + SELECT Combo to leave active game back to Shell
                    if (currentHeld.start && currentHeld.select && _activeCartridge.value != null) {
                        exitCurrentCartridge()
                    } else if (_activeCartridge.value != null) {
                        // Game loop active cart
                        _activeCartridge.value?.update(input, dt)
                        _activeCartridge.value?.draw(surface)
                    } else {
                        // Shell OS loop
                        updateShell(input, dt)
                        drawShell()
                    }

                    prevHeld = currentHeld
                }

                // Update Dev Stats periodically
                if (frameCount % 10 == 0L) {
                    val uptime = (System.currentTimeMillis() - startTime) / 1000
                    _devStats.value = DevToolkitStats(
                        fps = 60,
                        frameTimeMs = deltaMs,
                        frameCount = frameCount,
                        uptimeSeconds = uptime,
                        isPaused = isDevPaused,
                        activeCartridgeName = _activeCartridge.value?.name,
                        heldState = _heldState.value,
                        memorySlotsCount = _memorySlots.value.size,
                        isMuted = _muted.value,
                        volume = _volume.value
                    )
                }

                delay(16)
            }
        }
    }

    private fun updateShell(input: InputSnapshot, dt: Float) {
        when (_currentScreen.value) {
            ShellScreen.SPLASH -> {
                splashProgress += dt
                if (input.pressed.a || input.pressed.start || splashProgress > 2.5f) {
                    _currentScreen.value = ShellScreen.MENU
                    soundEngine.powerup()
                }
            }
            ShellScreen.MENU -> {
                val menuCount = ShellScreenRenderer.MENU_ITEMS.size
                if (input.pressed.up) {
                    _menuIndex.value = (_menuIndex.value - 1 + menuCount) % menuCount
                    soundEngine.menuMove()
                } else if (input.pressed.down) {
                    _menuIndex.value = (_menuIndex.value + 1) % menuCount
                    soundEngine.menuMove()
                } else if (input.pressed.a || input.pressed.start) {
                    val target = ShellScreenRenderer.MENU_ITEMS[_menuIndex.value].second
                    _currentScreen.value = target
                    soundEngine.menuSelect()
                }
            }
            ShellScreen.CARTS -> {
                val carts = registry.getCartridges()
                if (carts.isNotEmpty()) {
                    if (input.pressed.up) {
                        _cartIndex.value = (_cartIndex.value - 1 + carts.size) % carts.size
                        soundEngine.menuMove()
                    } else if (input.pressed.down) {
                        _cartIndex.value = (_cartIndex.value + 1) % carts.size
                        soundEngine.menuMove()
                    } else if (input.pressed.a || input.pressed.start) {
                        launchCartridge(carts[_cartIndex.value])
                    } else if (input.pressed.b) {
                        _currentScreen.value = ShellScreen.MENU
                        soundEngine.menuBack()
                    }
                } else if (input.pressed.b) {
                    _currentScreen.value = ShellScreen.MENU
                    soundEngine.menuBack()
                }
            }
            ShellScreen.HOWTO, ShellScreen.MEMORY, ShellScreen.SETTINGS, ShellScreen.DEBUG, ShellScreen.CREDITS -> {
                if (input.pressed.b || input.pressed.start) {
                    _currentScreen.value = ShellScreen.MENU
                    soundEngine.menuBack()
                }
            }
            ShellScreen.EXIT -> {
                if (input.pressed.start || input.pressed.a) {
                    _currentScreen.value = ShellScreen.MENU
                }
            }
        }
    }

    private fun drawShell() {
        ShellScreenRenderer.draw(
            surface = surface,
            screen = _currentScreen.value,
            palette = _palette.value,
            menuIndex = _menuIndex.value,
            cartIndex = _cartIndex.value,
            cartridges = registry.getCartridges(),
            splashProgress = splashProgress
        )
    }

    fun launchCartridge(cart: Cartridge) {
        surface.currentCartId = cart.id
        surface.currentCartName = cart.name
        cart.init(surface)
        _activeCartridge.value = cart
        soundEngine.powerup()
    }

    fun exitCurrentCartridge() {
        _activeCartridge.value?.destroy()
        _activeCartridge.value = null
        _currentScreen.value = ShellScreen.CARTS
        soundEngine.menuBack()
    }

    fun getScreenBitmap(): Bitmap = surface.bitmap

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
