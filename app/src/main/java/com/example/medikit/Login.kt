package com.example.medikit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast // Para mostrar mensajes de ejemplo
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
// Asumiendo que tienes Firebase Auth, si no, puedes quitar estas líneas y la lógica de Firebase
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var loginEmailLayout: TextInputLayout
    private lateinit var loginPasswordLayout: TextInputLayout

    private lateinit var loginEditTextEmailAddress: TextInputEditText
    private lateinit var loginEditTextPassword: TextInputEditText
    private lateinit var loginCheckBoxRememberMe: CheckBox

    private lateinit var btnLogin: Button
    private lateinit var loginProgressBar: ProgressBar
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewRegisterLink: TextView

    // Flags para interacción del usuario (similar a Registro.kt)
    private var emailTouched = false
    private var passwordTouched = false

    // Firebase Auth (opcional, si lo usas)
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Asegúrate que este es tu layout XML de login

        // Inicializar Firebase Auth (opcional)
        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupFieldFocusListeners()
        setupTextWatchers()

        // Botón de login inicialmente deshabilitado
        btnLogin.isEnabled = false

        btnLogin.setOnClickListener {
            emailTouched = true
            passwordTouched = true

            if (validateAllFieldsAndShowErrors()) {
                performLogin()
            }
        }

        textViewRegisterLink.setOnClickListener {
            // Navegar a la actividad de Registro
            val intent = Intent(this, Registro::class.java) // Asumiendo que tu Activity de registro se llama Registro
            startActivity(intent)
        }

        textViewForgotPassword.setOnClickListener {
            // Lógica para "Olvidé mi contraseña"
            // Por ejemplo, mostrar un diálogo o navegar a otra pantalla
            Toast.makeText(this, "Funcionalidad 'Olvidé contraseña' no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        loginEmailLayout = findViewById(R.id.loginEmailLayout)
        loginPasswordLayout = findViewById(R.id.loginPasswordLayout)

        loginEditTextEmailAddress = findViewById(R.id.loginEditTextEmailAddress)
        loginEditTextPassword = findViewById(R.id.loginEditTextPassword)
        loginCheckBoxRememberMe = findViewById(R.id.loginCheckBoxRememberMe)

        btnLogin = findViewById(R.id.btnLogin)
        loginProgressBar = findViewById(R.id.loginProgressBar)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink)
    }

    private fun setupFieldFocusListeners() {
        loginEditTextEmailAddress.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !emailTouched) {
                emailTouched = true
                validateEmail(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                loginEmailLayout.error = null
            }
        }

        loginEditTextPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !passwordTouched) {
                passwordTouched = true
                validatePassword(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                loginPasswordLayout.error = null
            }
        }
    }

    private fun setupTextWatchers() {
        val commonTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                when {
                    loginEditTextEmailAddress.hasFocus() && emailTouched -> validateEmail(true)
                    loginEditTextPassword.hasFocus() && passwordTouched -> validatePassword(true)
                }
                checkAllFieldsValidForButtonState()
            }
        }

        loginEditTextEmailAddress.addTextChangedListener(commonTextWatcher)
        loginEditTextPassword.addTextChangedListener(commonTextWatcher)
    }

    private fun validateAllFieldsAndShowErrors(): Boolean {
        val emailValid = validateEmail(true)
        val passwordValid = validatePassword(true)

        return emailValid && passwordValid
    }

    private fun checkAllFieldsValidForButtonState() {
        val emailValid = validateEmail(false) // No mostrar error, solo obtener validez
        val passwordValid = validatePassword(false)

        btnLogin.isEnabled = emailValid && passwordValid
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val email = loginEditTextEmailAddress.text.toString().trim()
        val isNotEmpty = email.isNotEmpty()
        val isValidFormat = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        if (showError && emailTouched) {
            loginEmailLayout.error = when {
                !isNotEmpty -> "El correo electrónico es obligatorio"
                !isValidFormat -> "Ingresa un correo electrónico válido"
                else -> null
            }
        } else if (!showError) {
            if (isNotEmpty && isValidFormat && loginEmailLayout.error != null) loginEmailLayout.error = null
        }
        return isNotEmpty && isValidFormat
    }

    private fun validatePassword(showError: Boolean): Boolean {
        val password = loginEditTextPassword.text.toString().trim()
        val isNotEmpty = password.isNotEmpty()
        // Para el login, usualmente no se valida el formato de la contraseña aquí,
        // solo que no esté vacío. El servidor se encarga de la validez de la contraseña.
        // Si quieres una validación de longitud mínima en el cliente:
        // val isLongEnough = password.length >= 8

        if (showError && passwordTouched) {
            loginPasswordLayout.error = when {
                !isNotEmpty -> "La contraseña es obligatoria"
                // !isLongEnough -> "La contraseña debe tener al menos 8 caracteres" // Ejemplo
                else -> null
            }
        } else if (!showError) {
            // if (isNotEmpty && isLongEnough && loginPasswordLayout.error != null) loginPasswordLayout.error = null // Ejemplo
            if (isNotEmpty && loginPasswordLayout.error != null) loginPasswordLayout.error = null
        }
        // return isNotEmpty && isLongEnough // Ejemplo
        return isNotEmpty
    }

    private fun performLogin() {
        val email = loginEditTextEmailAddress.text.toString().trim()
        val password = loginEditTextPassword.text.toString().trim()

        loginProgressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false // Deshabilitar botón durante el proceso

        // --- Lógica de Inicio de Sesión (Ejemplo con Firebase Auth) ---
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginProgressBar.visibility = View.GONE
                btnLogin.isEnabled = true // Rehabilitar botón

                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Toast.makeText(baseContext, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Cierra LoginActivity para que el usuario no pueda volver con el botón "Atrás"
                } else {
                    // Si el inicio de sesión falla, mostrar un mensaje al usuario.
                    val errorMsg = task.exception?.localizedMessage ?: "Error de autenticación"
                    Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
                    loginPasswordLayout.error = errorMsg
                }
            }
        // --- Fin de la Lógica de Inicio de Sesión (Ejemplo) ---

        // Si no usas Firebase, aquí iría tu propia lógica de autenticación
        // (ej. llamar a tu API, verificar en una base de datos local, etc.)
        // Si es solo un prototipo sin backend:
        /*
        Handler(Looper.getMainLooper()).postDelayed({
            loginProgressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            if (email == "test@example.com" && password == "password123") {
                Toast.makeText(this, "Login Exitoso (Simulado)", Toast.LENGTH_SHORT).show()
                // Navegar a la siguiente pantalla
                // val intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish()
            } else {
                Toast.makeText(this, "Credenciales incorrectas (Simulado)", Toast.LENGTH_SHORT).show()
                loginPasswordLayout.error = "Correo o contraseña incorrectos"
            }
        }, 2000) // Simular retraso de red
        */
    }
}

