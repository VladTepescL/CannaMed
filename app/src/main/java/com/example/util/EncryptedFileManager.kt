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
     * Saves an input stream as an encrypted file into a specific internal storage subfolder using AES256_GCM_HKDF_4KB.
     */
    fun saveEncryptedFileToDir(context: Context, inputStream: InputStream, targetDirName: String, fileName: String): File {
        val targetDir = File(context.filesDir, targetDirName)
        if (!targetDir.exists()) targetDir.mkdirs()

        val destFile = File(targetDir, fileName)
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
     * Saves patient identification documents (DNI or selfie holding DNI) into private sandbox.
     */
    fun saveEncryptedIdentityDoc(context: Context, inputStream: InputStream, fileName: String): File {
        return saveEncryptedFileToDir(context, inputStream, "patient_identity", fileName)
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
     * Exports a standard copy of a video to the public device gallery (Movies/CannaMed) via MediaStore.
     */
    fun exportVideoToGallery(context: Context, sourceVideoFile: File, title: String): android.net.Uri? {
        return try {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, "$title.mp4")
                put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(android.provider.MediaStore.Video.Media.DESCRIPTION, "Evidencia Médica CannaMed (Copia de Respaldo)")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/CannaMed")
                    put(android.provider.MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val collectionUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val itemUri = resolver.insert(collectionUri, values) ?: return null

            resolver.openOutputStream(itemUri)?.use { output ->
                sourceVideoFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.clear()
                values.put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
            itemUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks if a file path is an encrypted document managed by this utility.
     */
    fun isEncryptedDocPath(path: String?): Boolean {
        if (path == null) return false
        return path.contains("medical_encrypted_docs") ||
                path.contains("patient_identity") ||
                path.contains("emergency_recordings") ||
                path.endsWith(".cannaenc")
    }
}
