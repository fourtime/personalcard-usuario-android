package br.com.tln.personalcard.usuario.ui.initialization.cardlastdigits

import br.com.tln.personalcard.usuario.core.Navigator

interface InitializationCardLastDigitsNavigator : Navigator {
    fun navigateToInitializationCreatePassword()
    fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean)
}