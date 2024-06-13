package com.example.viajeai

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CostesAdapter(
    private val context: Context,
    private val idViaje: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val costes = mutableListOf<Coste>()

    companion object {
        private const val VIEW_TYPE_COSTE = 0
        private const val VIEW_TYPE_TOTAL = 1
    }

    fun addItem(coste: Coste) {
        costes.add(coste)
        notifyDataSetChanged()
    }

    fun setItems(costesList: List<Coste>) {
        costes.clear()
        costes.addAll(costesList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < costes.size) VIEW_TYPE_COSTE else VIEW_TYPE_TOTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COSTE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coste, parent, false)
            CosteViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_total, parent, false)
            TotalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CosteViewHolder) {
            val coste = costes[position]
            holder.bind(coste)
            if (position == 0) {
                updateProgressBar()
            }
        } else if (holder is TotalViewHolder) {
            holder.bind(getTotalCoste())
        }
    }
    private fun updateProgressBar() {
        val totalCoste = getTotalCoste()
        val progressBar = (context as CostesActivity).findViewById<ProgressBar>(R.id.progressBar)
        val textViewProgress = (context as CostesActivity).findViewById<TextView>(R.id.textViewProgress)
        val viaje = (context as CostesActivity).viaje
        progressBar.max = viaje.presupuesto!!.toInt()
        progressBar.progress = totalCoste.toInt()
        textViewProgress.text = "${(totalCoste / viaje.presupuesto!! * 100).toInt()}%"
    }


    override fun getItemCount() = costes.size + 1

    private fun getTotalCoste(): Double {
        return costes.sumOf { it.coste }
    }

    inner class CosteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDescripcion: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val textViewFecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val textViewCoste: TextView = itemView.findViewById(R.id.textViewCoste)
        private val btnModificar: ImageButton = itemView.findViewById(R.id.btnModificar)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)

        fun bind(coste: Coste) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateText = dateFormat.format(Date(coste.fecha))
            textViewFecha.text = dateText
            textViewDescripcion.text = "${coste.descripcion}"
            textViewCoste.text = "$${coste.coste}"

            btnModificar.setOnClickListener {
                showModifyCostDialog(adapterPosition)
            }

            btnEliminar.setOnClickListener {
                showDeleteCostDialog(adapterPosition)
            }
        }

        private fun showModifyCostDialog(position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_modify_cost, null)
            val editTextNuevoCoste = dialogView.findViewById<EditText>(R.id.editTextNuevoCoste)

            AlertDialog.Builder(context)
                .setTitle("Modificar Costo")
                .setView(dialogView)
                .setPositiveButton("Guardar") { dialog, _ ->
                    val nuevoCosteText = editTextNuevoCoste.text.toString()
                    val nuevoCoste = nuevoCosteText.toDoubleOrNull()

                    if (nuevoCoste != null) {
                        actualizarCosteEnFirestore(position, nuevoCoste)
                    } else {
                        Toast.makeText(context, "Ingrese un costo válido", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun actualizarCosteEnFirestore(position: Int, nuevoCoste: Double) {
            if (position != RecyclerView.NO_POSITION) {
                val coste = costes[position]
                val costeRef = FirebaseFirestore.getInstance().collection("costes").document(coste.id)
                val nuevaFecha = System.currentTimeMillis()

                costeRef.update(mapOf(
                    "coste" to nuevoCoste,
                    "fecha" to nuevaFecha
                ))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Costo actualizado correctamente", Toast.LENGTH_SHORT).show()
                        coste.coste = nuevoCoste
                        coste.fecha = nuevaFecha
                        notifyItemChanged(position)
                        notifyItemChanged(costes.size)
                        updateProgressBar()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al actualizar el costo", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        private fun eliminarCosteEnFirestore(position: Int) {
            if (position != RecyclerView.NO_POSITION) {
                val coste = costes[position]
                val costeRef = FirebaseFirestore.getInstance().collection("costes").document(coste.id)

                costeRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Costo eliminado correctamente", Toast.LENGTH_SHORT).show()
                        costes.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemChanged(costes.size)
                        updateProgressBar()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar el costo", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        private fun showDeleteCostDialog(position: Int) {
            AlertDialog.Builder(context)
                .setTitle("Eliminar Costo")
                .setMessage("¿Estás seguro de que deseas eliminar este costo?")
                .setPositiveButton("Eliminar") { dialog, _ ->
                    eliminarCosteEnFirestore(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        private fun updateProgressBar() {
            val totalCoste = getTotalCoste()
            val progressBar = (context as CostesActivity).findViewById<ProgressBar>(R.id.progressBar)
            val textViewProgress = (context as CostesActivity).findViewById<TextView>(R.id.textViewProgress)
            val viaje = (context as CostesActivity).viaje
            progressBar.max = viaje.presupuesto!!.toInt()
            progressBar.progress = totalCoste.toInt()
            textViewProgress.text = "${(totalCoste / viaje.presupuesto!! * 100).toInt()}%"
        }



    }

    inner class TotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTotal: TextView = itemView.findViewById(R.id.textViewTotal)

        fun bind(total: Double) {
            textViewTotal.text = "$${total}"
        }
    }
}
