package br.com.tln.personalcard.usuario.ui.paywithqrcode

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.RequestCode
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentPayWithQrCodeReaderBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.observeEvent
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class PayWithQrCodeReaderFragment :
    SessionRequiredBaseFragment<FragmentPayWithQrCodeReaderBinding, PayWithQrCodeReaderNavigator, PayWithQrCodeReaderViewModel>(
        layoutRes = R.layout.fragment_pay_with_qr_code_reader,
        viewModelClass = PayWithQrCodeReaderViewModel::class.java
    ), HasToolbar, PayWithQrCodeReaderNavigator, ZXingScannerView.ResultHandler {

    private val args: PayWithQrCodeReaderFragmentArgs by navArgs()

    private var loadingDialog: ProgressDialog? = null
    private var neverAskAgainSnackbar: Snackbar? = null

    private var cameraStarted = false

    override fun onDestroy() {
        super.onDestroy()
        neverAskAgainSnackbar = neverAskAgainSnackbar?.let {
            it.dismiss()
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireBinding().reader.setFormats(listOf(BarcodeFormat.QR_CODE))
        requireBinding().reader.setResultHandler(this)
        startCameraWithPermissionCheck()
    }

    override fun onResume() {
        super.onResume()

        val cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }

        viewModel.invalidQrCodeLiveData.observeEvent(this) {
            BottomSheetDialog(requireContext())
                .show {
                    title(getString(R.string.pay_with_qr_code_unknow_error_dialog_title))
                    message(getString(R.string.dialog_invalid_qr_code_message))
                    confirmText(getString(R.string.dialog_generic_error_button_text))
                    setOnDismissListener {
                        requireBinding().reader.resumeCameraPreview(this@PayWithQrCodeReaderFragment)
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.PAY_WITH_QR_CODE_READER_ENABLE_CAMERA_PERMISSION) {
            val cameraPermission =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                navigateBack()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions = PayWithQrCodeReaderFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun navigateToQrPaymentData(paymentData: PaymentData, paymentToken: String) {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        val directions =
            PayWithQrCodeReaderFragmentDirections.actionPayWithQrCodeReaderFragmentToPayWithQrCodePaymentDataFragment(
                card = args.card,
                paymentData = paymentData
            )
        findNavController().navigate(directions)
    }

    override fun connectionErrorDialogDismissListener() = DialogInterface.OnDismissListener {
        requireBinding().reader.resumeCameraPreview(this)
    }

    override fun unknownErrorDialogDismissListener() = DialogInterface.OnDismissListener {
        requireBinding().reader.resumeCameraPreview(this)
    }

    override fun handleResult(rawResult: Result) {
        getPaymentData(rawResult.text)
    }

    private fun navigateBack() {
        if (findNavController().currentDestination?.id != R.id.payWithQrCodeReaderFragment) {
            return
        }

        if (!findNavController().popBackStack() && !findNavController().navigateUp()) {
            val directions = PayWithQrCodeReaderFragmentDirections.actionGlobalHomeFragment()
            findNavController().navigate(directions)
        }
    }

    private fun stopCamera() {
        if (cameraStarted) {
            cameraStarted = false
            requireBinding().reader.stopCamera()
        }
    }

    @NeedsPermission(value = [Manifest.permission.CAMERA])
    fun startCamera() {
        if (!cameraStarted) {
            cameraStarted = true
            requireBinding().reader.startCamera()
        }
    }

    @OnPermissionDenied(value = [Manifest.permission.CAMERA])
    fun startCameraPermissionDenied() {
        navigateBack()
    }

    @OnShowRationale(value = [Manifest.permission.CAMERA])
    fun startCameraShowRationale() {
        BottomSheetDialog(requireContext())
            .show {
                cancelable(true) {
                    navigateBack()
                }
                showCloseButton(true) {
                    dismiss()
                    navigateBack()
                }
                title(getString(R.string.pay_with_qr_code_reader_permission_camera_rationale_title))
                message(getString(R.string.pay_with_qr_code_reader_permission_camera_rationale_message))
                confirmText(getString(R.string.pay_with_qr_code_reader_permission_camera_rationale_confirm)) {
                    dismiss()
                    proceedStartCameraPermissionRequest()
                }
                neutralText(getString(R.string.pay_with_qr_code_reader_permission_camera_rationale_cancel)) {
                    dismiss()
                    navigateBack()
                }
            }
    }

    @OnNeverAskAgain(value = [Manifest.permission.CAMERA])
    fun startCameraNeverAskAgain() {
        val snackbar = Snackbar.make(
            requireBinding().root,
            getString(R.string.pay_with_qr_code_reader_permission_camera_never_ask_again_message),
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction(getString(R.string.pay_with_qr_code_reader_permission_camera_never_ask_again_action)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(
                intent,
                RequestCode.PAY_WITH_QR_CODE_READER_ENABLE_CAMERA_PERMISSION
            )
        }

        snackbar.show()

        neverAskAgainSnackbar = snackbar
    }

    private fun getPaymentData(token: String) {
        viewModel.getPaymentDataLiveData(token = token)
            .observe(viewLifecycleOwner) { resource ->
                val x = when (resource) {
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