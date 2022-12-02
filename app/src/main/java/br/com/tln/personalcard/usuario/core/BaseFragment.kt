package br.com.tln.personalcard.usuario.core

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import br.com.tln.personalcard.usuario.BR
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.di.Injectable
import br.com.tln.personalcard.usuario.extensions.observeEvent
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog
import javax.inject.Inject

abstract class BaseFragment<DB : ViewDataBinding, NV : Navigator, VM : BaseViewModel<in NV>>(
    @LayoutRes private val layoutRes: Int,
    private val viewModelClass: Class<VM>,
    private val retainViewInstance: Boolean = false
) : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val viewModelProvider by lazy {
        ViewModelProviders.of(this, viewModelFactory)
    }
    protected val viewModel by lazy {
        viewModelProvider.get(viewModelClass)
    }

    protected var binding: DB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this as? NV
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding: DB = if (retainViewInstance) {
            this.binding ?: inflateBinding(inflater = inflater, container = container)
        } else {
            inflateBinding(inflater = inflater, container = container)
        }

        this.binding = binding

        binding.lifecycleOwner = viewLifecycleOwner
        binding.setVariable(BR.viewModel, viewModel)

        if (this is HasToolbar) {
            setupToolbar()
        }

        viewModel.connectionErrorLiveData.observeEvent(viewLifecycleOwner) {
            showConnectionError()
        }

        viewModel.unknownErrorLiveData.observeEvent(viewLifecycleOwner) {
            showUnkownError()
        }

        viewModel.errorMessageLiveData.observeEvent(viewLifecycleOwner) {
            showErrorMessage(it.data)
        }

        return binding.root
    }

    protected open fun showConnectionError() {
        BottomSheetDialog.connectionError(requireContext()) {
            setOnDismissListener(connectionErrorDialogDismissListener())
        }.show()
    }

    protected open fun showUnkownError() {
        BottomSheetDialog.genericError(requireContext()) {
            setOnDismissListener(unknownErrorDialogDismissListener())
        }.show()
    }

    protected open fun showErrorMessage(message: String) {
        BottomSheetDialog(requireContext())
            .show {
                icon(R.drawable.error)
                message(message)
                confirmText(getString(R.string.dialog_custom_error_button_text))
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (!retainViewInstance) {
            binding = null
        }
    }

    private fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DB {
        return DataBindingUtil.inflate(inflater, layoutRes, container, false)
    }

    protected open fun connectionErrorDialogDismissListener(): DialogInterface.OnDismissListener? {
        return null
    }

    protected open fun unknownErrorDialogDismissListener(): DialogInterface.OnDismissListener? {
        return null
    }

    protected fun requireBinding(): DB {
        return binding ?: throw IllegalStateException(
            "Can't access the Fragment View's DataBinding when getView() is null i.e., before onCreateView() or after onDestroyView()"
        )
    }

}