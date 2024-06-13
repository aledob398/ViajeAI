package com.example.viajeai

import Viaje
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AgregarViajeActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viajesCollection = firestore.collection("viajes")
    private val costesCollection = firestore.collection("costes")


    private lateinit var spinnerDestino: Spinner
    private lateinit var etPresupuesto: EditText
    private lateinit var etDuracion: EditText
    private lateinit var etActividades: EditText
    private lateinit var btnGuardar: ImageButton

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_viaje)


        spinnerDestino = findViewById(R.id.spinnerDestino)
        etPresupuesto = findViewById(R.id.etPresupuesto)
        etDuracion = findViewById(R.id.etDuracion)
        etActividades = findViewById(R.id.etActividades)
        btnGuardar = findViewById(R.id.btnGuardar)
        val animationView: ImageView = findViewById(R.id.imageViewAnimacion)

        val animationDrawable: Drawable = animationView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }
        val animationView1: ImageView = findViewById(R.id.imageViewAnimacion1)


        val animationDrawable1: Drawable = animationView1.drawable
        if (animationDrawable1 is AnimatedImageDrawable) {
            animationDrawable1.start()
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_item,
            Ciudades.values().map { it.nombre }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDestino.adapter = adapter

        btnGuardar.setOnClickListener {

            val destino = spinnerDestino.selectedItem.toString()
            val presupuesto = etPresupuesto.text.toString().toDoubleOrNull()
            val duracion = etDuracion.text.toString().toIntOrNull()
            val actividades = etActividades.text.toString().split(",").map { it.trim() }


            val userId = auth.currentUser?.uid
            if (userId != null && destino.isNotEmpty() && presupuesto != null && duracion != null && actividades.isNotEmpty()) {
                val nuevoViaje = Viaje(userId, destino, presupuesto, duracion, actividades)


                viajesCollection.add(nuevoViaje)
                    .addOnSuccessListener { documentReference ->

                        val idViaje = documentReference.id

                        val costeInicial = hashMapOf(
                            "idViaje" to idViaje,
                            "coste" to 0.0,
                            "descripcion" to "",
                            "fecha" to System.currentTimeMillis()
                        )
                        costesCollection.add(costeInicial)
                            .addOnSuccessListener {

                                finish()
                            }
                            .addOnFailureListener { exception ->

                            }
                    }
                    .addOnFailureListener { exception ->

                    }
            }
        }
    }
}
