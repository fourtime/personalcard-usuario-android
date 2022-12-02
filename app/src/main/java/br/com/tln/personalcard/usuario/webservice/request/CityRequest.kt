package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityRequest(
    @Json(name = "idCartao") val idCard: String,
    @Json(name = "tipoCartao") val type: Int,
    @Json(name = "segmento") val segment: String,
    @Json(name = "especialidade") val specialty: String,
    @Json(name = "uf") val uf: String
)