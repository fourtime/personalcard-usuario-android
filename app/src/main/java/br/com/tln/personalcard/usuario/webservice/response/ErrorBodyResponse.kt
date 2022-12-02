package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorBodyResponse(
    @Json(name = "details") val details: List<String>,
    @Json(name = "moreinfo") val moreinfo: String?,
    @Json(name = "status") val status: Int
) {

    val message: String?
        get() = details.firstOrNull()
}