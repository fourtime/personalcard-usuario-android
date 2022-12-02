package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivityBranchRequest(
    @Json(name = "idCartao") val idCard: String,
    @Json(name = "tipoCartao") val type: Int,
    @Json(name = "segmento") val segment: String
)