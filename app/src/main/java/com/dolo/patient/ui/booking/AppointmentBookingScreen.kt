package com.dolo.patient.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dolo.patient.R
import com.dolo.patient.data.PatientUiState
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.home.DoloPatientBottomNavigation
import com.dolo.patient.ui.home.PatientBottomItem
import com.dolo.patient.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun AppointmentBookingRoute(
    doctorId: String, patientState: PatientUiState, onBack: () -> Unit,
    onNotifications: () -> Unit, onToggleFavourite: (String) -> Unit,
    onConfirmed: (CreateWalkInAppointmentRequest) -> Unit, onHome: () -> Unit,
    onAppointments: () -> Unit, onBook: () -> Unit, onHistory: () -> Unit,
    onProfile: () -> Unit,
    viewModel: AppointmentBookingViewModel = viewModel(
        key = "appointment-booking-$doctorId",
        factory = AppointmentBookingViewModelFactory(doctorId, patientState)
    )
) {
    val state by viewModel.uiState.collectAsState()
    var chargeDialog by remember { mutableStateOf(false) }
    var dateDialog by remember { mutableStateOf(false) }
    LaunchedEffect(patientState) { viewModel.syncPatientState(patientState) }
    LaunchedEffect(state.bookingResult?.appointmentId) {
        if (state.bookingResult != null) {
            state.pendingRequest?.let(onConfirmed)
            viewModel.acknowledgeNavigation()
        }
    }
    if (chargeDialog) BookingInfoDialog("DO-LO service charge", "This configurable platform charge supports booking and live queue services. Consultation fees are collected at the clinic in this prototype.") { chargeDialog = false }
    if (dateDialog) BookingInfoDialog("More appointment dates", "Calendar selection is reserved for the API-enabled phase. Choose an available date shown here.") { dateDialog = false }
    AppointmentBookingScreen(state, { event ->
        when (event) {
            AppointmentBookingUiEvent.BackClicked -> onBack()
            AppointmentBookingUiEvent.NotificationsClicked -> onNotifications()
            AppointmentBookingUiEvent.ServiceChargeInfoClicked -> chargeDialog = true
            AppointmentBookingUiEvent.MoreDatesClicked -> dateDialog = true
            AppointmentBookingUiEvent.SaveDoctorClicked -> {
                state.doctor?.id?.let(onToggleFavourite); viewModel.onEvent(event)
            }
            else -> viewModel.onEvent(event)
        }
    }, onHome, onAppointments, onBook, onHistory, onProfile)
}

@Composable
private fun BookingInfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } })
}

@Composable
fun AppointmentBookingScreen(
    state: AppointmentBookingUiState, onEvent: (AppointmentBookingUiEvent) -> Unit,
    onHome: () -> Unit = {}, onAppointments: () -> Unit = {}, onBook: () -> Unit = {},
    onHistory: () -> Unit = {}, onProfile: () -> Unit = {}
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BookingTopBar(state.notificationCount, onEvent) },
        bottomBar = { DoloPatientBottomNavigation(PatientBottomItem.BOOK, onHome, onAppointments, onBook, onHistory, onProfile) }
    ) { padding ->
        when {
            state.isLoading -> BookingLoading(Modifier.padding(padding))
            state.doctor == null -> BookingEmpty("Doctor details are unavailable", onEvent, Modifier.padding(padding))
            state.errorMessage != null && state.clinics.isEmpty() -> BookingEmpty(state.errorMessage.orEmpty(), onEvent, Modifier.padding(padding))
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding), state = listState,
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { state.doctor?.let { doctor -> DoctorBookingSummary(doctor) { onEvent(AppointmentBookingUiEvent.SaveDoctorClicked) } } }
                item { WhoIsVisitingSection(state.visitors, state.selectedVisitorId) { onEvent(AppointmentBookingUiEvent.VisitorSelected(it)) } }
                item { ClinicSelectionSection(state.clinics, state.selectedClinicId) { onEvent(AppointmentBookingUiEvent.ClinicSelected(it)) } }
                item { AppointmentDateSelector(state.dates, state.selectedDate, { onEvent(AppointmentBookingUiEvent.DateSelected(it)) }) { onEvent(AppointmentBookingUiEvent.MoreDatesClicked) } }
                item { WalkInSessionSection(state.sessions, state.selectedSessionId) { onEvent(AppointmentBookingUiEvent.SessionSelected(it)) } }
                item { SelectedPatientDetailsCard(state.selectedVisitor) { onEvent(AppointmentBookingUiEvent.ChangePatientClicked); scope.launch { listState.animateScrollToItem(1) } } }
                state.fees?.let { fees -> item { AppointmentFeeSummary(fees) { onEvent(AppointmentBookingUiEvent.ServiceChargeInfoClicked) } } }
                item { SecureBookingInfo() }
                state.errorMessage?.let { item { BookingError(it) } }
                item { ConfirmBookingButton(state.canConfirm, state.isBooking) { onEvent(AppointmentBookingUiEvent.ConfirmBookingClicked) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingTopBar(count: Int, onEvent: (AppointmentBookingUiEvent) -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Book Appointment", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = { IconButton(onClick = { onEvent(AppointmentBookingUiEvent.BackClicked) }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go back") } },
        actions = { Box {
            IconButton(onClick = { onEvent(AppointmentBookingUiEvent.NotificationsClicked) }, modifier = Modifier.semantics { contentDescription = "Notifications, $count unread" }) { Icon(Icons.Outlined.NotificationsNone, null) }
            if (count > 0) Badge(Modifier.align(Alignment.TopEnd).offset((-3).dp, 4.dp), containerColor = DoloCoral) { Text(count.coerceAtMost(99).toString()) }
        } },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun DoctorBookingSummary(doctor: DoctorBookingSummaryUiModel, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { BrandLogo(); Spacer(Modifier.weight(1f)); Text("Walk-in booking", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        Row(verticalAlignment = Alignment.Top) {
            Surface(Modifier.size(82.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                doctor.imageRes?.let { Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    ?: Icon(Icons.Outlined.Person, null, Modifier.padding(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(doctor.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); if (doctor.isVerified) Icon(Icons.Outlined.Verified, "Verified doctor", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
                Text(doctor.qualifications, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(doctor.specialty, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, null, tint = Color(0xFFF3A51B), modifier = Modifier.size(16.dp)); Text(" ${doctor.rating} (${doctor.reviewCount}) | ${doctor.experienceYears}+ Years", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onSave) { Icon(if (doctor.isFavourite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, if (doctor.isFavourite) "Remove from favorites" else "Save doctor", tint = if (doctor.isFavourite) DoloCoral else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Row(verticalAlignment = Alignment.Top) { Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(doctor.primaryClinic, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); doctor.distanceKm?.let { Text("$it km", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) } }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
@Composable
fun WhoIsVisitingSection(visitors: List<AppointmentVisitorUiModel>, selectedId: String?, onSelect: (String) -> Unit) {
    SectionTitle(Icons.Outlined.Groups, "Who is visiting?")
    Spacer(Modifier.height(10.dp))
    if (visitors.isEmpty()) Text("No patient profiles available", color = MaterialTheme.colorScheme.error)
    else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(visitors, key = { it.id }) { AppointmentVisitorOption(it, it.id == selectedId) { onSelect(it.id) } } }
}

@Composable
fun AppointmentVisitorOption(visitor: AppointmentVisitorUiModel, selected: Boolean, onClick: () -> Unit) {
    val description = "${visitor.name}, ${visitor.relationLabel}, ${if (selected) "selected" else "not selected"} for appointment"
    SelectableSurface(166.dp, selected, visitor.isProfileComplete, description, onClick) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), CircleShape, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { Text(visitor.initials, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) } }
            Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(visitor.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(visitor.relationLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            SelectionIcon(selected)
        }
    }
}

@Composable
fun ClinicSelectionSection(clinics: List<ClinicOptionUiModel>, selectedId: String?, onSelect: (String) -> Unit) {
    SectionTitle(Icons.Outlined.LocalHospital, "1  Select Clinic"); Spacer(Modifier.height(10.dp))
    if (clinics.none { it.isAvailable }) Text("No clinic is available for booking", color = MaterialTheme.colorScheme.error)
    else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(clinics, key = { it.id }) { CompactClinicCard(it, it.id == selectedId) { onSelect(it.id) } } }
}

@Composable
fun CompactClinicCard(clinic: ClinicOptionUiModel, selected: Boolean, onClick: () -> Unit) {
    val distance = clinic.distanceKm?.let { "$it kilometres away" } ?: "distance unavailable"
    SelectableSurface(174.dp, selected, clinic.isAvailable, "${clinic.name}, ${clinic.address}, $distance, ${if (selected) "selected" else "not selected"}", onClick) {
        Column(Modifier.heightIn(min = 142.dp).padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row { Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.weight(1f)); SelectionIcon(selected) }
            Text(clinic.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(clinic.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            clinic.distanceKm?.let { Text("$it km away", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            if (!clinic.isAvailable) Text("Unavailable", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SelectableSurface(width: androidx.compose.ui.unit.Dp, selected: Boolean, enabled: Boolean, description: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.width(width).heightIn(min = 94.dp).alpha(if (enabled) 1f else .5f).semantics(mergeDescendants = true) { contentDescription = description }.clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(18.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), shadowElevation = if (selected) 3.dp else 1.dp,
        content = content
    )
}

@Composable
private fun SelectionIcon(selected: Boolean) { Icon(if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp)) }

@Composable
fun AppointmentDateSelector(dates: List<AppointmentDateUiModel>, selected: LocalDate?, onSelect: (LocalDate) -> Unit, onMore: () -> Unit) {
    SectionTitle(Icons.Outlined.CalendarMonth, "2  Select Date"); Spacer(Modifier.height(10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(dates, key = { it.date.toString() }) { DateOption(it, it.date == selected) { onSelect(it.date) } }
        item { OutlinedButton(onClick = onMore, modifier = Modifier.width(72.dp).height(78.dp), contentPadding = PaddingValues(4.dp), shape = RoundedCornerShape(15.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.CalendarToday, null, Modifier.size(20.dp)); Text("More", fontSize = 11.sp) } } }
    }
}

@Composable
private fun DateOption(option: AppointmentDateUiModel, selected: Boolean, onClick: () -> Unit) {
    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.width(62.dp).height(78.dp).alpha(if (option.isAvailable) 1f else .45f).semantics { contentDescription = "${option.date}, ${if (option.isAvailable) "available" else "unavailable"}${if (selected) ", selected" else ""}" }.clickable(enabled = option.isAvailable, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(15.dp), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) { Column(Modifier.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(option.date.format(DateTimeFormatter.ofPattern("EEE")), style = MaterialTheme.typography.labelMedium, color = foreground)
        Text(option.date.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge, color = foreground)
        Text(option.date.format(DateTimeFormatter.ofPattern("MMM")), style = MaterialTheme.typography.bodySmall, color = foreground)
    } }
}

@Composable
fun WalkInSessionSection(sessions: List<WalkInSessionUiModel>, selectedId: String?, onSelect: (String) -> Unit) {
    SectionTitle(Icons.Outlined.Schedule, "3  Select Session (Walk-in)")
    Text("Choose a session. Walk-in patients will be seen in order of token.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(sessions, key = { it.id }) { WalkInSessionCard(it, it.id == selectedId) { onSelect(it.id) } } }
}

@Composable
fun WalkInSessionCard(session: WalkInSessionUiModel, selected: Boolean, onClick: () -> Unit) {
    val time = session.startTime.format(timeFormat) + " - " + session.endTime.format(timeFormat)
    SelectableSurface(244.dp, selected, session.isAvailable, "${session.name}, $time, ${session.availableTokens} tokens available${if (selected) ", selected" else ""}", onClick) {
        Column(Modifier.heightIn(min = 160.dp).padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (session.type == WalkInSessionType.MORNING) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(session.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); SelectionIcon(selected) }
            Text(time, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
            Text(if (session.isAvailable) "${session.availableTokens} Tokens Available" else "Session Full", style = MaterialTheme.typography.labelLarge, color = if (session.isAvailable) DoloSuccess else MaterialTheme.colorScheme.error)
            session.reportingTime?.let { Text("Reporting: ${it.format(timeFormat)} onwards", style = MaterialTheme.typography.bodySmall) }
            Text("Walk-in patients only", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
fun SelectedPatientDetailsCard(visitor: AppointmentVisitorUiModel?, onChange: () -> Unit) {
    SectionTitle(Icons.Outlined.Badge, "4  Patient Details"); Spacer(Modifier.height(10.dp))
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(46.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text(visitor?.initials ?: "--", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
                Text(visitor?.name ?: "Select a patient", style = MaterialTheme.typography.titleMedium)
                val details = visitor?.phone?.takeIf(String::isNotBlank)?.let { "+91 ${formatPhone(it)}" } ?: visitor?.age?.let { "${visitor.relationLabel} | Age $it" } ?: visitor?.relationLabel.orEmpty()
                Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onChange) { Text("Change") }
        }
    }
}

@Composable
fun AppointmentFeeSummary(fees: AppointmentFeeUiModel, onInfo: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Fee Summary", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        FeeRow("Consultation Fee", AppointmentBookingLogic.formatInr(fees.consultationFee))
        Row(verticalAlignment = Alignment.CenterVertically) { Text("DO-LO Service Charge", Modifier.weight(1f)); IconButton(onClick = onInfo, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Info, "About service charge", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }; Text(AppointmentBookingLogic.formatInr(fees.serviceCharge)) }
        if (fees.discount > 0) FeeRow("Discount", "-${AppointmentBookingLogic.formatInr(fees.discount)}")
        HorizontalDivider(); FeeRow("Total Payable", AppointmentBookingLogic.formatInr(fees.totalPayable), true)
        Text("Consultation fee is collected at the clinic. Online payment is not enabled yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeeRow(label: String, value: String, strong: Boolean = false) { Row(Modifier.fillMaxWidth()) { Text(label, Modifier.weight(1f), style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium); Text(value, style = if (strong) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium, color = if (strong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) } }

@Composable
fun SecureBookingInfo() { Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.GppGood, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(11.dp)); Column { Text("Secure Booking", style = MaterialTheme.typography.titleSmall); Text("Your data is safe with us.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable
fun ConfirmBookingButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !loading, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { contentDescription = if (loading) "Confirming appointment" else "Confirm booking" }, shape = RoundedCornerShape(17.dp)) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        else { Text("Confirm Booking", style = MaterialTheme.typography.labelLarge); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null) }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { heading() }) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)); Text(title, style = MaterialTheme.typography.titleLarge) } }

@Composable
private fun BookingError(message: String) { Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.errorContainer) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(8.dp)); Text(message, Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer) } } }

@Composable
private fun BookingLoading(modifier: Modifier) { Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text("Loading booking options...") } } }

@Composable
private fun BookingEmpty(message: String, onEvent: (AppointmentBookingUiEvent) -> Unit, modifier: Modifier) { Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.EventBusy, null, Modifier.size(48.dp)); Spacer(Modifier.height(12.dp)); Text(message, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(12.dp)); Button(onClick = { onEvent(AppointmentBookingUiEvent.Retry) }) { Text("Try again") } } } }

private fun formatPhone(phone: String): String { val digits = phone.filter(Char::isDigit).takeLast(10); return if (digits.length == 10) digits.chunked(5).joinToString(" ") else phone }
private val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")

private fun bookingPreviewState(): AppointmentBookingUiState {
    val today = LocalDate.now()
    return AppointmentBookingUiState(
        doctor = DoctorBookingSummaryUiModel("3", "Dr. Rohan Mehta", "MBBS, MD (Cardiology), DM", "Interventional Cardiologist", 4.8, 152, 12, "Heart Care Clinic, Sector 45, Gurugram", 2.3, 700, R.drawable.doctor_rohan),
        visitors = listOf(AppointmentVisitorUiModel("self", "Rahul Sharma", "Self", "RS", isSelf = true, phone = "9876543210"), AppointmentVisitorUiModel("family", "Aarav Sharma", "Child", "AS", age = 9)), selectedVisitorId = "self",
        clinics = listOf(ClinicOptionUiModel("primary", "Heart Care Clinic", "Sector 45, Gurugram, Haryana", 2.3), ClinicOptionUiModel("life", "Life Line Hospital", "Sector 44, Main Road, Gurugram, Haryana", 3.8), ClinicOptionUiModel("long", "Advanced Cardiac and Multispeciality Care Centre", "Sushant Lok, Phase 1, Gurugram, Haryana", 4.1)), selectedClinicId = "primary",
        dates = (0L..6L).map { AppointmentDateUiModel(today.plusDays(it), it != 1L) }, selectedDate = today,
        sessions = listOf(WalkInSessionUiModel("morning", "Morning Session", LocalTime.of(9, 0), LocalTime.of(13, 0), LocalTime.of(8, 30), 30, 40, WalkInSessionType.MORNING, true), WalkInSessionUiModel("evening", "Evening Session", LocalTime.of(17, 0), LocalTime.of(21, 0), LocalTime.of(16, 30), 22, 35, WalkInSessionType.EVENING, true)), selectedSessionId = "morning",
        fees = AppointmentFeeUiModel(700, 20), notificationCount = 3
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingDefaultPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState(), {}) } }
@Preview(showBackground = true, widthDp = 320, heightDp = 720) @Composable private fun BookingSmallPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState(), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.3f) @Composable private fun BookingLargeFontPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState(), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingFamilyPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState().copy(selectedVisitorId = "family"), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingLoadingPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState().copy(isLoading = true), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingErrorPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState().copy(errorMessage = "Unable to complete booking. Please try again"), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingNoVisitorPreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState().copy(selectedVisitorId = null), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingUnavailablePreview() { DoloTheme { AppointmentBookingScreen(bookingPreviewState().copy(sessions = bookingPreviewState().sessions.map { it.copy(isAvailable = false) }, selectedSessionId = null), {}) } }
@Preview(showBackground = true, widthDp = 360, heightDp = 800) @Composable private fun BookingDarkPreview() { DoloTheme(true) { AppointmentBookingScreen(bookingPreviewState(), {}) } }