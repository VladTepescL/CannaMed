package com.example.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

/**
 * Enterprise-grade cryptographic security utility conforming to HIPAA Security Rule (45 CFR § 164.312(a)(2)(iv))
 * and GDPR Article 32 (Security of processing).
 *
 * Implements:
 * - PBKDF2 with HMAC-SHA256 and 65,536 iterations for key derivation
 * - AES-256 in Galois/Counter Mode (GCM) with 128-bit authentication tag
 * - Cryptographically secure 12-byte IV per encryption operation
 * - Integrity and confidentiality verification
 */
object CryptoSecurityUtil {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH_BIT = 256

    /**
     * Encrypts plaintext payload with user-provided passphrase using AES-256-GCM.
     * Returns a structured JSON string containing salt, IV, ciphertext and crypto metadata.
     */
    fun encryptPayload(plainText: String, passphrase: CharArray): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH_BYTE).apply { random.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTE).apply { random.nextBytes(this) }

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, ITERATION_COUNT, KEY_LENGTH_BIT)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val exportJson = JSONObject()
        exportJson.put("format", "CANNAMED_SECURE_ENCRYPTED_BACKUP")
        exportJson.put("cryptoStandard", "AES-256-GCM / PBKDF2-HMAC-SHA256")
        exportJson.put("iterations", ITERATION_COUNT)
        exportJson.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
        exportJson.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
        exportJson.put("ciphertext", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
        exportJson.put("timestamp", System.currentTimeMillis())

        return exportJson.toString(2)
    }

    /**
     * Decrypts an encrypted payload JSON using user passphrase.
     * Throws an exception if authentication tag fails or passphrase is wrong.
     */
    fun decryptPayload(encryptedJsonString: String, passphrase: CharArray): String {
        val json = JSONObject(encryptedJsonString)
        val salt = Base64.decode(json.getString("salt"), Base64.DEFAULT)
        val iv = Base64.decode(json.getString("iv"), Base64.DEFAULT)
        val cipherBytes = Base64.decode(json.getString("ciphertext"), Base64.DEFAULT)
        val iterations = json.optInt("iterations", ITERATION_COUNT)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, iterations, KEY_LENGTH_BIT)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decryptedBytes = cipher.doFinal(cipherBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Checks if the backup payload string is an encrypted package.
     */
    fun isEncryptedBackup(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            json.optString("format") == "CANNAMED_SECURE_ENCRYPTED_BACKUP"
        } catch (_: Exception) {
            false
        }
    }
}
