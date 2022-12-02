package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "senhaAtual") val currentPassword: String,
    @Json(name = "novaSenha") val newPassword: String
)