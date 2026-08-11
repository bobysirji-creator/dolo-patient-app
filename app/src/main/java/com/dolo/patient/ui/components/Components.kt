package com.dolo.patient.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
        Text("DO-", color = MaterialTheme.colorScheme.onSurface, fontSize = size, fontWeight = FontWeight.ExtraBold)
        Text("LO", color = MaterialTheme.colorScheme.primary, fontSize = size, fontWeight = FontWeight.ExtraBold)
        Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.padding(start = 3.dp).size(if (compact) 14.dp else 17.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(2.dp))
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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
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
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 3.dp
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(11.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier, accent: Color = Color.Unspecified) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "$label: $value" },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.displaySmall, color = if (accent == Color.Unspecified) MaterialTheme.colorScheme.primary else accent)
        }
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).semantics { contentDescription = label },
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge)
        if (!loading) {
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, enabled: Boolean = true, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DoloCard(modifier: Modifier = Modifier, containerColor: Color = Color.Unspecified, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (containerColor == Color.Unspecified) MaterialTheme.colorScheme.surface else containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 3.dp
    ) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) }
}

@Composable
fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 84.dp).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
            }
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 2)
        }
    }
}

@Composable
fun StatusBadge(label: String, color: Color = Color.Unspecified) {
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    Surface(shape = RoundedCornerShape(50), color = resolvedColor.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = resolvedColor, style = MaterialTheme.typography.labelMedium)
    }
}

enum class PatientBottomDestination { HOME, BOOK, APPOINTMENTS }

@Composable
fun DoloBottomBar(
    selected: PatientBottomDestination,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    NavigationBar(
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = selected == PatientBottomDestination.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", maxLines = 1) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == PatientBottomDestination.BOOK,
            onClick = onBook,
            icon = { Icon(Icons.Outlined.Add, contentDescription = "Book appointment") },
            label = { Text("Book", maxLines = 1) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == PatientBottomDestination.APPOINTMENTS,
            onClick = onAppointments,
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Appointments") },
            label = { Text("Appointments", maxLines = 1) },
            colors = itemColors
        )
    }
}