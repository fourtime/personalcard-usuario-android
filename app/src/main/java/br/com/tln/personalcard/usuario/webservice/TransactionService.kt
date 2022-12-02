package br.com.tln.personalcard.usuario.webservice

import br.com.tln.personalcard.usuario.webservice.request.ConfirmPaymentRequest
import br.com.tln.personalcard.usuario.webservice.response.ConfirmPaymentResponse
import br.com.tln.personalcard.usuario.webservice.response.TelenetBaseResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TransactionService {

    @POST("services/transacao/cartao/pagamentoviatoken")
    suspend fun confirmPaymennt(
        @Header("Authorization") applicationToken: String,
        @Body confirmPaymentRequest: ConfirmPaymentRequest
    ): TelenetBaseResponse<ConfirmPaymentResponse>
}