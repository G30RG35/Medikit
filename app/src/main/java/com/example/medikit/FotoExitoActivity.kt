package com.example.medikit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class FotoExitoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto_exito)

        val ruta = ImagenGlobal.rutaImagen
        if (ruta != null) {
            
            // Ejecutar análisis en segundo plano
            Thread {
                try {
                    val resultado = Detector.analizarImagen(this, ruta)
                    
                    // Volver al hilo principal
                    runOnUiThread {
                        if (resultado != null) {
                            ImagenGlobal.resultadoIA = resultado
                            // Persistir resultado localmente
                            try {
                                val registro = AnalisisStorage.crearRegistro(
                                    rutaImagen = ruta,
                                    clase = resultado.clase,
                                    confianza = resultado.confianza
                                )
                                AnalisisStorage.append(this, registro)
                            } catch (e: Exception) {
                                Log.e("FotoExitoActivity", "Error guardando análisis", e)
                            }
                            
                            // Esperar un momento y luego ir a resultados
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, ResultadoActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 1500)
                        } else {
                            Log.e("FotoExitoActivity", "Error en el análisis: resultado nulo")
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
                        Log.e("FotoExitoActivity", "Error en análisis", e)
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000)
                    }
                }
            }.start()
            
        } else {
            Log.e("FotoExitoActivity", "No se encontró la imagen tomada")
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
    }
}
