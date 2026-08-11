package com.dolo.patient.ui.doctors

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dolo.patient.R
import com.dolo.patient.data.PatientUiState
import com.dolo.patient.location.ApproximateLocationProvider
import com.dolo.patient.location.ApproximateLocationResult
import com.dolo.patient.location.ClinicNavigation
import com.dolo.patient.platform.*
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.home.DoloPatientBottomNavigation
import com.dolo.patient.ui.home.PatientBottomItem
import com.dolo.patient.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DoctorListRoute(
    categoryId: String,
    categoryName: String,
    patientState: PatientUiState,
    platformState: PlatformConnectionState,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onDoctorSelected: (String) -> Unit,
    onHostedDoctorSelected: (String) -> Unit,
    onBookNow: (String) -> Unit,
    onRefreshHosted: () -> Unit,
    onFindNearby: (Double, Double) -> Unit,
    onFavourite: (String) -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    viewModel: DoctorListViewModel = viewModel(
        key = "doctor-list-" + categoryId,
        factory = DoctorListViewModelFactory(categoryId, categoryName)
    )
) {
    val state by viewModel.uiState
    val clinicSource = if (state.isNearbyMode) platformState.nearbyClinics else platformState.clinics
    val hostedDoctors = if (platformState.status == PlatformConnectionStatus.CONNECTED) {
        clinicSource
            .filter { clinic ->
                categoryId.equals("all", ignoreCase = true) ||
                    PlatformDiscovery.matches(clinic, categoryName, "") ||
                    hostedSpecialtyForCategory(categoryId)?.let { PlatformDiscovery.matches(clinic, it, "") } == true
            }
            .map(PlatformClinic::toDoctorListItem)
    } else emptyList()

    LaunchedEffect(hostedDoctors) { viewModel.updateHostedDoctors(hostedDoctors) }
    LaunchedEffect(patientState.favouriteIds) { viewModel.syncFavourites(patientState.favouriteIds) }
    LaunchedEffect(categoryId) { onRefreshHosted() }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var nearbyRequestStarted by remember { mutableStateOf(false) }
    val handleLocationResult: (ApproximateLocationResult) -> Unit = { result ->
        when (result) {
            is ApproximateLocationResult.Available -> {
                nearbyRequestStarted = true
                viewModel.onEvent(DoctorListUiEvent.SortChanged(DoctorSortOption.DISTANCE))
                viewModel.activateNearbyMode()
                onFindNearby(result.latitude, result.longitude)
            }
            is ApproximateLocationResult.Unavailable -> scope.launch {
                snackbar.showSnackbar(result.message)
            }
        }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) ApproximateLocationProvider.request(context, handleLocationResult)
        else scope.launch { snackbar.showSnackbar("Location permission was not granted. You can still search by city.") }
    }
    LaunchedEffect(platformState.isFindingNearby, platformState.message) {
        if (nearbyRequestStarted && !platformState.isFindingNearby) {
            nearbyRequestStarted = false
            snackbar.showSnackbar(platformState.message)
        }
    }

    var callbackDialog by remember { mutableStateOf(false) }

    DoctorListScreen(
        state = state.copy(notificationCount = patientState.notifications.count { !it.isRead }),
        snackbarHostState = snackbar,
        onEvent = { event ->
            viewModel.onEvent(event)
            when (event) {
                DoctorListUiEvent.BackClicked -> onBack()
                DoctorListUiEvent.NotificationsClicked -> onNotifications()
                is DoctorListUiEvent.DoctorSelected -> state.doctors.firstOrNull { it.id == event.doctorId }?.let {
                    if (it.isHostedProfile) onHostedDoctorSelected(it.id) else onDoctorSelected(it.id)
                }
                is DoctorListUiEvent.BookNowClicked -> state.doctors.firstOrNull { it.id == event.doctorId }?.let {
                    if (it.isHostedProfile) onHostedDoctorSelected(it.id) else onBookNow(it.id)
                }
                is DoctorListUiEvent.FavouriteClicked -> onFavourite(event.doctorId)
                DoctorListUiEvent.NearMeClicked -> {
                    if (ApproximateLocationProvider.hasPermission(context)) {
                        ApproximateLocationProvider.request(context, handleLocationResult)
                    } else {
                        locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }
                is DoctorListUiEvent.ClinicLocationClicked -> {
                    val doctor = state.doctors.firstOrNull { it.id == event.doctorId }
                    val latitude = doctor?.latitude
                    val longitude = doctor?.longitude
                    when {
                        latitude == null || longitude == null -> scope.launch {
                            snackbar.showSnackbar("Navigation is unavailable because this clinic has no verified coordinates.")
                        }
                        !ClinicNavigation.open(context, latitude, longitude) -> scope.launch {
                            snackbar.showSnackbar("No compatible maps app or browser was found.")
                        }
                    }
                }
                DoctorListUiEvent.RequestCallbackClicked -> callbackDialog = true
                DoctorListUiEvent.Retry, DoctorListUiEvent.Refresh -> onRefreshHosted()
                else -> Unit
            }
        },
        onHome = onHome,
        onAppointments = onAppointments,
        onBook = onBook,
        onHistory = onHistory,
        onProfile = onProfile
    )

    if (callbackDialog) {
        AlertDialog(
            onDismissRequest = { callbackDialog = false },
            icon = { Icon(Icons.Outlined.SupportAgent, contentDescription = null) },
            title = { Text("Callback request noted") },
            text = { Text("This prototype does not contact a real support service. The callback workflow is ready for a future API.") },
            confirmButton = { TextButton(onClick = { callbackDialog = false }) { Text("Got it") } }
        )
    }
}

private fun hostedSpecialtyForCategory(categoryId: String): String? = when (categoryId.lowercase()) {
    "general-physician", "general" -> "General Physician"
    "pediatrics", "child" -> "Pediatrician"
    "dermatology", "skin" -> "Dermatologist"
    "gynecology", "gyn" -> "Gynecologist"
    "orthopedics", "ortho" -> "Orthopedic"
    "cardiology", "cardio" -> "Cardiologist"
    "ent" -> "ENT Specialist"
    "ophthalmology", "eye" -> "Ophthalmologist"
    "dentistry", "dental" -> "Dentist"
    "psychiatry", "mental" -> "Psychiatrist"
    "neurology", "neuro" -> "Neurologist"
    else -> null
}

private fun PlatformClinic.toDoctorListItem(): DoctorListItemUiModel =
    DoctorListItemUiModel(
        id = id,
        name = doctorName,
        qualifications = qualification.ifBlank { "Qualification verified by DO-LO" },
        specialty = specialty,
        imageRes = R.drawable.doctor_rohan,
        rating = publishedRatingAverage ?: 0.0,
        reviewCount = publishedReviewCount,
        experienceYears = experienceYears,
        clinicName = name,
        clinicAddress = listOf(addressLine, city, pincode).filter(String::isNotBlank).joinToString(", "),
        distanceKm = distanceMeters?.div(1_000.0),
        latitude = latitude,
        longitude = longitude,
        consultationFee = consultationFeeMinor / 100,
        availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.AVAILABLE_TODAY, "Available Today"),
        isHostedProfile = true
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen(
    state: DoctorListUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (DoctorListUiEvent) -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DoloDoctorListTopBar(
                notificationCount = state.notificationCount,
                onBack = { onEvent(DoctorListUiEvent.BackClicked) },
                onNotifications = { onEvent(DoctorListUiEvent.NotificationsClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            DoloPatientBottomNavigation(
                selected = PatientBottomItem.BOOK,
                onHome = onHome,
                onAppointments = onAppointments,
                onBook = onBook
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(DoctorListUiEvent.Refresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            state.category?.let { category ->
                item { DoctorCategoryHeader(category) }
                item {
                    DoctorSearchSection(
                        query = state.searchQuery,
                        category = category,
                        onSearchChanged = { onEvent(DoctorListUiEvent.SearchChanged(it)) },
                        onClear = { onEvent(DoctorListUiEvent.ClearSearch) },
                        onNearMe = { onEvent(DoctorListUiEvent.NearMeClicked) }
                    )
                }
                item {
                    DoctorFilterRow(
                        selectedSort = state.selectedSort,
                        filters = state.filters,
                        onSort = { onEvent(DoctorListUiEvent.SortChanged(it)) },
                        onFilters = { onEvent(DoctorListUiEvent.FiltersChanged(it)) }
                    )
                }
            }
            when {
                state.isLoading -> items(4) { DoctorListLoadingItem() }
                state.errorMessage != null -> item {
                    DoctorListMessage(Icons.Outlined.Refresh, "Unable to load Doctors", state.errorMessage, "Retry") {
                        onEvent(DoctorListUiEvent.Retry)
                    }
                }
                state.hasNoSearchResults -> item {
                    DoctorListMessage(Icons.Outlined.Search, "No Doctors found", "Try another name, clinic, or search term.", "Clear Search") {
                        onEvent(DoctorListUiEvent.ClearSearch)
                    }
                }
                state.hasNoDoctors -> item {
                    if (state.isNearbyMode) {
                        DoctorListMessage(
                            Icons.Outlined.LocationOff,
                            "No hosted clinics found within 50 km",
                            "Your location may be outside the current prototype clinic area.",
                            "Show all hosted clinics"
                        ) { onEvent(DoctorListUiEvent.ShowAllHostedClicked) }
                    } else {
                        DoctorListMessage(Icons.Outlined.MedicalServices, "No Doctors are currently available in this category", "Please check again later or explore another specialty.")
                }
                    }
                state.hasFilteredEmptyState -> item {
                    DoctorListMessage(Icons.Outlined.FilterList, "No Doctors match these filters", "Remove one or more filters to see more Doctors.", "Clear Filters") {
                        onEvent(DoctorListUiEvent.FiltersChanged(DoctorFilterState()))
                    }
                }
                else -> items(state.filteredDoctors, key = DoctorListItemUiModel::id) { doctor ->
                    DoctorListItem(
                        doctor = doctor,
                        favouriteUpdating = state.favouriteUpdateDoctorId == doctor.id,
                        onOpen = { onEvent(DoctorListUiEvent.DoctorSelected(doctor.id)) },
                        onFavourite = { onEvent(DoctorListUiEvent.FavouriteClicked(doctor.id)) },
                        onBook = { onEvent(DoctorListUiEvent.BookNowClicked(doctor.id)) },
                        onLocation = { onEvent(DoctorListUiEvent.ClinicLocationClicked(doctor.id)) }
                    )
                }
            }
                item { DoctorListAssistanceSection { onEvent(DoctorListUiEvent.RequestCallbackClicked) } }
            }
        }
    }
}

@Composable
fun DoloDoctorListTopBar(notificationCount: Int, onBack: () -> Unit, onNotifications: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back to Doctor Categories")
            }
            BrandLogo(compact = true)
        }
        Text(
            "Doctor List",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center).semantics { heading() }
        )
        IconButton(onClick = onNotifications, modifier = Modifier.align(Alignment.CenterEnd).size(48.dp)) {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = DoloCoral) { Text(notificationCount.coerceAtMost(99).toString()) }
                    }
                }
            ) { Icon(Icons.Outlined.Notifications, contentDescription = "Notifications") }
        }
    }
}

@Composable
fun DoctorCategoryHeader(category: DoctorCategoryHeaderUiModel) {
    Row(Modifier.fillMaxWidth().heightIn(min = 128.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(category.iconRes), null, Modifier.size(46.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                category.pluralDisplayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(4.dp))
            Text(category.supportingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        category.illustrationRes?.let {
            Image(
                painterResource(it),
                category.categoryName + " medical illustration",
                Modifier.size(width = 112.dp, height = 104.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Composable
fun DoctorSearchSection(
    query: String,
    category: DoctorCategoryHeaderUiModel,
    onSearchChanged: (String) -> Unit,
    onClear: () -> Unit,
    onNearMe: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onSearchChanged,
            modifier = Modifier.weight(1f).height(56.dp).semantics {
                contentDescription = "Search " + category.pluralDisplayName + " and clinics"
            },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onClear(); focusManager.clearFocus() }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear Doctor search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                focusManager.clearFocus()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        OutlinedButton(
            onClick = onNearMe,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(5.dp))
            Text("Near me", maxLines = 1)
        }
    }
}

@Composable
fun DoctorFilterRow(
    selectedSort: DoctorSortOption,
    filters: DoctorFilterState,
    onSort: (DoctorSortOption) -> Unit,
    onFilters: (DoctorFilterState) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 1.dp)) {
        item {
            DoctorMenuChip(
                label = if (selectedSort == DoctorSortOption.RECOMMENDED) "Sort" else selectedSort.label,
                selected = selectedSort != DoctorSortOption.RECOMMENDED,
                options = DoctorSortOption.entries.map { it.label },
                onSelected = { label -> DoctorSortOption.entries.firstOrNull { it.label == label }?.let(onSort) }
            )
        }
        item {
            FilterChip(
                selected = filters.availability == DoctorAvailabilityType.AVAILABLE_NOW,
                onClick = {
                    onFilters(
                        filters.copy(
                            availability = if (filters.availability == DoctorAvailabilityType.AVAILABLE_NOW) null
                            else DoctorAvailabilityType.AVAILABLE_NOW
                        )
                    )
                },
                label = { Text("Available Now") },
                leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(18.dp)) }
            )
        }
        item {
            DoctorMenuChip(
                label = if (filters.maximumFee == null) "Fees" else "Up to Rs " + filters.maximumFee,
                selected = filters.minimumFee != null || filters.maximumFee != null,
                options = listOf("Any fee", "Under Rs 500", "Rs 500 - Rs 700", "Rs 700 - Rs 1,000", "Above Rs 1,000"),
                onSelected = {
                    onFilters(
                        when (it) {
                            "Under Rs 500" -> filters.copy(minimumFee = null, maximumFee = 499)
                            "Rs 500 - Rs 700" -> filters.copy(minimumFee = 500, maximumFee = 700)
                            "Rs 700 - Rs 1,000" -> filters.copy(minimumFee = 700, maximumFee = 1000)
                            "Above Rs 1,000" -> filters.copy(minimumFee = 1001, maximumFee = null)
                            else -> filters.copy(minimumFee = null, maximumFee = null)
                        }
                    )
                }
            )
        }
        item {
            DoctorMenuChip(
                label = if (filters.minimumExperience == null) "Experience" else filters.minimumExperience.toString() + "+ years",
                selected = filters.minimumExperience != null,
                options = listOf("Any experience", "5+ years", "10+ years", "15+ years"),
                onSelected = {
                    onFilters(filters.copy(minimumExperience = it.substringBefore('+').toIntOrNull()))
                }
            )
        }
        item {
            FilterChip(
                selected = filters.isActive,
                onClick = { onFilters(DoctorFilterState()) },
                label = { Text(if (filters.isActive) "Clear Filters" else "More Filters") },
                leadingIcon = { Icon(Icons.Outlined.FilterList, null, Modifier.size(18.dp)) }
            )
        }
    }
}

@Composable
private fun DoctorMenuChip(
    label: String,
    selected: Boolean,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(label, maxLines = 1) },
            trailingIcon = { Icon(Icons.Outlined.ExpandMore, null, Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { expanded = false; onSelected(option) }
                )
            }
        }
    }
}

@Composable
fun DoctorListItem(
    doctor: DoctorListItemUiModel,
    favouriteUpdating: Boolean,
    onOpen: () -> Unit,
    onFavourite: () -> Unit,
    onBook: () -> Unit,
    onLocation: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = doctor.accessibleSummary }
            .clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(12.dp)) {
            if (maxWidth < 330.dp) {
                DoctorCompactLayout(doctor, favouriteUpdating, onFavourite, onBook, onLocation)
            } else {
                DoctorWideLayout(doctor, favouriteUpdating, onFavourite, onBook, onLocation)
            }
        }
    }
}

@Composable
private fun DoctorWideLayout(
    doctor: DoctorListItemUiModel,
    favouriteUpdating: Boolean,
    onFavourite: () -> Unit,
    onBook: () -> Unit,
    onLocation: () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        DoctorPortraitColumn(doctor, favouriteUpdating, onFavourite)
        Spacer(Modifier.width(10.dp))
        DoctorInformation(doctor, onLocation, Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        DoctorFeeSection(doctor, onBook, Modifier.widthIn(min = 104.dp, max = 122.dp))
    }
}

@Composable
private fun DoctorCompactLayout(
    doctor: DoctorListItemUiModel,
    favouriteUpdating: Boolean,
    onFavourite: () -> Unit,
    onBook: () -> Unit,
    onLocation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            DoctorPortraitColumn(doctor, favouriteUpdating, onFavourite)
            Spacer(Modifier.width(10.dp))
            DoctorInformation(doctor, onLocation, Modifier.weight(1f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            DoctorAvailabilityBadge(doctor.availability)
            Spacer(Modifier.weight(1f))
            Text("\u20B9 " + doctor.consultationFee, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBook,
                enabled = doctor.availability.type != DoctorAvailabilityType.NOT_AVAILABLE,
                shape = RoundedCornerShape(13.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) { Text("Book Now") }
        }
    }
}

@Composable
private fun DoctorPortraitColumn(
    doctor: DoctorListItemUiModel,
    favouriteUpdating: Boolean,
    onFavourite: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onFavourite,
            enabled = !favouriteUpdating,
            modifier = Modifier.size(48.dp).semantics {
                contentDescription = (if (doctor.isFavourite) "Remove " else "Add ") + doctor.name +
                    (if (doctor.isFavourite) " from favourites" else " to favourites")
            }
        ) {
            if (favouriteUpdating) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    if (doctor.isFavourite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    null,
                    tint = if (doctor.isFavourite) DoloCoral else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box {
            if (doctor.imageRes != null) {
                Image(
                    painterResource(doctor.imageRes),
                    "Portrait of " + doctor.name,
                    Modifier.size(78.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(Modifier.size(78.dp), CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Icon(Icons.Outlined.MedicalServices, null, Modifier.padding(19.dp))
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).size(18.dp),
                shape = CircleShape,
                color = if (doctor.availability.type == DoctorAvailabilityType.NOT_AVAILABLE) MaterialTheme.colorScheme.outline else DoloSuccess,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface)
            ) {}
        }
    }
}
@Composable
private fun DoctorInformation(
    doctor: DoctorListItemUiModel,
    onLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                doctor.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (doctor.isVerified) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Verified Doctor",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(doctor.qualifications, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(doctor.specialty, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Star, null, Modifier.size(17.dp), tint = DoloWarning)
            Text(" " + doctor.rating + " (" + doctor.reviewCount + ")", style = MaterialTheme.typography.bodySmall)
        }
        Text(doctor.experienceYears.toString() + "+ Years Experience", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onLocation),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.LocationOn, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Column {
                Text(doctor.clinicName + ", " + doctor.clinicAddress, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                doctor.distanceKm?.let {
                    Text(it.toString() + " km away", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DoctorAvailabilityBadge(availability: DoctorAvailabilityUiModel) {
    val color = when (availability.type) {
        DoctorAvailabilityType.AVAILABLE_NOW -> DoloSuccess
        DoctorAvailabilityType.AVAILABLE_SOON -> MaterialTheme.colorScheme.primary
        DoctorAvailabilityType.AVAILABLE_TODAY -> DoloWarning
        DoctorAvailabilityType.NOT_AVAILABLE -> MaterialTheme.colorScheme.error
    }
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.1f)) {
        Text(
            availability.label,
            Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DoctorFeeSection(
    doctor: DoctorListItemUiModel,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DoctorAvailabilityBadge(doctor.availability)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\u20B9 " + doctor.consultationFee, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Consultation Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Button(
            onClick = onBook,
            enabled = doctor.availability.type != DoctorAvailabilityType.NOT_AVAILABLE,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics {
                contentDescription = "Book walk-in appointment with " + doctor.name
            },
            shape = RoundedCornerShape(13.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
        ) { Text("Book Now", maxLines = 1) }
    }
}

@Composable
private fun DoctorListLoadingItem() {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 150.dp).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(Modifier.size(30.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text("Loading available Doctors", style = MaterialTheme.typography.titleMedium)
            Text("Checking clinics and sessions...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DoctorListMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun DoctorListAssistanceSection(onRequestCallback: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            if (maxWidth < 360.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.SupportAgent, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Can't find the right Doctor?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("We will help you find the perfect specialist.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = onRequestCallback,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(13.dp)
                    ) { Text("Request Callback") }
                }
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SupportAgent, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Can't find the right Doctor?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("We will help you find the perfect specialist.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onRequestCallback, shape = RoundedCornerShape(13.dp)) {
                        Text("Request Callback")
                    }
                }
            }
        }
    }
}

private val previewNoOp = {}

@Preview(showBackground = true, widthDp = 430, heightDp = 900)
@Composable
private fun DoctorListStandardPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                doctors = DoctorListCatalog.cardiologists,
                filteredDoctors = DoctorListCatalog.cardiologists,
                notificationCount = 3,
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 760)
@Composable
private fun DoctorListSmallPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                doctors = DoctorListCatalog.cardiologists.take(2),
                filteredDoctors = DoctorListCatalog.cardiologists.take(2),
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, fontScale = 1.3f)
@Composable
private fun DoctorListLargeTextPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                doctors = DoctorListCatalog.cardiologists.take(1),
                filteredDoctors = DoctorListCatalog.cardiologists.take(1).map { it.copy(isFavourite = true) },
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Search results")
@Composable
private fun DoctorListSearchPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                searchQuery = "Anjali",
                doctors = DoctorListCatalog.cardiologists,
                filteredDoctors = DoctorListCatalog.cardiologists.filter { it.id == "4" },
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "No search results")
@Composable
private fun DoctorListNoSearchResultsPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                searchQuery = "Unknown clinic",
                doctors = DoctorListCatalog.cardiologists,
                filteredDoctors = emptyList(),
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "No Doctors")
@Composable
private fun DoctorListEmptyCategoryPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Doctor unavailable")
@Composable
private fun DoctorUnavailablePreview() {
    val unavailable = DoctorListCatalog.cardiologists.first().copy(
        availability = DoctorAvailabilityUiModel(DoctorAvailabilityType.NOT_AVAILABLE, "Not Available")
    )
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                doctors = listOf(unavailable),
                filteredDoctors = listOf(unavailable),
                isLoading = false
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun DoctorListLoadingPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology")),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun DoctorListErrorPreview() {
    DoloTheme {
        DoctorListScreen(
            state = DoctorListUiState(
                category = DoctorCategoryHeaders.resolve("cardiology", "Cardiology"),
                isLoading = false,
                errorMessage = "Network unavailable. Check your connection and try again."
            ),
            onEvent = {},
            onHome = previewNoOp,
            onAppointments = previewNoOp,
            onBook = previewNoOp,
            onHistory = previewNoOp,
            onProfile = previewNoOp
        )
    }
}