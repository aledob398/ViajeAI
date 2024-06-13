package com.example.viajeai

import Perfil
import Preferencia
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ModificarPerfil : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    private val preferenciasSeleccionadas = mutableListOf<Preferencia>()
    private lateinit var editTextNombreUsuario: EditText
    private lateinit var imgPerfilCard: CardView
    private lateinit var fotoPerfil: ImageButton
    private lateinit var editTextNombre: EditText
    private var PICK_IMAGE_REQUEST = 1
    private lateinit var btnAceptar: ImageButton
    private val perfilesCollection = FirebaseFirestore.getInstance().collection("perfiles")
    private lateinit var perfil: Perfil
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private var imagenSeleccionada: Uri? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_perfil)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        imgPerfilCard = findViewById(R.id.imgPerfilCard2)
        fotoPerfil = findViewById(R.id.fotoPerfil)
        btnAceptar = findViewById(R.id.btnCrearPerfil)
        editTextNombreUsuario= findViewById(R.id.editTextNombreUsuario)
        perfil = intent.getParcelableExtra("perfil")!!
        var nombre:String=perfil.nombre
        editTextNombreUsuario.setText(nombre)
        val storageReference = FirebaseStorage.getInstance().reference.child("imagenes/$userId.jpg")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(fotoPerfil)
        }.addOnFailureListener {

        }
        setUpPreferenciaButtons()

        fotoPerfil.setOnClickListener {
            seleccionarImagen()
        }

        btnAceptar.setOnClickListener {
            crearPerfil()
            val intent = Intent(this, MiPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpPreferenciaButtons() {
        val buttonMuseos = findViewById<ImageButton>(R.id.buttonMuseos)
        val buttonTeatros = findViewById<ImageButton>(R.id.buttonTeatros)
        val buttonConciertos = findViewById<ImageButton>(R.id.buttonConciertos)
        val buttonDeportesExtremos = findViewById<ImageButton>(R.id.buttonDeportesExtremos)
        val buttonActividadesAlAireLibre = findViewById<ImageButton>(R.id.buttonActividadesAlAireLibre)
        val buttonSitiosHistoricos = findViewById<ImageButton>(R.id.buttonSitiosHistoricos)
        val buttonMonumentos = findViewById<ImageButton>(R.id.buttonMonumentos)
        val buttonPlaya = findViewById<ImageButton>(R.id.buttonPlaya)
        val buttonMontana = findViewById<ImageButton>(R.id.buttonMontana)
        val buttonParquesNaturales = findViewById<ImageButton>(R.id.buttonParquesNaturales)
        val buttonParquesDeAtracciones = findViewById<ImageButton>(R.id.buttonParquesDeAtracciones)
        val buttonVidaNocturna = findViewById<ImageButton>(R.id.buttonVidaNocturna)

        buttonMuseos.setOnClickListener { togglePreferencia(buttonMuseos, Preferencia.MUSEOS) }
        buttonTeatros.setOnClickListener { togglePreferencia(buttonTeatros, Preferencia.TEATROS) }
        buttonConciertos.setOnClickListener { togglePreferencia(buttonConciertos, Preferencia.CONCIERTOS) }
        buttonDeportesExtremos.setOnClickListener { togglePreferencia(buttonDeportesExtremos, Preferencia.DEPORTES) }
        buttonActividadesAlAireLibre.setOnClickListener { togglePreferencia(buttonActividadesAlAireLibre, Preferencia.ACTIVIDADES_AL_AIRE_LIBRE) }
        buttonSitiosHistoricos.setOnClickListener { togglePreferencia(buttonSitiosHistoricos, Preferencia.SITIOS_HISTORICOS) }
        buttonMonumentos.setOnClickListener { togglePreferencia(buttonMonumentos, Preferencia.MONUMENTOS) }
        buttonPlaya.setOnClickListener { togglePreferencia(buttonPlaya, Preferencia.PLAYA) }
        buttonMontana.setOnClickListener { togglePreferencia(buttonMontana, Preferencia.MONTAÑA) }
        buttonParquesNaturales.setOnClickListener { togglePreferencia(buttonParquesNaturales, Preferencia.PARQUES_NATURALES) }
        buttonParquesDeAtracciones.setOnClickListener { togglePreferencia(buttonParquesDeAtracciones, Preferencia.PARQUES_DE_ATRACCIONES) }
        buttonVidaNocturna.setOnClickListener { togglePreferencia(buttonVidaNocturna, Preferencia.VIDA_NOCTURNA) }
    }

    private fun togglePreferencia(button: ImageButton, preferencia: Preferencia) {
        if (preferenciasSeleccionadas.contains(preferencia)) {
            preferenciasSeleccionadas.remove(preferencia)
            button.setImageResource(R.drawable.button_selector)
        } else {
            preferenciasSeleccionadas.add(preferencia)
            button.setImageResource(R.drawable.button_background_selected)
        }
    }

    private fun crearPerfil() {
        val nombreUsuario = editTextNombreUsuario.text.toString()
        val nuevoPerfil = Perfil(FirebaseAuth.getInstance().uid.toString(), nombreUsuario, preferenciasSeleccionadas)

        if (imagenSeleccionada != null) {
            subirImagen(userId)
        }
        almacenarPerfilEnBaseDeDatos(nuevoPerfil, userId)
    }

    private fun seleccionarImagen() {
        val intent = Intent().setType("image/*")
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imagenSeleccionada = data.data
            val fotoUsuario = findViewById<ImageButton>(R.id.fotoPerfil)
            Glide.with(this).load(imagenSeleccionada).into(fotoUsuario)
        }
    }

    private fun subirImagen(userId: String) {
        val storageReference = firebaseStorage.reference.child("imagenes/$userId.jpg")
        imagenSeleccionada?.let {
            storageReference.putFile(it)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun almacenarPerfilEnBaseDeDatos(perfil: Perfil, userId: String) {
        perfilesCollection.document(userId)
            .set(perfil)
            .addOnSuccessListener {
                mostrarMensaje("Perfil creado y almacenado en la base de datos")

            }
            .addOnFailureListener { e ->
                mostrarMensaje("Error al almacenar el perfil en la base de datos: $e")
            }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
