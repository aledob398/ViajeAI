package com.example.viajeai

import Perfil
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MiPerfilActivity : AppCompatActivity() {

    private val perfilesCollection = FirebaseFirestore.getInstance().collection("perfiles")
    private lateinit var txtNombreUsuario: TextView
    private lateinit var chipGroupPreferencias: ChipGroup
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: Button
    private lateinit var btnImagenPerfil: ImageButton
    private lateinit var card: CardView
    private lateinit var btnCambiarImagen: Button
    private lateinit var buttonModificar: Button
    private lateinit var imagenSeleccionada: Uri
    private lateinit var perfil: Perfil
    private lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_perfil)

        txtNombreUsuario = findViewById(R.id.txtNombreUsuario)
        chipGroupPreferencias = findViewById(R.id.chipGroupPreferencias)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        btnImagenPerfil = findViewById(R.id.btnImagenPerfil)
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen)
        buttonModificar = findViewById(R.id.buttonModificar)
        card = findViewById(R.id.card)
        auth = FirebaseAuth.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        cargarYMostrarPerfil(userId)

        val logOutButton: Button = findViewById(R.id.logOutButton)
        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        buttonModificar.setOnClickListener {
            val intent=Intent(this,ModificarPerfil::class.java)
            intent.putExtra("perfil",perfil)
            startActivity(intent)
        }
        btnImagenPerfil.setOnClickListener {
            mostrarBotonCambiarImagen()
            Handler(Looper.getMainLooper()).postDelayed({
                btnCambiarImagen.visibility = View.INVISIBLE
                card.visibility = View.INVISIBLE
            }, 3000)
        }

        btnCambiarImagen.setOnClickListener {
            seleccionarImagen()
        }

        openDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
            drawerLayout.openDrawer(GravityCompat.END)
        }
        val desdeViajes=intent.getBooleanExtra("desdeViajes",false)

            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.options2)
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

    }

    private fun obtenerIdUsuario(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser?.uid ?: "ID_DEFECTO_SI_NO_HAY_USUARIO_LOGUEADO"
    }

    private fun cargarYMostrarPerfil(userId: String) {
        perfilesCollection.whereEqualTo("idUsuario", userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.documents[0]
                        perfil = document.toObject(Perfil::class.java)!!
                        mostrarPerfilEnInterfaz(perfil)
                    } else {
                        mostrarMensaje("Perfil no encontrado en la base de datos")
                    }
                } else {
                    mostrarMensaje("Error al cargar el perfil desde la base de datos: ${task.exception}")
                }
            }

        val storageReference = FirebaseStorage.getInstance().reference.child("imagenes/$userId.jpg")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(btnImagenPerfil)
        }.addOnFailureListener {
            // Handle any errors
        }
    }

    private fun mostrarPerfilEnInterfaz(perfil: Perfil?) {
        if (perfil != null) {
            txtNombreUsuario.text = "${perfil.nombre}"
            chipGroupPreferencias.removeAllViews()
            perfil.preferencias.forEach { preferencia ->
                val chip = Chip(this)
                chip.text = preferencia.name.replace("_"," ")
                chip.isClickable = false
                chipGroupPreferencias.addView(chip)
            }
        } else {
            mostrarMensaje("Perfil no válido")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }



    private fun mostrarBotonCambiarImagen() {
        card.visibility = View.VISIBLE
        btnCambiarImagen.visibility = View.VISIBLE
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imagenSeleccionada = data.data!!
            subirImagen(imagenSeleccionada)
        }
    }

    private fun subirImagen(imagenSeleccionada: Uri) {
        val userId = obtenerIdUsuario()
        val storageReference = FirebaseStorage.getInstance().reference.child("imagenes/$userId.jpg")
        storageReference.putFile(imagenSeleccionada)
            .addOnSuccessListener {
                Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()
                Glide.with(this).load(imagenSeleccionada).into(btnImagenPerfil)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
