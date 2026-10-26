# DeutschStream Android – Variante C

Deutschsprachiges Android-Projekt für Smartphone, Android TV und Google TV.

## Bauen
- Android Studio, Android SDK 36 und JDK 17
- Android Studio: **Open** → Ordner `DeutschStream` öffnen
- Alternativ wird bei Änderungen in diesem Ordner automatisch GitHub Actions gestartet.

## Enthalten
- Jetpack Compose Oberfläche mit Demo-Katalog und Suche
- Merkliste über DataStore
- Media3/ExoPlayer für direkte HTTPS-Video-URLs
- Bevorzugte deutsche Ton- und Untertitelspuren
- Startversuch für Kodi mit einer autorisierten HTTPS-URL
- Provider-Schnittstelle als Grundlage für autorisierte Quellen

## Noch nicht enthalten
- Echter Film-/Serienkatalog oder automatische Quellensuche
- Direkte Ausführung von Kodi-Python-Add-ons in DeutschStream
- Ein bestätigter APK-Build; GitHub Actions wird dies beim ersten Lauf prüfen.

Kodi muss separat installiert sein. Nutze ausschließlich Quellen, die du rechtmäßig verwenden darfst.
