package com.example.util

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Manages zero-knowledge hardware-backed encrypted storage for sensitive medical documents (prescriptions, labs)
 * utilizing AndroidX Security Crypto MasterKey (AES-256-GCM backed by Android KeyStore).
 */
object EncryptedFileManager {

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    /**
     * Saves an input stream as an encrypted file on internal storage using AES256_GCM_HKDF_4KB.
     */
    fun saveEncryptedFile(context: Context, inputStream: InputStream, fileName: String): File {
        val docsDir = File(context.filesDir, "medical_encrypted_docs")
        if (!docsDir.exists()) docsDir.mkdirs()

        val destFile = File(docsDir, fileName)
        if (destFile.exists()) destFile.delete()

        val masterKey = getMasterKey(context)
        val encryptedFile = EncryptedFile.Builder(
            context,
            destFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return destFile
    }

    /**
     * Opens an encrypted file and provides the decrypted InputStream.
     */
    fun openDecryptedInputStream(context: Context, file: File): InputStream {
        val masterKey = getMasterKey(context)
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return encryptedFile.openFileInput()
    }

    /**
     * Decrypts an encrypted file into a temporary cache file (for external viewing or sharing).
     */
    fun decryptToTempFile(context: Context, encryptedFile: File, tempFileName: String): File {
        val tempDir = File(context.cacheDir, "temp_decrypted_docs")
        if (!tempDir.exists()) tempDir.mkdirs()

        val tempFile = File(tempDir, tempFileName)
        if (tempFile.exists()) tempFile.delete()

        openDecryptedInputStream(context, encryptedFile).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    /**
     * Checks if a file path is an encrypted document managed by this utility.
     */
    fun isEncryptedDocPath(path: String?): Boolean {
        if (path == null) return false
        return path.contains("medical_encrypted_docs")
    }
}
