package br.com.tln.personalcard.usuario.ui.initialization.password

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationPasswordBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.widget.ErrorToast

class InitializationPasswordFragment :
    BaseFragment<FragmentInitializationPasswordBinding, InitializationPasswordNavigator, InitializationPasswordViewModel>(
        R.layout.fragment_initialization_password,
        InitializationPasswordViewModel::class.java
    ), InitializationPasswordNavigator {

    private var errorToast: ErrorToast? = null

    private val args: InitializationPasswordFragmentArgs by navArgs()

    private var loadingDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cpf = args.cpf
        viewModel.registerEmail = args.registerEmail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().continueClickListener = continueClickListener()

        requireBinding().initializationPasswordPassword.editText?.showSoftKeyboad()
    }

    override fun navigateToForgotPassword() {
        if (findNavController().currentDestination?.id != R.id.initializationPasswordFragment) {
            return
        }

        val directions =
            InitializationPasswordFragmentDirections.actionInitializationPasswordFragmentToInitializationForgotPasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToWelcome(firstAccess: Boolean) {
        if (findNavController().currentDestination?.id != R.id.initializationPasswordFragment) {
            return
        }

        val directions =
            InitializationPasswordFragmentDirections.actionInitializationPasswordFragmentToInitializationWelcomeFragment(
                firstAccess = firstAccess
            )
        findNavController().navigate(directions)
    }

    override fun navigateToHome() {
        if (findNavController().currentDestination?.id != R.id.initializationPasswordFragment) {
            return
        }

        val directions = InitializationPasswordFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    override fun showErrorMessage(message: String) {
        errorToast?.dismiss()

        if (message.isBlank()) {
            return
        }

        this.errorToast = ErrorToast.show(
            context = requireContext(),
            view = requireBinding().root,
            error = message,
            indeterminate = true
        )

    }

    private fun hideErrorToast() {
        viewModel.password.observe(viewLifecycleOwner) {
            errorToast?.dismiss()
        }
        viewModel.validPassword.observe(viewLifecycleOwner) {
            errorToast?.dismiss()
        }
    }

    private fun continueClickListener() = View.OnClickListener {
        hideErrorToast()

        viewModel.continueClicked()?.observeResource(viewLifecycleOwner) { resource ->
            val x = when (resource) {
                is LoadingResource -> {
                    showLoading()
                }
                is SuccessResource -> {
                    hideLoading()
                }
                is ErrorResource -> {
                    hideLoading()
                }
            }
        }
    }

    private fun showLoading() {
        val loadingDialog = this.loadingDialog ?: ProgressDialog(context).also {
            it.setCancelable(false)
            it.setTitle(R.string.app_name)
            it.setMessage(getString(R.string.dialog_loading))
            it.show()
        }

        this.loadingDialog = loadingDialog

        loadingDialog.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss().also { loadingDialog = null }
    }
}