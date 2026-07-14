package com.dolo.patient.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.ui.theme.*

private val gradient = Brush.horizontalGradient(
    listOf(DoloTeal, Color(0xFF03B3A8))
)

@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "DO-LO Patient"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DO-", color = DoloNavy, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text("LO", color = DoloTeal, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "+",
            color = DoloTeal,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}

@Composable
fun ScreenTitle(title: String, onBack: (() -> Unit)? = null) {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                color = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "Back from " + title,
                        tint = DoloNavy
                    )
                }
            }
        }
        BrandLogo()
    }
}

@Composable
fun SearchBar(
    text: String = "Search doctors, clinics, specialties...",
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .semantics { contentDescription = "Search doctors, clinics and specialties" }
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = DoloMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(13.dp))
            Text(text, color = DoloMuted, fontSize = 15.sp)
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = DoloTeal
) {
    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = label + ": " + value
            },
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(label.uppercase(), color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(9.dp))
            Text("Token Number", color = DoloMuted, fontSize = 13.sp)
            Text(value, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .background(
                brush = if (enabled) {
                    gradient
                } else {
                    Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray))
                },
                shape = RoundedCornerShape(22.dp)
            )
            .semantics { contentDescription = label }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

enum class PatientBottomDestination {
    HOME,
    APPOINTMENTS,
    BOOK
}

@Composable
fun DoloBottomBar(
    selected: PatientBottomDestination,
    onHome: () -> Unit,
    onAppointments: () -> Unit,
    onBook: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = selected == PatientBottomDestination.HOME,
                onClick = onHome
            )
            BottomItem(
                icon = Icons.Outlined.CalendarMonth,
                label = "Appointments",
                selected = selected == PatientBottomDestination.APPOINTMENTS,
                onClick = onAppointments
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .sizeIn(minWidth = 56.dp, minHeight = 56.dp)
                        .semantics { contentDescription = "Book appointment" }
                        .clickable(role = Role.Button, onClick = onBook),
                    shape = CircleShape,
                    color = DoloTeal,
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(15.dp).size(28.dp)
                    )
                }
                Text(
                    "Book",
                    fontSize = 10.sp,
                    color = DoloTeal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .sizeIn(minWidth = 72.dp, minHeight = 56.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = label + if (selected) ", selected" else ""
            }
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) DoloTeal else DoloMuted
        )
        Text(
            label,
            fontSize = 10.sp,
            color = if (selected) DoloTeal else DoloMuted
        )
    }
}
