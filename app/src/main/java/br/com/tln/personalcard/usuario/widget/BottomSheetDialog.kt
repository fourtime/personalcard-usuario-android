package br.com.tln.personalcard.usuario.widget

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.databinding.WidgetBottomSheetDialogBinding
import br.com.tln.personalcard.usuario.databinding.WidgetBottomSheetDialogOptionBinding

class BottomSheetDialog : com.google.android.material.bottomsheet.BottomSheetDialog {

    val binding: WidgetBottomSheetDialogBinding

    constructor(context: Context) : super(context, R.style.AppTheme_Dialog)
    constructor(context: Context, theme: Int) : super(context, theme)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(
        context,
        cancelable,
        cancelListener
    ) {
        cancelable(cancelable) {
            cancelListener?.onCancel(this)
        }
    }

    init {
        setContentView(R.layout.widget_bottom_sheet_dialog)
        binding =
            WidgetBottomSheetDialogBinding.bind(findViewById(R.id.bottomSheetDialogContainer)!!)

        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    fun cancelable(cancelable: Boolean, clickListener: ((BottomSheetDialog) -> Unit)? = null) {
        super.setCancelable(cancelable)

        setOnCancelListener {
            clickListener?.invoke(this)
        }
    }

    fun showCloseButton(
        show: Boolean,
        clickListener: ((BottomSheetDialog) -> Unit) = { _ -> dismiss() }
    ) {
        binding.closeButtonVisible = show
        if (show) {
            binding.closeButtonClickListener = View.OnClickListener { clickListener(this) }
        }
    }

    fun icon(@DrawableRes res: Int) {
        binding.iconDrawable = ContextCompat.getDrawable(context, res)
    }

    fun title(title: CharSequence) {
        binding.title = title
    }

    fun message(message: CharSequence) {
        binding.message = message
    }

    fun neutralText(
        neutralText: CharSequence,
        clickListener: ((BottomSheetDialog) -> Unit) = { _ -> dismiss() }
    ) {
        binding.neutralText = neutralText
        binding.neutralClickListener = View.OnClickListener { clickListener(this) }
    }

    fun confirmText(
        confirmText: CharSequence,
        clickListener: ((BottomSheetDialog) -> Unit) = { _ -> dismiss() }
    ) {
        binding.confirmText = confirmText
        binding.confirmClickListener = View.OnClickListener { clickListener(this) }
    }

    fun dangerText(
        dangerText: CharSequence,
        clickListener: ((BottomSheetDialog) -> Unit) = { _ -> dismiss() }
    ) {
        binding.dangerText = dangerText
        binding.dangerClickListener = View.OnClickListener { clickListener(this) }
    }

    fun options(func: Options.() -> Unit) {
        val optionsObj = Options()
        optionsObj.func()

        val options = optionsObj.options

        binding.bottomSheetDialogOptions.visibility = if (options.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        options.forEach { option ->
            val binding = WidgetBottomSheetDialogOptionBinding.inflate(
                LayoutInflater.from(context),
                this.binding.bottomSheetDialogOptions,
                true
            )

            binding.text = option.text
            binding.clickListener = View.OnClickListener {
                option.clickListener(option)
            }
        }
    }

    inline fun build(block: BottomSheetDialog.() -> Unit): BottomSheetDialog {
        this.block()
        return this
    }

    inline fun show(block: BottomSheetDialog.() -> Unit): BottomSheetDialog {
        this.block()
        this.show()
        return this
    }

    class Options {
        val options: MutableList<Option> = mutableListOf()

        fun option(text: String, clickListener: (Option) -> Unit) {
            this.options.add(Option(text = text, clickListener = clickListener))
        }

        data class Option(val text: String, val clickListener: (Option) -> Unit)
    }

    companion object {
        inline fun genericError(
            context: Context,
            block: BottomSheetDialog.() -> Unit = {}
        ): BottomSheetDialog {
            return BottomSheetDialog(context)
                .apply {
                    block()
                }
                .build {
                    cancelable(true)
                    icon(R.drawable.error)
                    message(context.getString(R.string.dialog_generic_error_message))
                    confirmText(context.getString(R.string.dialog_generic_error_button_text))
                }
        }

        fun connectionError(
            context: Context,
            block: BottomSheetDialog.() -> Unit = {}
        ): BottomSheetDialog {
            return BottomSheetDialog(context)
                .apply {
                    block()
                }
                .build {
                    cancelable(true)
                    icon(R.drawable.error)
                    message(context.getString(R.string.dialog_generic_error_message))
                    confirmText(context.getString(R.string.dialog_generic_error_button_text))
                }
        }
    }
}