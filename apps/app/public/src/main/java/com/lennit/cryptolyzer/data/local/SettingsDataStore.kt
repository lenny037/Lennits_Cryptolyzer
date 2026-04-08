package com.lennit.cryptolyzer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lennit.cryptolyzer.domain.model.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lennit_settings")

/**
 * MODULE 11: DataStore-backed settings persistence.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_API_ENDPOINT  = stringPreferencesKey("api_endpoint")
        private val KEY_API_KEY       = stringPreferencesKey("api_key")
        private val KEY_DEMO_MODE     = booleanPreferencesKey("use_demo_mode")
        private val KEY_DEVIATION     = doublePreferencesKey("deviation_threshold")
        private val KEY_POLL_INTERVAL = doublePreferencesKey("poll_interval")
        private val KEY_MAX_POSITION  = doublePreferencesKey("max_position_usd")
        private val KEY_DARK_MODE     = booleanPreferencesKey("dark_mode")
        private val KEY_BIOMETRIC     = booleanPreferencesKey("biometric_enabled")
    }

    val appConfig: Flow<AppConfig> = context.dataStore.data.map { prefs ->
        AppConfig(
            apiEndpoint          = prefs[KEY_API_ENDPOINT]  ?: "",
            apiKey               = prefs[KEY_API_KEY]       ?: "",
            useDemoMode          = prefs[KEY_DEMO_MODE]     ?: false,
            deviationThreshold   = prefs[KEY_DEVIATION]     ?: 5.0,
            pollIntervalSeconds  = prefs[KEY_POLL_INTERVAL] ?: 2.0,
            maxPositionSizeUsd   = prefs[KEY_MAX_POSITION]  ?: 10_000.0,
            isDarkMode           = prefs[KEY_DARK_MODE]     ?: true,
            biometricEnabled     = prefs[KEY_BIOMETRIC]     ?: true,
        )
    }

    suspend fun saveApiEndpoint(value: String)   = save(KEY_API_ENDPOINT,  value)
    suspend fun saveApiKey(value: String)         = save(KEY_API_KEY,       value)
    suspend fun saveDemoMode(value: Boolean)      = save(KEY_DEMO_MODE,     value)
    suspend fun saveDeviation(value: Double)      = save(KEY_DEVIATION,     value)
    suspend fun savePollInterval(value: Double)   = save(KEY_POLL_INTERVAL, value)
    suspend fun saveMaxPosition(value: Double)    = save(KEY_MAX_POSITION,  value)
    suspend fun saveDarkMode(value: Boolean)      = save(KEY_DARK_MODE,     value)
    suspend fun saveBiometric(value: Boolean)     = save(KEY_BIOMETRIC,     value)

    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
