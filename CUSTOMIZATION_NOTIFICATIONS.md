# 🎨 Personnalisation de la page Notifications

## Options de personnalisation

### 1. **Couleurs**

#### Modifier la couleur du titre
Dans `fragment_notifications.xml` :
```xml
<TextView
    android:id="@+id/titleNotifications"
    android:textColor="#000000"  ← Changez cette couleur
```

#### Modifier la couleur de fond
```xml
<LinearLayout
    android:background="@android:color/white"  ← Changez cette couleur
```

#### Modifier la couleur du texte des notifications
Dans `item_notification.xml` :
```xml
<TextView
    android:id="@+id/notificationText"
    android:textColor="@android:color/black"  ← Changez cette couleur
```

#### Modifier la couleur du lieu
```xml
<TextView
    android:id="@+id/locationText"
    android:textColor="#666666"  ← Changez cette couleur
```

### 2. **Tailles**

#### Taille de la photo de profil
Dans `item_notification.xml` :
```xml
<com.google.android.material.imageview.ShapeableImageView
    android:layout_width="48dp"   ← Changez la taille
    android:layout_height="48dp"  ← Gardez la même valeur pour un cercle
```

#### Taille du texte principal
```xml
<TextView
    android:id="@+id/notificationText"
    android:textSize="15sp"  ← Changez cette taille
```

#### Espacement entre les notifications
```xml
<androidx.cardview.widget.CardView
    android:layout_marginTop="8dp"     ← Espacement haut
    android:layout_marginBottom="8dp"  ← Espacement bas
```

### 3. **Ajouter une ombre aux notifications**

Dans `item_notification.xml`, modifier le CardView :
```xml
<androidx.cardview.widget.CardView
    app:cardElevation="4dp"  ← Changez de 0dp à 4dp pour une ombre
    app:cardCornerRadius="8dp"  ← Ajoutez des coins arrondis
```

### 4. **Marquer les notifications non lues**

#### Option A : Ajouter un indicateur visuel

Dans `Notification.kt`, ajoutez un champ :
```kotlin
data class Notification(
    val id: Int,
    val userName: String,
    val action: String,
    val location: String,
    val timeAgo: String,
    val profileImageUrl: String? = null,
    val isRead: Boolean = false  ← Nouveau champ
)
```

Dans `item_notification.xml`, ajoutez un point coloré :
```xml
<View
    android:id="@+id/unreadIndicator"
    android:layout_width="8dp"
    android:layout_height="8dp"
    android:background="@drawable/circle_background"
    android:visibility="gone"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintBottom_toBottomOf="parent" />
```

Dans `NotificationsAdapter.kt`, gérez la visibilité :
```kotlin
if (!notification.isRead) {
    holder.unreadIndicator.visibility = View.VISIBLE
} else {
    holder.unreadIndicator.visibility = View.GONE
}
```

#### Option B : Changer la couleur de fond

```kotlin
if (!notification.isRead) {
    holder.itemView.setBackgroundColor(Color.parseColor("#F0F0F0"))
} else {
    holder.itemView.setBackgroundColor(Color.WHITE)
}
```

### 5. **Ajouter des interactions**

#### Click sur une notification

Dans `NotificationsAdapter.kt`, ajoutez un listener :
```kotlin
class NotificationsAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit  ← Nouveau paramètre
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        // ...existing code...
        
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }
    }
}
```

Dans `NotificationsFragment.kt` :
```kotlin
notificationsAdapter = NotificationsAdapter(emptyList()) { notification ->
    // Action au click
    Toast.makeText(context, "Clicked on ${notification.userName}", Toast.LENGTH_SHORT).show()
}
```

### 6. **Charger des vraies images de profil**

#### Ajouter Glide dans `app/build.gradle.kts` :
```kotlin
dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

#### Dans `NotificationsAdapter.kt` :
```kotlin
import com.bumptech.glide.Glide

override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
    val notification = notifications[position]
    
    if (notification.profileImageUrl != null) {
        Glide.with(holder.itemView.context)
            .load(notification.profileImageUrl)
            .placeholder(R.drawable.ic_profile_default)
            .circleCrop()
            .into(holder.profileImage)
    } else {
        holder.profileImage.setImageResource(R.drawable.ic_profile_default)
    }
}
```

### 7. **Animations d'entrée**

Dans `NotificationsFragment.kt` :
```kotlin
binding.notificationsRecyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = notificationsAdapter
    
    // Animation
    layoutAnimation = AnimationUtils.loadLayoutAnimation(
        context,
        R.anim.layout_animation_fall_down
    )
}
```

Créez `res/anim/layout_animation_fall_down.xml` :
```xml
<?xml version="1.0" encoding="utf-8"?>
<layoutAnimation xmlns:android="http://schemas.android.com/apk/res/android"
    android:delay="15%"
    android:animationOrder="normal"
    android:animation="@anim/item_animation_fall_down" />
```

### 8. **Grouper les notifications par date**

Modifiez le ViewModel pour grouper :
```kotlin
sealed class NotificationItem {
    data class Header(val date: String) : NotificationItem()
    data class Item(val notification: Notification) : NotificationItem()
}
```

Cela nécessite un adaptateur plus complexe avec plusieurs types de vues.

## 💡 Conseils

- **Testez chaque modification** avant de passer à la suivante
- **Utilisez les couleurs de votre thème** pour une cohérence visuelle
- **Gardez les composants réutilisables** en évitant le code trop spécifique
- **Documentez vos modifications** pour faciliter la maintenance

## 🎯 Modifications recommandées

1. ✅ Ajouter le système de notifications lues/non lues
2. ✅ Implémenter le click sur les notifications
3. ✅ Charger les vraies images de profil avec Glide
4. ⚠️ Les animations sont optionnelles mais améliorent l'expérience

