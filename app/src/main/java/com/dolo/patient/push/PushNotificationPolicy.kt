package com.dolo.patient.push

import java.security.MessageDigest

object PushNotificationPolicy {
    const val EXTRA_DESTINATION = "com.dolo.patient.push.DESTINATION"
    private val appointmentRoute = Regex("^/appointments/([A-Za-z0-9_-]{1,80})$")

    fun destinationFor(rawRoute: String?): String? {
        val appointmentId = rawRoute
            ?.trim()
            ?.let { appointmentRoute.matchEntire(it) }
            ?.groupValues
            ?.getOrNull(1)
            ?: return null
        return "queue/$appointmentId"
    }

    fun tokenFingerprint(token: String): String {
        require(token.length in 16..4096) { "Invalid FCM registration token." }
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    fun notificationId(messageId: String?, destination: String?): Int {
        val stable = listOf(messageId.orEmpty(), destination.orEmpty()).joinToString("|")
        return stable.hashCode() and Int.MAX_VALUE
    }
}
