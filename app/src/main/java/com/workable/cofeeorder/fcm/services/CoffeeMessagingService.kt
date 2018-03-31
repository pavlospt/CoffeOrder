package com.workable.cofeeorder.fcm.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class CoffeeMessagingService : FirebaseMessagingService() {

  private val TAG = "CoffeeMessagingService"

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)

    Log.d(TAG, "Message received")
  }
}
