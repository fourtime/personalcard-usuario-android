package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmPaymentRequest(
    @Json(name = "cartao") val card: String,
    @Json(name = "senhaCartao") val cardPassword: String,
    @Json(name = "token") val token: String
)