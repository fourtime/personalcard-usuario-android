package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmPaymentResponse(
    @Json(name = "dataHoraAutorizacao") val transactionTime: String,
    @Json(name = "nsuAutorizacao") val nsuAuthorization: String,
    @Json(name = "nsuHost") val nsuHost: String,
    @Json(name = "nomePortador") val carrierName: String,
    @Json(name = "saldoAposTransacao") val balanceAfterTransaction: Float
)