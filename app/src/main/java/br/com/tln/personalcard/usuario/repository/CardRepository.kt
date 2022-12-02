package br.com.tln.personalcard.usuario.repository

import androidx.lifecycle.LiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.db.CardDao
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.CardType
import br.com.tln.personalcard.usuario.exception.ConnectionErrorException
import br.com.tln.personalcard.usuario.exception.InvalidAuthenticationException
import br.com.tln.personalcard.usuario.extensions.toLocalDateTime
import br.com.tln.personalcard.usuario.model.CardStatement
import br.com.tln.personalcard.usuario.model.Transaction
import br.com.tln.personalcard.usuario.webservice.PaymentDataService
import br.com.tln.personalcard.usuario.webservice.request.CardStatementRequest
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val paymentDataService: PaymentDataService,
    private val cardDao: CardDao
) {

    suspend fun getStatement(
        request: CardStatementRequest,
        accessToken: AccessToken
    ): Either<Throwable, Resource<CardStatement, String?>> {

        return statementRequest(
            applicationToken = accessToken.formattedToken,
            cardStatementRequest = request
        )
    }

    private suspend fun statementRequest(
        cardStatementRequest: CardStatementRequest,
        applicationToken: String
    ): Either<Throwable, Resource<CardStatement, String?>> {

        val response = try {
            paymentDataService.getStatement(
                applicationToken = applicationToken,
                cardStatementRequest = cardStatementRequest
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: KotlinNullPointerException) {
            return Either.left(InvalidAuthenticationException())
        } catch (ex: HttpException) {
            return if (ex.code() == 400) {
                Either.left(InvalidAuthenticationException())
            } else {
                Either.right(ErrorResource(null as String?))
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


        val transaction = response.results.transactions
        val transactions = transaction.map {

            var type: CardType? = CardType.fromId(it.cardType)
            if (type == null) {
                type = CardType.FLEET
            }

            Transaction(
                cardId = it.cardId,
                cardLastDigits = it.cardLastDigits,
                label = it.establishment,
                value = it.value,
                instalments = it.instalments,
                cardType = type,
                nsuHost = it.nsuHost,
                nsuAuthorization = it.nsuAuthorization,
                time = it.time.toLocalDateTime()
            )
        }

        val card = response.results
        val cardStatement = CardStatement(
            pageNumber = card.pageNumber,
            pageSize = card.pageSize,
            totalSize = card.totalRegister,
            rechargeDate = card.rechargeDate?.toLocalDateTime(),
            renovationDate = card.renovationDate?.toLocalDateTime(),
            rechargeValue = card.rechargeValue,
            limitValue = card.limitValue,
            transactions = transactions
        )

        return Either.right(SuccessResource(cardStatement))
    }

    fun getCard(cardId: String): LiveData<Card> {
        return cardDao.getCard(cardId)
    }
}