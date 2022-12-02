package br.com.tln.personalcard.usuario.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment

fun EditText.showSoftKeyboad() {
    requestFocus()
    val inputMethodManager: InputMethodManager? =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager

    inputMethodManager?.let {
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideSoftKeyboard() {
    clearFocus()
    val inputMethodManager: InputMethodManager? =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager

    inputMethodManager?.let {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Fragment.hideSoftKeyboard() {
    view?.hideSoftKeyboard()
}