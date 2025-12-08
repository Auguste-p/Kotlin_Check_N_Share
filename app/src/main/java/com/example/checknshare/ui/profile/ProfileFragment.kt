package com.example.checknshare.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checknshare.R
import com.example.checknshare.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoGridAdapter: PhotoGridAdapter
    private val photosList = mutableListOf<Photo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Récupérer les informations de l'utilisateur depuis SharedPreferences
        loadUserInfo()

        // Configurer la grille de photos
        setupPhotoGrid()

        // Configurer le bouton modifier le profil
        val btnEditProfile = root.findViewById<Button>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            Toast.makeText(context, "Modification du profil à implémenter", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun loadUserInfo() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Utilisateur") ?: "Utilisateur"

        // Afficher le nom d'utilisateur
        val profileUsername = binding.root.findViewById<TextView>(R.id.profile_username)
        profileUsername.text = username

        // Pour l'instant, utiliser des valeurs fixes pour les statistiques
        // TODO: Récupérer les vraies valeurs depuis la base de données
        val postsCount = binding.root.findViewById<TextView>(R.id.posts_count)
        val followersCount = binding.root.findViewById<TextView>(R.id.followers_count)
        val followingCount = binding.root.findViewById<TextView>(R.id.following_count)

        // Compter le nombre de photos (sera mis à jour avec les vraies données)
        postsCount.text = photosList.size.toString()
        followersCount.text = "0"
        followingCount.text = "0"
    }

    private fun setupPhotoGrid() {
        // Générer des photos de démonstration avec différentes couleurs
        // TODO: Remplacer par de vraies photos depuis la base de données
        val placeholders = listOf(
            R.drawable.photo_placeholder_1,
            R.drawable.photo_placeholder_2,
            R.drawable.photo_placeholder_3
        )

        for (i in 1..21) {
            val imageResId = placeholders[(i - 1) % placeholders.size]
            photosList.add(Photo(i, imageResId))
        }

        // Configurer le RecyclerView
        val photosGrid = binding.root.findViewById<RecyclerView>(R.id.photos_grid)
        photoGridAdapter = PhotoGridAdapter(photosList)
        photosGrid.adapter = photoGridAdapter
        photosGrid.layoutManager = GridLayoutManager(context, 3)

        // Mettre à jour le compteur de posts
        val postsCount = binding.root.findViewById<TextView>(R.id.posts_count)
        postsCount.text = photosList.size.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}