package com.davidstudioz.david.gesture

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat
import com.davidstudioz.david.R

/**
 * GesturePointerOverlay - Mouse-like pointer for gesture control
 * ✅ Visual pointer that follows hand position
 * ✅ Click animation on pinch gesture
 * ✅ Customizable appearance
 */
class GesturePointerOverlay(context: Context) : View(context) {
    
    private var pointerX = 0f
    private var pointerY = 0f
    private var isVisible = false
    private var isClicking = false
    
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
    }
    
    private val pointerOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private val clickRipplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    private var rippleRadius = 0f
    private var rippleAlpha = 255
    
    fun updatePosition(x: Float, y: Float) {
        pointerX = x * width
        pointerY = y * height
        isVisible = true
        invalidate()
    }
    
    fun hide() {
        isVisible = false
        invalidate()
    }
    
    fun triggerClick() {
        isClicking = true
        rippleRadius = 0f
        rippleAlpha = 255
        animateClick()
    }
    
    private fun animateClick() {
        post(object : Runnable {
            override fun run() {
                if (rippleRadius < 50f) {
                    rippleRadius += 5f
                    rippleAlpha = (255 * (1 - rippleRadius / 50f)).toInt()
                    invalidate()
                    postDelayed(this, 16)
                } else {
                    isClicking = false
                    invalidate()
                }
            }
        })
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!isVisible) return
        
        // Draw pointer
        canvas.drawCircle(pointerX, pointerY, 20f, pointerPaint)
        canvas.drawCircle(pointerX, pointerY, 20f, pointerOutlinePaint)
        
        // Draw inner dot
        pointerPaint.alpha = 180
        canvas.drawCircle(pointerX, pointerY, 8f, pointerPaint)
        pointerPaint.alpha = 255
        
        // Draw click ripple
        if (isClicking && rippleAlpha > 0) {
            clickRipplePaint.alpha = rippleAlpha
            canvas.drawCircle(pointerX, pointerY, rippleRadius, clickRipplePaint)
        }
    }
}
