package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccreditedNetworkResponse(
    @Json(name = "codigo") val id: String,
    @Json(name = "nomeFantasia") val fantasyName: String,
    @Json(name = "endereco") val address: String,
    @Json(name = "complemento") val complement: String,
    @Json(name = "cidade") val city: String,
    @Json(name = "bairro") val neighborhood: String,
    @Json(name = "uf") val uf: String,
    @Json(name = "latitude") val latitude: String,
    @Json(name = "longitude") val longitude: String,
    @Json(name = "telefone") val phone: String
)