package br.com.tln.personalcard.usuario.ui.initialization.createpassword

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
import br.com.tln.personalcard.usuario.entity.InitializationData
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.webservice.request.ChangePasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitializationCreatePasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : BaseViewModel<InitializationCreatePasswordNavigator>() {

    val password = MutableLiveData<String?>()
    val passwordConfirmation = MutableLiveData<String?>()

    val errorMessage: MutableLiveData<Int> by lazy {
        val liveData = MediatorLiveData<Int>()

        liveData.addSource(password) {
            liveData.value = R.string.empty
        }

        liveData.addSource(passwordConfirmation) {
            liveData.value = R.string.empty
        }

        liveData
    }

    val validPassword: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(password) {
            liveData.postValue(isValidForm())
        }

        liveData.addSource(passwordConfirmation) {
            liveData.postValue(isValidForm())
        }

        liveData
    }

    private fun isValidForm(): Boolean {
        val password = this.password.value
        val passwordConfirmation = this.passwordConfirmation.value

        return !password.isNullOrEmpty() && password == passwordConfirmation
    }

    fun errorClicked() {
        errorMessage.value = R.string.empty
    }

    fun continueClicked(): LiveData<Resource<Nothing?, Nothing?>>? {
        val password = this.password.value
        val passwordConfirmation = this.passwordConfirmation.value

        if (password.isNullOrEmpty() || passwordConfirmation.isNullOrEmpty()) {
            errorMessage.postValue(R.string.initialization_create_password_invalid_password)
            return null
        } else if (password != passwordConfirmation) {
            errorMessage.postValue(R.string.initialization_create_password_invalid_password_confirmation)
            return null
        } else if (password.length < resourceProvider.getInteger(R.integer.register_password_min_length)) {
            errorMessage.postValue(R.string.initialization_create_password_invalid_password_min_lenght)
            return null
        } else if (password.length > resourceProvider.getInteger(R.integer.register_password_max_length)) {
            errorMessage.postValue(R.string.initialization_create_password_invalid_password_max_lenght)
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()
        liveData.value = LoadingResource()

        viewModelScope.launch {
            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val initializationData = withContext(Dispatchers.IO) {
                sessionRepository.getInitializationData()
            } ?: return@launch

            val request = ChangePasswordRequest(
                currentPassword = account.accountData.username.substring(0, 8),
                newPassword = password
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
                    onChangePasswordDataSuccess(
                        resource = result.b,
                        initializationData = initializationData
                    ) { value ->
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
        errorNotifier.notify(throwable)
    }

    private fun onChangePasswordDataSuccess(
        resource: Resource<Nothing?, String?>,
        initializationData: InitializationData,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onChangePasswordLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onChangePasswordSuccess(
                    initializationData = initializationData,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onChangePasswordError(valueListener = valueListener)
            }
        }
    }

    private fun onChangePasswordLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onChangePasswordSuccess(
        initializationData: InitializationData,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                sessionRepository.update(initializationData.copy(firstAccess = false))
            }

            if (initializationData.hasEmail) {
                navigator?.navigateToHome()
            } else {
                navigator?.navigateToInitializationEmail(firstAccess = true)
            }
        }
    }

    private fun onChangePasswordError(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(ErrorResource(data = null))
        _unknownErrorLiveData.postValue(Event(data = null))
    }
}