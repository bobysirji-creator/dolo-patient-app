package com.dolo.patient.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dolo.patient.R
import com.dolo.patient.data.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PatientHomeViewModel : ViewModel() {
    private var dismissedBroadcastIds by mutableStateOf(emptySet<String>())

    fun dismissBroadcast(id: String) { dismissedBroadcastIds = dismissedBroadcastIds + id }

    fun buildUiState(patientState: PatientUiState, hostedState: HostedSyncUiState?, nowMillis: Long): PatientHomeUiState {
        val localQueues = patientState.appointments
            .filter { it.status in listOf(AppointmentStatus.BOOKED, AppointmentStatus.WAITING, AppointmentStatus.IN_CONSULTATION) }
            .map { it.toQueueSummary(patientState.queues[it.id], nowMillis) }
        val hostedQueues = hostedState?.snapshot?.let { snapshot ->
            HostedHomePresentation.activeAppointments(snapshot).map {
                it.toQueueSummary(HostedHomePresentation.liveQueue(snapshot, it.id))
            }
        }.orEmpty()
        val broadcasts = hostedState?.snapshot?.let(HostedHomePresentation::homeCommunications)
            ?.map(HostedCommunication::toBroadcast)?.takeIf { it.isNotEmpty() }
            ?: listOf(defaultBroadcast)
        val favorites = DummyData.doctors.filter { it.id in patientState.favouriteIds }.mapIndexed { index, doctor ->
            FavoriteDoctorUiModel(doctor.id, doctor.name, doctor.specialty, doctor.rating, 98 + index * 22, 2.3 + index * 0.5, doctorPlaceholder(index))
        }
        return PatientHomeUiState(
            patientName = hostedState?.snapshot?.bootstrap?.profile?.name?.takeIf { it.isNotBlank() } ?: patientState.profile.name.ifBlank { "Patient" },
            patientCity = patientState.profile.city,
            patientGender = patientState.profile.gender,
            queues = (hostedQueues + localQueues).sortedBy { it.patientToken.toIntOrNull() ?: Int.MAX_VALUE },
            broadcasts = broadcasts.filterNot { it.id in dismissedBroadcastIds },
            favoriteDoctors = favorites,
            notificationCount = patientState.notifications.count { !it.isRead } + (hostedState?.snapshot?.notifications?.count { !it.read } ?: 0),
            isLoading = hostedState?.loading == true && hostedState?.snapshot == null,
            broadcastLoading = hostedState?.loading == true && hostedState?.snapshot == null,
            errorMessage = hostedState?.takeIf { it.error }?.message
        )
    }

    private fun doctorPlaceholder(index: Int): Int = when (index % 3) {
        0 -> R.drawable.category_general
        1 -> R.drawable.category_skin
        else -> R.drawable.category_child
    }

    companion object {
        val defaultBroadcast = BroadcastUiModel(
            id = "local-cardiology-opening",
            title = "New Cardiology Clinic Now Open!",
            message = "Our advanced Cardiology clinic is now open on 2nd Floor. Book your appointment today.",
            buttonText = "Know More",
            actionType = BroadcastActionType.CATEGORY,
            actionValue = "Cardiologist"
        )
    }
}

private val bookingTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

private fun Appointment.toQueueSummary(queue: QueueSnapshot?, nowMillis: Long): QueueSummaryUiModel {
    val bookingTime = id.toLongOrNull()?.let { timestamp ->
        runCatching { Instant.ofEpochMilli(timestamp.coerceAtMost(nowMillis)).atZone(ZoneId.systemDefault()).format(bookingTimeFormatter) }.getOrNull()
    } ?: "Scheduled"
    return QueueSummaryUiModel(
        appointmentId = id,
        patientName = patientName,
        patientToken = token.toString(),
        currentToken = queue?.currentToken?.toString() ?: "--",
        estimatedWaitMinutes = queue?.estimatedMinutes?.let { it..(it + 5) },
        bookingTime = "Booked at $bookingTime",
        doctorName = doctorName,
        clinicName = clinic,
        session = session.name.lowercase().replaceFirstChar(Char::uppercase),
        queueStatus = when (status) {
            AppointmentStatus.IN_CONSULTATION -> QueueStatus.ACTIVE
            AppointmentStatus.COMPLETED -> QueueStatus.COMPLETED
            else -> if (queue == null) QueueStatus.UPCOMING else QueueStatus.ACTIVE
        }
    )
}

private fun HostedAppointment.toQueueSummary(live: HostedLiveQueue?) = QueueSummaryUiModel(
    appointmentId = id,
    patientName = patientName,
    patientToken = token.toString(),
    currentToken = live?.currentToken?.toString() ?: "--",
    estimatedWaitMinutes = live?.estimatedMinutes?.let { it..(it + 5) },
    bookingTime = "$date - ${session.lowercase().replaceFirstChar(Char::uppercase)}",
    doctorName = doctorName,
    clinicName = clinicName,
    session = session.lowercase().replaceFirstChar(Char::uppercase),
    queueStatus = when {
        live?.countdownState?.contains("PAUSED", true) == true -> QueueStatus.PAUSED
        status == "COMPLETED" -> QueueStatus.COMPLETED
        live == null -> QueueStatus.UPCOMING
        else -> QueueStatus.ACTIVE
    },
    isHosted = true
)

private fun HostedCommunication.toBroadcast() = BroadcastUiModel(
    id = id,
    title = title,
    message = message,
    buttonText = "Know More",
    actionType = BroadcastActionType.INFORMATION,
    actionValue = id
)