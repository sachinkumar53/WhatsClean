package com.sachin.app.whatsclean.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.SortType
import com.sachin.app.whatsclean.data.module.ApplicationScope
import com.sachin.app.whatsclean.util.extension.externalStorageDir
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @ApplicationScope
    private val scope: CoroutineScope
) {
    private val dataStore = context.dataStore

    fun setWhatsAppDirectory(path: String) = edit(KEY.WHATSAPP_PATH, path)

    fun getWhatsAppDirectory(): Flow<String> = dataStore.data.map {
        it[KEY.WHATSAPP_PATH] ?: kotlin.run {
            val sdCard = context.externalStorageDir
            val whatsAppFolder = File(sdCard, "WhatsApp")
            whatsAppFolder.absolutePath
        }
    }

    fun getFingerprint(): Flow<Boolean> = get(KEY.FINGERPRINT, false)

    fun setFingerPrint(enabled: Boolean) = edit(KEY.FINGERPRINT, enabled)

    fun setSortType(sortType: SortType) = edit(KEY.SORT_BY, sortType.name)

    fun getSortType(): Flow<SortType> = dataStore.data.map {
        it[KEY.SORT_BY]?.let { name ->
            SortType.valueOf(name)
        } ?: SortType.NEWEST_FIRST
    }

    fun setTheme(theme: String) = edit(KEY.THEME, theme)
    fun getTheme(): Flow<String> = get(KEY.THEME, context.getString(R.string.pref_default_theme))


    fun setTreeUri(uri: Uri) = edit(KEY.TREE_URI, uri.toString())

    fun getTreeUri(): Flow<Uri?> = dataStore.data.map {
        it[KEY.TREE_URI]?.let { uri -> Uri.parse(uri) }
    }

    private object KEY {
        val THEME = stringPreferencesKey("theme")
        val WHATSAPP_PATH = stringPreferencesKey("whatsapp_path")
        val FINGERPRINT = booleanPreferencesKey("fingerprint")
        val SORT_BY = stringPreferencesKey("sort_by")
        val TREE_URI = stringPreferencesKey("tree_uri")
    }

    private fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> = dataStore.data.catch {
        Log.e(TAG, "get: Error getting value for key: $key", it)
    }.map {
        it[key] ?: defaultValue
    }

    private fun <T> edit(key: Preferences.Key<T>, value: T) = scope.launch {
        dataStore.edit {
            it[key] = value
        }
    }

}

private const val TAG = "SettingsManager"

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")