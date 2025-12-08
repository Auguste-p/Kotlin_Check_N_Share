package com.example.checknshare.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.checknshare.R
import com.example.checknshare.models.Post
import com.example.checknshare.utils.PhotoManager

class PostAdapter(
    private val context: Context,
    private val getLikeCount: (postId: Int) -> Int,
    private val onLikeToggle: (postId: Int) -> Int
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val subtitleText: TextView = itemView.findViewById(R.id.subtitle_text)
        private val mainPhoto: ImageView = itemView.findViewById(R.id.main_photo)
        private val bottomRightText: TextView = itemView.findViewById(R.id.bottom_right_text)
        private val likeIcon: ImageView = itemView.findViewById(R.id.like_icon)
        private val likeCount: TextView = itemView.findViewById(R.id.like_count)

        fun bind(post: Post) {
            titleText.text = post.username
            subtitleText.text = post.location ?: ""
            bottomRightText.text = post.createdAt

            // Charger la photo (vraie photo ou placeholder drawable)
            post.imageName?.let { imageName ->
                // D'abord, essayer de charger comme drawable resource (pour les placeholders)
                val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                if (resId != 0) {
                    // C'est un drawable resource
                    mainPhoto.setImageResource(resId)
                } else {
                    // Sinon, essayer de charger comme vraie photo stockée
                    val photoManager = PhotoManager(context)
                    val bitmap = photoManager.loadPhoto(imageName)
                    if (bitmap != null) {
                        mainPhoto.setImageBitmap(bitmap)
                    } else {
                        // Fallback: placeholder par défaut
                        mainPhoto.setImageResource(R.drawable.photo_placeholder_1)
                    }
                }
            } ?: run {
                // Si pas d'image name, utiliser placeholder
                mainPhoto.setImageResource(R.drawable.photo_placeholder_1)
            }

            // Initialiser le nombre de likes et l'icône
            val currentLikeCount = getLikeCount(post.id)
            likeCount.text = currentLikeCount.toString()
            if (currentLikeCount > 0) {
                likeIcon.setImageResource(R.drawable.ic_heart)
            } else {
                likeIcon.setImageResource(R.drawable.ic_heart_outline)
            }

            // Gérer le clic sur le like
            likeIcon.setOnClickListener {
                val newCount = onLikeToggle(post.id)
                likeCount.text = newCount.toString()
                if (newCount > 0) {
                    likeIcon.setImageResource(R.drawable.ic_heart)
                } else {
                    likeIcon.setImageResource(R.drawable.ic_heart_outline)
                }
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}

