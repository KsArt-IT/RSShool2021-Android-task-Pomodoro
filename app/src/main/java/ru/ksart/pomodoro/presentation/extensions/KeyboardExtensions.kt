package ru.ksart.pomodoro.presentation.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboardAndClearFocus() {
    val view = currentFocus ?: View(this)
    hideKeyboardFrom(view)
    view.clearFocus()
}

fun Context.hideKeyboardFrom(view: View) {
    getSystemService(Activity.INPUT_METHOD_SERVICE)
        .let { it as InputMethodManager }
        .hideSoftInputFromWindow(view.windowToken, 0)
}
