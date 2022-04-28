package com.ssmmhh.jibam.presentation.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Forces the device soft keyboard to show up.
 *
 * @param editText, The edit text that will receive soft keyboard input.
 */
fun forceKeyboardToOpenForEditText(activity: Activity, editText: EditText) {
    editText.requestFocus()
    val imm: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}