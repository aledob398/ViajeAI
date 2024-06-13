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
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.regex.Pattern

class CrearPerfilGoogleActivity : AppCompatActivity() {

    private val preferenciasSeleccionadas = mutableListOf<Preferencia>()
    private val perfilesCollection = FirebaseFirestore.getInstance().collection("perfiles")
    private lateinit var auth: FirebaseAuth
    private var PICK_IMAGE_REQUEST = 1
    private lateinit var storageRef: StorageReference
    private var imagenSeleccionada: Uri? = null
    private lateinit var imageView: ImageView
    private val RC_SIGN_IN = 9001

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_perfil_google)


        imageView = findViewById(R.id.imageViewClic)

        val fotoPerfil = findViewById<ImageButton>(R.id.fotoPerfil)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        val btnCrearPerfil = findViewById<ImageButton>(R.id.btnCrearPerfilGoogle)

        val animationDrawable: Drawable = imageView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)


        btnCrearPerfil.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            crearPerfil()
        }

        fotoPerfil.setOnClickListener {
            seleccionarImagen()
        }

        setUpPreferenciaButtons()
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

    private fun contraValida(password: String): Boolean {
        val condiciones = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +
                    "(?=.*[A-Z])" +
                    "(?=.*[@#\$%^&+=])" +
                    "(?=\\S+\$)" +
                    ".{6,}" +
                    "\$"
        )
        return condiciones.matcher(password).matches()
    }

    private fun crearPerfil() {
        val nombreUsuario = findViewById<EditText>(R.id.editTextNombreUsuario)
        FirebaseAuth.getInstance().uid
        val nuevoPerfil = Perfil(FirebaseAuth.getInstance().uid.toString(), nombreUsuario.text.toString(), preferenciasSeleccionadas)

        val userId = obtenerIdUsuario()
        subirImagen(userId)
        almacenarPerfilEnBaseDeDatos(nuevoPerfil, userId)
    }

    private fun seleccionarImagen() {
        val intent = Intent().setType("image/*")
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        when (requestCode) {
            RC_SIGN_IN -> {
                if (resultCode == RESULT_OK && data != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {

                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {

                        Toast.makeText(this, "Error al autenticar con Google: ${e.message}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            PICK_IMAGE_REQUEST -> {
                if (resultCode == RESULT_OK && data != null && data.data != null) {
                    imagenSeleccionada = data.data
                    imageView.visibility = View.GONE
                    val fotoUsuario = findViewById<ImageButton>(R.id.fotoPerfil)
                    Glide.with(this).load(imagenSeleccionada).into(fotoUsuario)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    Toast.makeText(this, "Autenticación con Google exitosa", Toast.LENGTH_SHORT).show()

                } else {

                    Toast.makeText(this, "Error al autenticar con Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
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

    private fun obtenerIdUsuario(): String {
        val currentUser = auth.currentUser
        return currentUser?.uid ?: "ID_DEFECTO_SI_NO_HAY_USUARIO_LOGUEADO"
    }

    private fun almacenarPerfilEnBaseDeDatos(perfil: Perfil, userId: String) {
        perfilesCollection.document(userId)
            .set(perfil)
            .addOnSuccessListener {
                mostrarMensaje("Perfil creado y almacenado en la base de datos")
                val intent = Intent(this, MisViajes::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                mostrarMensaje("Error al almacenar el perfil en la base de datos: $e")
            }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}

