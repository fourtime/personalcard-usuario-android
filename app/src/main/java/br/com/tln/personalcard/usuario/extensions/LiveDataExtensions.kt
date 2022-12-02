package br.com.tln.personalcard.usuario.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit = {}) {
    observe(owner, Observer {
        block(it)
    })
}

fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, block: (Event<T>) -> Unit = {}) {
    observe(owner) { event ->
        if (event.handled) {
            return@observe
        }

        event.handled = true

        block(event)
    }
}

fun <S, E> LiveData<Resource<S, E>>.observeResource(
    owner: LifecycleOwner,
    block: (Resource<S, E>) -> Unit = {}
) {
    val observer = object : Observer<Resource<S, E>> {

        override fun onChanged(resource: Resource<S, E>) {
            val x = when (resource) {
                is SuccessResource, is ErrorResource -> {
                    removeObserver(this)
                }
                is LoadingResource -> {
                }
            }

            block(resource)
        }
    }

    observe(owner, observer)
}