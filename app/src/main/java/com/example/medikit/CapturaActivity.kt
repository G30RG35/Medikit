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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CapturaActivity : AppCompatActivity() {
    private lateinit var ivPreview: ImageView
    private lateinit var btnTomarFoto: Button
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captura)

        ivPreview = findViewById(R.id.ivPreview)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)

        btnTomarFoto.isEnabled = false
        checkCameraPermissionAndOpenCamera()

        btnTomarFoto.setOnClickListener {
            // No hacer nada, la cámara ya se abre automáticamente
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.widget.Toast.makeText(this, "Permiso de cámara concedido", android.widget.Toast.LENGTH_SHORT).show()
            dispatchTakePictureIntent()
        } else {
            android.widget.Toast.makeText(this, "Solicitando permiso de cámara", android.widget.Toast.LENGTH_SHORT).show()
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
                android.widget.Toast.makeText(this, "Permiso de cámara denegado", android.widget.Toast.LENGTH_LONG).show()
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
                android.widget.Toast.makeText(this, "Lanzando intent de cámara", android.widget.Toast.LENGTH_SHORT).show()
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            android.widget.Toast.makeText(this, "No se encontró app de cámara", android.widget.Toast.LENGTH_LONG).show()
        }
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
            android.widget.Toast.makeText(this, "Foto tomada correctamente", android.widget.Toast.LENGTH_SHORT).show()
            photoFile?.let {
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                ivPreview.setImageBitmap(bitmap)
                // Ir a pantalla de éxito
                val intent = Intent(this, FotoExitoActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            android.widget.Toast.makeText(this, "No se tomó la foto o se canceló", android.widget.Toast.LENGTH_SHORT).show()
            // Si el usuario cancela, habilitar el botón para intentar de nuevo
            btnTomarFoto.isEnabled = true
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CAMERA_PERMISSION = 2
    }
}
