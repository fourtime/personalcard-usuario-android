package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymentDataResponse(
    @Json(name = "condicao") val condition: String,
    @Json(name = "dataHoraTransacao") val transactionTime: String,
    @Json(name = "endereco") val address: String,
    @Json(name = "estabelecimento") val establishment: String,
    @Json(name = "valor") val value: Float
)