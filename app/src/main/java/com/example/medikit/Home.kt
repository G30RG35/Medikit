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
        // Si quieres aplicar insets, usa el layout raíz correctamente:
        // val root = findViewById<LinearLayout>(R.id.main) // pero tu layout no tiene id 'main'
        // Si necesitas insets, ponle android:id="@+id/main" al LinearLayout raíz en activity_home.xml
        // O simplemente elimina la línea que causa el error:
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { ... }
    }
}