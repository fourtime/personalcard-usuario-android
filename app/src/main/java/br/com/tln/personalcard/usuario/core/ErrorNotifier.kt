package br.com.tln.personalcard.usuario.core

import androidx.lifecycle.MutableLiveData

class ErrorNotifier(
    private val fallbackHandler: MutableLiveData<Event<Nothing?>>,
    private val errorHandlersMapping: Map<Class<out Throwable>, List<MutableLiveData<Event<Nothing?>>>>
) {
    fun notify(throwable: Throwable) {

        errorHandlersMapping[throwable::class.java]?.find { handler ->
            handler.hasObservers()
        }?.let { handler ->
            handler.postValue(Event(null))
            return
        }

        fallbackHandler.postValue(Event(null))
    }
}