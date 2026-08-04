package com.dolo.patient.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.dolo.patient.R
import com.dolo.patient.ui.components.BrandLogo
import com.dolo.patient.ui.components.DoloCard
import com.dolo.patient.ui.components.PrimaryButton
import com.dolo.patient.ui.theme.DoloTheme

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onOtpRequested: (String) -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(state.otpRequestedFor) {
        state.otpRequestedFor?.let { phone ->
            viewModel.consumeOtpNavigation()
            onOtpRequested(phone)
        }
    }

    LoginScreen(
        uiState = state,
        onPhoneNumberChange = viewModel::updatePhoneNumber,
        onSendOtp = viewModel::sendOtp,
        onCreateAccount = viewModel::showAccountCreationNotice,
        onOpenLegalDocument = viewModel::openLegalDocument,
        onCloseLegalDocument = viewModel::closeLegalDocument,
        onAcknowledgeLegalDocument = viewModel::simulateLegalAcknowledgement
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onCreateAccount: () -> Unit,
    onOpenLegalDocument: (String) -> Unit = {},
    onCloseLegalDocument: () -> Unit = {},
    onAcknowledgeLegalDocument: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val invalidMessage = stringResource(R.string.login_invalid_phone)
    val networkMessage = stringResource(R.string.login_network_error)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    BrandLogo()
                    Text(
                        text = stringResource(R.string.login_brand_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.login_welcome_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.login_welcome_supporting),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Image(
                    painter = painterResource(R.drawable.login_healthcare_hero),
                    contentDescription = stringResource(R.string.login_illustration_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 156.dp, max = 230.dp)
                        .aspectRatio(1.55f)
                        .clip(RoundedCornerShape(26.dp))
                )
            }

            item {
                DoloCard {
                    Text(
                        text = stringResource(R.string.login_card_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    PhoneNumberField(
                        value = uiState.phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        isError = uiState.error == LoginError.InvalidPhone,
                        errorMessage = if (uiState.error == LoginError.InvalidPhone) {
                            invalidMessage
                        } else {
                            null
                        },
                        enabled = !uiState.isLoading,
                        onDone = onSendOtp
                    )

                    if (uiState.error == LoginError.Network) {
                        LoginMessage(
                            message = networkMessage,
                            isError = true
                        )
                    }

                    PrimaryButton(
                        label = if (uiState.isLoading) {
                            stringResource(R.string.login_sending_otp)
                        } else {
                            stringResource(R.string.login_send_otp)
                        },
                        onClick = onSendOtp,
                        enabled = !uiState.isLoading,
                        loading = uiState.isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.login_secure_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = stringResource(R.string.login_or),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.login_new_user),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onCreateAccount) {
                            Text(
                                text = stringResource(R.string.login_create_account),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.accountCreationNoticeVisible) {
                        val registrationMessage = when (uiState.registrationStatus) {
                            RegistrationReadinessStatus.Checking ->
                                stringResource(R.string.login_registration_checking)
                            RegistrationReadinessStatus.ActivationGatesBlocked -> {
                                val gateList = uiState.activationRequirements?.gates
                                    ?.joinToString("\n") { "- ${activationGateLabel(it.key)}" }
                                    .orEmpty()
                                val consentList = uiState.consentCatalog?.requirements
                                    ?.joinToString("\n") {
                                        "- ${consentCategoryLabel(it.category)}: reserved, not published"
                                    }
                                    .orEmpty()
                                stringResource(
                                    R.string.login_registration_activation_blocked,
                                    gateList,
                                    uiState.enrollmentReadiness?.publicIdPolicy?.format
                                        ?: "DLO-PAT-NNNNNN",
                                    consentList
                                )
                            }
                            RegistrationReadinessStatus.Unavailable ->
                                stringResource(R.string.login_registration_status_unavailable)
                        }
                        LoginMessage(
                            message = registrationMessage,
                            isError = uiState.registrationStatus ==
                                RegistrationReadinessStatus.Unavailable
                        )
                        uiState.legalDocumentPreview?.let { preview ->
                            LegalPreviewLinks(
                                preview = preview,
                                acknowledgedCategories = uiState.simulatedAcknowledgements,
                                onOpenDocument = onOpenLegalDocument
                            )
                        }
                    }
                }
            }
        }
    }
    uiState.selectedLegalDocument?.let { document ->
        LegalDocumentPreviewDialog(
            document = document,
            acknowledged = document.category in uiState.simulatedAcknowledgements,
            onDismiss = onCloseLegalDocument,
            onAcknowledge = { onAcknowledgeLegalDocument(document.category) }
        )
    }
}

@Composable
fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    enabled: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val description = stringResource(R.string.login_phone_content_description)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = description },
        enabled = enabled,
        isError = isError,
        singleLine = true,
        label = { Text(stringResource(R.string.login_phone_label)) },
        placeholder = { Text(stringResource(R.string.login_phone_placeholder)) },
        prefix = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_country_code),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
            }
        },
        supportingText = errorMessage?.let { message ->
            {
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            }
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun LoginMessage(message: String, isError: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Start
        )
    }
}

@Preview(name = "Login default", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginDefaultPreview() {
    DoloTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onPhoneNumberChange = {},
            onSendOtp = {},
            onCreateAccount = {}
        )
    }
}

@Preview(name = "Login loading", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginLoadingPreview() {
    DoloTheme {
        LoginScreen(
            uiState = LoginUiState(
                phoneNumber = "9876543210",
                isLoading = true
            ),
            onPhoneNumberChange = {},
            onSendOtp = {},
            onCreateAccount = {}
        )
    }
}

@Preview(name = "Login invalid number", showBackground = true, widthDp = 320, heightDp = 720)
@Composable
private fun LoginErrorPreview() {
    DoloTheme {
        LoginScreen(
            uiState = LoginUiState(
                phoneNumber = "98765",
                error = LoginError.InvalidPhone
            ),
            onPhoneNumberChange = {},
            onSendOtp = {},
            onCreateAccount = {}
        )
    }
}
@Preview(name = "Login network error", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginNetworkErrorPreview() {
    DoloTheme {
        LoginScreen(
            uiState = LoginUiState(
                phoneNumber = "9876543210",
                error = LoginError.Network
            ),
            onPhoneNumberChange = {},
            onSendOtp = {},
            onCreateAccount = {}
        )
    }
}