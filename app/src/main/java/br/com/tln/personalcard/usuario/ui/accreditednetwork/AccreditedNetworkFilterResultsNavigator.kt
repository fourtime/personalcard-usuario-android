package br.com.tln.personalcard.usuario.ui.accreditednetwork

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator
import br.com.tln.personalcard.usuario.model.AccreditedNetwork

interface AccreditedNetworkFilterResultsNavigator : SessionRequiredNavigator {
    fun navigateToAccreditedNetworkMap(accreditedNetwork: AccreditedNetwork)
}