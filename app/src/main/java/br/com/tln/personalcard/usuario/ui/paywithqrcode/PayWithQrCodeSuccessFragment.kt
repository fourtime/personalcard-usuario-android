package br.com.tln.personalcard.usuario.ui.paywithqrcode

import android.app.ProgressDialog
import android.os.Bundle
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
import br.com.tln.personalcard.usuario.databinding.FragmentPayWithQrCodeSuccessBinding
import br.com.tln.personalcard.usuario.extensions.observe

class PayWithQrCodeSuccessFragment :
    SessionRequiredBaseFragment<FragmentPayWithQrCodeSuccessBinding, PayWithQrCodeSuccessNavigator, PayWithQrCodeSuccessViewModel>(
        layoutRes = R.layout.fragment_pay_with_qr_code_success,
        viewModelClass = PayWithQrCodeSuccessViewModel::class.java
    ), HasToolbar, PayWithQrCodeSuccessNavigator {

    private var loadingDialog: ProgressDialog? = null

    private val args: PayWithQrCodeSuccessFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setPaymentData(args.paymentData, args.confirmPayment)

        requireBinding().updateCardBalanceListener = onUpdateCardBalanceClick()
    }

    private fun onUpdateCardBalanceClick() = View.OnClickListener {
        viewModel.updateCardBalance()
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

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions = PayWithQrCodeSuccessFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions =
            PayWithQrCodeSuccessFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions =
            PayWithQrCodeSuccessFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions =
            PayWithQrCodeSuccessFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions =
            PayWithQrCodeSuccessFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToHome() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions = PayWithQrCodeSuccessFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeSuccessFragment) {
            return
        }

        val directions =
            PayWithQrCodeSuccessFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }
}