package com.example.medikit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern
class Registro : AppCompatActivity() {


    private lateinit var nameLayout: TextInputLayout
    private lateinit var lastName1Layout: TextInputLayout
    private lateinit var lastName2Layout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    private lateinit var nameEditText: TextInputEditText
    private lateinit var lastName1EditText: TextInputEditText
    private lateinit var lastName2EditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var checkBox: CheckBox

    private lateinit var btnRegistrarse: Button
    private lateinit var progressBar: ProgressBar

    // Firebase (versión sin KTX)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Flags para saber si el usuario ha interactuado con un campo
    private var nameTouched = false
    private var lastName1Touched = false
    private var lastName2Touched = false
    private var emailTouched = false
    private var passwordTouched = false
    private var confirmPasswordTouched = false

    companion object {
        private val PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar Firebase (versión sin KTX)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupFieldFocusListeners()
        setupTextWatchers()

        btnRegistrarse.isEnabled = false

        btnRegistrarse.setOnClickListener {
            // Marcar todos los campos como "tocados" para que muestren error si es necesario
            nameTouched = true
            lastName1Touched = true
            lastName2Touched = true
            emailTouched = true
            passwordTouched = true
            confirmPasswordTouched = true

            if (validateAllFieldsAndShowErrors()) {
                performRegistration()
            }
        }
    }
    private fun initializeViews() {
        nameLayout = findViewById(R.id.nameLayout)
        lastName1Layout = findViewById(R.id.lastName1Layout)
        lastName2Layout = findViewById(R.id.lastName2Layout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)

        nameEditText = findViewById(R.id.editTextText2)
        lastName1EditText = findViewById(R.id.editTextText3)
        lastName2EditText = findViewById(R.id.editTextText4)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextTextPassword2)
        checkBox = findViewById(R.id.checkBox)

        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupFieldFocusListeners() {
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !nameTouched) {
                nameTouched = true
                validateName(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                nameLayout.error = null
            }
        }
        lastName1EditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !lastName1Touched) {
                lastName1Touched = true
                validateLastName1(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                lastName1Layout.error = null
            }
        }
        lastName2EditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !lastName2Touched) {
                lastName2Touched = true
                validateLastName2(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                lastName2Layout.error = null
            }
        }
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !emailTouched) {
                emailTouched = true
                validateEmail(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                emailLayout.error = null
            }
        }
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !passwordTouched) {
                passwordTouched = true
                validatePassword(true)
                validateConfirmPassword(confirmPasswordTouched)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                passwordLayout.error = null
            }
        }
        confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !confirmPasswordTouched) {
                confirmPasswordTouched = true
                validateConfirmPassword(true)
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                confirmPasswordLayout.error = null
            }
        }
    }

    private fun setupTextWatchers() {
        val commonTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                when {
                    nameEditText.hasFocus() && nameTouched -> validateName(true)
                    lastName1EditText.hasFocus() && lastName1Touched -> validateLastName1(true)
                    lastName2EditText.hasFocus() && lastName2Touched -> validateLastName2(true)
                    emailEditText.hasFocus() && emailTouched -> validateEmail(true)
                    passwordEditText.hasFocus() && passwordTouched -> {
                        validatePassword(true)
                        validateConfirmPassword(confirmPasswordTouched)
                    }
                    confirmPasswordEditText.hasFocus() && confirmPasswordTouched -> validateConfirmPassword(true)
                }
                checkAllFieldsValidForButtonState()
            }
        }

        nameEditText.addTextChangedListener(commonTextWatcher)
        lastName1EditText.addTextChangedListener(commonTextWatcher)
        lastName2EditText.addTextChangedListener(commonTextWatcher)
        emailEditText.addTextChangedListener(commonTextWatcher)
        passwordEditText.addTextChangedListener(commonTextWatcher)
        confirmPasswordEditText.addTextChangedListener(commonTextWatcher)
    }

    private fun validateAllFieldsAndShowErrors(): Boolean {
        val nameValid = validateName(true)
        val lastName1Valid = validateLastName1(true)
        val lastName2Valid = validateLastName2(true)
        val emailValid = validateEmail(true)
        val passwordValid = validatePassword(true)
        val confirmPasswordValid = validateConfirmPassword(true)

        return nameValid && lastName1Valid && lastName2Valid &&
                emailValid && passwordValid && confirmPasswordValid
    }

    private fun checkAllFieldsValidForButtonState() {
        val nameValid = validateName(false)
        val lastName1Valid = validateLastName1(false)
        val lastName2Valid = validateLastName2(false)
        val emailValid = validateEmail(false)
        val passwordValid = validatePassword(false)
        val confirmPasswordValid = validateConfirmPassword(false)

        btnRegistrarse.isEnabled = nameValid && lastName1Valid && lastName2Valid &&
                emailValid && passwordValid && confirmPasswordValid
    }

    private fun validateName(showError: Boolean): Boolean {
        val name = nameEditText.text.toString().trim()
        val isValid = name.isNotEmpty()
        if (showError && nameTouched) {
            nameLayout.error = if (!isValid) "El nombre es obligatorio" else null
        } else if (!showError) {
            if (isValid && nameLayout.error != null) nameLayout.error = null
        }
        return isValid
    }

    private fun validateLastName1(showError: Boolean): Boolean {
        val lastName = lastName1EditText.text.toString().trim()
        val isValid = lastName.isNotEmpty()
        if (showError && lastName1Touched) {
            lastName1Layout.error = if (!isValid) "El apellido paterno es obligatorio" else null
        } else if (!showError) {
            if (isValid && lastName1Layout.error != null) lastName1Layout.error = null
        }
        return isValid
    }

    private fun validateLastName2(showError: Boolean): Boolean {
        val lastName = lastName2EditText.text.toString().trim()
        val isValid = lastName.isNotEmpty()
        if (showError && lastName2Touched) {
            lastName2Layout.error = if (!isValid) "El apellido materno es obligatorio" else null
        } else if (!showError) {
            if (isValid && lastName2Layout.error != null) lastName2Layout.error = null
        }
        return isValid
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val email = emailEditText.text.toString().trim()
        val isNotEmpty = email.isNotEmpty()
        val isValidFormat = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        if (showError && emailTouched) {
            emailLayout.error = when {
                !isNotEmpty -> "El correo electrónico es obligatorio"
                !isValidFormat -> "Ingresa un correo electrónico válido"
                else -> null
            }
        } else if (!showError) {
            if (isNotEmpty && isValidFormat && emailLayout.error != null) emailLayout.error = null
        }
        return isNotEmpty && isValidFormat
    }

    private fun validatePassword(showError: Boolean): Boolean {
        val password = passwordEditText.text.toString().trim()
        val isNotEmpty = password.isNotEmpty()
        val isValidPattern = PASSWORD_PATTERN.matcher(password).matches()

        if (showError && passwordTouched) {
            passwordLayout.error = when {
                !isNotEmpty -> "La contraseña es obligatoria"
                !isValidPattern -> "La contraseña debe tener al menos 8 caracteres, incluir letras, números y símbolos"
                else -> null
            }
        } else if (!showError) {
            if (isNotEmpty && isValidPattern && passwordLayout.error != null) passwordLayout.error = null
        }
        return isNotEmpty && isValidPattern
    }

    private fun validateConfirmPassword(showError: Boolean): Boolean {
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val isNotEmpty = confirmPassword.isNotEmpty()
        val doPasswordsMatch = confirmPassword == password

        if (showError && confirmPasswordTouched) {
            confirmPasswordLayout.error = when {
                !isNotEmpty -> "Confirma tu contraseña"
                !doPasswordsMatch -> "Las contraseñas no coinciden"
                else -> null
            }
        } else if (!showError) {
            if (isNotEmpty && doPasswordsMatch && confirmPasswordLayout.error != null) confirmPasswordLayout.error = null
        }
        return isNotEmpty && doPasswordsMatch
    }

    private fun performRegistration() {
        val nombre = nameEditText.text.toString().trim()
        val apellidoPaterno = lastName1EditText.text.toString().trim()
        val apellidoMaterno = lastName2EditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val rememberMe = checkBox.isChecked

        showLoading(true)

        // Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Actualizar perfil con nombre completo
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("$nombre $apellidoPaterno $apellidoMaterno")
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    // Guardar usuario en Firestore
                                    saveUserToFirestore(it.uid, nombre, apellidoPaterno, apellidoMaterno, email, rememberMe)
                                } else {
                                    showLoading(false)
                                    Toast.makeText(
                                        this,
                                        "Error al actualizar perfil: ${profileTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Error en registro: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    private fun saveUserToFirestore(
        userId: String,
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        rememberMe: Boolean
    ) {
        val user = hashMapOf(
            "userId" to userId,
            "nombre" to nombre,
            "apellidoPaterno" to apellidoPaterno,
            "apellidoMaterno" to apellidoMaterno,
            "email" to email,
            "rememberMe" to rememberMe,
            "fechaRegistro" to System.currentTimeMillis(),
            "ultimoAcceso" to System.currentTimeMillis(),
            "activo" to true
        )

        db.collection("usuarios")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                // Aquí puedes redirigir a la siguiente actividad
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al guardar datos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(loading: Boolean) {
        if (loading) {
            progressBar.visibility = View.VISIBLE
            btnRegistrarse.isEnabled = false
            btnRegistrarse.text = "Registrando..."
        } else {
            progressBar.visibility = View.GONE
            btnRegistrarse.isEnabled = true
            btnRegistrarse.text = "Registrarse"
        }
    }
}