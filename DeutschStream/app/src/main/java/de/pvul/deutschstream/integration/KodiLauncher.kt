package de.pvul.deutschstream.integration

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object KodiLauncher {
    const val KODI_PACKAGE = "org.xbmc.kodi"

    sealed interface Result {
        data object Started : Result
        data object NotInstalled : Result
        data class Failed(val message: String) : Result
    }

    fun openVideo(context: Context, url: String, title: String): Result {
        val uri = runCatching { Uri.parse(url.trim()) }.getOrNull()
            ?: return Result.Failed("Ungültige URL")
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
            return Result.Failed("Nur gültige HTTPS-URLs werden unterstützt.")
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(KODI_PACKAGE)
            setDataAndType(uri, "video/*")
            putExtra(Intent.EXTRA_TITLE, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Result.Started
        } catch (_: ActivityNotFoundException) {
            Result.NotInstalled
        } catch (e: Exception) {
            Result.Failed(e.localizedMessage ?: "Kodi konnte nicht gestartet werden.")
        }
    }
}
