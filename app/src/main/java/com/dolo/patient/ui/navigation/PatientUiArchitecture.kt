package com.dolo.patient.ui.navigation

data class PatientFeatureGroup(val title:String,val destinations:List<String>)

object PatientUiArchitecture {
    val primaryDestinations = listOf("Home", "Book", "Appointments")
    val accountGroups = listOf(
        PatientFeatureGroup("Your care", listOf("Profile & family", "Favourite doctors", "Notifications")),
        PatientFeatureGroup("Help", listOf("Help & support", "App status & diagnostics"))
    )
    const val prototypeControlsOnPrimaryNavigation = false
}
