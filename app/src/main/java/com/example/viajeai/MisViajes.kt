package com.example.viajeai

import Viaje
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MisViajes : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var openDrawer: Button
    private lateinit var linearLayoutViajes: LinearLayout
    private lateinit var btnAddViaje: ImageButton
    private lateinit var btnDeleteViajes: ImageButton
    private var eliminandoViaje: Boolean = false

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viajesCollection: CollectionReference = firestore.collection("viajes")

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_viajes)
        val desdeViajes=false
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        linearLayoutViajes = findViewById(R.id.linear_layout_viajes)
        btnAddViaje = findViewById(R.id.btn_add_viaje)
        btnDeleteViajes = findViewById(R.id.btn_delete_viajes)

        openDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
            drawerLayout.openDrawer(GravityCompat.END)
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.one -> {
                    val intent = Intent(this, MisViajes::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)

                    return@setNavigationItemSelectedListener true
                }
                R.id.two -> {
                    val intent = Intent(this, MiPerfilActivity::class.java)
                    intent.putExtra("desdeViajes",desdeViajes)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.three -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }


                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
        }

        btnAddViaje.setOnClickListener {
            eliminandoViaje = false
            btnDeleteViajes.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red_apagado), PorterDuff.Mode.SRC_IN)
            val intent = Intent(this, AgregarViajeActivity::class.java)
            startActivity(intent)

        }

        btnDeleteViajes.setOnClickListener {
            eliminandoViaje = !eliminandoViaje
            if (eliminandoViaje) {

                btnDeleteViajes.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)
            } else {

                btnDeleteViajes.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red_apagado), PorterDuff.Mode.SRC_IN)
            }
        }

        cargarViajesDesdeFirebase()

    }

    private fun agregarViajeALayout(viaje: Viaje?) {

        val relativeLayout = RelativeLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            120
        ).apply {
            setMargins(0, 16, 0, 16)
        }
        relativeLayout.layoutParams = layoutParams
        relativeLayout.setPadding(40, 10, 10, 10)


        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.baseline_airplanemode_active_24)
        val iconParams = RelativeLayout.LayoutParams(64, 64)
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_START)
        iconParams.addRule(RelativeLayout.CENTER_VERTICAL)
        imageView.layoutParams = iconParams


        val textView = TextView(this)
        textView.text = viaje?.destino ?: "Destino desconocido"
        textView.text= textView.text.toString().toUpperCase()
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        val textParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,

        )
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        textView.layoutParams = textParams


        relativeLayout.addView(imageView)
        relativeLayout.addView(textView)


        val drawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(this@MisViajes,R.color.negro_gris))
            cornerRadius = 70f
            setStroke(4, ContextCompat.getColor(this@MisViajes, R.color.white))
        }
        relativeLayout.background = drawable


        linearLayoutViajes.addView(relativeLayout)


        relativeLayout.setOnClickListener {
            if (!eliminandoViaje) {
                val intent = Intent(this, DetalleViajeActivity::class.java)
                intent.putExtra("viaje", viaje)

                startActivity(intent)
            } else {
                confirmarEliminacion(viaje)
            }
        }
    }





    private fun confirmarEliminacion(viaje: Viaje?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("¿Estás seguro de que deseas eliminar este viaje?")
            .setCancelable(false)
            .setPositiveButton("Sí") { dialog, id ->
                eliminarViaje(viaje!!,FirebaseAuth.getInstance().currentUser!!.uid)
            }
            .setNegativeButton("No") { dialog, id ->
                eliminandoViaje = false
                btnDeleteViajes.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red_apagado), PorterDuff.Mode.SRC_IN)
            }
        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun eliminarViaje(viaje:Viaje,userId:String) {
        if (viaje != null && userId != null) {
            viajesCollection.whereEqualTo("destino", viaje.destino).whereEqualTo("presupuesto",viaje.presupuesto).whereEqualTo("actividades",viaje.actividades).whereEqualTo("duracion",viaje.duracion)
                .whereEqualTo("idUsuario", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { exception ->

                                exception.printStackTrace()
                            }
                    }
                }
                .addOnFailureListener { exception ->

                    exception.printStackTrace()
                }
        } else {

        }
    }

    private fun agregarNuevoViaje() {
        val intent = Intent(this, AgregarViajeActivity::class.java)
        startActivity(intent)
    }

    private fun cargarViajesDesdeFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viajesCollection.whereEqualTo("idUsuario", userId).addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    return@addSnapshotListener
                }

                linearLayoutViajes.removeAllViews()

                for (document in snapshot?.documents ?: emptyList()) {
                    try {
                        val viaje = document.toObject(Viaje::class.java)
                        agregarViajeALayout(viaje)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

