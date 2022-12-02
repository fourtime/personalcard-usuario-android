package br.com.tln.personalcard.usuario.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SessionRequiredBaseViewModel<T : SessionRequiredNavigator>(
    protected val sessionRepository: SessionRepository
) : BaseViewModel<T>() {

    private val _authenticationErrorLiveData = MutableLiveData<Event<Nothing?>>()

    val authenticationErrorLiveData: LiveData<Event<Nothing?>> = _authenticationErrorLiveData
    val sessionMonitorLiveData: LiveData<Nothing?> by lazy {
        val liveData: MediatorLiveData<Nothing?> = MediatorLiveData()

        liveData.addSource(
            sessionRepository.getSessionLiveData(
                allowCache = false
            )
        ) { session ->
            val account = session?.account
            val initializationData = session?.initializationData

            if (initializationData == null) {
                onSessionNotFound()
                navigator?.navigateToPreLogin()
            } else if (initializationData.firstAccess) {
                onInitializingSessionFound()
                navigator?.navigateToCreatePassword()
            } else if (!initializationData.firstAccess && account == null) {
                onInitializingSessionFound()
                navigator?.navigateToLogin(
                    cpf = initializationData.cpf,
                    registerEmail = !initializationData.hasEmail
                )
            } else if (!initializationData.hasEmail) {
                onInitializingSessionFound()
                navigator?.navigateToRegisterEmail()
            } else if (account != null) {
                if (account.accountData.locked) {
                    navigator?.navigateToInitializationPassword(
                        cpf = initializationData.cpf,
                        registerEmail = !initializationData.hasEmail
                    )
                } else {
                    onSessionFound()
                    liveData.value = null
                }
            } else {
                onSessionFound()
                liveData.value = null
            }
        }

        liveData
    }

    override fun getAdditionalErrorHandlersMapping(): LinkedHashMap<Class<out Throwable>, List<MutableLiveData<Event<Nothing?>>>> {
        return linkedMapOf(
            InvalidAuthenticationException::class.java to listOf(
                _authenticationErrorLiveData
            )
        )
    }

    protected open fun onSessionNotFound() {
    }

    protected open fun onInitializingSessionFound() {
    }

    protected open fun onSessionFound() {
    }

    fun lockSession() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sessionRepository.getAccountData()?.let { accountData ->
                    sessionRepository.update(accountData.copy(locked = true))
                }
            } ?: return@launch
        }
    }
}