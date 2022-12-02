package br.com.tln.personalcard.usuario.ui.initialization.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseViewModel
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.webservice.request.RecoveryPasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitializationForgotPasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val appPreferences: AppPreferences
) : BaseViewModel<InitializationForgotPasswordNavigator>() {

    private val _helpLiveData = MutableLiveData<Event<Nothing?>>()

    val cpf = MutableLiveData<String?>()

    val validCpf: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(cpf) {
            liveData.postValue(!it.isNullOrEmpty())
        }

        liveData
    }

    val helpLiveData: LiveData<Event<Nothing?>> = _helpLiveData

    fun helpClicked() {
        _helpLiveData.postValue(Event(data = null))
    }

    fun continueClicked(): LiveData<Resource<Nothing?, Nothing?>>? {

        _errorMessageLiveData.postValue(Event(data = ""))

        val cpf = this.cpf.value

        if (cpf.isNullOrEmpty() || cpf.length != resourceProvider.getInteger(R.integer.cpf_length)) {

            _errorMessageLiveData.postValue(Event(data = resourceProvider.getString(R.string.initialization_cpf_invalid_cpf)))
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {

            val initializationData = withContext(Dispatchers.IO) {
                sessionRepository.getInitializationData()
            } ?: return@launch

            val requestRecoveryPassword = RecoveryPasswordRequest(
                operatorCode = BuildConfig.OPERATOR_CODE.toInt(),
                cpf = cpf,
                imei = appPreferences.getDeviceId()
            )

            val result = withContext(Dispatchers.IO) {
                userRepository.recoveryPassword(recoveryPasswordRequest = requestRecoveryPassword)
            }

            when (result) {
                is Either.Left -> {
                    onRecoveryPasswordFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onRecoveryPasswordSuccess(
                        result.b,
                        cpf,
                        initializationData.firstAccess
                    ) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onRecoveryPasswordFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onRecoveryPasswordSuccess(
        resource: Resource<Nothing?, String?>,
        cpf: String,
        firstAccess: Boolean,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onRecoveryPasswordLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onRecoveryPasswordSuccess(
                    firstAccess = firstAccess,
                    cpf = cpf,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onRecoveryPasswordError(errorMessage = resource.data, valueListener = valueListener)
            }
        }
    }

    private fun onRecoveryPasswordLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onRecoveryPasswordSuccess(
        cpf: String,
        firstAccess: Boolean,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))
        navigator?.navigateToRecoveryPasswordSuccess(cpf = cpf, firstAccess = firstAccess)
    }

    private fun onRecoveryPasswordError(
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