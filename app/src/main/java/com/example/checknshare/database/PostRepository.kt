package com.example.checknshare.database

import android.content.Context
import android.util.Log
import com.example.checknshare.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les opérations CRUD sur les posts dans la base Supabase
 */
class PostRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    companion object {
        private const val TAG = "PostRepository"
    }

    /**
     * Créer un nouveau post
     * @param userId ID de l'utilisateur créateur
     * @param imageName Nom de l'image du post
     * @param location Localisation du post
     * @return ID du post créé, ou -1 en cas d'erreur
     */
    suspend fun createPost(userId: Int, imageName: String?, location: String?): Long = withContext(Dispatchers.IO) {
        try {
            val postId = dbHelper.createPost(userId, imageName, location)
            if (postId != -1L) {
                Log.d(TAG, "Post créé avec succès: ID=$postId")
            } else {
                Log.e(TAG, "Échec de la création du post")
            }
            postId
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création du post", e)
            -1L
        }
    }

    /**
     * Récupérer tous les posts (avec username et données complètes)
     * @return Liste de tous les posts, triés du plus récent au plus ancien
     */
    suspend fun getAllPosts(): List<Post> = withContext(Dispatchers.IO) {
        try {
            val posts = dbHelper.getAllPosts()
            Log.d(TAG, "Récupération de ${posts.size} posts")
            posts
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des posts", e)
            emptyList()
        }
    }

    /**
     * Récupérer un post par son ID
     * @param postId ID du post
     * @return Le post correspondant, ou null si non trouvé
     */
    suspend fun getPostById(postId: Int): Post? = withContext(Dispatchers.IO) {
        try {
            val posts = dbHelper.getAllPosts()
            val post = posts.find { it.id == postId }
            if (post != null) {
                Log.d(TAG, "Post trouvé: ID=$postId")
            } else {
                Log.d(TAG, "Post non trouvé: ID=$postId")
            }
            post
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du post ID=$postId", e)
            null
        }
    }

    /**
     * Récupérer tous les posts d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des posts de l'utilisateur
     */
    suspend fun getPostsByUserId(userId: Int): List<Post> = withContext(Dispatchers.IO) {
        try {
            val allPosts = dbHelper.getAllPosts()
            val userPosts = allPosts.filter { it.userId == userId }
            Log.d(TAG, "Récupération de ${userPosts.size} posts pour l'utilisateur ID=$userId")
            userPosts
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des posts de l'utilisateur ID=$userId", e)
            emptyList()
        }
    }

    /**
     * Supprimer un post par son ID
     * @param postId ID du post à supprimer
     * @return true si supprimé avec succès, false sinon
     */
    suspend fun deletePost(postId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val deleted = dbHelper.deletePost(postId)
            if (deleted > 0) {
                Log.d(TAG, "Post supprimé: ID=$postId")
                true
            } else {
                Log.e(TAG, "Échec de la suppression du post ID=$postId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression du post ID=$postId", e)
            false
        }
    }

    /**
     * Obtenir le nombre de likes d'un post
     * @param postId ID du post
     * @return Nombre de likes
     */
    suspend fun getLikeCount(postId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val count = dbHelper.getLikeCount(postId)
            Log.d(TAG, "Post ID=$postId a $count likes")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du nombre de likes pour le post ID=$postId", e)
            0
        }
    }

    /**
     * Ajouter un like à un post
     * @param postId ID du post
     * @param userId ID de l'utilisateur qui like
     * @return true si like ajouté, false si déjà liké ou erreur
     */
    suspend fun addLike(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (dbHelper.hasUserLiked(postId, userId)) {
                Log.d(TAG, "L'utilisateur ID=$userId a déjà liké le post ID=$postId")
                return@withContext false
            }
            val result = dbHelper.addLike(postId, userId)
            if (result != -1L) {
                Log.d(TAG, "Like ajouté: Post ID=$postId par User ID=$userId")
                true
            } else {
                Log.e(TAG, "Échec de l'ajout du like")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout du like", e)
            false
        }
    }

    /**
     * Retirer un like d'un post
     * @param postId ID du post
     * @param userId ID de l'utilisateur qui unlike
     * @return true si like retiré, false si pas liké ou erreur
     */
    suspend fun removeLike(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = dbHelper.removeLike(postId, userId)
            if (result > 0) {
                Log.d(TAG, "Like retiré: Post ID=$postId par User ID=$userId")
                true
            } else {
                Log.e(TAG, "Échec du retrait du like")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du retrait du like", e)
            false
        }
    }

    /**
     * Toggle like/unlike sur un post
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @return true si maintenant liké, false si maintenant pas liké
     */
    suspend fun toggleLike(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = dbHelper.toggleLike(postId, userId)
            Log.d(TAG, "Toggle like: Post ID=$postId par User ID=$userId, maintenant liké=$result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du toggle like", e)
            false
        }
    }

    /**
     * Vérifier si un utilisateur a liké un post
     * @param postId ID du post
     * @param userId ID de l'utilisateur
     * @return true si l'utilisateur a liké le post
     */
    suspend fun hasUserLiked(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            dbHelper.hasUserLiked(postId, userId)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification du like", e)
            false
        }
    }
}

