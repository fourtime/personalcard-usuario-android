package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SegmentResponse(
    @Json(name = "nome") val name: String,
    @Json(name = "tipo") val type: Int
)