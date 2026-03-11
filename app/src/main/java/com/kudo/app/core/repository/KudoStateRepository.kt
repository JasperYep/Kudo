package com.kudo.app.core.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoStateJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class KudoStateRepository(
    private val context: Context,
    applicationScope: CoroutineScope
) {

    private data class Snapshot(
        val state: KudoState,
        val theme: String
    )

    private val snapshot: StateFlow<Snapshot> = context.dataStore.data
        .map { preferences ->
            Snapshot(
                state = KudoStateJson.decode(preferences[Keys.STATE_JSON]),
                theme = preferences[Keys.THEME_MODE] ?: THEME_SYSTEM
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = Snapshot(
                state = KudoState(),
                theme = THEME_SYSTEM
            )
        )

    val state: Flow<KudoState> = snapshot
        .map { it.state }
        .distinctUntilChanged()

    val theme: Flow<String> = snapshot
        .map { it.theme }
        .distinctUntilChanged()

    suspend fun getState(): KudoState {
        return snapshot.value.state
    }

    suspend fun saveState(state: KudoState) {
        val encoded = withContext(Dispatchers.Default) {
            KudoStateJson.encode(KudoStateJson.sanitize(state))
        }
        context.dataStore.edit { preferences ->
            preferences[Keys.STATE_JSON] = encoded
        }
    }

    suspend fun updateState(transform: (KudoState) -> KudoState) {
        val encoded = withContext(Dispatchers.Default) {
            KudoStateJson.encode(
                KudoStateJson.sanitize(transform(snapshot.value.state))
            )
        }
        context.dataStore.edit { preferences ->
            preferences[Keys.STATE_JSON] = encoded
        }
    }

    suspend fun importJson(raw: String): Boolean {
        val parsed = KudoStateJson.decodeOrNull(raw) ?: return false
        saveState(parsed)
        return true
    }

    suspend fun importFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val raw = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                reader.readText()
            } ?: error("Unable to read backup file")
        }.getOrNull() ?: return@withContext false

        importJson(raw)
    }

    suspend fun exportJson(): String {
        return KudoStateJson.encode(getState())
    }

    suspend fun exportToUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val raw = exportJson()
            context.contentResolver.openOutputStream(uri, "wt")
                ?.bufferedWriter(Charsets.UTF_8)
                ?.use { writer ->
                    writer.write(raw)
                }
                ?: error("Unable to create backup file")
        }.isSuccess
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = theme
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "kudo_state")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        private object Keys {
            val STATE_JSON = stringPreferencesKey("state_json")
            val THEME_MODE = stringPreferencesKey("theme_mode")
        }
    }
}
