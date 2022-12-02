package br.com.tln.personalcard.usuario.ui.initialization.createpassword

import br.com.tln.personalcard.usuario.core.Navigator

interface InitializationCreatePasswordNavigator : Navigator {
    fun navigateToInitializationEmail(firstAccess: Boolean)
    fun navigateToHome()
}