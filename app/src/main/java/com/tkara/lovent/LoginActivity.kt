package com.tkara.lovent

import HttpJsonClient
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleLogin: MaterialButton
    private lateinit var btnFacebookLogin: MaterialButton
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var cbRememberMe: CheckBox

    // HTTP Client ve Session Manager
    private lateinit var httpClient: HttpJsonClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize HTTP client ve Session Manager
        httpClient = HttpJsonClient()
        httpClient.setTimeouts(StaticValues.CONNECT_TIMEOUT, StaticValues.READ_TIMEOUT)
        sessionManager = SessionManager.getInstance(this)

        // Initialize views
        initViews()

        // Set click listeners
        setClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tilEmail = findViewById(R.id.til_email)
        tilPassword = findViewById(R.id.til_password)
        btnLogin = findViewById(R.id.btn_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
        btnFacebookLogin = findViewById(R.id.btn_facebook_login)
        tvRegister = findViewById(R.id.tv_register)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        cbRememberMe = findViewById(R.id.cb_remember_me)
    }

    private fun setClickListeners() {
        // Normal login
        btnLogin.setOnClickListener {
            performLogin()
        }

        // Google login
        btnGoogleLogin.setOnClickListener {
            // Google Sign-In integration burada olacak
            showToast("Google ile giriş yakında...")
        }

        // Facebook login
        btnFacebookLogin.setOnClickListener {
            // Facebook login integration burada olacak
            showToast("Facebook ile giriş yakında...")
        }

        // Register page
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Forgot password
        tvForgotPassword.setOnClickListener {
            showToast("Şifre sıfırlama yakında...")
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validation
        if (!validateInputs(email, password)) {
            return
        }

        // Show loading
        btnLogin.isEnabled = false
        btnLogin.text = "Giriş yapılıyor..."

        // API call
        lifecycleScope.launch {
            try {
                val loginData = mapOf(
                    "email" to email,
                    "password" to password
                )

                val response = httpClient.postJson(StaticValues.AUTH_LOGIN, loginData)
                StaticValues.debugLog("LoginActivity", "Raw response: $response")

                val responseMap = httpClient.parseJsonResponse(response)
                StaticValues.debugLog("LoginActivity", "Parsed response: $responseMap")

                if (responseMap["success"] == true) {
                    // Login successful
                    showToast(StaticValues.SuccessMessages.LOGIN_SUCCESS)

                    // User data'yı al
                    val userData = responseMap["user"] as? Map<String, Any?> ?: mapOf()

                    StaticValues.debugLog("LoginActivity", "User data: $userData")

                    // Session'ı kaydet (SessionManager ile)
                    val rememberMe = cbRememberMe.isChecked
                    sessionManager.saveUserSession(
                        userId = userData["id"].toString(),
                        name = userData["name"].toString(),
                        email = userData["email"].toString(),
                        photoUrl = userData["profile_photo"]?.toString(),
                        rememberMe = rememberMe,
                        autoLoginToken = userData["token"]?.toString() // Backend'den gelecek
                    )

                    // Navigate to main activity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login failed
                    val errorMessage = responseMap["error"] as? String ?: StaticValues.ErrorMessages.INVALID_CREDENTIALS
                    showToast(errorMessage)
                }

            } catch (e: Exception) {
                showToast(StaticValues.ErrorMessages.NETWORK_ERROR + ": ${e.message}")
            } finally {
                // Reset button
                btnLogin.isEnabled = true
                btnLogin.text = "Giriş Yap"
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

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
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}