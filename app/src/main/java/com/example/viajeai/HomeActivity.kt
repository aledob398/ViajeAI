package com.example.viajeai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val bundle:Bundle?=intent.extras
        val email:String?=bundle?.getString("email")
        val provider:String?=bundle?.getString("provider")
        setup(email?:"",provider?:"")

    }

    private fun setup(email:String,provider:String){
        title="inicio"
        val emailTextView:TextView =findViewById<TextView>(R.id.email)
        val proveedorTextView:TextView=findViewById<TextView>(R.id.proveedor)
        val logOutButton:Button=findViewById<Button>(R.id.logOutButton)
        emailTextView.text=email
        proveedorTextView.text=provider

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }
}