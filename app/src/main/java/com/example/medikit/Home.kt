package com.example.medikit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val btnPerfil = findViewById<android.widget.Button>(R.id.btnPerfil)
        btnPerfil.setOnClickListener {
            val intent = android.content.Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        val btnCaptura = findViewById<android.widget.Button>(R.id.btnCaptura)
        btnCaptura.setOnClickListener {
            val intent = android.content.Intent(this, CapturaActivity::class.java)
            startActivity(intent)
        }

        val btnHistorial = findViewById<android.widget.Button>(R.id.btnHistorial)
        btnHistorial.setOnClickListener {
            val intent = android.content.Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }
    }
}