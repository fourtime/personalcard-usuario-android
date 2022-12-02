package br.com.tln.personalcard.usuario.ui.paywithqrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.BuildConfig
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.extensions.CryptographyUtils.encrypt
import br.com.tln.personalcard.usuario.model.ConfirmPayment
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.preferences.AppPreferences
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.TransactionRepository
import br.com.tln.personalcard.usuario.webservice.request.ConfirmPaymentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayWithQrCodeCardPasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    sessionRepository: SessionRepository,
    private val transactionRepository: TransactionRepository,
    private val initializationRepository: SessionRepository,
    private val appPreferences: AppPreferences
) : SessionRequiredBaseViewModel<PayWithQrCodeCardPasswordNavigator>(
    sessionRepository = sessionRepository
) {

    private val passwordLength: Int by lazy {
        resourceProvider.getInteger(R.integer.card_password_length)
    }

    private val _validPassword: MutableLiveData<Boolean?> by lazy {
        val liveData = MediatorLiveData<Boolean?>()

        liveData.addSource(password) {
            _validPassword.value = it.length == passwordLength
        }

        liveData
    }

    val password = MutableLiveData<String>()
    val validPassword: LiveData<Boolean?>
        get() = _validPassword

    fun payClicked(
        card: Card,
        paymentData: PaymentData
    ): LiveData<Resource<Nothing?, Nothing?>>? {
        val password = this.password.value

        if (password.isNullOrEmpty() || password.length < passwordLength) {
            return null
        }

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val accountData = withContext(Dispatchers.IO) {
                initializationRepository.getAccountData()
            } ?: return@launch

            val request = ConfirmPaymentRequest(
                card = card.id,
                cardPassword = encryptPassword(
                    password = password,
                    cpf = accountData.username,
                    cardId = card.id,
                    paymentToken = paymentData.token
                ),
                token = paymentData.token
            )

            val result = withContext(Dispatchers.IO) {
                transactionRepository.confirmPayment(
                    request = request,
                    accessToken = account.accessToken
                )
            }

            when (result) {
                is Either.Left -> {
                    onConfirmPaymentFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onConfirmPaymentSuccess(result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun encryptPassword(
        password: String,
        cpf: String,
        cardId: String,
        paymentToken: String
    ): String {

        val imei = appPreferences.getDeviceId().substring(0, 8).toLowerCase()
        val cardId = cardId.substring(cardId.length - 8)
        val paymentToken = paymentToken.substring(0, 8)
        val cpf = cpf.substring(cpf.length - 8)

        val seed = "$imei$cardId$paymentToken$cpf"

        val key = seed.substring(0, 16)
        val initializationVector = seed.substring(16)

        return password.encrypt(
            key = key,
            initializationVector = initializationVector,
            transformation = BuildConfig.CARD_PASSWORD_CRYPTOGRAPHY_TRANSFORMATION,
            algorithm = BuildConfig.CARD_PASSWORD_CRYPTOGRAPHY_ALGORITHM
        )
    }

    private fun onConfirmPaymentFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onConfirmPaymentSuccess(
        resource: Resource<ConfirmPayment, String?>,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onConfirmPaymentLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onConfirmPaymentSuccess(
                    confirmPayment = resource.data,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onConfirmPaymentError(
                    errorMessage = resource.data,
                    valueListener = valueListener
                )
            }
        }
    }

    private fun onConfirmPaymentLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onConfirmPaymentSuccess(
        confirmPayment: ConfirmPayment,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))
        navigator?.navigateToSuccess(confirmPayment)
    }

    private fun onConfirmPaymentError(
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