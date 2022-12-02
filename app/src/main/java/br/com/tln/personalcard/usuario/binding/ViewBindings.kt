package br.com.tln.personalcard.usuario.binding

import android.view.View
import androidx.databinding.BindingAdapter
import br.com.tln.personalcard.usuario.R

object ViewBindings {

    @JvmStatic
    @BindingAdapter(value = ["visible"])
    fun View.visible(visible: Boolean) {
        this.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter(value = ["invisible"])
    fun View.invisible(invisible: Boolean) {
        this.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter(value = ["gone"])
    fun View.gone(gone: Boolean) {
        this.visibility = if (gone) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter(value = ["android:onClick", "delay"], requireAll = false)
    fun View.onClick(clickListener: View.OnClickListener?, delayParam: Long) {
        setDelayedClickListener(clickListener, delayParam)
    }

    @JvmStatic
    @BindingAdapter(value = ["android:onClickListener", "delay"], requireAll = false)
    fun View.onClickListener(clickListener: View.OnClickListener?, delayParam: Long) {
        setDelayedClickListener(clickListener, delayParam)
    }

    private fun View.setDelayedClickListener(
        clickListener: View.OnClickListener?,
        delayParam: Long
    ) {
        if (clickListener == null) {
            setOnClickListener(null)
            return
        }

        val delay = if (delayParam == 0L) {
            250
        } else {
            delayParam
        }

        setOnClickListener OnClickListener@{
            val lastClick = getTag(R.id.clickDelay) as? Long?

            val canClick = lastClick?.let {
                System.currentTimeMillis() - lastClick >= delay
            } ?: true

            if (!canClick) return@OnClickListener

            setTag(R.id.clickDelay, System.currentTimeMillis())
            clickListener.onClick(it)
        }
    }
}