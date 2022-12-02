package br.com.tln.personalcard.usuario.ui.initialization.forgotpassword

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.fragment.findNavController
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationForgotPasswordBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.observeEvent
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.widget.ErrorToast

class InitializationForgotPasswordFragment :
    BaseFragment<FragmentInitializationForgotPasswordBinding, InitializationForgotPasswordNavigator, InitializationForgotPasswordViewModel>(
        R.layout.fragment_initialization_forgot_password,
        InitializationForgotPasswordViewModel::class.java
    ), InitializationForgotPasswordNavigator {

    private var errorToast: ErrorToast? = null

    private var loadingDialog: ProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().continueClickListener = continueClickListener()

        viewModel.helpLiveData.observeEvent(viewLifecycleOwner) {
            onHelpClicked()
        }

        requireBinding().initializationForgotPasswordCpf.editText?.showSoftKeyboad()

        viewModel.cpf.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
    }

    override fun navigateToForgotPasswordConfirmation() {
//        if (findNavController().currentDestination?.id != R.id.initializationPasswordFragment) {
//            return
//        }
//
//        val directions =
//            InitializationPasswordFragmentDirections.actionInitializationPasswordFragmentToForgotPasswordFragment()
//        findNavController().navigate(directions)
    }

    override fun navigateToRecoveryPasswordSuccess(cpf: String, firstAccess: Boolean) {
        if (findNavController().currentDestination?.id != R.id.initializationForgotPasswordFragment) {
            return
        }

        val directions =
            InitializationForgotPasswordFragmentDirections.actionInitializationForgotPasswordFragmentToRecoveryPasswordSuccessFragment(
                cpf = cpf,
                firstAccess = firstAccess
            )
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

    override fun onDestroy() {
        super.onDestroy()
        errorToast?.dismiss()
    }

    private fun continueClickListener() = View.OnClickListener {
        viewModel.continueClicked()?.observeResource(viewLifecycleOwner) {
            when (it) {
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

    private fun onHelpClicked() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.SUPPORT_LINK))
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