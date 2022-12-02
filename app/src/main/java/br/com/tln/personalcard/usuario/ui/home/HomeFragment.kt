package br.com.tln.personalcard.usuario.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentHomeBinding
import br.com.tln.personalcard.usuario.databinding.HomeNavViewHeaderBinding
import br.com.tln.personalcard.usuario.databinding.ItemHomeCardStatementBinding
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.extensions.hideSoftKeyboard
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.Transaction
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import javax.inject.Inject

class HomeFragment : SessionRequiredBaseFragment<FragmentHomeBinding, HomeNavigator, HomeViewModel>(
    layoutRes = R.layout.fragment_home,
    viewModelClass = HomeViewModel::class.java,
    retainViewInstance = true
), HasToolbar, HomeNavigator, HomeCardFragment.OnCardClickListener {

    private lateinit var navHeaderBinding: HomeNavViewHeaderBinding

    private lateinit var cards: List<Card>

    private var initialPosition: Float? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCards()

        hideSoftKeyboard()

        requireBinding().navView.itemIconTintList = null

        if (!::navHeaderBinding.isInitialized) {
            navHeaderBinding =
                HomeNavViewHeaderBinding.bind(requireBinding().navView.getHeaderView(0))
            navHeaderBinding.lifecycleOwner = viewLifecycleOwner
            navHeaderBinding.viewModel = viewModel
        }

        requireBinding().navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_nav_edit_profile -> {
                    navigateToEditProfile()
                }
                R.id.home_nav_change_password -> {
                    navigateToChangePassword()
                }
                R.id.home_nav_rate_app -> {
                    navigateToRateApp()
                }
                R.id.home_nav_support -> {
                    openSupport()
                }
                R.id.home_nav_terms_of_use -> {
                    openTermsOfUse()
                }
                R.id.home_nav_privacy_policy -> {
                    openPrivacyPolicy()
                }
                R.id.home_nav_exit -> {
                    viewModel.lockSession()
                }
                else -> return@setNavigationItemSelectedListener false
            }

            requireBinding().drawerLayout.closeDrawer(requireBinding().navView, true)
            true
        }

        configureViewPager()

        val adapter =
            StatementAdapter(StatementAdapter.OnClickListener { nsuAuthorization: String, holder: StatementAdapter.ViewHolder ->
                Toast.makeText(
                    activity,
                    getString(
                        R.string.toast_home_card_statement_extract_authorization,
                        nsuAuthorization
                    ),
                    Toast.LENGTH_LONG
                )
                    .show()
            })

        requireBinding().recycler.adapter = adapter

        viewModel.pagedListLiveData.observe(viewLifecycleOwner) { pagedList ->
            pagedList.observe(this) { transition ->
                adapter.submitList(transition)
            }
        }

        requireBinding().swipeRefresh.setOnRefreshListener {

            viewModel.loadCardStatamentSwipeRefresh()

            adapter.notifyDataSetChanged()

            requireBinding().swipeRefresh.isRefreshing = false
        }
    }

    fun navigateToRateApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BuildConfig.LINK_PLAY_STORE)
        startActivity(intent)
    }

    fun openTermsOfUse() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.TERMS_OF_USE_LINK))
    }

    fun openSupport() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.SUPPORT_LINK))
    }

    fun openPrivacyPolicy() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.PRIVACY_POLICY))
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentCard()?.apply {
            requireBinding().toolbar.title = label
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is HomeCardFragment) {
            childFragment.onCardClickListener = this
        }
    }

    override fun setupToolbar() {
        requireBinding().navView.setupWithNavController(findNavController())
        requireBinding().toolbar.setupWithNavController(
            findNavController(),
            requireBinding().drawerLayout
        )
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions =
            HomeFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionGlobalInitializationPasswordFragment(
            cpf = cpf,
            registerEmail = registerEmail
        )
        findNavController().navigate(directions)
    }

    override fun navigateToEditProfile() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionHomeFragmentToEditProfileFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToChangePassword() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions = HomeFragmentDirections.actionHomeFragmentToChangePasswordFragment()
        findNavController().navigate(directions)
    }

    @Inject
    lateinit var res: ResourceProvider

    override fun navigateToAccreditedNetwork(label: String, card: Card) {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val accreditedNetwork: AccreditedNetwork? = null

        val directions = HomeFragmentDirections.actionHomeFragmentToAccreditedNetworkMapFragment(
            label = label,
            card = card,
            accreditedNetwork = accreditedNetwork
        )
        findNavController().navigate(directions)
    }

    override fun navigateToPayWithQrCode(card: Card) {
        if (findNavController().currentDestination?.id != R.id.homeFragment) {
            return
        }

        val directions =
            HomeFragmentDirections.actionHomeFragmentToPayWithQrCodeReaderFragment(card = card)
        findNavController().navigate(directions)
    }

    override fun onCardClicked(cardIndex: Int) {
        if (cardIndex < 0) {
            return
        }

        requireBinding().pager.setCurrentItem(cardIndex, true)
    }

    private fun configureViewPager() {
        requireBinding().pager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onCardSelected(cards[position])
            }
        })

        requireBinding().pager.setPageTransformer(false, object : ViewPager.PageTransformer {

            private var selectedPosition: Float? = null
            private var positionsDifference: Float = 1.0f
            private var minPositionsDifferenceForAnimation: Float = 1.0f

            private val selectedScaleX = 1f
            private val selectedScaleY = 1f
            private val unselectedScaleX = 0.95f
            private val unselectedScaleY = 0.8f
            private val scaleDifferenceX = selectedScaleX - unselectedScaleX
            private val scaleDifferenceY = selectedScaleY - unselectedScaleY

            override fun transformPage(page: View, position: Float) {
                val selectedPosition = this.selectedPosition
                val positionsDifference = this.positionsDifference
                val minPositionsDifferenceForAnimation = this.minPositionsDifferenceForAnimation

                if (initialPosition == null) {
                    initialPosition = position
                }

                if (selectedPosition == null) {

                    this.selectedPosition = if (position < 0) initialPosition else position

                    page.scaleX = selectedScaleX
                    page.scaleY = selectedScaleY
                    return
                }

                val difference = calculateDifference(selectedPosition, position)

                var scaleX = unselectedScaleX
                var scaleY = unselectedScaleY

                if (position == selectedPosition) {
                    scaleX = selectedScaleX
                    scaleY = selectedScaleY
                } else if (difference < minPositionsDifferenceForAnimation) {
                    val percent = difference / positionsDifference

                    scaleX = selectedScaleX - (scaleDifferenceX * percent)
                    scaleY = selectedScaleY - (scaleDifferenceY * percent)
                }

                page.scaleX = scaleX
                page.scaleY = scaleY
            }

            fun calculateDifference(val1: Float, val2: Float): Float {
                return if (val1 < val2) {
                    val2.toBigDecimal().minus(val1.toBigDecimal()).toFloat()
                } else {
                    val1.toBigDecimal().minus(val2.toBigDecimal()).toFloat()
                }
            }
        })
    }

    private fun getCards() {
        viewModel.getCardsLiveData()
            .observe(viewLifecycleOwner) { resource ->
                val x = when (resource) {
                    is LoadingResource -> {
                    }
                    is SuccessResource -> {
                        this.cards = resource.data
                        onCardSelected(cards.first())
                        showCards()
                    }
                    is ErrorResource -> {
                    }
                }
            }
    }

    private fun getCardStatement(card: Card) {
        viewModel.loadCardStatement(card = card)
    }

    private fun onCardSelected(card: Card) {
        requireBinding().toolbar.title = card.label
        getCardStatement(card)
    }

    private fun showCards() {
        requireBinding().pager.adapter = CardAdapter(
            fragmentManager = childFragmentManager,
            cards = cards
        )
    }
}

class StatementAdapter(private val onClickListener: OnClickListener) :
    PagedListAdapter<Transaction, StatementAdapter.ViewHolder>(DIFF_CONFIG) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeCardStatementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val nsuAuthorization = it.nsuAuthorization
            holder.itemView.setOnClickListener {
                onClickListener.onClick(nsuAuthorization, holder)
            }
            holder.configure(it)
        }
    }

    class OnClickListener(val clickListener: (nsuAuthorization: String, holder: ViewHolder) -> Unit) {
        fun onClick(nsuAuthorization: String, holder: ViewHolder) =
            clickListener(nsuAuthorization, holder)
    }

    inner class ViewHolder(val binding: ItemHomeCardStatementBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun configure(transaction: Transaction) {
            binding.transaction = transaction

            binding.divider.visibility = if (adapterPosition == itemCount - 1) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    companion object {
        val DIFF_CONFIG = AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<Transaction>() {
                override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
                    return oldItem.nsuAuthorization == newItem.nsuAuthorization && oldItem.nsuHost == newItem.nsuHost
                }

                override fun areContentsTheSame(
                    oldItem: Transaction,
                    newItem: Transaction
                ): Boolean {
                    return oldItem == newItem
                }
            }
        ).build()
    }
}
