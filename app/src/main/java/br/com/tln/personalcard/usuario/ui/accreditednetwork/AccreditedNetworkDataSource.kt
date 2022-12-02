package br.com.tln.personalcard.usuario.ui.accreditednetwork

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.AccreditedNetworkFilter

class AccreditedNetworkDataSource(
    private val viewModel: AccreditedNetworkFilterResultsViewModel,
    private val accreditedNetworkFilter: AccreditedNetworkFilter
) :
    PositionalDataSource<AccreditedNetwork>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<AccreditedNetwork>
    ) {

        viewModel.loadPagedAccreditedNetwork(
            pageNumber = 1,
            pageSize = params.pageSize,
            accreditedNetworkFilter = accreditedNetworkFilter
        ) {
            callback.onResult(it.list, 0)
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<AccreditedNetwork>
    ) {
        val nextPage: Int
        val startPosition = params.startPosition
        val loadSize = params.loadSize

        val hasNextPage = startPosition % loadSize == 0

        if (hasNextPage) {
            nextPage = startPosition / loadSize
        } else {
            return
        }

        viewModel.loadPagedAccreditedNetwork(
            pageNumber = nextPage,
            pageSize = params.loadSize,
            accreditedNetworkFilter = accreditedNetworkFilter
        ) {
            callback.onResult(it.list)
        }
    }

    class Factory(
        val viewModel: AccreditedNetworkFilterResultsViewModel,
        val accreditedNetworkFilter: AccreditedNetworkFilter
    ) : DataSource.Factory<Int, AccreditedNetwork>() {
        override fun create(): DataSource<Int, AccreditedNetwork> {
            return AccreditedNetworkDataSource(
                viewModel = viewModel,
                accreditedNetworkFilter = accreditedNetworkFilter
            )
        }
    }

}