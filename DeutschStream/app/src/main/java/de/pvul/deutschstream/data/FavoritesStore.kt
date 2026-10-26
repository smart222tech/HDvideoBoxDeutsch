package de.pvul.deutschstream.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore by preferencesDataStore(name = "deutschstream_preferences")

/** Persistente Merkliste. Gespeichert werden nur stabile Titel-IDs, keine Stream-URLs. */
class FavoritesStore(private val context: Context) {
    private val favoritesKey = stringSetPreferencesKey("favorite_media_ids")
    val favorites: Flow<Set<String>> = context.favoritesDataStore.data.map { prefs ->
        prefs[favoritesKey] ?: emptySet()
    }

    suspend fun setFavorite(id: String, enabled: Boolean) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[favoritesKey]?.toMutableSet() ?: mutableSetOf()
            if (enabled) current.add(id) else current.remove(id)
            prefs[favoritesKey] = current
        }
    }

    suspend fun toggleFavorite(id: String, currentlyFavorite: Boolean) =
        setFavorite(id, !currentlyFavorite)
}
