package com.example.medikit

object ImagenGlobal {
    var rutaImagen: String? = null
    var resultadoIA: Detector.ResultadoIA? = null
    
    fun limpiarDatos() {
        rutaImagen = null
        resultadoIA = null
    }
}