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

    private var emailTouched = false
    private var passwordTouched = false

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupFieldFocusListeners()
        setupTextWatchers()

        // Recuperar email guardado si existe
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val savedEmail = prefs.getString("email", "")
        if (!savedEmail.isNullOrEmpty()) {
            loginEditTextEmailAddress.setText(savedEmail)
            loginCheckBoxRememberMe.isChecked = true
        }

        btnLogin.isEnabled = false

        btnLogin.setOnClickListener {
            emailTouched = true
            passwordTouched = true

            if (validateAllFieldsAndShowErrors()) {
                performLogin()
            }
        }

        textViewRegisterLink.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        textViewForgotPassword.setOnClickListener {
            val email = loginEditTextEmailAddress.text.toString().trim()
            if (email.isEmpty()) {
                loginEmailLayout.error = "Ingresa tu correo para recuperar la contraseña"
                loginEditTextEmailAddress.requestFocus()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                loginEmailLayout.error = "Correo inválido"
                loginEditTextEmailAddress.requestFocus()
            } else {
                loginEmailLayout.error = null
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Se envió un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show()
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Error al enviar correo de recuperación"
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            }
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

        if (showError && passwordTouched) {
            loginPasswordLayout.error = when {
                !isNotEmpty -> "La contraseña es obligatoria"
                else -> null
            }
        } else if (!showError) {
            if (isNotEmpty && loginPasswordLayout.error != null) loginPasswordLayout.error = null
        }
        return isNotEmpty
    }

    private fun performLogin() {
        val email = loginEditTextEmailAddress.text.toString().trim()
        val password = loginEditTextPassword.text.toString().trim()

        loginProgressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginProgressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()
                    // Guardar email si el checkbox está marcado
                    val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
                    if (loginCheckBoxRememberMe.isChecked) {
                        prefs.edit().putString("email", email).apply()
                    } else {
                        prefs.edit().remove("email").apply()
                    }
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Error de autenticación"
                    Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
                    loginPasswordLayout.error = errorMsg
                }
            }
    }
}

