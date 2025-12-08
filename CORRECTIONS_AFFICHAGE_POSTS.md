# Corrections pour l'affichage des posts

## Problème initial
Les posts ne s'affichaient pas à l'écran malgré le code en place.

## Cause principale
Le layout `item_photo_card.xml` avait une **hauteur de 0dp** pour la CardView, ce qui rendait tous les posts invisibles.

## Corrections apportées

### 1. **item_photo_card.xml**
- ✅ Changé `android:layout_height="0dp"` → `android:layout_height="wrap_content"` pour la CardView
- ✅ Ajouté des marges et des coins arrondis à la CardView (8dp, radius 12dp)
- ✅ Changé la hauteur de l'ImageView principale de `0dp` → `400dp` (hauteur fixe)
- ✅ Retiré la contrainte `app:layout_constraintBottom_toTopOf="@id/bottom_bar"` de l'image
- ✅ Changé le positionnement du `bottom_bar` de `constraintBottom_toBottomOf="parent"` → `constraintTop_toBottomOf="@id/main_photo"`
- ✅ Remplacé `android:tint` par `app:tint` pour l'icône de like (correction erreur de compilation)

### 2. **PostAdapter.kt**
- ✅ Inversé l'ordre de chargement des images : vérifie d'abord si c'est un drawable resource avant d'essayer de charger comme photo réelle
- ✅ Élimine les messages d'erreur "Photo introuvable" pour les placeholders

### 3. **HomeFragment.kt**
- ✅ Création de 5 posts factices avec la fonction `getFakePosts()`
- ✅ Ajout d'une map `fakeLikes` pour gérer les likes en mémoire
- ✅ Code BDD conservé en commentaire avec des TODO pour faciliter le retour

## Données de test

Les 5 posts affichés :
1. Marie Dupont - Paris, France (15 likes)
2. Jean Martin - Lyon, France (8 likes)
3. Sophie Bernard - Marseille, France (23 likes)
4. Marie Dupont - Nice, France (5 likes)
5. Pierre Durand - Toulouse, France (12 likes)

Chaque post utilise un placeholder coloré (photo_placeholder_1, 2, ou 3).

## Résultat attendu

L'écran home affiche maintenant :
- ✅ 5 cartes de posts visibles avec images colorées
- ✅ Noms d'utilisateurs et localisations
- ✅ Dates de création
- ✅ Compteurs de likes fonctionnels (cliquables)
- ✅ Design propre avec coins arrondis et espacement

## Pour passer aux données réelles plus tard

Décommenter dans `HomeFragment.kt` :
```kotlin
// Dans loadPosts()
val posts = dbHelper.getAllPosts()

// Dans l'adapter
getLikeCount = { postId -> dbHelper.getLikeCount(postId) }
```

Et supprimer/commenter les lignes utilisant `getFakePosts()` et `fakeLikes`.

