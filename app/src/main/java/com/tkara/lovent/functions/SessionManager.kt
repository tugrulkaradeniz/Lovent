package com.tkara.lovent

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Güvenli oturum yönetimi sınıfı
 * - Şifrelenmiş veri saklama
 * - Auto-login sistemi
 * - Session expire kontrolü
 * - Remember me özelliği
 */
class SessionManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Session keys
        private const val SESSION_FILE = "lovent_secure_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_AUTO_LOGIN_TOKEN = "auto_login_token"
        private const val KEY_LAST_ACTIVITY = "last_activity"

        // Session expire süreleri
        private val SESSION_EXPIRE_TIME = TimeUnit.DAYS.toMillis(30) // 30 gün
        private val ACTIVITY_EXPIRE_TIME = TimeUnit.DAYS.toMillis(7)  // 7 gün inaktiflik
    }

    // Şifrelenmiş SharedPreferences
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SESSION_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            StaticValues.debugLog("SessionManager", "Şifreleme hatası, normal SharedPreferences kullanılıyor: ${e.message}")
            // Fallback to normal SharedPreferences
            context.getSharedPreferences(SESSION_FILE, Context.MODE_PRIVATE)
        }
    }

    /**
     * Kullanıcı girişini kaydet
     */
    fun saveUserSession(
        userId: String,
        name: String,
        email: String,
        photoUrl: String? = null,
        rememberMe: Boolean = false,
        autoLoginToken: String? = null
    ) {
        val editor = sharedPreferences.edit()
        val currentTime = System.currentTimeMillis()

        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PHOTO, photoUrl ?: "")
            putLong(KEY_LOGIN_TIME, currentTime)
            putLong(KEY_LAST_ACTIVITY, currentTime)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putString(KEY_AUTO_LOGIN_TOKEN, autoLoginToken ?: "")
        }.apply()

        StaticValues.debugLog("SessionManager", "Kullanıcı oturumu kaydedildi: $email")
    }

    /**
     * Kullanıcının giriş yapıp yapmadığını kontrol et
     */
    fun isLoggedIn(): Boolean {
        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false
        }

        // Session expire kontrolü
        if (isSessionExpired()) {
            clearSession()
            return false
        }

        // Activity expire kontrolü
        if (isInactivityExpired() && !isRememberMeEnabled()) {
            clearSession()
            return false
        }

        // Son aktiviteyi güncelle
        updateLastActivity()

        return true
    }

    /**
     * Auto-login yapılabilir mi kontrol et
     */
    fun canAutoLogin(): Boolean {
        return isLoggedIn() &&
                isRememberMeEnabled() &&
                getAutoLoginToken().isNotEmpty()
    }

    /**
     * Session expire olmuş mu kontrol et
     */
    private fun isSessionExpired(): Boolean {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
        return (System.currentTimeMillis() - loginTime) > SESSION_EXPIRE_TIME
    }

    /**
     * Inaktivite süresi dolmuş mu kontrol et
     */
    private fun isInactivityExpired(): Boolean {
        val lastActivity = sharedPreferences.getLong(KEY_LAST_ACTIVITY, 0)
        return (System.currentTimeMillis() - lastActivity) > ACTIVITY_EXPIRE_TIME
    }

    /**
     * Remember me aktif mi
     */
    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    /**
     * Son aktivite zamanını güncelle
     */
    fun updateLastActivity() {
        sharedPreferences.edit()
            .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
            .apply()
    }

    /**
     * Kullanıcı bilgilerini getir
     */
    fun getUserInfo(): UserInfo? {
        if (!isLoggedIn()) return null

        return UserInfo(
            id = sharedPreferences.getString(KEY_USER_ID, "") ?: "",
            name = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
            email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: "",
            photoUrl = sharedPreferences.getString(KEY_USER_PHOTO, "")
        )
    }

    /**
     * Auto login token'ı getir
     */
    fun getAutoLoginToken(): String {
        return sharedPreferences.getString(KEY_AUTO_LOGIN_TOKEN, "") ?: ""
    }

    /**
     * Kullanıcı ID'sini getir
     */
    fun getUserId(): String {
        val userId = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
        StaticValues.debugLog("SessionManager", "getUserId: '$userId'")
        return userId
    }

    /**
     * Kullanıcı adını getir
     */
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "Kullanıcı") ?: "Kullanıcı"
    }

    /**
     * Kullanıcı email'ini getir
     */
    fun getUserEmail(): String {
        return sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    /**
     * Profil fotoğrafı URL'ini getir
     */
    fun getProfilePhotoUrl(): String? {
        val url = sharedPreferences.getString(KEY_USER_PHOTO, "")
        return if (url?.isNotEmpty() == true) url else null
    }

    /**
     * Session bilgilerini temizle
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        StaticValues.debugLog("SessionManager", "Kullanıcı oturumu temizlendi")
    }

    /**
     * Sadece remember me'yi kapat (logout'ta kullan)
     */
    fun disableRememberMe() {
        sharedPreferences.edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .putString(KEY_AUTO_LOGIN_TOKEN, "")
            .apply()
    }

    /**
     * Session süresini kontrol et ve bilgi döndür
     */
    fun getSessionInfo(): SessionInfo {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
        val lastActivity = sharedPreferences.getLong(KEY_LAST_ACTIVITY, 0)
        val remainingSessionTime = SESSION_EXPIRE_TIME - (System.currentTimeMillis() - loginTime)
        val remainingActivityTime = ACTIVITY_EXPIRE_TIME - (System.currentTimeMillis() - lastActivity)

        return SessionInfo(
            loginTime = loginTime,
            lastActivity = lastActivity,
            remainingSessionTime = maxOf(0, remainingSessionTime),
            remainingActivityTime = maxOf(0, remainingActivityTime),
            isRememberMeEnabled = isRememberMeEnabled()
        )
    }

    /**
     * Session'ı yenile (app açıldığında çağır)
     */
    fun refreshSession() {
        if (isLoggedIn()) {
            updateLastActivity()
            StaticValues.debugLog("SessionManager", "Session yenilendi")
        }
    }

    // Data classes
    data class UserInfo(
        val id: String,
        val name: String,
        val email: String,
        val photoUrl: String?
    )

    data class SessionInfo(
        val loginTime: Long,
        val lastActivity: Long,
        val remainingSessionTime: Long,
        val remainingActivityTime: Long,
        val isRememberMeEnabled: Boolean
    )
}