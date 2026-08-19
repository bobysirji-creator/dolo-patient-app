package com.dolo.patient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.patient.data.HostedAppointment
import com.dolo.patient.data.HostedLiveQueue
import com.dolo.patient.data.HostedSyncUiState
import com.dolo.patient.ui.components.DoloBottomBar
import com.dolo.patient.ui.components.DoloCard
import com.dolo.patient.ui.components.PatientBottomDestination
import com.dolo.patient.ui.components.PrimaryButton
import com.dolo.patient.ui.components.ScreenTitle
import kotlinx.coroutines.delay

private val hostedTerminalStatuses = setOf("COMPLETED", "ABSENT", "RESCHEDULED", "EXPIRED")

@Composable
fun HostedAppointmentHistoryScreen(
    state: HostedSyncUiState,
    onBack: () -> Unit,
    onQueue: (String) -> Unit,
    onRefresh: () -> Unit,
    onHome: () -> Unit,
    onBook: () -> Unit
) {
    var filter by remember { mutableStateOf("UPCOMING") }
    val appointments = state.snapshot?.appointments.orEmpty()
    val visible = appointments.filter { appointment ->
        when (filter) {
            "UPCOMING" -> appointment.status !in hostedTerminalStatuses
            "PAST" -> appointment.status in hostedTerminalStatuses
            else -> true
        }
    }
    LaunchedEffect(Unit) { onRefresh() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DoloBottomBar(PatientBottomDestination.APPOINTMENTS, onHome, {}, onBook) }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenTitle("Appointments", onBack) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPCOMING" to "Upcoming", "PAST" to "Past", "ALL" to "All").forEach { option ->
                        FilterChip(
                            selected = filter == option.first,
                            onClick = { filter = option.first },
                            label = { Text(option.second) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            if (state.loading && state.snapshot == null) {
                item { CircularProgressIndicator() }
            }
            if (state.error) {
                item {
                    DoloCard(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.CloudOff, contentDescription = null)
                            Column(Modifier.weight(1f)) {
                                Text("Could not refresh appointments", fontWeight = FontWeight.Bold)
                                Text(state.message, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        TextButton(onClick = onRefresh, enabled = !state.loading) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Text("Retry")
                        }
                    }
                }
            }
            if (!state.loading && visible.isEmpty()) {
                item {
                    DoloCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Icon(Icons.Outlined.EventNote, contentDescription = null)
                        Text(
                            if (filter == "UPCOMING") "No upcoming appointments" else "No appointments in this section",
                            fontWeight = FontWeight.Bold
                        )
                        if (filter == "UPCOMING") PrimaryButton("Book an appointment", onBook)
                    }
                }
            } else {
                items(visible, key = { it.id }) { appointment ->
                    HostedAppointmentCard(appointment, onQueue)
                }
            }
        }
    }
}

@Composable
private fun HostedAppointmentCard(appointment: HostedAppointment, onQueue: (String) -> Unit) {
    DoloCard {
        Text(appointment.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            appointment.patientName + " • " + appointment.clinicName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            appointment.date + " • " + appointment.session.lowercase().replaceFirstChar(Char::uppercase),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Token " + appointment.token + " • " + appointment.status.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
        if (appointment.status !in hostedTerminalStatuses) {
            PrimaryButton("Track live queue", { onQueue(appointment.id) })
        }
    }
}

@Composable
fun HostedLiveQueueScreen(
    state: HostedSyncUiState,
    appointmentId: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val snapshot = state.snapshot
    val appointment = snapshot?.appointments?.firstOrNull { it.id == appointmentId }
    val live = snapshot?.live?.firstOrNull { it.appointmentId == appointmentId }
    LaunchedEffect(appointmentId) {
        while (true) {
            onRefresh()
            delay(15_000)
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Live Queue", onBack) }
        if (appointment == null) {
            item {
                DoloCard(containerColor = if (state.error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (state.error) "Unable to load appointment" else "Loading appointment", fontWeight = FontWeight.Bold)
                    Text(state.message)
                    PrimaryButton("Refresh", onRefresh, enabled = !state.loading)
                }
            }
        } else {
            item {
                DoloCard {
                    Text(appointment.doctorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(appointment.patientName + " • " + appointment.clinicName)
                    Text(appointment.date + " • " + appointment.session.lowercase().replaceFirstChar(Char::uppercase))
                }
            }
            item { HostedQueueStatusCard(appointment, live) }
            if (state.error) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(state.message)
                            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Retry") }
                        }
                    }
                }
            }
            item { PrimaryButton(if (state.loading) "Refreshing..." else "Refresh queue", onRefresh, enabled = !state.loading) }
        }
    }
}

@Composable
private fun HostedQueueStatusCard(appointment: HostedAppointment, live: HostedLiveQueue?) {
    DoloCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Text("Your token", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(appointment.token.toString(), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
        Text("Current token: " + (live?.currentToken?.toString() ?: "Not started"), fontWeight = FontWeight.Bold)
        Text("Patients ahead: " + (live?.patientsAhead?.toString() ?: "Pending"))
        Text("Estimated wait: " + (live?.estimatedMinutes?.let { it.toString() + " minutes" } ?: "Available after queue admission"))
        Text(
            when {
                live == null -> "Appointment booked. The live position appears after the clinic confirms the fee and admits the patient."
                else -> "Status: " + live.status.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
