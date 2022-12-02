package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class TransactionResponse(
    @Json(name = "estabelecimento") val establishment: String,
    @Json(name = "dataHoraTransacao") val time: String,
    @Json(name = "idCartao") val cardId: String,
    @Json(name = "ultimosDigitosCartao") val cardLastDigits: String,
    @Json(name = "valor") val value: BigDecimal,
    @Json(name = "numeroParcelas") val instalments: Int,
    @Json(name = "tipoCartao") val cardType: Int,
    @Json(name = "nsuAutorizacao") val nsuAuthorization: String,
    @Json(name = "nsuHost") val nsuHost: String
)