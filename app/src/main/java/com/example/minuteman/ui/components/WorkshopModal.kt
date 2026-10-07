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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun WorkshopModal(
    isOpen: Boolean,
    palette: ConsolePalette,
    onClose: () -> Unit,
    onCreateCart: (String, String) -> Unit
) {
    if (!isOpen) return

    var cartName by remember { mutableStateOf("MY RETRO GAME") }
    var cartAuthor by remember { mutableStateOf("ARCADE DEV") }

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14171A), RoundedCornerShape(12.dp))
                .border(2.dp, Color(palette.actionBtn), RoundedCornerShape(12.dp))
                .padding(16.dp)
                .testTag("workshop_modal")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CARTRIDGE WORKSHOP",
                        color = Color(palette.screenText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_workshop")
                    ) {
                        Text("CLOSE", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "CREATE A CUSTOM RETRO CARTRIDGE",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cartName,
                    onValueChange = { cartName = it },
                    label = { Text("Game Title", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1B2028),
                        unfocusedContainerColor = Color(0xFF1B2028)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_workshop_cart_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cartAuthor,
                    onValueChange = { cartAuthor = it },
                    label = { Text("Author", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1B2028),
                        unfocusedContainerColor = Color(0xFF1B2028)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_workshop_author")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (cartName.isNotBlank()) {
                                onCreateCart(cartName, cartAuthor)
                                onClose()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(palette.actionBtnActive)),
                        modifier = Modifier.testTag("btn_create_custom_cart")
                    ) {
                        Text("BURN CARTRIDGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
