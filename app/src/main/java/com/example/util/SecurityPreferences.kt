package com.example.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages security preferences for CannaMed:
 * - Biometric App Lock requirement
 * - Anti-Screenshot Window.FLAG_SECURE toggle
 */
class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cannamed_security_prefs", Context.MODE_PRIVATE)

    var isBiometricLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_LOCK, true) // Enabled by default for patient medical privacy
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, value).apply()

    var isScreenshotProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREENSHOT_PROTECTION, true) // Enabled by default to protect prescription data
        set(value) = prefs.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, value).apply()

    var isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false) // False by default so user can test AdMob banners/interstitials
        set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

    var isAgeVerified18Plus: Boolean
        get() = prefs.getBoolean(KEY_AGE_VERIFIED, false) // Google Play Mature 17+ / Medical Adult verification
        set(value) = prefs.edit().putBoolean(KEY_AGE_VERIFIED, value).apply()

    var hasAcceptedMedicalTerms: Boolean
        get() = prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, value).apply()

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_BIOMETRIC_LOCK = "key_biometric_lock"
        private const val KEY_SCREENSHOT_PROTECTION = "key_screenshot_protection"
        private const val KEY_IS_PREMIUM = "key_is_premium"
        private const val KEY_AGE_VERIFIED = "key_age_verified_18_plus"
        private const val KEY_TERMS_ACCEPTED = "key_terms_accepted"
    }
}
