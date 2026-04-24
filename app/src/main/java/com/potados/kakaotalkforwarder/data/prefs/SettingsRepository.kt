package com.potados.kakaotalkforwarder.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.toSettings() }

    suspend fun current(): Settings = settings.first()

    suspend fun update(
        apiUrl: String,
        bearerToken: String,
        filterNickname: String,
    ) {
        store.edit { prefs ->
            prefs[Keys.API_URL] = apiUrl
            prefs[Keys.BEARER_TOKEN] = bearerToken
            prefs[Keys.FILTER_NICKNAME] = filterNickname.ifBlank { Settings.DEFAULT_FILTER_NICKNAME }
        }
    }

    private fun Preferences.toSettings() = Settings(
        apiUrl = this[Keys.API_URL].orEmpty(),
        bearerToken = this[Keys.BEARER_TOKEN].orEmpty(),
        filterNickname = this[Keys.FILTER_NICKNAME]?.takeIf { it.isNotBlank() }
            ?: Settings.DEFAULT_FILTER_NICKNAME,
    )

    private object Keys {
        val API_URL = stringPreferencesKey("api_url")
        val BEARER_TOKEN = stringPreferencesKey("bearer_token")
        val FILTER_NICKNAME = stringPreferencesKey("filter_nickname")
    }
}
