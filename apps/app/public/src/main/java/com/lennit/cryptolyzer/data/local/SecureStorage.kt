package com.lennit.cryptolyzer.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MODULE 09/11: EncryptedSharedPreferences — hardware-backed key storage.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "lennit_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun get(key: String, default: String = "") = prefs.getString(key, default) ?: default
    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun contains(key: String) = prefs.contains(key)
    fun clear() = prefs.edit().clear().apply()
}
