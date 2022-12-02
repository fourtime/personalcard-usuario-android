package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CardResponse(
        @Json(name = "dataRenovacao") val renovationDate: String?,
        @Json(name = "dataProgramadaCarga") val rechargeDate: String?,
        @Json(name = "dependentes") val dependents: List<Any>,
        @Json(name = "idCartao") val idCard: String,
        @Json(name = "nomeTitular") val cardHolderName: String,
        @Json(name = "rotulo") val label: String,
        @Json(name = "saldo") val balance: Float,
        @Json(name = "saldoSegmentos") val balanceSegments: List<Any>,
        @Json(name = "tipoCartao") val type: Int,
        @Json(name = "ultimosDigitosCartao") val cardLastDigits: String,
        @Json(name = "valorProgramadoCarga") val scheduledLoadvalue: Float,
        @Json(name = "valorLimite") val limitValue: Float
)