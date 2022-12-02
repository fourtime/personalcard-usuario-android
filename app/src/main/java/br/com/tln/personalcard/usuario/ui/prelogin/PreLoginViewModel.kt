package br.com.tln.personalcard.usuario.ui.prelogin

import br.com.tln.personalcard.usuario.core.BaseViewModel
import javax.inject.Inject

class PreLoginViewModel @Inject constructor() : BaseViewModel<PreLoginNavigator>() {

    fun enterClicked() {
        navigator?.navigateToInitializationCpf()
    }
}