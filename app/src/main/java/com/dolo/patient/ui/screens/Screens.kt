package com.dolo.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.auth.AuthStep
import com.dolo.patient.auth.AuthViewModel
import com.dolo.patient.data.DummyData
import com.dolo.patient.data.model.Doctor
import com.dolo.patient.data.model.Session
import com.dolo.patient.ui.components.*
import com.dolo.patient.ui.theme.DoloBackground
import com.dolo.patient.ui.theme.DoloMint

private val page = Modifier.fillMaxSize().background(DoloBackground).padding(20.dp)

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Box(page, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text("D+", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp)); Text("DO-LO", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your turn. Your time.", color = Color(0xFF63718A))
            Spacer(Modifier.height(44.dp)); PrimaryButton("Get started", onContinue)
        }
    }
}

@Composable
fun LoginScreen(auth: AuthViewModel, onLogin: () -> Unit) {
    val state = auth.uiState
    LaunchedEffect(state.step) { if (state.step == AuthStep.AUTHENTICATED) onLogin() }
    Column(page, verticalArrangement = Arrangement.Center) {
        Text(if (state.step == AuthStep.OTP) "Verify your number" else "Welcome to DO-LO", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(if (state.step == AuthStep.OTP) "Enter the OTP sent to +91 ${state.phone}" else "Book from home and arrive near your turn.", color = Color(0xFF63718A))
        Spacer(Modifier.height(32.dp))
        if (state.step == AuthStep.PHONE) {
            OutlinedTextField(value = state.phone, onValueChange = auth::updatePhone, label = { Text("Mobile number") }, prefix = { Text("+91  ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = state.error != null)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Continue with OTP", auth::requestOtp, state.phone.length == 10 && !state.isLoading)
        } else {
            OutlinedTextField(value = state.otp, onValueChange = auth::updateOtp, label = { Text("6-digit OTP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), isError = state.error != null)
            Spacer(Modifier.height(10.dp))
            Text("Demo OTP: 123456", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Verify and continue", auth::verifyOtp, state.otp.length == 6 && !state.isLoading)
            TextButton(onClick = auth::editPhone, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Change mobile number") }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
        if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
        Spacer(Modifier.height(12.dp))
        Text("Offline Stage 2 demo. Real SMS will be connected later.", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun HomeScreen(onCategories: () -> Unit, onDoctor: (String) -> Unit, onLogout: () -> Unit) {
    LazyColumn(page, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Welcome, Patient", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("How can we help you today?", color = Color(0xFF63718A))
                }
                IconButton(onClick = onLogout) { Icon(Icons.Outlined.Logout, contentDescription = "Log out") }
            }
        }
        item { SearchBar(onClick = onCategories) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Your token", "A-18", Modifier.weight(1f)); MetricCard("Now serving", "A-12", Modifier.weight(1f)) } }
        item { Surface(shape = RoundedCornerShape(20.dp), color = DoloMint.copy(alpha = .14f)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Approximate time to your turn"); Text("~ 35 minutes", fontSize = 24.sp, fontWeight = FontWeight.Bold) } } }
        item { Text("Favourite doctors", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(DummyData.doctors.take(2)) { DoctorCard(it) { onDoctor(it.id) } }
        item { TextButton(onClick = onCategories, modifier = Modifier.fillMaxWidth()) { Text("Browse all categories") } }
    }
}

@Composable
fun CategoriesScreen(onBack: () -> Unit, onSelect: (String) -> Unit) {
    LazyColumn(page, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle("Doctor categories", onBack); Text("Choose the care you need", color = Color(0xFF63718A)) }
        items(DummyData.categories.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    Surface(Modifier.weight(1f).height(140.dp).clickable { onSelect(category.name) }, shape = RoundedCornerShape(22.dp), color = Color.White) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) { Text(category.symbol, fontSize = 34.sp, color = MaterialTheme.colorScheme.primary); Text(category.name, fontWeight = FontWeight.SemiBold) }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DoctorListScreen(category: String, onBack: () -> Unit, onBook: (String) -> Unit) {
    val doctors = DummyData.doctors.filter { it.specialty == category }.ifEmpty { DummyData.doctors }
    LazyColumn(page, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenTitle(category.ifBlank { "Doctors" }, onBack); SearchBar("Search in this category") {} }
        items(doctors) { DoctorCard(it) { onBook(it.id) } }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, onBook: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White, tonalElevation = 1.dp) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) { Text("Dr", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(doctor.specialty, color = MaterialTheme.colorScheme.primary); Text(doctor.clinic, fontSize = 13.sp, color = Color.Gray) }
            }
            Spacer(Modifier.height(14.dp)); Text("★ ${doctor.rating}  •  ${doctor.experienceYears} years  •  ₹${doctor.consultationFee}")
            Spacer(Modifier.height(12.dp)); PrimaryButton("Book walk-in", onBook)
        }
    }
}

@Composable
fun BookingScreen(doctorId: String, onBack: () -> Unit, onConfirm: (String, Session) -> Unit) {
    val doctor = DummyData.doctors.firstOrNull { it.id == doctorId } ?: DummyData.doctors.first()
    var session by remember { mutableStateOf(Session.MORNING) }
    Column(page) {
        ScreenTitle("Book walk-in", onBack); Spacer(Modifier.height(20.dp)); DoctorCard(doctor) {}
        Spacer(Modifier.height(24.dp)); Text("Choose session", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Tokens are called in queue order; this is not a fixed time slot.", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Session.entries.forEach { item -> FilterChip(selected = session == item, onClick = { session = item }, label = { Text(if (item == Session.MORNING) "Morning" else "Evening") }, modifier = Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(20.dp)); Surface(shape = RoundedCornerShape(18.dp), color = Color.White) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Today", fontWeight = FontWeight.Bold); Text(if (session == Session.MORNING) "9:00 AM – 1:00 PM" else "5:00 PM – 9:00 PM"); Text("Estimated next token: A-18", color = MaterialTheme.colorScheme.primary) } }
        Spacer(Modifier.weight(1f)); PrimaryButton("Confirm booking", onClick = { onConfirm(doctor.id, session) })
    }
}

@Composable
fun ConfirmationScreen(doctorId: String, session: String, onDone: () -> Unit) {
    val doctor = DummyData.doctors.firstOrNull { it.id == doctorId } ?: DummyData.doctors.first()
    Column(page, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(82.dp).background(DoloMint, CircleShape), contentAlignment = Alignment.Center) { Text("✓", color = Color.White, fontSize = 42.sp) }
        Spacer(Modifier.height(20.dp)); Text("Booking confirmed", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Your walk-in token is allotted", color = Color.Gray)
        Spacer(Modifier.height(24.dp)); Surface(shape = RoundedCornerShape(24.dp), color = Color.White) { Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("TOKEN NUMBER", fontSize = 12.sp, color = Color.Gray); Text("A-18", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary); HorizontalDivider(Modifier.padding(vertical = 16.dp)); Text(doctor.name, fontWeight = FontWeight.Bold); Text("${session.lowercase().replaceFirstChar(Char::uppercase)} session"); Text(doctor.clinic, textAlign = TextAlign.Center, color = Color.Gray) } }
        Spacer(Modifier.height(28.dp)); PrimaryButton("Go to home", onDone)
    }
}

