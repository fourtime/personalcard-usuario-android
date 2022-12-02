package br.com.tln.personalcard.usuario.ui.accreditednetwork

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator
import br.com.tln.personalcard.usuario.model.AccreditedNetworkFilter

interface AccreditedNetworkFilterNavigator : SessionRequiredNavigator {
    fun navigateToFilterResults(accreditedNetworkFilter: AccreditedNetworkFilter)
}