package br.com.tln.personalcard.usuario.ui.editprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import arrow.core.Either
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.binding.ConditionalMask
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.extensions.format
import br.com.tln.personalcard.usuario.extensions.initials
import br.com.tln.personalcard.usuario.extensions.isEmail
import br.com.tln.personalcard.usuario.extensions.toLocalDateOrNull
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.webservice.request.EditProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val userRepository: UserRepository,
    sessionRepository: SessionRepository
) : SessionRequiredBaseViewModel<EditProfileNavigator>(
    sessionRepository = sessionRepository
) {

    private val phoneLength by lazy {
        resourceProvider.getInteger(R.integer.phone_length)
    }
    private val mobilePhoneLength by lazy {
        resourceProvider.getInteger(R.integer.mobile_phone_length)
    }
    private val birthDatePattern by lazy {
        resourceProvider.getString(R.string.edit_profile_pattern_birth_date)
    }
    private val initializationDataSourceLiveData by lazy {
        sessionRepository.getInitializationDataLiveData()
    }
    private val profileSourceLiveData by lazy {
        sessionRepository.getProfileLiveData()
    }

    private val _selectNewProfilePictureLiveData = MutableLiveData<Event<Nothing?>>()
    private val _phone: MediatorLiveData<String?> by lazy {
        val liveData = MediatorLiveData<String?>()

        liveData.addSource(profileSourceLiveData) {
            liveData.removeSource(profileSourceLiveData)
            liveData.value = it?.phone
        }

        liveData
    }
    private val _email: MediatorLiveData<String?> by lazy {
        val liveData = MediatorLiveData<String?>()

        liveData.addSource(profileSourceLiveData) {
            liveData.removeSource(profileSourceLiveData)
            liveData.value = it?.email
        }

        liveData
    }
    private val _birthDate: MediatorLiveData<String?> by lazy {
        val liveData = MediatorLiveData<String?>()

        liveData.addSource(profileSourceLiveData) {
            liveData.removeSource(profileSourceLiveData)
            liveData.value = it?.birthDate?.format(pattern = birthDatePattern)
        }

        liveData
    }

    val selectNewProfilePictureLiveData: LiveData<Event<Nothing?>>
        get() = _selectNewProfilePictureLiveData

    val phoneMasks = listOf(
        ConditionalMask(resourceProvider.getString(R.string.mask_dynamic_phone_phone)) {
            it == null || it.length <= phoneLength
        },
        ConditionalMask(resourceProvider.getString(R.string.mask_dynamic_phone_mobile)) {
            it == null || it.length == mobilePhoneLength
        }
    )

    val nameInitials: LiveData<String?> =
        Transformations.map(initializationDataSourceLiveData) {
            it?.name?.initials()
        }
    val name: LiveData<String?> = Transformations.map(initializationDataSourceLiveData) {
        it?.name
    }
    val hasPicture: LiveData<Boolean?> = Transformations.map(profileSourceLiveData) {
        it?.picture?.isNotEmpty()
    }
    val phone: MutableLiveData<String?>
        get() = _phone
    val email: MutableLiveData<String?>
        get() = _email
    val birthDate: MutableLiveData<String?>
        get() = _birthDate
    val picture: LiveData<String?> = Transformations.map(profileSourceLiveData) {
        it?.picture
    }

    fun setPhone(phone: String) {
        _phone.value = phone
        _phone.removeSource(profileSourceLiveData)
    }

    fun setEmail(email: String) {
        _email.value = email
        _email.removeSource(profileSourceLiveData)
    }

    fun setBirthDate(birthDate: String) {
        _birthDate.value = birthDate
        _birthDate.removeSource(profileSourceLiveData)
    }

    fun changePictureClicked() {
        _selectNewProfilePictureLiveData.value = Event(data = null)
    }

    fun saveClicked(): LiveData<Resource<Nothing?, Nothing?>>? {

        _errorMessageLiveData.postValue(Event(""))

        val phone = this.phone.value
        val email = this.email.value
        val birthDate = this.birthDate.value?.toLocalDateOrNull(pattern = birthDatePattern)
        val avatarPath = this.picture.value

        if (phone.isNullOrEmpty()) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.edit_profile_validation_invalid_phone)))
            return null
        } else if (email.isNullOrEmpty() || !email.isEmail()) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.edit_profile_validation_invalid_email)))
            return null
        } else if (birthDate == null) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.edit_profile_validation_invalid_birth_date)))
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val request = EditProfileRequest(
                email = email,
                phone = phone,
                birthDate = birthDate.toString()
            )

            val result = withContext(Dispatchers.IO) {
                userRepository.editProfile(accessToken = account.accessToken, request = request)
            }

            when (result) {
                is Either.Left -> {
                    onEditProfileDataFailure(result.a) { value -> liveData.value = value }
                }
                is Either.Right -> {
                    val avatar = File(avatarPath)
                    if (avatar.exists()) {
                        userRepository.updateAvatar(accessToken = account.accessToken, avatar = avatar)
                    }
                    onEditProfileDataSuccess(resource = result.b) { value -> liveData.value = value }
                }
            }

        }

        return liveData
    }

    private fun onEditProfileDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onEditProfileDataSuccess(
        resource: Resource<Nothing?, String?>,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onEditProfileLoading(valueListener = valueListener)
            }
            is SuccessResource -> {


                onEditProfileSuccess(valueListener = valueListener)
            }
            is ErrorResource -> {
                onEditProfileError(valueListener = valueListener)
            }
        }
    }

    private fun onEditProfileLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onEditProfileSuccess(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))

        navigator?.navigateBack()
    }

    private fun onEditProfileError(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(ErrorResource(data = null))
        _unknownErrorLiveData.postValue(Event(data = null))
    }

    fun changePicture(picture: File): LiveData<Resource<Nothing?, Nothing?>>? {
        if (!picture.exists()) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.edit_profile_validation_invalid_picture)))
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val profile = withContext(Dispatchers.IO) { sessionRepository.getProfile() } ?: return@launch

            withContext(Dispatchers.IO) {
                sessionRepository.update(
                    profile.copy(picture = picture.absolutePath)
                )

                // TODO delete picture picture.delete()
            }

            liveData.value = SuccessResource(data = null)
        }

        return liveData
    }
}