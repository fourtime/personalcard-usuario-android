package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccreditedNetworkGeoLocationRequest(
    @Json(name = "idCartao") val id: String,
    @Json(name = "tipoCartao") val type: Int,
    @Json(name = "latitude") val latitude: String,
    @Json(name = "longitude") val longitude: String
)
