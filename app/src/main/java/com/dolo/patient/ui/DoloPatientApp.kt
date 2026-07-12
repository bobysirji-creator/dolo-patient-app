package com.dolo.patient.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dolo.patient.ui.screens.*

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Home = "home"
    const val Categories = "categories"
    const val Doctors = "doctors/{category}"
    const val Booking = "booking/{doctorId}"
    const val Confirmation = "confirmation/{doctorId}/{session}"
}

@Composable
fun DoloPatientApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Splash) {
        composable(Routes.Splash) { SplashScreen { nav.navigate(Routes.Login) { popUpTo(Routes.Splash) { inclusive = true } } } }
        composable(Routes.Login) { LoginScreen { nav.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } } } }
        composable(Routes.Home) { HomeScreen(onCategories = { nav.navigate(Routes.Categories) }, onDoctor = { nav.navigate("booking/$it") }) }
        composable(Routes.Categories) { CategoriesScreen(onBack = nav::popBackStack, onSelect = { nav.navigate("doctors/$it") }) }
        composable(Routes.Doctors, arguments = listOf(navArgument("category") { type = NavType.StringType })) { entry ->
            DoctorListScreen(entry.arguments?.getString("category").orEmpty(), nav::popBackStack) { nav.navigate("booking/$it") }
        }
        composable(Routes.Booking, arguments = listOf(navArgument("doctorId") { type = NavType.StringType })) { entry ->
            BookingScreen(entry.arguments?.getString("doctorId").orEmpty(), nav::popBackStack) { id, session -> nav.navigate("confirmation/$id/${session.name}") }
        }
        composable(Routes.Confirmation) { entry ->
            ConfirmationScreen(entry.arguments?.getString("doctorId").orEmpty(), entry.arguments?.getString("session").orEmpty()) {
                nav.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
            }
        }
    }
}

