package br.com.tln.personalcard.usuario.model

import br.com.tln.personalcard.usuario.entity.CardType
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

data class CardStatement(
        val pageNumber: Long,
        val pageSize: Long,
        val totalSize: Long,
        val rechargeDate: LocalDateTime?,
        val renovationDate: LocalDateTime?,
        var rechargeValue: BigDecimal,
        val limitValue: BigDecimal,
        val transactions: List<Transaction>
)

data class Transaction(
    val cardId: String,
    val cardLastDigits: String,
    val label: String,
    val value: BigDecimal,
    val instalments: Int,
    val cardType: CardType,
    val nsuHost: String,
    val nsuAuthorization: String,
    val time: LocalDateTime
)