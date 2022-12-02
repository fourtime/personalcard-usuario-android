package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AccreditedNetworkGeoLocationResponse(
    @Json(name = "codigo") val code: String,
    @Json(name = "latitude") val latitude: String,
    @Json(name = "longitude") val longitude: String
)
