package com.example.viajeai
import Alojamiento
import Viaje
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class Alojamientos_restaurantes : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: ImageButton
    private lateinit var rootRef: DatabaseReference
    private var idViaje=""
    private lateinit var viaje: Viaje
    private lateinit var recyclerViewAlojamientos: RecyclerView
    private lateinit var recyclerViewRestaurantes: RecyclerView
    private lateinit var alojamientoAdapter: AlojamientoAdapter
    private lateinit var restauranteAdapter: RestauranteAdapter
    private lateinit var textViewWelcome: TextView
    private lateinit var auth: FirebaseAuth

    private val TAG = "Alojamientos_restaurantes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alojamientos_restaurantes)

        textViewWelcome = findViewById(R.id.textViewWelcome)
        recyclerViewAlojamientos = findViewById(R.id.recyclerViewAlojamientos)
        recyclerViewRestaurantes = findViewById(R.id.recyclerViewRestaurantes)

        val presupuesto = 50
        recyclerViewAlojamientos.layoutManager = LinearLayoutManager(this)
        recyclerViewRestaurantes.layoutManager = LinearLayoutManager(this)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        alojamientoAdapter = AlojamientoAdapter(ArrayList())
        restauranteAdapter = RestauranteAdapter(ArrayList())

        recyclerViewAlojamientos.adapter = alojamientoAdapter
        recyclerViewRestaurantes.adapter = restauranteAdapter
        viaje = intent.getParcelableExtra("viaje")!!
        idViaje= intent.getStringExtra("idViaje").toString()
        database = FirebaseDatabase.getInstance("https://viajeai-default-rtdb.europe-west1.firebasedatabase.app/")
        rootRef = database.reference
        auth = FirebaseAuth.getInstance()
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
        FirebaseFirestore.getInstance().collection("perfiles").document(auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre")
                        textViewWelcome.text = "Hola $nombre, estas son nuestras recomendaciones según tu presupuesto"
                    } else {
                        Log.d(TAG, "No such document")
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.exception)
                }
            }
        Log.d(TAG, "Alojamiento obtenido: holaaa")
        obtenerAlojamientosRestaurantes(presupuesto)
    }

    private fun obtenerAlojamientosRestaurantes(presupuesto: Int) {

        rootRef.child("alojamientos").child("serviceList").child("service")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alojamientosDisponibles = ArrayList<Alojamiento>()
                    for (ds in snapshot.children) {
                        var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                        descripcion = descripcion.replace("<p>", "").replace("</p>", "").replace("<strong>", "").replace("</strong>", "")
                        val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()
                        val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0 // Valor predeterminado si es nulo
                        val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                        val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0
                        if (imageUrl.contains("https:")) {
                            val alojamiento = Alojamiento(descripcion, name, email, phone, web, precio, imageUrl ,latitud, longitud)
                            alojamientosDisponibles.add(alojamiento)
                        }
                    }
                    val alojamientosAleatorios = alojamientosDisponibles.shuffled().take(10)
                    alojamientoAdapter.updateData(alojamientosAleatorios)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Error al obtener alojamientos", error.toException())
                }
            })

        rootRef.child("restaurantes").child("serviceList").child("service")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val restaurantesDisponibles = ArrayList<Restaurante>()
                    for (ds in snapshot.children) {
                        var descripcion = ds.child("basicData").child("body").child("__cdata").value.toString()
                        descripcion = descripcion.replace("<p>", "").replace("</p>", " ").replace("<strong>", "").replace("</strong>", " ")
                        val name = ds.child("basicData").child("name").child("__cdata").value.toString()
                        val email = ds.child("basicData").child("email").value.toString()
                        val phone = ds.child("basicData").child("phone").value.toString()
                        val web = ds.child("basicData").child("web").value.toString()
                        val precioString = ds.child("extradata").child("item").child("precio").value.toString()
                        val precio = precioString.toDoubleOrNull() ?: 0.0 // Valor predeterminado si es nulo
                        val imageUrl = ds.child("multimedia").child("media").child("url").value.toString()
                        val latitud = ds.child("location").child("latitude").value.toString().toDoubleOrNull() ?: 0.0
                        val longitud = ds.child("location").child("longitude").value.toString().toDoubleOrNull() ?: 0.0
                        Log.d(TAG, "Restaurante obtenido: $imageUrl")
                        if (imageUrl.contains("https:")) {
                            val restaurante = Restaurante(descripcion, name, email, phone, web, precio, imageUrl, latitud, longitud)
                            restaurantesDisponibles.add(restaurante)
                        }
                    }
                    val restaurantesAleatorios = restaurantesDisponibles.shuffled().take(10)
                    restauranteAdapter.updateData(restaurantesAleatorios)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Error al obtener restaurantes", error.toException())
                }
            })
    }

}

