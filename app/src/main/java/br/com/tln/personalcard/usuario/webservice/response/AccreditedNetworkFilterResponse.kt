package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AccreditedNetworkFilterResponse(
    @Json(name = "codigo") val code: String,
    @Json(name = "complemento") val complement: String,
    @Json(name = "endereco") val address: String,
    @Json(name = "latitude") val latitude: String,
    @Json(name = "longitude") val longitude: String,
    @Json(name = "cidade") val city: String,
    @Json(name = "bairro") val neighborhood: String,
    @Json(name = "nomeFantasia") val fantasyName: String,
    @Json(name = "telefone") val phone: String?,
    @Json(name = "uf") val uf: String
)