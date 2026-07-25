package com.llama.redchat.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.llama.redchat.notification.NotificationHelper

class RedMeshService : Service() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.initNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationHelper.buildPersistentServiceNotification(
            context = this,
            peerCount = intent?.getIntExtra("peerCount", 0) ?: 0,
            isBleActive = intent?.getBooleanExtra("isBleActive", true) ?: true,
            isTorActive = intent?.getBooleanExtra("isTorActive", false) ?: false
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NotificationHelper.MESH_SERVICE_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NotificationHelper.MESH_SERVICE_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

