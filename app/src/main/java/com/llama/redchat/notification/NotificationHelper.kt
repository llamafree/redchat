package com.llama.redchat.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_MESSAGES_ID = "redchat_private_messages"
    const val CHANNEL_CHANNELS_ID = "redchat_public_channels"
    const val CHANNEL_MESH_SERVICE_ID = "redchat_mesh_channel"
    const val CHANNEL_SECURITY_ID = "redchat_security_alerts"

    const val KEY_TEXT_REPLY = "key_text_reply"
    const val ACTION_REPLY = "com.llama.redchat.ACTION_REPLY"
    const val ACTION_MARK_READ = "com.llama.redchat.ACTION_MARK_READ"

    const val EXTRA_TARGET_ID = "extra_target_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    const val MESH_SERVICE_NOTIFICATION_ID = 1001

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // 1. Private Messages Channel (High Priority)
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                "Mensajes Privados",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones instantáneas para chats directos cifrados"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 80, 150)
                setSound(defaultSoundUri, audioAttributes)
            }

            // 2. Public Channels (Default Priority)
            val channelsChannel = NotificationChannel(
                CHANNEL_CHANNELS_ID,
                "Canales Públicos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de mensajes en canales comunitarios"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            // 3. Persistent Mesh Foreground Service Channel
            val meshServiceChannel = NotificationChannel(
                CHANNEL_MESH_SERVICE_ID,
                "Estado de REDChat Mesh",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Estado persistente del motor Bluetooth Mesh y ruteo P2P"
                setShowBadge(false)
            }

            // 4. Security Alerts
            val securityChannel = NotificationChannel(
                CHANNEL_SECURITY_ID,
                "Seguridad y Claves",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de intercambio de claves y verificación de firmas"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(channelsChannel)
            notificationManager.createNotificationChannel(meshServiceChannel)
            notificationManager.createNotificationChannel(securityChannel)
        }
    }

    fun buildPersistentServiceNotification(
        context: Context,
        peerCount: Int = 0,
        isBleActive: Boolean = true,
        isTorActive: Boolean = false
    ): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = buildString {
            append(if (isBleActive) "Bluetooth Mesh: Activo" else "Bluetooth Mesh: Inactivo")
            append(" | Peers: $peerCount")
            if (isTorActive) append(" | Tor Onion: Conectado")
        }

        return NotificationCompat.Builder(context, CHANNEL_MESH_SERVICE_ID)
            .setContentTitle("REDChat Mesh Network")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun showIncomingMessageNotification(
        context: Context,
        targetId: String,
        senderName: String,
        messageText: String,
        isChannel: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = targetId.hashCode()

        // Content intent to open conversation directly
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_TARGET_ID, targetId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick Reply
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Escribe una respuesta...")
            .build()

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_TARGET_ID, targetId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Responder",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Action: Mark as Read
        val markReadIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_TARGET_ID, targetId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10000,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Marcar como leído",
            markReadPendingIntent
        ).build()

        val channelId = if (isChannel) CHANNEL_CHANNELS_ID else CHANNEL_MESSAGES_ID

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(replyAction)
            .addAction(markReadAction)
            .setVibrate(longArrayOf(0, 150, 80, 150))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
