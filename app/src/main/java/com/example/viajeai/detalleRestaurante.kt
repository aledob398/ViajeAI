package com.example.viajeai

import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class DetalleRestaurante : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_restaurante)

        database = FirebaseDatabase.getInstance().reference

        val restaurante = intent.getParcelableExtra<Restaurante>("restaurante")
        val itinerarioName = intent.getStringExtra("name")

        if (itinerarioName != null) {
            loadRestaurantFromDatabase(itinerarioName)
        } else if (restaurante != null) {
            displayRestaurantDetails(restaurante)
        }
    }

    private fun loadRestaurantFromDatabase(name: String) {
        val restaurantRef = database.child("restaurantes").child("serviceList").child("service")
        restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val restaurantName = ds.child("basicData").child("name").child("__cdata").value.toString()
                    if (restaurantName == name) {
                        val descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                            .replace("<p>", "")
                            .replace("</p>", " ")
                            .replace("<strong>", "")
                            .replace("</strong>", " ")
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()

                        val latitud = ds.child("geoData").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("geoData").child("longitude").value.toString().toDoubleOrNull() ?: 0.0
                        val precioString =
                            ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0
                        val imageUrl =
                            ds.child("multimedia").child("media").child("url").value.toString()

                        val restaurante = Restaurante(descripcion, restaurantName, email, phone, web, precio, imageUrl, latitud, longitud)
                        displayRestaurantDetails(restaurante)
                        return
                    }
                }

                Log.d(TAG, "No se encontró ningún restaurante con el nombre: $name")
                Toast.makeText(this@DetalleRestaurante, "No se encontró ningún restaurante con el nombre: $name", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar el restaurante desde la base de datos: ${error.message}")
                Toast.makeText(this@DetalleRestaurante, "Error al cargar el restaurante", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun displayRestaurantDetails(restaurant: Restaurante) {
        findViewById<TextView>(R.id.textViewNombreRestaurante).text = restaurant.name
        findViewById<TextView>(R.id.textViewDescripcionRestaurante).text = restaurant.descripcion

        val emailButton = findViewById<Button>(R.id.buttonEmailRestaurante)
        emailButton.text = restaurant.email
        emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${restaurant.email}")
            startActivity(intent)
        }

        loadImageWithFallback(restaurant.imageUrl, findViewById(R.id.imageViewRestaurante), R.drawable.signin)

        findViewById<Button>(R.id.buttonAdd).setOnClickListener {
            showTimePickerDialog(restaurant)
        }
    }

    private fun loadImageWithFallback(url: String, imageView: ImageView, placeholderResId: Int) {
        val requestOptions = RequestOptions()
            .placeholder(placeholderResId)
            .error(placeholderResId)

        Glide.with(imageView.context)
            .load(url)
            .apply(requestOptions)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    imageView.setImageDrawable(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e(TAG, "Error al cargar la imagen desde la URL: $url")
                    imageView.setImageResource(placeholderResId)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun showTimePickerDialog(restaurant: Restaurante) {
        val calendar = Calendar.getInstance()

        TimePickerDialog(this, { _, startHour, startMinute ->
            TimePickerDialog(this, { _, endHour, endMinute ->
                saveActivityToFirebase(restaurant, startHour, startMinute, endHour, endMinute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun saveActivityToFirebase(restaurant: Restaurante, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val activity = hashMapOf(
            "idUsuario" to userId,
            "name" to restaurant.name,
            "startHour" to startHour,
            "startMinute" to startMinute,
            "endHour" to endHour,
            "endMinute" to endMinute,
            "tipo" to "Restaurante"
        )

        db.collection("actividades")
            .add(activity)
            .addOnSuccessListener {
                Toast.makeText(this, "Actividad añadida", Toast.LENGTH_SHORT).show()
                startItinerarioActivity()
            }
            .addOnFailureListener { e ->
                Log.w("DetalleRestaurante", "Error adding document", e)
                Toast.makeText(this, "Error al añadir actividad", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startItinerarioActivity() {
        val intent = Intent(this, Itinerario::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}
