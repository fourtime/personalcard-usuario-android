package br.com.tln.personalcard.usuario.ui.accreditednetwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.core.successResource
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.AccreditedNetworkGeoLocation
import br.com.tln.personalcard.usuario.repository.AccreditedNetworkRepository
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.webservice.request.AccreditedNetworkGeoLocationRequest
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AccreditedNetworkMapViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val accreditedNetworkRepository: AccreditedNetworkRepository
) : SessionRequiredBaseViewModel<AccreditedNetworkMapNavigator>(
    sessionRepository = sessionRepository
) {

    private val _markersLiveData =
        MutableLiveData<Resource<List<AccreditedNetworkGeoLocation>, Nothing?>>()
    private val _selectedAccreditedNetwork = MutableLiveData<AccreditedNetwork?>()
    private val _accreditedNetworkLiveData =
        MutableLiveData<Resource<AccreditedNetwork?, Nothing?>>()
    private val _hasUserLocation = MutableLiveData<Boolean>()
    private var cancelCurrentAccreditedNetworkLoad: () -> Unit = {}
    private var cancelCurrentAccreditedNetworks: () -> Unit = {}

    val selectedAccreditedNetwork: LiveData<AccreditedNetwork?> get() = _selectedAccreditedNetwork
    val markersLiveData: LiveData<Resource<List<AccreditedNetworkGeoLocation>, Nothing?>> get() = _markersLiveData
    val accreditedNetworkLiveData: LiveData<Resource<AccreditedNetwork?, Nothing?>> get() = _accreditedNetworkLiveData
    val hasSelectedAccreditedNetwork: LiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(_selectedAccreditedNetwork) {
            liveData.value = it != null
        }

        liveData
    }
    val selectedAccreditedNetworkFantasyName: LiveData<String> by lazy {
        val liveData = MediatorLiveData<String>()

        liveData.addSource(_selectedAccreditedNetwork) {
            liveData.value = it?.fantasyName
        }

        liveData
    }
    val selectedAccreditedNetworkAddress: LiveData<String> by lazy {
        val liveData = MediatorLiveData<String>()

        liveData.addSource(_selectedAccreditedNetwork) { accreditedNetwork ->
            val value: String = if (accreditedNetwork != null) {
                """
                    ${accreditedNetwork.address}, ${accreditedNetwork.complement}
                    ${accreditedNetwork.neighborhood} - ${accreditedNetwork.city} - ${accreditedNetwork.uf}
                """.trimIndent()
            } else {
                ""
            }

            liveData.value = value
        }

        liveData
    }

    val hasUserLocation: LiveData<Boolean>
        get() = _hasUserLocation

    fun loadAccreditedNetworkGeoLocation(card: Card, location: LatLng) {

        cancelCurrentAccreditedNetworks()

        val job = viewModelScope.launch(Dispatchers.IO) {

            val account = sessionRepository.getAccount() ?: return@launch

            val request = AccreditedNetworkGeoLocationRequest(
                id = card.id,
                type = card.type.id,
                latitude = location.latitude.toString(),
                longitude = location.longitude.toString()
            )

            val result = accreditedNetworkRepository.getAccreditedNetworksGeoLocation(
                accessToken = account.accessToken,
                request = request
            )

            val onComplete = {
                cancelCurrentAccreditedNetworks = {}
            }

            when (result) {
                is Either.Left -> {
                    onGetAccreditedNetworksGeoLocationDataFailure(
                        throwable = result.a,
                        onComplete = onComplete
                    )
                }
                is Either.Right -> {
                    onAccreditedNetworksGeoLocationDataSuccess(
                        resource = result.b,
                        onComplete = onComplete
                    )
                }
            }

        }

        cancelCurrentAccreditedNetworks = {
            job.cancel()
        }

    }

    private fun onGetAccreditedNetworksGeoLocationDataFailure(
        throwable: Throwable,
        onComplete: () -> Unit
    ) {
        errorNotifier.notify(throwable)
        onComplete()
    }

    private fun onAccreditedNetworksGeoLocationDataSuccess(
        resource: Resource<List<AccreditedNetworkGeoLocation>, Nothing?>,
        onComplete: () -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onAccreditedNetworksGeoLocationSuccess(accreditedNetworkList = resource.data)
                onComplete()
            }
            is ErrorResource -> {
                onAccreditedNetworksGeoLocationError()
                onComplete()
            }
        }
    }

    private fun onAccreditedNetworksGeoLocationSuccess(accreditedNetworkList: List<AccreditedNetworkGeoLocation>) {
        _markersLiveData.postValue(accreditedNetworkList.successResource())
    }

    private fun onAccreditedNetworksGeoLocationError() {
        Timber.w("Error while getting markers")
        _markersLiveData.postValue(ErrorResource(null))
    }

    fun loadAccreditedNetworkDetail(code: String) {

        _accreditedNetworkLiveData.postValue(LoadingResource())

        cancelCurrentAccreditedNetworkLoad()

        val job = viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val result = accreditedNetworkRepository.getAccreditedNetwork(
                accessToken = account.accessToken,
                code = code
            )

            val onComplete = {
                cancelCurrentAccreditedNetworkLoad = {}
            }

            when (result) {
                is Either.Left -> {
                    onGetStatementDataFailure(throwable = result.a, onComplete = onComplete)
                }
                is Either.Right -> {
                    onGetStatementDataSuccess(resource = result.b, onComplete = onComplete)
                }
            }

        }

        cancelCurrentAccreditedNetworkLoad = {
            job.cancel()
        }
    }

    private fun onGetStatementDataFailure(throwable: Throwable, onComplete: () -> Unit) {
        errorNotifier.notify(throwable)
        onComplete()
    }

    private fun onGetStatementDataSuccess(
        resource: Resource<AccreditedNetwork, Nothing?>,
        onComplete: () -> Unit
    ) {
        val x = when (resource) {
            is LoadingResource -> {
                onGetStatementLoading()
            }
            is SuccessResource -> {
                onGetStatementSuccess(accreditedNetwork = resource.data)
                onComplete()
            }
            is ErrorResource -> {
                onGetStatementError()
                onComplete()
            }
        }
    }

    private fun onGetStatementLoading() {
        _accreditedNetworkLiveData.postValue(LoadingResource())
    }

    private fun onGetStatementSuccess(accreditedNetwork: AccreditedNetwork) {
        _accreditedNetworkLiveData.postValue(accreditedNetwork.successResource())
    }

    private fun onGetStatementError() {
        Timber.w("Error while getting markers")
        _accreditedNetworkLiveData.postValue(ErrorResource(null))
    }

    fun filterClicked() {
        navigator?.navigateToFilter()
    }

    fun setSelectedAccreditedNetwork(accreditedNetwork: AccreditedNetwork?) {
        if (this._selectedAccreditedNetwork.value?.id == accreditedNetwork?.id) {
            this._selectedAccreditedNetwork.value = null
            return
        }

        this._selectedAccreditedNetwork.value = accreditedNetwork
    }

    fun setHasUserLocation(hasLocation: Boolean) {
        if (!hasLocation) {
            this._hasUserLocation.value = false
            return
        }

        this._hasUserLocation.value = hasLocation
    }
}