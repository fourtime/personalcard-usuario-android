package br.com.tln.personalcard.usuario.ui.initialization.forgotpassword

import br.com.tln.personalcard.usuario.core.Navigator

interface InitializationForgotPasswordNavigator : Navigator {
    fun navigateToForgotPasswordConfirmation()
    fun navigateToRecoveryPasswordSuccess(cpf: String, firstAccess: Boolean)
}