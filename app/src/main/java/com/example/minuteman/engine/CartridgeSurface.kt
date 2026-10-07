package com.example.minuteman.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.example.minuteman.audio.RetroSoundEngine
import com.example.minuteman.memory.MemoryCardSystem

class CartridgeSurface(
    val width: Int = 160,
    val height: Int = 144,
    val audio: RetroSoundEngine,
    private val memoryCard: MemoryCardSystem,
    var currentCartId: String = "",
    var currentCartName: String = ""
) {
    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas: Canvas = Canvas(bitmap)

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = false
    }

    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = false
        strokeWidth = 1f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = false
        typeface = Typeface.MONOSPACE
        textSize = 8f
    }

    fun clear(color: Int = 0xFF0F300F.toInt()) {
        bitmap.eraseColor(color)
    }

    fun fillRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        fillPaint.color = color
        canvas.drawRect(x, y, x + w, y + h, fillPaint)
    }

    fun strokeRect(x: Float, y: Float, w: Float, h: Float, color: Int, strokeWidth: Float = 1f) {
        strokePaint.color = color
        strokePaint.strokeWidth = strokeWidth
        canvas.drawRect(x, y, x + w, y + h, strokePaint)
    }

    fun drawCircle(cx: Float, cy: Float, radius: Float, color: Int, fill: Boolean = true) {
        val paint = if (fill) fillPaint else strokePaint
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
    }

    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, strokeWidth: Float = 1f) {
        strokePaint.color = color
        strokePaint.strokeWidth = strokeWidth
        canvas.drawLine(x1, y1, x2, y2, strokePaint)
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        color: Int = 0xFF00FF33.toInt(),
        textSize: Float = 8f,
        align: Paint.Align = Paint.Align.LEFT
    ) {
        textPaint.color = color
        textPaint.textSize = textSize
        textPaint.textAlign = align
        canvas.drawText(text, x, y, textPaint)
    }

    fun measureText(text: String, textSize: Float = 8f): Float {
        textPaint.textSize = textSize
        return textPaint.measureText(text)
    }

    fun save(dataJson: String): Boolean {
        if (currentCartId.isBlank()) return false
        return memoryCard.saveCartridgeData(currentCartId, currentCartName, dataJson)
    }

    fun load(): String? {
        if (currentCartId.isBlank()) return null
        return memoryCard.loadCartridgeData(currentCartId)
    }
}
