package br.com.tln.personalcard.usuario.ui.accreditednetwork

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
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.model.AccreditedNetwork

class AccreditedNetworkFilterResultsFragment :
    SessionRequiredBaseFragment<br.com.tln.personalcard.usuario.databinding.FragmentAccreditedNetworkFilterResultsBinding, AccreditedNetworkFilterResultsNavigator, AccreditedNetworkFilterResultsViewModel>(
        layoutRes = R.layout.fragment_accredited_network_filter_results,
        viewModelClass = AccreditedNetworkFilterResultsViewModel::class.java
    ), HasToolbar, AccreditedNetworkFilterResultsNavigator {

    private val args: AccreditedNetworkFilterResultsFragmentArgs by navArgs()
    private var loadingDialog: ProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadAccreditedNetwork(accreditedNetworkFilter = args.accreditedNetworkFilter)

        observeRequestState()

        val adapter = AccreditedNetworkResultsAdapter { accreditedNetwork ->
            viewModel.accreditedNetworkSelected(accreditedNetwork)
        }

        requireBinding().recycler.adapter = adapter

        viewModel.pagedListLiveData.observe(viewLifecycleOwner) { pagedList ->
            pagedList.observe(this) { accreditedNetworkFilter ->
                adapter.submitList(accreditedNetworkFilter)
            }
        }

    }

    private fun observeRequestState() {
        viewModel.accreditedNetworkLiveData.observe(viewLifecycleOwner) {
            val x = when (it) {
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
            it.setCancelable(true)
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
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToAccreditedNetworkMap(accreditedNetwork: AccreditedNetwork) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterResultsFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterResultsFragmentDirections.actionAccreditedNetworkFilterResultsFragmentToAccreditedNetworkMapFragment(
                accreditedNetwork = accreditedNetwork,
                card = args.card,
                label = getString(R.string.toolbar_title_accredited_network, args.card.label)
            )
        findNavController().navigate(directions)
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

}



