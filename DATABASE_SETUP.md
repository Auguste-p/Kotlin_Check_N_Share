# Configuration de la Base de Données PostgreSQL

## Prérequis

1. **Installer PostgreSQL** sur votre machine locale
   - macOS: `brew install postgresql@15`
   - Démarrer PostgreSQL: `brew services start postgresql@15`

## Étapes de Configuration

### 1. Créer la base de données

Exécutez le script SQL fourni :

```bash
psql postgres < database_setup.sql
```

Ou manuellement :

```bash
# Se connecter à PostgreSQL
psql postgres

# Créer la base de données
CREATE DATABASE checknshare_db;

# Se connecter à la nouvelle base
\c checknshare_db

# Créer la table users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# Créer un utilisateur de test
# Mot de passe: test123 (hashé en SHA-256)
INSERT INTO users (username, email, password) 
VALUES (
    'test', 
    'test@example.com', 
    'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae'
);
```

### 2. Configurer les identifiants de connexion

Modifiez le fichier `DatabaseConfig.kt` avec vos paramètres PostgreSQL :

```kotlin
const val DB_HOST = "10.0.2.2" // Pour émulateur Android
const val DB_PORT = "5432"
const val DB_NAME = "checknshare_db"
const val DB_USER = "postgres"
const val DB_PASSWORD = "votre_mot_de_passe"
```

**Note importante :**
- `10.0.2.2` est l'adresse pour accéder à localhost depuis l'émulateur Android
- Si vous testez sur un appareil physique, utilisez l'adresse IP de votre ordinateur sur le réseau local

### 3. Tester la connexion

Utilisateur de test créé :
- **Username:** `test`
- **Password:** `test123`

## Vérification de la Configuration

### Vérifier que PostgreSQL fonctionne :

```bash
psql -U postgres -d checknshare_db -c "SELECT * FROM users;"
```

### Créer un nouvel utilisateur manuellement :

```sql
INSERT INTO users (username, email, password) 
VALUES (
    'nouveau_user', 
    'user@example.com', 
    'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae'
);
```

## Sécurité

⚠️ **Important pour la production :**

1. Ne jamais stocker les mots de passe en clair
2. Utiliser un algorithme de hashing plus robuste (bcrypt, Argon2)
3. Utiliser HTTPS/SSL pour les connexions
4. Ne jamais exposer les identifiants dans le code
5. Utiliser des variables d'environnement ou un fichier de configuration sécurisé

## Dépannage

### Erreur de connexion :

1. Vérifier que PostgreSQL est démarré :
   ```bash
   brew services list
   ```

2. Vérifier les logs PostgreSQL :
   ```bash
   tail -f /usr/local/var/log/postgresql@15.log
   ```

3. Tester la connexion locale :
   ```bash
   psql -U postgres -d checknshare_db
   ```

### Problème avec l'émulateur Android :

- Assurez-vous d'utiliser `10.0.2.2` et non `localhost`
- Vérifiez que les permissions Internet sont dans AndroidManifest.xml
- Vérifiez `android:usesCleartextTraffic="true"` dans AndroidManifest.xml

## Structure de l'Application

### Fichiers créés :

1. **database/DatabaseConfig.kt** - Configuration de la connexion
2. **database/DatabaseHelper.kt** - Gestion de la connexion et création des tables
3. **database/UserRepository.kt** - Opérations CRUD sur les utilisateurs
4. **LoginActivity.kt** - Activité de connexion
5. **activity_login.xml** - Interface de connexion

### Flux de l'application :

1. **Lancement** → LoginActivity (première page)
2. **Connexion réussie** → MainActivity (avec session sauvegardée)
3. **Déconnexion** → Retour à LoginActivity

## Commandes Utiles

```bash
# Démarrer PostgreSQL
brew services start postgresql@15

# Arrêter PostgreSQL
brew services stop postgresql@15

# Se connecter à la base
psql -U postgres -d checknshare_db

# Lister les tables
\dt

# Voir la structure d'une table
\d users

# Quitter psql
\q
```

