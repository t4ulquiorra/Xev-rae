package com.xevrae.data.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.xevrae.common.ContextHolder
import com.xevrae.common.SETTINGS_FILENAME
import createDataStore

fun createDataStoreInstance(): DataStore<Preferences> {
    return createDataStore(
        producePath = { ContextHolder.context.filesDir.resolve("datastore/$SETTINGS_FILENAME.preferences_pb").absolutePath }
    )
}
