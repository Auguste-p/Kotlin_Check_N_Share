package com.example.checknshare.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.checknshare.R
import com.google.android.material.imageview.ShapeableImageView

class NotificationsAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: ((notificationId: Int) -> Unit)? = null
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val notificationText: TextView = itemView.findViewById(R.id.notificationText)
        val locationText: TextView = itemView.findViewById(R.id.locationText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Construire le texte de la notification
        val fullText = "${notification.userName} ${notification.action}"
        holder.notificationText.text = fullText

        holder.locationText.text = notification.location
        holder.timeText.text = notification.timeAgo

        // TODO: Charger l'image de profil avec Glide ou Picasso si URL fournie
        if (notification.profileImageUrl != null) {
            // Charger l'image depuis l'URL
        } else {
            // Image par défaut
            holder.profileImage.setImageResource(R.drawable.ic_profile_default)
        }

        holder.itemView.setOnClickListener {
            onNotificationClick?.invoke(notification.id)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
