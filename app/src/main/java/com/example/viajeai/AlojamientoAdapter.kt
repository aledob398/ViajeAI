package com.example.viajeai

import Alojamiento
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class AlojamientoAdapter(private val alojamientos: ArrayList<Alojamiento>) :
    RecyclerView.Adapter<AlojamientoAdapter.AlojamientoViewHolder>() {

    fun updateData(newData: List<Alojamiento>) {
        alojamientos.clear()
        alojamientos.addAll(newData)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlojamientoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alojamiento, parent, false)
        return AlojamientoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlojamientoViewHolder, position: Int) {
        val alojamiento = alojamientos[position]
        holder.bind(alojamiento)
    }

    override fun getItemCount() = alojamientos.size

    inner class AlojamientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnDetalle: Button = itemView.findViewById(R.id.btnDetalleAlojamiento)

        fun bind(alojamiento: Alojamiento) {

            btnDetalle.text = alojamiento.name

            btnDetalle.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetalleGeneral::class.java)
                intent.putExtra("type", "alojamientos")
                intent.putExtra("name", "${alojamiento.name.toString()}")
                context.startActivity(intent)
            }
        }
    }
}
