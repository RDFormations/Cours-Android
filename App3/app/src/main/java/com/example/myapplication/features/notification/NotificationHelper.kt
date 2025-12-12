package com.example.myapplication.features.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_GENERAL = "general_channel"
        const val CHANNEL_ID_IMPORTANT = "important_channel"
        const val CHANNEL_ID_PROMO = "promo_channel"

        private var notificationId = 0
        fun getNextNotificationId() = notificationId++
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "Notifications générales",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications d'information générale"
                },
                NotificationChannel(
                    CHANNEL_ID_IMPORTANT,
                    "Notifications importantes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertes et notifications urgentes"
                },
                NotificationChannel(
                    CHANNEL_ID_PROMO,
                    "Promotions",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Offres et promotions"
                }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                when (channelId) {
                    CHANNEL_ID_IMPORTANT -> NotificationCompat.PRIORITY_HIGH
                    CHANNEL_ID_PROMO -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context).notify(
                getNextNotificationId(),
                builder.build()
            )
        }
    }

    fun sendBigTextNotification(
        title: String,
        shortMessage: String,
        longMessage: String,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(longMessage)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context).notify(
                getNextNotificationId(),
                builder.build()
            )
        }
    }

    fun sendProgressNotification(
        title: String,
        progress: Int,
        max: Int = 100
    ): Int {
        val notificationId = getNextNotificationId()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setProgress(max, progress, false)
            .setOngoing(progress < max)
            .setAutoCancel(progress >= max)

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context).notify(
                notificationId,
                builder.build()
            )
        }

        return notificationId
    }

    fun updateProgressNotification(
        notificationId: Int,
        title: String,
        progress: Int,
        max: Int = 100
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(title)
            .setContentText(if (progress >= max) "Terminé !" else "$progress%")
            .setProgress(max, progress, false)
            .setOngoing(progress < max)
            .setAutoCancel(progress >= max)

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context).notify(
                notificationId,
                builder.build()
            )
        }
    }

    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
