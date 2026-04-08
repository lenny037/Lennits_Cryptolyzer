package com.lennit.cryptolyzer.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * MODULE 09: Android Keystore-backed AES-GCM encryption.
 * All keys live in the hardware-backed Android Keystore (TEE on S20 FE).
 */
class KeystoreManager(private val keyAlias: String = "lennit_master_key") {

    private val KEYSTORE   = "AndroidKeyStore"
    private val ALGO       = "AES/GCM/NoPadding"
    private val TAG_LENGTH = 128

    init { ensureKey() }

    private fun ensureKey() {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        if (!ks.containsAlias(keyAlias)) {
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                setKeySize(256)
                setUserAuthenticationRequired(false)
            }.build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
                .apply { init(spec) }
                .generateKey()
        }
    }

    private fun getKey(): SecretKey =
        (KeyStore.getInstance(KEYSTORE).apply { load(null) }
            .getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv         = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined   = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        val combined   = Base64.decode(encoded, Base64.NO_WRAP)
        val iv         = combined.copyOfRange(0, 12)
        val ciphertext = combined.copyOfRange(12, combined.size)
        val cipher     = Cipher.getInstance(ALGO)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
