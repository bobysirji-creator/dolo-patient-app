package com.dolo.patient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.patient.data.HostedPatientSyncViewModel
import com.dolo.patient.data.HostedReschedulePolicy
import com.dolo.patient.data.HostedReceiptPresentation
import com.dolo.patient.data.HostedPreferences
import com.dolo.patient.ui.components.ScreenTitle
import com.dolo.patient.ui.theme.DoloSurfaceAlt
import com.dolo.patient.ui.theme.DoloTeal
import kotlinx.coroutines.delay

@Composable
fun HostedSyncScreen(onBack: () -> Unit, viewModel: HostedPatientSyncViewModel) {
    val state = viewModel.uiState
    var selectedProfileId by rememberSaveable { mutableStateOf<String?>(null) }
    var rescheduleAppointmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var rescheduleSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.snapshot?.bootstrap?.profiles) {
        val profiles = state.snapshot?.bootstrap?.profiles.orEmpty()
        if (profiles.none { it.id == selectedProfileId }) selectedProfileId = profiles.firstOrNull()?.id
    }
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
                    Text("Stages 31A-31B - communication preferences", fontWeight = FontWeight.Bold)
                    Text(state.message)
                    Text(
                        "Your local profile, family, favourites and local appointments are not uploaded. These communication choices belong only to the hosted dummy Patient.",
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
                    "Seeded dummy household | Clinic fee paid separately at clinic",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            snapshot.preferences?.let { preferences ->
                item { PatientCommunicationPreferenceCard(preferences, state.loading, viewModel::updatePreferences) }
            }
            val patientCommunications = snapshot.communications.filter { it.audience == "ALL_PATIENTS" }
            item { Text("Updates for you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (patientCommunications.isEmpty()) {
                item { Text("No active DO-LO broadcast today. Doctor announcements are shown only on that Doctor's profile.") }
            } else {
                items(patientCommunications, key = { "communication-${it.id}" }) { communication ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "DO-LO broadcast",
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
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Book for", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    snapshot.bootstrap.profiles.forEach { profile ->
                        FilterChip(
                            selected = selectedProfileId == profile.id,
                            onClick = { selectedProfileId = profile.id },
                            label = { Text("${profile.name} (${profile.relationship.lowercase()})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text("These are fixed dummy server profiles. Your real local family list is not uploaded.", style = MaterialTheme.typography.bodySmall)
                }
            }
            item { Text("Available server sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(snapshot.bootstrap.sessions, key = { it.id }) { session ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${session.date} - ${session.name}", fontWeight = FontWeight.Bold)
                        Text("${session.startsAt.take(5)} to ${session.endsAt.take(5)} | ${session.available} tokens available")
                        Button(
                            onClick = { selectedProfileId?.let { profileId -> viewModel.book(session.id, profileId) } },
                            enabled = session.enabled && selectedProfileId != null && !state.loading,
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
                            Text(HostedReceiptPresentation.text(appointment), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            if (appointment.rescheduledFromAppointmentId != null) {
                                Text("One-time replacement appointment", style = MaterialTheme.typography.bodySmall)
                            }
                            live?.let {
                                Text("Current token: ${it.currentToken?.toString() ?: "Not started"}")
                                Text("Patients ahead: ${it.patientsAhead?.toString() ?: "Not available"} | Estimated wait: ${it.estimatedMinutes?.let { minutes -> "$minutes min" } ?: "Not available"}")
                                Text("Countdown: ${it.countdownState}")
                            }
                            if (appointment.status == "COMPLETED") {
                                val hostedReview = snapshot.reviews.firstOrNull { it.appointmentId == appointment.id }
                                if (hostedReview != null) {
                                    Text("Your rating: ${hostedReview.rating}/5", fontWeight = FontWeight.Bold)
                                    if (hostedReview.comment.isNotBlank()) Text(hostedReview.comment)
                                    Text(
                                        if (hostedReview.status == "PENDING") "Submitted - pending Admin moderation" else "Moderation: ${hostedReview.status}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DoloTeal
                                    )
                                } else {
                                    HostedReviewEditor(appointment.id, state.loading) { rating, comment ->
                                        viewModel.submitReview(appointment.id, rating, comment)
                                    }
                                }
                            }
                            val targets = HostedReschedulePolicy.eligibleSessions(appointment, snapshot.bootstrap.rescheduleSessions, snapshot.bootstrap.rescheduleWindowDays)
                            if (appointment.status == "ABSENT" && !appointment.rescheduleUsed) {
                                Button(
                                    onClick = {
                                        rescheduleAppointmentId = if (rescheduleAppointmentId == appointment.id) null else appointment.id
                                        rescheduleSessionId = null
                                    },
                                    enabled = !state.loading,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (rescheduleAppointmentId == appointment.id) "Cancel reschedule" else "Reschedule missed appointment") }
                                if (rescheduleAppointmentId == appointment.id) {
                                    if (targets.isEmpty()) {
                                        Text("No eligible replacement session is currently available.", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text("Choose one available replacement session:", fontWeight = FontWeight.SemiBold)
                                        targets.forEach { target ->
                                            FilterChip(
                                                selected = rescheduleSessionId == target.id,
                                                onClick = { rescheduleSessionId = target.id },
                                                label = { Text("${target.date} - ${target.name}") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                rescheduleSessionId?.let { targetId -> viewModel.reschedule(appointment.id, targetId) }
                                                rescheduleAppointmentId = null
                                                rescheduleSessionId = null
                                            },
                                            enabled = rescheduleSessionId != null && !state.loading,
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Confirm one-time reschedule") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun PatientCommunicationPreferenceCard(current:HostedPreferences,loading:Boolean,onSave:(HostedPreferences)->Unit){
    var appointmentUpdates by rememberSaveable(current.consentedAt){mutableStateOf(current.appointmentServiceUpdates)}
    var healthInformation by rememberSaveable(current.consentedAt){mutableStateOf(current.healthInformation)}
    var promotions by rememberSaveable(current.consentedAt){mutableStateOf(current.promotionalMessages)}
    var inApp by rememberSaveable(current.consentedAt){mutableStateOf(current.inAppMessages)}
    var language by rememberSaveable(current.consentedAt){mutableStateOf(current.preferredLanguage)}
    Card(colors=CardDefaults.cardColors(containerColor=DoloSurfaceAlt)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text("Communication preferences",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        PreferenceSwitch("Appointment and service updates",appointmentUpdates){appointmentUpdates=it}
        PreferenceSwitch("Health information",healthInformation){healthInformation=it}
        Text("Future health grouping uses only specialties of Doctors previously consulted. No diagnosis or disease is inferred or stored.",style=MaterialTheme.typography.bodySmall)
        PreferenceSwitch("DO-LO promotional messages",promotions){promotions=it}
        PreferenceSwitch("In-app messages",inApp){inApp=it}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(language=="en",{language="en"},{Text("English")});FilterChip(language=="hi",{language="hi"},{Text("Hindi")})}
        Text("SMS is reserved only for OTP authentication and is never used for promotions.",style=MaterialTheme.typography.bodySmall,fontWeight=FontWeight.SemiBold)
        Button({onSave(current.copy(appointmentServiceUpdates=appointmentUpdates,healthInformation=healthInformation,promotionalMessages=promotions,inAppMessages=inApp,preferredLanguage=language))},enabled=!loading,modifier=Modifier.fillMaxWidth()){Text("Save preferences")}
    }}
}
@Composable
private fun PreferenceSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun HostedReviewEditor(appointmentId: String, loading: Boolean, onSubmit: (Int, String) -> Unit) {
    var rating by rememberSaveable(appointmentId) { mutableStateOf(5) }
    var comment by rememberSaveable(appointmentId) { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Rate this completed consultation", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (1..5).forEach { value ->
                FilterChip(selected = rating == value, onClick = { rating = value }, label = { Text(value.toString()) })
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= 500) comment = it },
            label = { Text("Comment (optional)") },
            supportingText = { Text("${comment.length}/500") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Your review is private until Admin moderation is completed.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { onSubmit(rating, comment) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text("Submit hosted review")
        }
    }
}