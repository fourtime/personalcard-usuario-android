package br.com.tln.personalcard.usuario.ui.home

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator
import br.com.tln.personalcard.usuario.entity.Card

interface HomeNavigator : SessionRequiredNavigator {
    fun navigateToEditProfile()
    fun navigateToChangePassword()
    fun navigateToAccreditedNetwork(label: String, card: Card)
    fun navigateToPayWithQrCode(card: Card)
}