package com.workable.cofeeorder.fcm.services

import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.workable.cofeeorder.R
import com.workable.cofeeorder.notifications.NotificationRegistry


class CoffeeMessagingService : FirebaseMessagingService() {

  private val TAG = "CoffeeMessagingService"

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)

    Log.d(TAG, "Message received")

    remoteMessage ?: return

    val notificationAction = remoteMessage.data[NotificationRegistry.Data.ACTION_KEY]

    notificationAction ?: return

    val orderOpened = notificationAction == NotificationRegistry.Action.ORDER_OPENED

    if (orderOpened) {
      showOrderOpenedNotification()
    }
  }

  private fun showOrderOpenedNotification() {
    val notification = NotificationCompat.Builder(applicationContext,
        NotificationRegistry.Channel.ORDER_STATUS_CHANGE_CHANNEL)
        .setSmallIcon(R.drawable.ic_free_breakfast_white_24dp)
        .setContentTitle("It is coffee time!")
        .setContentText("Open the app to order your favorite coffee!")
        .build()

    NotificationManagerCompat.from(applicationContext).notify(
        NotificationRegistry.Id.ORDER_OPENED_ID, notification)
  }
}
