package br.com.tln.personalcard.usuario.webservice.request

import br.com.tln.personalcard.usuario.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SettingInstanceRequest(
    @Json(name = "imei") val imei: String,
    @Json(name = "IdInstancia") val instanceId: String,
    @Json(name = "versao") val version: Int = BuildConfig.VERSION_CODE,
    @Json(name = "plataforma") val platform: String = "android"
)