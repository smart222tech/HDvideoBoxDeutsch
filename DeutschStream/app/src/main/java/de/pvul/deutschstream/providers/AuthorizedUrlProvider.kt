package de.pvul.deutschstream.providers

/** MVP-Provider für eigene oder anderweitig autorisierte HTTPS-Teststreams. */
class AuthorizedUrlProvider(
    private val entries: List<VideoSource> = emptyList()
) : VideoSourceProvider {
    override val id = "authorized-url"
    override val displayName = "Eigene / autorisierte Quellen"

    override suspend fun search(query: String): List<VideoSource> = entries.filter {
        it.title.contains(query, ignoreCase = true) ||
            it.providerName.contains(query, ignoreCase = true)
    }
}
