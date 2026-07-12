package com.dolo.patient.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dolo.patient.auth.*
import com.dolo.patient.data.*
import com.dolo.patient.ui.screens.*

object Routes { const val Splash="splash";const val Login="login";const val Home="home";const val Categories="categories";const val Doctors="doctors/{category}";const val Booking="booking/{doctorId}";const val Confirmation="confirmation/{doctorId}/{session}" }

@Composable fun DoloPatientApp(authRepository:AuthRepository,patientRepository:PatientRepository){
 val nav=rememberNavController();val auth:AuthViewModel=viewModel(factory=AuthViewModelFactory(authRepository));val patient:PatientViewModel=viewModel(factory=PatientViewModelFactory(patientRepository))
 NavHost(nav,startDestination=Routes.Splash){
  composable(Routes.Splash){SplashScreen{nav.navigate(if(auth.uiState.step==AuthStep.AUTHENTICATED)Routes.Home else Routes.Login){popUpTo(Routes.Splash){inclusive=true}}}}
  composable(Routes.Login){LoginScreen(auth){nav.navigate(Routes.Home){popUpTo(Routes.Login){inclusive=true}}}}
  composable(Routes.Home){HomeScreen({nav.navigate(Routes.Categories)},{nav.navigate("booking/"+it)},{auth.logout();nav.navigate(Routes.Login){popUpTo(Routes.Home){inclusive=true}}},patient.uiState.active)}
  composable(Routes.Categories){CategoriesScreen(nav::popBackStack){nav.navigate("doctors/"+it)}}
  composable(Routes.Doctors,arguments=listOf(navArgument("category"){type=NavType.StringType})){e->DoctorListScreen(e.arguments?.getString("category").orEmpty(),nav::popBackStack){nav.navigate("booking/"+it)}}
  composable(Routes.Booking,arguments=listOf(navArgument("doctorId"){type=NavType.StringType})){e->BookingScreen(e.arguments?.getString("doctorId").orEmpty(),nav::popBackStack){id,s->patient.book(id,s);nav.navigate("confirmation/"+id+"/"+s.name)}}
  composable(Routes.Confirmation){e->ConfirmationScreen(e.arguments?.getString("doctorId").orEmpty(),e.arguments?.getString("session").orEmpty(),patient.uiState.active){nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}}}
 }
}
