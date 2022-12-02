package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class TelenetBaseResponse<T>(
    @Json(name = "status") val status: Int,
    @Json(name = "results") val results: T
)