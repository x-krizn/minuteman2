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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.minuteman.model.MemorySlot

@Composable
fun MemoryCardModal(
    isOpen: Boolean,
    palette: ConsolePalette,
    slots: List<MemorySlot>,
    onClose: () -> Unit,
    onFormat: () -> Unit,
    onDeleteSlot: (String) -> Unit,
    onRevertSnapshot: (String, String) -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14171A), RoundedCornerShape(12.dp))
                .border(2.dp, Color(palette.actionBtn), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .testTag("memory_card_modal")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEMORY CARD (VMS-64)",
                        color = Color(palette.screenText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_memory_modal")
                    ) {
                        Text("CLOSE", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${slots.size} CARTRIDGE SAVES STORED",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (slots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF1C2026), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO GAME SAVES YET\nPlay a cartridge to auto-save",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(slots) { slot ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F242C))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = slot.cartName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${slot.sizeBytes} B",
                                            color = Color(palette.screenText),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Saved: ${slot.updatedAt}",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row {
                                        Button(
                                            onClick = { onDeleteSlot(slot.cartId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1D40)),
                                            modifier = Modifier.testTag("btn_delete_slot_${slot.cartId}")
                                        ) {
                                            Text("DELETE", fontSize = 9.sp)
                                        }
                                        if (slot.snapshots.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onRevertSnapshot(slot.cartId, slot.snapshots.first().id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5EAA)),
                                                modifier = Modifier.testTag("btn_undo_slot_${slot.cartId}")
                                            ) {
                                                Text("UNDO (${slot.snapshots.size})", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onFormat,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB52B55)),
                        modifier = Modifier.testTag("btn_format_memory")
                    ) {
                        Text("FORMAT CARD", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
