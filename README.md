# COVOIT'ALL Mobile

Application Android native (Kotlin, Activities + View Binding) construite selon l'architecture **MVVM**.

## Périmètre fonctionnel

- **Authentification et comptes** : inscription (passager ou conducteur), connexion, déconnexion, profil.
- **Covoiturage (passager)** : recherche de trajets avec filtres dynamiques et réservation de places, puis suivi des réservations (modification du nombre de places, annulation, retrait).
- **Navettes** : consultation des lignes, de leurs arrêts et horaires, et suivi GPS en temps réel de la navette sur une carte OpenStreetMap avec estimation du temps d'attente.
- **Conducteur** : publication, modification et suppression de trajets, acceptation ou refus des demandes de réservation, statut « arrivé » puis « terminé ».

Le paiement, les notifications push, le dépôt des documents et les espaces administrateur et chauffeur de navette sont assurés par l'application web.

## Architecture

```
ui/ (Activities)  ──observe LiveData──▶  ViewModels  ──▶  Repositories  ──▶  Retrofit (API FastAPI)
      │                                  (viewModelScope)        │
      └── appels de fonctions du ViewModel uniquement              └──▶  Room (cache + session)
```

| Critère | Mise en œuvre |
|---|---|
| Séparation des couches | `ui/*` : Activities qui observent les `LiveData` et appellent les fonctions du ViewModel. `data/*` : couche données. |
| ViewModel | `viewModelScope.launch` pour toutes les coroutines. `ui/common/ViewModelFactory.kt` instancie chaque ViewModel avec ses repositories (via `AppContainer`). |
| Repository | `AuthRepository` (session et jeton), séparé des repositories métier `LineRepository`, `RideRepository` et `ReservationRepository`. Ils synchronisent l'API et le cache Room, qui est la source unique de vérité observée par l'UI. |
| Data | `data/remote/ApiService.kt` : fonctions `suspend` avec `@GET`, `@POST`, `@PUT`, `@DELETE` et `@Body`. `data/local` : Room (`AppDatabase`, entités, DAO). |
| Authentification | `LoginRequest` / `RegisterRequest` envoyés par `AuthRepository`. |
| Session | Jeton JWT et utilisateur stockés dans l'entité Room `UserEntity` (table `session_user`). `MainActivity` utilise `authViewModel.authenticatedUser.observe(...)` pour afficher l'accueil ou le tableau de bord. Une réponse 401 vide la session. |
| CRUD | Trajets : GET `rides/mine`, POST `rides`, PUT `rides/{id}`, DELETE `rides/{id}`. Réservations : GET, POST, PUT (places ou annulation), DELETE. |
| Fonctionnalités avancées | Recherche multi-critères triée par départ, prix ou note. Calcul du montant total. Règles de validation (départ ≥ 15 min, 1 à 8 places, prix, jours d'un trajet régulier, départ ≠ arrivée). Indicateurs conducteur (taux de remplissage, revenus attendus, demandes en attente). Prochain départ calculé depuis les fiches horaires. Suivi GPS avec temps d'attente par arrêt. |
| Gestion des erreurs | `data/repository/Resource.kt` : `try/catch` (réseau, délai, JSON) et vérification de `response.isSuccessful`, avec le message `detail` de FastAPI affiché à l'utilisateur. |

## Lancement

1. Démarrer l'API depuis `backend` : `uvicorn app.main:app --host 0.0.0.0 --port 8000`.
2. Ouvrir le dossier `mobile` dans Android Studio, synchroniser Gradle et lancer sur un émulateur.

L'émulateur joint la machine hôte via `10.0.2.2:8000`. Pour un appareil physique, indiquez l'IP locale du PC :

```bash
./gradlew assembleDebug -PapiBaseUrl=http://192.168.1.20:8000/api/
```

Pour suivre une navette en direct, un chauffeur doit avoir démarré son service et partagé sa position depuis l'espace web « Mon service » (bouton « Simuler le parcours » pour une démonstration).
