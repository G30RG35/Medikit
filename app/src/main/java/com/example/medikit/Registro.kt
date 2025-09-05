package com.example.medikit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class Registro : AppCompatActivity() {

    private lateinit var nameLayout: TextInputLayout
    private lateinit var lastName1Layout: TextInputLayout
    private lateinit var lastName2Layout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    // LÍNEA CORREGIDA:
    private lateinit var nameEditText: TextInputEditText
    private lateinit var lastName1EditText: TextInputEditText
    // FIN DE LA CORRECCIÓN

    private lateinit var lastName2EditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText

    private lateinit var btnRegistrarse: Button

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

        initializeViews()
        setupFieldFocusListeners() // Para marcar campos como "tocados"
        setupTextWatchers() // Para validación y habilitación del botón

        // Inicialmente el botón de registrarse estará deshabilitado
        // y se habilitará a medida que los campos se llenen correctamente.
        // O puedes dejarlo habilitado y solo validar al hacer clic.
        // Por ahora, lo dejaremos deshabilitado y se habilitará con la validación.
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
        // NO llamar a validateAllFields() aquí para evitar errores iniciales
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

        btnRegistrarse = findViewById(R.id.btnRegistrarse)
    }

    private fun setupFieldFocusListeners() {
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !nameTouched) { // Si pierde el foco y no había sido tocado antes
                nameTouched = true
                validateName(true) // Validar y mostrar error si es necesario
                checkAllFieldsValidForButtonState()
            } else if (hasFocus) {
                nameLayout.error = null // Limpiar error al ganar foco si se desea
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
                validateConfirmPassword(confirmPasswordTouched) // Revalidar confirmación si la contraseña cambia
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
                // Valida el campo actual si ha sido "tocado"
                when {
                    nameEditText.hasFocus() && nameTouched -> validateName(true)
                    lastName1EditText.hasFocus() && lastName1Touched -> validateLastName1(true)
                    lastName2EditText.hasFocus() && lastName2Touched -> validateLastName2(true)
                    emailEditText.hasFocus() && emailTouched -> validateEmail(true)
                    passwordEditText.hasFocus() && passwordTouched -> {
                        validatePassword(true)
                        validateConfirmPassword(confirmPasswordTouched) // Revalidar si la confirmación ya fue tocada
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

    /**
     * Valida todos los campos y MUESTRA errores si es necesario.
     * Se usa principalmente al hacer clic en el botón de registrar.
     */
    private fun validateAllFieldsAndShowErrors(): Boolean {
        // Llamamos a cada validador individual con showErrors = true
        val nameValid = validateName(true)
        val lastName1Valid = validateLastName1(true)
        val lastName2Valid = validateLastName2(true)
        val emailValid = validateEmail(true)
        val passwordValid = validatePassword(true)
        val confirmPasswordValid = validateConfirmPassword(true)

        val allValid = nameValid && lastName1Valid && lastName2Valid &&
                emailValid && passwordValid && confirmPasswordValid

        // El estado del botón ya se maneja en checkAllFieldsValidForButtonState
        // pero podemos asegurarlo aquí también si es necesario, aunque es redundante.
        // btnRegistrarse.isEnabled = allValid
        return allValid
    }

    /**
     * Verifica si todos los campos son válidos (sin mostrar errores)
     * y actualiza el estado del botón de registrarse.
     */
    private fun checkAllFieldsValidForButtonState() {
        val nameValid = validateName(false) // No mostrar error, solo obtener validez
        val lastName1Valid = validateLastName1(false)
        val lastName2Valid = validateLastName2(false)
        val emailValid = validateEmail(false)
        val passwordValid = validatePassword(false)
        val confirmPasswordValid = validateConfirmPassword(false)

        btnRegistrarse.isEnabled = nameValid && lastName1Valid && lastName2Valid &&
                emailValid && passwordValid && confirmPasswordValid
    }


    // --- Métodos de Validación Individual ---
    // Modificados para aceptar un parámetro 'showError'

    private fun validateName(showError: Boolean): Boolean {
        val name = nameEditText.text.toString().trim()
        val isValid = name.isNotEmpty()
        if (showError && nameTouched) { // Solo mostrar error si el campo ha sido "tocado"
            nameLayout.error = if (!isValid) "El nombre es obligatorio" else null
        } else if (!showError) { // Si no se debe mostrar error, pero queremos limpiar uno previo si ahora es válido
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
                !isValidFormat -> "Ingresa un correo electrónico válido (debe contener @ y dominio)"
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
                !isValidPattern -> "La contraseña debe tener al menos 8 caracteres, incluir letras, números y símbolos básicos (@$!%*#?&)"
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
        // Lógica de registro
    }
}
