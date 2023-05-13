package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.graphics.Color.parseColor
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val drawingList = ArrayList<CustomPath>() // List of all completed drawings
    private val undoList = ArrayList<CustomPath>() // List of all undone drawings
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var canvasPaint: Paint? = null
    private var drawColor: Int = Color.BLACK // Initial drawing color
    private var brushSize: Float = 0f  // Initial stroke width
    private var paint: Paint? = null
    private var path: CustomPath? = null // Path representing the current drawing

    init {
        canvasPaint = Paint(Paint.DITHER_FLAG)
        paint = Paint().apply {
            color = drawColor // Set the initial drawing color
            setBackgroundColor(Color.WHITE)
            style = Paint.Style.STROKE // Set the paint style to stroke
            strokeJoin = Paint.Join.ROUND // Set the stroke join style to round
            strokeCap = Paint.Cap.ROUND // Set the stroke cap style to round
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        path = CustomPath(drawColor, brushSize)
    }

    // Override the onDraw method to draw the completed drawings
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        for (path in drawingList) {
            paint?.strokeWidth = path.brushThickness
            paint?.color = path.paintColor
            canvas.drawPath(path, paint!!) // Draw each completed path using the current paint settings
        }
        canvas.drawPath(path!!, paint!!)
    }

    // Override the onTouchEvent method to handle user input
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> { // User has touched the screen
                path = CustomPath(drawColor, brushSize)
                path!!.moveTo(event.x, event.y) // Start a new path at the touch coordinates
                return true
            }
            MotionEvent.ACTION_MOVE -> { // User is moving their finger across the screen
                path!!.lineTo(event.x, event.y) // Add a line to the current path from the previous touch coordinates to the current touch coordinates
//                drawCircle(event.x, event.y, brushSize, path)
            }
            MotionEvent.ACTION_UP -> { // User has lifted their finger from the screen
                drawingList.add(path!!) // Add the completed path to the list of all completed drawings
//                path.reset() // Reset the current path
                path = CustomPath(drawColor, brushSize)
            }
            else -> return false
        }
        invalidate() // Force a redraw of the view
        return true
    }

    // Method to set the drawing color
    fun setDrawColor(color: Int) {
        drawColor = color
        paint!!.color = drawColor
    }

    fun erase(){
        drawColor = Color.WHITE
        paint!!.color = drawColor
//        paint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // Method to set the stroke width
    fun setStrokeWidth(width: Float) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, resources.displayMetrics)
        paint!!.strokeWidth = brushSize
    }

    // Method to clear all completed drawings
    fun clearDrawing() {
        drawingList.clear()
        invalidate()
    }

    //method to undo drawing
    fun undo(){
        if (drawingList.isNotEmpty()){
            undoList.add(drawingList.last())
            drawingList.removeLast()
            invalidate()
        }
    }

    //method to redo drawing
    fun redo(){
        if (undoList.isNotEmpty()){
            drawingList.add(undoList.last())
            undoList.removeLast()
            invalidate()
        }
    }

//     Method to draw a circle at the given coordinates with the given radius
    private fun drawCircle(x: Float, y: Float, radius: Float, path: CustomPath) {
            path.apply {
            addCircle(x, y, radius, Path.Direction.CW)
        }
//        drawingList.add(circlePath)
//        invalidate()
    }
//
//    // Method to draw a rectangle at the given coordinates with the given width and height
//    fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
//        val rectPath = Path().apply {
//            addRect(left, top, right, bottom, Path.Direction.CW)
//        }
//        drawingList.add(rectPath)
//        invalidate()
//    }
//
//    // Method to draw a triangle at the given coordinates with the given side length
//    fun drawTriangle(x: Float, y: Float, sideLength: Float) {
//        val trianglePath = Path().apply {
//            moveTo(x, y) // Move to the starting point of the triangle
//            lineTo(x + sideLength, y) // Draw a line to the first corner of the triangle
//
//        }
//    }

    internal inner class CustomPath(val paintColor: Int, val brushThickness: Float) : Path()
}