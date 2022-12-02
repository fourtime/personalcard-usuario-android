package br.com.tln.personalcard.usuario.ui.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseViewModel
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.webservice.request.ChangePasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangePasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : BaseViewModel<ChangePasswordNavigator>() {

    val currentPassword = MutableLiveData<String?>()
    val newPassword = MutableLiveData<String?>()
    val newPasswordConfirmation = MutableLiveData<String?>()

    val validPassword: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(currentPassword) {
            liveData.postValue(isValidForm())
        }

        liveData.addSource(newPassword) {
            liveData.postValue(isValidForm())
        }

        liveData.addSource(newPasswordConfirmation) {
            liveData.postValue(isValidForm())
        }

        liveData
    }

    private fun isValidForm(): Boolean {
        val currentPassword = this.currentPassword.value
        val newPassword = this.newPassword.value
        val newPasswordConfirmation = this.newPasswordConfirmation.value

        _errorMessageLiveData.postValue(Event(""))

        return !currentPassword.isNullOrEmpty() && !newPassword.isNullOrEmpty() && !newPasswordConfirmation.isNullOrEmpty()
    }

    fun saveClicked(): LiveData<Resource<Nothing?, Nothing?>>? {

        _errorMessageLiveData.postValue(Event(""))

        val currentPassword = this.currentPassword.value
        val newPassword = this.newPassword.value
        val newPasswordConfirmation = this.newPasswordConfirmation.value

        if (currentPassword.isNullOrEmpty() || newPassword.isNullOrEmpty() || newPasswordConfirmation.isNullOrEmpty()) {
            return null
        } else if (newPassword != newPasswordConfirmation) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.change_password_invalid_password_confirmation)))
            return null
        } else if (newPassword.length < resourceProvider.getInteger(R.integer.change_password_min_length)) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.change_password_invalid_password_min_lenght)))
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()
        liveData.value = LoadingResource()

        viewModelScope.launch {

            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            val result = withContext(Dispatchers.IO) {
                userRepository.changePassword(
                    accessToken = account.accessToken,
                    request = request
                )
            }

            when (result) {
                is Either.Left -> {
                    onChangePasswordDataFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onChangePasswordDataSuccess(result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onChangePasswordDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        _errorMessageLiveData.postValue(Event(data = ""))
        errorNotifier.notify(throwable)
    }

    private fun onChangePasswordDataSuccess(
        resource: Resource<Nothing?, String?>,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onChangePasswordLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onChangePasswordSuccess(valueListener = valueListener)
            }
            is ErrorResource -> {
                onChangePasswordError(errorMessage = resource.data, valueListener = valueListener)
            }
        }
    }

    private fun onChangePasswordLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onChangePasswordSuccess(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))

        GlobalScope.launch(Dispatchers.Main) {
            navigator?.navigateToHome()
        }
    }

    private fun onChangePasswordError(
        errorMessage: String?,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {

        valueListener(ErrorResource(data = null))

        if (errorMessage.isNullOrEmpty()) {
            _unknownErrorLiveData.postValue(Event(data = null))
        } else {
            _errorMessageLiveData.postValue(Event(data = errorMessage))
        }
    }
}