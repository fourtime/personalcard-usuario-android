package br.com.tln.personalcard.usuario.binding

import android.content.res.Resources
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import br.com.tln.personalcard.usuario.PT_BR
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.isPositive
import br.com.tln.personalcard.usuario.extensions.isValueEqual
import com.redmadrobot.inputmask.helper.Mask
import com.redmadrobot.inputmask.model.CaretString
import org.threeten.bp.temporal.TemporalAccessor
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.roundToInt


object TextViewBindings {

    @JvmStatic
    @BindingAdapter(value = ["android:text", "mask"], requireAll = true)
    fun TextView.mask(rawText: String?, mask: String) {

        val originalText = getTag(R.id.textViewMaskRawText) as? String?

        if (originalText == rawText) {
            return
        }

        val text = rawText?.let {
            Mask(mask).apply(
                text = CaretString(rawText, rawText.length),
                autocomplete = false
            ).formattedText.string
        }

        val oldText = this.text

        if (text === oldText || text == null && oldText.isEmpty()) {
            return
        }

        if (text == oldText) {
            return
        }

        setTag(R.id.textViewMaskRawText, rawText)
        this.text = text
    }

    @JvmStatic
    @BindingAdapter(value = ["android:text", "reverseAnimation", "animation"], requireAll = true)
    fun TextView.animate(@StringRes resId: Int, reverseAnimation: Runnable, animation: Runnable) {
        val text = try {
            resources.getString(resId)
        } catch (e: Resources.NotFoundException) {
            resources.getString(R.string.empty)
        }

        animate(text, reverseAnimation, animation)
    }

    @JvmStatic
    @BindingAdapter(value = ["android:text", "reverseAnimation", "animation"], requireAll = true)
    fun TextView.animate(text: String?, reverseAnimation: Runnable, animation: Runnable) {

        val oldText = this.text

        if (text === oldText || text == null && oldText.isEmpty()) {
            return
        }

        if (text == oldText) {
            return
        }

        val animationState = (getTag(R.id.textViewAnimationState) as? TextViewAnimationState)

        this.text = text

        if (text.isNullOrEmpty()) {
            if (animationState != TextViewAnimationState.ANIMATED) {
                return
            }

            reverseAnimation.run()
            setTag(R.id.textViewAnimationState, TextViewAnimationState.REVERSED)
        } else {
            if (animationState == TextViewAnimationState.ANIMATED) {
                return
            }

            animation.run()
            setTag(R.id.textViewAnimationState, TextViewAnimationState.ANIMATED)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["android:text"], requireAll = true)
    fun TextView.formatCurrency(value: BigDecimal?) {
        formatCurrency(newValue = value, symbolTextSize = textSize)
    }

    @JvmStatic
    @BindingAdapter(value = ["android:text", "symbolTextSize"], requireAll = true)
    fun TextView.formatCurrency(newValue: BigDecimal?, symbolTextSize: Float) {
        val initialized = getTag(R.id.textViewCurrencyValueInitialized) as? Boolean ?: false
        val currentValue = getTag(R.id.textViewCurrencyValue) as? BigDecimal

        if (initialized && newValue.isValueEqual(currentValue)) {
            return
        }

        val text: CharSequence = if (newValue == null || newValue.isValueEqual(BigDecimal.ZERO)) {
            "-"
        } else {
            val currencyInstance = NumberFormat.getCurrencyInstance(PT_BR)
            val currencyPrefix = if (currencyInstance is DecimalFormat) {
                if (newValue.isPositive()) {
                    currencyInstance.positivePrefix
                } else {
                    currencyInstance.negativePrefix
                }
            } else {
                currencyInstance.currency.symbol
            }

            val formattedValue = currencyInstance.format(newValue)

            if (symbolTextSize == textSize) {
                formattedValue
            } else {
                SpannableString(formattedValue).apply {
                    setSpan(
                        AbsoluteSizeSpan(symbolTextSize.roundToInt()),
                        formattedValue.indexOf(currencyPrefix),
                        formattedValue.indexOf(currencyPrefix) + currencyPrefix.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
        }

        val oldText = this.text

        if (text == oldText) {
            return
        }

        if (!initialized) {
            setTag(R.id.textViewCurrencyValueInitialized, true)
        }

        setTag(R.id.textViewCurrencyValue, newValue)
        this.text = text
    }

    @JvmStatic
    @BindingAdapter(value = ["android:text", "pattern"], requireAll = true)
    fun TextView.formatTemporalAccessor(newValue: TemporalAccessor?, pattern: String) {
        val initialized = getTag(R.id.textViewTemporalValueInitialized) as? Boolean ?: false
        val currentValue = getTag(R.id.textViewTemporalValue) as? TemporalAccessor

        if (initialized && newValue == currentValue) {
            return
        }

        val text = newValue?.format(pattern)

        val oldText = this.text

        if (text == oldText) {
            return
        }

        if (!initialized) {
            setTag(R.id.textViewTemporalValueInitialized, true)
        }

        setTag(R.id.textViewTemporalValue, newValue)
        this.text = text
    }

    enum class TextViewAnimationState {
        REVERSED, ANIMATED
    }
}