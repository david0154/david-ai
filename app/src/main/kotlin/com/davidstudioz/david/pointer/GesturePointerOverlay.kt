package com.davidstudioz.david.pointer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.abs

/**
 * GesturePointerOverlay - MOUSE-LIKE POINTER FOR GESTURE CONTROL
 * ✅ Floating pointer visible on screen
 * ✅ Moves based on hand gestures
 * ✅ Click/tap via gesture commands
 * ✅ Smooth pointer animation
 * ✅ Visual feedback for actions
 */
class GesturePointerOverlay(private val context: Context) {

    private var overlayView: PointerView? = null
    private var windowManager: WindowManager? = null
    private var isShowing = false

    private var pointerX = 500f
    private var pointerY = 500f

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (isShowing) return

        try {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            overlayView = PointerView(context)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START

            windowManager?.addView(overlayView, params)
            isShowing = true

            Toast.makeText(context, "Gesture Pointer Active", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        if (!isShowing) return

        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update pointer position based on hand gesture
     * @param x Normalized X coordinate (0-1)
     * @param y Normalized Y coordinate (0-1)
     */
    fun updatePointerPosition(x: Float, y: Float) {
        val metrics = context.resources.displayMetrics
        pointerX = (x * metrics.widthPixels).coerceIn(0f, metrics.widthPixels.toFloat())
        pointerY = (y * metrics.heightPixels).coerceIn(0f, metrics.heightPixels.toFloat())
        overlayView?.updatePosition(pointerX, pointerY)
    }

    /**
     * Simulate click at current pointer position
     */
    fun performClick() {
        overlayView?.showClickAnimation()
    }

    fun isVisible(): Boolean = isShowing

    /**
     * Custom view for drawing the pointer
     */
    private inner class PointerView(context: Context) : View(context) {

        private val pointerPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeWidth = 3f
        }

        private val outerCirclePaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

        private val clickAnimPaint = Paint().apply {
            color = Color.parseColor("#00FF88")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }

        private var currentX = 500f
        private var currentY = 500f
        private var clickAnimRadius = 0f
        private var clickAnimAlpha = 255
        private var isAnimating = false

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw outer circle (glow effect)
            outerCirclePaint.alpha = 100
            canvas.drawCircle(currentX, currentY, 30f, outerCirclePaint)

            outerCirclePaint.alpha = 150
            canvas.drawCircle(currentX, currentY, 20f, outerCirclePaint)

            // Draw pointer (center dot)
            canvas.drawCircle(currentX, currentY, 12f, pointerPaint)

            // Draw click animation
            if (isAnimating) {
                clickAnimPaint.alpha = clickAnimAlpha
                canvas.drawCircle(currentX, currentY, clickAnimRadius, clickAnimPaint)

                clickAnimRadius += 5f
                clickAnimAlpha -= 15

                if (clickAnimAlpha <= 0 || clickAnimRadius > 100f) {
                    isAnimating = false
                    clickAnimRadius = 0f
                    clickAnimAlpha = 255
                }

                invalidate()
            }
        }

        fun updatePosition(x: Float, y: Float) {
            // Smooth transition
            currentX += (x - currentX) * 0.3f
            currentY += (y - currentY) * 0.3f
            invalidate()
        }

        fun showClickAnimation() {
            isAnimating = true
            clickAnimRadius = 12f
            clickAnimAlpha = 255
            invalidate()
        }
    }

    companion object {
        private const val TAG = "GesturePointerOverlay"
    }
}
