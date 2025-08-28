package com.tkara.lovent

object StaticValues {

    // ==================== SERVER CONFIG ====================

    // Ana server URL'leri - sadece burayı değiştir!
    private const val DEVELOPMENT_BASE_URL = "http://192.168.1.4/lovent" // Kendi IP'in
    private const val LOCAL_EMULATOR_URL = "http://10.0.2.2"        // Android Emülatör için
    private const val PRODUCTION_BASE_URL = "https://api.lovent.com" // Canlı server

    // Aktif environment seç
    private  val CURRENT_ENVIRONMENT = Environment.DEVELOPMENT

    // Environment enum
    private enum class Environment {
        DEVELOPMENT,
        LOCAL_EMULATOR,
        PRODUCTION
    }

    // Aktif base URL
    val BASE_URL: String = when (CURRENT_ENVIRONMENT) {
        Environment.DEVELOPMENT -> DEVELOPMENT_BASE_URL
        Environment.LOCAL_EMULATOR -> LOCAL_EMULATOR_URL
        Environment.PRODUCTION -> PRODUCTION_BASE_URL
    }

    // ==================== API ENDPOINTS ====================

    // Auth endpoints
    const val AUTH_REGISTER = "/auth.php/register"
    const val AUTH_LOGIN = "/auth.php/login"
    const val AUTH_LOGOUT = "/auth.php/logout"
    const val AUTH_FORGOT_PASSWORD = "/auth.php/forgot-password"

    // User endpoints
    const val USER_PROFILE = "/api.php/user/profile"
    const val USER_UPDATE = "/api.php/user/update"
    const val USER_DELETE = "/api.php/user/delete"
    const val USER_UPLOAD_PHOTO = "/api.php/user/upload-photo"

    // Event endpoints
    const val EVENTS_API_BASE = "/events_api.php"
    const val EVENTS_FEED = "$EVENTS_API_BASE/events/feed"
    const val EVENTS_CREATE = "$EVENTS_API_BASE/events/create"
    const val EVENTS_UPDATE = "$EVENTS_API_BASE/events/update"
    const val EVENTS_DELETE = "$EVENTS_API_BASE/events/delete"
    const val EVENTS_JOIN = "$EVENTS_API_BASE/events/join"
    const val EVENTS_LEAVE = "$EVENTS_API_BASE/events/leave"
    const val EVENTS_MY_EVENTS = "$EVENTS_API_BASE/events/my-events"
    const val EVENTS_DETAIL = "$EVENTS_API_BASE/events/detail"

    // Discovery endpoints
    const val DISCOVER_USERS = "/api.php/discover/users"
    const val DISCOVER_EVENTS = "/api.php/discover/events"
    const val FOLLOW_USER = "/api.php/follow"
    const val UNFOLLOW_USER = "/api.php/unfollow"

    // Social endpoints
    const val GET_MESSAGES = "/api.php/messages"
    const val SEND_MESSAGE = "/api.php/send-message"

    // ==================== APP CONFIG ====================

    // Uygulama ayarları
    const val APP_VERSION = "1.0.0"
    const val APP_NAME = "Lovent"
    const val DEVELOPER_NAME = "Tugrul Kara"

    // Session keys
    const val PREF_USER_SESSION = "user_session"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_NAME = "name"
    const val PREF_USER_EMAIL = "email"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_REMEMBER_ME = "remember_me"

    // ==================== NETWORK CONFIG ====================

    // Timeout değerleri (milisaniye)
    const val CONNECT_TIMEOUT = 10000 // 10 saniye
    const val READ_TIMEOUT = 15000    // 15 saniye

    // Request limits
    const val MAX_RETRY_COUNT = 3
    const val REQUEST_DELAY_MS = 1000L // 1 saniye

    // ==================== UI CONFIG ====================

    // Animation süreleri
    const val SPLASH_DURATION = 3000L // 3 saniye
    const val FADE_ANIMATION_DURATION = 500L
    const val SLIDE_ANIMATION_DURATION = 300L

    // Image sizes
    const val PROFILE_IMAGE_SIZE = 500 // pixels
    const val THUMBNAIL_SIZE = 100     // pixels

    // ==================== VALIDATION CONFIG ====================

    // Validation rules
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50
    const val MAX_BIO_LENGTH = 500
    const val MIN_AGE = 18
    const val MAX_AGE = 100

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Tam API URL'i oluşturur
     */
    fun getFullUrl(endpoint: String): String {
        return BASE_URL + endpoint
    }

    /**
     * Debug için environment bilgisi
     */
    fun getCurrentEnvironmentInfo(): String {
        return "Environment: $CURRENT_ENVIRONMENT, Base URL: $BASE_URL"
    }

    /**
     * Development modda mı?
     */
    fun isDevelopmentMode(): Boolean {
        return CURRENT_ENVIRONMENT == Environment.DEVELOPMENT ||
                CURRENT_ENVIRONMENT == Environment.LOCAL_EMULATOR
    }

    /**
     * Production modda mı?
     */
    fun isProductionMode(): Boolean {
        return CURRENT_ENVIRONMENT == Environment.PRODUCTION
    }

    // ==================== ERROR MESSAGES ====================

    object ErrorMessages {
        const val NETWORK_ERROR = "İnternet bağlantısını kontrol edin"
        const val SERVER_ERROR = "Sunucu hatası, lütfen tekrar deneyin"
        const val INVALID_CREDENTIALS = "E-posta veya şifre hatalı"
        const val EMAIL_ALREADY_EXISTS = "Bu e-posta adresi zaten kullanılıyor"
        const val WEAK_PASSWORD = "Şifre en az $MIN_PASSWORD_LENGTH karakter olmalı"
        const val INVALID_EMAIL = "Geçerli bir e-posta adresi girin"
        const val FIELD_REQUIRED = "Bu alan gerekli"
        const val UNKNOWN_ERROR = "Beklenmeyen bir hata oluştu"
    }

    // ==================== SUCCESS MESSAGES ====================

    object SuccessMessages {
        const val REGISTRATION_SUCCESS = "Kayıt başarılı! Giriş yapabilirsiniz"
        const val LOGIN_SUCCESS = "Giriş başarılı!"
        const val LOGOUT_SUCCESS = "Çıkış yapıldı"
        const val PROFILE_UPDATED = "Profil güncellendi"
        const val PASSWORD_RESET_SENT = "Şifre sıfırlama bağlantısı gönderildi"
    }

    // ==================== DEBUG CONFIG ====================

    // Debug flags
    const val ENABLE_LOGGING = true
    const val ENABLE_CRASH_REPORTING = false // Production'da true yap
    const val SHOW_DEBUG_MESSAGES = true     // Production'da false yap

    /**
     * Debug log yazdır
     */
    fun debugLog(tag: String, message: String) {
        if (ENABLE_LOGGING && isDevelopmentMode()) {
            println("[$tag] $message")
        }
    }
}