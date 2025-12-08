package com.example.checknshare.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checknshare.R
import com.example.checknshare.databinding.FragmentHomeBinding
import com.example.checknshare.database.DatabaseHelper
import com.example.checknshare.utils.PhotoManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var photoManager: PhotoManager
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var photoUri: Uri
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userPosition: String = ""
    private var userPositionAddress: String = ""

    // Variable pour stocker le nom de l'image sélectionnée dans la dialog
    private var selectedImageName: String? = null

    // Variable pour stocker la référence à l'ImageView de prévisualisation
    private var currentPreviewImage: android.widget.ImageView? = null
    private var currentPhotoButton: View? = null

    // Callback pour mettre à jour l'UI quand la localisation est reçue
    private var onLocationReceived: ((String) -> Unit)? = null

    // Launcher demande de position
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Vérifier si la permission est vraiment accordée avant d'appeler
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getUserLocation()
            }
        } else {
            Toast.makeText(context, "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
        }
    }

    // Convertir latitude/longitude en adresse lisible
    private fun getAddressModern(lat: Double, lon: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        // API 33+ → méthode asynchrone recommandée
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    callback("${addr.locality}, ${addr.countryName}")
                } else {
                    callback(null)
                }
            }
        }
        // API < 33 → méthode classique (bloquante mais encore supportée)
        else {
            try {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    callback("${addr.locality}, ${addr.countryName}")
                } else {
                    callback(null)
                }
            } catch (_: Exception) {
                callback(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude

                    // Convertir en adresse moderne
                    getAddressModern(lat, lon) { address ->
                        if (address != null) {
                            userPosition = lat.toString() + lon.toString()
                            userPositionAddress = address
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "📍 Position : $address", Toast.LENGTH_LONG).show()
                                // Appeler le callback pour mettre à jour l'UI
                                onLocationReceived?.invoke(address)
                            }
                        } else {
                            val fallbackAddress = "$lat, $lon"
                            userPositionAddress = fallbackAddress
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "📍 Position : $fallbackAddress", Toast.LENGTH_LONG).show()
                                // Appeler le callback même avec les coordonnées brutes
                                onLocationReceived?.invoke(fallbackAddress)
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Impossible de récupérer la localisation",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Erreur de localisation",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Launcher pour prendre une photo avec la caméra
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La photo est déjà écrite dans photoUri
            val fileName = photoManager.savePhoto(photoUri)
            selectedImageName = fileName

            // Afficher la prévisualisation de la photo
            currentPreviewImage?.let { imageView ->
                imageView.setImageURI(photoUri)
                imageView.visibility = View.VISIBLE
            }
            // Cacher le bouton "Prendre une photo"
            currentPhotoButton?.visibility = View.GONE

            Toast.makeText(context, "Photo capturée ! 📷", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Échec de la capture", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Init DB helper et PhotoManager
        dbHelper = DatabaseHelper(requireContext())
        photoManager = PhotoManager(requireContext())

        // Demande de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // RecyclerView setup
        recycler = root.findViewById(R.id.photo_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostAdapter(
            context = requireContext(),
            getLikeCount = { postId ->
                dbHelper.getLikeCount(postId)
            },
            onLikeToggle = { postId ->
                 val uid = getCurrentUserId()
                 val userId = if (uid != -1) uid else dbHelper.getAnyUserId()
                 if (userId == -1) {
                     0
                 } else {
                     dbHelper.toggleLike(postId, userId)
                     dbHelper.getLikeCount(postId)
                 }
            }
        )
        recycler.adapter = adapter

        // Load initial posts
        loadPosts()

        // Configuration du FloatingActionButton
        binding.fabAddPost.setOnClickListener {
            showAddPostDialog()
        }

        return root
    }

    // Récupérer l'userId courant depuis SharedPreferences (ou fallback)
    private fun getCurrentUserId(): Int {
        val prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val id = prefs.getInt("userId", -1)
        return if (id != -1) id else dbHelper.getAnyUserId()
    }

    // Charger les posts
    private fun loadPosts() {
        val posts = dbHelper.getAllPosts()

        // Créer une nouvelle liste pour forcer DiffUtil à détecter les changements
        adapter.submitList(posts.toList())
        Log.d("HOME", "Posts chargés: ${posts.size}")
    }

    // Rafraîchir les posts et scroller vers le haut pour voir le nouveau post
    private fun refreshPosts() {
        loadPosts()
        // Scroller vers le haut pour voir le post le plus récent
        recycler.smoothScrollToPosition(0)
    }

    // Afficher la boîte de dialogue pour ajouter un post
    private fun showAddPostDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_post, null)

        val btnClose = dialogView.findViewById<View>(R.id.btn_close)
        val btnTakePhoto = dialogView.findViewById<View>(R.id.btn_take_photo)
        val layoutPhotoButton = dialogView.findViewById<View>(R.id.layout_photo_button)
        val imagePreview = dialogView.findViewById<android.widget.ImageView>(R.id.image_preview)
        val layoutLocation = dialogView.findViewById<View>(R.id.layout_location)
        val textLocation = dialogView.findViewById<TextView>(R.id.text_location)
        val btnPublish = dialogView.findViewById<View>(R.id.btn_publish)

        // Stocker les références pour les utiliser dans le launcher
        currentPreviewImage = imagePreview
        currentPhotoButton = layoutPhotoButton

        // Réinitialiser l'état
        selectedImageName = null
        imagePreview.visibility = View.GONE
        layoutPhotoButton.visibility = View.VISIBLE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Rendre le fond de la boîte de dialogue transparent pour voir les coins arrondis
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Au clic sur la croix pour quitter
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Fonction pour déclencher la prise de photo
        val takePhotoAction = {
            val photoFile = File.createTempFile("photo_", ".jpg", context?.cacheDir)
            context?.let { it1 ->
                photoUri = FileProvider.getUriForFile(
                    it1,
                    "${context?.packageName}.provider",
                    photoFile
                )
            }
            takePictureLauncher.launch(photoUri)
            selectedImageName = photoFile.name
        }

        // Au clic sur "Prendre une photo"
        btnTakePhoto.setOnClickListener {
            takePhotoAction()
        }

        // Au clic sur la prévisualisation, reprendre une photo
        imagePreview.setOnClickListener {
            takePhotoAction()
        }

        // Au clic sur le bouton de localisation
        layoutLocation.setOnClickListener {
            // Configurer le callback pour mettre à jour l'UI quand l'adresse est reçue
            onLocationReceived = { address ->
                textLocation.text = address
                Log.d("HOME", "Adresse user: $address")
            }
            // On récupère la position de l'utilisateur
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Au clic sur "Publier"
        btnPublish.setOnClickListener {
            val uid = getCurrentUserId()
            if (uid == -1) {
                Toast.makeText(requireContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val locationText = textLocation.text.toString().takeIf { it.isNotBlank() && it != getString(R.string.add_location) }

            val rowId = dbHelper.createPost(uid, selectedImageName, locationText)
            if (rowId != -1L) {
                // Affichage du toast de confirmation
                Toast.makeText(requireContext(), "Post publié !", Toast.LENGTH_SHORT).show()
                // Fermeture de la modale
                dialog.dismiss()
                // Rafraîchissement de la liste et scroll vers le haut
                refreshPosts()
            } else {
                Toast.makeText(requireContext(), "Erreur lors de la publication", Toast.LENGTH_SHORT).show()
            }
        }

        // Nettoyer les références quand le dialogue est fermé
        dialog.setOnDismissListener {
            currentPreviewImage = null
            currentPhotoButton = null
            selectedImageName = null
            userPositionAddress = ""
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}