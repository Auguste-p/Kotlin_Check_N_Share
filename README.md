## **Nom du projet : “Check’n'Share”**

### **Concept général**

Une application mobile permettant aux utilisateurs de **partager une photo accompagnée de leur position géographique** quand ils arrivent à un lieu particulier (ex : un restaurant, un point touristique, un événement).
Les autres utilisateurs abonnés reçoivent une **notification push** indiquant :

> “Marie vient de publier une photo depuis le Vieux-Port de Marseille 📍”

---

## **Fonctionnalités principales**

### 1. **Caméra**

* L’utilisateur prend une photo directement depuis l’application.
* La photo est compressée côté client et envoyée au serveur.

### 2. **Géolocalisation**

* Lors de la publication, l’app récupère automatiquement la latitude/longitude via le GPS.
* Le serveur convertit cette position en adresse (géocodage inverse).

### 3. **Serveur distant sécurisé**

* Stocke les publications de manière sécurisée (photo, texte, position, utilisateur, date).
* Gère l’API pour :
  * Créer une publication.
  * Récupérer les publications proches de l’utilisateur.
  * Envoyer des notifications aux abonnés.

### 4. **Notifications push**

* Quand un utilisateur que tu suis publie un nouveau post, tu reçois une notification :
  *« Alex vient de partager une photo près de toi ! »*

---

## **Exemple de scénario utilisateur**

1. L’utilisateur ouvre l’app.
2. Il prend une photo d’un lieu qu’il visite.
3. L’application envoie :
   * la photo,
   * sa position GPS,
   * un message facultatif.
4. Le serveur enregistre la publication et envoie une notification push à ses abonnés.
5. Les abonnés peuvent ouvrir la notification et voir la photo + la localisation sur une carte.

---

## **Architecture simplifiée**

* **Front (mobile)** :
  Flutter / React Native

  * Accès caméra
  * Accès GPS
  * Gestion des notifications (Firebase Cloud Messaging)
  * Appel d’API REST

* **Back-end** :
  Node.js / Express ou Python Flask

  * API REST pour les publications
  * Stockage images (S3 ou serveur local simple)
  * Base de données : PostgreSQL ou MongoDB
  * Gestion des notifications push via Firebase

* **Base de données (modèle simplifié)**

  ```
  Users(id, name, email, photo_url)
  Posts(id, user_id, image_url, latitude, longitude, address, timestamp)
  Followers(follower_id, followed_id)
  ```

---

## **Avantages du projet**

Utilise les 4 modules demandés :

* Caméra
* Géolocalisation
* Serveur distant
* Notifications push
  Peut évoluer facilement vers un mini réseau social géolocalisé.
