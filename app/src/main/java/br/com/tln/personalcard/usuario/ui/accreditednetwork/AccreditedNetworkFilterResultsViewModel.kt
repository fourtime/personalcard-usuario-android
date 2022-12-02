package br.com.tln.personalcard.usuario.ui.accreditednetwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.AccreditedNetworkFilter
import br.com.tln.personalcard.usuario.provider.ResourceProvider
import br.com.tln.personalcard.usuario.repository.AccreditedNetworkRepository
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkFilterRequest
import br.com.tln.personalcard.usuario.webservice.response.ListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AccreditedNetworkFilterResultsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val accreditedNetworkRepository: AccreditedNetworkRepository,
    private val resourceProvider: ResourceProvider
) : SessionRequiredBaseViewModel<AccreditedNetworkFilterResultsNavigator>(
    sessionRepository = sessionRepository
) {

    private val _accreditedNetworkFilterLiveData =
        MediatorLiveData<Resource<List<AccreditedNetwork>, Nothing?>>()
    private val _selectedAccreditedNetwork = MutableLiveData<AccreditedNetwork>()

    private val _accreditedNetworkHasContent: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().also {
            it.value = true
        }
    private val _loadingAccreditedNetwork: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().also {
            it.value = false
        }

    val accreditedNetworkHasContent: LiveData<Boolean> get() = _accreditedNetworkHasContent
    val loadingAccreditedNetwork: LiveData<Boolean> get() = _loadingAccreditedNetwork

    val accreditedNetworkLiveData: MediatorLiveData<Resource<List<AccreditedNetwork>, Nothing?>> get() = _accreditedNetworkFilterLiveData

    val selectedAccreditedNetwork: LiveData<AccreditedNetwork> by lazy {
        val liveData = MediatorLiveData<AccreditedNetwork>()

        liveData.addSource(_selectedAccreditedNetwork) {
            liveData.value = it
        }
        liveData
    }

    /* PagedList */
    private val _pagedListLiveData = MutableLiveData<LiveData<PagedList<AccreditedNetwork>>>()

    val pagedListLiveData: LiveData<LiveData<PagedList<AccreditedNetwork>>> get() = _pagedListLiveData

    fun loadAccreditedNetwork(accreditedNetworkFilter: AccreditedNetworkFilter) {
        val listConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(30)
            .setPageSize(10)
            .setPrefetchDistance(10)
            .build()

        val pagedList = LivePagedListBuilder(
            AccreditedNetworkDataSource.Factory(this, accreditedNetworkFilter),
            listConfig
        )
            .build()

        _pagedListLiveData.postValue(pagedList)
    }

    fun loadPagedAccreditedNetwork(
        pageNumber: Int,
        pageSize: Int,
        accreditedNetworkFilter: AccreditedNetworkFilter,
        callback: (accreditedNetworkFilterList: ListResponse<AccreditedNetwork>) -> Unit
    ) {
        _accreditedNetworkFilterLiveData.postValue(LoadingResource())

        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = AccreditedNetworkFilterRequest(
                idCard = accreditedNetworkFilter.idCard,
                type = accreditedNetworkFilter.type,
                segment = accreditedNetworkFilter.segment,
                speciality = accreditedNetworkFilter.speciality,
                uf = accreditedNetworkFilter.uf,
                city = accreditedNetworkFilter.city,
                neighborhood = accreditedNetworkFilter.neighborhood,
                activityBranch = accreditedNetworkFilter.activityBranch,
                pageNumber = pageNumber,
                pageSize = pageSize
            )

            val result = accreditedNetworkRepository.getAccreditedNetworkFilter(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetAccreditedNetworkDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetSegmentDataSuccess(resource = result.b)

                    when (val resource: Resource<ListResponse<AccreditedNetwork>, Nothing?> =
                        result.b) {
                        is SuccessResource -> {
                            callback(resource.data)
                        } else -> {}
                    }
                }
            }
        }
    }

    private fun onGetAccreditedNetworkDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
        _loadingAccreditedNetwork.postValue(false)
        _accreditedNetworkFilterLiveData.postValue(ErrorResource(null))
    }

    private fun onGetSegmentDataSuccess(resource: Resource<ListResponse<AccreditedNetwork>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
                onGetAccreditedNetworkLoading()
            }
            is SuccessResource -> {
                onGetAccreditedNetworkSuccess(accreditedNetworkList = resource.data)
            }
            is ErrorResource -> {
                onGetSegmentError()
            }
        }
    }

    private fun onGetAccreditedNetworkLoading() {
        _accreditedNetworkFilterLiveData.postValue(LoadingResource())
        _loadingAccreditedNetwork.postValue(true)
    }

    private fun onGetAccreditedNetworkSuccess(accreditedNetworkList: ListResponse<AccreditedNetwork>) {
        _accreditedNetworkHasContent.postValue(accreditedNetworkList.list.isNotEmpty())
        _accreditedNetworkFilterLiveData.postValue(SuccessResource(data = accreditedNetworkList.list))
        _loadingAccreditedNetwork.postValue(false)
    }

    private fun onGetSegmentError() {
        Timber.w("Error while getting accredited networks")
        _accreditedNetworkFilterLiveData.postValue(ErrorResource(data = null))
        _loadingAccreditedNetwork.postValue(false)
    }

    fun accreditedNetworkSelected(accreditedNetwork: AccreditedNetwork) {
        this._selectedAccreditedNetwork.value = accreditedNetwork
        navigator?.navigateToAccreditedNetworkMap(accreditedNetwork)
    }
}