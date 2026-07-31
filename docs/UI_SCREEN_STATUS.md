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

Status: stabilized in `0.29.2-otp-stability` (version code 37); GitHub Actions and physical-device acceptance pending.

Reference: `C:\Users\Poly\Documents\codex\2026-07-11\OTP.png`

Implemented:

- reference-led Compose layout with a dedicated female-Patient healthcare hero asset and no floating illustration icons
- phone-number summary with Edit and Android back navigation to login
- six reusable visual OTP digit boxes backed by one stable input focus
- one stable numeric input renders six visual digit boxes, preserving backspace, paste and Done handling without per-digit focus or vertical movement
- countdown placed beside Resend OTP; expired codes remain editable and show an error only after verification
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
## 3. Patient Home

Status: implemented in 0.30.0-home-ui (version code 38); GitHub Actions and physical-device acceptance pending.

Reference: C:\Users\Poly\Documents\codex\2026-07-11\home.jpeg

Implemented:

- fixed left-aligned menu and DO-LO logo top bar with an accessible unread-notification badge
- responsive greeting card with a lightweight original Patient-at-home hero image
- doctor search and Maps-ready Near me placeholder action
- reusable patient-token and current-token cards backed by local and hosted queue data
- patient name, Doctor name, estimated waiting range, queue state and multiple-active-queue indicator
- dedicated All Queues route for current and upcoming local/hosted bookings
- swipeable, dismissible Admin Broadcast carousel with action callbacks, indicators and loading/empty states
- horizontally scrolling favourite-Doctor cards with reusable booking/profile actions
- fixed five-item Patient bottom navigation with the emphasized centre Book action
- functional navigation drawer for profile, favourites, support, hosted sync and sign out
- loading, empty, paused, error, multiple-queue, no-broadcast and no-favourite states
- normal, loading, no-token, paused, multiple-queue, no-broadcast, no-favourite, network-error, small-phone and large-font previews
- content descriptions, headings, merged card semantics and minimum touch targets

Architecture:

- PatientHomeUiState, QueueSummaryUiModel, BroadcastUiModel and FavoriteDoctorUiModel keep data out of leaf composables.
- PatientHomeViewModel maps the existing local and authoritative hosted data into presentation models and owns broadcast dismissal.
- PatientHomeRoute owns refresh timing, navigation events and drawer state.
- PatientHomeScreen is stateless and split into reusable top bar, greeting, search, queue, broadcast, favourite and bottom-navigation components.
- Login and OTP implementation were not modified.

Assets:

- patient_home_hero.webp is an original app-specific Patient-at-home lifestyle asset.
- admin_broadcast_megaphone.webp is an original lightweight 3D broadcast asset.
- favourite-Doctor imagery uses the existing local specialty artwork as an explicit placeholder until final Doctor portraits are supplied.

Provider boundary:

- Near me remains a local placeholder; Maps is not enabled.
- Broadcast data uses authoritative Admin communications when available and local dummy content otherwise.
- SMS, Push and payment providers remain disabled.

Verification:

- git diff --check and source-structure checks passed.
- This lightweight checkout has no Gradle wrapper, local Gradle or local Java installation; GitHub Actions remains the authoritative compile, lint, unit-test and APK gate.
### Patient Home layout and theme refinement

Patient App 0.30.1-home-polish (version code 39) applies the first physical-layout refinement to the new Home screen:

- greeting content is aligned to the top-left
- the decorative waving-hand icon is removed
- Patient names remain on one horizontal line with adaptive font sizing
- greeting card height is reduced from 148 dp to 112 dp, approximately 25 percent
- search occupies approximately 75 percent of its row
- Near me is now an unboxed text action
- View all moved from the token card to the Live appointment status heading
- a persistent Dark Mode switch is available from the Home navigation drawer
- Home surfaces, text, outlines, queue cards and navigation now consume Material theme colors in both modes
- the DO-LO logo adapts its navy text to the active theme

Dark Mode is stored locally in the existing private app preferences. No profile, appointment, family or hosted data is changed or uploaded.
### Patient Home artwork and queue-card correction

Patient App 0.30.2-home-card-fix (version code 40) strengthens the Patient-at-home greeting artwork with improved contrast, color and sharpness. The illustration now renders at full opacity, while the Home gradient preserves text readability without hiding the artwork in Dark Mode.

The Your Token and Currently in Process cards now share the tallest intrinsic row height. Both cards remain equal in size when Patient names, Doctor names or status content require additional vertical space.

Verification: confirm artwork visibility in Light and Dark modes, then test both queue cards with short and long Patient and Doctor names. GitHub Actions remains the authoritative Android compile, lint, unit-test and APK gate for this lightweight checkout.
