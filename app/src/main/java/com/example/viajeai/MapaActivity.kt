package com.example.viajeai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var destination: LatLng
    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        destination = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(destination).title("Destino"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15f))

        requestLocation()
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        mMap.isMyLocationEnabled = true

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.locations[0]
                    val userLocation = LatLng(location.latitude, location.longitude)

                    if (userMarker == null) {
                        userMarker = mMap.addMarker(MarkerOptions().position(userLocation).title("Mi ubicación"))
                    } else {
                        userMarker?.position = userLocation
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                    showRoute(userLocation, destination)
                }
            }
        }

        // Iniciar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showRoute(origin: LatLng, destination: LatLng) {
        val apiKey = getString(R.string.google_maps_key) // Asegúrate de tener tu API Key en strings.xml

        val context = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directions = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .await()

                if (directions.routes.isNotEmpty()) {
                    val route = directions.routes[0]
                    val path = route.overviewPolyline.decodePath()
                    val polylineOptions = PolylineOptions().addAll(path.map { LatLng(it.lat, it.lng) }).color(R.color.purple_500).width(5f)

                    withContext(Dispatchers.Main) {
                        mMap.addPolyline(polylineOptions)

                        val bounds = LatLngBounds(
                            LatLng(route.bounds.southwest.lat, route.bounds.southwest.lng),
                            LatLng(route.bounds.northeast.lat, route.bounds.northeast.lng)
                        )

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                } else {
                    println("No routes found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error: ${e.message}")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        } else {
            println("Permiso de ubicación denegado")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
