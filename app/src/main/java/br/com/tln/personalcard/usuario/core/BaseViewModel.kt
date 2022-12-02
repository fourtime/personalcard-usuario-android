package br.com.tln.personalcard.usuario.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.lang.ref.WeakReference

abstract class BaseViewModel<T : Navigator> : ViewModel() {

    protected val viewModelJob = Job()

    protected val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    protected val _connectionErrorLiveData = MutableLiveData<Event<Nothing?>>()
    protected val _unknownErrorLiveData = MutableLiveData<Event<Nothing?>>()
    protected val _errorMessageLiveData = MutableLiveData<Event<String>>()


    private var _navigator = WeakReference<T?>(null)

    var navigator: T?
        get() = _navigator.get()
        set(value) {
            _navigator = WeakReference(value)
        }

    protected val errorNotifier: ErrorNotifier by lazy {
        val errorHandlerMap: LinkedHashMap<Class<out Throwable>, List<MutableLiveData<Event<Nothing?>>>> =
            linkedMapOf(
                ConnectionErrorException::class.java to listOf(
                    _connectionErrorLiveData
                )
            )

        errorHandlerMap.putAll(getAdditionalErrorHandlersMapping())

        ErrorNotifier(
            fallbackHandler = _unknownErrorLiveData,
            errorHandlersMapping = errorHandlerMap.toMap()
        )
    }

    val connectionErrorLiveData: LiveData<Event<Nothing?>> = _connectionErrorLiveData
    val unknownErrorLiveData: LiveData<Event<Nothing?>> = _unknownErrorLiveData
    val errorMessageLiveData: LiveData<Event<String>> = _errorMessageLiveData

    protected open fun getAdditionalErrorHandlersMapping(): LinkedHashMap<Class<out Throwable>, List<MutableLiveData<Event<Nothing?>>>> {
        return linkedMapOf()
    }

    override fun onCleared() {
        viewModelJob.cancel()
    }
}