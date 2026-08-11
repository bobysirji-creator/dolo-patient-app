package com.dolo.patient.ui.confirmation

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dolo.patient.R
import com.dolo.patient.data.Appointment
import com.dolo.patient.data.DummyData
import com.dolo.patient.data.QueueSnapshot
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.theme.DoloTheme
import com.dolo.patient.ui.theme.DoloSuccess
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun BookingConfirmationRoute(
    appointment: Appointment?,
    queue: QueueSnapshot?,
    notificationCount: Int,
    onNotifications: () -> Unit,
    onDoctorProfile: (String) -> Unit,
    onViewMap: () -> Unit,
    onAppointments: () -> Unit,
    onHome: () -> Unit,
) {
    val context = LocalContext.current
    val doctor = appointment?.let { booked -> DummyData.doctors.firstOrNull { it.id == booked.doctorId } }
    val repository = LocalBookingConfirmationRepository(appointment, doctor, queue, notificationCount)
    val confirmationViewModel: BookingConfirmationViewModel = viewModel(
        key = "confirmation-${appointment?.id.orEmpty()}",
        factory = BookingConfirmationViewModelFactory(appointment?.id.orEmpty(), repository),
    )
    val state by confirmationViewModel.uiState.collectAsState()
    BookingConfirmationScreen(
        state = state,
        onNotifications = onNotifications,
        onRetry = confirmationViewModel::retry,
        onDoctorProfile = onDoctorProfile,
        onViewMap = {
            onViewMap()
            Toast.makeText(context, "Maps integration will be enabled later.", Toast.LENGTH_SHORT).show()
        },
        onAddToCalendar = {
            val date = appointment?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val start = date?.atTime(if (appointment?.session?.name == "MORNING") 9 else 17, 0)
                ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "DO-LO appointment with ${appointment?.doctorName.orEmpty()}")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, appointment?.clinic.orEmpty())
            start?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
            runCatching { context.startActivity(intent) }
                .onFailure { Toast.makeText(context, "Calendar app is not available.", Toast.LENGTH_SHORT).show() }
        },
        onShare = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, BookingConfirmationText.shareSummary(state))
            }
            context.startActivity(Intent.createChooser(intent, "Share appointment"))
        },
        onSave = { Toast.makeText(context, "Appointment saved. PDF export will be added later.", Toast.LENGTH_SHORT).show() },
        onAppointments = onAppointments,
        onHome = onHome,
    )
}

@Composable
fun BookingConfirmationScreen(
    state: BookingConfirmationUiState,
    onNotifications: () -> Unit,
    onRetry: () -> Unit,
    onDoctorProfile: (String) -> Unit,
    onViewMap: () -> Unit,
    onAddToCalendar: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onAppointments: () -> Unit,
    onHome: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { ConfirmationTopBar(state.notificationCount, onNotifications) }
        when {
            state.isLoading -> item { ConfirmationLoadingState() }
            state.status == BookingStatus.FAILED -> item { ConfirmationErrorState(state.errorMessage, onRetry, onAppointments, onHome) }
            state.status == BookingStatus.PENDING -> item { ConfirmationPendingState(onAppointments, onHome) }
            state.canShowConfirmation -> {
                item { ConfirmationSuccessHeader() }
                item { TokenCard(state.token!!) }
                item { AppointmentDetailsCard(state, onDoctorProfile, onViewMap) }
                item { QueueEstimateCard(state.queueEstimate) }
                item { ImportantInstructionsCard() }
                item { ConfirmationQuickActions(onAddToCalendar, onShare, onSave) }
                item {
                    Button(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Outlined.Home, null)
                        Spacer(Modifier.width(9.dp))
                        Text("Back to Home", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ConfirmationTopBar(count: Int, onNotifications: () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        BrandLogo()
        Box(Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onNotifications, modifier = Modifier.semantics { contentDescription = "Notifications, $count unread" }) {
                Icon(Icons.Outlined.NotificationsNone, null, modifier = Modifier.size(27.dp))
            }
            if (count > 0) Surface(color = MaterialTheme.colorScheme.error, shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd).size(18.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(count.coerceAtMost(9).toString(), color = MaterialTheme.colorScheme.onError, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ConfirmationSuccessHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = DoloSuccess.copy(alpha = 0.12f), shape = CircleShape, modifier = Modifier.size(88.dp)) {
            Icon(Icons.Outlined.CheckCircle, "Booking confirmed", tint = DoloSuccess, modifier = Modifier.padding(17.dp))
        }
        Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.semantics { heading() })
        Text("Your walk-in appointment is confirmed.\nPlease reach the clinic before your turn.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun TokenCard(token: TokenUiModel) {
    Card(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).semantics { contentDescription = "Your token number is ${token.number}. Keep this number safe." }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("YOUR TOKEN NUMBER", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Text(token.number.toString(), fontSize = 76.sp, lineHeight = 80.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 14.dp))
            Surface(color = DoloSuccess.copy(alpha = 0.1f), shape = RoundedCornerShape(50), modifier = Modifier.padding(bottom = 18.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, null, tint = DoloSuccess, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Keep this number safe", style = MaterialTheme.typography.labelMedium, color = DoloSuccess)
                }
            }
        }
    }
}

@Composable
private fun AppointmentDetailsCard(state: BookingConfirmationUiState, onDoctorProfile: (String) -> Unit, onViewMap: () -> Unit) {
    Card(Modifier.fillMaxWidth().widthIn(max = 520.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            ConfirmationDetailRow(Icons.Outlined.CalendarMonth, "Date", state.appointmentDate, trailing = { ConfirmationBadge("Confirmed") })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            state.session?.let { ConfirmationDetailRow(Icons.Outlined.WbSunny, it.title, it.timeRange) }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            state.doctor?.let { doctor ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (doctor.imageRes != null) Image(painterResource(doctor.imageRes), "Portrait of ${doctor.name}", Modifier.size(52.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    else Surface(Modifier.size(52.dp), CircleShape, MaterialTheme.colorScheme.surfaceVariant) { Icon(Icons.Outlined.Person, null, Modifier.padding(13.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Doctor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(doctor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(doctor.specialty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    TextButton(onClick = { onDoctorProfile(doctor.id) }) { Text("Profile") }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            state.clinic?.let { clinic ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(42.dp), RoundedCornerShape(13.dp), MaterialTheme.colorScheme.primaryContainer) { Icon(Icons.Outlined.LocationOn, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clinic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(clinic.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(clinic.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    TextButton(onClick = onViewMap) { Icon(Icons.Outlined.Map, null, Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("Map") }
                }
            }
            if (state.patientName.isNotBlank()) Text("Appointment for ${state.patientName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ConfirmationDetailRow(icon: ImageVector, label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(42.dp), RoundedCornerShape(13.dp), MaterialTheme.colorScheme.primaryContainer) { Icon(icon, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
        trailing?.invoke()
    }
}

@Composable
private fun ConfirmationBadge(text: String) {
    Surface(color = DoloSuccess.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) { Text(text, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = DoloSuccess, style = MaterialTheme.typography.labelSmall) }
}

@Composable
fun QueueEstimateCard(estimate: QueueEstimateUiModel?) {
    Surface(Modifier.fillMaxWidth().widthIn(max = 520.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(56.dp), CircleShape, MaterialTheme.colorScheme.surface) { Icon(Icons.Outlined.Schedule, null, Modifier.padding(14.dp), tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Approx. Time for Your Turn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (estimate == null) Text("Queue estimate will appear after check-in", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                else {
                    Text(estimate.timeLabel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("${estimate.patientsAhead} patients ahead of you", style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Outlined.Info, "Waiting-time information", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun ImportantInstructionsCard() {
    Surface(Modifier.fillMaxWidth().widthIn(max = 520.dp), shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF5DE).let { if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) MaterialTheme.colorScheme.surfaceVariant else it }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Important Instructions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            InstructionRow(Icons.Outlined.Schedule, "Please reach the clinic 10-15 mins before your turn.")
            InstructionRow(Icons.Outlined.Person, "Your token may be skipped if you are not present.")
            InstructionRow(Icons.Outlined.CalendarMonth, "You can reschedule once if you miss your appointment.")
        }
    }
}

@Composable
private fun InstructionRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(9.dp)); Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f)) }
}

@Composable
fun ConfirmationQuickActions(onAddToCalendar: () -> Unit, onShare: () -> Unit, onSave: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
        val actions = listOf(Triple("Add to Calendar", Icons.Outlined.CalendarMonth, onAddToCalendar), Triple("Share Booking", Icons.Outlined.Share, onShare), Triple("Download / Save", Icons.Outlined.CloudDownload, onSave))
        if (maxWidth < 340.dp) Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { actions.forEach { QuickConfirmationAction(it.first, it.second, it.third, Modifier.fillMaxWidth()) } }
        else Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { actions.forEach { QuickConfirmationAction(it.first, it.second, it.third, Modifier.weight(1f)) } }
    }
}

@Composable
private fun QuickConfirmationAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 76.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), contentPadding = PaddingValues(8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2) }
    }
}

@Composable
private fun ConfirmationLoadingState() {
    Column(Modifier.fillMaxWidth().widthIn(max = 520.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CircularProgressIndicator()
        Text("Preparing your confirmation...", style = MaterialTheme.typography.titleMedium)
        repeat(3) { Surface(Modifier.fillMaxWidth().height(92.dp), RoundedCornerShape(20.dp), MaterialTheme.colorScheme.surfaceVariant) {} }
    }
}

@Composable
private fun ConfirmationPendingState(onAppointments: () -> Unit, onHome: () -> Unit) {
    ConfirmationMessageState(Icons.Outlined.Schedule, "Token allocation pending", "Your booking is saved. Your token will appear as soon as the clinic confirms this walk-in session.", "View Appointments", onAppointments, onHome)
}

@Composable
private fun ConfirmationErrorState(message: String?, onRetry: () -> Unit, onAppointments: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxWidth().widthIn(max = 460.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.Refresh, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(60.dp))
        Text("Confirmation unavailable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(message ?: "Something went wrong.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Retry") }
        OutlinedButton(onClick = onAppointments, modifier = Modifier.fillMaxWidth()) { Text("View Appointments") }
        TextButton(onClick = onHome) { Text("Back to Home") }
    }
}

@Composable
private fun ConfirmationMessageState(icon: ImageVector, title: String, message: String, actionLabel: String, onAction: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxWidth().widthIn(max = 460.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(actionLabel) }
        TextButton(onClick = onHome) { Text("Back to Home") }
    }
}

private fun Color.luminance(): Float = (red * 0.299f) + (green * 0.587f) + (blue * 0.114f)

private val previewConfirmed = BookingConfirmationUiState(
    isLoading = false, status = BookingStatus.CONFIRMED, appointmentId = "a-18", token = TokenUiModel(18), appointmentDate = "Mon, 03 Aug 2026",
    session = SessionConfirmationUiModel("Morning Session", "9:00 AM - 1:00 PM"),
    doctor = DoctorConfirmationUiModel("3", "Dr. Rohan Mehta", "Cardiologist", R.drawable.doctor_rohan),
    clinic = ClinicConfirmationUiModel("Heart Care Clinic", "Sector 45, Gurugram"), patientName = "Rahul Verma",
    queueEstimate = QueueEstimateUiModel(35, 40, 5), notificationCount = 3,
)

@Preview(showBackground = true, widthDp = 390, heightDp = 1400) @Composable private fun ConfirmedPreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 320, heightDp = 1500) @Composable private fun SmallPhonePreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed.copy(token = TokenUiModel(125)), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1400, fontScale = 1.4f) @Composable private fun LargeFontPreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1400) @Composable private fun EveningPreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed.copy(session = SessionConfirmationUiModel("Evening Session", "5:00 PM - 8:00 PM")), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1000) @Composable private fun NoQueuePreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed.copy(queueEstimate = null), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 900) @Composable private fun LoadingPreview() = DoloTheme { BookingConfirmationScreen(BookingConfirmationUiState(isLoading = true), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 900) @Composable private fun PendingPreview() = DoloTheme { BookingConfirmationScreen(BookingConfirmationUiState(isLoading = false, status = BookingStatus.PENDING), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 900) @Composable private fun ErrorPreview() = DoloTheme { BookingConfirmationScreen(BookingConfirmationUiState(isLoading = false, status = BookingStatus.FAILED, errorMessage = "Network connection failed."), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1400) @Composable private fun LongClinicPreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed.copy(clinic = ClinicConfirmationUiModel("DO-LO Multispeciality and Diagnostic Centre", "Main Market Road, Sector 45, Gurugram, Haryana")), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1400) @Composable private fun FamilyBookingPreview() = DoloTheme { BookingConfirmationScreen(previewConfirmed.copy(patientName = "Sushila Verma (Mother)"), {}, {}, {}, {}, {}, {}, {}, {}, {}) }
@Preview(showBackground = true, widthDp = 390, heightDp = 1400) @Composable private fun DarkPreview() = DoloTheme(darkTheme = true) { BookingConfirmationScreen(previewConfirmed, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
