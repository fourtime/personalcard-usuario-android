package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ProfileResponse(
    @Json(name = "bairro") val neighborhood: String,
    @Json(name = "cep") val cep: String,
    @Json(name = "cidade") val city: String,
    @Json(name = "complemento") val complement: String,
    @Json(name = "dataNascimento") val birthDate: String?,
    @Json(name = "email") val email: String,
    @Json(name = "endereco") val address: String,
    @Json(name = "nome") val name: String,
    @Json(name = "numero") val numero: String,
    @Json(name = "telefone") val phone: String,
    @Json(name = "uf") val uf: String
)