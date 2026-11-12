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

    var isAgeVerified21Plus: Boolean
        get() = prefs.getBoolean(KEY_AGE_VERIFIED_21, false) // 21+ verification for medicinal cannabis
        set(value) = prefs.edit().putBoolean(KEY_AGE_VERIFIED_21, value).apply()

    // Backward compatibility alias for 18+
    var isAgeVerified18Plus: Boolean
        get() = isAgeVerified21Plus
        set(value) { isAgeVerified21Plus = value }

    var hasAcceptedMedicalTerms: Boolean
        get() = prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, value).apply()

    var patientFullName: String
        get() = prefs.getString(KEY_PATIENT_NAME, "Paciente CannaMed") ?: "Paciente CannaMed"
        set(value) = prefs.edit().putString(KEY_PATIENT_NAME, value).apply()

    var patientIdNumber: String
        get() = prefs.getString(KEY_PATIENT_ID_NUM, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PATIENT_ID_NUM, value).apply()

    var identityDocEncryptedPath: String?
        get() = prefs.getString(KEY_IDENTITY_DOC_PATH, null)
        set(value) = prefs.edit().putString(KEY_IDENTITY_DOC_PATH, value).apply()

    var identitySelfieDocEncryptedPath: String?
        get() = prefs.getString(KEY_IDENTITY_SELFIE_PATH, null)
        set(value) = prefs.edit().putString(KEY_IDENTITY_SELFIE_PATH, value).apply()

    var identityVerifiedTimestamp: Long
        get() = prefs.getLong(KEY_IDENTITY_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_IDENTITY_TIMESTAMP, value).apply()

    val hasIdentityVerification: Boolean
        get() = !identityDocEncryptedPath.isNullOrBlank() && !identitySelfieDocEncryptedPath.isNullOrBlank()

    fun clearIdentityDocs() {
        prefs.edit()
            .remove(KEY_IDENTITY_DOC_PATH)
            .remove(KEY_IDENTITY_SELFIE_PATH)
            .remove(KEY_IDENTITY_TIMESTAMP)
            .apply()
    }

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_BIOMETRIC_LOCK = "key_biometric_lock"
        private const val KEY_SCREENSHOT_PROTECTION = "key_screenshot_protection"
        private const val KEY_IS_PREMIUM = "key_is_premium"
        private const val KEY_AGE_VERIFIED_21 = "key_age_verified_21_plus"
        private const val KEY_TERMS_ACCEPTED = "key_terms_accepted"
        private const val KEY_PATIENT_NAME = "key_patient_name"
        private const val KEY_PATIENT_ID_NUM = "key_patient_id_number"
        private const val KEY_IDENTITY_DOC_PATH = "key_identity_doc_encrypted_path"
        private const val KEY_IDENTITY_SELFIE_PATH = "key_identity_selfie_encrypted_path"
        private const val KEY_IDENTITY_TIMESTAMP = "key_identity_timestamp"
    }
}
