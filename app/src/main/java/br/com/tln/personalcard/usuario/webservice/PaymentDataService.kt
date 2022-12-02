package br.com.tln.personalcard.usuario.webservice

import br.com.tln.personalcard.usuario.webservice.request.CardStatementRequest
import br.com.tln.personalcard.usuario.webservice.request.PaymentDataRequest
import br.com.tln.personalcard.usuario.webservice.response.CardStatementResponse
import br.com.tln.personalcard.usuario.webservice.response.PaymentDataResponse
import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymentDataService {

    @POST("services/transacao/cartao/identificatopkenpagamento")
    suspend fun getPaymentData(
        @Header("Authorization") applicationToken: String,
        @Body paymentDataRequest: PaymentDataRequest
    ): TelenetBaseResponse<PaymentDataResponse>

    @POST("services/consulta/cartao/extrato")
    suspend fun getStatement(
        @Header("Authorization") applicationToken: String,
        @Body cardStatementRequest: CardStatementRequest
    ): TelenetBaseResponse<CardStatementResponse>
}