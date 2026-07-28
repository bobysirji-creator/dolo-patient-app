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

## Next screen

2. OTP verification — not redesigned in this change.