package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListResponse<T>(
    @Json(name = "numeroPagina") val pageNumber: Long,
    @Json(name = "tamanhoPagina") val pageSize: Long,
    @Json(name = "totalRegistros") val totalSize: Long,
    @Json(name = "dados") val list: List<T>
)