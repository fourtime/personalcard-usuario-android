package br.com.tln.personalcard.usuario.ui.paywithqrcode

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator
import br.com.tln.personalcard.usuario.model.PaymentData

interface PayWithQrCodeReaderNavigator : SessionRequiredNavigator {
    fun navigateToQrPaymentData(paymentData: PaymentData, paymentToken: String)
}