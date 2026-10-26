package de.pvul.deutschstream

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.pvul.deutschstream.integration.KodiLauncher
import de.pvul.deutschstream.data.FavoritesStore
import kotlinx.coroutines.launch

data class MediaEntry(
    val id: String,
    val title: String,
    val year: Int,
    val kind: String,
    val description: String,
    val audioNote: String = "Tonspur abhängig von der Quelle"
)

private val demoCatalog = listOf(
    MediaEntry("demo1", "Beispiel: Der letzte Stern", 2024, "Film",
        "Demo-Eintrag für die Oberfläche. Dies ist kein echter Filmstream."),
    MediaEntry("demo2", "Beispiel: Stadt ohne Schlaf", 2023, "Serie",
        "Demo-Eintrag für die Oberfläche. Quellen werden später über Anbieter ergänzt."),
    MediaEntry("demo3", "Beispiel: Reise zum Nordlicht", 2022, "Film",
        "Hier erscheinen später echte Katalogdaten kompatibler Anbieter.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DeutschStreamApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeutschStreamApp() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<MediaEntry?>(null) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var streamUrl by remember { mutableStateOf("") }
    val favoritesStore = remember(context) { FavoritesStore(context.applicationContext) }
    val favorites by favoritesStore.favorites.collectAsState(initial = emptySet())
    val coroutineScope = rememberCoroutineScope()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Bereit – noch keine Videoquelle verbunden.") }

    val filtered = demoCatalog.filter {
        (it.title.contains(query, ignoreCase = true) ||
            it.kind.contains(query, ignoreCase = true) ||
            it.year.toString().contains(query)) &&
            (!showFavoritesOnly || favorites.contains(it.id))
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DeutschStream") },
                    actions = {
                        TextButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                            Text(if (showFavoritesOnly) "Alle Titel" else "♥ Merkliste")
                        }
                    }
                )
            }
        ) { padding ->
            if (selected == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Filme und Serien – auf Deutsch entdecken",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Film, Serie oder Jahr suchen") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(status, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered, key = { it.id }) { item ->
                            MediaCard(
                                item = item,
                                isFavorite = favorites.contains(item.id),
                                onFavorite = {
                                    coroutineScope.launch {
                                        favoritesStore.toggleFavorite(item.id, favorites.contains(item.id))
                                    }
                                },
                                onOpen = { selected = item }
                            )
                        }
                    }
                    if (filtered.isEmpty()) Text("Keine Treffer. Suche anpassen oder Merkliste prüfen.")
                }
            } else {
                val item = selected!!
                DetailScreen(
                    item = item,
                    isFavorite = favorites.contains(item.id),
                    onBack = { selected = null },
                    onFavorite = {
                        coroutineScope.launch {
                            favoritesStore.toggleFavorite(item.id, favorites.contains(item.id))
                        }
                    },
                    onPlay = { showUrlDialog = true }
                )
            }
        }
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Videoquelle öffnen") },
            text = {
                Column {
                    Text("Füge die HTTPS-URL einer Videoquelle ein, die du nutzen darfst (z. B. einen eigenen Teststream).")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = { streamUrl = it },
                        label = { Text("HTTPS-Video-URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = {
                        val uri = runCatching { Uri.parse(streamUrl.trim()) }.getOrNull()
                        if (uri != null && uri.scheme == "https" && !uri.host.isNullOrBlank()) {
                            showUrlDialog = false
                            status = "Video wird im Media3-Player geöffnet."
                            selected?.let { item ->
                                PlayerActivityStarter.open(context, uri.toString(), item.title)
                            }
                        } else status = "Bitte eine gültige HTTPS-URL eingeben."
                    }) { Text("Media3") }
                    TextButton(onClick = {
                        when (val result = KodiLauncher.openVideo(context, streamUrl, selected?.title ?: "DeutschStream")) {
                            KodiLauncher.Result.Started -> {
                                showUrlDialog = false
                                status = "Kodi wurde gestartet. Ob diese URL unterstützt wird, hängt von Kodi ab."
                            }
                            KodiLauncher.Result.NotInstalled -> status = "Kodi ist auf diesem Gerät nicht installiert."
                            is KodiLauncher.Result.Failed -> status = result.message
                        }
                    }) { Text("Kodi") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun MediaCard(
    item: MediaEntry,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen() }) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("${item.kind} · ${item.year}", style = MaterialTheme.typography.bodyMedium)
                Text(item.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Aus Merkliste entfernen" else "Zur Merkliste hinzufügen"
                )
            }
        }
    }
}

@Composable
private fun DetailScreen(
    item: MediaEntry,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onPlay: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Zurück") }
        Text(item.title, style = MaterialTheme.typography.headlineMedium)
        Text("${item.kind} · ${item.year}")
        Text(item.description)
        Text("Audio: ${item.audioNote}")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Quelle abspielen")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onFavorite) {
                Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isFavorite) "Gemerkt" else "Merken")
            }
        }
        Text(
            "Hinweis: Die angezeigten Titel sind Platzhalter. Dieses Grundgerüst enthält noch keinen echten Filmkatalog und keine Kodi-Addon-Bridge.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
