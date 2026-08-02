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

### Queue-card order and Dark Mode contrast correction

Patient App 0.30.3-dark-contrast (version code 41) places the queue state immediately below the current token number and moves the Doctor identity panel beneath it in the Currently in Process card.

The Patient UI now uses semantic Material theme colors across Login, OTP, shared cards and navigation, Home, discovery, booking, appointment, notification and diagnostics surfaces. The mobile-number field explicitly defines theme-aware text, placeholder, label, cursor, background, border and disabled colors. Fixed pale cards and dark navy text were replaced where they caused low contrast, while intentional white-on-navy promotional content remains unchanged.

Verification: check Login mobile-number readability, OTP boxes and phone summary, Home queue cards, discovery/booking cards, notifications and diagnostics in both Light and Dark modes. GitHub Actions remains the authoritative compile, lint, unit-test and APK gate.

## 4. Doctor Categories

Status: implemented in `0.31.0-categories-ui` (version code 42); GitHub Actions and physical-device acceptance pending.

Reference: `C:\Users\Poly\Documents\codex\2026-07-11\categories.jpeg`

Implemented:

- responsive two-column specialty grid with 16 required categories and visible Doctor counts
- dedicated premium medical illustrations for Gastroenterology, Pulmonology, Urology, Endocrinology and Oncology; existing approved category artwork is reused where suitable
- compact DO-LO top bar with Back, logo and accessible unread-notification badge
- realistic healthcare hero using the approved local Doctor-and-Patient artwork
- live case-insensitive search across names, health needs and specialty aliases
- search IME action, clear control, focus/keyboard handling and no-result recovery
- loading skeletons, repository empty state, retryable network-error state and disabled-category presentation
- category selection routes with both stable category ID and visible category name
- existing local specialty aliases remain compatible with the current Doctor list
- verified-healthcare-professional information banner
- reusable five-item Patient navigation with Book selected
- merged card semantics, descriptive imagery and responsive small-phone previews
- unit coverage for filtering, aliases, multi-term matching, empty results, category counts and unique IDs

Architecture:

- `DoctorCategoriesUiState` contains render state only.
- `DoctorCategoriesUiEvent` defines search, clear, retry and selection actions.
- `DoctorCategoriesViewModel` owns loading and live filtering.
- `DoctorCategoryRepository` keeps the screen REST-ready; `FakeDoctorCategoryRepository` supplies current prototype data.
- `DoctorCategoriesRoute` owns navigation and only opens available categories.
- `DoctorCategoriesScreen`, `DoctorCategoryCard` and the search/header/status components remain stateless and reusable.

Provider boundary:

- category data is local fake repository data in this checkpoint.
- no Maps, payment, SMS or Push provider was enabled.
- REST replacement can be introduced behind `DoctorCategoryRepository` without redesigning the screen.

Verification:

- run GitHub Actions for compile, lint, unit tests and APK generation.
- install the stable APK and compare the normal categories screen with the reference on a physical Android phone.
- verify live search, clear, back, notifications, all bottom actions, each available category and Light/Dark themes.

### Doctor Categories cardless refinement

Patient App 0.31.1-categories-flat (version code 43) removes the decorative hero and all category/status card backgrounds. Specialty artwork now uses transparent lossless WebP assets and ContentScale.Fit within a consistently sized image area, so illustrations remain fully visible on small phones without cropping.

The search field keeps a transparent container, while loading, empty, error and verification content render directly on the page background. Category selection, accessibility descriptions, unavailable states and REST-ready repository boundaries are unchanged.

The Categories bottom-navigation Home action now explicitly pops back to the existing Home destination and falls back to a single-top Home navigation only when Home is absent from the stack.

## 5. Doctor List

Status: implemented in 0.32.0-doctor-list-ui (version code 44); GitHub Actions and physical-device visual acceptance pending.

Reference: C:\Users\Poly\Documents\codex\2026-07-11\doctor_list.jpeg

Implemented:

- open specialty header with dynamic category names, supporting copy and transparent category artwork
- left-aligned Back plus DO-LO branding, visually centred Doctor List title and accessible notification badge
- live case-insensitive search across Doctor, specialty, qualification, clinic and locality
- Near me and clinic-location placeholders isolated behind Maps-ready callbacks
- horizontally scrollable Sort, Available Now, Fees, Experience and filter-reset controls
- responsive lightweight Doctor items with realistic local portraits, verification, rating, experience, clinic, distance, availability, fee, favourite, Profile and Book Now actions
- compact narrow-phone layout that moves fee and booking below Doctor details
- loading, search-empty, category-empty, filtered-empty, network-error and unavailable states
- hosted Doctor profiles remain discoverable without bypassing the hosted Profile flow
- Request Callback confirmation remains a non-provider prototype boundary
- existing five-item Patient navigation is reused
- previews cover standard, search, no-results, empty-category, unavailable, small-phone, large-font, loading and error states
- unit coverage validates search, sorting, combined filters, category arguments and action IDs

Provider boundary:

- no Maps, calls, SMS, Push or Payment provider was enabled.
- local realistic portraits are packaged as optimised WebP.
- DoctorListRepository and filter/query models are ready for a future paginated REST endpoint.

Verification:

- GitHub Actions must run compile, lint, unit tests and stable APK generation.
- install the APK and verify Cardiology plus at least two other categories.
- verify search, each sort/filter, favourites, Profile, Book Now, notifications, five bottom destinations and callback confirmation.
- test 320 dp layout, increased font size, Light/Dark themes and long Doctor/clinic text.

### Doctor List search-row refinement

Patient App 0.32.1-doctor-list-search (version code 45) gives the Doctor search field and Near me action the same fixed 56 dp height. The visible search placeholder is shortened to `Search`; the screen-reader description remains category-aware.
## 6. Appointment Booking

Status: implemented in 0.33.0-booking-ui (version code 46); GitHub Actions and physical-device visual acceptance pending.

Reference: C:\Users\Poly\Documents\codex\2026-07-11\appointment.png

Implemented:

- reference-led, vertically scrolling Material 3 booking journey with Back, notification badge and reusable Patient bottom navigation
- selected Doctor summary with portrait, verification, specialty, rating, experience, clinic, distance and favourite toggle
- mutually exclusive Self/family visitor selection backed by saved Patient and family profiles
- compact horizontally scrolling clinic choices without photographs; Maps-ready coordinate fields remain provider-neutral
- seven-day selector with unavailable-date treatment and a future date-picker placeholder
- independent Morning and Evening walk-in session cards with token capacity, reporting time and unavailable states
- Patient details derived from the selected visitor rather than duplicated UI values
- configurable fee model, DO-LO service-charge explanation, clinic-only consultation-fee notice and secure-booking panel
- validation, loading, retry, booking-progress, duplicate-tap protection, accessibility descriptions and Light/Dark previews
- fake repository boundary ready for future GET booking-options and POST appointment APIs
- existing local appointment persistence, token allocation, confirmation and live-queue journey remain connected

Provider boundary:

- real Maps, Payments, SMS and Push providers remain disabled.
- consultation fees remain clinic-collected; no Doctor-fee transaction is processed by the Patient app.
- the More-date action is a safe placeholder until server availability rules are authoritative.

Verification:

- GitHub Actions must compile, lint, run unit tests and produce the stable APK.
- install over the accepted Patient App and complete `docs/appointment-booking-device-test.md`.
- capture the populated booking screen and compare it with the source reference before visual acceptance.
### Appointment Booking readability and payment refinement

Patient App 0.33.1-booking-polish (version code 47) simplifies visitor selection to the Patient name plus selection indicator, removes initials avatars from the Who is visiting row, widens all horizontal booking choices and gives each card a stable height. Selected cards now use a subtle translucent teal surface and lighter border instead of a high-contrast fill.

The fee model now excludes the clinic-collected consultation fee from the booking payment. `Total Payable Now` equals the configurable DO-LO service charge after any applicable service-charge discount/tax, while the consultation fee is separately labelled for payment at the clinic. Fee labels and amounts share aligned columns.
### Appointment selection-outline correction

Patient App 0.33.2-selection-outline (version code 48) keeps one neutral 1 dp outline and the same subtle elevation for selected and unselected booking cards. Selection is now communicated by a light primary-container tint, teal text/tick and accessibility state, preventing a stronger border from consuming visible card space.
### Appointment selection-halo correction

Patient App 0.33.3-selection-halo (version code 49) replaces generic clickable indications on booking choices with radio-selection semantics and a deliberately disabled visual press/focus halo. The normal thin card border remains unchanged; selection continues through the light tint, tick and accessible selected state without adding a grey outer ring.