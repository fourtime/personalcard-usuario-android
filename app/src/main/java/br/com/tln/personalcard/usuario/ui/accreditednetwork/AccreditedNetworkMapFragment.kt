package br.com.tln.personalcard.usuario.ui.accreditednetwork

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.ErrorResource
import br.com.tln.personalcard.usuario.core.HasToolbar
import br.com.tln.personalcard.usuario.core.LoadingResource
import br.com.tln.personalcard.usuario.core.Resource
import br.com.tln.personalcard.usuario.core.SessionRequiredBaseFragment
import br.com.tln.personalcard.usuario.core.SuccessResource
import br.com.tln.personalcard.usuario.databinding.FragmentAccreditedNetworkMapBinding
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.model.AccreditedNetwork
import br.com.tln.personalcard.usuario.model.AccreditedNetworkGeoLocation
import br.com.tln.personalcard.usuario.widget.BottomSheetDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber
import java.util.concurrent.TimeUnit

@RuntimePermissions
class AccreditedNetworkMapFragment :
    SessionRequiredBaseFragment<FragmentAccreditedNetworkMapBinding, AccreditedNetworkMapNavigator, AccreditedNetworkMapViewModel>(
        layoutRes = R.layout.fragment_accredited_network_map,
        viewModelClass = AccreditedNetworkMapViewModel::class.java
    ), HasToolbar, AccreditedNetworkMapNavigator, OnMapReadyCallback {

    private val args: AccreditedNetworkMapFragmentArgs by navArgs()
    private var loadingDialog: ProgressDialog? = null

    private var userNavigating = false
    private var googleMap: GoogleMap? = null
    private var userLocation: Location? = null
    private var currentLocationMarker: Marker? = null
    private val markers: MutableList<Marker> = mutableListOf()
    private var centerLocation: LatLng? = null
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private var initializingGps = true
    private var accreditedNetworkDetail: AccreditedNetwork? = null

    private val locationRequest by lazy {
        LocationRequest()
            .setFastestInterval(TimeUnit.SECONDS.toMillis(30))
            .setSmallestDisplacement(2f)
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation

                viewModel.setHasUserLocation(lastLocation != null)

                setUserLocation(lastLocation)
                if (!userNavigating) {
                    centerOnUserLocation()
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {

                if (initializingGps && !locationAvailability.isLocationAvailable && args.accreditedNetwork == null) {
                    navigateToFilter()
                }

                initializingGps = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args.accreditedNetwork?.run {
            userNavigating = true
            centerLocation = LatLng(
                latitude.toDouble(),
                longitude.toDouble()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            MapsInitializer.initialize(requireActivity().applicationContext)
            requireBinding().mapView.onCreate(savedInstanceState)

            viewModel.accreditedNetworkLiveData.observe(viewLifecycleOwner) { accreditedNetwork ->
                if (accreditedNetwork == null) {
                    return@observe
                }

                when (val resource: Resource<AccreditedNetwork?, Nothing?> = accreditedNetwork) {
                    is SuccessResource -> {
                        resource.data?.run {
                            onAccreditedNetworkMarkerClick(resource.data)
                        }
                    } else -> {}
                }

                val x = when (accreditedNetwork) {
                    is LoadingResource -> {
                        showLoading()
                    }
                    is SuccessResource -> {
                        hideLoading()
                    }
                    is ErrorResource -> {
                        hideLoading()
                    }
                }
            }

            viewModel.markersLiveData.observe(viewLifecycleOwner) { markers ->
                when (val resource: Resource<List<AccreditedNetworkGeoLocation>, Nothing?> =
                    markers) {
                    is SuccessResource -> {
                        showMarkers(resource.data)
                    } else -> {}
                }
            }

            viewModel.selectedAccreditedNetwork.observe(viewLifecycleOwner) { accreditedNetwork ->
                accreditedNetworkDetail = accreditedNetwork
            }
        }
    }

    override fun onDestroyView() {
        googleMap = null
        requireBinding().mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().onClickShowMyLocation = onClickShowMyLocation()

        requireBinding().mapView.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        getLocationWithPermissionCheck()
    }

    override fun onStop() {
        super.onStop()

        if (this::locationProviderClient.isInitialized) {
            locationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        requireBinding().mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        requireBinding().mapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        requireBinding().mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        configureMap()

        if (userLocation != null) {
            createUserLocationMarker()
        }

        args.accreditedNetwork?.apply {
            val accreditedNetworkGeoLocation = AccreditedNetworkGeoLocation(
                code = this.id,
                latitude = this.latitude,
                longitude = this.longitude
            )
            createAccreditedNetworkMarker(accreditedNetworkGeoLocation)
            clickedAccreditedNetwork(accreditedNetworkGeoLocation)
        }

        centerLocation?.apply {
            centerOnLocation(this)
        }
    }

    override fun setupToolbar() {
        requireBinding().toolbar.setupWithNavController(findNavController())
    }

    override fun navigateToPreLogin() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions = AccreditedNetworkMapFragmentDirections.actionGlobalPreLoginFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitialization() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionGlobalInitializationCpfFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToInitializationPassword(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToLogin(cpf: String, registerEmail: Boolean) {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionGlobalInitializationPasswordFragment(
                cpf = cpf,
                registerEmail = registerEmail
            )
        findNavController().navigate(directions)
    }

    override fun navigateToCreatePassword() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionGlobalInitializationCreatePasswordFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToRegisterEmail() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionGlobalInitializationWelcomeFragment()
        findNavController().navigate(directions)
    }

    override fun navigateToFilter() {
        if (findNavController().currentDestination?.id != R.id.accreditedNetworkMapFragment) {
            return
        }

        val directions =
            AccreditedNetworkMapFragmentDirections.actionAccreditedNetworkMapFragmentToAccreditedNetworkFilterFragment(
                card = args.card
            )
        findNavController().navigate(directions)
    }

    @NeedsPermission(value = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLocation() {
        val locationPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        val coarseLocationPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (locationPermission != PackageManager.PERMISSION_GRANTED || coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (!this::locationProviderClient.isInitialized) {
            locationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        } else {
            locationProviderClient.removeLocationUpdates(locationCallback)
        }

        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    @NeedsPermission(Manifest.permission.CALL_PHONE)
    fun callAccreditedNetwork(phone: String) {
        val callPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
        if (callPermission == PackageManager.PERMISSION_GRANTED) {
            requireActivity().let {
                val callIntent = Intent(Intent.ACTION_DIAL)
                val cleanPhone = phone.replace(Regex("[^0-9]"), "")
                callIntent.data = Uri.parse("tel:$cleanPhone")
                it.startActivity(callIntent)
            }
        }
    }

    private fun showLoading() {
        val loadingDialog = this.loadingDialog ?: ProgressDialog(context).also {
            it.setCancelable(true)
            it.setTitle(R.string.app_name)
            it.setMessage(getString(R.string.dialog_loading))
        }

        this.loadingDialog = loadingDialog

        loadingDialog.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss().also { loadingDialog = null }
    }

    private fun configureMap() {
        googleMap?.apply {
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json)
            )
            setOnMarkerClickListener { marker ->
                when (val tag = marker.tag) {
                    is AccreditedNetworkGeoLocation -> {
                        clickedAccreditedNetwork(tag)
                    }
                }
                true
            }

            setOnMapClickListener {
                viewModel.setSelectedAccreditedNetwork(null)
            }

            setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    userNavigating = true
                }
            }

            setOnCameraIdleListener {
                cameraPosition.target?.run {
                    loadAccreditedNetworks(this)
                }
            }

        } ?: Timber.e("Trying to use map before it was initialized")
    }

    private fun setUserLocation(location: Location?) {
        this.userLocation = location
        createUserLocationMarker()
    }

    private fun createUserLocationMarker() {
        val location = userLocation ?: return
        val googleMap = this.googleMap ?: return

        currentLocationMarker?.remove()

        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_user_location))
        ).also {
            currentLocationMarker = it
        }
    }

    private fun centerOnUserLocation() {
        currentLocationMarker?.run {
            val location = this.position
            centerOnLocation(location)
            loadAccreditedNetworks(location)
        }
    }

    private fun loadAccreditedNetworks(location: LatLng) {
        if (accreditedNetworkDetail != null) {
            return
        }
        viewModel.loadAccreditedNetworkGeoLocation(
            card = args.card,
            location = location
        )
    }

    private fun centerOnLocation(location: LatLng) {
        this.centerLocation = location
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 16.5f)
        googleMap?.animateCamera(cameraUpdate)
            ?: Timber.e("Trying to use map before it was initialized.")
    }

    private fun onClickShowMyLocation(): View.OnClickListener = View.OnClickListener {
        userNavigating = false
        centerOnUserLocation()
    }

    private fun showMarkers(markers: List<AccreditedNetworkGeoLocation>) {
        this.markers.forEach {
            it.remove()
        }
        this.markers.clear()

        markers.forEach {
            createAccreditedNetworkMarker(it)?.also { marker ->
                this.markers.add(marker)
            }
        }
    }

    private fun createAccreditedNetworkMarker(accreditedNetwork: AccreditedNetworkGeoLocation): Marker? {
        val location =
            LatLng(accreditedNetwork.latitude.toDouble(), accreditedNetwork.longitude.toDouble())

        return googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.geolocation_accredited_network))
        )?.also { marker ->
            marker.tag = accreditedNetwork
        } ?: run {
            Timber.e("Trying to use map before it was initialized")
            null
        }
    }

    private fun clickedAccreditedNetwork(accreditedNetworkGeoLocation: AccreditedNetworkGeoLocation) {
        viewModel.loadAccreditedNetworkDetail(accreditedNetworkGeoLocation.code)
    }

    private fun onAccreditedNetworkMarkerClick(accreditedNetwork: AccreditedNetwork) {
        viewModel.setSelectedAccreditedNetwork(accreditedNetwork)

        if(accreditedNetwork.phone == null || accreditedNetwork.phone.length < 11){
            requireBinding().accreditedNetworkMapDetailsButtonCall.background = resources.getDrawable(R.drawable.button_client_disabled)
        } else {
            requireBinding().onClickCallToNetwork = onClickCallToNetwork(accreditedNetwork)
        }

        requireBinding().onClickGoToNetwork = onClickGoToNetwork(accreditedNetwork)
    }

    private fun onClickCallToNetwork(accreditedNetwork: AccreditedNetwork): View.OnClickListener =
        View.OnClickListener {
            if(accreditedNetwork.phone == null){
                return@OnClickListener
            }
            callAccreditedNetworkWithPermissionCheck(accreditedNetwork.phone)
        }

    private fun onClickGoToNetwork(accreditedNetwork: AccreditedNetwork): View.OnClickListener =
        View.OnClickListener {
            accreditedNetworkRoute(accreditedNetwork)
        }

    private fun accreditedNetworkRoute(accreditedNetwork: AccreditedNetwork) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(
            "geo:${accreditedNetwork.latitude},${accreditedNetwork.longitude}" +
                    "?q=${accreditedNetwork.address}+${accreditedNetwork.neighborhood}"
        )

        if (intent.resolveActivity(requireActivity().packageManager) == null) {
            BottomSheetDialog.genericError(requireContext()).show() //FIXME mensagem específica
        } else {
            startActivity(intent)
        }
    }
}