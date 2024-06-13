package com.example.viajeai

import Viaje
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class CostesActivity : AppCompatActivity() {

    lateinit var viaje: Viaje
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddCost: Button
    private lateinit var costesAdapter: CostesAdapter
    private var totalCoste = 0.0
    private var idViaje: String? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: Button
    private lateinit var lineChart: LineChart
    private lateinit var textViewProgress: TextView

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_costes)

        viaje = intent.getParcelableExtra("viaje")!!
        idViaje = intent.getStringExtra("idViaje")

        if (idViaje == null) {
            Toast.makeText(this, "ID del viaje no disponible.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerViewCostes)
        btnAddCost = findViewById(R.id.btnAddCost)
        lineChart = findViewById(R.id.lineChart)
        textViewProgress = findViewById(R.id.textViewProgress)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)
        val animationView: ImageView = findViewById(R.id.btnAddImage)

        // Cargar la animación WebP
        val animationDrawable: Drawable = animationView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        costesAdapter = CostesAdapter(this, idViaje!!)
        recyclerView.adapter = costesAdapter

        loadCostes()
        btnAddCost.setOnClickListener {
            showAddCostDialog()
        }

        openDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.one -> {
                    val intent = Intent(this, MisViajes::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)

                    return@setNavigationItemSelectedListener true
                }
                R.id.two -> {
                    val intent = Intent(this, MiPerfilActivity::class.java)

                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.three -> {
                    val intent = Intent(this, ActividadesAleatorias::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.four -> {
                    val intent = Intent(this, Itinerario::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.five -> {
                    val intent = Intent(this, Alojamientos_restaurantes::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.six -> {
                    val intent = Intent(this, CostesActivity::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.seven -> {
                    val intent = Intent(this, DetalleViajeActivity::class.java)
                    intent.putExtra("viaje",viaje)
                    intent.putExtra("idViaje",idViaje)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }
                R.id.eight -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }

                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
        }
        setupLineChart()
    }

    private fun setupLineChart() {

        val xAxis: XAxis = lineChart.xAxis
        xAxis.textColor = Color.WHITE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

               val yAxisLeft: YAxis = lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.setDrawGridLines(false)


        val yAxisRight: YAxis = lineChart.axisRight
        yAxisRight.isEnabled = false

        // Configuración de la leyenda
        val legend = lineChart.legend
        legend.textColor = Color.WHITE

        // Configuración de la descripción
        lineChart.description.textColor = Color.WHITE
        lineChart.description.text = ""

        lineChart.invalidate()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddCostDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_cost, null)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val editTextCost = dialogView.findViewById<EditText>(R.id.editTextCost)

        dialogBuilder.setView(dialogView)
            .setPositiveButton("Agregar") { dialog, which ->
                val description = editTextDescription.text.toString()
                val cost = editTextCost.text.toString().toDoubleOrNull()
                if (description.isNotEmpty() && cost != null) {
                    addCoste(description, cost)
                } else {
                    Toast.makeText(this, "Por favor, introduce una descripción y un coste válido.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun loadCostes() {
        firestore.collection("costes")
            .whereEqualTo("idViaje", idViaje)
            .get()
            .addOnSuccessListener { documents ->
                val costes = mutableListOf<Coste>()
                for (document in documents) {
                    val descripcion = document.getString("descripcion") ?: ""
                    val coste = document.getDouble("coste") ?: 0.0
                    val fecha = document.getLong("fecha") ?: System.currentTimeMillis()
                    val id = document.id
                    if(coste>0){
                        val newCoste = Coste(id, idViaje!!, descripcion, coste, fecha)
                        costes.add(newCoste)
                        totalCoste += coste
                    }

                }
                costesAdapter.setItems(costes)
                updateProgressBar()
                analizarGastos(costes)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los costes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addCoste(descripcion: String, coste: Double) {
        val fecha = System.currentTimeMillis()
        val nuevoDocumento = hashMapOf(
            "idViaje" to idViaje,
            "descripcion" to descripcion,
            "coste" to coste,
            "fecha" to fecha
        )

        firestore.collection("costes")
            .add(nuevoDocumento)
            .addOnSuccessListener { documentReference ->
                val newCoste = Coste(documentReference.id, idViaje!!, descripcion, coste, fecha)
                costesAdapter.addItem(newCoste)
                totalCoste += coste
                updateProgressBar()
                analizarGastos(costesAdapter.costes)
                Toast.makeText(this, "Coste añadido con éxito.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al añadir el coste: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgressBar() {
        progressBar.max = viaje.presupuesto!!.toInt()
        progressBar.progress = totalCoste.toInt()
        textViewProgress.text = "${totalCoste.toInt()}%"
    }

    private fun analizarGastos(costes: List<Coste>) {

        val mapFechasTotales = TreeMap<String, Float>()

        for (coste in costes) {

            val fecha = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(coste.fecha))


            val totalCoste = mapFechasTotales[fecha] ?: 0.0f

            mapFechasTotales[fecha] = totalCoste + coste.coste.toFloat()
        }


        val fechasUnicas = mapFechasTotales.keys.toList()
        val totales = fechasUnicas.map { mapFechasTotales[it] ?: 0.0f }

        actualizarLineChart(fechasUnicas, totales)
    }


    fun calcularCostesDiarios(costes: List<Coste>): Map<String, Float> {
        val mapGastosDiarios = mutableMapOf<String, Float>()

        for (coste in costes) {
            val fecha = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(coste.fecha))
            val gastoDiario = mapGastosDiarios[fecha] ?: 0.0f
            mapGastosDiarios[fecha] = gastoDiario + coste.coste.toFloat()
        }

        return mapGastosDiarios
    }

    private fun actualizarLineChart(fechas: List<String>, totales: List<Float>) {
        val entries = mutableListOf<Entry>()
        for (i in totales.indices) {
            entries.add(Entry(i.toFloat(), totales[i]))
        }

        val lineDataSet = LineDataSet(entries, "Costes Diarios")
        lineDataSet.color = Color.WHITE
        lineDataSet.valueTextColor = Color.WHITE
        val lineData = LineData(lineDataSet)

        lineChart.data = lineData
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(fechas)
        lineChart.invalidate()
    }

}
