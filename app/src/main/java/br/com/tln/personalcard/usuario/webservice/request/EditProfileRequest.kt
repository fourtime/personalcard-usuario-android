package br.com.tln.personalcard.usuario.webservice.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditProfileRequest(
    @Json(name = "dataNascimento") val birthDate: String,
    @Json(name = "email") val email: String,
    @Json(name = "telefone") val phone: String
)