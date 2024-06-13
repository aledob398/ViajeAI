package com.example.viajeai

import android.app.DatePickerDialog
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

class DetalleGeneral : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_general)

        database = FirebaseDatabase.getInstance().reference

        val itemType = intent.getStringExtra("type")
        val itemName = intent.getStringExtra("name")

        if (itemType != null && itemName != null) {
            loadItemFromDatabase(itemType, itemName)
        } else {
            Toast.makeText(this, "Tipo o nombre del elemento no proporcionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItemFromDatabase(type: String, name: String) {
        val itemRef = database.child(type).child("serviceList").child("service")
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val itemName = ds.child("basicData").child("name").child("__cdata").value.toString()
                    if (itemName == name) {
                        val descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                            .replace(Regex("<[^>]+>"), "")
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()
                        val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0 // Valor predeterminado si es nulo
                        val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                        val latitud = ds.child("geoData").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("geoData").child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                        val item = GeneralItem(descripcion, itemName, email, phone, web, precio, imageUrl, type, latitud, longitud)
                        displayItemDetails(item)
                        return
                    }
                }

                Log.d(TAG, "No se encontró ningún item con el nombre: $name")
                Toast.makeText(this@DetalleGeneral, "No se encontró ningún item con el nombre: $name", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar el item desde la base de datos: ${error.message}")
                Toast.makeText(this@DetalleGeneral, "Error al cargar el item", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun displayItemDetails(item: GeneralItem) {
        if (!item.name.isNullOrEmpty()){
            findViewById<TextView>(R.id.textViewNombreItem).text = item.name
        }
        if (!item.descripcion.isNullOrEmpty()) {
            findViewById<TextView>(R.id.textViewDescripcionItem).text = item.descripcion
        }

        val emailButton = findViewById<Button>(R.id.buttonEmailItem)
        val webButton = findViewById<Button>(R.id.buttonWebItem)
        if (!item.web.isNullOrEmpty()) {
            webButton.text = item.web
            webButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(item.web)
                startActivity(intent)
            }
        }

        val phoneButton = findViewById<Button>(R.id.buttonTelefonoItem)
        if (!item.phone.isNullOrEmpty()) {
            phoneButton.text = item.phone
            phoneButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${item.phone}")
                startActivity(intent)
            }

        }
        if (!item.email.isNullOrEmpty()) {
            emailButton.text = item.email
            emailButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:${item.email}")
                startActivity(intent)
            }
        }



        loadImageWithFallback(item.imageUrl, findViewById(R.id.imageViewItem), R.drawable.signin)

        findViewById<Button>(R.id.buttonAdd).setOnClickListener {
            showDateTimePickerDialog(item)
        }

        val mapaButton = findViewById<Button>(R.id.buttonMap)
        mapaButton.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java).apply {
                putExtra("latitude", item.latitud)
                putExtra("longitude", item.longitud)
            }
            startActivity(intent)
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

    private fun showDateTimePickerDialog(item: GeneralItem) {
        val calendar = Calendar.getInstance()


        DatePickerDialog(this, { _, year, month, dayOfMonth ->

            calendar.set(year, month, dayOfMonth)


            TimePickerDialog(this, { _, startHour, startMinute ->

                calendar.set(Calendar.HOUR_OF_DAY, startHour)
                calendar.set(Calendar.MINUTE, startMinute)


                TimePickerDialog(this, { _, endHour, endMinute ->

                    saveActivityToFirebase(item, startHour, startMinute, endHour, endMinute, year, month + 1, dayOfMonth)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveActivityToFirebase(item: GeneralItem, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, year: Int, month: Int, day: Int) {
        val date = "$year-$month-$day"

        val activity = hashMapOf(
            "idUsuario" to userId,
            "name" to item.name,
            "startHour" to startHour,
            "startMinute" to startMinute,
            "endHour" to endHour,
            "endMinute" to endMinute,
            "tipo" to item.type,
            "date" to date,
            "day" to day,
            "month" to month
        )

        db.collection("actividades")
            .add(activity)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Actividad añadida", Toast.LENGTH_SHORT).show()
                startItinerarioActivity()
            }
            .addOnFailureListener { e ->
                Log.w("DetalleGeneral", "Error adding document", e)
                Toast.makeText(this, "Error al añadir actividad", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startItinerarioActivity() {
        val intent = Intent(this, Itinerario::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}

data class GeneralItem(
    val descripcion: String,
    val name: String,
    val email: String,
    val phone: String,
    val web: String,
    val precio: Double,
    val imageUrl: String,
    val type: String,
    val latitud: Double,
    val longitud: Double
)

