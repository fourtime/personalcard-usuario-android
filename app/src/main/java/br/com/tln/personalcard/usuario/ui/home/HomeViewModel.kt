package br.com.tln.personalcard.usuario.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import arrow.core.Either
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.Event
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.Profile
import br.com.tln.personalcard.usuario.extensions.initials
import br.com.tln.personalcard.usuario.extensions.observeEvent
import br.com.tln.personalcard.usuario.model.CardStatement
import br.com.tln.personalcard.usuario.model.Transaction
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.CardRepository
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.repository.UserRepository
import br.com.tln.personalcard.usuario.task.RenewUserDataTask
import br.com.tln.personalcard.usuario.webservice.request.CardStatementRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val renewUserDataTask: RenewUserDataTask,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) : SessionRequiredBaseViewModel<HomeNavigator>(
    sessionRepository = sessionRepository
) {

    private val initializationDataSourceLiveData by lazy {
        sessionRepository.getInitializationDataLiveData()
    }

    val profileSourceLiveData by lazy {
        sessionRepository.getProfileLiveData()
    }

    val hasActiveSession: LiveData<Boolean> =
        Transformations.map(sessionRepository.getSessionLiveData()) {
            it?.initializationData != null && it.account != null && !it.initializationData.firstAccess && it.initializationData.hasEmail && !it.account.accountData.locked
        }
    /* User Data */
    val nameInitials: LiveData<String> =
        Transformations.map(initializationDataSourceLiveData) {
            it?.name?.initials()
        }
    val name: LiveData<String> =
        Transformations.map(initializationDataSourceLiveData) {
            it?.name
        }
    val email: LiveData<String> = Transformations.map(profileSourceLiveData) {
        it?.email
    }
    val hasPicture: LiveData<Boolean?> = Transformations.map(profileSourceLiveData) {
        it?.picture?.isNotEmpty()
    }
    val picture: LiveData<String?> = Transformations.map(profileSourceLiveData) {
        it?.picture
    }

    /* Cards */
    private var currentCard: Card? = null

    /* Card Statement */
    private val _cardStatementLiveData = MutableLiveData<Resource<CardStatement, Nothing?>>()

    private val _cardStatementHasContent: MutableLiveData<Boolean> = MutableLiveData()
    private val _loadingCardStatement: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also {
        it.value = false
    }

    private val _cardStatementSelectorSelectedOption = MutableLiveData<Int>().also {
        it.value = 1
    }
    val cardStatementSelectorSelectedOption: LiveData<Int> get() = _cardStatementSelectorSelectedOption

    private var selectedCardStatementInitialDate = LocalDate.now().minusDays(15).atStartOfDay()
    private val selectedCardStatementFinalDate = LocalDate.now().atStartOfDay()

    val loadingCardStatement: LiveData<Boolean> get() = _loadingCardStatement
    val cardStatementHasContent: LiveData<Boolean> get() = _cardStatementHasContent

    private var cancelCurrentCardStatementLoad: () -> Unit = {}

    /* PagedList */
    private val _pagedListLiveData = MutableLiveData<LiveData<PagedList<Transaction>>>()

    val pagedListLiveData: LiveData<LiveData<PagedList<Transaction>>> get() = _pagedListLiveData

    override fun onCleared() {
        renewUserDataTask.stop()
    }

    override fun onSessionNotFound() {
        renewUserDataTask.stop()
    }

    override fun onSessionFound() {
        renewUserDataTask.start()
    }

    fun getCardsLiveData(): LiveData<Resource<List<Card>, Nothing?>> {
        val liveData = MutableLiveData<Resource<List<Card>, Nothing?>>()
        liveData.value = LoadingResource()

        viewModelScope.launch {
            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            liveData.value = SuccessResource(data = account.cards)
        }

        return liveData
    }

    fun getCardStatementLiveData(): LiveData<Resource<CardStatement, Nothing?>> {
        return _cardStatementLiveData
    }

    fun loadCardStatamentSwipeRefresh(){
        val card = getCurrentCard() ?: return
        loadPagedCardStatement(card)
        updateCardBalance()
    }

    fun loadCardStatement(card: Card) {
        if (this.currentCard?.id != card.id) {
            this.currentCard = card
            loadPagedCardStatement(card)
        }
    }

    fun getCurrentCard(): Card? {
        return currentCard
    }

    private fun loadPagedCardStatement(card: Card) {
        val listConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(30)
            .setPageSize(10)
            .setPrefetchDistance(10)
            .build()

        val builder = LivePagedListBuilder(StatementDataSource.Factory(this, card), listConfig)
            .build()

        _pagedListLiveData.postValue(builder)
    }

    fun loadCardStatementTransactions(
        card: Card,
        pageNumber: Int,
        pageSize: Int,
        callback: (cardStatement: CardStatement) -> Unit
    ) {
        cancelCurrentCardStatementLoad()

        val job = viewModelScope.launch {

            val account = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()
            } ?: return@launch

            val request = CardStatementRequest(
                cardId = card.id,
                type = card.type.id,
                from = selectedCardStatementInitialDate.toString(),
                to = selectedCardStatementFinalDate.toString(),
                pageNumber = pageNumber,
                pageSize = pageSize
            )

            val result = cardRepository.getStatement(
                accessToken = account.accessToken,
                request = request
            )

            val onComplete = {
                cancelCurrentCardStatementLoad = {}
            }

            when (result) {
                is Either.Left -> {
                    onGetStatementDataFailure(throwable = result.a, onComplete = onComplete)
                }
                is Either.Right -> {
                    onGetStatementDataSuccess(resource = result.b, onComplete = onComplete)

                    when (val resource: Resource<CardStatement, String?> = result.b) {
                        is SuccessResource -> {
                            callback(resource.data)
                        } else -> {}
                    }

                }
            }
        }

        cancelCurrentCardStatementLoad = {
            job.cancel()
        }
    }

    private fun onGetStatementDataFailure(throwable: Throwable, onComplete: () -> Unit) {
        errorNotifier.notify(throwable)
        _cardStatementLiveData.value = ErrorResource(data = null)
        _loadingCardStatement.value = false

        onComplete()
    }

    private fun onGetStatementDataSuccess(
        resource: Resource<CardStatement, String?>,
        onComplete: () -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onGetStatementLoading()
            }
            is SuccessResource -> {
                onGetStatementSuccess(statement = resource.data)
                onComplete()
            }
            is ErrorResource -> {
                onGetStatementError(resource.data)
                onComplete()
            }
        }
    }

    private fun onGetStatementLoading() {
        _cardStatementLiveData.value = LoadingResource()
        _loadingCardStatement.value = true
    }

    private fun onGetStatementSuccess(statement: CardStatement) {
        _cardStatementHasContent.value = statement.transactions.isNotEmpty()
        _cardStatementLiveData.value = SuccessResource(data = statement)
        _loadingCardStatement.value = false
    }

    private fun onGetStatementError(errorMessage: String?) {
        _cardStatementLiveData.value = ErrorResource(data = null)

        if (errorMessage.isNullOrEmpty()) {
            _unknownErrorLiveData.postValue(Event(data = null))
        } else {
            _errorMessageLiveData.postValue(Event(data = errorMessage))
        }
        
        _loadingCardStatement.value = false
    }

    fun accreditedNetworkClicked() {
        val card = currentCard ?: return
        val label =
            resourceProvider.getString(R.string.toolbar_title_accredited_network, card.label)

        navigator?.navigateToAccreditedNetwork(label = label, card = card)
    }

    fun payWithQrCodeClicked() {
        val card = currentCard ?: return

        navigator?.navigateToPayWithQrCode(card = card)
    }

    fun statementSelectorOption1Clicked() {
        _cardStatementSelectorSelectedOption.value = 1
        selectedCardStatementInitialDate = LocalDate.now().minusDays(15).atStartOfDay()
        val card = currentCard ?: return
        loadPagedCardStatement(card)
    }

    fun statementSelectorOption2Clicked() {
        _cardStatementSelectorSelectedOption.value = 2
        selectedCardStatementInitialDate = LocalDate.now().minusDays(30).atStartOfDay()
        val card = currentCard ?: return
        loadPagedCardStatement(card)
    }

    fun statementSelectorOption3Clicked() {
        _cardStatementSelectorSelectedOption.value = 3
        selectedCardStatementInitialDate = LocalDate.now().minusDays(90).atStartOfDay()
        val card = currentCard ?: return
        loadPagedCardStatement(card)
    }

    fun updateCardBalance(): LiveData<Resource<Nothing?, Nothing?>>? {

        val liveData = MutableLiveData<Resource<Nothing?, Nothing?>>()

        viewModelScope.launch {
            liveData.value = LoadingResource()

            val result = withContext(Dispatchers.IO) {
                sessionRepository.getAccount()?.let { account ->
                    userRepository.updateCards(
                        accessToken = account.accessToken
                    )
                } ?: null
            } ?: return@launch

            when (result) {
                is Either.Left -> {
                    onUpdateCardBalanceFailure(result.a) { value ->
                        liveData.value = value
                    }
                }
                is Either.Right -> {
                    onUpdateCardBalanceSuccess(result.b) { value ->
                        liveData.value = value
                    }
                }
            }
        }

        return liveData
    }

    private fun onUpdateCardBalanceFailure(
        throwable: Throwable,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
        errorNotifier.notify(throwable)
    }

    private fun onUpdateCardBalanceSuccess(
        resource: Resource<Nothing?, Nothing?>,
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onUpdateCardBalanceLoading(valueListener = valueListener)
            }
            is SuccessResource -> {
                onUpdateCardBalanceSuccess(
                    valueListener = valueListener
                )
            }
            is ErrorResource -> {
                onConfirmPaymentBalanceError(
                    valueListener = valueListener
                )
            }
        }
    }

    private fun onUpdateCardBalanceLoading(valueListener: (Resource<Nothing?, Nothing?>) -> Unit) {
        valueListener(LoadingResource())
    }

    private fun onUpdateCardBalanceSuccess(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(SuccessResource(data = null))
    }

    private fun onConfirmPaymentBalanceError(
        valueListener: (Resource<Nothing?, Nothing?>) -> Unit
    ) {
        valueListener(ErrorResource(data = null))
    }
}