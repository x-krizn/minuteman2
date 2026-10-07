package com.example.minuteman.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minuteman.model.ConsolePalette

@Composable
fun ScreenViewport(
    bitmap: Bitmap?,
    palette: ConsolePalette,
    scanlines: Boolean = true,
    isCartRunning: Boolean = false,
    activeCartName: String? = null,
    modifier: Modifier = Modifier
) {
    val bezelBg = Color(palette.dpadBg)
    val screenBg = Color(palette.screenBg)
    val borderColor = Color(palette.screenBorder)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .background(bezelBg, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF1E2229), RoundedCornerShape(12.dp))
            .padding(top = 10.dp, bottom = 10.dp, start = 14.dp, end = 14.dp)
            .testTag("screen_viewport_container")
    ) {
        // Upper bezel header text
        Text(
            text = if (isCartRunning) "● RUNNING: ${activeCartName ?: ""}" else "MINUTEMAN 8-BIT LCD",
            color = if (isCartRunning) Color(palette.screenText) else Color(palette.accent),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(bottom = 6.dp)
        )

        // 160x144 Aspect Ratio Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .aspectRatio(160f / 144f)
                .clip(RoundedCornerShape(6.dp))
                .background(screenBg)
                .border(2.dp, borderColor, RoundedCornerShape(6.dp))
                .testTag("retro_screen_canvas_box")
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("pixel_canvas")) {
                if (bitmap != null) {
                    val imageBitmap = bitmap.asImageBitmap()
                    drawImage(
                        image = imageBitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                        filterQuality = FilterQuality.None
                    )
                }

                // CRT Scanlines effect
                if (scanlines) {
                    val scanlineHeight = 3f
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = Color(0x33000000),
                            topLeft = Offset(0f, y),
                            size = Size(size.width, 1.5f)
                        )
                        y += scanlineHeight
                    }
                }
            }
        }
    }
}
