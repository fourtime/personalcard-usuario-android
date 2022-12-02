package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InitializationRequest(
    @Json(name = "codigoOperadora") val operatorCode: String,
    @Json(name = "cpf") val cpf: String,
    @Json(name = "ultimosDigitosCartao") val cardLastDigits: String,
    @Json(name = "imei") val deviceId: String
)