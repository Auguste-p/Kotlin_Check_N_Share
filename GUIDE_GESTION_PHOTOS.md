# 📸 Guide complet : Gestion des photos dans l'application

## 🎯 Contexte actuel

Ton app utilise actuellement des **placeholders** (images statiques dans `res/drawable/`) avec des noms comme `photo_placeholder_1`, `photo_placeholder_2`, etc.

**Problème :** On ne peut pas ajouter de vraies photos prises par l'utilisateur, seulement des placeholders fixes.

**Solution :** Implémenter un système de gestion de photos avec plusieurs options.

---

## 📋 Table des matières

1. [Option 1 : Stockage interne (Recommandé pour commencer)](#option-1--stockage-interne)
2. [Option 2 : Utiliser la galerie existante (Plus simple)](#option-2--galerie-existante)
3. [Option 3 : Prendre une photo avec la caméra](#option-3--caméra)
4. [Option 4 : Upload vers un serveur distant](#option-4--serveur-distant)
5. [Implémentation complète recommandée](#implémentation-complète)

---

## 🏆 Option 1 : Stockage interne (Recommandé)

### Principe
- Les photos sont enregistrées dans le **stockage interne** de l'app (`/data/data/com.example.checknshare/files/photos/`)
- Chaque photo a un **nom unique** (UUID ou timestamp)
- Le chemin de la photo est stocké en base de données
- Les photos sont **privées** à l'app (sécurisé)

### Avantages
✅ Pas besoin de permissions complexes (Android 10+)  
✅ Photos privées et sécurisées  
✅ Contrôle total sur les fichiers  
✅ Facile à implémenter  

### Inconvénients
❌ Photos supprimées si on désinstalle l'app  
❌ Prend de l'espace sur le stockage interne  

### Structure de stockage
```
/data/data/com.example.checknshare/
  └── files/
      └── photos/
          ├── 1700123456789.jpg  (timestamp)
          ├── 1700123460123.jpg
          └── a1b2c3d4-uuid.jpg   (ou UUID)
```

### Code : PhotoManager.kt (Helper pour gérer les photos)

```kotlin
package com.example.checknshare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Classe utilitaire pour gérer le stockage et la récupération des photos
 */
class PhotoManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PhotoManager"
        private const val PHOTOS_DIR = "photos"
        private const val PHOTO_QUALITY = 85 // Qualité de compression JPEG (0-100)
    }

    /**
     * Obtenir le dossier où sont stockées les photos
     */
    private fun getPhotosDirectory(): File {
        val photosDir = File(context.filesDir, PHOTOS_DIR)
        if (!photosDir.exists()) {
            photosDir.mkdirs()
            Log.d(TAG, "Dossier photos créé: ${photosDir.absolutePath}")
        }
        return photosDir
    }

    /**
     * Sauvegarder une photo depuis un URI (galerie, caméra, etc.)
     * @param uri URI de l'image source
     * @return Nom du fichier sauvegardé, ou null en cas d'erreur
     */
    fun savePhotoFromUri(uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Log.e(TAG, "Impossible de décoder l'image depuis URI")
                return null
            }

            return savePhoto(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde depuis URI", e)
            return null
        }
    }

    /**
     * Sauvegarder un Bitmap dans le stockage interne
     * @param bitmap Image à sauvegarder
     * @return Nom du fichier (sans chemin complet), ou null en cas d'erreur
     */
    fun savePhoto(bitmap: Bitmap): String? {
        try {
            // Générer un nom unique basé sur le timestamp
            val fileName = "${System.currentTimeMillis()}.jpg"
            val photosDir = getPhotosDirectory()
            val photoFile = File(photosDir, fileName)

            // Compresser et sauvegarder
            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, out)
            }

            Log.d(TAG, "Photo sauvegardée: $fileName")
            return fileName
        } catch (e: IOException) {
            Log.e(TAG, "Erreur lors de la sauvegarde de la photo", e)
            return null
        }
    }

    /**
     * Charger une photo depuis son nom de fichier
     * @param fileName Nom du fichier (sans chemin)
     * @return Bitmap de l'image, ou null si introuvable
     */
    fun loadPhoto(fileName: String?): Bitmap? {
        if (fileName.isNullOrBlank()) {
            return null
        }

        try {
            val photosDir = getPhotosDirectory()
            val photoFile = File(photosDir, fileName)

            if (!photoFile.exists()) {
                Log.w(TAG, "Photo introuvable: $fileName")
                return null
            }

            return BitmapFactory.decodeFile(photoFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement de la photo: $fileName", e)
            return null
        }
    }

    /**
     * Supprimer une photo
     * @param fileName Nom du fichier à supprimer
     * @return true si supprimé avec succès
     */
    fun deletePhoto(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) {
            return false
        }

        try {
            val photosDir = getPhotosDirectory()
            val photoFile = File(photosDir, fileName)

            if (photoFile.exists()) {
                val deleted = photoFile.delete()
                if (deleted) {
                    Log.d(TAG, "Photo supprimée: $fileName")
                }
                return deleted
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la photo: $fileName", e)
            return false
        }
    }

    /**
     * Obtenir le chemin complet d'une photo
     * @param fileName Nom du fichier
     * @return Chemin complet, ou null si la photo n'existe pas
     */
    fun getPhotoPath(fileName: String?): String? {
        if (fileName.isNullOrBlank()) {
            return null
        }

        val photosDir = getPhotosDirectory()
        val photoFile = File(photosDir, fileName)
        return if (photoFile.exists()) photoFile.absolutePath else null
    }

    /**
     * Nettoyer les photos orphelines (non référencées en BDD)
     * @param validFileNames Liste des noms de fichiers valides (depuis la BDD)
     * @return Nombre de fichiers supprimés
     */
    fun cleanOrphanedPhotos(validFileNames: Set<String>): Int {
        val photosDir = getPhotosDirectory()
        val allFiles = photosDir.listFiles() ?: return 0
        var deletedCount = 0

        for (file in allFiles) {
            if (!validFileNames.contains(file.name)) {
                if (file.delete()) {
                    deletedCount++
                    Log.d(TAG, "Photo orpheline supprimée: ${file.name}")
                }
            }
        }

        return deletedCount
    }

    /**
     * Obtenir la taille totale des photos stockées
     * @return Taille en octets
     */
    fun getTotalPhotosSize(): Long {
        val photosDir = getPhotosDirectory()
        val allFiles = photosDir.listFiles() ?: return 0L
        return allFiles.sumOf { it.length() }
    }
}
```

---

## 📱 Option 2 : Galerie existante (Plus simple)

### Principe
L'utilisateur **sélectionne une photo existante** depuis sa galerie.

### Permissions nécessaires (AndroidManifest.xml)

```xml
<!-- Pour Android 12 et inférieur -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Pour Android 13+ (granularité photos/vidéos) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### Code : Sélection depuis la galerie

```kotlin
// Dans HomeFragment.kt

private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        // Sauvegarder l'image sélectionnée
        val photoManager = PhotoManager(requireContext())
        val fileName = photoManager.savePhotoFromUri(it)
        
        if (fileName != null) {
            selectedImageName = fileName
            Toast.makeText(context, "Photo sélectionnée !", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
        }
    }
}

// Dans showAddPostDialog(), remplacer le bouton "Prendre photo"
btnTakePhoto.setOnClickListener {
    // Lancer le sélecteur de photos
    pickImageLauncher.launch("image/*")
}
```

### Demander la permission (si nécessaire)

```kotlin
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        pickImageLauncher.launch("image/*")
    } else {
        Toast.makeText(context, "Permission refusée", Toast.LENGTH_SHORT).show()
    }
}

// Vérifier et demander la permission
private fun checkAndRequestPermission() {
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED -> {
            // Permission accordée
            pickImageLauncher.launch("image/*")
        }
        else -> {
            // Demander la permission
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }
}
```

---

## 📷 Option 3 : Caméra

### Permissions (AndroidManifest.xml)

```xml
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Code : Prendre une photo

```kotlin
private val takePictureLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicturePreview()
) { bitmap: Bitmap? ->
    bitmap?.let {
        val photoManager = PhotoManager(requireContext())
        val fileName = photoManager.savePhoto(it)
        
        if (fileName != null) {
            selectedImageName = fileName
            Toast.makeText(context, "Photo capturée !", Toast.LENGTH_SHORT).show()
        }
    }
}

// Lancer la caméra
btnTakePhoto.setOnClickListener {
    takePictureLauncher.launch(null)
}
```

---

## 🌐 Option 4 : Serveur distant (Avancé)

### Principe
- Photos uploadées sur un serveur (Firebase Storage, AWS S3, serveur personnel)
- URL de la photo stockée en BDD
- Chargement via URL (Glide, Coil, Picasso)

### Avantages
✅ Photos conservées même après désinstallation  
✅ Partage entre appareils  
✅ Backup automatique  

### Inconvénients
❌ Nécessite connexion internet  
❌ Coût de stockage serveur  
❌ Plus complexe à implémenter  

---

## 🎯 Implémentation complète recommandée

Je vais créer une solution complète qui combine :
1. **Stockage interne** (PhotoManager)
2. **Sélection galerie** OU **Caméra**
3. **Affichage optimisé** avec gestion du fallback

### Étape 1 : Créer PhotoManager.kt

(Voir code ci-dessus)

### Étape 2 : Créer un dossier utils/

```
app/src/main/java/com/example/checknshare/
  └── utils/
      ├── PhotoManager.kt      ← Nouveau
      └── ImageLoader.kt       ← Nouveau (optionnel)
```

### Étape 3 : Modifier PostAdapter pour charger les vraies photos

```kotlin
// Dans PostAdapter.kt, méthode bind()

fun bind(post: Post) {
    titleText.text = post.username
    subtitleText.text = post.location ?: ""
    bottomRightText.text = post.createdAt

    // Charger la photo (vraie ou placeholder)
    val photoManager = PhotoManager(context)
    post.imageName?.let { name ->
        // Essayer de charger comme photo réelle
        val bitmap = photoManager.loadPhoto(name)
        if (bitmap != null) {
            mainPhoto.setImageBitmap(bitmap)
        } else {
            // Fallback: essayer comme drawable resource
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) {
                mainPhoto.setImageResource(resId)
            } else {
                mainPhoto.setImageResource(R.drawable.photo_placeholder_1)
            }
        }
    } ?: mainPhoto.setImageResource(R.drawable.photo_placeholder_1)

    // ... reste du code (likes, etc.)
}
```

### Étape 4 : Modifier HomeFragment pour la sélection de photo

```kotlin
// Ajouter au début de la classe
private lateinit var photoManager: PhotoManager
private var selectedImageName: String? = null

// Launcher pour sélectionner depuis la galerie
private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        val fileName = photoManager.savePhotoFromUri(it)
        if (fileName != null) {
            selectedImageName = fileName
            Toast.makeText(context, "Photo sélectionnée !", Toast.LENGTH_SHORT).show()
            // Optionnel: afficher preview dans la dialog
        } else {
            Toast.makeText(context, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
        }
    }
}

// Launcher pour prendre une photo
private val takePictureLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicturePreview()
) { bitmap: Bitmap? ->
    bitmap?.let {
        val fileName = photoManager.savePhoto(it)
        if (fileName != null) {
            selectedImageName = fileName
            Toast.makeText(context, "Photo capturée !", Toast.LENGTH_SHORT).show()
        }
    }
}

// Dans onCreateView(), initialiser PhotoManager
override fun onCreateView(...): View {
    // ... code existant ...
    photoManager = PhotoManager(requireContext())
    // ... reste ...
}

// Dans showAddPostDialog()
private fun showAddPostDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_add_post, null)
    // ... setup views ...

    // Réinitialiser l'image sélectionnée
    selectedImageName = null

    btnTakePhoto.setOnClickListener {
        // Afficher un choix : Galerie ou Caméra
        showPhotoSourceDialog()
    }

    btnPublish.setOnClickListener {
        val uid = getCurrentUserId()
        if (uid == -1) {
            Toast.makeText(requireContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        // Vérifier qu'une photo est sélectionnée
        if (selectedImageName == null) {
            Toast.makeText(requireContext(), "Sélectionnez une photo", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        val locationText = textLocation.text.toString()
            .takeIf { it.isNotBlank() && it != getString(R.string.add_location) }

        val rowId = dbHelper.createPost(uid, selectedImageName, locationText)
        if (rowId != -1L) {
            Toast.makeText(requireContext(), "Post publié !", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            refreshPosts()
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la publication", Toast.LENGTH_SHORT).show()
        }
    }

    dialog.show()
}

// Nouvelle méthode: Dialogue pour choisir la source (Galerie ou Caméra)
private fun showPhotoSourceDialog() {
    val options = arrayOf("Galerie", "Caméra")
    AlertDialog.Builder(requireContext())
        .setTitle("Choisir une photo")
        .setItems(options) { _, which ->
            when (which) {
                0 -> pickImageLauncher.launch("image/*") // Galerie
                1 -> takePictureLauncher.launch(null)    // Caméra
            }
        }
        .show()
}
```

---

## 📦 Résumé des fichiers à créer/modifier

### Nouveaux fichiers à créer :
1. ✅ `app/src/main/java/com/example/checknshare/utils/PhotoManager.kt`

### Fichiers à modifier :
1. ✅ `app/src/main/java/com/example/checknshare/ui/home/HomeFragment.kt`
2. ✅ `app/src/main/java/com/example/checknshare/ui/home/PostAdapter.kt`
3. ✅ `app/src/main/AndroidManifest.xml` (ajouter permissions si nécessaire)

---

## 🧪 Tests à effectuer

### Test 1 : Sélectionner une photo depuis la galerie
1. Cliquer sur FAB (+)
2. Cliquer sur "Prendre photo"
3. Choisir "Galerie"
4. Sélectionner une photo
5. Ajouter une localisation
6. Publier
7. ✅ **Vérifier** : La photo apparaît dans le feed

### Test 2 : Prendre une photo avec la caméra
1. Cliquer sur FAB (+)
2. Cliquer sur "Prendre photo"
3. Choisir "Caméra"
4. Prendre une photo
5. Publier
6. ✅ **Vérifier** : La photo apparaît dans le feed

### Test 3 : Compatibilité avec les anciens posts (placeholders)
1. ✅ **Vérifier** : Les posts existants avec `photo_placeholder_1/2/3` s'affichent toujours

---

## 🎨 Améliorations optionnelles

### 1. Compression intelligente
```kotlin
// Redimensionner avant de sauvegarder
fun compressBitmap(bitmap: Bitmap, maxWidth: Int = 1080): Bitmap {
    if (bitmap.width <= maxWidth) return bitmap
    
    val ratio = maxWidth.toFloat() / bitmap.width
    val height = (bitmap.height * ratio).toInt()
    return Bitmap.createScaledBitmap(bitmap, maxWidth, height, true)
}
```

### 2. Preview dans la dialog
Afficher un aperçu de la photo sélectionnée dans `dialog_add_post.xml`

### 3. Gestion du cache avec Glide
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

// Dans PostAdapter
Glide.with(context)
    .load(photoFile)
    .placeholder(R.drawable.photo_placeholder_1)
    .into(mainPhoto)
```

---

## ⚠️ Points d'attention

1. **Taille des photos** : Compresser pour éviter OutOfMemoryError
2. **Nettoyage** : Supprimer les photos des posts supprimés
3. **Permissions** : Gérer les refus de permissions gracieusement
4. **Stockage** : Surveiller l'espace disque disponible
5. **Thread UI** : Charger les bitmaps sur un thread background pour de gros fichiers

---

## 🚀 Prochaines étapes

1. **Créer `PhotoManager.kt`** (je peux le faire pour toi)
2. **Modifier `HomeFragment.kt`** pour la sélection de photos
3. **Modifier `PostAdapter.kt`** pour l'affichage
4. **Ajouter permissions** dans `AndroidManifest.xml`
5. **Tester** !

---

Veux-tu que j'implémente tout ça directement dans ton projet ? 🚀

