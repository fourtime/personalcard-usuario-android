package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccreditedNetworkDetailRequest(
    @Json(name = "codigo") val code: String
)