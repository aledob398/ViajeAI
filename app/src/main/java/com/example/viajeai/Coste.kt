package com.example.viajeai
data class Coste(
    val id: String = "",
    val idViaje: String = "",
    val descripcion: String = "",
    var coste: Double = 0.0,
    var fecha: Long = System.currentTimeMillis()
)


