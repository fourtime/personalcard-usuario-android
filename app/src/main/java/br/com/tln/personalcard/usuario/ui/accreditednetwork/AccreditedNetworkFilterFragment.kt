package br.com.tln.personalcard.usuario.ui.accreditednetwork

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentAccreditedNetworkFilterBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.model.AccreditedNetworkFilter
import br.com.tln.personalcard.usuario.model.ActivityBranch
import br.com.tln.personalcard.usuario.model.City
import br.com.tln.personalcard.usuario.model.Neighborhood
import br.com.tln.personalcard.usuario.model.Segment
import br.com.tln.personalcard.usuario.model.Specialty
import br.com.tln.personalcard.usuario.model.UF
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems

class AccreditedNetworkFilterFragment :
    SessionRequiredBaseFragment<FragmentAccreditedNetworkFilterBinding, AccreditedNetworkFilterNavigator, AccreditedNetworkFilterViewModel>(
        layoutRes = R.layout.fragment_accredited_network_filter,
        viewModelClass = AccreditedNetworkFilterViewModel::class.java
    ), HasToolbar, AccreditedNetworkFilterNavigator {

    private var loadingDialog: ProgressDialog? = null

    private val args: AccreditedNetworkFilterFragmentArgs by navArgs()

    private var waitingForSegments = false
    private var waitingForActivityBranches = false
    private var waitingForSpecialties = false
    private var waitingForUFs = false
    private var waitingCities = false
    private var waitingForNeighborhood = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setCard(args.card)
        observeData()
    }

    private fun observeData() {
        viewModel.segmentsLiveData.observe(viewLifecycleOwner) { segments ->
            if (segments != null && waitingForSegments) {
                hideLoading()
                segmentListDialog(segments)
                waitingForSegments = false
            }

            requireBinding().onClickSegmentSelector = View.OnClickListener {
                if (segments == null) {
                    showLoading()
                    viewModel.loadSegmentIfFailed()
                    waitingForSegments = true
                } else {
                    segmentListDialog(segments)
                }
            }
        }

        viewModel.activityBranchLivedata.observe(viewLifecycleOwner) { activityBranches ->
            if (activityBranches != null && waitingForActivityBranches) {
                hideLoading()
                activityBranchListDialog(activityBranches)
                waitingForActivityBranches = false
            }


            requireBinding().onClickActivityBranchSelector = View.OnClickListener {
                if (activityBranches == null) {
                    showLoading()
                    viewModel.loadActivityBranchIfFailed()
                    waitingForActivityBranches = true
                } else {
                    activityBranchListDialog(activityBranches)
                }
            }
        }

        viewModel.specialtyLivedata.observe(viewLifecycleOwner) { specialties ->
            if (specialties != null && waitingForSpecialties) {
                hideLoading()
                specialtyListDialog(specialties)
                waitingForSpecialties = false
            }

            requireBinding().onClickActivitySpecialty = View.OnClickListener {
                if (specialties == null) {
                    showLoading()
                    viewModel.loadSpecialtyIfFailed()
                    waitingForSpecialties = true
                } else {
                    specialtyListDialog(specialties)
                }
            }
        }

        viewModel.ufLiveData.observe(viewLifecycleOwner) { ufs ->
            if (ufs != null && waitingForUFs) {
                hideLoading()
                ufDialog(ufs)
                waitingForUFs = false
            }

            requireBinding().onClickUFSelector = View.OnClickListener {
                if (ufs == null) {
                    showLoading()
                    viewModel.loadUFIfFailed()
                    waitingForUFs = true
                } else {
                    ufDialog(ufs)
                }
            }
        }

        viewModel.cityLiveData.observe(viewLifecycleOwner) { cities ->
            if (cities != null && waitingCities) {
                hideLoading()
                cityDialog(cities)
                waitingCities = false
            }

            requireBinding().onClickCitySelector = View.OnClickListener {
                if (cities == null) {
                    showLoading()
                    viewModel.loadCityIfFailed()
                    waitingCities = true
                } else {
                    cityDialog(cities)
                }
            }
        }

        viewModel.neighborhoodLiveData.observe(viewLifecycleOwner) { neighborhoods ->
            if (neighborhoods != null && waitingForNeighborhood) {
                hideLoading()
                neighborhoodDialog(neighborhoods)
                waitingForNeighborhood = false
            }

            requireBinding().onClickNeighborhoodSelector = View.OnClickListener {
                if (neighborhoods == null) {
                    showLoading()
                    viewModel.loadNeighborhoodIfFailed()
                    waitingForNeighborhood = true
                } else {
                    neighborhoodDialog(neighborhoods)
                }
            }
        }
    }

    private fun segmentListDialog(segments: List<Segment>) {
        val listOptions = segments.map {
            it.name
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.segmentSelected(segments[index])
            }
        }

    }

    private fun activityBranchListDialog(activityBranches: List<ActivityBranch>) {
        val listOptions = activityBranches.map {
            it.activityBranch
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.activityBranchSelected(listOptions[index])
            }
        }

    }

    private fun specialtyListDialog(specialties: List<Specialty>) {
        val listOptions = specialties.map {
            it.specialty
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.specialtySelected(listOptions[index])
            }
        }

    }

    private fun ufDialog(ufs: List<UF>) {
        val listOptions = ufs.map {
            it.uf
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.ufSelected(listOptions[index])
            }
        }

    }

    private fun cityDialog(cities: List<City>) {
        val listOptions = cities.map {
            it.city
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.citySelected(listOptions[index])
            }
        }

    }

    private fun neighborhoodDialog(neighborhoods: List<Neighborhood>) {
        val listOptions = neighborhoods.map {
            it.neighborhood
        }

        MaterialDialog(requireActivity()).show {
            title(R.string.accredited_network_filter_selector_title)
            listItems(items = listOptions) { dialog, index, text ->
                viewModel.neighborhoodSelected(listOptions[index])
            }
        }

    }

    private fun showLoading() {
        val loadingDialog = this.loadingDialog ?: ProgressDialog(context).also {
            it.setCancelable(true)
            it.setTitle(R.string.app_name)
            it.setMessage(getString(R.string.dialog_loading))
            it.show()
            it.setOnDismissListener {
                waitingForSegments = false
                waitingForActivityBranches = false
                waitingForSpecialties = false
                waitingForUFs = false
                waitingCities = false
                waitingForNeighborhood = false
            }
        }

        this.loadingDialog = loadingDialog

        loadingDialog.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss().also { loadingDialog = null }
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToFilterResults(accreditedNetworkFilter: AccreditedNetworkFilter) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionAccreditedNetworkFilterFragmentToAccreditedNetworkFilterResultsFragment(
                accreditedNetworkFilter = accreditedNetworkFilter,
                card = args.card
            )
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkFilterFragment) {
            return
        }

        val directions =
            AccreditedNetworkFilterFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

}