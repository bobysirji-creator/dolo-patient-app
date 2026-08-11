package com.dolo.patient.ui.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
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
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.home.DoloPatientBottomNavigation
import com.dolo.patient.ui.home.PatientBottomItem
import com.dolo.patient.ui.theme.DoloCoral
import com.dolo.patient.ui.theme.DoloTheme

@Composable
fun DoctorCategoriesRoute(
    notificationCount: Int,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onCategorySelected: (DoctorCategoryUiModel) -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    viewModel: DoctorCategoriesViewModel = viewModel(factory = DoctorCategoriesViewModelFactory())
) {
    val state by viewModel.uiState
    DoctorCategoriesScreen(
        state = state,
        notificationCount = notificationCount,
        onBack = onBack,
        onNotifications = onNotifications,
        onEvent = { event ->
            viewModel.onEvent(event)
            if (event is DoctorCategoriesUiEvent.CategorySelected) {
                viewModel.findCategory(event.categoryId)
                    ?.takeIf(DoctorCategoryUiModel::isAvailable)
                    ?.let(onCategorySelected)
            }
        },
        onHome = onHome,
        onAppointments = onAppointments,
        onBook = onBook,
        onHistory = onHistory,
        onProfile = onProfile
    )
}

@Composable
fun DoctorCategoriesScreen(
    state: DoctorCategoriesUiState,
    notificationCount: Int,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onEvent: (DoctorCategoriesUiEvent) -> Unit,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DoctorCategoriesTopBar(
                notificationCount = notificationCount,
                onBack = onBack,
                onNotifications = onNotifications
            )
        },
        bottomBar = {
            DoloPatientBottomNavigation(
                selected = PatientBottomItem.BOOK,
                onHome = onHome,
                onAppointments = onAppointments,
                onBook = onBook
            )
        }
    ) { scaffoldPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(scaffoldPadding).fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item(span = { GridItemSpan(maxLineSpan) }) {
                CategorySearchField(
                    query = state.query,
                    onQueryChanged = { onEvent(DoctorCategoriesUiEvent.SearchChanged(it)) },
                    onClear = { onEvent(DoctorCategoriesUiEvent.ClearSearch) },
                    focusManager = focusManager,
                    onSearch = {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.query.isBlank()) "Doctor Categories" else "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).semantics { heading() }
                    )
                    if (!state.isLoading && state.errorMessage == null) {
                        Text(
                            text = "${state.visibleCategories.size} specialties",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when {
                state.isLoading -> items(6, key = { "loading-$it" }) { LoadingCategoryCard() }
                state.errorMessage != null -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoriesMessageCard(
                        title = "Unable to load doctor categories",
                        message = state.errorMessage,
                        actionLabel = "Try again",
                        onAction = { onEvent(DoctorCategoriesUiEvent.Retry) }
                    )
                }
                state.isEmpty -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoriesMessageCard(
                        title = "Doctor categories are currently unavailable",
                        message = "Please try again later."
                    )
                }
                state.hasNoSearchResults -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoriesMessageCard(
                        title = "No categories found",
                        message = "Try searching with another specialty or health concern.",
                        actionLabel = "Clear search",
                        onAction = { onEvent(DoctorCategoriesUiEvent.ClearSearch) }
                    )
                }
                else -> items(state.visibleCategories, key = DoctorCategoryUiModel::id) { category ->
                    DoctorCategoryCard(
                        category = category,
                        onClick = { onEvent(DoctorCategoriesUiEvent.CategorySelected(category.id)) }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                VerificationBanner()
            }
        }
    }
}

@Composable
private fun DoctorCategoriesTopBar(
    notificationCount: Int,
    onBack: () -> Unit,
    onNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Go back" }) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = null)
        }
        BrandLogo(modifier = Modifier.weight(1f), compact = true)
        IconButton(onClick = onNotifications) {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = DoloCoral) {
                            Text(if (notificationCount > 9) "9+" else notificationCount.toString())
                        }
                    }
                }
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        }
    }
}

@Composable
private fun CategorySearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    focusManager: FocusManager,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search doctor categories" },
        placeholder = { Text("Search category...") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onClear(); focusManager.clearFocus() }) {
                    Icon(Icons.Outlined.Clear, contentDescription = "Clear category search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun DoctorCategoryCard(
    category: DoctorCategoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp)
            .alpha(if (category.isAvailable) 1f else 0.58f)
            .semantics(mergeDescendants = true) {
                contentDescription = "${category.contentDescription}. ${category.name}, ${category.doctorCount} doctors available"
                if (!category.isAvailable) disabled()
            }
            .clickable(
                enabled = category.isAvailable,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(category.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            if (category.isAvailable) "${category.doctorCount} Doctors" else "Coming soon",
            style = MaterialTheme.typography.bodySmall,
            color = if (category.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingCategoryCard() {
    Column(
        modifier = Modifier.fillMaxWidth().height(178.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        Spacer(Modifier.height(10.dp))
        Text("Loading specialty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategoriesMessageCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (actionLabel == "Try again") Icons.Outlined.Refresh else Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
        )
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun VerificationBanner() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.HealthAndSafety,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "All doctors are verified and experienced professionals.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Your health is our priority.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val noOp = {}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategoriesDefaultPreview() {
    DoloTheme {
        DoctorCategoriesScreen(
            state = DoctorCategoriesUiState(categories = DoctorCategoryCatalog.categories, visibleCategories = DoctorCategoryCatalog.categories, isLoading = false),
            notificationCount = 3,
            onBack = noOp,
            onNotifications = noOp,
            onEvent = {},
            onHome = noOp,
            onAppointments = noOp,
            onBook = noOp,
            onHistory = noOp,
            onProfile = noOp
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 720)
@Composable
private fun CategoriesLoadingPreview() {
    DoloTheme {
        DoctorCategoriesScreen(DoctorCategoriesUiState(), 0, noOp, noOp, {}, noOp, noOp, noOp, noOp, noOp)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun CategoriesErrorPreview() {
    DoloTheme {
        DoctorCategoriesScreen(
            DoctorCategoriesUiState(isLoading = false, errorMessage = "Network unavailable. Check your connection and try again."),
            1,
            noOp,
            noOp,
            {},
            noOp,
            noOp,
            noOp,
            noOp,
            noOp
        )
    }
}
