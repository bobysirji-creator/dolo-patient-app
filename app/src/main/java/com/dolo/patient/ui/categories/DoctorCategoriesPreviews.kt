package com.dolo.patient.ui.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dolo.patient.ui.theme.DoloTheme

private val previewNoOp = {}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Search result")
@Composable
private fun CategoriesSearchResultPreview() {
    val results = DoctorCategorySearch.filter(DoctorCategoryCatalog.categories, "heart")
    PreviewScreen(
        DoctorCategoriesUiState(
            query = "heart",
            categories = DoctorCategoryCatalog.categories,
            visibleCategories = results,
            isLoading = false
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "No search results")
@Composable
private fun CategoriesNoResultsPreview() {
    PreviewScreen(
        DoctorCategoriesUiState(
            query = "unknown",
            categories = DoctorCategoryCatalog.categories,
            visibleCategories = emptyList(),
            isLoading = false
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Empty repository")
@Composable
private fun CategoriesEmptyPreview() {
    PreviewScreen(DoctorCategoriesUiState(isLoading = false))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Unavailable category")
@Composable
private fun CategoriesUnavailablePreview() {
    val disabled = DoctorCategoryCatalog.categories.take(4).mapIndexed { index, category ->
        if (index == 1) category.copy(isAvailable = false) else category
    }
    PreviewScreen(
        DoctorCategoriesUiState(categories = disabled, visibleCategories = disabled, isLoading = false)
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640, name = "Small phone")
@Composable
private fun CategoriesSmallPhonePreview() {
    PreviewScreen(
        DoctorCategoriesUiState(
            categories = DoctorCategoryCatalog.categories,
            visibleCategories = DoctorCategoryCatalog.categories,
            isLoading = false
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f, name = "Large font")
@Composable
private fun CategoriesLargeFontPreview() {
    PreviewScreen(
        DoctorCategoriesUiState(
            categories = DoctorCategoryCatalog.categories,
            visibleCategories = DoctorCategoryCatalog.categories,
            isLoading = false
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
private fun CategoriesDarkPreview() {
    DoloTheme(darkTheme = true) {
        CategoriesPreviewContent(
            DoctorCategoriesUiState(
                categories = DoctorCategoryCatalog.categories,
                visibleCategories = DoctorCategoryCatalog.categories,
                isLoading = false
            )
        )
    }
}

@Composable
private fun PreviewScreen(state: DoctorCategoriesUiState) {
    DoloTheme { CategoriesPreviewContent(state) }
}

@Composable
private fun CategoriesPreviewContent(state: DoctorCategoriesUiState) {
    DoctorCategoriesScreen(
        state = state,
        notificationCount = 3,
        onBack = previewNoOp,
        onNotifications = previewNoOp,
        onEvent = {},
        onHome = previewNoOp,
        onAppointments = previewNoOp,
        onBook = previewNoOp,
        onHistory = previewNoOp,
        onProfile = previewNoOp
    )
}
