package com.example.medikit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Configurar el click del botón de Registro
        val botonRegistro = findViewById<Button>(R.id.btnIrARegistro)
        botonRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Configurar el click del botón de Iniciar Sesión (si lo necesitas)
        val botonIniciarSesion = findViewById<Button>(R.id.btnIrLogin)
        botonIniciarSesion.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}