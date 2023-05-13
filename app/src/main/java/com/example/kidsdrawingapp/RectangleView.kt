package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RectangleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var resizeCorner = -1 // -1: not resizing, 0: top-left, 1:top-right, 2: bottom-right, 3:bottom-left
    private var lastX = 0f
    private var lastY = 0f

    private val paint = Paint().apply{
        color = Color.BLUE
        strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(startX, startY, endX, endY, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    if (isInResizeCorner(event.x, event.y)){
                        // if the touch is inside a resizable corner, start resizing
                        resizeCorner = getResizeCorner(event.x, event.y)
                    }else{
                        // otherwise, start dragging the rectange
                        startX = event.x - (endX - startX)
                        startY = event.y - (endY - startY)
                        endX = event.x
                        endY = event.y
                    }
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    if(resizeCorner >= 0){
                        // if resizing, adjust the corner based on the touch position
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        when(resizeCorner){
                            0 -> {
                                startX += dx
                                startY += dy
                            }
                            1 -> {
                                endX += dx
                                startY += dy
                            }
                            2 -> {
                                endX += dx
                                endY += dy
                            }
                            0 -> {
                                startX += dx
                                endY += dy
                            }
                        }
                        lastX = event.x
                        lastY = event.y
                    }else{
                        // otherwise, move the rectangle
                        endX = event.x
                        endY = event.y
                        startX = event.x - (endX - startX)
                        startY = event.y - (endY - startY)
                    }
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    resizeCorner = -1 //stop resizing
                }
                else -> return false
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun isInResizeCorner(x: Float, y: Float) : Boolean{
        val threshold = 5f //increase to make if easier to touch the corners
        return x >= startX - threshold && x <= startX + threshold && y >= startY - threshold && y<= startY + threshold ||
                x >= endX - threshold && x <= endX + threshold && y >= startY - threshold && y<= startY + threshold ||
                x >= endX - threshold && x <= endX + threshold && y >= endY - threshold && y<= endY + threshold ||
                x >= startX - threshold && x <= startX + threshold && y >= endY - threshold && y<= endY + threshold
    }

    private fun getResizeCorner(x: Float, y: Float) : Int{
        val threshold = 5f //increase to make if easier to touch the corners
        return when{
            x >= startX - threshold && x <= startX + threshold && y >= startY - threshold && y<= startY + threshold -> 0
            x >= endX - threshold && x <= endX + threshold && y >= startY - threshold && y<= startY + threshold -> 1
            x >= endX - threshold && x <= endX + threshold && y >= endY - threshold && y<= endY + threshold -> 2
            x >= startX - threshold && x <= startX + threshold && y >= endY - threshold && y<= endY + threshold -> 3
            else -> -1 //not in any corner
        }
    }

    fun setRect(left: Float, top: Float, right: Float, bottom: Float){
        startX = left
        startY = top
        endX = right
        endY = bottom
        invalidate()
    }
}