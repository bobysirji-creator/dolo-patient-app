package com.dolo.patient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.patient.data.HostedPatientSyncViewModel
import com.dolo.patient.ui.components.ScreenTitle
import com.dolo.patient.ui.theme.DoloSurfaceAlt
import com.dolo.patient.ui.theme.DoloTeal
import kotlinx.coroutines.delay

@Composable
fun HostedSyncScreen(onBack: () -> Unit, viewModel: HostedPatientSyncViewModel) {
    val state = viewModel.uiState
    LaunchedEffect(Unit) {
        viewModel.refresh()
        while (true) {
            delay(15_000)
            viewModel.refresh()
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Hosted Prototype Sync", onBack) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = if (state.error) MaterialTheme.colorScheme.errorContainer else DoloSurfaceAlt)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Stage 18B - hosted communication feed", fontWeight = FontWeight.Bold)
                    Text(state.message)
                    Text(
                        "Your local profile, family, favourites, reviews and existing appointments are not uploaded.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(viewModel::refresh, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                        if (state.loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Refresh server data")
                    }
                }
            }
        }
        state.snapshot?.let { snapshot ->
            item {
                Text(snapshot.bootstrap.clinic.doctorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${snapshot.bootstrap.clinic.specialty} - ${snapshot.bootstrap.clinic.name}, ${snapshot.bootstrap.clinic.city}")
                Text(
                    "Prototype patient: ${snapshot.bootstrap.profile.name} | Clinic fee paid separately at clinic",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item { Text("Updates for you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (snapshot.communications.isEmpty()) {
                item { Text("No active Doctor announcements or DO-LO broadcasts today.") }
            } else {
                items(snapshot.communications, key = { "communication-${it.id}" }) { communication ->
                    val adminBroadcast = communication.audience == "ALL_PATIENTS"
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (adminBroadcast) MaterialTheme.colorScheme.primaryContainer else DoloSurfaceAlt
                        )
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (adminBroadcast) "DO-LO broadcast" else communicationLabel(communication.kind),
                                style = MaterialTheme.typography.labelLarge,
                                color = DoloTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(communication.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(communication.message)
                            Text("Active ${communication.startsOn} to ${communication.endsOn}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { Text("Available server sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(snapshot.bootstrap.sessions, key = { it.id }) { session ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${session.date} - ${session.name}", fontWeight = FontWeight.Bold)
                        Text("${session.startsAt.take(5)} to ${session.endsAt.take(5)} | ${session.available} tokens available")
                        Button(
                            onClick = { viewModel.book(session.id, snapshot.bootstrap.profile.id) },
                            enabled = session.enabled && !state.loading,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (session.enabled) "Book authoritative token" else "Booking closed") }
                    }
                }
            }
            item { Text("Server appointment history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (snapshot.appointments.isEmpty()) {
                item { Text("No hosted prototype appointments yet.") }
            } else {
                items(snapshot.appointments, key = { it.id }) { appointment ->
                    val live = snapshot.live.firstOrNull { it.appointmentId == appointment.id }
                    Card(colors = CardDefaults.cardColors(containerColor = DoloSurfaceAlt)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Token ${appointment.token}", style = MaterialTheme.typography.headlineSmall, color = DoloTeal, fontWeight = FontWeight.ExtraBold)
                            Text("${appointment.patientName} - ${appointment.date} - ${appointment.session}")
                            Text("${appointment.doctorName} - ${appointment.clinicName}")
                            Text("Status: ${appointment.status}")
                            live?.let {
                                Text("Current token: ${it.currentToken?.toString() ?: "Not started"}")
                                Text("Patients ahead: ${it.patientsAhead?.toString() ?: "Not available"} | Estimated wait: ${it.estimatedMinutes?.let { minutes -> "$minutes min" } ?: "Not available"}")
                                Text("Countdown: ${it.countdownState}")
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun communicationLabel(kind: String): String = when (kind) {
    "DOCTOR_AVAILABILITY" -> "Doctor availability"
    "DOCTOR_CAMP" -> "Health camp"
    "DOCTOR_OFFER" -> "Doctor offer"
    else -> "Doctor announcement"
}
