package com.example.medikit

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PerfilActivity : AppCompatActivity() {
    private lateinit var tvNombre: TextView
    private lateinit var tvApellidos: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvFechaRegistro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        tvNombre = findViewById(R.id.tvNombre)
        tvApellidos = findViewById(R.id.tvApellidos)
        tvEmail = findViewById(R.id.tvEmail)
        tvFechaRegistro = findViewById(R.id.tvFechaRegistro)

        val btnCerrarSesion = findViewById<android.widget.Button>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = android.content.Intent(this, Inicio::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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
                        tvNombre.text = "Nombre: $nombre"
                        tvApellidos.text = "Apellidos: $apellidoPaterno $apellidoMaterno"
                        if (fechaRegistro > 0) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val fecha = Date(fechaRegistro)
                            tvFechaRegistro.text = "Fecha de registro: ${sdf.format(fecha)}"
                        }
                    }
                }
        }
    }
}
