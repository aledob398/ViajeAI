package com.example.viajeai

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class RestauranteAdapter(private val restaurantes: ArrayList<Restaurante>) :
    RecyclerView.Adapter<RestauranteAdapter.RestauranteViewHolder>() {

    fun updateData(newData: List<Restaurante>) {
        restaurantes.clear()
        restaurantes.addAll(newData)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestauranteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurante, parent, false)
        return RestauranteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RestauranteViewHolder, position: Int) {
        val restaurante = restaurantes[position]
        holder.bind(restaurante)
    }

    override fun getItemCount() = restaurantes.size

    inner class RestauranteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnDetalle: Button = itemView.findViewById(R.id.btnDetalleRestaurante)

        fun bind(restaurante: Restaurante) {

            btnDetalle.text = restaurante.name


            btnDetalle.setOnClickListener {
                val context = itemView.context

                val intent = Intent(context, DetalleGeneral::class.java)
                intent.putExtra("type", "restaurantes")
                intent.putExtra("name", "${restaurante.name.toString()}")

                context.startActivity(intent)
            }
        }
    }
}
