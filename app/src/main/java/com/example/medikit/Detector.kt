package com.example.medikit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object Detector {
    private const val TAG = "Detector"
    private const val MODEL_PATH = "modelo_lesiones.tflite"
    private const val INPUT_SIZE = 224
    
    private val clases = listOf(
        "Alergias", "Cortaduras", "Esguinces", "Fracturas",
        "Hematomas", "No lesiones", "Quemaduras", "Raspaduras"
    )

    data class ResultadoIA(
        val clase: String, 
        val confianza: Float,
        val porcentaje: Int = (confianza * 100).toInt(),
        val esConfiable: Boolean = confianza > 0.7f
    )

    fun analizarImagen(context: Context, rutaImagen: String): ResultadoIA? {
        return try {
            Log.d(TAG, "Cargando imagen desde: $rutaImagen")
            val bitmap = BitmapFactory.decodeFile(rutaImagen)
            if (bitmap != null) {
                analizarImagen(context, bitmap)
            } else {
                Log.e(TAG, "No se pudo cargar la imagen")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al analizar imagen", e)
            null
        }
    }

    fun analizarImagen(context: Context, bitmap: Bitmap): ResultadoIA? {
        return try {
            Log.d(TAG, "Iniciando análisis de imagen con IA")
            
            // Cargar modelo desde assets
            val modelBuffer = cargarModelo(context)
            val interpreter = Interpreter(modelBuffer)
            
            // Preprocesar imagen
            val input = bitmapAByteBuffer(bitmap)
            val output = Array(1) { FloatArray(clases.size) }
            
            // Ejecutar predicción
            interpreter.run(input, output)
            
            // Obtener resultado
            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0
            val clase = clases[maxIndex]
            val confianza = output[0][maxIndex]
            
            Log.d(TAG, "Análisis completado - Clase: $clase, Confianza: $confianza")
            
            interpreter.close()
            ResultadoIA(clase, confianza)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante análisis IA", e)
            // Devolver resultado por defecto en caso de error
            ResultadoIA("Error en análisis", 0.0f)
        }
    }
    
    private fun cargarModelo(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun bitmapAByteBuffer(bitmap: Bitmap): ByteBuffer {
        Log.d(TAG, "Preprocesando imagen: ${bitmap.width}x${bitmap.height}")
        
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
            buffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
            buffer.putFloat(((pixel and 0xFF) / 255f))
        }

        return buffer
    }
}
