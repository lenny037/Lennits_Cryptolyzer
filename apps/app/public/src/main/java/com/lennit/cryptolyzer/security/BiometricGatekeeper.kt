package com.lennit.cryptolyzer.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * MODULE 09: Biometric authentication with sealed result types.
 *
 * Three distinct outcomes:
 *   AuthResult.Success  — biometrics matched, proceed
 *   AuthResult.Failure  — biometrics mismatch (retryable)
 *   AuthResult.Error    — hardware issue / user cancelled
 */
class BiometricGatekeeper(private val activity: FragmentActivity) {

    sealed class AuthResult {
        object Success : AuthResult()
        object Failure : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    fun canAuthenticate(): Boolean =
        BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        BiometricManager.BIOMETRIC_SUCCESS

    /** Convenience overload — maps to Boolean pass/fail. */
    fun authenticate(onResult: (Boolean) -> Unit) {
        authenticateWithResult { onResult(it is AuthResult.Success) }
    }

    fun authenticateWithResult(onResult: (AuthResult) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) =
                onResult(AuthResult.Success)
            override fun onAuthenticationFailed() =
                onResult(AuthResult.Failure)
            override fun onAuthenticationError(code: Int, msg: CharSequence) =
                onResult(AuthResult.Error(msg.toString()))
        }

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Lennit CryptoLyzer")
            .setSubtitle("Authenticate to authorize")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(info)
    }
}
