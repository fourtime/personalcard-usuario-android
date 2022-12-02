package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryPasswordRequest(
    @Json(name = "codigoOperadora") val operatorCode: Int,
    @Json(name = "CPF") val cpf: String,
    @Json(name = "Imei") val imei: String
)