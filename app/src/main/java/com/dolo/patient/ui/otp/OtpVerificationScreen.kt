package com.dolo.patient.ui.otp

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.patient.R
import com.dolo.patient.auth.PatientSession
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.components.DoloCard
import com.dolo.patient.ui.components.PrimaryButton
import com.dolo.patient.ui.theme.DoloBackground
import com.dolo.patient.ui.theme.DoloBorder
import com.dolo.patient.ui.theme.DoloMuted
import com.dolo.patient.ui.theme.DoloNavy
import com.dolo.patient.ui.theme.DoloSurfaceAlt
import com.dolo.patient.ui.theme.DoloTeal
import com.dolo.patient.ui.theme.DoloTheme

@Composable
fun OtpVerificationRoute(
    viewModel: OtpVerificationViewModel,
    phoneNumber: String,
    onEditNumber: () -> Unit,
    onVerified: (PatientSession) -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(phoneNumber) {
        viewModel.start(phoneNumber)
    }
    LaunchedEffect(state.verifiedSession) {
        state.verifiedSession?.let { session ->
            viewModel.consumeVerifiedSession()
            onVerified(session)
        }
    }
    LaunchedEffect(state.editRequested) {
        if (state.editRequested) {
            viewModel.consumeEditRequest()
            onEditNumber()
        }
    }

    BackHandler {
        viewModel.onEvent(OtpVerificationUiEvent.EditNumber)
    }

    OtpVerificationScreen(
        uiState = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun OtpVerificationScreen(
    uiState: OtpVerificationUiState,
    onEvent: (OtpVerificationUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMessage = when (uiState.error) {
        OtpError.Incomplete -> stringResource(R.string.otp_incomplete_error)
        OtpError.Invalid -> stringResource(R.string.otp_invalid_error)
        OtpError.Expired -> stringResource(R.string.otp_expired_error)
        OtpError.Network -> stringResource(R.string.otp_network_error)
        OtpError.TooManyAttempts -> stringResource(R.string.otp_too_many_error)
        null -> null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DoloBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    BrandLogo()
                    Text(
                        text = stringResource(R.string.login_brand_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoloMuted
                    )
                }
            }

            item {
                OtpHero()
            }

            item {
                DoloCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            color = DoloSurfaceAlt,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HealthAndSafety,
                                contentDescription = null,
                                tint = DoloTeal,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.otp_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = DoloNavy
                            )
                            Text(
                                text = stringResource(R.string.otp_supporting),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DoloMuted
                            )
                        }
                    }

                    PhoneSummary(
                        phoneNumber = uiState.phoneNumber,
                        onEdit = {
                            onEvent(OtpVerificationUiEvent.EditNumber)
                        }
                    )

                    OtpInputField(
                        value = uiState.otp,
                        enabled = !uiState.isExpired && !uiState.isVerifying && !uiState.isResending,
                        isError = uiState.error != null,
                        onDigitChanged = { index, value ->
                            onEvent(OtpVerificationUiEvent.DigitChanged(index, value))
                        },
                        onPaste = { value ->
                            onEvent(OtpVerificationUiEvent.OtpPasted(value))
                        },
                        onBackspace = { index ->
                            onEvent(OtpVerificationUiEvent.Backspace(index))
                        },
                        onSubmit = {
                            onEvent(OtpVerificationUiEvent.Verify)
                        }
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    OtpCountdownTimer(
                        secondsRemaining = uiState.secondsRemaining,
                        expired = uiState.isExpired
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.otp_did_not_receive),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DoloMuted
                        )
                        TextButton(
                            onClick = {
                                onEvent(OtpVerificationUiEvent.Resend)
                            },
                            enabled = uiState.canResend
                        ) {
                            if (uiState.isResending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(17.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(7.dp))
                            }
                            Text(
                                text = if (uiState.isResending) {
                                    stringResource(R.string.otp_resending)
                                } else {
                                    stringResource(R.string.otp_resend)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.confirmationMessageVisible) {
                        Text(
                            text = stringResource(R.string.otp_sent_successfully),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodySmall,
                            color = DoloTeal,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    SecurityInfoCard()

                    PrimaryButton(
                        label = if (uiState.isVerifying) {
                            stringResource(R.string.otp_verifying)
                        } else {
                            stringResource(R.string.otp_verify)
                        },
                        onClick = {
                            onEvent(OtpVerificationUiEvent.Verify)
                        },
                        enabled = uiState.canVerify,
                        loading = uiState.isVerifying
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 158.dp, max = 220.dp)
            .aspectRatio(1.55f)
            .clip(RoundedCornerShape(26.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.login_healthcare_hero),
            contentDescription = stringResource(R.string.otp_illustration_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        FloatingHealthIcon(
            icon = Icons.Outlined.CalendarMonth,
            description = "Appointment calendar",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )
        FloatingHealthIcon(
            icon = Icons.Outlined.Schedule,
            description = "Appointment time",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
        FloatingHealthIcon(
            icon = Icons.Outlined.Lock,
            description = "Secure verification",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
    }
}

@Composable
private fun FloatingHealthIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(38.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 3.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = DoloTeal,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun PhoneSummary(
    phoneNumber: String,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "OTP sent to ${formatIndianPhone(phoneNumber)}"
            },
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = DoloSurfaceAlt,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = DoloNavy,
                    modifier = Modifier.padding(9.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Text(
                text = formatIndianPhone(phoneNumber),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = DoloNavy
            )
            TextButton(onClick = onEdit) {
                Text(
                    text = stringResource(R.string.otp_edit),
                    color = DoloTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpInputField(
    value: String,
    enabled: Boolean,
    isError: Boolean,
    onDigitChanged: (Int, String) -> Unit,
    onPaste: (String) -> Unit,
    onBackspace: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val requesters = remember {
        List(OtpRules.OTP_LENGTH) { FocusRequester() }
    }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        requesters.first().requestFocus()
    }
    LaunchedEffect(value) {
        if (value.length in 1 until OtpRules.OTP_LENGTH) {
            requesters[value.length].requestFocus()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(OtpRules.OTP_LENGTH) { index ->
            OtpDigitBox(
                value = value.getOrNull(index)?.toString().orEmpty(),
                index = index,
                enabled = enabled,
                isError = isError,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(requesters[index]),
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit)
                    when {
                        digits.length > 1 -> {
                            onPaste(digits)
                            focusManager.clearFocus()
                        }
                        digits.isEmpty() -> {
                            onBackspace(index)
                            if (index > 0) requesters[index - 1].requestFocus()
                        }
                        else -> {
                            onDigitChanged(index, digits)
                            if (index < OtpRules.OTP_LENGTH - 1) {
                                requesters[index + 1].requestFocus()
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                    }
                },
                onBackspace = {
                    onBackspace(index)
                    if (index > 0) requesters[index - 1].requestFocus()
                },
                onSubmit = onSubmit
            )
        }
    }
}

@Composable
fun OtpDigitBox(
    value: String,
    index: Int,
    enabled: Boolean,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        focused -> DoloTeal
        else -> DoloBorder
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = DoloNavy,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = if (index == OtpRules.OTP_LENGTH - 1) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        modifier = modifier
            .heightIn(min = 56.dp)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                    onBackspace()
                    true
                } else {
                    false
                }
            }
            .semantics {
                contentDescription = "OTP digit ${index + 1} of ${OtpRules.OTP_LENGTH}"
            },
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(15.dp),
                color = if (enabled) Color.White else DoloBackground,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (focused || isError) 2.dp else 1.dp,
                    color = borderColor
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun OtpCountdownTimer(
    secondsRemaining: Int,
    expired: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (expired) {
                    "OTP expired"
                } else {
                    "OTP expires in ${OtpRules.countdown(secondsRemaining)}"
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = DoloBorder)
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = if (expired) MaterialTheme.colorScheme.error else DoloTeal,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (expired) {
                stringResource(R.string.otp_expired)
            } else {
                stringResource(
                    R.string.otp_expires_in,
                    OtpRules.countdown(secondsRemaining)
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (expired) MaterialTheme.colorScheme.error else DoloMuted,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(Modifier.weight(1f), color = DoloBorder)
    }
}

@Composable
fun SecurityInfoCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Secure and encrypted sign-in. We never share your information."
            },
        color = DoloSurfaceAlt,
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DoloTeal.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Outlined.HealthAndSafety,
                    contentDescription = null,
                    tint = DoloTeal,
                    modifier = Modifier.padding(9.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(
                    text = stringResource(R.string.otp_security_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = DoloNavy
                )
                Text(
                    text = stringResource(R.string.otp_security_supporting),
                    style = MaterialTheme.typography.bodySmall,
                    color = DoloMuted
                )
            }
        }
    }
}

private fun formatIndianPhone(phoneNumber: String): String {
    val digits = phoneNumber.filter(Char::isDigit).takeLast(10)
    return if (digits.length == 10) {
        "+91 ${digits.take(5)} ${digits.takeLast(5)}"
    } else {
        "+91 $digits"
    }
}

private val previewPhone = "9876543210"

@Preview(name = "OTP empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpEmptyPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone))

@Preview(name = "OTP partial", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpPartialPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123"))

@Preview(name = "OTP complete", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpCompletePreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123456"))

@Preview(name = "OTP invalid", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpInvalidPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "654321", error = OtpError.Invalid))

@Preview(name = "OTP expired", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpExpiredPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123456", secondsRemaining = 0, isExpired = true, error = OtpError.Expired))

@Preview(name = "OTP resending", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpResendingPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, secondsRemaining = 0, isExpired = true, isResending = true))

@Preview(name = "OTP verifying", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpVerifyingPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123456", isVerifying = true))

@Preview(name = "OTP network error", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OtpNetworkPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123456", error = OtpError.Network))

@Preview(name = "OTP small phone", showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun OtpSmallPhonePreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "12"))

@Preview(name = "OTP large text", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun OtpLargeTextPreview() = OtpPreview(OtpVerificationUiState(phoneNumber = previewPhone, otp = "123456"))

@Composable
private fun OtpPreview(state: OtpVerificationUiState) {
    DoloTheme {
        OtpVerificationScreen(
            uiState = state,
            onEvent = {}
        )
    }
}
