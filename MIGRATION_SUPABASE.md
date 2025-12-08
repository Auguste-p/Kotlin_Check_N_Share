# 🚀 Migration vers Supabase - Guide Complet

## ✅ Étapes Complétées

### 1. Sécurisation des Clés API
- ✅ Ajout des clés Supabase dans `local.properties` (fichier non versionné)
- ✅ Configuration de BuildConfig pour exposer les clés de manière sécurisée
- ✅ Mise à jour de `DatabaseConfig.kt` pour utiliser BuildConfig

### 2. Dépendances
- ✅ Ajout du plugin `kotlinx-serialization`
- ✅ Ajout des dépendances Supabase :
  - `supabase:bom:2.6.0`
  - `postgrest-kt` (pour les requêtes)
  - `realtime-kt` (pour le temps réel)
  - `storage-kt` (pour le stockage)
  - `ktor-client-android` (client HTTP)
- ✅ Ajout de `kotlinx-serialization-json`

### 3. Nouveau Helper Supabase
- ✅ Création de `SupabaseDatabaseHelper.kt` avec toutes les opérations async
- ✅ Création de `SupabaseDatabaseHelperWrapper.kt` (wrapper synchrone)
- ✅ Définition de la classe `DatabaseHelper` comme alias du wrapper

### 4. Migration des Fichiers
- ✅ `LoginActivity.kt` - migré vers DatabaseHelper
- ✅ `SignupActivity.kt` - migré vers DatabaseHelper
- ✅ `HomeFragment.kt` - migré vers DatabaseHelper
- ✅ `NotificationsFragment.kt` - migré vers DatabaseHelper
- ✅ `NotificationsViewModel.kt` - migré vers DatabaseHelper
- ✅ `PostRepository.kt` - migré vers DatabaseHelper

## 📋 Étapes à Suivre

### 1. Créer les Tables dans Supabase

1. Connectez-vous à votre compte Supabase : https://app.supabase.com
2. Sélectionnez votre projet
3. Allez dans **SQL Editor** (dans le menu de gauche)
4. Copiez et exécutez le contenu du fichier `SUPABASE_SETUP.sql`
5. Vérifiez que toutes les tables ont été créées dans **Table Editor**

### 2. Configurer les Permissions (RLS - Row Level Security)

Par défaut, Supabase active RLS. Pour le développement, vous pouvez le désactiver temporairement :

```sql
-- Désactiver RLS pour le développement (ATTENTION: À NE PAS FAIRE EN PRODUCTION!)
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE posts DISABLE ROW LEVEL SECURITY;
ALTER TABLE likes DISABLE ROW LEVEL SECURITY;
ALTER TABLE notifications DISABLE ROW LEVEL SECURITY;
```

**OU** créer des policies pour autoriser les opérations :

```sql
-- Autoriser tout le monde à lire les users (pour l'authentification)
CREATE POLICY "Allow public read access" ON users FOR SELECT USING (true);

-- Autoriser tout le monde à insérer des users (pour l'inscription)
CREATE POLICY "Allow public insert" ON users FOR INSERT WITH CHECK (true);

-- Autoriser tout le monde à tout faire sur posts (pour le développement)
CREATE POLICY "Allow all on posts" ON posts FOR ALL USING (true);

-- Autoriser tout le monde à tout faire sur likes
CREATE POLICY "Allow all on likes" ON likes FOR ALL USING (true);

-- Autoriser tout le monde à tout faire sur notifications
CREATE POLICY "Allow all on notifications" ON notifications FOR ALL USING (true);
```

### 3. Synchroniser Gradle

```bash
./gradlew clean
./gradlew build
```

OU dans Android Studio : **File > Sync Project with Gradle Files**

### 4. Tester l'Application

1. Lancez l'application
2. Testez la connexion avec :
   - **Email:** `test@example.com`
   - **Mot de passe:** `test123`
3. Vérifiez que :
   - ✅ L'authentification fonctionne
   - ✅ Les posts s'affichent (vides au début)
   - ✅ La création de posts fonctionne
   - ✅ Les notifications s'affichent
   - ✅ Les likes fonctionnent

## 🔧 Structure de la Base de Données

### Tables

1. **users**
   - `id` (BIGSERIAL PRIMARY KEY)
   - `username` (VARCHAR UNIQUE)
   - `email` (VARCHAR UNIQUE)
   - `password` (VARCHAR - SHA-256 hash)
   - `created_at` (TIMESTAMP)

2. **posts**
   - `id` (BIGSERIAL PRIMARY KEY)
   - `userId` (INTEGER FK → users.id)
   - `imageName` (VARCHAR)
   - `location` (VARCHAR)
   - `location_address` (VARCHAR)
   - `created_at` (TIMESTAMP)

3. **likes**
   - `id` (BIGSERIAL PRIMARY KEY)
   - `post_id` (INTEGER FK → posts.id)
   - `user_id` (INTEGER FK → users.id)
   - Contrainte UNIQUE sur (post_id, user_id)

4. **notifications**
   - `id` (BIGSERIAL PRIMARY KEY)
   - `recipient_user_id` (INTEGER FK → users.id)
   - `actor_name` (VARCHAR)
   - `action_text` (VARCHAR)
   - `location` (VARCHAR)
   - `time_ago` (VARCHAR)
   - `profile_image` (VARCHAR)
   - `is_read` (INTEGER DEFAULT 0)
   - `created_at` (TIMESTAMP)

## 📱 Fichiers Modifiés

### Configuration
- `local.properties` - Ajout des clés Supabase
- `app/build.gradle.kts` - Ajout des dépendances et BuildConfig

### Database
- `DatabaseConfig.kt` - Utilise BuildConfig
- `SupabaseDatabaseHelper.kt` - ✨ NOUVEAU - Helper Supabase async
- `SupabaseDatabaseHelperWrapper.kt` - ✨ NOUVEAU - Wrapper synchrone
- `PostRepository.kt` - Utilise DatabaseHelper

### Activities & Fragments
- `LoginActivity.kt` - Utilise DatabaseHelper
- `SignupActivity.kt` - Utilise DatabaseHelper
- `HomeFragment.kt` - Utilise DatabaseHelper
- `NotificationsFragment.kt` - Utilise DatabaseHelper
- `NotificationsViewModel.kt` - Utilise DatabaseHelper

## ⚠️ Notes Importantes

### Ancien Code (LocalDatabaseHelper)
L'ancien `LocalDatabaseHelper.kt` est toujours présent mais **n'est plus utilisé**. 
Vous pouvez le garder comme référence ou le supprimer.

### Performances
Le wrapper `runBlocking` est utilisé pour la compatibilité avec l'ancien code synchrone.
Pour de meilleures performances, envisagez de rendre toutes les opérations async/await.

### Sécurité
- ✅ Les clés API sont dans `local.properties` (non versionnées)
- ⚠️ Le mot de passe est hashé en SHA-256 (basique)
- 💡 Envisagez d'utiliser Supabase Auth pour une meilleure sécurité

### Hors Ligne
- ⚠️ L'application nécessite maintenant une connexion Internet
- 💡 Envisagez d'ajouter une couche de cache locale pour le mode hors ligne

## 🎯 Prochaines Étapes Recommandées

1. **Implémenter le Storage Supabase** pour les images des posts
2. **Utiliser Supabase Auth** au lieu de l'authentification manuelle
3. **Ajouter Realtime** pour les notifications en temps réel
4. **Optimiser les requêtes** avec des index et des vues
5. **Ajouter un cache local** pour le mode hors ligne

## 🐛 Dépannage

### Erreur de connexion à Supabase
- Vérifiez que `SUPABASE_URL` et `SUPABASE_API_KEY` sont corrects dans `local.properties`
- Vérifiez que votre projet Supabase est actif
- Vérifiez que les tables ont été créées

### Erreur "Cannot resolve symbol 'BuildConfig'"
- Synchronisez Gradle : **File > Sync Project with Gradle Files**
- Nettoyez le projet : **Build > Clean Project**
- Rebuild : **Build > Rebuild Project**

### Erreur "Row Level Security"
- Désactivez RLS ou créez des policies (voir section 2 ci-dessus)

### L'authentification ne fonctionne pas
- Vérifiez que l'utilisateur test a été créé avec le bon hash de mot de passe
- Vérifiez les logs Android (Logcat) pour voir les erreurs Supabase

## 📚 Ressources

- [Documentation Supabase](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)

---

**L'application est maintenant configurée pour utiliser Supabase ! 🎉**

