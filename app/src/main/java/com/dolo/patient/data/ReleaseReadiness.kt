package com.dolo.patient.data

object ReleaseReadiness {
    const val QUEUE_REFRESH_INTERVAL_MILLIS = 15_000L
    const val QUEUE_STALE_AFTER_MILLIS = 60_000L
    const val MAX_PROFILE_FIELD_LENGTH = 80

    fun readableStatus(status: String): String =
        status
            .trim()
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar(Char::uppercase)

    fun isQueueStale(
        refreshedAt: Long,
        now: Long = System.currentTimeMillis()
    ): Boolean =
        refreshedAt <= 0L ||
            now < refreshedAt ||
            now - refreshedAt > QUEUE_STALE_AFTER_MILLIS

    fun safeSingleLine(
        value: String,
        maxLength: Int = MAX_PROFILE_FIELD_LENGTH
    ): String =
        value
            .replace('|', '/')
            .replace(Regex("[\r\n\t]+"), " ")
            .trim()
            .take(maxLength.coerceAtLeast(0))
}
