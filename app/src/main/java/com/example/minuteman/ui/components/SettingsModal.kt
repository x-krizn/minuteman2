package com.example.minuteman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.minuteman.model.ConsolePalette
import com.example.minuteman.model.Palettes

@Composable
fun SettingsModal(
    isOpen: Boolean,
    currentPalette: ConsolePalette,
    scanlines: Boolean,
    muted: Boolean,
    volume: Float,
    haptics: Boolean,
    onPaletteSelect: (ConsolePalette) -> Unit,
    onScanlinesToggle: (Boolean) -> Unit,
    onMuteToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14171A), RoundedCornerShape(12.dp))
                .border(2.dp, Color(currentPalette.actionBtn), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .testTag("settings_modal")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSOLE SETTINGS",
                        color = Color(currentPalette.screenText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_settings")
                    ) {
                        Text("CLOSE", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Palettes
                Text(
                    text = "CONSOLE PALETTES",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Palettes.CONSOLE_PALETTES.forEach { pal ->
                        val isSelected = pal.id == currentPalette.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(Color(pal.bodyBg), RoundedCornerShape(6.dp))
                                .border(
                                    if (isSelected) 2.5.dp else 1.dp,
                                    if (isSelected) Color(currentPalette.screenText) else Color(0x44000000),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onPaletteSelect(pal) }
                                .testTag("palette_choice_${pal.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(pal.actionBtn), CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CRT Scanlines
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CRT SCANLINES", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = scanlines,
                        onCheckedChange = onScanlinesToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(currentPalette.screenText)),
                        modifier = Modifier.testTag("switch_scanlines")
                    )
                }

                // Mute
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AUDIO SOUND", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = !muted,
                        onCheckedChange = { onMuteToggle(!it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(currentPalette.screenText)),
                        modifier = Modifier.testTag("switch_audio")
                    )
                }

                // Volume slider
                if (!muted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VOL", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = volume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(currentPalette.screenText),
                                activeTrackColor = Color(currentPalette.screenText)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_volume")
                        )
                    }
                }

                // Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TACTILE HAPTICS", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Switch(
                        checked = haptics,
                        onCheckedChange = onHapticsToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(currentPalette.screenText)),
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }
            }
        }
    }
}
