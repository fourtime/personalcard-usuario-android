package br.com.tln.personalcard.usuario.ui.initialization.cpf

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationCpfBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.widget.ErrorToast

class InitializationCpfFragment :
    BaseFragment<FragmentInitializationCpfBinding, InitializationCpfNavigator, InitializationCpfViewModel>(
        R.layout.fragment_initialization_cpf,
        InitializationCpfViewModel::class.java
    ), InitializationCpfNavigator {

    private var errorToast: ErrorToast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cpf.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
    }

    override fun navigateToInitializationCardLastDigits(cpf: String) {
        if (findNavController().currentDestination?.id != R.id.initializationCpfFragment) {
            return
        }

        val directions =
            InitializationCpfFragmentDirections.actionInitializationCpfFragmentToInitializationCardLastDigitsFragment(
                cpf = cpf
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
}