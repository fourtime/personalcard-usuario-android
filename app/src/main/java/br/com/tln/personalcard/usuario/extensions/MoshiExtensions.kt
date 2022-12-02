package br.com.tln.personalcard.usuario.extensions

import com.squareup.moshi.Moshi
import retrofit2.Response

inline fun <reified T> Moshi.responseError(response: Response<out Any>?): T? {
    return response?.errorBody()?.string()?.let { errorBody ->
        return fromJson(errorBody)
    }
}

inline fun <reified T> Moshi.fromJson(string: String): T? {
    return adapter<T>(T::class.java).fromJson(string)
}

inline fun <reified T> Moshi.toJson(value: T): String {
    return adapter<T>(T::class.java).toJson(value)
}