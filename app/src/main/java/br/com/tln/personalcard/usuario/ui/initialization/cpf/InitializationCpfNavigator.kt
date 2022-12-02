package br.com.tln.personalcard.usuario.ui.initialization.cpf

import br.com.tln.personalcard.usuario.core.Navigator

interface InitializationCpfNavigator : Navigator {

    fun navigateToInitializationCardLastDigits(cpf: String)
}