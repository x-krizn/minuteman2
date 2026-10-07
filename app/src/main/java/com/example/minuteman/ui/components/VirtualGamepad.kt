package com.example.minuteman.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minuteman.model.ConsolePalette
import com.example.minuteman.model.GamepadButtonKey
import com.example.minuteman.model.GamepadState
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun VirtualGamepad(
    palette: ConsolePalette,
    held: GamepadState,
    onButtonChange: (GamepadButtonKey, Boolean) -> Unit,
    onStickChange: (Float, Float) -> Unit,
    hapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val triggerHaptic = remember(hapticsEnabled) {
        {
            if (hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Shoulder Buttons (L / R)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShoulderButton(
                label = "L",
                isHeld = held.l,
                palette = palette,
                onStateChange = {
                    onButtonChange(GamepadButtonKey.L, it)
                    if (it) triggerHaptic()
                },
                testTag = "btn_shoulder_l"
            )
            ShoulderButton(
                label = "R",
                isHeld = held.r,
                palette = palette,
                onStateChange = {
                    onButtonChange(GamepadButtonKey.R, it)
                    if (it) triggerHaptic()
                },
                testTag = "btn_shoulder_r"
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Controls: D-Pad on Left, Action Buttons on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Directional Cross D-Pad
            DPadController(
                palette = palette,
                held = held,
                onDirectionChange = { key, isDown ->
                    onButtonChange(key, isDown)
                    if (isDown) triggerHaptic()
                },
                onStick = onStickChange
            )

            // Right Action Diamond (A, B, X, Y)
            ActionButtonsCluster(
                palette = palette,
                held = held,
                onButtonChange = { key, isDown ->
                    onButtonChange(key, isDown)
                    if (isDown) triggerHaptic()
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Buttons (SELECT / START)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SystemPillButton(
                label = "SELECT",
                isHeld = held.select,
                palette = palette,
                onStateChange = {
                    onButtonChange(GamepadButtonKey.SELECT, it)
                    if (it) triggerHaptic()
                },
                testTag = "btn_select"
            )
            Spacer(modifier = Modifier.width(28.dp))
            SystemPillButton(
                label = "START",
                isHeld = held.start,
                palette = palette,
                onStateChange = {
                    onButtonChange(GamepadButtonKey.START, it)
                    if (it) triggerHaptic()
                },
                testTag = "btn_start"
            )
        }
    }
}

@Composable
fun DPadController(
    palette: ConsolePalette,
    held: GamepadState,
    onDirectionChange: (GamepadButtonKey, Boolean) -> Unit,
    onStick: (Float, Float) -> Unit
) {
    val sizeDp = 138.dp
    val dpadBg = Color(palette.dpadBg)
    val activeBg = Color(palette.activeDpad)

    Box(
        modifier = Modifier
            .size(sizeDp)
            .testTag("dpad_touch_area")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handleTouch(offset.x, offset.y, size.width.toFloat(), onDirectionChange, onStick)
                    },
                    onDragEnd = {
                        resetDPad(onDirectionChange, onStick)
                    },
                    onDragCancel = {
                        resetDPad(onDirectionChange, onStick)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        handleTouch(change.position.x, change.position.y, size.width.toFloat(), onDirectionChange, onStick)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        handleTouch(offset.x, offset.y, size.width.toFloat(), onDirectionChange, onStick)
                        tryAwaitRelease()
                        resetDPad(onDirectionChange, onStick)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Horizontal Arm
        Box(
            modifier = Modifier
                .width(134.dp)
                .height(44.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .background(dpadBg, RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF1A1C20), RoundedCornerShape(8.dp))
        )

        // Vertical Arm
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(134.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .background(dpadBg, RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF1A1C20), RoundedCornerShape(8.dp))
        )

        // Center Pivot Indent
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF22252A), CircleShape)
                .border(1.dp, Color(0xFF111316), CircleShape)
        )

        // Direction Arrow Glyphs
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Up",
            tint = if (held.up) activeBg else Color(0xFF7A8494),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Down",
            tint = if (held.down) activeBg else Color(0xFF7A8494),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Left",
            tint = if (held.left) activeBg else Color(0xFF7A8494),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(26.dp)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Right",
            tint = if (held.right) activeBg else Color(0xFF7A8494),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(26.dp)
        )
    }
}

private fun handleTouch(
    x: Float,
    y: Float,
    dimension: Float,
    onDirectionChange: (GamepadButtonKey, Boolean) -> Unit,
    onStick: (Float, Float) -> Unit
) {
    val center = dimension / 2f
    val dx = x - center
    val dy = y - center
    val radius = dimension * 0.45f
    val len = hypot(dx, dy)

    if (len > 12f) {
        val normX = (dx / radius).coerceIn(-1f, 1f)
        val normY = (dy / radius).coerceIn(-1f, 1f)
        onStick(normX, normY)

        onDirectionChange(GamepadButtonKey.LEFT, normX < -0.3f)
        onDirectionChange(GamepadButtonKey.RIGHT, normX > 0.3f)
        onDirectionChange(GamepadButtonKey.UP, normY < -0.3f)
        onDirectionChange(GamepadButtonKey.DOWN, normY > 0.3f)
    } else {
        resetDPad(onDirectionChange, onStick)
    }
}

private fun resetDPad(
    onDirectionChange: (GamepadButtonKey, Boolean) -> Unit,
    onStick: (Float, Float) -> Unit
) {
    onStick(0f, 0f)
    onDirectionChange(GamepadButtonKey.UP, false)
    onDirectionChange(GamepadButtonKey.DOWN, false)
    onDirectionChange(GamepadButtonKey.LEFT, false)
    onDirectionChange(GamepadButtonKey.RIGHT, false)
}

@Composable
fun ActionButtonsCluster(
    palette: ConsolePalette,
    held: GamepadState,
    onButtonChange: (GamepadButtonKey, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(136.dp)
            .testTag("action_buttons_cluster"),
        contentAlignment = Alignment.Center
    ) {
        // Button A (Right)
        ActionButton(
            label = "A",
            isHeld = held.a,
            palette = palette,
            onStateChange = { onButtonChange(GamepadButtonKey.A, it) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-4).dp, y = 4.dp),
            testTag = "btn_a"
        )

        // Button B (Bottom)
        ActionButton(
            label = "B",
            isHeld = held.b,
            palette = palette,
            onStateChange = { onButtonChange(GamepadButtonKey.B, it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-12).dp, y = (-4).dp),
            testTag = "btn_b"
        )

        // Secondary Button X (Top)
        SmallActionButton(
            label = "X",
            isHeld = held.x,
            palette = palette,
            onStateChange = { onButtonChange(GamepadButtonKey.X, it) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 12.dp, y = 4.dp),
            testTag = "btn_x"
        )

        // Secondary Button Y (Left)
        SmallActionButton(
            label = "Y",
            isHeld = held.y,
            palette = palette,
            onStateChange = { onButtonChange(GamepadButtonKey.Y, it) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 4.dp, y = (-12).dp),
            testTag = "btn_y"
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    isHeld: Boolean,
    palette: ConsolePalette,
    onStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "action_btn"
) {
    val baseColor = if (isHeld) Color(palette.actionBtnActive) else Color(palette.actionBtn)
    val shadowColor = Color(palette.actionShadow)
    val scale by animateFloatAsState(targetValue = if (isHeld) 0.94f else 1f, label = "btn_scale")

    Box(
        modifier = modifier
            .size(52.dp)
            .scale(scale)
            .shadow(if (isHeld) 2.dp else 5.dp, CircleShape)
            .background(shadowColor, CircleShape)
            .padding(bottom = if (isHeld) 1.dp else 4.dp)
            .background(baseColor, CircleShape)
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SmallActionButton(
    label: String,
    isHeld: Boolean,
    palette: ConsolePalette,
    onStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "small_action_btn"
) {
    val baseColor = if (isHeld) Color(palette.actionBtnActive) else Color(palette.actionBtn)
    val shadowColor = Color(palette.actionShadow)
    val scale by animateFloatAsState(targetValue = if (isHeld) 0.94f else 1f, label = "sbtn_scale")

    Box(
        modifier = modifier
            .size(36.dp)
            .scale(scale)
            .shadow(if (isHeld) 1.dp else 3.dp, CircleShape)
            .background(shadowColor, CircleShape)
            .padding(bottom = if (isHeld) 1.dp else 3.dp)
            .background(baseColor, CircleShape)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SystemPillButton(
    label: String,
    isHeld: Boolean,
    palette: ConsolePalette,
    onStateChange: (Boolean) -> Unit,
    testTag: String
) {
    val pillColor = if (isHeld) Color(palette.activeDpad) else Color(palette.dpadBg)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .rotate(-25f)
                .width(42.dp)
                .height(14.dp)
                .shadow(2.dp, RoundedCornerShape(7.dp))
                .background(pillColor, RoundedCornerShape(7.dp))
                .border(1.dp, Color(0xFF1E2228), RoundedCornerShape(7.dp))
                .testTag(testTag)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onStateChange(true)
                            tryAwaitRelease()
                            onStateChange(false)
                        }
                    )
                }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(palette.dpadBg),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ShoulderButton(
    label: String,
    isHeld: Boolean,
    palette: ConsolePalette,
    onStateChange: (Boolean) -> Unit,
    testTag: String
) {
    val bg = if (isHeld) Color(palette.activeDpad) else Color(palette.dpadBg)

    Box(
        modifier = Modifier
            .width(72.dp)
            .height(28.dp)
            .shadow(3.dp, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(bg, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .border(1.dp, Color(0xFF111316), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
