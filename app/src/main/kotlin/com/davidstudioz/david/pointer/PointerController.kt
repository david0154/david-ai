package com.davidstudioz.david.pointer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

/**
 * Pointer Controller
 * Shows hand-controlled mouse pointer for device control
 * Uses gesture coordinates to move pointer and interact
 */
class PointerController(
    private val context: Context
) {

    private val TAG = "PointerController"
    private var windowManager: WindowManager? = null
    private var pointerView: PointerView? = null
    private var isShowing = false
    private var onClickListener: ((Float, Float) -> Unit)? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    /**
     * Show pointer overlay
     */
    fun showPointer() {
        if (isShowing) return

        if (!hasOverlayPermission()) {
            Log.w(TAG, "Overlay permission not granted")
            return
        }

        try {
            pointerView = PointerView(context)
            val params = WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                format = android.graphics.PixelFormat.TRANSLUCENT
                width = 100
                height = 100
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            windowManager?.addView(pointerView, params)
            isShowing = true
            Log.d(TAG, "Pointer shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show pointer", e)
        }
    }

    /**
     * Hide pointer overlay
     */
    fun hidePointer() {
        if (!isShowing || pointerView == null) return

        try {
            windowManager?.removeView(pointerView)
            pointerView = null
            isShowing = false
            Log.d(TAG, "Pointer hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide pointer", e)
        }
    }

    /**
     * Move pointer to position (x, y in normalized coordinates 0-1)
     */
    fun movePointer(normalizedX: Float, normalizedY: Float) {
        if (!isShowing || pointerView == null) return

        try {
            val displayMetrics = context.resources.displayMetrics
            val screenX = (normalizedX * displayMetrics.widthPixels).toInt()
            val screenY = (normalizedY * displayMetrics.heightPixels).toInt()

            val params = WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                format = android.graphics.PixelFormat.TRANSLUCENT
                width = 100
                height = 100
                gravity = Gravity.TOP or Gravity.START
                x = screenX - 50  // Center pointer
                y = screenY - 50
            }

            windowManager?.updateViewLayout(pointerView, params)
            pointerView?.setPosition(screenX.toFloat(), screenY.toFloat())  // FIXED: Convert Int to Float
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move pointer", e)
        }
    }

    /**
     * Simulate click at pointer position
     */
    fun click() {
        if (!isShowing || pointerView == null) return
        pointerView?.performClick()
    }

    /**
     * Simulate double click
     */
    fun doubleClick() {
        click()
        try {
            Thread.sleep(100)
            click()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Simulate long press
     */
    fun longPress(duration: Long = 500) {
        if (!isShowing || pointerView == null) return
        pointerView?.performLongPress(duration)
    }

    /**
     * Set click listener
     */
    fun setOnClickListener(listener: (Float, Float) -> Unit) {
        onClickListener = listener
    }

    /**
     * Get pointer position
     */
    fun getPointerPosition(): PointF? {
        return pointerView?.position
    }

    /**
     * Check overlay permission
     */
    private fun hasOverlayPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Release resources
     */
    fun release() {
        hidePointer()
    }
}

/**
 * Pointer View
 * Custom view for rendering the pointer/cursor
 */
class PointerView(context: Context) : View(context) {

    var position = PointF(0f, 0f)
    private val paint = Paint().apply {
        color = android.graphics.Color.CYAN
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint().apply {
        color = android.graphics.Color.argb(100, 0, 255, 255)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = 20f

        // Draw circle (pointer)
        canvas.drawCircle(centerX, centerY, radius, fillPaint)
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw crosshair
        canvas.drawLine(centerX - 10, centerY, centerX + 10, centerY, paint)
        canvas.drawLine(centerX, centerY - 10, centerX, centerY + 10, paint)
    }

    fun setPosition(x: Float, y: Float) {
        position.set(x, y)
    }

    // FIXED: Add override modifier
    override fun performClick(): Boolean {
        super.performClick()
        // Visual feedback
        alpha = 0.5f
        postDelayed({ alpha = 1f }, 100)
        return true
    }

    fun performLongPress(duration: Long) {
        // Visual feedback for long press
        alpha = 0.3f
        postDelayed({ alpha = 1f }, duration)
    }
}
