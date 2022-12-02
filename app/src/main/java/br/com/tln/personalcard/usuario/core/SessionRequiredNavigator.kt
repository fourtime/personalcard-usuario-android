package br.com.tln.personalcard.usuario.core

interface SessionRequiredNavigator : Navigator {
    fun navigateToPreLogin()

    fun navigateToInitialization()

    fun navigateToLogin(cpf: String, registerEmail: Boolean)

    fun navigateToCreatePassword()

    fun navigateToRegisterEmail()

    fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean)
}