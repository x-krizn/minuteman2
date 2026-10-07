package com.example.minuteman.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minuteman.model.Palettes
import com.example.minuteman.ui.components.DevToolkitOverlay
import com.example.minuteman.ui.components.MemoryCardModal
import com.example.minuteman.ui.components.ScreenViewport
import com.example.minuteman.ui.components.SettingsModal
import com.example.minuteman.ui.components.VirtualGamepad
import com.example.minuteman.ui.components.WorkshopModal
import com.example.minuteman.viewmodel.ConsoleViewModel

@Composable
fun MainConsoleScreen(viewModel: ConsoleViewModel) {
    val palette by viewModel.palette.collectAsState()
    val scanlines by viewModel.scanlines.collectAsState()
    val muted by viewModel.muted.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val haptics by viewModel.haptics.collectAsState()
    val activeCart by viewModel.activeCartridge.collectAsState()
    val heldState by viewModel.heldState.collectAsState()
    val devStats by viewModel.devStats.collectAsState()
    val memorySlots by viewModel.memorySlots.collectAsState()

    val isMemoryOpen by viewModel.isMemoryModalOpen.collectAsState()
    val isWorkshopOpen by viewModel.isWorkshopModalOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsModalOpen.collectAsState()
    val isDevToolkitOpen by viewModel.isDevToolkitOpen.collectAsState()

    val bodyBg = Color(palette.bodyBg)

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF14171A)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
                    .background(bodyBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Tool Bar / Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Power indicator LED
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF3333), CircleShape)
                                .shadow(2.dp, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MINUTEMAN",
                            color = Color(palette.dpadBg),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Palette switch
                        IconButton(
                            onClick = {
                                val curIndex = Palettes.CONSOLE_PALETTES.indexOfFirst { it.id == palette.id }
                                val next = Palettes.CONSOLE_PALETTES[(curIndex + 1) % Palettes.CONSOLE_PALETTES.size]
                                viewModel.setPalette(next)
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_quick_palette")
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Palette", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }

                        // CRT Scanlines toggle
                        IconButton(
                            onClick = { viewModel.toggleScanlines(!scanlines) },
                            modifier = Modifier.size(32.dp).testTag("btn_quick_scanlines")
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = "Scanlines", tint = if (scanlines) Color(palette.actionBtn) else Color.Gray, modifier = Modifier.size(18.dp))
                        }

                        // Audio Mute toggle
                        IconButton(
                            onClick = { viewModel.toggleMute(!muted) },
                            modifier = Modifier.size(32.dp).testTag("btn_quick_mute")
                        ) {
                            Icon(if (muted) Icons.Default.VolumeMute else Icons.Default.VolumeUp, contentDescription = "Mute", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }

                        // Memory Card Modal
                        IconButton(
                            onClick = { viewModel.setMemoryModalOpen(true) },
                            modifier = Modifier.size(32.dp).testTag("btn_open_memory_card")
                        ) {
                            Icon(Icons.Default.SdStorage, contentDescription = "Memory Card", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }

                        // Cartridge Workshop
                        IconButton(
                            onClick = { viewModel.setWorkshopModalOpen(true) },
                            modifier = Modifier.size(32.dp).testTag("btn_open_workshop")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Workshop", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }

                        // Debugger / Dev Toolkit
                        IconButton(
                            onClick = { viewModel.setDevToolkitOpen(true) },
                            modifier = Modifier.size(32.dp).testTag("btn_open_debugger")
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = "Debug", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }

                        // Settings Modal
                        IconButton(
                            onClick = { viewModel.setSettingsModalOpen(true) },
                            modifier = Modifier.size(32.dp).testTag("btn_open_settings")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(palette.dpadBg), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Retro 160x144 LCD Viewport
                ScreenViewport(
                    bitmap = viewModel.getScreenBitmap(),
                    palette = palette,
                    scanlines = scanlines,
                    isCartRunning = activeCart != null,
                    activeCartName = activeCart?.name
                )

                // Handheld Chassis Grill Accents
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .width(3.dp)
                                .height(16.dp)
                                .background(Color(palette.actionShadow), RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tactical Virtual Gamepad
                VirtualGamepad(
                    palette = palette,
                    held = heldState,
                    onButtonChange = { key, isDown -> viewModel.onButtonChange(key, isDown) },
                    onStickChange = { x, y -> viewModel.onStickChange(x, y) },
                    hapticsEnabled = haptics
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Modals & Overlays
        MemoryCardModal(
            isOpen = isMemoryOpen,
            palette = palette,
            slots = memorySlots,
            onClose = { viewModel.setMemoryModalOpen(false) },
            onFormat = { viewModel.formatMemoryCard() },
            onDeleteSlot = { viewModel.deleteMemorySlot(it) },
            onRevertSnapshot = { cartId, snapId -> viewModel.revertSnapshot(cartId, snapId) }
        )

        WorkshopModal(
            isOpen = isWorkshopOpen,
            palette = palette,
            onClose = { viewModel.setWorkshopModalOpen(false) },
            onCreateCart = { name, author -> viewModel.createCustomCart(name, author) }
        )

        SettingsModal(
            isOpen = isSettingsOpen,
            currentPalette = palette,
            scanlines = scanlines,
            muted = muted,
            volume = volume,
            haptics = haptics,
            onPaletteSelect = { viewModel.setPalette(it) },
            onScanlinesToggle = { viewModel.toggleScanlines(it) },
            onMuteToggle = { viewModel.toggleMute(it) },
            onVolumeChange = { viewModel.setVolume(it) },
            onHapticsToggle = { viewModel.toggleHaptics(it) },
            onClose = { viewModel.setSettingsModalOpen(false) }
        )

        DevToolkitOverlay(
            isOpen = isDevToolkitOpen,
            palette = palette,
            stats = devStats,
            onTogglePause = { viewModel.toggleDevPause() },
            onStepFrame = { viewModel.stepDevFrame() },
            onClose = { viewModel.setDevToolkitOpen(false) }
        )
    }
}
