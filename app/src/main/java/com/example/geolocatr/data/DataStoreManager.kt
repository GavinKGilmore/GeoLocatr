package com.example.geolocatr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {
    companion object {
        private const val DATA_STORE_NAME = "datastore"

        private val Context.dataStore: DataStore<Preferences>
                by preferencesDataStore(name = DATA_STORE_NAME)

        private val BUILDING_KEY = booleanPreferencesKey("building_enabled")
        private val TOOLBAR_KEY = booleanPreferencesKey("toolbar_enabled")
    }

    val buildingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BUILDING_KEY] ?: true
    }

    val toolbarFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TOOLBAR_KEY] ?: true
    }

    suspend fun setBuildings(newValue: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BUILDING_KEY] = newValue
        }
    }

    suspend fun setToolbar(newValue: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TOOLBAR_KEY] = newValue
        }
    }
}