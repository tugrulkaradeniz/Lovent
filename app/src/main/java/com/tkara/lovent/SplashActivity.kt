package com.tkara.lovent

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var clockImageView: ImageView


    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize session manager
        sessionManager = SessionManager.getInstance(this)

        // Initialize views
        logoImageView = findViewById(R.id.iv_logo)
        clockImageView = findViewById(R.id.iv_clock)

        // Start animations
        startLogoAnimation()
        startClockAnimation()

        // Check auto-login after animations
        Handler(Looper.getMainLooper()).postDelayed({
            checkAutoLoginAndNavigate()
        }, StaticValues.SPLASH_DURATION)
    }

    private fun checkAutoLoginAndNavigate() {
        // Session'ı yenile
        sessionManager.refreshSession()

        when {
            sessionManager.canAutoLogin() -> {
                // Auto-login yapılabilir, direkt ana sayfaya git
                StaticValues.debugLog("SplashActivity", "Auto-login yapılıyor...")
                navigateToMain()
            }
            sessionManager.isLoggedIn() -> {
                // Giriş yapılmış ama remember me kapalı, ana sayfaya git
                StaticValues.debugLog("SplashActivity", "Kullanıcı giriş yapmış, ana sayfaya yönlendiriliyor...")
                navigateToMain()
            }
            else -> {
                // Giriş yapılmamış, login sayfasına git
                StaticValues.debugLog("SplashActivity", "Giriş yapılmamış, login sayfasına yönlendiriliyor...")
                navigateToLogin()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLogoAnimation() {
        // Logo fade in animation
        logoImageView.alpha = 0f
        logoImageView.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(500)
            .start()
    }

    private fun startClockAnimation() {
        // Clock tick-tock animation (saat animasyonu)
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000 // 2 saniye bir tam tur
            repeatCount = Animation.INFINITE
            fillAfter = true
        }

        clockImageView.startAnimation(rotateAnimation)
    }
}