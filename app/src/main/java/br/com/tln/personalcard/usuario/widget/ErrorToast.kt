package br.com.tln.personalcard.usuario.widget

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import br.com.tln.personalcard.usuario.R
import com.androidadvance.topsnackbar.TSnackbar

class ErrorToast(val snackBar: TSnackbar) {

    fun show() {
        snackBar.show()
    }

    fun dismiss() {
        snackBar.dismiss()
    }

    companion object {
        fun make(context: Context, view: View, error: String, indeterminate: Boolean): ErrorToast {
            val left = context.resources.getDimensionPixelOffset(R.dimen.snackbar_margin_left)
            val right = context.resources.getDimensionPixelOffset(R.dimen.snackbar_margin_right)
            val top = context.resources.getDimensionPixelOffset(R.dimen.snackbar_margin_top)
            val horizontal =
                context.resources.getDimensionPixelOffset(R.dimen.snackbar_padding_horizontal)

            val duration = if (indeterminate) {
                TSnackbar.LENGTH_INDEFINITE
            } else {
                TSnackbar.LENGTH_SHORT
            }

            val snackBar = TSnackbar.make(view, error, duration)

            val snackBarView = snackBar.view
            snackBarView.background = context.resources.getDrawable(R.drawable.error_floating_label)
            (snackBarView.layoutParams as? FrameLayout.LayoutParams)?.let { params ->
                params.marginStart = left
                params.marginEnd = right
                params.topMargin = top

                snackBarView.layoutParams = params
            }

            val textView =
                snackBarView.findViewById<TextView>(com.androidadvance.topsnackbar.R.id.snackbar_text)
            textView.setTextColor(context.resources.getColor(R.color.telenetColorOnError))
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            textView.setPadding(horizontal, 0, horizontal, 0)

            snackBar.setIconLeft(R.drawable.error_floating_label_alert, 16f)

            return ErrorToast(snackBar)
        }

        fun show(context: Context, view: View, error: String, indeterminate: Boolean): ErrorToast {
            return make(
                context = context,
                view = view,
                error = error,
                indeterminate = indeterminate
            ).also {
                it.show()
            }
        }
    }
}