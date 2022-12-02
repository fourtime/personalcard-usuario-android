package br.com.tln.personalcard.usuario.ui.changepassword

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.*
import br.com.tln.personalcard.usuario.databinding.FragmentChangePasswordBinding
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog
import br.com.tln.personalcard.usuario.widget.ErrorToast

class ChangePasswordFragment :
    BaseFragment<FragmentChangePasswordBinding, ChangePasswordNavigator, ChangePasswordViewModel>(
        layoutRes = R.layout.fragment_change_password,
        viewModelClass = ChangePasswordViewModel::class.java
    ), ChangePasswordNavigator, HasToolbar {

    private var errorToast: ErrorToast? = null

    private var loadingDialog: ProgressDialog? = null

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().saveClickListener = saveClickListener()
    }

    override fun navigateToHome() {
        if (findNavController().currentDestination?.id != R.id.changePasswordFragment) {
            return
        }

        val directions = ChangePasswordFragmentDirections.actionGlobalHomeFragment()
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

    private fun saveClickListener() = View.OnClickListener {
        viewModel.saveClicked()?.observeResource(viewLifecycleOwner) { resource ->
            val x = when (resource) {
                is LoadingResource -> {
                    showLoading()
                }
                is SuccessResource -> {
                    hideLoading()
                    successDialog()
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

    fun successDialog() {
        BottomSheetDialog(requireContext())
            .show {
                icon(R.drawable.change_password_success)
                message(getString(R.string.change_password_dialog_message_confirm_success))
                confirmText(getString(R.string.change_password_dialog_button_confirm_success)) {
                    dismiss()
                }
            }
    }

}