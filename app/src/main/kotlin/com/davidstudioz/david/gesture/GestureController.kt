package com.davidstudioz.david.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

interface GestureListener {
    fun onSwipeUp()
    fun onSwipeDown()
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onDoubleTap()
    fun onLongPress()
    fun onPinchZoom(scale: Float)
    fun onRotate(angle: Float)
}

@Singleton
class GestureController @Inject constructor(
    context: Context
) : GestureDetector.OnGestureListener {
    
    private var listener: GestureListener? = null
    private var gestureDetector = GestureDetector(context, this)
    private var scaleGestureDetector: android.view.ScaleGestureDetector? = null
    
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    
    companion object {
        const val SWIPE_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }
    
    fun setGestureListener(listener: GestureListener) {
        this.listener = listener
    }
    
    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
    
    override fun onDown(e: MotionEvent): Boolean {
        downX = e.x
        downY = e.y
        lastX = e.x
        lastY = e.y
        return true
    }
    
    override fun onShowPress(e: MotionEvent) {}
    
    override fun onSingleTapUp(e: MotionEvent): Boolean = true
    
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return true
    }
    
    override fun onLongPress(e: MotionEvent) {
        listener?.onLongPress()
    }
    
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        e1 ?: return false
        
        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y
        
        if (abs(diffX) > abs(diffY)) {
            // Horizontal swipe
            if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    listener?.onSwipeRight()
                } else {
                    listener?.onSwipeLeft()
                }
                return true
            }
        } else {
            // Vertical swipe
            if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    listener?.onSwipeDown()
                } else {
                    listener?.onSwipeUp()
                }
                return true
            }
        }
        return false
    }
    
    fun handleDoubleTap() {
        listener?.onDoubleTap()
    }
}
