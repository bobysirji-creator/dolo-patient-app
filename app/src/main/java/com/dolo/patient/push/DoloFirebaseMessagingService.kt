package com.dolo.patient.push

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
import com.dolo.patient.MainActivity
import com.dolo.patient.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class DoloFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        runCatching { PushRegistrationState(applicationContext).record(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        DoloPushNotifications.createChannel(applicationContext)
        val destination = PushNotificationPolicy.destinationFor(message.data["route"])
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            destination?.let { putExtra(PushNotificationPolicy.EXTRA_DESTINATION, it) }
        }
        val requestCode = PushNotificationPolicy.notificationId(message.messageId, destination)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, getString(R.string.push_channel_appointments_id))
            .setSmallIcon(R.drawable.ic_dolo_notification)
            .setColor(getColor(R.color.dolo_notification_color))
            .setContentTitle(getString(R.string.push_notification_title))
            .setContentText(getString(R.string.push_notification_body))
            .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.push_notification_body)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (permitted) {
            NotificationManagerCompat.from(this).notify(requestCode, notification)
        }
    }
}

class PushRegistrationState(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun record(token: String) {
        preferences.edit()
            .putString(KEY_FINGERPRINT, PushNotificationPolicy.tokenFingerprint(token))
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    fun hasFirebaseRegistration(): Boolean =
        !preferences.getString(KEY_FINGERPRINT, null).isNullOrBlank()

    fun installationId():String {
        val existing=preferences.getString(KEY_INSTALLATION_ID,null)
        if(existing!=null&&existing.matches(Regex("^android-[0-9a-f-]{36}$"))) return existing
        return "android-${UUID.randomUUID()}".also { preferences.edit().putString(KEY_INSTALLATION_ID,it).apply() }
    }

    private companion object {
        const val PREFERENCES = "dolo_push_registration"
        const val KEY_FINGERPRINT = "fcm_token_sha256"
        const val KEY_UPDATED_AT = "fcm_token_updated_at"
        const val KEY_INSTALLATION_ID = "installation_id"
    }
}

object DoloPushNotifications {
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            context.getString(R.string.push_channel_appointments_id),
            context.getString(R.string.push_channel_appointments_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.push_channel_appointments_description)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
