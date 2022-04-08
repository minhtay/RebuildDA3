package com.example.doan3.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object Utils {
    fun hideSoftKeyboard(context: Context,view: View){
        try {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}