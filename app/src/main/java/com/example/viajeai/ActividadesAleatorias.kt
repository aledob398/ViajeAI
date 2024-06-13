package com.example.viajeai


import Perfil
import Preferencia
import Viaje
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ActividadesAleatorias : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: ImageButton
    private var isDeletingEnabled = false
    private var idViaje=""
    private lateinit var viaje: Viaje
    private lateinit var rootRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var actividadesLayout: LinearLayout
    private lateinit var actividadAdapter: ActividadAdapter
    private lateinit var perfil: Perfil
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades_aleatorias)
        perfil = Perfil()
        actividadesLayout = findViewById(R.id.linearLayoutActividadesAleatorias)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        viaje = intent.getParcelableExtra("viaje")!!
        idViaje= intent.getStringExtra("idViaje").toString()
        database =
            FirebaseDatabase.getInstance("https://viajeai-default-rtdb.europe-west1.firebasedatabase.app/")
        rootRef = database.reference
        actividadAdapter = ActividadAdapter(ArrayList())
        obtenerActividadesAleatorias(actividadAdapter)

        FirebaseFirestore.getInstance().collection("perfiles").document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {

                        perfil = document.toObject(Perfil::class.java)!!

                    }
                }
            }
        openDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
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

                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.three -> {
                    val intent = Intent(this, ActividadesAleatorias::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.four -> {
                    val intent = Intent(this, Itinerario::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.five -> {
                    val intent = Intent(this, Alojamientos_restaurantes::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.six -> {
                    val intent = Intent(this, CostesActivity::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.seven -> {
                    val intent = Intent(this, DetalleViajeActivity::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.eight -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }

                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
        }
    }

    private fun obtenerActividadesAleatorias(adapter: ActividadAdapter) {
        val eventosDisponibles = ArrayList<Evento>()
        val maxEventos = 50

        fun addEvento(evento: Evento) {
            if (eventosDisponibles.size < maxEventos) {
                eventosDisponibles.add(evento)
                var barajados: ArrayList<Evento> = eventosDisponibles.shuffled() as ArrayList<Evento>
                adapter.updateData(barajados)
                displayActividades(barajados)
            }
        }

        if (perfil.preferencias.contains(Preferencia.DEPORTES)) {
            rootRef.child("deportes").child("serviceList").child("service").limitToFirst(50)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                            descripcion = descripcion.replace("<p>", "").replace("</p>", "").replace("<strong>", "").replace("</strong>", "")
                            val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                            val email = ds.child("basicData").child("email").value.toString()
                            val phone = ds.child("basicData").child("phone").value.toString()
                            val web = ds.child("basicData").child("web").value.toString()
                            val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                            val precio = precioString.toDoubleOrNull() ?: 0.0
                            val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                            val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                            val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                            if (imageUrl.contains("https:")) {
                                val evento = Evento(
                                    descripcion, name, email, phone, web, precio, imageUrl, "deportes", latitud, longitud
                                )
                                addEvento(evento)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Error al obtener deportes", error.toException())
                    }
                })
        }

        if (perfil.preferencias.contains(Preferencia.VIDA_NOCTURNA)) {
            rootRef.child("ocio_nocturno").child("serviceList").child("service").limitToFirst(50)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                            descripcion = descripcion.replace("<p>", "").replace("</p>", "").replace("<strong>", "").replace("</strong>", "")
                            val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                            val email = ds.child("basicData").child("email").value.toString()
                            val phone = ds.child("basicData").child("phone").value.toString()
                            val web = ds.child("basicData").child("web").value.toString()
                            val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                            val precio = precioString.toDoubleOrNull() ?: 0.0
                            val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                            val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                            val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                            if (imageUrl.contains("https:")) {
                                val evento = Evento(
                                    descripcion, name, email, phone, web, precio, imageUrl, "ocio_nocturno", latitud, longitud
                                )
                                addEvento(evento)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Error al obtener alojamientos", error.toException())
                    }
                })
        }

        rootRef.child("puntos_turisticos").child("serviceList").child("service").limitToFirst(50)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                        descripcion = descripcion.replace("<p>", "").replace("</p>", "").replace("<strong>", "").replace("</strong>", "")
                        val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()
                        val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0
                        val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                        val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                        if (imageUrl.contains("https:")) {
                            val evento = Evento(
                                descripcion, name, email, phone, web, precio, imageUrl, "puntos_turisticos", latitud, longitud
                            )
                            addEvento(evento)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Error al obtener alojamientos", error.toException())
                }
            })

        rootRef.child("tiendas").child("serviceList").child("service").limitToFirst(50)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                        descripcion = descripcion.replace("<p>", "").replace("</p>", "").replace("<strong>", "").replace("</strong>", "")
                        val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()
                        val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0
                        val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                        val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0

                        if (imageUrl.contains("https:")) {
                            val evento = Evento(
                                descripcion, name, email, phone, web, precio, imageUrl, "tiendas", latitud, longitud
                            )
                            addEvento(evento)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Error al obtener alojamientos", error.toException())
                }
            })
    }


    private fun displayActividades(eventos: List<Evento>) {
        actividadesLayout.removeAllViews()
        for (evento in eventos) {
            val cardView = createActivityCardView(evento, actividadesLayout)
            actividadesLayout.addView(cardView)
        }
    }

    @SuppressLint("ResourceType")
    private fun createActivityCardView(evento: Evento, actividadesLayout: LinearLayout): CardView {
        val cardView = CardView(this)
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16)
        }
        cardView.layoutParams = layoutParams
        cardView.radius = 8F
        cardView.cardElevation = 8F
        cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)  // Fondo transparente

        val button = Button(this)
        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.layoutParams = buttonLayoutParams
        button.text = evento.name

        when (evento.tipo) {
            "deportes" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_downhill_skiing_24,
                    0,
                    0,
                    0
                )

            }
            "ocio_nocturno" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark5)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_celebration_24,
                    0,
                    0,
                    0
                )

            }
            "puntos_turisticos" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark4)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_place_24,
                    0,
                    0,
                    0
                )
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "tiendas" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark2)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_shopping_bag_24,
                    0,
                    0,
                    0,
                )

            }
            else -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark3)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_money,
                    0,
                    0,
                    0
                )
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        button.setOnClickListener {
            val intent = Intent(this, DetalleGeneral::class.java).apply {
                putExtra("name", evento.name)
                putExtra("type", evento.tipo)
            }
            startActivity(intent)
        }

        cardView.addView(button)
        return cardView
    }



}

data class Evento(
    val descripcion: String,
    val name: String,
    val email: String,
    val phone: String,
    val web: String,
    val precio: Double,
    val imageUrl: String,
    val tipo: String,
    val latitud: Double,
    val longitud: Double
)
