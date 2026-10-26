package de.pvul.deutschstream.providers

data class VideoSource(
    val id: String,
    val providerName: String,
    val title: String,
    val playbackUrl: String?,
    val audioLanguages: List<String>,
    val subtitleLanguages: List<String>,
    val qualityLabel: String?,
    val requiresExternalApp: Boolean = false,
    val note: String? = null
)

interface VideoSourceProvider {
    val id: String
    val displayName: String
    suspend fun search(query: String): List<VideoSource>
}

fun choosePreferredLanguage(languages: List<String>, preferred: String = "de"): String? =
    languages.firstOrNull { it.equals(preferred, ignoreCase = true) }
        ?: languages.firstOrNull { it.startsWith("$preferred-", ignoreCase = true) }
