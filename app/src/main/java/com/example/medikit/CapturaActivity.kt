package com.example.medikit

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class CapturaActivity : AppCompatActivity() {
    private lateinit var ivPreview: ImageView
    private lateinit var btnTomarFoto: Button
    private lateinit var btnGaleria: Button
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captura)

        ivPreview = findViewById(R.id.ivPreview)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnGaleria = findViewById(R.id.btnGaleria)

        // Botón de tomar foto va directo a la cámara
        btnTomarFoto.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        // Botón de galería va directo a seleccionar imagen
        btnGaleria.setOnClickListener {
            abrirGaleria()
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                btnTomarFoto.isEnabled = true
                Log.w("CapturaActivity", "Permiso de cámara denegado")
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            photoFile?.also {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "com.example.medikit.fileprovider",
                    it
                )
                photoUri = uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Log.e("CapturaActivity", "No se encontró app de cámara")
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_GALLERY_PICK)
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${timeStamp}_", ".jpg", storageDir
            ).also { photoFile = it }
        } catch (ex: Exception) {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoFile?.let {
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                ivPreview.setImageBitmap(bitmap)
                
                // Guardar la ruta de la imagen
                ImagenGlobal.rutaImagen = it.absolutePath
                
                // Mostrar pantalla de éxito temporalmente
                val intent = Intent(this, FotoExitoActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else if (requestCode == REQUEST_GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                // Conservar permiso de lectura para el URI
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { /* puede fallar si no es persistible, ignorable */ }

                // Copiar contenido a almacenamiento de la app
                val destino = createImageFileFromGallery()
                if (destino != null) {
                    try {
                        contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destino).use { output ->
                                val buffer = ByteArray(8 * 1024)
                                var bytesRead = input.read(buffer)
                                while (bytesRead != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    bytesRead = input.read(buffer)
                                }
                                output.flush()
                            }
                        }

                        // Mostrar preview y continuar flujo
                        val bitmap = BitmapFactory.decodeFile(destino.absolutePath)
                        ivPreview.setImageBitmap(bitmap)

                        ImagenGlobal.rutaImagen = destino.absolutePath

                        val intent = Intent(this, FotoExitoActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("CapturaActivity", "Error al importar imagen", e)
                    }
                } else {
                    Log.e("CapturaActivity", "No se pudo crear archivo de destino")
                }
            } else {
                Log.w("CapturaActivity", "No se seleccionó imagen")
            }
        } else {
            Log.i("CapturaActivity", "Operación cancelada o sin resultado")
            // Si el usuario cancela, habilitar el botón para intentar de nuevo
            btnTomarFoto.isEnabled = true
        }
    }

    private fun createImageFileFromGallery(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "GALLERY_${'$'}timeStamp_", ".jpg", storageDir
            )
        } catch (ex: Exception) {
            null
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
        private const val REQUEST_GALLERY_PICK = 3
    }
}
