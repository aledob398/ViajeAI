package com.example.viajeai

import Perfil
import Preferencia
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CrearPerfilActivity : AppCompatActivity() {

    private val preferenciasSeleccionadas = mutableListOf<Preferencia>()
    private val perfilesCollection = FirebaseFirestore.getInstance().collection("perfiles")
    private lateinit var auth: FirebaseAuth
    private var PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef: StorageReference
    private var imagenSeleccionada: Uri? = null
    private lateinit var imageView: ImageView

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_perfil)
        val fotoPerfil = findViewById<ImageButton>(R.id.fotoPerfil)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        val btnCrearPerfil = findViewById<ImageButton>(R.id.btnCrearPerfil)
        setUpPreferenciaButtons()
        imageView = findViewById(R.id.imageViewClic)
        val animationView: ImageView = findViewById(R.id.imageViewClic)

        val animationDrawable: Drawable = animationView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }
        fotoPerfil.setOnClickListener {

            seleccionarImagen()



        }
        btnCrearPerfil.setOnClickListener {
            crearPerfilYUsuario()
        }
        val toogleContra = findViewById<ImageButton>(R.id.toogleContra)

        toogleContra.setOnClickListener {
            val editTextContraseña = findViewById<EditText>(R.id.editTextPassword)
            if (editTextContraseña.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                editTextContraseña.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT

                toogleContra.setImageResource(R.drawable.ic_visibility)
            } else {
                editTextContraseña.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toogleContra.setImageResource(R.drawable.ic_visibility_off)
            }
            editTextContraseña.setSelection(editTextContraseña.text.length)
        }

    }

    private fun crearPerfilYUsuario() {
        val nombreUsuario = findViewById<EditText>(R.id.editTextNombreUsuario)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)


        val nuevoPerfil =Perfil(FirebaseAuth.getInstance().uid.toString(),nombreUsuario.text.toString(), preferenciasSeleccionadas)


        if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty() && contraValida(passwordEditText.text.toString())) {
            auth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: "ID_DEFECTO_SI_NO_HAY_USUARIO_LOGUEADO"
                        almacenarPerfilEnBaseDeDatos(nuevoPerfil, userId)

                        auth.signInWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    subirImagen(userId)
                                    val intent = Intent(this, MisViajes::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    mostrarMensaje("Error al iniciar sesión después de crear el usuario: ${signInTask.exception?.message}")
                                }
                            }
                    } else {
                        mostrarMensaje("Error al crear el usuario: ${task.exception?.message}")
                    }
                }
        } else {
            passwordEditText.error = "La contraseña debe tener al menos 6 caracteres, incluyendo al menos una letra mayúscula y un carácter especial."
        }
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
    private fun contraValida(password: String): Boolean {
        if(password.length>=6){
            val pattern = "(?=.*[A-Z])(?=.*\\d).{8,}".toRegex()
            return pattern.matches(password)
        }else return false

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
            imageView.visibility = View.GONE
            val fotoUsuario = findViewById<ImageButton>(R.id.fotoPerfil)
            Glide.with(this).load(imagenSeleccionada).into(fotoUsuario)
        }
    }
    private fun subirImagen(userId: String) {
        val storageReference = storageRef.child("imagenes/$userId.jpg")
        storageReference.putFile(imagenSeleccionada!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
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
