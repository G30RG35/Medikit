package com.example.medikit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PerfilActivity : AppCompatActivity() {
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellidoPaterno: TextInputEditText
    private lateinit var etApellidoMaterno: TextInputEditText
    private lateinit var tvEmail: TextView
    private lateinit var tvFechaRegistro: TextView
    private lateinit var btnEditarPerfil: Button
    private lateinit var btnGuardarPerfil: Button
    private lateinit var btnCerrarSesion: Button

    private var modoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        inicializarVistas()
        cargarDatosPerfil()
        configurarBotones()
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombre)
        etApellidoPaterno = findViewById(R.id.etApellidoPaterno)
        etApellidoMaterno = findViewById(R.id.etApellidoMaterno)
        tvEmail = findViewById(R.id.tvEmail)
        tvFechaRegistro = findViewById(R.id.tvFechaRegistro)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun configurarBotones() {
        btnEditarPerfil.setOnClickListener {
            activarModoEdicion()
        }

        btnGuardarPerfil.setOnClickListener {
            guardarCambios()
        }

        btnCerrarSesion.setOnClickListener {
            // Eliminar email guardado en SharedPreferences
            val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            prefs.edit().remove("email").apply()

            FirebaseAuth.getInstance().signOut()
            val intent = android.content.Intent(this, Inicio::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDatosPerfil() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            tvEmail.text = "Email: ${user.email}"
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: ""
                        val apellidoPaterno = document.getString("apellidoPaterno") ?: ""
                        val apellidoMaterno = document.getString("apellidoMaterno") ?: ""
                        val fechaRegistro = document.getLong("fechaRegistro") ?: 0L
                        
                        etNombre.setText(nombre)
                        etApellidoPaterno.setText(apellidoPaterno)
                        etApellidoMaterno.setText(apellidoMaterno)
                        
                        if (fechaRegistro > 0) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val fecha = Date(fechaRegistro)
                            tvFechaRegistro.text = "Fecha de registro: ${sdf.format(fecha)}"
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar los datos del perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun activarModoEdicion() {
        modoEdicion = true
        etNombre.isEnabled = true
        etApellidoPaterno.isEnabled = true
        etApellidoMaterno.isEnabled = true
        
        btnEditarPerfil.visibility = View.GONE
        btnGuardarPerfil.visibility = View.VISIBLE
    }

    private fun desactivarModoEdicion() {
        modoEdicion = false
        etNombre.isEnabled = false
        etApellidoPaterno.isEnabled = false
        etApellidoMaterno.isEnabled = false
        
        btnEditarPerfil.visibility = View.VISIBLE
        btnGuardarPerfil.visibility = View.GONE
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val apellidoPaterno = etApellidoPaterno.text.toString().trim()
        val apellidoMaterno = etApellidoMaterno.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es obligatorio"
            etNombre.requestFocus()
            return
        }

        if (apellidoPaterno.isEmpty()) {
            etApellidoPaterno.error = "El apellido paterno es obligatorio"
            etApellidoPaterno.requestFocus()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val datosActualizados = hashMapOf(
                "nombre" to nombre,
                "apellidoPaterno" to apellidoPaterno,
                "apellidoMaterno" to apellidoMaterno
            )

            db.collection("usuarios").document(user.uid)
                .update(datosActualizados as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    desactivarModoEdicion()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}