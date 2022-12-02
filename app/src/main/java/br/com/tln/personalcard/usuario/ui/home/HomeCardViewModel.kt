package br.com.tln.personalcard.usuario.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseViewModel
import br.com.tln.personalcard.usuario.entity.CardType
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.CardRepository
import org.threeten.bp.LocalDate
import java.math.BigDecimal
import javax.inject.Inject

class HomeCardViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val resourceProvider: ResourceProvider
) :
    BaseViewModel<HomeCardNavigator>() {
    private val cardId = MutableLiveData<String>()

    private var card = Transformations.switchMap(cardId) {
        cardRepository.getCard(it)
    }

    val lastDigits: LiveData<String> =
        Transformations.map(card) {
            it?.lastDigits
        }

    val label: LiveData<String> =
        Transformations.map(card) {
            it?.label
        }

    val balance: LiveData<BigDecimal> =
        Transformations.map(card) {
            it?.balance?.toBigDecimal()
        }

    val nextRecharge: LiveData<LocalDate?> =
            Transformations.map(card) {
                if (it?.type?.id == CardType.PRE_PAID.id) {
                    it?.rechargeDate?.toLocalDate()
                } else {
                    it?.renovationDate?.toLocalDate()
                }
            }

    val cardRechargeType: LiveData<String> =
        Transformations.map(card) {
            if (it?.type?.id == CardType.PRE_PAID.id) {
                resourceProvider.getString(R.string.home_card_next_recharge)
            } else {
                resourceProvider.getString(R.string.home_card_next_renovation)
            }
        }

    val averageDailyBalance: LiveData<BigDecimal> =
        Transformations.map(card) {
            it?.averangeDailyBalance?.toBigDecimal()
        }

    val hasNextRecharge: LiveData<Boolean?> = Transformations.map(card) {
        it?.rechargeDate != null || it?.renovationDate != null
    }

    fun setCard(cardId: String) {
        this.cardId.value = cardId
    }
}