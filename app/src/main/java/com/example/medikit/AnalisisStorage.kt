package com.example.medikit

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AnalisisStorage {
    private const val FILE_NAME = "analisis_medikit.jsonl" // JSON Lines: una línea por análisis

    data class RegistroAnalisis(
        val timestamp: Long,
        val fecha: String,
        val rutaImagen: String,
        val clase: String,
        val confianza: Float
    )

    fun append(context: Context, registro: RegistroAnalisis) {
        val file = File(context.filesDir, FILE_NAME)
        val json = JSONObject()
            .put("timestamp", registro.timestamp)
            .put("fecha", registro.fecha)
            .put("rutaImagen", registro.rutaImagen)
            .put("clase", registro.clase)
            .put("confianza", registro.confianza)
        file.appendText(json.toString() + "\n")
    }

    fun leerUltimo(context: Context): RegistroAnalisis? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null
        val lines = file.readLines()
        if (lines.isEmpty()) return null
        return fromJson(lines.last())
    }

    fun leerTodos(context: Context): List<RegistroAnalisis> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { runCatching { fromJson(it) }.getOrNull() }
    }

    fun eliminarPorTimestamp(context: Context, timestamp: Long): Boolean {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return false
        val actuales = file.readLines().mapNotNull { runCatching { fromJson(it) }.getOrNull() }
        val nuevos = actuales.filterNot { it.timestamp == timestamp }
        if (nuevos.size == actuales.size) return false // no found
        escribirTodos(context, nuevos)
        return true
    }

    private fun escribirTodos(context: Context, registros: List<RegistroAnalisis>) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText("")
        registros.forEach { r ->
            val json = JSONObject()
                .put("timestamp", r.timestamp)
                .put("fecha", r.fecha)
                .put("rutaImagen", r.rutaImagen)
                .put("clase", r.clase)
                .put("confianza", r.confianza)
            file.appendText(json.toString() + "\n")
        }
    }

    private fun fromJson(jsonStr: String): RegistroAnalisis {
        val obj = JSONObject(jsonStr)
        return RegistroAnalisis(
            timestamp = obj.getLong("timestamp"),
            fecha = obj.optString("fecha"),
            rutaImagen = obj.getString("rutaImagen"),
            clase = obj.getString("clase"),
            confianza = obj.getDouble("confianza").toFloat()
        )
    }

    fun crearRegistro(rutaImagen: String, clase: String, confianza: Float): RegistroAnalisis {
        val ts = System.currentTimeMillis()
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts))
        return RegistroAnalisis(ts, fecha, rutaImagen, clase, confianza)
    }
}