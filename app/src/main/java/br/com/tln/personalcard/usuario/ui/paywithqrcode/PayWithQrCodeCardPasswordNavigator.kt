package br.com.tln.personalcard.usuario.ui.paywithqrcode

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator
import br.com.tln.personalcard.usuario.model.ConfirmPayment

interface PayWithQrCodeCardPasswordNavigator : SessionRequiredNavigator {
    fun navigateToSuccess(confirmPayment: ConfirmPayment)
}