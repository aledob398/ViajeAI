package com.example.viajeai

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

internal enum class ProviderType {
    BASIC,
    GOOGLE
}

class MainActivity : AppCompatActivity() {
    private var googleSignInClient: GoogleSignInClient? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var passwordEditText: EditText? = null
    private var visibilityToggle: ImageButton? = null
    private var isPasswordVisible = false

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        setupGoogleSignIn()
        setup()

        if (FirebaseAuth.getInstance().currentUser != null) {

            val intent = Intent(
                this,
                MisViajes::class.java
            )
            startActivity(intent)
            finish()
        }

        passwordEditText = findViewById(R.id.contraseña)
        val toogleContra = findViewById<ImageButton>(R.id.toogleContra)

        toogleContra.setOnClickListener {
            val editTextContraseña = findViewById<EditText>(R.id.contraseña)
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

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText!!.transformationMethod = PasswordTransformationMethod.getInstance()
            visibilityToggle!!.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordEditText!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
            visibilityToggle!!.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        passwordEditText!!.setSelection(passwordEditText!!.text.length) // Move cursor to the end
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @SuppressLint("WrongViewCast")
    private fun setup() {
        val signUpButton = findViewById<ImageButton>(R.id.registrarse)
        val logInButton = findViewById<ImageButton>(R.id.acceder)
        val googleSignInButton = findViewById<ImageButton>(R.id.googleSignInButton)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.contraseña)

        title = "Autenticación"

        signUpButton.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                CrearPerfilActivity::class.java
            )
            startActivity(intent)
        }

        logInButton.setOnClickListener {
            if (emailEditText.text.length > 0 && passwordEditText.text.length > 0) {
                if (contraValida(passwordEditText.text.toString())) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        emailEditText.text.toString(),
                        passwordEditText.text.toString()
                    ).addOnCompleteListener { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@MainActivity, MisViajes::class.java)
                            startActivity(intent)
                        } else {
                            showAlert()
                        }
                    }
                } else {
                    passwordEditText.error = "La contraseña debe tener al menos 6 caracteres, incluyendo al menos una letra mayúscula, un número y un carácter especial."
                }
            } else {
                if (emailEditText.text.isEmpty()) {
                    emailEditText.error = "El correo no puede estar vacío"
                }
                if (passwordEditText.text.isEmpty()) {
                    passwordEditText.error = "La contraseña no puede estar vacía"
                }
            }
        }

        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(
                    ApiException::class.java
                )
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }

    private fun contraValida(password: String): Boolean {
        if(password.length>=6){
            val pattern = "(?=.*[A-Z])(?=.*\\d).{8,}".toRegex()
            return pattern.matches(password)
        }else return false

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth!!.signInWithCredential(credential).addOnCompleteListener(this) { authResult ->
            if (authResult.isSuccessful) {
                val isNewUser = authResult.result?.additionalUserInfo?.isNewUser ?: true
                if (isNewUser) {

                    val intent = Intent(this, CrearPerfilGoogleActivity::class.java)
                    startActivity(intent)
                } else {

                    val intent = Intent(this, MisViajes::class.java)
                    startActivity(intent)
                }
            } else {
                showAlert()
            }
        }
    }

    private fun showAlert() {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Se ha producido un error autentificando el usuario")
            .setPositiveButton("Aceptar", null)
            .create()
            .show()
    }
}
