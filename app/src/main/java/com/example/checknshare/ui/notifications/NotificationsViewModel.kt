package com.example.checknshare.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.checknshare.database.DatabaseHelper

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application.applicationContext)

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        loadNotifications()
    }

    fun loadNotifications(recipientUserId: Int? = null) {
        val list = dbHelper.getAllNotifications(recipientUserId)
        _notifications.postValue(list)
    }
}