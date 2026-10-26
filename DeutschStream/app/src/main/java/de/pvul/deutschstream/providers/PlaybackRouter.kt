package de.pvul.deutschstream.providers

enum class PlaybackTarget { MEDIA3, KODI, UNAVAILABLE }

fun VideoSource.playbackTarget(): PlaybackTarget = when {
    requiresExternalApp -> PlaybackTarget.KODI
    !playbackUrl.isNullOrBlank() && playbackUrl.startsWith("https://", ignoreCase = true) -> PlaybackTarget.MEDIA3
    else -> PlaybackTarget.UNAVAILABLE
}
