package br.com.tln.personalcard.usuario.ui.editprofile

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.RequestCode
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentEditProfileBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.observeEvent
import br.com.tln.personalcard.usuario.extensions.observeResource
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog
import br.com.tln.personalcard.usuario.widget.ErrorToast
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.util.UUID


@RuntimePermissions
class EditProfileFragment :
    SessionRequiredBaseFragment<FragmentEditProfileBinding, EditProfileNavigator, EditProfileViewModel>(
        R.layout.fragment_edit_profile,
        EditProfileViewModel::class.java
    ), EditProfileNavigator, HasToolbar {

    private var errorToast: ErrorToast? = null

    private var loadingDialog: ProgressDialog? = null
    private var cameraPictureFile: File? = null
    private var croppedPictureFile: File? = null

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.apply {
            getString(STATE_CAMERA_PICTURE_FILE)?.let {
                cameraPictureFile = File(it)
            }
            getString(STATE_CROPPED_PICTURE_FILE)?.let {
                croppedPictureFile = File(it)
            }
            getString(STATE_PHONE)?.let {
                viewModel.setPhone(it)
            }
            getString(STATE_EMAIL)?.let {
                viewModel.setEmail(it)
            }
            getString(STATE_BIRTH_DATE)?.let {
                viewModel.setBirthDate(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().toolbar.inflateMenu(R.menu.options_edit_profile)
        requireBinding().toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.editProfileSave -> {
                    saveProfile()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        viewModel.selectNewProfilePictureLiveData.observeEvent(viewLifecycleOwner) {
            BottomSheetDialog(requireContext())
                .show {
                    showCloseButton(true)
                    title(getString(R.string.edit_profile_change_profile_picture))
                    options {
                        option(
                            text = getString(R.string.edit_profile_change_profile_picture_camera),
                            clickListener = {
                                dismiss()
                                navigateToCamera()
                            }
                        )
                        option(
                            text = getString(R.string.edit_profile_change_profile_picture_gallery),
                            clickListener = {
                                dismiss()
                                navigateToGallery()
                            }
                        )
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.EDIT_PROFILE_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                val destinationFile = requireContext().createNewFile(
                    subDir = Environment.DIRECTORY_PICTURES,
                    extension = "png"
                )

                val sourceUri = data?.data ?: return
                val destinationUri = Uri.fromFile(destinationFile)

                cropPicture(
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    destinationFile = destinationFile
                )
            }
        } else if (requestCode == RequestCode.EDIT_PROFILE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val cameraPictureFile =
                    this.cameraPictureFile
                        ?: throw IllegalStateException("Camera saved picture to an unknown file")
                val sourceUri = Uri.fromFile(cameraPictureFile)
                val destinationUri = Uri.fromFile(cameraPictureFile)

                cropPicture(
                    sourceUri = sourceUri,
                    destinationUri = destinationUri,
                    destinationFile = cameraPictureFile
                )
            }
        } else if (requestCode == RequestCode.EDIT_PROFILE_CROP_PICTURE) {
            val croppedPictureFile = this.croppedPictureFile
            if (resultCode == Activity.RESULT_OK && croppedPictureFile != null) {
                changePicture(croppedPictureFile)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_CAMERA_PICTURE_FILE, cameraPictureFile?.absolutePath)
        outState.putString(STATE_CROPPED_PICTURE_FILE, croppedPictureFile?.absolutePath)
        outState.putString(STATE_PHONE, viewModel.phone.value)
        outState.putString(STATE_EMAIL, viewModel.email.value)
        outState.putString(STATE_BIRTH_DATE, viewModel.birthDate.value)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun navigateBack() {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        if (!findNavController().popBackStack() && !findNavController().navigateUp()) {
            val directions = EditProfileFragmentDirections.actionGlobalHomeFragment()
            findNavController().navigate(directions)
        }
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions = EditProfileFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions = EditProfileFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions =
            EditProfileFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions =
            EditProfileFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions = EditProfileFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.editProfileFragment) {
            return
        }

        val directions =
            EditProfileFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
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

    private fun hideErrorToast() {
        viewModel.phone.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
        viewModel.email.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
        viewModel.birthDate.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
        viewModel.picture.observe(viewLifecycleOwner) {
            showErrorMessage(message = "")
        }
    }

    private fun navigateToCamera() {
        showCameraIntentWithPermissionCheck()
    }

    private fun navigateToGallery() {
        showGalleryIntentWithPermissionCheck()
    }

    private fun saveProfile() {
        hideErrorToast()
        viewModel.saveClicked()
            ?.observeResource(viewLifecycleOwner) { resource ->
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

    private fun changePicture(picture: File) {
        hideErrorToast()
        viewModel.changePicture(picture = picture)
            ?.observeResource(viewLifecycleOwner) { resource ->
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

    @NeedsPermission(value = [Manifest.permission.CAMERA])
    fun showCameraIntent() {
        val cameraPictureFile =
            requireContext().createNewFile(
                extension = "png",
                subDir = Environment.DIRECTORY_PICTURES
            )
        this.cameraPictureFile = cameraPictureFile

        val pictureUri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.FILE_PROVIDER_AUTHORITY,
            cameraPictureFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

        startActivityForResult(intent, RequestCode.EDIT_PROFILE_CAMERA)
    }

    @OnShowRationale(value = [Manifest.permission.CAMERA])
    fun showCameraIntentShowRationale() {
        BottomSheetDialog(requireContext())
            .show {
                showCloseButton(true)
                title(getString(R.string.edit_profile_permission_camera_rationale_title))
                message(getString(R.string.edit_profile_permission_camera_rationale_message))
                confirmText(getString(R.string.edit_profile_permission_camera_rationale_confirm)) {
                    dismiss()
                    proceedShowCameraIntentPermissionRequest()
                }
                neutralText(getString(R.string.edit_profile_permission_camera_rationale_cancel)) {
                    dismiss()
                }
            }
    }

    @OnNeverAskAgain(value = [Manifest.permission.CAMERA])
    fun showCameraIntentNeverAskAgain() {
        val snackbar = Snackbar.make(
            requireBinding().root,
            getString(R.string.edit_profile_permission_camera_never_ask_again_message),
            Snackbar.LENGTH_LONG
        )

        snackbar.setAction(getString(R.string.edit_profile_permission_camera_never_ask_again_action)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        snackbar.show()
    }

    @NeedsPermission(value = [Manifest.permission.READ_EXTERNAL_STORAGE])
    fun showGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, RequestCode.EDIT_PROFILE_GALLERY)
    }

    @OnShowRationale(value = [Manifest.permission.READ_EXTERNAL_STORAGE])
    fun showGalleryIntentShowRationale() {
        BottomSheetDialog(requireContext())
            .show {
                showCloseButton(true)
                title(getString(R.string.edit_profile_permission_gallery_rationale_title))
                message(getString(R.string.edit_profile_permission_gallery_rationale_message))
                confirmText(getString(R.string.edit_profile_permission_gallery_rationale_confirm)) {
                    dismiss()
                    proceedShowGalleryIntentPermissionRequest()
                }
                neutralText(getString(R.string.edit_profile_permission_gallery_rationale_cancel)) {
                    dismiss()
                }
            }
    }

    @OnNeverAskAgain(value = [Manifest.permission.READ_EXTERNAL_STORAGE])
    fun showGalleryIntentNeverAskAgain() {
        val snackbar = Snackbar.make(
            requireBinding().root,
            getString(R.string.edit_profile_permission_gallery_never_ask_again_message),
            Snackbar.LENGTH_LONG
        )

        snackbar.setAction(getString(R.string.edit_profile_permission_gallery_never_ask_again_action)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        snackbar.show()
    }

    private fun cropPicture(sourceUri: Uri, destinationUri: Uri, destinationFile: File) {
        croppedPictureFile = destinationFile

        val options = UCrop.Options()
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.SCALE)
        options.setHideBottomControls(true)
        options.withAspectRatio(1f, 1f)
        options.withMaxResultSize(400, 400)
        options.setToolbarTitle(getString(R.string.edit_profile_crop_picture))
        options.setStatusBarColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.telenetColorPrimaryVariant
            )
        )
        options.setToolbarColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.telenetColorPrimary
            )
        )
        options.setToolbarWidgetColor(ContextCompat.getColor(requireContext(), R.color.white))

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .start(requireContext(), this, RequestCode.EDIT_PROFILE_CROP_PICTURE)
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

    fun Context.createNewFile(subDir: String, extension: String): File {
        val fileName = UUID.randomUUID().toString() + "." + extension

        val dir = getExternalFilesDir(subDir) ?: File(requireContext().filesDir, subDir)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return File(dir, fileName)
    }

    companion object {
        const val STATE_CAMERA_PICTURE_FILE = "cameraPictureFile"
        const val STATE_CROPPED_PICTURE_FILE = "croppedPictureFile"
        const val STATE_PHONE = "phone"
        const val STATE_EMAIL = "email"
        const val STATE_BIRTH_DATE = "birthDate"
    }
}