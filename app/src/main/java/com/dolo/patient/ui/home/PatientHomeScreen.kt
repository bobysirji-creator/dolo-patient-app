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
    darkModeEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
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
                darkModeEnabled = darkModeEnabled,
                onDarkModeChange = onDarkModeChange,
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
            darkModeEnabled = darkModeEnabled,
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
    darkModeEnabled: Boolean = false,
    onEvent: (PatientHomeUiEvent) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                onBook = { onEvent(PatientHomeUiEvent.OpenBook) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PatientGreetingHeader(uiState.patientName, uiState.patientCity, darkModeEnabled) }
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
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Menu, "Open menu", tint = MaterialTheme.colorScheme.onSurface)
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
                ) { Icon(Icons.Outlined.Notifications, "View notifications", tint = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

@Composable
fun PatientGreetingHeader(patientName: String, patientCity: String, darkModeEnabled: Boolean) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Box(Modifier.fillMaxWidth().height(112.dp)) {
            Image(
                painterResource(R.drawable.patient_home_hero),
                contentDescription = "Patient comfortably using a smartphone at home",
                modifier = Modifier.fillMaxSize().align(Alignment.CenterEnd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                alpha = 1f
            )
            Box(
                Modifier.matchParentSize().background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to MaterialTheme.colorScheme.surface,
                            .54f to MaterialTheme.colorScheme.surface.copy(alpha = .90f),
                            1f to MaterialTheme.colorScheme.surface.copy(alpha = if (darkModeEnabled) .08f else 0f)
                        )
                    )
                )
            )
            Column(
                Modifier.align(Alignment.TopStart).fillMaxWidth(.74f).padding(start = 15.dp, top = 12.dp, end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text("Welcome,", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    patientName,
                    fontSize = if (patientName.length > 18) 18.sp else 22.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                if (patientCity.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(patientCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
    }
}
@Composable
fun DoctorSearchRow(onSearch: () -> Unit, onNearMe: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.weight(3f).heightIn(min = 56.dp).semantics { contentDescription = "Search doctors, clinics and specialties" }.clickable(role = Role.Button, onClick = onSearch),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 3.dp
        ) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("Search doctors, clinics, specialties...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        TextButton(
            onClick = onNearMe,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { contentDescription = "Find doctors near me" },
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(3.dp))
            Text("Near me", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.semantics { contentDescription = "View all active and upcoming queues" }
            ) {
                Text(if (queueCount > 1) "View all (" + queueCount + ")" else "View all")
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
        when {
            loading -> QueueLoadingCards()
            queue == null -> NoActiveQueueCard(onViewAll)
            else -> {
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PatientTokenCard(queue, Modifier.weight(1f).fillMaxHeight())
                    CurrentTokenCard(queue, onOpen, Modifier.weight(1f).fillMaxHeight())
                }
                if (queue.queueStatus == QueueStatus.PAUSED) {
                    Surface(color = DoloWarning.copy(alpha = .13f), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PauseCircle, null, tint = DoloWarning)
                            Spacer(Modifier.width(8.dp))
                            Text("This queue is temporarily paused.", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PatientTokenCard(queue: QueueSummaryUiModel, modifier: Modifier = Modifier) {
    HomeStatusCard(modifier, "YOUR TOKEN", MaterialTheme.colorScheme.primary) {
        Text("Your Token Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(queue.patientToken, fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            queue.estimatedWaitMinutes?.let {
                Row(Modifier.padding(bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text(" " + it.first + "–" + it.last + " min", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(9.dp))
            }
        }
        Text("Patient: " + queue.patientName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(queue.bookingTime, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
@Composable
fun CurrentTokenCard(queue: QueueSummaryUiModel, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    HomeStatusCard(
        modifier.semantics { contentDescription = "Current token ${queue.currentToken}, with ${queue.doctorName}" }.clickable { onOpen(queue.appointmentId) },
        "CURRENTLY IN PROCESS",
        MaterialTheme.colorScheme.secondary
    ) {
        Text("Token Number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(queue.currentToken, fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.MedicalServices, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(9.dp))
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            when (queue.queueStatus) {
                QueueStatus.ACTIVE -> "Queue active"
                QueueStatus.UPCOMING -> "Upcoming"
                QueueStatus.PAUSED -> "Queue paused"
                QueueStatus.COMPLETED -> "Completed"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PersonOutline, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("With ${queue.doctorName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun HomeStatusCard(modifier: Modifier, eyebrow: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 4.dp) {
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
            Surface(Modifier.weight(1f).height(176.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index -> Surface(Modifier.fillMaxWidth(if (index == 1) .45f else .8f).height(if (index == 1) 32.dp else 12.dp), RoundedCornerShape(50), MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f)) {} }
                }
            }
        }
    }
}

@Composable
private fun NoActiveQueueCard(onBook: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.EventAvailable, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(11.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("No active queue", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Your live token status will appear here after booking.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Campaign, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("No current DO-LO broadcasts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    .clip(CircleShape).background(if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
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
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().heightIn(min = 190.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Campaign, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Admin Broadcast", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(broadcast.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(broadcast.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
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
                ) { Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun BroadcastLoadingCard() {
    Surface(Modifier.fillMaxWidth().height(190.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            repeat(4) { index ->
                Surface(Modifier.fillMaxWidth(if (index == 1) .65f else if (index == 3) .3f else .9f).height(if (index == 1) 22.dp else 12.dp), RoundedCornerShape(50), MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f)) {}
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
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("No favorite doctors yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Save a doctor to make repeat booking faster.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
            Text(doctor.name, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(doctor.specialty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Star, null, tint = DoloWarning, modifier = Modifier.size(16.dp))
                Text(" ${doctor.rating} (${doctor.reviewCount})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            doctor.distanceKm?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(" ${"%.1f".format(it)} km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

enum class PatientBottomItem { HOME, BOOK, APPOINTMENTS }

@Composable
fun DoloPatientBottomNavigation(
    selected: PatientBottomItem,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationBarItem(
            selected = selected == PatientBottomItem.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", maxLines = 1) }
        )
        NavigationBarItem(
            selected = selected == PatientBottomItem.BOOK,
            onClick = onBook,
            icon = { Icon(Icons.Outlined.Add, contentDescription = "Book appointment") },
            label = { Text("Book", maxLines = 1) }
        )
        NavigationBarItem(
            selected = selected == PatientBottomItem.APPOINTMENTS,
            onClick = onAppointments,
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Appointments") },
            label = { Text("Appointments", maxLines = 1) }
        )
    }
}
@Composable
private fun DoloPatientDrawer(
    patientName: String,
    darkModeEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onProfile: () -> Unit,
    onFavorites: () -> Unit,
    onSupport: () -> Unit,
    onHostedSync: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandLogo()
            Spacer(Modifier.height(8.dp))
            Text(patientName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("Patient account", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DrawerActionItem(Icons.Outlined.PersonOutline, "Profile & family", onProfile)
            DrawerActionItem(Icons.Outlined.FavoriteBorder, "Favorite doctors", onFavorites)
            DrawerActionItem(Icons.Outlined.SupportAgent, "Help & support", onSupport)
            DrawerActionItem(Icons.Outlined.CloudSync, "Hosted prototype sync", onHostedSync)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable { onDarkModeChange(!darkModeEnabled) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.DarkMode, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text("Dark Mode", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = onDarkModeChange,
                    modifier = Modifier.semantics { contentDescription = "Dark Mode" }
                )
            }
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 62.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
                    Text("All active queues", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
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
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 3.dp
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text(queue.doctorName, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(queue.clinicName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${queue.patientName} • ${queue.session}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Token ${queue.patientToken}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                            }
                            HorizontalDivider()
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Current: ${queue.currentToken}", fontWeight = FontWeight.SemiBold)
                                Text(queue.estimatedWaitMinutes?.let { "${it.first}–${it.last} min" } ?: "Wait pending", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(queue.queueStatus.name.lowercase().replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.primary)
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
@Preview(name = "Dark mode", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DarkHomePreview() {
    DoloTheme(darkTheme = true) { PatientHomeScreen(previewHomeState(), darkModeEnabled = true) }
}