package br.com.tln.personalcard.usuario.ui.initialization.password

import br.com.tln.personalcard.usuario.core.Navigator

interface InitializationPasswordNavigator : Navigator {
    fun navigateToForgotPassword()
    fun navigateToWelcome(firstAccess: Boolean)
    fun navigateToHome()
}