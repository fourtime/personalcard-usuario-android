package br.com.tln.personalcard.usuario.ui.initialization.cardlastdigits

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
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitializationCardLastDigitsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val userRepository: UserRepository
) : BaseViewModel<InitializationCardLastDigitsNavigator>() {

    lateinit var cpf: String

    private val lastDigitsLength: Int by lazy {
        resourceProvider.getInteger(R.integer.last_digits_length)
    }

    val errorMessage: MutableLiveData<Int> by lazy {
        val liveData = MediatorLiveData<Int>()

        liveData.addSource(lastDigits) {
            liveData.value = R.string.empty
        }

        liveData
    }

    val validLastDigits: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(lastDigits) { value ->
            liveData.value = value.length == lastDigitsLength
        }

        liveData
    }

    val lastDigits = MutableLiveData<String>()

    fun errorClicked() {
        errorMessage.value = R.string.empty
    }

    fun continueClicked(): LiveData<Resource<Nothing?, String?>>? {
        errorMessage.value = R.string.empty

        val lastDigits = this.lastDigits.value

        if (lastDigits.isNullOrEmpty() || lastDigits.length != lastDigitsLength) {
            errorMessage.value = R.string.initialization_card_last_digits_invalid_last_digits
            return null
        }

        val liveData = MediatorLiveData<Resource<Nothing?, String?>>()

        liveData.value = LoadingResource()

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                userRepository.initialize(cpf, lastDigits)
            }

            when (result) {
                is Either.Left -> {
                    onInitializationDataFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onInitializationDataSuccess(result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onInitializationDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))

        if (throwable is InvalidAuthenticationException) {
            errorMessage.postValue(R.string.dialog_generic_error_message)
        } else {
            errorNotifier.notify(throwable)
        }
    }

    private fun onInitializationDataSuccess(
        resource: Resource<InitializationData, String?>,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onInitializationLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onInitializationSuccess(
                    initializationData = resource.data,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onInitializationError(
                    apiErrorMessage = resource.data,
                    valueListener = valueListener
                )
            }
        }
    }

    private fun onInitializationLoading(valueListener: (Resource<Nothing?, String?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onInitializationSuccess(
        initializationData: InitializationData,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))

        if (initializationData.firstAccess) {
            navigator?.navigateToInitializationCreatePassword()
        } else {
            navigator?.navigateToInitializationPassword(
                cpf = initializationData.cpf,
                registerEmail = !initializationData.hasEmail
            )
        }
    }

    private fun onInitializationError(
        apiErrorMessage: String?,
        valueListener: (Resource<Nothing?, String?>) -> Unit
    ) {
        valueListener(ErrorResource(null))

        if (apiErrorMessage.isNullOrEmpty()) {
            _unknownErrorLiveData.postValue(Event(data = null))
        } else {
            _errorMessageLiveData.postValue(Event(data = apiErrorMessage))
        }
    }
}