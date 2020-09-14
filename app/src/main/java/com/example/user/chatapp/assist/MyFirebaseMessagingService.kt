package com.example.user.chatapp.assist

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.user.chatapp.MainActivity
import com.example.user.chatapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    val CHANNEL_ID = "ChatApp"
    val NOTIFICATION_ID = 1

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        val cUser = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .getString("current_user","none")

        if (p0!!.data["sender_id"] != cUser)
            showNotification(p0.data["title"], p0.data["message"])
    }

    private fun showNotification(title: String?, message: String?) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_default_user_img)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_SOUND)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "ChatApp"
            val description = "Receiving messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}