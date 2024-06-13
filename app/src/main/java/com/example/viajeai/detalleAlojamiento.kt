package com.example.viajeai


import Alojamiento
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class detalleAlojamiento : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_alojamiento)

        val alojamiento = intent.getParcelableExtra<Alojamiento>("alojamiento")

        val textViewNombreAlojamiento = findViewById<TextView>(R.id.textViewNombreAlojamiento)
        val textViewDescripcionAlojamiento = findViewById<TextView>(R.id.textViewDescripcionAlojamiento)
        val textViewEmailAlojamiento = findViewById<TextView>(R.id.textViewEmailAlojamiento)
        val textViewTelefonoAlojamiento = findViewById<TextView>(R.id.textViewTelefonoAlojamiento)
        val textViewWebAlojamiento = findViewById<TextView>(R.id.textViewWebAlojamiento)
        val imageViewAlojamiento = findViewById<ImageView>(R.id.imageViewAlojamiento)

        textViewNombreAlojamiento.text = alojamiento?.name
        textViewDescripcionAlojamiento.text = alojamiento?.descripcion
        textViewEmailAlojamiento.text = alojamiento?.email
        textViewTelefonoAlojamiento.text = alojamiento?.phone
        textViewWebAlojamiento.text = alojamiento?.web
        Glide.with(this)
            .load(alojamiento?.imageUrl)
            .placeholder(R.drawable.signin)
            .error(R.drawable.signin)
            .into(imageViewAlojamiento)

    }
}
