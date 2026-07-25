package com.llama.redchat.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class NotificationReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val targetId = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_ID) ?: return
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)

        when (intent.action) {
            NotificationHelper.ACTION_REPLY -> {
                val remoteInputResults = RemoteInput.getResultsFromIntent(intent)
                val replyText = remoteInputResults?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString()

                if (!replyText.isNull_or_blank()) {
                    // Send reply back via real Mesh Engine broadcast
                    val isChannel = targetId.startsWith("chan_")
                    // Note: The app will update state when ChatRepository handles this action
                    val replyIntent = Intent("com.llama.redchat.NOTIFICATION_SEND_REPLY").apply {
                        putExtra("targetId", targetId)
                        putExtra("isChannel", isChannel)
                        putExtra("text", replyText)
                    }
                    context.sendBroadcast(replyIntent)
                }
                NotificationHelper.cancelNotification(context, notificationId)
            }
            NotificationHelper.ACTION_MARK_READ -> {
                NotificationHelper.cancelNotification(context, notificationId)
            }
        }
    }

    private fun CharSequence?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
