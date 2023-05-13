package com.example.kidsdrawingapp

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import yuku.ambilwarna.AmbilWarnaDialog

class CustomEditText(context: Context, attrs: AttributeSet? = null): androidx.appcompat.widget.AppCompatEditText(context, attrs), View.OnTouchListener {
    private var initialX = 0f
    private var initialY = 0f
    private var activity: AppCompatActivity = context as AppCompatActivity //get the current activity the view is running on
    private val textBoxTools: ConstraintLayout = activity.findViewById(R.id.cl_text_box_tools)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) {
            changeBackgroundColor()
            changeTextColor()
            toggleBorder()
            removeTextBox()
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        requestFocus() //switch focus to the view
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                initialX = view!!.x - event.rawX
                initialY = view.y - event.rawY
                //show the text box tools bar when view is touched
                if (textBoxTools.visibility != View.VISIBLE){
                    textBoxTools.visibility = View.VISIBLE
                    textBoxTools.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                view!!.x = event.rawX + initialX
                view.y = event.rawY + initialY
            }
            MotionEvent.ACTION_UP -> {
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) //show the keyboard
                return true
            }
            else -> return false
        }
        super.onTouchEvent(event)
        return true
    }

//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
//            override fun onGlobalLayout() {
//                viewTreeObserver.removeOnGlobalLayoutListener(this)
//                toggleBorder()
//            }
//        })
//    }

    //method to toggle textbox border
    private fun toggleBorder(){
        val toggleBtn = activity.findViewById<ImageButton?>(R.id.ib_toggle_border)
        toggleBtn.setOnClickListener {
            if(this.background == null) this.setBackgroundResource(R.drawable.et_bg)
            else this.background = null
        }
    }

    //method to remove a textbox
    private fun removeTextBox(){
        val removeBtn = activity.findViewById<ImageButton?>(R.id.ib_remove_text_box)
        removeBtn.setOnClickListener{
            //use alertbox to confirm view removal
            AlertDialog.Builder(context).apply {
                setTitle("Confirm Action")
                setMessage("You are about to delete the current textbox. \n Do you want to proceed?")
                setCancelable(true)
                setPositiveButton("Yes"){_, _ ->
                    val parent = this@CustomEditText.parent as ViewGroup
                    parent.removeView(this@CustomEditText)
                    //hide the tools bar when view is removed
                    if (textBoxTools.visibility != View.GONE){
                        textBoxTools.visibility = View.GONE
                        textBoxTools.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                    }
                }
                setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
        }
    }

    //change the text box text color using the color picker dialog
    private fun changeTextColor(){
        activity.findViewById<ImageButton>(R.id.ib_text_box_color_selector).setOnClickListener{
            val dialog = AmbilWarnaDialog(context,  Color.BLACK, true, object : AmbilWarnaDialog.OnAmbilWarnaListener{
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    this@CustomEditText.setTextColor(color)
                }
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
            })
            dialog.show()
        }

    }

    //change the text box background color using the color picker dialog
    private fun changeBackgroundColor(){
        activity.findViewById<ImageButton>(R.id.ib_text_box_bg_color).setOnClickListener{
            val dialog = AmbilWarnaDialog(context,  Color.BLACK, true, object : AmbilWarnaDialog.OnAmbilWarnaListener{
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    this@CustomEditText.setBackgroundColor(color)
                }
                override fun onCancel(dialog: AmbilWarnaDialog?) {}
            })
            dialog.show()
        }

    }
}