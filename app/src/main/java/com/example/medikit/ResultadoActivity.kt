package com.example.medikit

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultadoActivity : AppCompatActivity() {
    
    private lateinit var ivImagenAnalizada: ImageView
    private lateinit var tvClaseDetectada: TextView
    private lateinit var tvPorcentajeConfianza: TextView
    private lateinit var pbConfianza: ProgressBar
    private lateinit var tvRecomendaciones: TextView
    private lateinit var btnTomarOtraFoto: MaterialButton
    private lateinit var btnVolverHome: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        inicializarVistas()
        mostrarResultado()
        configurarBotones()
    }

    private fun inicializarVistas() {
        ivImagenAnalizada = findViewById(R.id.ivImagenAnalizada)
        tvClaseDetectada = findViewById(R.id.tvClaseDetectada)
        tvPorcentajeConfianza = findViewById(R.id.tvPorcentajeConfianza)
        pbConfianza = findViewById(R.id.pbConfianza)
        tvRecomendaciones = findViewById(R.id.tvRecomendaciones)
        btnTomarOtraFoto = findViewById(R.id.btnTomarOtraFoto)
        btnVolverHome = findViewById(R.id.btnVolverHome)
    }

    private fun mostrarResultado() {
        var rutaImagen = ImagenGlobal.rutaImagen
        var resultado = ImagenGlobal.resultadoIA

        // Si no hay datos en memoria, cargar el último guardado
        if (resultado == null) {
            val reg = AnalisisStorage.leerUltimo(this)
            if (reg != null) {
                rutaImagen = reg.rutaImagen
                resultado = Detector.ResultadoIA(reg.clase, reg.confianza)
            }
        }

        // Mostrar imagen analizada si está disponible
        rutaImagen?.let { ruta ->
            val bitmap = BitmapFactory.decodeFile(ruta)
            ivImagenAnalizada.setImageBitmap(bitmap)
        }

        // Mostrar resultado del análisis
        if (resultado != null) {
            tvClaseDetectada.text = resultado.clase
            tvPorcentajeConfianza.text = "${resultado.porcentaje}%"
            pbConfianza.progress = resultado.porcentaje
            
            // Cambiar colores según confianza
            if (resultado.esConfiable) {
                pbConfianza.progressTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#4CAF50")
                )
                tvPorcentajeConfianza.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                pbConfianza.progressTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FF9800")
                )
                tvPorcentajeConfianza.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            }
            
            // Mostrar recomendaciones según el tipo de lesión
            tvRecomendaciones.text = obtenerRecomendaciones(resultado.clase, resultado.esConfiable)
        } else run {
            // Si no hay resultado, mostrar error
            tvClaseDetectada.text = "Error en el análisis"
            tvPorcentajeConfianza.text = "0%"
            pbConfianza.progress = 0
            tvRecomendaciones.text = "No se pudo analizar la imagen. Por favor, intenta tomar otra foto."
        }
    }

    private fun obtenerRecomendaciones(clase: String, esConfiable: Boolean): String {
        val confiabilidad = if (esConfiable) "" else "\n\n⚠️ Nota: El nivel de confianza es bajo. Se recomienda consultar con un profesional médico."
        
        return when (clase) {
            "Cortaduras" -> """
                • Limpiar la herida con agua limpia
                • Aplicar presión para detener el sangrado
                • Desinfectar con antiséptico
                • Cubrir con vendaje estéril
                • Consultar médico si es profunda$confiabilidad
            """.trimIndent()
            
            "Quemaduras" -> """
                • Enfriar inmediatamente con agua fría
                • NO aplicar hielo directamente
                • Cubrir con vendaje húmedo y estéril
                • NO reventar ampollas
                • Consultar médico si es extensa$confiabilidad
            """.trimIndent()
            
            "Hematomas" -> """
                • Aplicar hielo envuelto en tela
                • Elevar la zona afectada
                • Evitar aplicar calor las primeras 48h
                • Descansar la zona lesionada
                • Consultar si el dolor persiste$confiabilidad
            """.trimIndent()
            
            "Esguinces" -> """
                • Reposo y evitar movimientos bruscos
                • Aplicar hielo 15-20 minutos cada 2-3 horas
                • Comprimir con vendaje elástico
                • Elevar la extremidad afectada
                • Consultar médico si hay deformidad$confiabilidad
            """.trimIndent()
            
            "Fracturas" -> """
                ⚠️ ATENCIÓN MÉDICA INMEDIATA
                • NO mover el área lesionada
                • Inmovilizar si es posible
                • Aplicar hielo envuelto en tela
                • Buscar ayuda médica urgente
                • NO dar medicamentos por vía oral$confiabilidad
            """.trimIndent()
            
            "Raspaduras" -> """
                • Limpiar con agua y jabón suave
                • Remover suciedad visible
                • Aplicar antiséptico
                • Cubrir con vendaje si es necesario
                • Mantener limpio y seco$confiabilidad
            """.trimIndent()
            
            "Alergias" -> """
                • Identificar y evitar el alérgeno
                • Lavar la zona con agua fría
                • Aplicar compresas frías
                • NO rascar la zona afectada
                • Consultar médico si empeora$confiabilidad
            """.trimIndent()
            
            "No lesiones" -> """
                • No se detectaron lesiones visibles
                • Mantener higiene adecuada
                • Observar cualquier cambio
                • Si hay dolor persistente, consultar médico
                • Prevenir lesiones futuras$confiabilidad
            """.trimIndent()
            
            else -> """
                • Mantener la zona limpia
                • Observar evolución
                • Consultar con profesional médico
                • Aplicar primeros auxilios básicos si es necesario$confiabilidad
            """.trimIndent()
        }
    }

    private fun configurarBotones() {
        btnTomarOtraFoto.setOnClickListener {
            // Limpiar datos y volver a captura
            ImagenGlobal.limpiarDatos()
            val intent = Intent(this, CapturaActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnVolverHome.setOnClickListener {
            // Limpiar datos y volver al home
            ImagenGlobal.limpiarDatos()
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Al presionar atrás, ir al home
        ImagenGlobal.limpiarDatos()
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}