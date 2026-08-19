package com.dolo.patient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolo.patient.data.HostedPatientSyncViewModel
import com.dolo.patient.ui.components.ScreenTitle

@Composable
fun HostedBookingScreen(
    clinicId: String,
    viewModel: HostedPatientSyncViewModel,
    onBack: () -> Unit,
    onAppointments: () -> Unit
) {
    val state = viewModel.uiState
    val snapshot = state.snapshot?.takeIf { it.bootstrap.clinic.id == clinicId }
    var selectedProfileId by rememberSaveable(clinicId) { mutableStateOf<String?>(null) }

    var addingFamily by rememberSaveable(clinicId) { mutableStateOf(false) }
    var familyName by rememberSaveable(clinicId) { mutableStateOf("") }
    var familyRelationship by rememberSaveable(clinicId) { mutableStateOf("CHILD") }
    LaunchedEffect(clinicId) { viewModel.refresh(clinicId) }
    LaunchedEffect(snapshot?.bootstrap?.profiles) {
        val profiles = snapshot?.bootstrap?.profiles.orEmpty()
        if (profiles.none { it.id == selectedProfileId }) selectedProfileId = profiles.firstOrNull()?.id

        val created=profiles.lastOrNull{addingFamily&&familyName.isNotBlank()&&it.name.equals(familyName.trim(),ignoreCase=true)&&it.relationship==familyRelationship}
        if(created!=null){
            selectedProfileId=created.id
            addingFamily=false
            familyName=""
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenTitle("Book Appointment", onBack) }
        if (snapshot == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (state.error) "Unable to load booking" else "Loading clinic sessions", fontWeight = FontWeight.Bold)
                        Text(if (state.error) state.message else "Please wait while DO-LO checks current availability.")
                        Button(onClick = { viewModel.refresh(clinicId) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                            if (state.loading) CircularProgressIndicator(strokeWidth = 2.dp)
                            else Text("Retry")
                        }
                    }
                }
            }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(snapshot.bootstrap.clinic.doctorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${snapshot.bootstrap.clinic.specialty} • ${snapshot.bootstrap.clinic.name}")
                        Text("${snapshot.bootstrap.clinic.city} • Consultation fee paid at clinic", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { Text("Who is visiting?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(snapshot.bootstrap.profiles, key = { "booking-profile-${it.id}" }) { profile ->
                FilterChip(
                    selected = selectedProfileId == profile.id,
                    onClick = { selectedProfileId = profile.id },
                    label = { Text("${profile.name} (${profile.relationship.lowercase()})") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedButton(onClick={addingFamily=!addingFamily},modifier=Modifier.fillMaxWidth()){
                    Text(if(addingFamily)"Cancel family member" else "Add family member")
                }
            }
            if(addingFamily)item{
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)){
                    Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        Text("New family member",fontWeight=FontWeight.Bold)
                        OutlinedTextField(familyName,{familyName=it.take(80)},label={Text("Full name")},singleLine=true,modifier=Modifier.fillMaxWidth())
                        Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                            listOf("SPOUSE" to "Spouse","CHILD" to "Child","PARENT" to "Parent","OTHER" to "Other").forEach{(value,label)->
                                FilterChip(selected=familyRelationship==value,onClick={familyRelationship=value},label={Text(label)},modifier=Modifier.fillMaxWidth())
                            }
                        }
                        Button(onClick={viewModel.createFamilyProfile(familyName,familyRelationship,clinicId)},enabled=familyName.trim().length>=2&&!state.loading,modifier=Modifier.fillMaxWidth()){Text("Save family member")}
                        Text("This creates a server-authoritative pilot profile. Existing local demo family data is not uploaded.",style=MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { Text("Available sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (snapshot.bootstrap.sessions.isEmpty()) {
                item { Text("No appointment session is currently available for this clinic.") }
            } else {
                items(snapshot.bootstrap.sessions, key = { "booking-session-${it.id}" }) { session ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${session.date} • ${session.name}", fontWeight = FontWeight.Bold)
                            Text("${session.startsAt.take(5)} to ${session.endsAt.take(5)}")
                            Text("${session.available} tokens available", style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = { selectedProfileId?.let { viewModel.book(session.id, it, clinicId) } },
                                enabled = session.enabled && selectedProfileId != null && !state.loading,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (session.enabled) "Book Appointment" else "Booking Closed") }
                        }
                    }
                }
            }
            if (state.error) item { Text(state.message, color = MaterialTheme.colorScheme.error) }
            if (state.loading) item { CircularProgressIndicator() }
            item { Button(onClick = onAppointments, modifier = Modifier.fillMaxWidth()) { Text("View My Appointments") } }
        }
    }
}