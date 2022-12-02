package br.com.tln.personalcard.usuario.ui.editprofile

import br.com.tln.personalcard.usuario.core.SessionRequiredNavigator

interface EditProfileNavigator : SessionRequiredNavigator {
    fun navigateBack()
}