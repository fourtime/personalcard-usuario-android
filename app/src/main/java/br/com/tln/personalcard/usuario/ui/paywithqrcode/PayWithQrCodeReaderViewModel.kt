package br.com.tln.personalcard.usuario.ui.paywithqrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.exception.InvalidQrCodeException
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.TransactionRepository
import br.com.tln.personalcard.usuario.webservice.request.PaymentDataRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PayWithQrCodeReaderViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val transactionRepository: TransactionRepository
) : SessionRequiredBaseViewModel<PayWithQrCodeReaderNavigator>(
    sessionRepository = sessionRepository
) {

    private val _invalidQrCodeLiveData = MutableLiveData<Event<Nothing?>>()

    val invalidQrCodeLiveData
        get() = _invalidQrCodeLiveData

    override fun getAdditionalErrorHandlersMapping(): LinkedHashMap<Class<out Throwable>, List<MutableLiveData<Event<Nothing?>>>> {
        return linkedMapOf(
            InvalidQrCodeException::class.java to listOf(
                _invalidQrCodeLiveData
            )
        )
    }

    fun getPaymentDataLiveData(token: String): LiveData<Resource<Nothing?, Nothing?>> {
        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val request = PaymentDataRequest(
                token = token
            )

            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val result = withContext(Dispatchers.IO) {
                transactionRepository.paymentData(
                    accessToken = account.accessToken,
                    request = request,
                    paymentToken = token
                )
            }

            when (result) {
                is Either.Left -> {
                    onPaymentDataFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onPaymentDataDataSuccess(result.b, token) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onPaymentDataFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onPaymentDataDataSuccess(
        resource: Resource<PaymentData, String?>,
        paymentToken: String,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onPaymentLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onPaymentDataSuccess(
                    paymentData = resource.data,
                    paymentToken = paymentToken,
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onPaymentDataError(
                    errorMessage = resource.data,
                    valueListener = valueListener
                )
            }
        }
    }

    private fun onPaymentLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onPaymentDataSuccess(
        paymentData: PaymentData,
        paymentToken: String,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))
        navigator?.navigateToQrPaymentData(paymentData, paymentToken)
    }

    private fun onPaymentDataError(
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