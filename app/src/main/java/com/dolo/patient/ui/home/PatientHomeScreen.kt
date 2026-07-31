package com.dolo.patient.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.dolo.patient.data.HostedSyncUiState
import com.dolo.patient.data.PatientUiState
import com.dolo.patient.data.ReleaseReadiness
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.components.PrimaryButton
import com.dolo.patient.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PatientHomeRoute(
    patientState: PatientUiState,
    hostedState: HostedSyncUiState?,
    onSearchDoctors: () -> Unit,
    onNearMe: () -> Unit,
    onAllQueues: () -> Unit,
    onQueue: (String) -> Unit,
    onNotifications: () -> Unit,
    onFavorites: () -> Unit,
    onDoctor: (String) -> Unit,
    onBookDoctor: (String) -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onSupport: () -> Unit,
    onLogout: () -> Unit,
    onRefreshQueues: () -> Unit,
    onRefreshHosted: () -> Unit,
    onHostedSync: () -> Unit,
    viewModel: PatientHomeViewModel = viewModel()
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(ReleaseReadiness.QUEUE_REFRESH_INTERVAL_MILLIS)
            onRefreshQueues()
        }
    }
    val uiState = viewModel.buildUiState(patientState, hostedState, nowMillis)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DoloPatientDrawer(
                patientName = uiState.patientName,
                onProfile = { scope.launch { drawerState.close() }; onProfile() },
                onFavorites = { scope.launch { drawerState.close() }; onFavorites() },
                onSupport = { scope.launch { drawerState.close() }; onSupport() },
                onHostedSync = { scope.launch { drawerState.close() }; onHostedSync() },
                onLogout = { scope.launch { drawerState.close() }; onLogout() }
            )
        }
    ) {
        PatientHomeScreen(
            uiState = uiState,
            onEvent = { event ->
                when (event) {
                    PatientHomeUiEvent.OpenMenu -> scope.launch { drawerState.open() }
                    PatientHomeUiEvent.OpenNotifications -> onNotifications()
                    PatientHomeUiEvent.SearchDoctors -> onSearchDoctors()
                    PatientHomeUiEvent.FindDoctorsNearMe -> onNearMe()
                    PatientHomeUiEvent.ViewAllQueues -> onAllQueues()
                    is PatientHomeUiEvent.OpenQueue -> onQueue(event.appointmentId)
                    is PatientHomeUiEvent.DismissBroadcast -> viewModel.dismissBroadcast(event.id)
                    is PatientHomeUiEvent.OpenBroadcast -> {
                        if (event.broadcast.actionType == BroadcastActionType.CATEGORY) onSearchDoctors() else onHostedSync()
                    }
                    PatientHomeUiEvent.ViewFavoriteDoctors -> onFavorites()
                    is PatientHomeUiEvent.OpenDoctor -> onDoctor(event.doctorId)
                    is PatientHomeUiEvent.BookAgain -> onBookDoctor(event.doctorId)
                    PatientHomeUiEvent.OpenAppointments -> onAppointments()
                    PatientHomeUiEvent.OpenBook -> onBook()
                    PatientHomeUiEvent.OpenHistory -> onHistory()
                    PatientHomeUiEvent.OpenProfile -> onProfile()
                }
            },
            onRetry = {
                onRefreshQueues()
                onRefreshHosted()
            }
        )
    }
}

@Composable
fun PatientHomeScreen(
    uiState: PatientHomeUiState,
    onEvent: (PatientHomeUiEvent) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = DoloBackground,
        topBar = {
            DoloTopAppBar(
                notificationCount = uiState.notificationCount,
                onMenu = { onEvent(PatientHomeUiEvent.OpenMenu) },
                onNotifications = { onEvent(PatientHomeUiEvent.OpenNotifications) }
            )
        },
        bottomBar = {
            DoloPatientBottomNavigation(
                selected = PatientBottomItem.HOME,
                onHome = {},
                onAppointments = { onEvent(PatientHomeUiEvent.OpenAppointments) },
                onBook = { onEvent(PatientHomeUiEvent.OpenBook) },
                onHistory = { onEvent(PatientHomeUiEvent.OpenHistory) },
                onProfile = { onEvent(PatientHomeUiEvent.OpenProfile) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PatientGreetingHeader(uiState.patientName, uiState.patientCity) }
            item {
                DoctorSearchRow(
                    onSearch = { onEvent(PatientHomeUiEvent.SearchDoctors) },
                    onNearMe = { onEvent(PatientHomeUiEvent.FindDoctorsNearMe) }
                )
            }
            item {
                QueueSummarySection(
                    queue = uiState.queueSummary,
                    queueCount = uiState.queues.size,
                    loading = uiState.isLoading,
                    onOpen = { id -> onEvent(PatientHomeUiEvent.OpenQueue(id)) },
                    onViewAll = { onEvent(PatientHomeUiEvent.ViewAllQueues) }
                )
            }
            uiState.errorMessage?.let { message ->
                item { HomeErrorCard(message, onRetry) }
            }
            item {
                AdminBroadcastCarousel(
                    broadcasts = uiState.broadcasts,
                    loading = uiState.broadcastLoading,
                    onDismiss = { onEvent(PatientHomeUiEvent.DismissBroadcast(it)) },
                    onAction = { onEvent(PatientHomeUiEvent.OpenBroadcast(it)) }
                )
            }
            item {
                FavoriteDoctorsSection(
                    doctors = uiState.favoriteDoctors,
                    onViewAll = { onEvent(PatientHomeUiEvent.ViewFavoriteDoctors) },
                    onOpen = { onEvent(PatientHomeUiEvent.OpenDoctor(it)) },
                    onBookAgain = { onEvent(PatientHomeUiEvent.BookAgain(it)) }
                )
            }
        }
    }
}

@Composable
fun DoloTopAppBar(notificationCount: Int, onMenu: () -> Unit, onNotifications: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Menu, "Open menu", tint = DoloNavy)
            }
            BrandLogo(compact = true)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNotifications, modifier = Modifier.size(48.dp)) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = DoloCoral) { Text(notificationCount.coerceAtMost(99).toString()) }
                        }
                    }
                ) { Icon(Icons.Outlined.Notifications, "View notifications", tint = DoloNavy) }
            }
        }
    }
}

@Composable
fun PatientGreetingHeader(patientName: String, patientCity: String) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
        Box(Modifier.fillMaxWidth().heightIn(min = 148.dp)) {
            Image(
                painterResource(R.drawable.patient_home_hero),
                contentDescription = "Patient comfortably using a smartphone at home",
                modifier = Modifier.fillMaxSize().align(Alignment.CenterEnd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd
            )
            Box(
                Modifier.matchParentSize().background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colorStops = arrayOf(0f to Color.White, .52f to Color.White.copy(alpha = .96f), 1f to Color.Transparent)
                    )
                )
            )
            Column(
                Modifier.align(Alignment.CenterStart).fillMaxWidth(.58f).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Welcome,", style = MaterialTheme.typography.titleLarge, color = DoloNavy)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(patientName, modifier = Modifier.weight(1f, fill = false), fontSize = 26.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, color = DoloTeal, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Outlined.WavingHand, "Friendly welcome", tint = DoloWarning, modifier = Modifier.size(24.dp))
                }
                if (patientCity.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, tint = DoloTeal, modifier = Modifier.size(15.dp))
                        Text(patientCity, style = MaterialTheme.typography.bodySmall, color = DoloMuted, maxLines = 1)
                    }
                }
            }
        }
    }
}
@Composable
fun DoctorSearchRow(onSearch: () -> Unit, onNearMe: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.weight(1f).heightIn(min = 58.dp).semantics { contentDescription = "Search doctors, clinics and specialties" }.clickable(role = Role.Button, onClick = onSearch),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DoloBorder),
            shadowElevation = 3.dp
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, null, tint = DoloMuted)
                Spacer(Modifier.width(9.dp))
                Text("Search doctors, clinics, specialties...", color = DoloMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Surface(
            modifier = Modifier.widthIn(min = 94.dp).heightIn(min = 58.dp).semantics { contentDescription = "Find doctors near me" }.clickable(role = Role.Button, onClick = onNearMe),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DoloBorder),
            shadowElevation = 3.dp
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.LocationOn, null, tint = DoloTeal, modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(5.dp))
                Text("Near me", color = DoloTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun QueueSummarySection(
    queue: QueueSummaryUiModel?,
    queueCount: Int,
    loading: Boolean,
    onOpen: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Live appointment status", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
            if (queueCount > 1) AssistChip(onClick = onViewAll, label = { Text("$queueCount active") })
        }
        when {
            loading -> QueueLoadingCards()
            queue == null -> NoActiveQueueCard(onViewAll)
            else -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PatientTokenCard(queue, onViewAll, Modifier.weight(1f))
                    CurrentTokenCard(queue, onOpen, Modifier.weight(1f))
                }
                if (queue.queueStatus == QueueStatus.PAUSED) {
                    Surface(color = DoloWarning.copy(alpha = .13f), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PauseCircle, null, tint = DoloWarning)
                            Spacer(Modifier.width(8.dp))
                            Text("This queue is temporarily paused.", color = DoloNavy, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientTokenCard(queue: QueueSummaryUiModel, onViewAll: () -> Unit, modifier: Modifier = Modifier) {
    HomeStatusCard(modifier, "YOUR TOKEN", DoloTeal) {
        Text("Your Token Number", style = MaterialTheme.typography.labelMedium, color = DoloNavy)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(queue.patientToken, fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, color = DoloTeal)
            Spacer(Modifier.width(6.dp))
            queue.estimatedWaitMinutes?.let {
                Row(Modifier.padding(bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = DoloTeal, modifier = Modifier.size(14.dp))
                    Text(" ${it.first}–${it.last} min", fontSize = 10.sp, color = DoloMuted, maxLines = 1)
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = DoloSurfaceAlt, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.ConfirmationNumber, null, tint = DoloTeal, modifier = Modifier.padding(9.dp))
            }
        }
        Text("Patient: ${queue.patientName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DoloNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Surface(color = DoloSurfaceAlt, shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = DoloTeal, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(queue.bookingTime, fontSize = 10.sp, color = DoloTeal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        TextButton(onClick = onViewAll, contentPadding = PaddingValues(0.dp), modifier = Modifier.heightIn(min = 40.dp).semantics { contentDescription = "View all active and upcoming queues" }) {
            Text("View all")
            Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CurrentTokenCard(queue: QueueSummaryUiModel, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    HomeStatusCard(
        modifier.semantics { contentDescription = "Current token ${queue.currentToken}, with ${queue.doctorName}" }.clickable { onOpen(queue.appointmentId) },
        "CURRENTLY IN PROCESS",
        DoloBlue
    ) {
        Text("Token Number", style = MaterialTheme.typography.labelMedium, color = DoloNavy)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(queue.currentToken, fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, color = DoloBlue)
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = Color(0xFFEDF3FF), modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.MedicalServices, null, tint = DoloBlue, modifier = Modifier.padding(9.dp))
            }
        }
        Spacer(Modifier.height(2.dp))
        Surface(color = Color(0xFFEDF3FF), shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PersonOutline, null, tint = DoloBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("With ${queue.doctorName}", fontSize = 10.sp, color = DoloBlue, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(
            when (queue.queueStatus) {
                QueueStatus.ACTIVE -> "Queue active"
                QueueStatus.UPCOMING -> "Upcoming"
                QueueStatus.PAUSED -> "Queue paused"
                QueueStatus.COMPLETED -> "Completed"
            },
            color = DoloMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun HomeStatusCard(modifier: Modifier, eyebrow: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder), shadowElevation = 4.dp) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(eyebrow, color = accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            content()
        }
    }
}

@Composable
private fun QueueLoadingCards() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(2) {
            Surface(Modifier.weight(1f).height(176.dp), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index -> Surface(Modifier.fillMaxWidth(if (index == 1) .45f else .8f).height(if (index == 1) 32.dp else 12.dp), RoundedCornerShape(50), DoloBorder.copy(alpha = .65f)) {} }
                }
            }
        }
    }
}

@Composable
private fun NoActiveQueueCard(onBook: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = DoloSurfaceAlt, border = BorderStroke(1.dp, DoloBorder)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.EventAvailable, null, tint = DoloTeal, modifier = Modifier.padding(11.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("No active queue", fontWeight = FontWeight.Bold, color = DoloNavy)
                Text("Your live token status will appear here after booking.", style = MaterialTheme.typography.bodySmall, color = DoloMuted)
            }
            TextButton(onClick = onBook) { Text("Book") }
        }
    }
}
@Composable
fun AdminBroadcastCarousel(
    broadcasts: List<BroadcastUiModel>,
    loading: Boolean,
    onDismiss: (String) -> Unit,
    onAction: (BroadcastUiModel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            loading -> BroadcastLoadingCard()
            broadcasts.isEmpty() -> {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Campaign, null, tint = DoloTeal)
                        Spacer(Modifier.width(10.dp))
                        Text("No current DO-LO broadcasts.", color = DoloMuted)
                    }
                }
            }
            else -> {
                val pagerState = rememberPagerState(pageCount = { broadcasts.size })
                HorizontalPager(state = pagerState, pageSpacing = 10.dp) { page ->
                    BroadcastCard(broadcasts[page], onDismiss, onAction)
                }
                if (broadcasts.size > 1) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        repeat(broadcasts.size) { index ->
                            Box(
                                Modifier.padding(horizontal = 3.dp).size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                    .clip(CircleShape).background(if (pagerState.currentPage == index) DoloTeal else DoloBorder)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastCard(broadcast: BroadcastUiModel, onDismiss: (String) -> Unit, onAction: (BroadcastUiModel) -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder), shadowElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().heightIn(min = 190.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Campaign, null, tint = DoloTeal, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Admin Broadcast", color = DoloTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(broadcast.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = DoloNavy, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(broadcast.message, style = MaterialTheme.typography.bodySmall, color = DoloMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    broadcast.buttonText?.let { label ->
                        Button(onClick = { onAction(broadcast) }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp)) {
                            Text(label)
                        }
                    }
                }
                Image(
                    painterResource(R.drawable.admin_broadcast_megaphone),
                    contentDescription = "Teal announcement megaphone",
                    modifier = Modifier.width(112.dp).height(130.dp).align(Alignment.CenterVertically),
                    contentScale = ContentScale.Fit
                )
            }
            if (broadcast.dismissible) {
                IconButton(
                    onClick = { onDismiss(broadcast.id) },
                    modifier = Modifier.align(Alignment.TopEnd).semantics { contentDescription = "Dismiss broadcast" }
                ) { Icon(Icons.Outlined.Close, null, tint = DoloMuted) }
            }
        }
    }
}

@Composable
private fun BroadcastLoadingCard() {
    Surface(Modifier.fillMaxWidth().height(190.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            repeat(4) { index ->
                Surface(Modifier.fillMaxWidth(if (index == 1) .65f else if (index == 3) .3f else .9f).height(if (index == 1) 22.dp else 12.dp), RoundedCornerShape(50), DoloBorder.copy(alpha = .65f)) {}
            }
        }
    }
}

@Composable
fun FavoriteDoctorsSection(
    doctors: List<FavoriteDoctorUiModel>,
    onViewAll: () -> Unit,
    onOpen: (String) -> Unit,
    onBookAgain: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Your Favorite Doctors", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
            TextButton(onClick = onViewAll) { Text("View all") }
        }
        if (doctors.isEmpty()) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, DoloBorder)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = DoloTeal)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("No favorite doctors yet", fontWeight = FontWeight.Bold, color = DoloNavy)
                        Text("Save a doctor to make repeat booking faster.", style = MaterialTheme.typography.bodySmall, color = DoloMuted)
                    }
                    TextButton(onClick = onViewAll) { Text("Browse") }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 4.dp)) {
                items(doctors, key = { it.id }) { doctor ->
                    FavoriteDoctorCard(doctor, { onOpen(doctor.id) }, { onBookAgain(doctor.id) })
                }
            }
        }
    }
}

@Composable
fun FavoriteDoctorCard(doctor: FavoriteDoctorUiModel, onOpen: () -> Unit, onBookAgain: () -> Unit) {
    Surface(
        modifier = Modifier.width(182.dp).semantics(mergeDescendants = true) { contentDescription = "${doctor.name}, ${doctor.specialty}" }.clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, DoloBorder),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Image(
                    painterResource(doctor.imageRes),
                    contentDescription = "Doctor photo placeholder for ${doctor.name}",
                    modifier = Modifier.align(Alignment.Center).size(76.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Icon(Icons.Outlined.Favorite, "Saved as favorite", tint = DoloCoral, modifier = Modifier.align(Alignment.TopEnd).size(22.dp))
            }
            Text(doctor.name, fontWeight = FontWeight.ExtraBold, color = DoloNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(doctor.specialty, style = MaterialTheme.typography.bodySmall, color = DoloMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Star, null, tint = DoloWarning, modifier = Modifier.size(16.dp))
                Text(" ${doctor.rating} (${doctor.reviewCount})", fontSize = 11.sp, color = DoloMuted)
            }
            doctor.distanceKm?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = DoloMuted, modifier = Modifier.size(16.dp))
                    Text(" ${"%.1f".format(it)} km", fontSize = 11.sp, color = DoloMuted)
                }
            }
            FilledTonalButton(
                onClick = onBookAgain,
                modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp).semantics { contentDescription = "Book again with ${doctor.name}" },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text("Book Again", maxLines = 1)
            }
        }
    }
}

enum class PatientBottomItem { HOME, APPOINTMENTS, BOOK, HISTORY, PROFILE }

@Composable
fun DoloPatientBottomNavigation(
    selected: PatientBottomItem,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().heightIn(min = 72.dp).padding(horizontal = 4.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PatientBottomNavItem(Icons.Outlined.Home, "Home", selected == PatientBottomItem.HOME, onHome, Modifier.weight(1f))
            PatientBottomNavItem(Icons.Outlined.CalendarMonth, "Appointments", selected == PatientBottomItem.APPOINTMENTS, onAppointments, Modifier.weight(1f))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(50.dp).semantics { contentDescription = "Book appointment" }.clickable(role = Role.Button, onClick = onBook),
                    shape = CircleShape,
                    color = DoloTeal,
                    shadowElevation = 6.dp
                ) { Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.padding(12.dp)) }
                Text("Book", fontSize = 10.sp, color = if (selected == PatientBottomItem.BOOK) DoloTeal else DoloMuted)
            }
            PatientBottomNavItem(Icons.Outlined.History, "History", selected == PatientBottomItem.HISTORY, onHistory, Modifier.weight(1f))
            PatientBottomNavItem(Icons.Outlined.PersonOutline, "Profile", selected == PatientBottomItem.PROFILE, onProfile, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PatientBottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = if (selected) DoloTeal else DoloMuted
    Column(
        modifier.heightIn(min = 58.dp).semantics(mergeDescendants = true) { contentDescription = label + if (selected) ", selected" else "" }.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(23.dp))
        Text(label, fontSize = 9.sp, color = color, maxLines = 1)
    }
}
@Composable
private fun DoloPatientDrawer(
    patientName: String,
    onProfile: () -> Unit,
    onFavorites: () -> Unit,
    onSupport: () -> Unit,
    onHostedSync: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = Color.White) {
        Column(Modifier.statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandLogo()
            Spacer(Modifier.height(8.dp))
            Text(patientName, style = MaterialTheme.typography.titleLarge, color = DoloNavy)
            Text("Patient account", style = MaterialTheme.typography.bodySmall, color = DoloMuted)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DrawerActionItem(Icons.Outlined.PersonOutline, "Profile & family", onProfile)
            DrawerActionItem(Icons.Outlined.FavoriteBorder, "Favorite doctors", onFavorites)
            DrawerActionItem(Icons.Outlined.SupportAgent, "Help & support", onSupport)
            DrawerActionItem(Icons.Outlined.CloudSync, "Hosted prototype sync", onHostedSync)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DrawerActionItem(Icons.Outlined.Logout, "Sign out", onLogout)
        }
    }
}

@Composable
private fun DrawerActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null) },
        shape = RoundedCornerShape(15.dp)
    )
}

@Composable
private fun HomeErrorCard(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(10.dp))
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, maxLines = 3)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun AllQueuesScreen(
    patientState: PatientUiState,
    hostedState: HostedSyncUiState?,
    onBack: () -> Unit,
    onQueue: (String) -> Unit,
    viewModel: PatientHomeViewModel = viewModel()
) {
    val state = viewModel.buildUiState(patientState, hostedState, System.currentTimeMillis())
    Scaffold(
        containerColor = DoloBackground,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 62.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
                    Text("All active queues", style = MaterialTheme.typography.titleLarge, color = DoloNavy)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.queues.isEmpty()) {
                item { NoActiveQueueCard(onBack) }
            } else {
                items(state.queues, key = { it.appointmentId }) { queue ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onQueue(queue.appointmentId) },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, DoloBorder),
                        shadowElevation = 3.dp
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text(queue.doctorName, fontWeight = FontWeight.ExtraBold, color = DoloNavy)
                                    Text(queue.clinicName, style = MaterialTheme.typography.bodySmall, color = DoloMuted)
                                    Text("${queue.patientName} • ${queue.session}", style = MaterialTheme.typography.bodySmall, color = DoloMuted)
                                }
                                Text("Token ${queue.patientToken}", color = DoloTeal, fontWeight = FontWeight.ExtraBold)
                            }
                            HorizontalDivider()
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Current: ${queue.currentToken}", fontWeight = FontWeight.SemiBold)
                                Text(queue.estimatedWaitMinutes?.let { "${it.first}–${it.last} min" } ?: "Wait pending", color = DoloMuted)
                                Text(queue.queueStatus.name.lowercase().replaceFirstChar(Char::uppercase), color = DoloTeal)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val previewQueue = QueueSummaryUiModel(
    appointmentId = "preview-18",
    patientName = "Rahul Sharma",
    patientToken = "18",
    currentToken = "12",
    estimatedWaitMinutes = 35..40,
    bookingTime = "Booked at 09:30 AM",
    doctorName = "Dr. Anjali Verma",
    clinicName = "Care Point Clinic",
    session = "Morning",
    queueStatus = QueueStatus.ACTIVE
)

private val previewDoctors = listOf(
    FavoriteDoctorUiModel("1", "Dr. Anjali Verma", "General Physician", 4.8, 120, 2.3, R.drawable.category_general),
    FavoriteDoctorUiModel("2", "Dr. Rohan Mehta", "Dermatologist", 4.7, 98, 3.1, R.drawable.category_skin),
    FavoriteDoctorUiModel("3", "Dr. Neha Singh", "Pediatrician", 4.9, 150, 2.8, R.drawable.category_child)
)

private fun previewHomeState(
    queues: List<QueueSummaryUiModel> = listOf(previewQueue),
    broadcasts: List<BroadcastUiModel> = listOf(PatientHomeViewModel.defaultBroadcast),
    favorites: List<FavoriteDoctorUiModel> = previewDoctors,
    loading: Boolean = false,
    broadcastLoading: Boolean = false,
    error: String? = null
) = PatientHomeUiState(
    patientName = "Rahul Sharma",
    patientCity = "New Delhi",
    queues = queues,
    broadcasts = broadcasts,
    favoriteDoctors = favorites,
    notificationCount = 3,
    isLoading = loading,
    broadcastLoading = broadcastLoading,
    errorMessage = error
)

@Preview(name = "Normal home", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun NormalHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState()) } }

@Preview(name = "Loading", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun LoadingHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(queues = emptyList(), loading = true, broadcastLoading = true)) } }

@Preview(name = "No active token", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun NoQueueHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(queues = emptyList())) } }

@Preview(name = "Queue paused", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun PausedQueueHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(queues = listOf(previewQueue.copy(queueStatus = QueueStatus.PAUSED)))) } }

@Preview(name = "Multiple queues", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun MultipleQueueHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(queues = listOf(previewQueue, previewQueue.copy(appointmentId = "preview-25", patientToken = "25")))) } }

@Preview(name = "No broadcasts", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun NoBroadcastHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(broadcasts = emptyList())) } }

@Preview(name = "No favorite doctors", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun NoFavoritesHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(favorites = emptyList())) } }

@Preview(name = "Network error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun ErrorHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState(error = "The latest queue update could not be loaded.")) } }

@Preview(name = "Small phone", showBackground = true, widthDp = 320, heightDp = 700)
@Composable private fun SmallHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState()) } }

@Preview(name = "Large font", showBackground = true, widthDp = 390, heightDp = 844, fontScale = 1.35f)
@Composable private fun LargeFontHomePreview() { DoloTheme { PatientHomeScreen(previewHomeState()) } }