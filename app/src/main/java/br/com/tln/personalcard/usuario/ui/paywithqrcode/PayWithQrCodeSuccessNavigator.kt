package br.com.tln.personalcard.usuario.ui.paywithqrcode

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator

interface PayWithQrCodeSuccessNavigator : SessionRequiredNavigator {
    fun navigateToHome()
}