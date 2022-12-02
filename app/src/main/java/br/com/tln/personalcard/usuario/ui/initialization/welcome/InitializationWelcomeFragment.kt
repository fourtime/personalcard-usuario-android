package br.com.tln.personalcard.usuario.ui.initialization.welcome

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
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationWelcomeBinding
import br.com.tln.personalcard.usuario.extensions.hideSoftKeyboard
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.extensions.slideInTop
import br.com.tln.personalcard.usuario.extensions.slideOutTop

class InitializationWelcomeFragment :
    BaseFragment<FragmentInitializationWelcomeBinding, InitializationWelcomeNavigator, InitializationWelcomeViewModel>(
        R.layout.fragment_initialization_welcome,
        InitializationWelcomeViewModel::class.java
    ), InitializationWelcomeNavigator {

    private val args: InitializationWelcomeFragmentArgs by navArgs()

    private var loadingDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.firstAccess = args.firstAccess
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().animationShowError = showErrorAnimation()
        requireBinding().animationHideError = hideErrorAnimation()
        requireBinding().concludeClickListener = concludeClickListener()

        if (args.firstAccess) {
            hideSoftKeyboard()
        } else {
            requireBinding().initializationWelcomeEmail.editText?.showSoftKeyboad()
        }
    }

    override fun navigateToHome() {
        if (findNavController().currentDestination?.id != R.id.initializationWelcomeFragment) {
            return
        }

        val directions = InitializationWelcomeFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    private fun hideErrorAnimation() = Runnable {
        requireBinding().initializationWelcomeError.slideOutTop(0)
    }

    private fun showErrorAnimation() = Runnable {
        requireBinding().initializationWelcomeError.slideInTop(resources.getDimensionPixelOffset(R.dimen.error_floating_label_margin_top))
    }

    private fun concludeClickListener() = View.OnClickListener {
        viewModel.concludeClicked()?.observeResource(viewLifecycleOwner) { resource ->
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