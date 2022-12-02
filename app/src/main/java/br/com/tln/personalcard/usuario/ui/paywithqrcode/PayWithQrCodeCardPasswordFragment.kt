package br.com.tln.personalcard.usuario.ui.paywithqrcode

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentPayWithQrCodeCardPasswordBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.model.ConfirmPayment
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog

class PayWithQrCodeCardPasswordFragment :
    SessionRequiredBaseFragment<FragmentPayWithQrCodeCardPasswordBinding, PayWithQrCodeCardPasswordNavigator, PayWithQrCodeCardPasswordViewModel>(
        layoutRes = R.layout.fragment_pay_with_qr_code_card_password,
        viewModelClass = PayWithQrCodeCardPasswordViewModel::class.java
    ), HasToolbar, PayWithQrCodeCardPasswordNavigator {

    private var loadingDialog: ProgressDialog? = null

    private val args: PayWithQrCodeCardPasswordFragmentArgs by navArgs()

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().payClickListener = onPayClick()
        requireBinding().payWithQrCodeCardPasswordDigit1.isSelected = true

        requireBinding().payWithQrCodeCardPassword.showSoftKeyboad()

        setupDigitsListeners()
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun navigateToSuccess(confirmPayment: ConfirmPayment) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionPayWithQrCodeCardPasswordFragmentToPayWithQrCodeSuccessFragment(
                card = args.card,
                paymentData = args.paymentData,
                confirmPayment = confirmPayment
            )
        findNavController().navigate(directions)
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions = PayWithQrCodeCardPasswordFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeCardPasswordFragment) {
            return
        }

        val directions =
            PayWithQrCodeCardPasswordFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun showErrorMessage(message: String) {
        BottomSheetDialog(requireContext())
            .show {
                cancelable(false)
                showCloseButton(true) {
                    dismiss()
                    navigateBackToHome()
                }
                message(message)
                confirmText(getString(R.string.pay_with_qr_code_ivalid_password_dialog_confirm_text)) {
                    dismiss()
                    navigateBackToHome()
                }
            }
    }

    override fun showUnkownError() {
        BottomSheetDialog(requireContext())
            .show {
                cancelable(false)
                showCloseButton(true) {
                    dismiss()
                    navigateBackToHome()
                }
                title(getString(R.string.pay_with_qr_code_unknow_error_dialog_title))
                message(getString(R.string.pay_with_qr_code_unknow_error_dialog_message))
                confirmText(getString(R.string.pay_with_qr_code_unknow_error_dialog_confirm_text)) {
                    dismiss()
                    navigateBackToHome()
                }
            }
    }

    private fun navigateBackToHome() {
        val directions = PayWithQrCodeCardPasswordFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    private fun onPayClick() = View.OnClickListener {
        viewModel.payClicked(card = args.card, paymentData = args.paymentData)
            ?.observe(viewLifecycleOwner) {
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

    private fun setupDigitsListeners() {
        val digits = listOf(
            requireBinding().payWithQrCodeCardPasswordDigit1,
            requireBinding().payWithQrCodeCardPasswordDigit2,
            requireBinding().payWithQrCodeCardPasswordDigit3,
            requireBinding().payWithQrCodeCardPasswordDigit4
        )

        requireBinding().payWithQrCodeCardPasswordOverlay.setOnClickListener {
            requireBinding().payWithQrCodeCardPassword.showSoftKeyboad()
        }

        requireBinding().payWithQrCodeCardPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""

                if (text.isNotEmpty()) {
                    requireBinding().payWithQrCodeCardPasswordDigit1.text =
                        getString(R.string.hide_password_character)
                } else {
                    requireBinding().payWithQrCodeCardPasswordDigit1.text = ""
                }

                if (text.length >= 2) {
                    requireBinding().payWithQrCodeCardPasswordDigit2.text =
                        getString(R.string.hide_password_character)
                } else {
                    requireBinding().payWithQrCodeCardPasswordDigit2.text = ""
                }

                if (text.length >= 3) {
                    requireBinding().payWithQrCodeCardPasswordDigit3.text =
                        getString(R.string.hide_password_character)
                } else {
                    requireBinding().payWithQrCodeCardPasswordDigit3.text = ""
                }

                if (text.length >= 4) {
                    requireBinding().payWithQrCodeCardPasswordDigit4.text =
                        getString(R.string.hide_password_character)
                } else {
                    requireBinding().payWithQrCodeCardPasswordDigit4.text = ""
                }

                digits.forEach {
                    it.isSelected = false
                }

                if (text.length != digits.size) {
                    digits[text.length].isSelected = true
                } else {
                    digits.last().isSelected = true
                }
            }
        })
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