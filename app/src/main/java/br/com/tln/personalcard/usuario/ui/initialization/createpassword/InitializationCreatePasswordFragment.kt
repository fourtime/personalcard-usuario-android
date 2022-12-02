package br.com.tln.personalcard.usuario.ui.initialization.createpassword

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationCreatePasswordBinding
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.extensions.slideInTop
import br.com.tln.personalcard.usuario.extensions.slideOutTop

class InitializationCreatePasswordFragment :
    BaseFragment<FragmentInitializationCreatePasswordBinding, InitializationCreatePasswordNavigator, InitializationCreatePasswordViewModel>(
        R.layout.fragment_initialization_create_password,
        InitializationCreatePasswordViewModel::class.java
    ), InitializationCreatePasswordNavigator {

    private var loadingDialog: ProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().animationShowError = showErrorAnimation()
        requireBinding().animationHideError = hideErrorAnimation()
        requireBinding().continueClickListener = continueClickListener()

        requireBinding().initializationCreatePasswordPassword.editText?.showSoftKeyboad()
    }

    override fun navigateToInitializationEmail(firstAccess: Boolean) {
        if (findNavController().currentDestination?.id != R.id.initializationCreatePasswordFragment) {
            return
        }

        val directions =
            InitializationCreatePasswordFragmentDirections.actionInitializationCreatePasswordFragmentToInitializationEmailFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToHome() {
        if (findNavController().currentDestination?.id != R.id.initializationCreatePasswordFragment) {
            return
        }

        val directions = InitializationCreatePasswordFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    private fun hideErrorAnimation() = Runnable {
        requireBinding().initializationCreatePasswordError.slideOutTop(0)
    }

    private fun showErrorAnimation() = Runnable {
        requireBinding().initializationCreatePasswordError.slideInTop(
            resources.getDimensionPixelOffset(
                R.dimen.error_floating_label_margin_top
            )
        )
    }

    private fun continueClickListener() = View.OnClickListener {
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