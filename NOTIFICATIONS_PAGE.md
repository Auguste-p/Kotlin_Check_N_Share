# Page Notifications - Documentation

## Structure implémentée

### Composants créés

1. **item_notification.xml** - Layout réutilisable pour une notification
   - Photo de profil circulaire (48x48dp)
   - Nom de l'utilisateur et action
   - Icône et nom du lieu
   - Temps écoulé

2. **Notification.kt** - Classe de données
   ```kotlin
   data class Notification(
       val id: Int,
       val userName: String,
       val action: String,
       val location: String,
       val timeAgo: String,
       val profileImageUrl: String? = null
   )
   ```

3. **NotificationsAdapter.kt** - Adapteur RecyclerView
   - Gère l'affichage de la liste de notifications
   - Peut être mis à jour avec `updateNotifications()`

4. **NotificationsViewModel.kt** - ViewModel
   - Fournit des données de test (2 notifications)
   - Utilise LiveData pour les notifications

5. **NotificationsFragment.kt** - Fragment principal
   - Configure le RecyclerView avec LinearLayoutManager
   - Observe les changements de notifications

### Fragment Layout (fragment_notifications.xml)

- **Bandeau titre** : "Notifications" en gras, 24sp
- **Divider** : Ligne de séparation
- **RecyclerView** : Liste scrollable des notifications

## Comment ajouter plus de notifications

Dans `NotificationsViewModel.kt`, ajoutez des éléments à la liste dans `getSampleNotifications()` :

```kotlin
private fun getSampleNotifications(): List<Notification> {
    return listOf(
        Notification(
            id = 1,
            userName = "Marie Dubois",
            action = "a publié une photo",
            location = "Tour Eiffel, Paris",
            timeAgo = "5 min"
        ),
        Notification(
            id = 2,
            userName = "Alex Martin",
            action = "a publié une photo",
            location = "Le Comptoir du Relais, Paris",
            timeAgo = "1h"
        ),
        // Ajoutez vos notifications ici
        Notification(
            id = 3,
            userName = "Sophie Bernard",
            action = "a commenté votre photo",
            location = "Musée du Louvre, Paris",
            timeAgo = "2h"
        )
    )
}
```

## Prochaines étapes

1. **Charger des images de profil réelles**
   - Utiliser Glide ou Coil pour charger les images depuis des URLs
   - Ajouter la dépendance dans `build.gradle.kts`

2. **Intégration avec la base de données**
   - Récupérer les notifications depuis SQLite
   - Implémenter un système de notifications en temps réel

3. **Interaction utilisateur**
   - Ajouter un click listener sur chaque notification
   - Naviguer vers le contenu concerné (photo, profil, etc.)

4. **Améliorer le design**
   - Ajouter des animations
   - Marquer les notifications comme lues/non lues
   - Ajouter un système de pagination pour les longues listes

