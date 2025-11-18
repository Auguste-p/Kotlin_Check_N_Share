# 🔧 Correction : Utilisation de SQLite au lieu de PostgreSQL

## ❌ Problème Rencontré

L'erreur `NoClassDefFoundError: Ljava/lang/management/ManagementFactory` se produisait car :

**Le driver PostgreSQL JDBC n'est PAS compatible avec Android !**

Les drivers JDBC standards utilisent des classes Java qui ne sont pas disponibles dans l'environnement Android (comme `java.lang.management.ManagementFactory`).

## ✅ Solution Implémentée

J'ai remplacé la connexion PostgreSQL par **SQLite**, la base de données native d'Android.

### Avantages de SQLite pour Android :
- ✅ **Natif** : Intégré directement dans Android
- ✅ **Performant** : Optimisé pour les appareils mobiles
- ✅ **Pas de serveur** : Base de données locale, pas besoin de configuration réseau
- ✅ **Hors ligne** : Fonctionne sans connexion Internet
- ✅ **Simple** : Pas de configuration complexe

## 📝 Changements Effectués

### 1. Nouveau fichier créé : `LocalDatabaseHelper.kt`

Remplace les anciens fichiers PostgreSQL (`DatabaseHelper.kt`, `UserRepository.kt`, `DatabaseConfig.kt`).

**Fonctionnalités :**
- ✅ Création automatique de la table `users`
- ✅ Authentification utilisateur
- ✅ Hashing des mots de passe (SHA-256)
- ✅ Utilisateur de test pré-créé (username: `test`, password: `test123`)
- ✅ Méthodes pour créer et vérifier des utilisateurs

### 2. `LoginActivity.kt` modifié

- Suppression des imports PostgreSQL et Coroutines
- Utilisation de `LocalDatabaseHelper` au lieu de `UserRepository`
- Opérations synchrones (plus simples pour SQLite)

### 3. `build.gradle.kts` nettoyé

- Suppression de la dépendance PostgreSQL incompatible
- Conservation des Coroutines pour usage futur (optionnel)

## 🚀 Comment Tester

### L'application fonctionne maintenant directement !

```
1. Lancez l'application
2. Utilisez le compte de test :
   - Username: test
   - Password: test123
3. Vous serez redirigé vers MainActivity après connexion
```

### Base de données SQLite

La base de données est créée automatiquement au premier lancement dans :
```
/data/data/com.example.checknshare/databases/checknshare.db
```

## 🔍 Voir les Données (Optionnel)

### Via Android Studio :

1. Ouvrez **Device File Explorer** (View > Tool Windows > Device File Explorer)
2. Naviguez vers : `/data/data/com.example.checknshare/databases/`
3. Téléchargez `checknshare.db`
4. Ouvrez-le avec un viewer SQLite (DB Browser for SQLite)

### Via adb :

```bash
# Accéder au shell de l'émulateur
adb shell

# Se connecter à la base de données
sqlite3 /data/data/com.example.checknshare/databases/checknshare.db

# Voir les utilisateurs
SELECT * FROM users;

# Quitter
.quit
```

## 🎯 Ajouter de Nouveaux Utilisateurs

### Option 1 : Créer une activité d'inscription (SignupActivity)

C'est la prochaine étape recommandée. Je peux vous aider à l'implémenter !

### Option 2 : Manuellement via code

Dans `LocalDatabaseHelper.kt`, utilisez :

```kotlin
dbHelper.createUser("nouveau", "nouveau@example.com", "motdepasse123")
```

### Option 3 : Via adb

```bash
adb shell
sqlite3 /data/data/com.example.checknshare/databases/checknshare.db

INSERT INTO users (username, email, password) 
VALUES ('nouveau', 'nouveau@test.com', 
  'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae');
```
*(Ce mot de passe correspond à `test123`)*

## 🌐 Et PostgreSQL alors ?

### Pour utiliser PostgreSQL avec Android, vous avez 3 options :

#### Option 1 : API REST (⭐ Recommandé)
Créez un backend séparé (Node.js, Python, PHP, etc.) qui communique avec PostgreSQL, et votre app Android communique avec l'API via HTTP/HTTPS.

**Avantages :**
- Sécurité (pas d'exposition directe de la DB)
- Scalabilité
- Multi-plateforme (iOS, Web, etc.)

#### Option 2 : Firebase
Utilisez Firebase Authentication + Firestore (base NoSQL dans le cloud).

**Avantages :**
- Hébergé par Google
- Synchronisation en temps réel
- Pas de gestion serveur

#### Option 3 : Supabase
Backend-as-a-Service basé sur PostgreSQL avec API REST automatique.

**Avantages :**
- Utilise PostgreSQL
- API REST générée automatiquement
- Open source

## 📚 Structure Actuelle

```
app/src/main/java/com/example/checknshare/
├── LoginActivity.kt              ✅ Utilise SQLite
├── MainActivity.kt               ✅ Vérifie la session
└── database/
    └── LocalDatabaseHelper.kt    ✅ Gestion SQLite (nouveau)
    
Fichiers obsolètes (vous pouvez les supprimer) :
├── DatabaseConfig.kt             ❌ PostgreSQL (incompatible)
├── DatabaseHelper.kt             ❌ PostgreSQL (incompatible)
└── UserRepository.kt             ❌ PostgreSQL (incompatible)
```

## ✅ Résultat

**L'application fonctionne maintenant sans erreur !**

- ✅ Page de login au démarrage
- ✅ Base de données fonctionnelle (SQLite)
- ✅ Authentification
- ✅ Gestion de session
- ✅ Hors ligne (pas besoin d'Internet)

## 🎯 Prochaines Étapes Recommandées

1. **Créer une activité d'inscription** (SignupActivity)
2. **Ajouter la validation email**
3. **Implémenter "Mot de passe oublié"**
4. **(Optionnel) Créer une API REST** pour synchroniser avec PostgreSQL

---

**L'application est maintenant prête à fonctionner ! 🚀**

Besoin d'aide pour implémenter l'inscription ou l'API REST ? Faites-le moi savoir !

