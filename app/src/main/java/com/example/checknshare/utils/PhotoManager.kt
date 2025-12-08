package com.example.checknshare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Classe utilitaire pour gérer le stockage et la récupération des photos
 */
class PhotoManager(private val context: Context) {

    companion object {
        private const val TAG = "PhotoManager"
        private const val PHOTOS_DIR = "photos"
    }

    /**
     * Obtenir le dossier où sont stockées les photos
     */
    private fun getPhotosDirectory(): File {
        val photosDir = File(context.filesDir, PHOTOS_DIR)
        if (!photosDir.exists()) {
            photosDir.mkdirs()
            Log.d(TAG, "Dossier photos créé: ${photosDir.absolutePath}")
        }
        return photosDir
    }

    /**
     * Sauvegarder une photo depuis un Uri (TakePicture)
     * @return Le nom du fichier sauvegardé, ou null si erreur
     */
    fun savePhoto(photoUri: Uri): String? {
        return try {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val photosDir = getPhotosDirectory()
            val photoFile = File(photosDir, fileName)

            // Ouvrir un InputStream depuis l'URI
            val inputStream = context.contentResolver.openInputStream(photoUri)
                ?: return null

            // Copier vers ton stockage interne
            inputStream.use { input ->
                FileOutputStream(photoFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Photo sauvegardée : $fileName (${photoFile.length() / 1024} KB)")
            fileName
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde de la photo", e)
            null
        }
    }


    /**
     * Charger une photo depuis son nom de fichier
     * @param fileName Nom du fichier (sans chemin)
     * @return Bitmap de l'image, ou null si introuvable
     */
    fun loadPhoto(fileName: String?): Bitmap? {
        if (fileName.isNullOrBlank()) {
            return null
        }

        try {
            val photosDir = getPhotosDirectory()
            val photoFile = File(photosDir, fileName)

            if (!photoFile.exists()) {
                Log.w(TAG, "Photo introuvable: $fileName")
                return null
            }

            return BitmapFactory.decodeFile(photoFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement de la photo: $fileName", e)
            return null
        }
    }
}

