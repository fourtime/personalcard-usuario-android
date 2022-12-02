package br.com.tln.personalcard.usuario.ui.prelogin

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentPreLoginBinding
import br.com.tln.personalcard.usuario.extensions.hideSoftKeyboard

class PreLoginFragment :
    BaseFragment<FragmentPreLoginBinding, PreLoginNavigator, PreLoginViewModel>(
        R.layout.fragment_pre_login,
        PreLoginViewModel::class.java
    ), PreLoginNavigator {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideSoftKeyboard()
    }

    override fun navigateToInitializationCpf() {
        if (findNavController().currentDestination?.id != R.id.preLoginFragment) {
            return
        }

        val directions =
            PreLoginFragmentDirections.actionPreLoginFragmentToInitializationCpfFragment()
        findNavController().navigate(directions)
    }
}