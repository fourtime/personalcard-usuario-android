package br.com.tln.personalcard.usuario.ui.initialization.password

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
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.model.Account
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import javax.inject.Inject

class InitializationPasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences
) : BaseViewModel<InitializationPasswordNavigator>() {

    lateinit var cpf: String
    var registerEmail: Boolean = false

    val password = MutableLiveData<String?>()

    val validPassword: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(password) {
            liveData.postValue(!it.isNullOrEmpty())
        }

        liveData
    }


    fun forgotPasswordClicked() {
        if (appPreferences.getCanRequestPassword()) {
            navigator?.navigateToForgotPassword()
        } else {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.initialization_forgot_password_invalid_recovery)))
        }
    }

    fun continueClicked(): LiveData<Resource<Nothing?, String?>>? {
        _errorMessageLiveData.postValue(Event(""))

        val password = this.password.value

        if (password.isNullOrEmpty()) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.initialization_password_invalid_password)))
            return null
        } else if (password.length < resourceProvider.getInteger(R.integer.register_password_min_length)) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.initialization_create_password_invalid_password_min_lenght)))
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, String?>>()
        liveData.value = LoadingResource()

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                userRepository.login(
                    cpf = cpf,
                    password = password
                )
            }

            when (result) {
                is Either.Left -> {
                    onLoginDataFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onLoginDataSuccess(resource = result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onLoginDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))

        if (throwable is InvalidAuthenticationException) {
            _errorMessageLiveData.postValue(Event(resourceProvider.getString(R.string.initialization_password_invalid_password_try_again)))
        } else {
            errorNotifier.notify(throwable)
        }
    }

    private fun onLoginDataSuccess(
        resource: Resource<Account, String?>,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onLoginLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onLoginSuccess(valueListener = valueListener)
            }
            is ErrorResource -> {
                onLoginError(valueListener = valueListener)
            }
        }
    }

    private fun onLoginLoading(valueListener: (Resource<Nothing?, String?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onLoginSuccess(valueListener: (Resource<Nothing?, String?>) -> Unit) {
        valueListener(SuccessResource(null))

        if (registerEmail) {
            navigator?.navigateToWelcome(firstAccess = false)
        } else {
            navigator?.navigateToHome()
        }
    }

    private fun onLoginError(valueListener: (Resource<Nothing?, String?>) -> Unit) {
        valueListener(ErrorResource(data = null))
        _unknownErrorLiveData.postValue(Event(data = null))
    }
}