package br.com.tln.personalcard.usuario.ui.paywithqrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.repository.SessionRepository
import java.math.BigDecimal
import javax.inject.Inject

class PayWithQrCodePaymentDataViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : SessionRequiredBaseViewModel<PayWithQrCodePaymentDataNavigator>(
    sessionRepository = sessionRepository
) {

    private val _establishmentName = MutableLiveData<String?>()
    private val _establishmentAddress = MutableLiveData<String?>()
    private val _transactionValue = MutableLiveData<BigDecimal>()
    private val _transactionCondition = MutableLiveData<String?>()
    private val _cardLabel = MutableLiveData<String?>()
    private val _cardLastDigits = MutableLiveData<String?>()

    val establishmentName: LiveData<String?>
        get() = _establishmentName
    val establishmentAddress: LiveData<String?>
        get() = _establishmentAddress
    val transactionValue: LiveData<BigDecimal>
        get() = _transactionValue
    val transactionCondition: LiveData<String?>
        get() = _transactionCondition
    val cardLabel: LiveData<String?>
        get() = _cardLabel
    val cardLastDigits: LiveData<String?>
        get() = _cardLastDigits

    fun setCard(card: Card) {
        _cardLabel.value = card.label
        _cardLastDigits.value = card.lastDigits
    }

    fun setPaymentData(paymentData: PaymentData) {
        _establishmentName.value = paymentData.establishment
        _establishmentAddress.value = paymentData.address
        _transactionValue.value = paymentData.value
        _transactionCondition.value = paymentData.condition
    }

    fun payClicked() {
        navigator?.navigateToCardPassword()
    }
}