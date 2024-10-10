package com.proxyrack.control.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.proxyrack.control.domain.repository.DataAccessor
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first


class DataAccessorImpl(private val datastore: DataStore<Preferences>, private val key: String): DataAccessor {

    override suspend fun get(): String {
        val key = stringPreferencesKey(key)
        val preferences = datastore.data.first()
        return preferences[key] ?: ""
    }

    override suspend fun set(value: String) {
        val key = stringPreferencesKey(key)
        datastore.edit { ds ->
            ds[key] = value
        }
    }

}