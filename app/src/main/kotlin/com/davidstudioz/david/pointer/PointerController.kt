package com.davidstudioz.david.pointer

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.graphics.drawable.ShapeDrawable
import android.graphics.shapes.OvalShape
import android.graphics.Paint
import android.graphics.Color
import android.os.Build

/**
 * Pointer Controller (AI Mouse Control)
 * AI can control device via pointer (mouse cursor)
 * Hand gestures translate to pointer movement
 */
class PointerController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var pointerView: ImageView? = null
    private var isPointerVisible = false
    private var currentX = 0f
    private var currentY = 0f
    private var pointerClickListener: ((x: Float, y: Float) -> Unit)? = null

    /**
     * Show pointer (mouse cursor) on screen
     */
    fun showPointer() {
        if (isPointerVisible) return

        isPointerVisible = true
        pointerView = ImageView(context).apply {
            // Create pointer drawable
            val drawable = ShapeDrawable(OvalShape()).apply {
                paint.apply {
                    color = Color.YELLOW
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
            }
            setImageDrawable(drawable)
            layoutParams = ImageView.LayoutParams(50, 50)
        }

        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            gravity = Gravity.TOP or Gravity.LEFT
            width = 50
            height = 50
            x = currentX.toInt()
            y = currentY.toInt()
        }

        windowManager.addView(pointerView, params)
    }

    /**
     * Hide pointer
     */
    fun hidePointer() {
        if (!isPointerVisible || pointerView == null) return

        try {
            windowManager.removeView(pointerView)
            pointerView = null
            isPointerVisible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Move pointer to position
     * Hand position from camera: (0.0 - 1.0, 0.0 - 1.0)
     */
    fun movePointer(normalizedX: Float, normalizedY: Float) {
        if (!isPointerVisible || pointerView == null) return

        val displayMetrics = context.resources.displayMetrics
        currentX = normalizedX * displayMetrics.widthPixels
        currentY = normalizedY * displayMetrics.heightPixels

        val params = pointerView?.layoutParams as? WindowManager.LayoutParams ?: return
        params.x = currentX.toInt() - 25 // Center the pointer
        params.y = currentY.toInt() - 25

        try {
            windowManager.updateViewLayout(pointerView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Simulate pointer click at current position
     * Command: "Click"
     */
    fun click() {
        // Find view at current position and perform click
        pointerClickListener?.invoke(currentX, currentY)
    }

    /**
     * Simulate double click
     * Command: "Double click"
     */
    fun doubleClick() {
        click()
        click()
    }

    /**
     * Simulate long press
     * Command: "Long press"
     */
    fun longPress(duration: Long = 500) {
        // Hold for specified duration
        pointerClickListener?.invoke(currentX, currentY)
        Thread.sleep(duration)
        pointerClickListener?.invoke(currentX, currentY)
    }

    /**
     * Simulate drag from current position to target
     * Command: "Drag to x,y"
     */
    fun drag(targetX: Float, targetY: Float, duration: Long = 1000) {
        val startX = currentX
        val startY = currentY
        val steps = 20
        val stepDuration = duration / steps

        for (i in 0..steps) {
            val progress = i.toFloat() / steps
            val x = startX + (targetX - startX) * progress
            val y = startY + (targetY - startY) * progress
            movePointer(x / context.resources.displayMetrics.widthPixels,
                       y / context.resources.displayMetrics.heightPixels)
            Thread.sleep(stepDuration)
        }
    }

    /**
     * Scroll up
     * Command: "Scroll up"
     */
    fun scrollUp(amount: Float = 100f) {
        movePointer(currentX / context.resources.displayMetrics.widthPixels,
                   (currentY - amount) / context.resources.displayMetrics.heightPixels)
    }

    /**
     * Scroll down
     * Command: "Scroll down"
     */
    fun scrollDown(amount: Float = 100f) {
        movePointer(currentX / context.resources.displayMetrics.widthPixels,
                   (currentY + amount) / context.resources.displayMetrics.heightPixels)
    }

    /**
     * Get current pointer position
     */
    fun getPointerPosition(): Pair<Float, Float> {
        return Pair(currentX, currentY)
    }

    /**
     * Set pointer click listener
     */
    fun setOnClickListener(listener: (x: Float, y: Float) -> Unit) {
        pointerClickListener = listener
    }

    /**
     * Release resources
     */
    fun release() {
        hidePointer()
        pointerClickListener = null
    }
}
