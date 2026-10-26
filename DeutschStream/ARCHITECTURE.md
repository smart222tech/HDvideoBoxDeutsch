# DeutschStream – Architektur (Variante C)

- **UI:** Jetpack Compose; deutsche Oberfläche, Suche, Detailseite und Merkliste.
- **Provider-Vertrag:** `VideoSourceProvider` für autorisierte Quellen.
- **Media3:** Wiedergabe direkter HTTPS-Streams; Deutsch wird bei Ton und Untertiteln bevorzugt, sofern verfügbar.
- **KodiLauncher:** Übergibt eine autorisierte HTTPS-URL an die installierte Kodi-App. Add-ons werden nicht direkt in DeutschStream ausgeführt.
- **FavoritesStore:** DataStore speichert Titel-IDs, keine Stream-URLs.

## Grenzen
1. Der Katalog besteht derzeit aus Demo-Titeln.
2. Es gibt keinen Adapter, der Kodi-Python-Add-ons direkt ausführt.
3. Die Kodi-Übergabe hängt von der installierten Kodi-Version ab.
4. Sprachpräferenzen garantieren keine vorhandene deutsche Spur.
5. Keine DRM-Umgehung oder Einbindung nicht autorisierter Streams.
