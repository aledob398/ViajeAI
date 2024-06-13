package com.example.viajeai

import Viaje
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Itinerario : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var openDrawer: ImageButton
    private var eliminando = false
    private var idViaje=""
    private val calendar = Calendar.getInstance()
    private lateinit var viaje: Viaje
    private val PICK_IMAGE_REQUEST = 1

    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerario)
        viaje = intent.getParcelableExtra("viaje")!!
        idViaje= intent.getStringExtra("idViaje").toString()
        val horarioLayout = findViewById<LinearLayout>(R.id.linearLayoutHorario)
        val textViewDate = findViewById<TextView>(R.id.textViewDate)
        val prevDayBtn = findViewById<ImageButton>(R.id.buttonPrevDay)
        val nextDayBtn = findViewById<ImageButton>(R.id.buttonNextDay)
        val deleteToggleBtn = findViewById<ImageButton>(R.id.ImageButtonEliminar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationview)
        openDrawer = findViewById(R.id.btn_open)

        updateDateDisplay(textViewDate)
        loadActivities(horarioLayout, getCurrentDate())
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
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    return@setNavigationItemSelectedListener true
                }

                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
        }
        prevDayBtn.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            updateDateDisplay(textViewDate)
            loadActivities(horarioLayout, getCurrentDate())
        }

        nextDayBtn.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            updateDateDisplay(textViewDate)
            loadActivities(horarioLayout, getCurrentDate())
        }



       deleteToggleBtn.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red_apagado), PorterDuff.Mode.SRC_IN)
        deleteToggleBtn.setOnClickListener {
            eliminando = !eliminando
            if (eliminando) {
                deleteToggleBtn.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)
            } else {
                deleteToggleBtn.drawable.setColorFilter(ContextCompat.getColor(this, R.color.red_apagado), PorterDuff.Mode.SRC_IN)
            }
        }

        findViewById<ImageButton>(R.id.btn_view_images).setOnClickListener {
            showImagesPopup()
        }
    }

    private fun getCurrentDate(): String {
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun updateDateDisplay(textViewDate: TextView) {
        textViewDate.text = getCurrentDate()
    }

    private fun loadActivities(horarioLayout: LinearLayout, date: String) {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1

        db.collection("actividades")
            .whereEqualTo("idUsuario", userId)
            .whereEqualTo("date", date)
            .whereEqualTo("day", day)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { documents ->
                val actividades = ArrayList<Actividad>()

                for (document in documents) {
                    val name = document.getString("name")
                    val startHour = document.getLong("startHour")?.toInt() ?: 0
                    val startMinute = document.getLong("startMinute")?.toInt() ?: 0
                    val endHour = document.getLong("endHour")?.toInt() ?: 0
                    val endMinute = document.getLong("endMinute")?.toInt() ?: 0
                    val tipo = document.getString("tipo")
                    val documentId = document.id
                    val activityDate = document.getString("date")
                    val activityDay = document.getLong("day")?.toInt() ?: 0
                    val activityMonth = document.getLong("month")?.toInt() ?: 0

                    val actividad = Actividad(
                        name!!, startHour, startMinute, endHour, endMinute, tipo, documentId, activityDate!!, activityDay, activityMonth
                    )
                    actividades.add(actividad)
                }


                actividades.sortWith(compareBy({ it.startHour }, { it.startMinute }))


                horarioLayout.removeAllViews()


                for (actividad in actividades) {
                    val cardView = createActivityCardView(actividad, horarioLayout)
                    horarioLayout.addView(cardView)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Itinerario", "Error getting documents: ", exception)
            }
    }


    private fun createActivityCardView(actividad: Actividad, horarioLayout: LinearLayout): CardView {
        val cardView = CardView(this)
        cardView.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16)
        }
        cardView.radius = 8F
        cardView.cardElevation = 8F

        val button = Button(this)
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        button.text = "${actividad.name} (${actividad.startHour}:${actividad.startMinute} - ${actividad.endHour}:${actividad.endMinute})"

        when (actividad.tipo) {
            "deportes" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_downhill_skiing_24,
                    0,
                    0,
                    0
                )

            }
            "ocio_nocturno" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark5)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_celebration_24,
                    0,
                    0,
                    0
                )

            }
            "puntos_turisticos" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark4)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_place_24,
                    0,
                    0,
                    0
                )
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            "tiendas" -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark2)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_shopping_bag_24,
                    0,
                    0,
                    0,
                )

            }
            else -> {
                button.setBackgroundResource(R.drawable.button_background_lila_dark3)
                button.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_dining_24_2,
                    0,
                    0,
                    0
                )
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        button.setOnClickListener {
            if (eliminando) {
                confirmarEliminacion(actividad, horarioLayout, cardView)
            } else {
                val intent = Intent(this, DetalleGeneral::class.java).apply {
                    putExtra("name", actividad.name)
                    putExtra("type", actividad.tipo)
                }
                startActivity(intent)
            }
        }

        cardView.addView(button)
        return cardView
    }

    private fun confirmarEliminacion(
        actividad: Actividad,
        horarioLayout: LinearLayout,
        cardView: CardView
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar actividad")
        builder.setMessage("¿Estás seguro de que deseas eliminar esta actividad?")
        builder.setPositiveButton("Sí") { _, _ ->

            db.collection("actividades").document(actividad.documentId)
                .delete()
                .addOnSuccessListener {
                    Log.d("Itinerario", "Actividad eliminada correctamente")

                    horarioLayout.removeView(cardView)
                }
                .addOnFailureListener { e ->
                    Log.w("Itinerario", "Error al eliminar la actividad", e)
                }
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showImagesPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Subir imágenes")

        val scrollView = ScrollView(this)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL


        loadImagesFromFirebase(linearLayout, intent.getStringExtra("idViaje")!!)


        val addButton = Button(this)
        addButton.text = "Añadir imagen"

        addButton.setOnClickListener {
            openImagePicker()
        }


        addButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }


        linearLayout.addView(addButton)


       scrollView.addView(linearLayout)


        builder.setView(scrollView)
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun uploadImageToFirebase(imageUri: Uri, idViaje: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("images/$idViaje/${imageUri.lastPathSegment}")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            Log.d("Itinerario", "Imagen subida exitosamente")
        }.addOnFailureListener { exception ->
            Log.w("Itinerario", "Error al subir la imagen", exception)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                val idViaje = intent.getStringExtra("idViaje")
                uploadImageToFirebase(it, idViaje!!)
            }
        }
    }

    private fun loadImagesFromFirebase(linearLayout: LinearLayout, idViaje: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$idViaje")
        storageReference.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        val cardView = CardView(this@Itinerario)
                        val cardParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400
                        )
                        cardParams.setMargins(16, 16, 16, 16)
                        cardView.layoutParams = cardParams
                        cardView.radius = 8F
                        cardView.cardElevation = 8F

                        val imageView = ImageView(this@Itinerario)
                        val imageParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        imageView.layoutParams = imageParams
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER


                        Glide.with(this@Itinerario)
                            .load(uri)
                            .into(imageView)

                        cardView.addView(imageView)
                        linearLayout.addView(cardView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Itinerario", "Error loading images", exception)
            }
    }


    data class Actividad(
        val name: String,
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int,
        val tipo: String?,
        val documentId: String,
        val date: String,
        val day: Int,
        val month: Int
    )
}
