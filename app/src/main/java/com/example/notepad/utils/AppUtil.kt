package com.example.notepad.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object AppUtil {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupKeyboardHiderForAllViews(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val focusedView = v.rootView.findFocus()
                    if (focusedView is EditText) {
                        focusedView.clearFocus()
                        hideKeyboard(focusedView)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupKeyboardHiderForAllViews(view.getChildAt(i))
            }
        }
    }

}