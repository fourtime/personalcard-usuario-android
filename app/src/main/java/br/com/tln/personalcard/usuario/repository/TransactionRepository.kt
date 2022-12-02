package br.com.tln.personalcard.usuario.repository

import arrow.core.Either
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.extensions.responseError
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import br.com.tln.personalcard.usuario.model.ConfirmPayment
import br.com.tln.personalcard.usuario.model.PaymentData
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.webservice.PaymentDataService
import br.com.tln.personalcard.usuario.webservice.TransactionService
import br.com.tln.personalcard.usuario.webservice.request.ConfirmPaymentRequest
import br.com.tln.personalcard.usuario.webservice.request.PaymentDataRequest
import br.com.tln.personalcard.usuario.webservice.response.ErrorBodyResponse
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val paymentDataService: PaymentDataService,
    private val transactionService: TransactionService,
    private val resourceProvider: ResourceProvider,
    private val moshi: Moshi
) {

    suspend fun paymentData(
        request: PaymentDataRequest,
        accessToken: AccessToken,
        paymentToken: String
    ): Either<Throwable, Resource<PaymentData, String?>> {

        val applicationToken = accessToken.formattedToken

        return paymentDataRequest(
            paymentDataRequest = request,
            applicationToken = applicationToken,
            paymentToken = paymentToken
        )
    }

    suspend fun confirmPayment(
        request: ConfirmPaymentRequest,
        accessToken: AccessToken
    ): Either<Throwable, Resource<ConfirmPayment, String?>> {

        val applicationToken = accessToken.formattedToken

        return confirmPaymentRequest(
            confirmPaymentRequest = request,
            applicationToken = applicationToken
        )
    }

    private suspend fun paymentDataRequest(
        paymentDataRequest: PaymentDataRequest,
        applicationToken: String,
        paymentToken: String
    ): Either<Throwable, Resource<PaymentData, String?>> {

        val response = try {
            paymentDataService.getPaymentData(
                applicationToken = applicationToken,
                paymentDataRequest = paymentDataRequest
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
            }

        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val paymentData = response.results
        val responsePaymentData = PaymentData(
            condition = paymentData.condition,
            establishment = paymentData.establishment,
            address = paymentData.address,
            transactionTime = paymentData.transactionTime.toLocalDateTime(),
            value = paymentData.value.toBigDecimal(),
            token = paymentToken
        )

        return Either.right(SuccessResource(responsePaymentData))
    }

    private suspend fun confirmPaymentRequest(
        confirmPaymentRequest: ConfirmPaymentRequest,
        applicationToken: String
    ): Either<Throwable, Resource<ConfirmPayment, String?>> {

        val response = try {
            transactionService.confirmPaymennt(
                applicationToken = applicationToken,
                confirmPaymentRequest = confirmPaymentRequest
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {

            val errorBodyResponse: ErrorBodyResponse? = moshi.responseError(ex.response())

            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else if (ex.code() == 409 && errorBodyResponse != null) {
                Either.right(ErrorResource(errorBodyResponse.message))
            } else {
                Either.right(ErrorResource(data = null as String?))
            }

        } catch (ex: TimeoutException) {

            val errorMessage: String? = resourceProvider.getString(R.string.transaction_timeout_error_message)

            return Either.right(ErrorResource(data = errorMessage))

        } catch (ex: EOFException) {
            return Either.left(ex)
        } catch (ex: JsonEncodingException) {
            return Either.left(ex)
        } catch (ex: IOException) {
            return Either.left(ConnectionErrorException(ex))
        } catch (t: Throwable) {
            return Either.left(t)
        }

        val paymentDataResponse = response.results
        val responseConfirmPayment = ConfirmPayment(
            transactionTime = paymentDataResponse.transactionTime.toLocalDateTime(),
            nsuAuthorization = paymentDataResponse.nsuAuthorization,
            nsuHost = paymentDataResponse.nsuHost,
            carrierName = paymentDataResponse.carrierName,
            balanceAfterTransaction = paymentDataResponse.balanceAfterTransaction
        )

        return Either.right(SuccessResource(responseConfirmPayment))
    }
}