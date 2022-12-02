package br.com.tln.personalcard.usuario.core

sealed class Resource<S, E>

data class LoadingResource<S, E>(val message: String? = null) : Resource<S, E>()

data class SuccessResource<S, E>(val data: S) : Resource<S, E>()

data class ErrorResource<S, E>(val data: E) : Resource<S, E>()

fun <S, E> S.successResource(): SuccessResource<S, E> {
    return SuccessResource(data = this)
}

fun <S, E> E.errorResource(): ErrorResource<S, E> {
    return ErrorResource(data = this)
}