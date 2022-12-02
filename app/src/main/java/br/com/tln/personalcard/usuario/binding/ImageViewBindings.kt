package br.com.tln.personalcard.usuario.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object ImageViewBindings {

    @JvmStatic
    @BindingAdapter(value = ["android:src", "circleCrop"], requireAll = true)
    fun ImageView.src(src: String?, circleCrop: Boolean) {
        Glide.with(this)
            .load(src)
            .let {
                if (circleCrop) {
                    it.apply(RequestOptions.circleCropTransform())
                } else {
                    it
                }
            }
            .into(this)
    }
}