package com.example.checknshare.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checknshare.databinding.FragmentNotificationsBinding
import com.example.checknshare.database.DatabaseHelper

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use AndroidViewModelFactory to get application context inside ViewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dbHelper = DatabaseHelper(requireContext())

        // Configurer le RecyclerView
        notificationsAdapter = NotificationsAdapter(emptyList()) { notificationId ->
            // Marquer comme lu et recharger
            dbHelper.markNotificationAsRead(notificationId)
            viewModel.loadNotifications()
        }
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationsAdapter
        }

        // Observer les notifications depuis la DB
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notificationsAdapter.updateNotifications(notifications)
        }

        // Exemple d'action : marquer la première notification comme lue au clic (ou implémenter dans adapter)
        // Ici on pourrait ajouter un listener dans NotificationsAdapter pour gérer click et appeler dbHelper.markNotificationAsRead

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}