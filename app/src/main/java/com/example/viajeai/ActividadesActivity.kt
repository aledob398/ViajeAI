package com.example.viajeai
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient

class ActividadesActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var textViewName: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var imageViewPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        textViewName = findViewById(R.id.textViewName)
        textViewAddress = findViewById(R.id.textViewAddress)
        imageViewPhoto = findViewById(R.id.imageViewPhoto)

        Places.initialize(applicationContext, "TU_CLAVE_DE_API")
        placesClient = Places.createClient(this)

    
        val madridLocation = LatLng(40.4168, -3.7038)

      
        searchMuseums(madridLocation)
    }

    @SuppressLint("MissingPermission")
    private fun searchMuseums(location: LatLng) {
        val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
        placesClient.findCurrentPlace(request).addOnSuccessListener { response: FindCurrentPlaceResponse ->
            for (placeLikelihood in response.placeLikelihoods) {
                val place = placeLikelihood.place
                if (place.types.contains(Place.Type.MUSEUM)) {
                    // Este lugar es un museo, puedes acceder a su información como el nombre, la dirección, etc.
                    val museumName = place.name
                    val museumAddress = place.address

                    // Mostrar la información del museo en las vistas
                    displayMuseumDetails(museumName, museumAddress)

                    // Obtener la primera foto del museo
                    val photoMetadataResponse = place.photoMetadatas
                    if (photoMetadataResponse != null && !photoMetadataResponse.isEmpty()) {
                        val photoMetadata = photoMetadataResponse[0]
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(640) // Tamaño máximo de la imagen
                            .setMaxHeight(480)
                            .build()
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
                            val bitmap = fetchPhotoResponse.bitmap
                            // Mostrar la imagen del museo en el ImageView
                            imageViewPhoto.setImageBitmap(bitmap)
                        }.addOnFailureListener { exception ->
                            // Manejar cualquier error que ocurra al obtener la imagen del museo
                            Toast.makeText(this, "Error al obtener la imagen del museo: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Manejar cualquier error que ocurra al buscar museos en Madrid
            Toast.makeText(this, "Error al buscar museos en Madrid: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayMuseumDetails(name: String?, address: String?) {
        // Mostrar el nombre y la dirección del museo en los TextViews
        textViewName.text = name
        textViewAddress.text = address
    }
}
