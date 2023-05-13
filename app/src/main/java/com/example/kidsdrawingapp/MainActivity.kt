package com.example.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.size
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //handle image gotten from gallery with intent
    private val gallery: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == RESULT_OK && result.data != null){
            val background: ImageView = findViewById(R.id.iv_bg)
            background.setImageURI(result.data?.data)
        }
    }
    private val WRITE_STORAGE_REQUEST_CODE = 101
    private val READ_STORAGE_REQUEST_CODE = 102
    private val VIEW_STORAGE_REQUEST_CODE = 103
    private var drawingView: DrawingView? = null
    private var colorDialog: Dialog? = null
    private var brushSizeSelectorBtn: ImageButton? = null
    private var eraser: ImageButton? = null
    private var saveImage: ImageButton? = null
    private var colorPicker: ImageButton? = null
    private var galleryBtn: ImageButton? = null
    private var undoBtn: ImageButton? = null
    private var redoBtn: ImageButton? = null
    private var textboxBtn: ImageButton? = null
    private var viewImagesBtn: ImageButton? = null
    private var closeTbTools: ImageButton? = null
    private var textBoxTools: ConstraintLayout? = null
    private var flDrawingCanvas: ViewGroup? = null
    private var toolsBar: LinearLayout? = null
    private var brushSizeSeekBarProgress: Int = 5

    //TODO: be able to change drawingview background color and update the eraser
    //TODO: view app files in a more supported way
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        closeTbTools = findViewById(R.id.ib_close_text_box_tools)
        textBoxTools = findViewById(R.id.cl_text_box_tools)
        closeTbTools?.setOnClickListener{ closeTextBoxTools(textBoxTools) }

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setStrokeWidth(5f)
//        drawingView?.setOnTouchListener { _, _ ->
//            closeTextBox(textBoxTools)
//            false
//        }

        flDrawingCanvas = findViewById(R.id.fl_drawing_canvas)

        brushSizeSelectorBtn = findViewById(R.id.ib_brushsize_selector)
        brushSizeSelectorBtn?.setOnClickListener { showBrushSizeDialog() }

        eraser = findViewById(R.id.ib_eraser)
        eraser?.setOnClickListener {drawingView?.erase() }

        colorPicker = findViewById(R.id.ib_color_picker)
        colorPicker?.setOnClickListener { colorPickerDialog() }

        undoBtn = findViewById(R.id.ib_undo)
        undoBtn?.setOnClickListener { drawingView?.undo() }

        redoBtn = findViewById(R.id.ib_redo)
        redoBtn?.setOnClickListener { drawingView?.redo() }

        toolsBar = findViewById(R.id.ll_tools)
        toolsBar?.setOnClickListener{ closeTextBoxTools(textBoxTools) }

        saveImage = findViewById(R.id.ib_save_image)
        saveImage?.setOnClickListener {
//            val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ContextCompat.checkSelfPermission(this, writePermission) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, arrayOf(writePermission), WRITE_STORAGE_REQUEST_CODE)
//                }
//            }
            AlertDialog.Builder(this).apply {
                setTitle("Confirm Action")
                setMessage("You are about to save your current drawing. \n Do you want to proceed?")
                setCancelable(true)
                setPositiveButton("Yes"){_, _ ->
                    saveImageToGallery(getBitmap(flDrawingCanvas!!))
                }
                setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
        }

        galleryBtn = findViewById(R.id.ib_gallery)
        galleryBtn?.setOnClickListener {
            val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED){
                openGallery()
            } else ActivityCompat.requestPermissions(this, arrayOf(readPermission), READ_STORAGE_REQUEST_CODE)
        }

        viewImagesBtn = findViewById(R.id.ib_view_images)
        viewImagesBtn?.setOnClickListener {
            val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, readPermission) == PackageManager.PERMISSION_GRANTED){
                //intent to view images through the app
                val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivity(intent) //start the intent activity
            } else ActivityCompat.requestPermissions(this, arrayOf(readPermission), VIEW_STORAGE_REQUEST_CODE)
        }

        //create a new text box using CustomEditText class
        textboxBtn = findViewById(R.id.ib_text_box)
        textboxBtn?.setOnClickListener{
            val textBox = CustomEditText(this)
            textBox.setBackgroundResource(R.drawable.et_bg)
            textBox.setPadding(30)
            textBox.textSize = 25f
            textBox.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textBox.requestFocus()
            textBox.setOnTouchListener(textBox)
            flDrawingCanvas?.addView(textBox, flDrawingCanvas!!.size)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(" comer", "$grantResults -- $requestCode")
        when(requestCode){
            READ_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }else Toast.makeText(this, "Read storage permission Denied", Toast.LENGTH_LONG).show()
            }
            VIEW_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //intent to view images through the app
                    val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivity(intent) //start the intent activity
                }else Toast.makeText(this, "Read storage permission Denied", Toast.LENGTH_LONG).show()
            }
            WRITE_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    saveImageToGallery(getBitmap(flDrawingCanvas!!))
                }else Toast.makeText(this, "Writing to storage Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    //method that opens the gallery and selects a background image
    private fun openGallery(){
        //intent to pick image from user images folder
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        gallery.launch(intent) //launch the intent using ActivityResultLauncher
    }

    //method to show the brush size dialog and also set the brush size
    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brushsize_dialog)
        brushDialog.setTitle("Brush size: ")
        val brushSizeSeekBar = brushDialog.findViewById<SeekBar>(R.id.brushsize_seekbar)
        brushSizeSeekBar.progress = brushSizeSeekBarProgress
        val tvBrushSizeSeekBarProgress = brushDialog.findViewById<TextView>(R.id.tv_brushsize_seekbar_progress)
        tvBrushSizeSeekBarProgress.text = brushSizeSeekBarProgress.toString()
        brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingView?.setStrokeWidth(progress.toFloat())
                tvBrushSizeSeekBarProgress.text = progress.toString()
                brushSizeSeekBarProgress = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        brushDialog.show()
    }

    //executed when a color is chosen from the color dialog
    fun selectColor(view: View){
        val imgBtn = view as ImageButton
        val color = Color.parseColor(imgBtn.tag.toString())
        drawingView?.setDrawColor(color)
        colorDialog?.hide()
    }

    //method to save the bitmap to user gallery
    private fun saveImageToGallery(bitmap: Bitmap){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val resolver = contentResolver
                val fileName = "kda_${System.currentTimeMillis()/100}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+ File.separator+"KidsDrawingApp")
                }
                val imageUrl = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                resolver.openOutputStream(imageUrl!!).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                }
                Toast.makeText(this, "Image successfully saved in $imageUrl", Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception){
            Toast.makeText(this, "File was not saved", Toast.LENGTH_LONG).show()
        }

    }

    //get the bitmap of the current drawing
    private fun getBitmap(view: View): Bitmap{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (view.background != null) view.background.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    //configure and implement the color picker dialog
    private fun colorPickerDialog(){
        val dialog = AmbilWarnaDialog(this@MainActivity,  Color.BLACK, true, object : AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView?.setDrawColor(color)
            }
            override fun onCancel(dialog: AmbilWarnaDialog?) {}
        })
        dialog.show()
    }

    //method for closing text box tools bar
    private fun closeTextBoxTools(view: View?){
        if (view?.visibility == View.VISIBLE){
            view.visibility = View.GONE
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
        }
    }
}
