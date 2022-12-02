package br.com.tln.personalcard.usuario.ui.home

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.model.Transaction

class StatementDataSource(private val viewModel: HomeViewModel, private val card: Card) :
    PositionalDataSource<Transaction>() {

    companion object {
        private const val FIRST_PAGE = 1
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Transaction>
    ) {

        viewModel.loadCardStatementTransactions(
            card = card,
            pageNumber = FIRST_PAGE,
            pageSize = params.pageSize
        ) {
            callback.onResult(it.transactions, 0)
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Transaction>
    ) {
        val nextPage: Int
        val startPosition = params.startPosition
        val loadSize = params.loadSize

        val hasNextPage = startPosition % loadSize == 0

        if (hasNextPage) {
            nextPage = (startPosition / loadSize) + FIRST_PAGE
        } else {
            return
        }

        viewModel.loadCardStatementTransactions(
            card = card,
            pageNumber = nextPage,
            pageSize = params.loadSize
        ) {
            callback.onResult(it.transactions)
        }
    }

    class Factory(val viewModel: HomeViewModel, val card: Card) :
        DataSource.Factory<Int, Transaction>() {

        override fun create(): DataSource<Int, Transaction> {
            return StatementDataSource(viewModel = viewModel, card = card)
        }
    }
}