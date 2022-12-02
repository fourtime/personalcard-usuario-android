package br.com.tln.personalcard.usuario.extensions

import android.transition.TransitionManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import timber.log.Timber

fun View.slideInTop(topMargin: Int) {
    val constraintLayout = parent as? ConstraintLayout
    val params = layoutParams as? ConstraintLayout.LayoutParams

    if (params == null || constraintLayout == null) {
        Timber.i("Animation supported only on ConstraintLayout")
        return
    }

    if (params.topMargin == topMargin) {
        return
    }

    TransitionManager.beginDelayedTransition(constraintLayout)

    params.topMargin = topMargin

    layoutParams = params
}

fun View.slideOutTop(topMargin: Int) {
    val constraintLayout = parent as? ConstraintLayout
    val params = layoutParams as? ConstraintLayout.LayoutParams

    if (params == null || constraintLayout == null) {
        Timber.i("Animation supported only on ConstraintLayout")
        return
    }

    if (params.topMargin == topMargin) {
        return
    }

    TransitionManager.beginDelayedTransition(constraintLayout)

    params.topMargin = topMargin

    layoutParams = params
}