package com.davidstudioz.david.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Gesture Recognition & Control
 * Detects: swipe, tap, long-press, pinch-zoom
 * Executes voice commands based on gestures
 */
class GestureController(
    context: Context,
    private val onGestureDetected: (gesture: GestureType, details: String) -> Unit
) : View.OnTouchListener {

    enum class GestureType {
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_UP,
        SWIPE_DOWN,
        SINGLE_TAP,
        DOUBLE_TAP,
        TRIPLE_TAP,
        LONG_PRESS,
        PINCH_IN,
        PINCH_OUT
    }

    private val gestureDetector: GestureDetector
    private var lastTouchTime = 0L
    private var tapCount = 0
    private val tapThreshold = 300L

    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    private var initialX = 0f
    private var initialY = 0f
    private var initialDistance = 0f
    private var pointerCount = 0

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSwipe(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = event2.x - event1.x
                val diffY = event2.y - event1.y

                return when {
                    Math.abs(diffX) > Math.abs(diffY) -> {
                        // Horizontal swipe
                        when {
                            diffX > swipeThreshold && velocityX > swipeVelocityThreshold -> {
                                onGestureDetected(GestureType.SWIPE_RIGHT, "Swiped right")
                                true
                            }
                            diffX < -swipeThreshold && velocityX < -swipeVelocityThreshold -> {
                                onGestureDetected(GestureType.SWIPE_LEFT, "Swiped left")
                                true
                            }
                            else -> false
                        }
                    }
                    else -> {
                        // Vertical swipe
                        when {
                            diffY > swipeThreshold && velocityY > swipeVelocityThreshold -> {
                                onGestureDetected(GestureType.SWIPE_DOWN, "Swiped down")
                                true
                            }
                            diffY < -swipeThreshold && velocityY < -swipeVelocityThreshold -> {
                                onGestureDetected(GestureType.SWIPE_UP, "Swiped up")
                                true
                            }
                            else -> false
                        }
                    }
                }
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onGestureDetected(GestureType.SINGLE_TAP, "Single tap at ${e.x}, ${e.y}")
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onGestureDetected(GestureType.DOUBLE_TAP, "Double tap at ${e.x}, ${e.y}")
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                onGestureDetected(GestureType.LONG_PRESS, "Long press at ${e.x}, ${e.y}")
            }
        })
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTapCount(event)
                initialX = event.x
                initialY = event.y
                pointerCount = event.pointerCount
                if (pointerCount == 2) {
                    initialDistance = calculateDistance(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {
                    val currentDistance = calculateDistance(event)
                    val distanceDiff = currentDistance - initialDistance

                    when {
                        distanceDiff > 10 -> {
                            onGestureDetected(GestureType.PINCH_OUT, "Pinched out: $distanceDiff")
                            initialDistance = currentDistance
                        }
                        distanceDiff < -10 -> {
                            onGestureDetected(GestureType.PINCH_IN, "Pinched in: ${Math.abs(distanceDiff)}")
                            initialDistance = currentDistance
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                gestureDetector.onTouchEvent(event)
            }
        }

        return gestureDetector.onTouchEvent(event)
    }

    /**
     * Handle tap count for triple tap detection
     */
    private fun handleTapCount(event: MotionEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTouchTime <= tapThreshold) {
            tapCount++
            if (tapCount == 3) {
                onGestureDetected(GestureType.TRIPLE_TAP, "Triple tap at ${event.x}, ${event.y}")
                tapCount = 0
            }
        } else {
            tapCount = 1
        }
        lastTouchTime = currentTime
    }

    /**
     * Calculate distance between two fingers (for pinch detection)
     */
    private fun calculateDistance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
}
