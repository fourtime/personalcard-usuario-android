package br.com.tln.personalcard.usuario.ui.initialization.forgotpassword

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentRecoveryPasswordSuccessBinding

class RecoveryPasswordSuccessFragment :
    BaseFragment<FragmentRecoveryPasswordSuccessBinding, RecoveryPasswordSuccessNavigator, RecoveryPasswordSuccessViewModel>(
        R.layout.fragment_recovery_password_success,
        RecoveryPasswordSuccessViewModel::class.java
    ), RecoveryPasswordSuccessNavigator {

    private val args: RecoveryPasswordSuccessFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().loginClickListener = goToLogin()
    }

    override fun navigateItializationPassword() {
        if (findNavController().currentDestination?.id != R.id.recoveryPasswordSuccessFragment) {
            return
        }

        val directions =
            RecoveryPasswordSuccessFragmentDirections.actionRecoveryPasswordSuccessFragmentToInitializationPasswordFragment(
                cpf = args.cpf,
                registerEmail = args.firstAccess
            )
        findNavController().navigate(directions)
    }

    private fun goToLogin() = View.OnClickListener {
        navigateItializationPassword()
    }
}