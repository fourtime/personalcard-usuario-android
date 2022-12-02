package br.com.tln.personalcard.usuario.ui.accreditednetwork

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator

interface AccreditedNetworkMapNavigator : SessionRequiredNavigator {
    fun navigateToFilter()
}