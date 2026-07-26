package com.dolo.patient.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.ui.theme.*

@Composable
fun BrandLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    val size = if (compact) 22.sp else 29.sp
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "DO-LO Patient" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DO-", color = DoloNavy, fontSize = size, fontWeight = FontWeight.ExtraBold)
        Text("LO", color = DoloTeal, fontSize = size, fontWeight = FontWeight.ExtraBold)
        Surface(color = DoloTeal, shape = CircleShape, modifier = Modifier.padding(start = 3.dp).size(if (compact) 14.dp else 17.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
        }
    }
}

@Composable
fun ScreenTitle(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder),
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back", tint = DoloNavy) }
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        BrandLogo(compact = true)
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(actionLabel) }
    }
}

@Composable
fun SearchBar(text: String = "Search doctors, clinics, specialties...", onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).semantics { contentDescription = "Search doctors, clinics and specialties" }.clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder),
        shadowElevation = 3.dp
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = DoloTeal, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(11.dp))
            Text(text, color = DoloMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ArrowForward, null, tint = DoloMuted, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier, accent: Color = DoloTeal) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "$label: $value" },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), color = DoloMuted, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.displaySmall, color = accent)
        }
    }
}

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).semantics { contentDescription = label },
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DoloTeal, disabledContainerColor = DoloBorder, disabledContentColor = DoloMuted),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, enabled: Boolean = true, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder)
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DoloCard(modifier: Modifier = Modifier, containerColor: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder.copy(alpha = 0.8f)),
        shadowElevation = 3.dp
    ) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) }
}

@Composable
fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 84.dp).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(11.dp), color = DoloSurfaceAlt, modifier = Modifier.size(36.dp)) {
                Icon(icon, null, tint = DoloTeal, modifier = Modifier.padding(8.dp))
            }
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 2)
        }
    }
}

@Composable
fun StatusBadge(label: String, color: Color = DoloTeal) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color, style = MaterialTheme.typography.labelMedium)
    }
}

enum class PatientBottomDestination { HOME, APPOINTMENTS, BOOK }

@Composable
fun DoloBottomBar(selected: PatientBottomDestination, onHome: () -> Unit, onAppointments: () -> Unit, onBook: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 10.dp, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().heightIn(min = 66.dp).padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(Icons.Outlined.Home, "Home", selected == PatientBottomDestination.HOME, onHome, Modifier.weight(1f))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(52.dp).semantics { contentDescription = "Book appointment" }.clickable(role = Role.Button, onClick = onBook),
                    shape = RoundedCornerShape(18.dp),
                    color = DoloTeal,
                    shadowElevation = 5.dp
                ) { Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.padding(13.dp)) }
                Text("Book", style = MaterialTheme.typography.labelMedium, color = if (selected == PatientBottomDestination.BOOK) DoloTeal else DoloMuted)
            }
            BottomItem(Icons.Outlined.CalendarMonth, "Appointments", selected == PatientBottomDestination.APPOINTMENTS, onAppointments, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color by animateColorAsState(if (selected) DoloTeal else DoloMuted, label = "bottom-color")
    val lift by animateDpAsState(if (selected) 2.dp else 0.dp, label = "bottom-lift")
    Column(
        modifier = modifier.heightIn(min = 56.dp).padding(bottom = lift).semantics(mergeDescendants = true) { contentDescription = label + if (selected) ", selected" else "" }.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}
