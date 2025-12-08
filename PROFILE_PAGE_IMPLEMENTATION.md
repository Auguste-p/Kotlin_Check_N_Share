# Page de Profil - Implémentation

## Résumé
La page de profil a été implémentée selon la capture d'écran fournie, avec tous les éléments demandés.

## Fichiers créés/modifiés

### 1. Layout principal du profil
**Fichier:** `app/src/main/res/layout/fragment_profile.xml`
- Photo de profil circulaire (100dp x 100dp)
- Nom d'utilisateur en dessous (24sp, bold)
- Section statistiques avec 3 colonnes :
  - Posts
  - Abonnés
  - Abonnements
- Bouton "Modifier le profil" (couleur #5F9EA0)
- RecyclerView avec GridLayoutManager (3 colonnes) pour afficher les photos

### 2. Layout des items de la grille
**Fichier:** `app/src/main/res/layout/item_profile_photo.xml`
- CardView avec coins arrondis (4dp)
- ImageView en centerCrop
- Taille fixe de 120dp de hauteur
- Marges de 2dp entre les photos

### 3. Adaptateur RecyclerView
**Fichier:** `app/src/main/java/com/example/checknshare/ui/profile/PhotoGridAdapter.kt`
- Data class `Photo` avec id et imageResId
- Adaptateur pour gérer l'affichage de la grille de photos
- ViewHolder optimisé

### 4. Fragment de profil
**Fichier:** `app/src/main/java/com/example/checknshare/ui/profile/ProfileFragment.kt`
- Récupération des informations utilisateur depuis SharedPreferences
- Affichage du nom d'utilisateur connecté
- Génération de 12 photos de démonstration
- Configuration du RecyclerView avec GridLayoutManager (3 colonnes)
- Gestion du bouton "Modifier le profil" (avec Toast pour l'instant)

### 5. Ressources drawables
**Fichiers créés:**
- `photo_placeholder_1.xml` (couleur rose: #FFB6C1)
- `photo_placeholder_2.xml` (couleur bleu ciel: #87CEEB)
- `photo_placeholder_3.xml` (couleur vert clair: #98FB98)

## Fonctionnalités implémentées

✅ Photo de profil circulaire en haut
✅ Nom de l'utilisateur connecté (récupéré depuis SharedPreferences)
✅ Statistiques (Posts, Abonnés, Abonnements)
✅ Bouton "Modifier le profil"
✅ Grille scrollable de photos (3 colonnes)
✅ Photos de démonstration avec différentes couleurs

## Points à améliorer (TODO)

1. **Base de données:**
   - Récupérer le nombre réel de posts depuis la base de données
   - Récupérer le nombre d'abonnés et d'abonnements
   - Charger les vraies photos de l'utilisateur depuis la base de données

2. **Photo de profil:**
   - Permettre à l'utilisateur de changer sa photo de profil
   - Stocker et charger la photo de profil depuis la base de données ou le stockage local

3. **Modification du profil:**
   - Implémenter la fonctionnalité de modification du profil (nom, email, bio, etc.)

4. **Photos:**
   - Charger les vraies photos depuis le serveur/base de données
   - Ajouter un clic sur les photos pour les afficher en plein écran
   - Implémenter le chargement d'images avec une bibliothèque comme Glide ou Coil

5. **Abonnements:**
   - Implémenter la navigation vers la liste des abonnés/abonnements au clic sur les statistiques

## Utilisation

La page de profil est accessible via la navigation bottom bar de l'application. Elle affiche automatiquement les informations de l'utilisateur connecté récupérées depuis les SharedPreferences.

## Structure du code

```
ProfileFragment
├── loadUserInfo() - Charge les infos utilisateur depuis SharedPreferences
├── setupPhotoGrid() - Configure la grille de photos avec RecyclerView
└── onDestroyView() - Nettoie le binding

PhotoGridAdapter
├── Photo (data class) - Modèle de données pour une photo
├── PhotoViewHolder - ViewHolder pour les items
└── Méthodes du RecyclerView (onCreateViewHolder, onBindViewHolder, getItemCount)
```

## Design

La page respecte le design de la capture d'écran :
- Layout blanc propre
- Espacements cohérents (16dp, 24dp, 32dp)
- Couleur du bouton: #5F9EA0 (bleu-vert)
- Grille de 3 colonnes avec espacement minimal
- ScrollView pour permettre le défilement quand il y a beaucoup de photos

