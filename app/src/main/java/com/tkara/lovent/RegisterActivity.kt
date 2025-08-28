package com.tkara.lovent

import HttpJsonClient
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnGoogleRegister: MaterialButton
    private lateinit var btnFacebookRegister: MaterialButton
    private lateinit var tvLogin: TextView

    // HTTP Client
    private lateinit var httpClient: HttpJsonClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize HTTP client
        httpClient = HttpJsonClient()
        httpClient.setTimeouts(StaticValues.CONNECT_TIMEOUT, StaticValues.READ_TIMEOUT)

        // Initialize views
        initViews()

        // Set click listeners
        setClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        tilName = findViewById(R.id.til_name)
        tilEmail = findViewById(R.id.til_email)
        tilPassword = findViewById(R.id.til_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        btnGoogleRegister = findViewById(R.id.btn_google_register)
        btnFacebookRegister = findViewById(R.id.btn_facebook_register)
        tvLogin = findViewById(R.id.tv_login)
    }

    private fun setClickListeners() {
        // Normal register
        btnRegister.setOnClickListener {
            performRegister()
        }

        // Google register
        btnGoogleRegister.setOnClickListener {
            showToast("Google ile kayıt yakında...")
        }

        // Facebook register
        btnFacebookRegister.setOnClickListener {
            showToast("Facebook ile kayıt yakında...")
        }

        // Login page
        tvLogin.setOnClickListener {
            finish() // Go back to login activity
        }
    }

    private fun performRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validation
        if (!validateInputs(name, email, password, confirmPassword)) {
            return
        }

        // Show loading
        btnRegister.isEnabled = false
        btnRegister.text = "Kayıt oluşturuluyor..."

        // API call
        lifecycleScope.launch {
            try {
                val registerData = mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )

                val response = httpClient.postJson(StaticValues.AUTH_REGISTER, registerData)
                val responseMap = httpClient.parseJsonResponse(response)

                if (responseMap["success"] == true) {
                    // Registration successful
                    val message = responseMap["message"] as? String ?: "Kayıt başarılı! Giriş yapabilirsiniz."
                    showToast(message)

                    // Go back to login
                    finish()
                } else {
                    // Registration failed
                    val errorMessage = responseMap["error"] as? String ?: "Kayıt başarısız"
                    showToast(errorMessage)
                }

            } catch (e: Exception) {
                showToast("Bağlantı hatası: ${e.message}")
            } finally {
                // Reset button
                btnRegister.isEnabled = true
                btnRegister.text = "Kayıt Ol"
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Name validation
        if (name.isEmpty()) {
            tilName.error = "Ad Soyad gerekli"
            isValid = false
        } else if (name.length < 2) {
            tilName.error = "Ad Soyad en az 2 karakter olmalı"
            isValid = false
        } else {
            tilName.error = null
        }

        // Email validation
        if (email.isEmpty()) {
            tilEmail.error = "E-posta adresi gerekli"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Geçerli bir e-posta adresi girin"
            isValid = false
        } else {
            tilEmail.error = null
        }

        // Password validation
        if (password.isEmpty()) {
            tilPassword.error = "Şifre gerekli"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Şifre en az 6 karakter olmalı"
            isValid = false
        } else if (!password.any { it.isDigit() }) {
            tilPassword.error = "Şifre en az bir sayı içermeli"
            isValid = false
        } else {
            tilPassword.error = null
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Şifre onayı gerekli"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "Şifreler eşleşmiyor"
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}