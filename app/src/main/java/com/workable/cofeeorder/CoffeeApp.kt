package com.workable.cofeeorder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import com.workable.cofeeorder.notifications.NotificationRegistry


class CoffeeApp : Application() {

  override fun onCreate() {
    super.onCreate()

    initializeNotificationChannels(this)
  }

  private fun initializeNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < 26) {
      return
    }

    val notificationManager = context.getSystemService(
        Context.NOTIFICATION_SERVICE) as NotificationManager?

    fun createNotificationChannel(channelId: String, @StringRes channelNameResId: Int) {
      val notificationChannel = NotificationChannel(channelId,
          context.getString(channelNameResId),
          NotificationManager.IMPORTANCE_DEFAULT).apply {
        enableLights(true)
        lightColor = ContextCompat.getColor(context, R.color.colorAccent)
      }

      notificationManager?.createNotificationChannel(notificationChannel)
    }

    createNotificationChannel(NotificationRegistry.Channel.ORDER_STATUS_CHANGE_CHANNEL,
        R.string.order_status_change_channel)
  }

}
