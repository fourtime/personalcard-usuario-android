package br.com.tln.personalcard.usuario.ui.paywithqrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.model.ConfirmPayment
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.math.BigDecimal
import javax.inject.Inject

class PayWithQrCodeSuccessViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : SessionRequiredBaseViewModel<PayWithQrCodeSuccessNavigator>(
    sessionRepository = sessionRepository
) {

    private val _establishmentName = MutableLiveData<String>()
    private val _transactionValue = MutableLiveData<BigDecimal>()
    private val _transactionDate = MutableLiveData<LocalDate>()
    private val _transactionTime = MutableLiveData<LocalTime>()
    private val _authorizationNumber = MutableLiveData<String>()
    private val _transactionNumber = MutableLiveData<String>()

    val establishmentName: LiveData<String>
        get() = _establishmentName
    val transactionValue: LiveData<BigDecimal>
        get() = _transactionValue
    val transactionDate: LiveData<LocalDate>
        get() = _transactionDate
    val transactionTime: LiveData<LocalTime>
        get() = _transactionTime
    val authorizationNumber: LiveData<String>
        get() = _authorizationNumber
    val transactionNumber: LiveData<String>
        get() = _transactionNumber

    fun setPaymentData(paymentData: PaymentData, confirmPayment: ConfirmPayment) {
        _establishmentName.value = paymentData.establishment
        _transactionValue.value = paymentData.value
        _transactionDate.value = confirmPayment.transactionTime.toLocalDate()
        _transactionTime.value = confirmPayment.transactionTime.toLocalTime()
        _authorizationNumber.value = confirmPayment.nsuAuthorization.toString()
        _transactionNumber.value = confirmPayment.nsuHost.toString()
    }

    fun updateCardBalance(): LiveData<Resource<Nothing?, Nothing?>>? {

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val result = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()?.let { account ->
                    userRepository.updateCards(
                        accessToken = account.accessToken
                    )
                } ?: null
            } ?: return@launch

            when (result) {
                is Either.Left -> {
                    onUpdateCardBalanceFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onUpdateCardBalanceSuccess(result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onUpdateCardBalanceFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onUpdateCardBalanceSuccess(
        resource: Resource<Nothing?, Nothing?>,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onUpdateCardBalanceLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onUpdateCardBalanceSuccess(
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onConfirmPaymentBalanceError(
                    valueListener = valueListener
                )
            }
        }
    }

    private fun onUpdateCardBalanceLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onUpdateCardBalanceSuccess(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))
        navigator?.navigateToHome()
    }

    private fun onConfirmPaymentBalanceError(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
    }
}