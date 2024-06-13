package com.example.viajeai

import Viaje
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DetalleViajeActivity : AppCompatActivity() {

    private var viaje: Viaje? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var idViaje: String
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: Button

    @SuppressLint("MissingInflatedId", "NewApi", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_viaje)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        imageView = findViewById(R.id.imageViewClic)
        imageView2 = findViewById(R.id.imageViewClick)

        val btnActividadesAleatorias = findViewById<ImageButton>(R.id.btnActividadesAleatorias)
        val btnItinerario = findViewById<ImageButton>(R.id.btnItinerario)
        val btnCostes = findViewById<ImageButton>(R.id.btnCostes)
        val btnAlojamientosRestaurantes = findViewById<ImageButton>(R.id.btnAlojamientoRestaurantes)
        val animationView: ImageView = findViewById(R.id.imageViewClick)

        val animationDrawable: Drawable = animationView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }


        viaje = intent.getParcelableExtra<Viaje>("viaje")
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        idViaje = " "
        obtenerIdViaje()
        imageView2.visibility = View.GONE
        imageView.visibility= View.GONE
        val imgPerfil = findViewById<ImageButton>(R.id.btnImgViaje)
        val storageReferencee = FirebaseStorage.getInstance().reference.child("images/$idViaje.jpg")
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

        imgPerfil.setOnClickListener {
            openFileChooser()
        }

        btnItinerario.setOnClickListener {
            val intent = Intent(this, Itinerario::class.java)
            intent.putExtra("viaje", viaje)
            intent.putExtra("idViaje", idViaje)
            startActivity(intent)
        }
        btnAlojamientosRestaurantes.setOnClickListener {
            val intent = Intent(this, Alojamientos_restaurantes::class.java)
            intent.putExtra("viaje", viaje)
            intent.putExtra("idViaje", idViaje)
            startActivity(intent)
        }
        btnActividadesAleatorias.setOnClickListener {
            val intent = Intent(this, ActividadesAleatorias::class.java)
            intent.putExtra("viaje", viaje)
            intent.putExtra("idViaje", idViaje)
            startActivity(intent)
        }
        btnCostes.setOnClickListener {
            val intent = Intent(this, CostesActivity::class.java)
            intent.putExtra("idViaje", idViaje)
            intent.putExtra("viaje", viaje)
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            if (::idViaje.isInitialized) {
                uploadImageToFirebase()
                imageView2.visibility = View.GONE
                val imgPerfil = findViewById<ImageButton>(R.id.btnImgViaje)
                val storageReference = FirebaseStorage.getInstance().reference.child("images/$idViaje.jpg")
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(imgPerfil)
                }.addOnFailureListener { exception ->
                }
            } else {

            }
        }
    }

    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val fileReference = storage.reference.child("images/$idViaje.jpg")
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageView.visibility = View.GONE
                    cargarImagenPerfil()
                }
                .addOnFailureListener { e ->

                }
        }
    }

    private fun obtenerIdViaje() {
        firestore.collection("viajes")
            .whereEqualTo("destino", viaje?.destino)
            .whereEqualTo("idUsuario", viaje?.idUsuario)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && documents.size() > 0) {
                    val document = documents.first()
                    idViaje = document.id
                    Log.d(TAG, "ID de viaje obtenido: $idViaje")
                    cargarImagenPerfil()
                } else {


                }
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun cargarImagenPerfil() {
        val imgPerfil = findViewById<ImageButton>(R.id.btnImgViaje)
        val storageReference = storage.reference.child("images/$idViaje.jpg")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(imgPerfil)
        }.addOnFailureListener { exception ->
            imageView2.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        }.addOnSuccessListener {
            imageView2.visibility = View.GONE
            imageView.visibility = View.GONE
        }
    }


}
