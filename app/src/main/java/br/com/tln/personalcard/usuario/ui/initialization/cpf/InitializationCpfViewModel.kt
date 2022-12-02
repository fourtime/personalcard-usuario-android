package br.com.tln.personalcard.usuario.ui.initialization.cpf

import androidx.lifecycle.MutableLiveData
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseViewModel
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import javax.inject.Inject

class InitializationCpfViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : BaseViewModel<InitializationCpfNavigator>() {

    val cpf = MutableLiveData<String>()

    fun continueClicked() {

        _errorMessageLiveData.postValue(Event(""))

        val cpf = this.cpf.value

        if (cpf.isNullOrEmpty() || cpf.length != resourceProvider.getInteger(R.integer.cpf_length)) {
            _errorMessageLiveData.postValue(Event(data = resourceProvider.getString(R.string.initialization_cpf_invalid_cpf)))
            return
        }

        navigator?.navigateToInitializationCardLastDigits(cpf)
    }
}