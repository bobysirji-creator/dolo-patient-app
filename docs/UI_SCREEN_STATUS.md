# DO-LO Patient UI Screen Status

This file tracks the screenshot-led Compose modernization one screen at a time. Reference screenshots guide appearance only; screens are built from responsive, reusable Compose components.

## Shared design foundation

Status: available from Stage 45AB.

- Material 3 teal/navy palette, typography and rounded shapes
- reusable DO-LO logo, primary/secondary buttons, cards and status elements
- responsive safe-area handling and minimum touch targets

## 1. Mobile-number login

Status: implemented in `0.28.0-login-ui` (version code 34); GitHub Actions and physical-device acceptance pending.

Reference: `C:\Users\Poly\Documents\codex\2026-07-11\login.png`

Implemented:

- DO-LO logo and tagline
- original lightweight doctor-and-Patient healthcare hero asset
- welcome heading and supporting text
- reusable Indian `+91` `PhoneNumberField`
- 10-digit normalization and validation
- mocked Send OTP request through the existing `AuthRepository`
- loading, invalid-number and future network-error states
- secure-login reassurance
- account-creation link with an honest production-registration notice
- numeric phone keyboard, Done action, focus control and IME-safe scrolling
- screen-reader descriptions and polite error announcements
- width-constrained, scrollable layout for small phones and larger displays
- default, loading, invalid-number and network-error Compose previews
- unit coverage for valid, invalid and loading state rules

Architecture:

- `LoginUiState` contains render state only.
- `LoginViewModel` validates input and owns the mocked OTP request.
- `LoginRoute` converts successful OTP-request state into navigation.
- `LoginScreen` and `PhoneNumberField` are stateless reusable composables.
- The existing OTP verification screen is preserved and remains the next UI-modernization task.

Provider boundary:

- SMS delivery remains mocked/disabled.
- No Maps, payment or Push integration was added.
- The screen is ready for a future `AuthRepository.requestOtp` implementation without changing its UI contract.

Verification:

- source structure and delimiter checks passed
- `git diff --check` passed
- local Android compilation is unavailable because this lightweight checkout has no Gradle wrapper or local JDK/Gradle installation
- the repository GitHub Actions workflow remains the authoritative compile, lint, unit-test and APK check

## 2. OTP verification

Status: implemented in `0.29.0-otp-ui` (version code 35); GitHub Actions and physical-device acceptance pending.

Reference: `C:\Users\Poly\Documents\codex\2026-07-11\OTP.png`

Implemented:

- reference-led Compose layout using the existing lightweight healthcare hero asset
- phone-number summary with Edit and Android back navigation to login
- six reusable, individually focused OTP digit boxes
- numeric password keyboard, focus advance, backspace, paste and Done handling
- 45-second countdown, expiration state and resend flow
- incomplete, invalid, expired, network and too-many-attempt error states
- loading and disabled states for verification and resend actions
- secure-sign-in information card and polite accessibility announcements
- safe-area, keyboard-aware scrolling and width constraints for small and large phones
- previews for empty, partial, complete, invalid, expired, resending, verifying, network-error, small-screen and large-font states
- unit coverage for OTP input, countdown and action eligibility

Architecture:

- `OtpVerificationUiState` contains render and action-eligibility state.
- `OtpVerificationUiEvent` defines all user interactions.
- `OtpVerificationViewModel` owns countdown, validation, retry and async verification state.
- `OtpRepository` keeps the UI ready for a real provider while adapting the current `AuthRepository`.
- `OtpVerificationRoute` owns navigation effects; `OtpVerificationScreen` remains stateless.

Provider boundary:

- OTP delivery remains mocked and the accepted demo code remains `123456`.
- No SMS provider, payment, maps or push integration was enabled.
- Existing hosted/local session behavior is preserved after successful verification.

Next: Patient home dashboard redesign.