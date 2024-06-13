package com.example.viajeai

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class ActividadAdapter(private val eventos: ArrayList<Evento>) :
    RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    fun updateData(newData: List<Evento>) {
        eventos.clear()
        eventos.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad, parent, false)
        return ActividadViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
    }

    override fun getItemCount() = eventos.size

    inner class ActividadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnDetalle: Button = itemView.findViewById(R.id.btnDetalleActividad)

        fun bind(evento: Evento) {
           
            btnDetalle.text = evento.name

           
            btnDetalle.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetalleGeneral::class.java)
                intent.putExtra("type", evento.tipo)
                intent.putExtra("name", evento.name)
                intent
                context.startActivity(intent)
            }

          
            when (evento.tipo) {
                "deportes" -> {
                  //  btnDetalle.setBackgroundResource(R.drawable.button_background_selected)
                  //  btnDetalle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_dining_24, 0, 0, 0)
                }
                "puntos_turisticos" -> {
                 //   btnDetalle.setBackgroundResource(R.drawable.button_background_selected)
                   // btnDetalle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_dining_24, 0, 0, 0)
                }
                "tiendas" -> {
                  //  btnDetalle.setBackgroundResource(R.drawable.button_background_selected)
                   // btnDetalle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_dining_24, 0, 0, 0)
                }
                "ocio_nocturno" -> {
                  //  btnDetalle.setBackgroundResource(R.drawable.button_background_selected)
                  //  btnDetalle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_dining_24, 0, 0, 0)
                }
                "alojamientos" -> {
                  //  btnDetalle.setBackgroundResource(R.drawable.button_selector_unselected)
                   // btnDetalle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_hotel_24, 0, 0, 0)
                }
                else -> {
                  //  btnDetalle.setBackgroundResource(R.drawable.baseline_hotel_24)
                }
            }
        }
    }
}
