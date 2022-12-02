package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class InitializationResponse(
    @Json(name = "nomeUsuario") val userName: String,
    @Json(name = "primeiroAcesso") val firstAccess: Boolean,
    @Json(name = "temEmail") val hasEmail: Boolean,
    @Json(name = "temFoto") val hasPhoto: Boolean
)