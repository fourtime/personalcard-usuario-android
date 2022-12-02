package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AccessTokenResponse(
    @Json(name = "scope") val scope: String,
    @Json(name = "token_type") val type: String,
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Long
)