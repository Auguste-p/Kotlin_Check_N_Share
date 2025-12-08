# 🧪 Guide de test - Page Notifications

## Comment tester la page notifications

### 1. **Lancer l'application**
```bash
cd /Users/auguste/Documents/Ynov-Cours/M2/Dev_mobile/App_Kotlin
./gradlew installDebug
```

### 2. **Navigation**
- Ouvrir l'application
- Cliquer sur l'onglet "Notifications" dans la barre de navigation inférieure (icône 🔔)

### 3. **Vérifications attendues**

#### ✅ Bandeau titre
- Le titre "Notifications" doit apparaître en haut
- Taille: 24sp
- Style: Bold
- Couleur: Noir

#### ✅ Liste des notifications
Vous devriez voir **8 notifications** :

1. **Marie Dubois** a publié une photo
   - 📍 Tour Eiffel, Paris
   - ⏰ 5 min

2. **Alex Martin** a publié une photo
   - 📍 Le Comptoir du Relais, Paris
   - ⏰ 1h

3. **Sophie Bernard** a commenté votre photo
   - 📍 Musée du Louvre, Paris
   - ⏰ 2h

4. **Thomas Laurent** a aimé votre photo
   - 📍 Arc de Triomphe, Paris
   - ⏰ 3h

5. **Julie Moreau** a publié une photo
   - 📍 Cathédrale Notre-Dame, Paris
   - ⏰ 5h

6. **Pierre Petit** vous suit maintenant
   - 📍 Montmartre, Paris
   - ⏰ 1j

7. **Emma Durand** a mentionné dans un commentaire
   - 📍 Jardin du Luxembourg, Paris
   - ⏰ 2j

8. **Lucas Martin** a partagé votre photo
   - 📍 Basilique du Sacré-Cœur, Paris
   - ⏰ 3j

#### ✅ Comportement scrollable
- Faites défiler la liste vers le bas
- La liste doit être scrollable si elle dépasse la hauteur de l'écran
- Le titre doit rester fixe en haut

#### ✅ Design de chaque notification
- Photo de profil circulaire (icône par défaut)
- Nom de l'utilisateur en noir
- Action en texte normal
- Icône de localisation grise
- Nom du lieu en gris
- Temps écoulé aligné à droite en gris clair

### 4. **Test de modification**

Pour ajouter plus de notifications, éditez :
`NotificationsViewModel.kt` → fonction `getSampleNotifications()`

```kotlin
Notification(
    id = 9,
    userName = "Test User",
    action = "a fait quelque chose",
    location = "Paris, France",
    timeAgo = "10 min"
)
```

### 5. **Débogage**

Si la page ne s'affiche pas correctement :

1. Vérifier les logs Android :
```bash
adb logcat | grep CheckNShare
```

2. Rebuilder le projet :
```bash
./gradlew clean build
```

3. Vérifier que les fichiers suivants existent :
   - ✅ `item_notification.xml`
   - ✅ `fragment_notifications.xml`
   - ✅ `Notification.kt`
   - ✅ `NotificationsAdapter.kt`
   - ✅ `NotificationsViewModel.kt`
   - ✅ `NotificationsFragment.kt`
   - ✅ `ic_profile_default.xml`

## 🎉 Résultat attendu

Une page de notifications scrollable avec 8 notifications affichées, chacune montrant :
- Une photo de profil par défaut
- Le nom et l'action
- Le lieu avec une icône
- Le temps écoulé

**Le composant est maintenant prêt à être multiplié et intégré avec votre base de données !**

