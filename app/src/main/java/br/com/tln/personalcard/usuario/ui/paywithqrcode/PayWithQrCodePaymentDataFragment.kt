package br.com.tln.personalcard.usuario.ui.paywithqrcode

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentPayWithQrCodePaymentDataBinding
import br.com.tln.personalcard.usuario.entity.CardType
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog

class PayWithQrCodePaymentDataFragment :
    SessionRequiredBaseFragment<FragmentPayWithQrCodePaymentDataBinding, PayWithQrCodePaymentDataNavigator, PayWithQrCodePaymentDataViewModel>(
        layoutRes = R.layout.fragment_pay_with_qr_code_payment_data,
        viewModelClass = PayWithQrCodePaymentDataViewModel::class.java
    ), HasToolbar, PayWithQrCodePaymentDataNavigator {

    val args: PayWithQrCodePaymentDataFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setCard(args.card)
        viewModel.setPaymentData(paymentDataCondition(args.paymentData))
    }

    private fun paymentDataCondition(paymentData: PaymentData): PaymentData {
        return if (args.card.type == CardType.PRE_PAID && !paymentData.condition.equals(
                getString(R.string.pay_with_qr_code_payment_data_chash_payment),
                ignoreCase = true
            )
        ) {
            cashPaymentAdvice()
            paymentData.copy(condition = getString(R.string.pay_with_qr_code_payment_data_1x))
        } else if (paymentData.condition.equals(
                getString(R.string.pay_with_qr_code_payment_data_chash_payment),
                ignoreCase = true
            )
        ) {
            paymentData.copy(condition = getString(R.string.pay_with_qr_code_payment_data_1x))
        } else {
            paymentData
        }
    }

    private fun cashPaymentAdvice() {
        BottomSheetDialog(requireContext())
            .show {
                cancelable(false)
                message(getString(R.string.pay_with_qr_code_payment_data_chash_payment_dialog_message))
                confirmText(getString(R.string.pay_with_qr_code_payment_data_chash_payment_dialog_confirm_button)) {
                    dismiss()
                }
                neutralText(getString(R.string.pay_with_qr_code_payment_data_chash_payment_dialog_neutral_button)) {
                    dismiss()
                    navigateToHome()
                }
            }
    }

    private fun navigateToHome() {
        val directions = PayWithQrCodePaymentDataFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions = PayWithQrCodePaymentDataFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun navigateToCardPassword() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodePaymentDataFragment) {
            return
        }

        val directions =
            PayWithQrCodePaymentDataFragmentDirections.actionPayWithQrCodePaymentDataFragmentToPayWithQrCodeCardPasswordFragment(
                card = args.card,
                paymentData = args.paymentData
            )
        findNavController().navigate(directions)
    }
}