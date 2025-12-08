package com.example.checknshare.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.checknshare.R

data class Photo(
    val id: Int,
    val imageResId: Int = R.drawable.photo_placeholder_1
)

class PhotoGridAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImage: ImageView = itemView.findViewById(R.id.photo_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.photoImage.setImageResource(photo.imageResId)
    }

    override fun getItemCount(): Int = photos.size
}

