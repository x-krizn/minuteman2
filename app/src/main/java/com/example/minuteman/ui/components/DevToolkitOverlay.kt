package com.example.minuteman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import com.example.minuteman.model.DevToolkitStats

@Composable
fun DevToolkitOverlay(
    isOpen: Boolean,
    palette: ConsolePalette,
    stats: DevToolkitStats,
    onTogglePause: () -> Unit,
    onStepFrame: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1216), RoundedCornerShape(12.dp))
                .border(2.dp, Color(palette.screenText), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .testTag("dev_toolkit_modal")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEV TOOLKIT / DEBUGGER",
                        color = Color(palette.screenText),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_dev_toolkit")
                    ) {
                        Text("CLOSE", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats rows
                StatRow("TARGET FPS", "${stats.fps} FPS")
                StatRow("FRAME TIME", "${"%.1f".format(stats.frameTimeMs)} ms")
                StatRow("TOTAL FRAMES", "${stats.frameCount}")
                StatRow("UPTIME", "${stats.uptimeSeconds}s")
                StatRow("ACTIVE CART", stats.activeCartridgeName ?: "SHELL OS")
                StatRow("LAST BUTTON", stats.lastButton.ifEmpty { "NONE" })
                StatRow("SAVES IN VMS", "${stats.memorySlotsCount} SLOTS")
                StatRow("RESOLUTION", "160 x 144 PIXELS")

                Spacer(modifier = Modifier.height(14.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onTogglePause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (stats.isPaused) Color(0xFF22C55E) else Color(0xFFEAB308)
                        ),
                        modifier = Modifier.testTag("btn_dev_pause_toggle")
                    ) {
                        Text(if (stats.isPaused) "RESUME" else "PAUSE", fontSize = 10.sp)
                    }
                    if (stats.isPaused) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onStepFrame,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(palette.actionBtnActive)),
                            modifier = Modifier.testTag("btn_dev_step_frame")
                        ) {
                            Text("STEP 1F", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
