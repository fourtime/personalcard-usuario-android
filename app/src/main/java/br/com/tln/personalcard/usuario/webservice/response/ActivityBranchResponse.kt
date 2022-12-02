package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ActivityBranchResponse(
    @Json(name = "results") val activityBranchs: String
)