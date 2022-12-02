package br.com.tln.personalcard.usuario.webservice.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class CardStatementResponse(
    @Json(name = "numeroPagina") val pageNumber: Long,
    @Json(name = "tamanhoPagina") val pageSize: Long,
    @Json(name = "totalRegistros") val totalRegister: Long,
    @Json(name = "dataProgramadaCarga") val rechargeDate: String?,
    @Json(name = "dataRenovacao") val renovationDate: String?,
    @Json(name = "valorProgramadoCarga") val rechargeValue: BigDecimal,
    @Json(name = "valorLimite") val limitValue: BigDecimal,
    @Json(name = "dados") val transactions: List<TransactionResponse>
)
