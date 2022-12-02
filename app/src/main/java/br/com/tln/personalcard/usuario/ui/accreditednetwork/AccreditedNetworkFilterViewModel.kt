package br.com.tln.personalcard.usuario.ui.accreditednetwork

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import arrow.core.Either
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseViewModel
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.core.successResource
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.model.AccreditedNetworkFilter
import br.com.tln.personalcard.usuario.model.ActivityBranch
import br.com.tln.personalcard.usuario.model.City
import br.com.tln.personalcard.usuario.model.Neighborhood
import br.com.tln.personalcard.usuario.model.Segment
import br.com.tln.personalcard.usuario.model.Specialty
import br.com.tln.personalcard.usuario.model.UF
import br.com.tln.personalcard.usuario.repository.AccreditedNetworkRepository
import br.com.tln.personalcard.usuario.repository.SessionRepository
import br.com.tln.personalcard.usuario.webservice.request.ActivityBranchRequest
import br.com.tln.personalcard.usuario.webservice.request.CityRequest
import br.com.tln.personalcard.usuario.webservice.request.NeighborhoodRequest
import br.com.tln.personalcard.usuario.webservice.request.SegmentRequest
import br.com.tln.personalcard.usuario.webservice.request.SpecialtyRequest
import br.com.tln.personalcard.usuario.webservice.request.UFRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccreditedNetworkFilterViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val accreditedNetworkRepository: AccreditedNetworkRepository
) : SessionRequiredBaseViewModel<AccreditedNetworkFilterNavigator>(
    sessionRepository = sessionRepository
) {

    private val _segmentsLiveData = MutableLiveData<Resource<List<Segment>?, Nothing?>?>()
    private val _selectedSegmentName = MutableLiveData<String?>()

    private val _activityBranchLiveData =
        MutableLiveData<Resource<List<ActivityBranch>?, Nothing?>?>()
    private val _selectedActivityBranchName = MutableLiveData<String?>()

    private val _specialtyLiveData = MutableLiveData<Resource<List<Specialty>?, Nothing?>?>()
    private val _selectedSpecialtyName = MutableLiveData<String?>()

    private val _ufLiveData = MutableLiveData<Resource<List<UF>?, Nothing?>?>()
    private val _selectedUFname = MutableLiveData<String?>()

    private val _cityLiveData = MutableLiveData<Resource<List<City>?, Nothing?>?>()
    private val _selectedCityname = MutableLiveData<String?>()

    private val _neighborhoodLiveData = MutableLiveData<Resource<List<Neighborhood>?, Nothing?>?>()
    private val _selectedNeighborhoodyname = MutableLiveData<String?>()

    val segmentsLiveData: LiveData<List<Segment>?> = Transformations.map(_segmentsLiveData) {
        when (it) {
            is SuccessResource -> {
                it.data
            }
            else -> {
                null
            }
        }
    }
    val selectedSegmentName: LiveData<String> = Transformations.map(_selectedSegmentName) {
        segment = it.toString()
        it
    }

    val activityBranchLivedata: LiveData<List<ActivityBranch>?> =
        Transformations.map(_activityBranchLiveData) {
            when (it) {
                is SuccessResource -> {
                    it.data
                }
                else -> {
                    null
                }
            }
        }
    val selectedActivityBranchName: LiveData<String> =
        Transformations.map(_selectedActivityBranchName) {
            activityBranch = it.toString()
            it
        }

    val specialtyLivedata: LiveData<List<Specialty>?> = Transformations.map(_specialtyLiveData) {
        when (it) {
            is SuccessResource -> {
                it.data
            }
            else -> {
                null
            }
        }
    }
    val selectedSpecialtyName: LiveData<String> = Transformations.map(_selectedSpecialtyName) {
        specialty = it.toString()
        it
    }

    val ufLiveData: LiveData<List<UF>?> = Transformations.map(_ufLiveData) {
        when (it) {
            is SuccessResource -> {
                it.data
            }
            else -> {
                null
            }
        }
    }
    val selectedUFname: LiveData<String> = Transformations.map(_selectedUFname) {
        uf = it.toString()
        it
    }

    val cityLiveData: LiveData<List<City>?> = Transformations.map(_cityLiveData) {
        when (it) {
            is SuccessResource -> {
                it.data
            }
            else -> {
                null
            }
        }
    }
    val selectedCityName: LiveData<String> = Transformations.map(_selectedCityname) {
        city = it.toString()
        it
    }

    val neighborhoodLiveData: LiveData<List<Neighborhood>?> =
        Transformations.map(_neighborhoodLiveData) {
            when (it) {
                is SuccessResource -> {
                    it.data
                }
                else -> {
                    null
                }
            }
        }
    val selectedNeighborhoodName: LiveData<String> =
        Transformations.map(_selectedNeighborhoodyname) {
            neighborhood = it.toString()
            it
        }

    val isSpeciality = MutableLiveData<Boolean?>()
    val isActivityBranch = MutableLiveData<Boolean?>()

    private lateinit var card: Card
    private val emptySelection = ""
    private var segment = emptySelection
    private var activityBranch = emptySelection
    private var specialty = emptySelection
    private var uf = emptySelection
    private var city = emptySelection
    private var neighborhood = emptySelection

    val valid: MutableLiveData<Boolean> by lazy {
        val liveData = MediatorLiveData<Boolean>()

        liveData.addSource(_selectedSegmentName) {
            liveData.postValue(isValid())
        }

        liveData.addSource(_selectedUFname) {
            liveData.postValue(isValid())
        }

        liveData.addSource(_selectedCityname) {
            liveData.postValue(isValid())
        }

        liveData.addSource(_selectedSpecialtyName) {
            liveData.postValue(isValid())
        }

        liveData.addSource(_selectedActivityBranchName) {
            liveData.postValue(isValid())
        }

        liveData.addSource(_selectedNeighborhoodyname) {
            liveData.postValue(isValid())
        }

        liveData
    }

    private fun isValid(): Boolean {
        return (!_selectedSegmentName.value.isNullOrEmpty() && !_selectedUFname.value.isNullOrEmpty() && !_selectedCityname.value.isNullOrEmpty() &&
                ((isSpeciality.value == true && !_selectedSpecialtyName.value.isNullOrEmpty()) || (isActivityBranch.value == true && !_selectedActivityBranchName.value.isNullOrEmpty()))
                && !_selectedNeighborhoodyname.value.isNullOrEmpty())
    }

    init {
        this.isSpeciality.value = false
        this.isActivityBranch.value = true
    }

    fun setCard(card: Card) {
        this.card = card

        loadSegment()
    }

    fun segmentSelected(segment: Segment) {
        this.setSegment(segment)
        this.setActivityBranch("")
        this.setSpecialty("")
        this.setUf("")
    }

    fun activityBranchSelected(activityBranch: String) {
        this.setActivityBranch(activityBranch)
    }

    fun specialtySelected(specialty: String) {
        this.setSpecialty(specialty)
    }

    fun ufSelected(uf: String) {
        this.setUf(uf)
        this.setCity("")
        this.setNeighborhood("")
    }

    fun citySelected(city: String) {
        this.setCity(city)
    }

    fun neighborhoodSelected(neighborhood: String) {
        this.setNeighborhood(neighborhood)
    }

    fun filterResultsClicked() {
        navigator?.navigateToFilterResults(
            AccreditedNetworkFilter(
                idCard = card.id,
                type = card.type.id,
                segment = segment,
                speciality = specialty,
                uf = uf,
                city = city,
                neighborhood = neighborhood,
                activityBranch = activityBranch,
                pageNumber = 1,
                pageSize = 30
            )
        )
    }

    private fun setSegment(segment: Segment) {
        this._selectedSegmentName.value = segment.name

        this.isSpeciality.value = segment.type == 2
        this.isActivityBranch.value = segment.type != 2

        if (!this._selectedSegmentName.value.isNullOrEmpty()) {
            loadActivityBranch()
            loadSpecialty()
            loadUF()
        }
    }

    private fun setActivityBranch(activityBranch: String) {
        this._selectedActivityBranchName.value = activityBranch
    }

    private fun setSpecialty(specialty: String) {
        this._selectedSpecialtyName.value = specialty
    }

    private fun setUf(uf: String) {
        this._selectedUFname.value = uf

        if (!this._selectedUFname.value.isNullOrEmpty()) {
            loadCity()
        }
    }

    private fun setCity(city: String) {
        this._selectedCityname.value = city

        if (!this._selectedCityname.value.isNullOrEmpty()) {
            loadNeighborhood()
        }
    }

    private fun setNeighborhood(neighborhood: String) {
        this._selectedNeighborhoodyname.value = neighborhood
    }

    fun loadSegmentIfFailed() {
        when (_segmentsLiveData.value) {
            is ErrorResource -> {
                loadSegment()
            } else -> {}
        }
    }

    private fun loadSegment() {
        _segmentsLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = SegmentRequest(
                idCard = card.id,
                type = card.type.id
            )

            val result = accreditedNetworkRepository.getSegment(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetSegmentDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetSegmentDataSuccess(resource = result.b)
                }
            }
        }

    }

    private fun onGetSegmentDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
        _segmentsLiveData.postValue(ErrorResource(null))
    }

    private fun onGetSegmentDataSuccess(resource: Resource<List<Segment>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetSegmentSuccess(segments = resource.data)
            }
            is ErrorResource -> {
                onGetSegmentError()
            }
        }
    }

    private fun onGetSegmentSuccess(segments: List<Segment>) {
        _segmentsLiveData.postValue(segments.successResource())
    }

    private fun onGetSegmentError() {
        _segmentsLiveData.postValue(ErrorResource(null))
    }

    fun loadActivityBranchIfFailed() {
        when (_activityBranchLiveData.value) {
            is ErrorResource -> {
                loadActivityBranch()
            } else -> {}
        }
    }

    private fun loadActivityBranch() {
        _activityBranchLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = ActivityBranchRequest(
                idCard = card.id,
                type = card.type.id,
                segment = segment
            )

            val result = accreditedNetworkRepository.getActivityBranch(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetActivityBranchDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetActivityBranchDataSuccess(resource = result.b)
                }
            }
        }
    }

    private fun onGetActivityBranchDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
    }

    private fun onGetActivityBranchDataSuccess(resource: Resource<List<ActivityBranch>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetActivityBranchSuccess(activityBranches = resource.data)
            }
            is ErrorResource -> {
                onGetActivityBranchError()
            }
        }
    }

    private fun onGetActivityBranchSuccess(activityBranches: List<ActivityBranch>) {
        _activityBranchLiveData.postValue(activityBranches.successResource())
    }

    private fun onGetActivityBranchError() {
        _activityBranchLiveData.postValue(ErrorResource(null))
    }

    fun loadSpecialtyIfFailed() {
        when (_specialtyLiveData.value) {
            is ErrorResource -> {
                loadSpecialty()
            } else -> {}
        }
    }

    private fun loadSpecialty() {
        _specialtyLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = SpecialtyRequest(
                idCard = card.id,
                type = card.type.id,
                segment = segment
            )

            val result = accreditedNetworkRepository.getSpecialty(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetSpecialtyDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetSpecialtyDataSuccess(resource = result.b)
                }
            }
        }
    }

    private fun onGetSpecialtyDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
    }

    private fun onGetSpecialtyDataSuccess(resource: Resource<List<Specialty>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetSpecialtySuccess(specialteis = resource.data)
            }
            is ErrorResource -> {
                onGetSpecialtyError()
            }
        }
    }

    private fun onGetSpecialtySuccess(specialteis: List<Specialty>) {
        _specialtyLiveData.postValue(specialteis.successResource())
    }

    private fun onGetSpecialtyError() {
        _specialtyLiveData.postValue(ErrorResource(null))
    }

    fun loadUFIfFailed() {
        when (_ufLiveData.value) {
            is ErrorResource -> {
                loadUF()
            } else -> {}
        }
    }

    private fun loadUF() {
        if (segment.isNullOrEmpty()) {
            _ufLiveData.postValue(ErrorResource(null))
            return
        }

        _ufLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = UFRequest(
                idCard = card.id,
                type = card.type.id,
                segment = segment,
                specialty = specialty
            )

            val result = accreditedNetworkRepository.getUF(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetUFDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetUFDataSuccess(resource = result.b)
                }
            }
        }
    }

    private fun onGetUFDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
    }

    private fun onGetUFDataSuccess(resource: Resource<List<UF>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetUFSuccess(ufs = resource.data)
            }
            is ErrorResource -> {
                onGetUFError()
            }
        }
    }

    private fun onGetUFSuccess(ufs: List<UF>) {
        _ufLiveData.postValue(ufs.successResource())
    }

    private fun onGetUFError() {
        _ufLiveData.postValue(ErrorResource(null))
    }

    fun loadCityIfFailed() {
        when (_cityLiveData.value) {
            is ErrorResource -> {
                loadCity()
            } else -> {}
        }
    }

    private fun loadCity() {
        _cityLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = CityRequest(
                idCard = card.id,
                type = card.type.id,
                segment = segment,
                specialty = specialty,
                uf = uf
            )

            val result = accreditedNetworkRepository.getCity(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetCityDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetCityDataSuccess(resource = result.b)
                }
            }
        }
    }

    private fun onGetCityDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
    }

    private fun onGetCityDataSuccess(resource: Resource<List<City>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetCitySuccess(cities = resource.data)
            }
            is ErrorResource -> {
                onGetCityError()
            }
        }
    }

    private fun onGetCitySuccess(cities: List<City>) {
        _cityLiveData.postValue(cities.successResource())
    }

    private fun onGetCityError() {
        _cityLiveData.postValue(ErrorResource(null))
    }

    fun loadNeighborhoodIfFailed() {
        when (_neighborhoodLiveData.value) {
            is ErrorResource -> {
                loadNeighborhood()
            } else -> {}
        }
    }

    private fun loadNeighborhood() {
        _neighborhoodLiveData.postValue(LoadingResource())
        viewModelScope.launch(Dispatchers.IO) {
            val account = sessionRepository.getAccount() ?: return@launch

            val request = NeighborhoodRequest(
                idCard = card.id,
                type = card.type.id,
                segment = segment,
                specialty = specialty,
                uf = uf,
                city = city,
                activityBranch = activityBranch
            )

            val result = accreditedNetworkRepository.getNeighborhood(
                accessToken = account.accessToken,
                request = request
            )

            when (result) {
                is Either.Left -> {
                    onGetNeighborhoodDataFailure(throwable = result.a)
                }
                is Either.Right -> {
                    onGetNeighborhoodDataSuccess(resource = result.b)
                }
            }
        }
    }

    private fun onGetNeighborhoodDataFailure(throwable: Throwable) {
        errorNotifier.notify(throwable)
    }

    private fun onGetNeighborhoodDataSuccess(resource: Resource<List<Neighborhood>, Nothing?>) {
        val x = when (resource) {
            is LoadingResource -> {
            }
            is SuccessResource -> {
                onGetNeighborhoodSuccess(neighborhoods = resource.data)
            }
            is ErrorResource -> {
                onGetNeighborhoodError()
            }
        }
    }

    private fun onGetNeighborhoodSuccess(neighborhoods: List<Neighborhood>) {
        _neighborhoodLiveData.postValue(neighborhoods.successResource())
    }

    private fun onGetNeighborhoodError() {
        _neighborhoodLiveData.postValue(ErrorResource(null))
    }

}