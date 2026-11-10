package com.example.util

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricCapability {
    READY,
    NOT_ENROLLED,
    NO_HARDWARE,
    UNAVAILABLE
}

object BiometricAuthHelper {

    fun checkBiometricCapability(context: Context): BiometricCapability {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
            else -> BiometricCapability.UNAVAILABLE
        }
    }

    fun canAuthenticate(context: Context): Boolean {
        return checkBiometricCapability(context) == BiometricCapability.READY
    }

    fun getBiometricStatusDescription(context: Context): String {
        return when (checkBiometricCapability(context)) {
            BiometricCapability.READY -> "Sensor biométrico listo (Huella / Rostro / PIN)"
            BiometricCapability.NOT_ENROLLED -> "Sin huella o PIN registrado en el dispositivo"
            BiometricCapability.NO_HARDWARE -> "Entorno sin sensor biométrico físico"
            BiometricCapability.UNAVAILABLE -> "Sensor biométrico temporalmente no disponible"
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Acceso Protegido a Datos Médicos",
        subtitle: String = "Autentícate para consultar tu registro de cannabis medicinal",
        description: String = "Garantiza la confidencialidad de tus recetas médicas, dosis e informes.",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels or negative button pressed
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Huella o rostro no reconocido. Inténtalo nuevamente.")
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        // On Android 11+ (API 30+), BIOMETRIC_STRONG or DEVICE_CREDENTIAL allows both biometric and device PIN/pattern
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            promptInfoBuilder.setNegativeButtonText("Cancelar")
        }

        try {
            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError("No se pudo iniciar el lector biométrico: ${e.message}")
        }
    }
}
