# Meteapp (TP Météo)

Application Android minimale pour consulter des prévisions météo via l'API gratuite Open-Meteo.

Résumé
------
Meteapp affiche la météo pour des villes recherchées ou pour la position de l'utilisateur. Le projet utilise Jetpack Compose, Retrofit, Room et Fused Location Provider (Play Services). Il implémente : recherche geocoding, ajout/suppression de favoris, détail météo, cache local (Room) et gestion de la permission de localisation.

Fonctionnalités
---------------
- Écran d'accueil avec barre de recherche.
- Recherche de ville via l'API de geocoding d'Open-Meteo.
- Ajouter/Supprimer des villes en favoris. (Suppression via appui long sur une carte.)
- Récupération de la météo pour une ville (modèle meteofrance_seamless recommandé dans repo).
- Affichage d'un écran détail pour la ville sélectionnée (température actuelle, min/max, vent, condition).
- Ajout d'un favori depuis la géolocalisation (permission runtime gérée).
- Cache des réponses (Room) pour permettre un fonctionnement basique hors-ligne.
- Gestion des erreurs réseau et affichage d'un message utilisateur.

Architecture & organisation
---------------------------
- `ui/` : écrans Compose (HomeScreen, DetailScreen, components).
- `data/remote` : modèles et API Retrofit.
- `data/local` : entités Room, DAO et base de données (cache + favoris).
- `data/repository` : `WeatherRepository` — centralise cache + réseau.
- `location/` : helper de localisation (FusedLocationProvider).
- `ui/navigation/Navigation.kt` : NavHost Compose.

Permissions
-----------
- `android.permission.INTERNET` (obligatoire pour les requêtes réseau)
- `android.permission.ACCESS_FINE_LOCATION` et `ACCESS_COARSE_LOCATION` (pour la position)

Construction et exécution
-------------------------
Pré-requis : JDK 11, Android SDK (compileSdk 36), appareil ou émulateur Android.

Depuis la racine du projet (PowerShell sous Windows) :

```powershell
# Build debug
.\gradlew.bat assembleDebug

# Installer l'APK sur un appareil connecté
.\gradlew.bat installDebug

# Nettoyer
.\gradlew.bat clean
```

Notes d'utilisation
-------------------
- La première fois que tu utilises "Ajouter ma position", l'application demandera la permission de localisation. Si l'utilisateur refuse définitivement, un dialog propose d'ouvrir les paramètres de l'application.
- Pour supprimer un favori : rester appuyé (long-press) sur la carte du favori, puis confirmer la suppression.
- Les noms de villes sont encodés dans l'URL de navigation pour éviter les problèmes d'espaces/caractères spéciaux.

API utilisées
-------------
- Geocoding : https://geocoding-api.open-meteo.com/v1/search?name=[VILLE]
- Forecast : https://api.open-meteo.com/v1/forecast?latitude=[lat]&longitude=[lon]&hourly=temperature_2m,...&models=meteofrance_seamless

Cache et fonctionnement hors-ligne
---------------------------------
- `WeatherRepository` met en cache les réponses météo dans Room (entité `WeatherCacheEntity`).
- En cas d'absence de réseau, le repository tente de retourner les données mises en cache (si elles existent). TTL simple appliqué côté repository.

Tests
-----
- Pas (ou peu) de tests fournis pour ce TP. Pour ajouter des tests :
  - DAO tests : utiliser une base Room in-memory.
  - Repository tests : MockWebServer pour simuler l'API Open-Meteo.

Problèmes connus / limites
-------------------------
- UI simple, icons météo sont des emojis pour l'instant (remplacer par vecteurs pour production).
- Pas d'injection de dépendances (Hilt) — repository instancié manuellement dans les Composables.
- Permissions runtime gérées simplement; pour Android 13+ on pourrait différencier precise/approximate.

Améliorations possibles
-----------------------
- Remplacer les emojis par des icônes vectorielles (SVG) pour un rendu professionnel.
- Ajouter animations et transitions (entrées des cartes, suppressions).
- Ajouter tests unitaires et instrumentation.
- Migrer vers Hilt pour DI et meilleure testabilité.
- Ajouter un travail planifié (WorkManager) pour rafraîchir périodiquement les favoris.

Contact & dépôt Git
-------------------
- Héberge le projet sur un dépôt git de ton choix (GitHub/GitLab). Commits et README suffisent pour rendre le TP.

Licence
-------
Code fourni sous licence permissive pour usage pédagogique.

