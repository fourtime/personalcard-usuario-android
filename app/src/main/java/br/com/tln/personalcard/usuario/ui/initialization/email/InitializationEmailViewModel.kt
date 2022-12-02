package br.com.tln.personalcard.usuario.ui.initialization.email

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
import br.com.tln.personalcard.usuario.extensions.isEmail
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.webservice.request.UpdateEmailRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitializationEmailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : BaseViewModel<InitializationEmailNavigator>() {

    val email = MutableLiveData<String?>()

    val errorMessage: MutableLiveData<Int> by lazy {
        val liveData = MediatorLiveData<Int>()

        liveData.addSource(email) {
            liveData.value = R.string.empty
        }

        liveData
    }

    val validEmail: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(email) {
            liveData.postValue(!it.isNullOrEmpty())
        }

        liveData
    }

    fun errorClicked() {
        errorMessage.value = R.string.empty
    }

    fun continueClicked(): LiveData<Resource<Nothing?, Nothing?>>? {
        val email = this.email.value

        if (email.isNullOrEmpty()) {
            errorMessage.postValue(R.string.initialization_email_invalid_email)
            return null
        } else if (!email.isEmail()) {
            errorMessage.postValue(R.string.initialization_email_invalid_email)
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

            val request = UpdateEmailRequest(
                email = email
            )

            val result = withContext(Dispatchers.IO) {
                userRepository.updateEmail(
                    accessToken = account.accessToken,
                    request = request
                )
            }

            when (result) {
                is Either.Left -> {
                    onUpdateEmailDataFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onUpdateEmailDataSuccess(
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

    private fun onUpdateEmailDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onUpdateEmailDataSuccess(
        resource: Resource<Nothing?, Nothing?>,
        initializationData: InitializationData,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onUpdateEmailLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onUpdateEmailSuccess(
                    initializationData = initializationData,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onUpdateEmailError(valueListener = valueListener)
            }
        }
    }

    private fun onUpdateEmailLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onUpdateEmailSuccess(
        initializationData: InitializationData,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                sessionRepository.update(initializationData.copy(hasEmail = true))
            }

            navigator?.navigateToWelcome()
        }
    }

    private fun onUpdateEmailError(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(ErrorResource(data = null))
        _unknownErrorLiveData.postValue(Event(data = null))
    }
}