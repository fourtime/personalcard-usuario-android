package br.com.tln.personalcard.usuario.ui.initialization.cardlastdigits

import android.app.ProgressDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentInitializationCardLastDigitsBinding
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.extensions.showSoftKeyboad
import br.com.tln.personalcard.usuario.extensions.slideInTop
import br.com.tln.personalcard.usuario.extensions.slideOutTop

class InitializationCardLastDigitsFragment :
    BaseFragment<FragmentInitializationCardLastDigitsBinding, InitializationCardLastDigitsNavigator, InitializationCardLastDigitsViewModel>(
        R.layout.fragment_initialization_card_last_digits,
        InitializationCardLastDigitsViewModel::class.java
    ), HasToolbar, InitializationCardLastDigitsNavigator {

    private val args: InitializationCardLastDigitsFragmentArgs by navArgs()

    private var loadingDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cpf = args.cpf
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().animationShowError = showErrorAnimation()
        requireBinding().animationHideError = hideErrorAnimation()
        requireBinding().continueClickListener = continueClickListener()

        requireBinding().initializationCardLastDigitsDigits.showSoftKeyboad()

        requireBinding().initializationCardLastDigitsDigit1.isSelected = true

        setupDigitsListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())

        requireBinding().toolbar.navigationIcon?.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.telenetColorExtra3
            ), PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun navigateToInitializationCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.initializationCardLastDigitsFragment) {
            return
        }

        val directions =
            InitializationCardLastDigitsFragmentDirections.actionInitializationCardLastDigitsFragmentToInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.initializationCardLastDigitsFragment) {
            return
        }

        val directions =
            InitializationCardLastDigitsFragmentDirections.actionInitializationCardLastDigitsFragmentToInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    private fun setupDigitsListeners() {
        val digits = listOf(
            requireBinding().initializationCardLastDigitsDigit1,
            requireBinding().initializationCardLastDigitsDigit2,
            requireBinding().initializationCardLastDigitsDigit3,
            requireBinding().initializationCardLastDigitsDigit4
        )

        requireBinding().initializationCardLastDigitsOverlay.setOnClickListener {
            requireBinding().initializationCardLastDigitsDigits.showSoftKeyboad()
        }

        requireBinding().initializationCardLastDigitsDigits.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""

                if (text.isNotEmpty()) {
                    requireBinding().initializationCardLastDigitsDigit1.text = text.substring(0, 1)
                } else {
                    requireBinding().initializationCardLastDigitsDigit1.text = ""
                }

                if (text.length >= 2) {
                    requireBinding().initializationCardLastDigitsDigit2.text = text.substring(1, 2)
                } else {
                    requireBinding().initializationCardLastDigitsDigit2.text = ""
                }

                if (text.length >= 3) {
                    requireBinding().initializationCardLastDigitsDigit3.text = text.substring(2, 3)
                } else {
                    requireBinding().initializationCardLastDigitsDigit3.text = ""
                }

                if (text.length >= 4) {
                    requireBinding().initializationCardLastDigitsDigit4.text = text.substring(3, 4)
                } else {
                    requireBinding().initializationCardLastDigitsDigit4.text = ""
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

    private fun hideErrorAnimation() = Runnable {
        requireBinding().initializationCardLastDigitsError.slideOutTop(0)
    }

    private fun showErrorAnimation() = Runnable {
        requireBinding().initializationCardLastDigitsError.slideInTop(
            resources.getDimensionPixelOffset(
                R.dimen.error_floating_label_margin_top
            )
        )
    }

    private fun continueClickListener() = View.OnClickListener {
        viewModel.continueClicked()?.observeResource(viewLifecycleOwner) {
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