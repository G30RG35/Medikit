package com.example.medikit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FotoExitoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto_exito)

        val ruta = ImagenGlobal.rutaImagen
        if (ruta != null) {
            Toast.makeText(this, "Analizando imagen con IA...", Toast.LENGTH_SHORT).show()
            
            // Ejecutar análisis en segundo plano
            Thread {
                try {
                    val resultado = Detector.analizarImagen(this, ruta)
                    
                    // Volver al hilo principal
                    runOnUiThread {
                        if (resultado != null) {
                            ImagenGlobal.resultadoIA = resultado
                            Toast.makeText(this, "Análisis completado", Toast.LENGTH_SHORT).show()
                            
                            // Esperar un momento y luego ir a resultados
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, ResultadoActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 1500)
                        } else {
                            Toast.makeText(this, "Error en el análisis", Toast.LENGTH_LONG).show()
                            // En caso de error, volver al home
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, Home::class.java)
                                startActivity(intent)
                                finish()
                            }, 2000)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000)
                    }
                }
            }.start()
            
        } else {
            Toast.makeText(this, "No se encontró la imagen tomada", Toast.LENGTH_LONG).show()
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
    }
}
