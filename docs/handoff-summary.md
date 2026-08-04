# Stage 1 Handoff

## Completed

Created a lean, single-module Kotlin/Jetpack Compose Patient App with a consistent blue, navy, mint and soft-background theme. Navigation connects splash, mobile login, patient home, doctor categories, doctor list, walk-in booking and token confirmation. Reusable cards/buttons and dummy doctor/category models support the prototype.

## Deliberately not implemented

- Real OTP, backend/API calls or authentication persistence
- Real doctor search, booking or token allocation
- Live queue transport
- Favourites/reviews persistence
- Payments, maps, SMS or push providers
- Doctor App or Admin App

## Low-resource Windows setup

1. Install Android Studio with Android SDK 35 and JDK 17, or use an existing installation.
2. Open this repository root; do not create additional app modules.
3. Allow Gradle sync to download dependencies once. The project caps Gradle at 1536 MB and disables parallel execution.
4. If SDK 35 is unavailable, install it from SDK Manager. Create `local.properties` automatically by opening in Android Studio.
5. Prefer a physical Android device over an emulator: enable Developer options and USB debugging, connect USB, approve the computer, then select the device and Run `app`.
6. For command-line validation after Gradle wrapper generation: `gradlew.bat :app:assembleDebug` and `gradlew.bat :app:installDebug`.

## Important limitation

The repository does not include a generated Gradle wrapper JAR because it is a binary artifact. If `gradle` is installed, run `gradle wrapper --gradle-version 8.9`; otherwise Android Studio can sync the project and its Gradle tooling can generate the wrapper.

## Next recommended task

Stage 2: introduce feature ViewModels/repositories, proper phone validation, OTP request/verify interfaces, session persistence, and unit testsâ€”using a fake provider until the backend/API contract is agreed.



## Stage 2

The Patient App now uses an AuthViewModel and provider-neutral AuthRepository. A FakeAuthRepository simulates OTP verification locally and stores only the demo phone session in private SharedPreferences.

Demo login:

- Enter any valid 10-digit mobile number.
- Use OTP 123456.
- The session survives app restarts.
- Use the logout icon on the home screen to clear the session.

No SMS is sent and no backend, access token, medical data, map, payment, or push provider is connected. The next stage should define the REST/authentication contract and secure token storage before integrating a real OTP provider.


## Stage 3

The Patient App now has API-ready PatientApi, ApiResult, PatientRepository and PatientViewModel boundaries. LocalPatientRepository provides the offline implementation and persists encoded appointments in private SharedPreferences.

A confirmed booking now creates a unique booking ID, stores doctor/clinic/date/session data, generates a token, updates the active appointment, and survives process restarts. Home and confirmation screens read the active persisted booking rather than relying only on fixed token data.

The next stage should introduce an HTTPS backend implementation of the existing contracts, environment-specific base URLs, and secure server-issued token storage. No real patient medical data should be stored until that security layer is complete.


## Stage 4 handoff

Stage 4 is complete. The Patient App now has search/category filtering, a doctor detail screen, persistent favourites, appointment history, and a view-details-before-booking flow. Data remains local through PatientRepository so a REST-backed implementation can replace it without redesigning screens.

Validated build: https://github.com/bobysirji-creator/dolo-patient-app/actions/runs/29201170208

Recommended next stage: live queue state and appointment lifecycle simulation, including patients ahead, estimated waiting time, refresh behavior, and reschedule eligibility.


## Stage 5 handoff

Stage 5 is complete. Each persisted appointment now has a local queue snapshot with current token, patients ahead, estimated waiting time, lifecycle status and refresh time. The Live Queue screen exposes explicit refresh plus deterministic demo controls for advancing the queue or marking an appointment missed.

Missed appointments can be rescheduled once within the configurable prototype rule of 10 days. Rescheduling moves the appointment to tomorrow, allocates a new token, records that the one-time option was used, and resets queue progress. The appointment codec remains backward compatible with records written by earlier stages.

Validated build: https://github.com/bobysirji-creator/dolo-patient-app/actions/runs/29226964239

Recommended next stage: replace demo queue advancement with a REST/WebSocket or polling contract, add foreground lifecycle refresh, queue-delay notifications, and backend-authoritative concurrency-safe token allocation.


### Stage 5 date-display correction

Booking now provides three selectable appointment dates and persists the selected date. Confirmation and Live Queue display a human-readable appointment date. The missed-appointment flow shows the exact proposed reschedule date before confirmation and the updated date afterward. Validated build: https://github.com/bobysirji-creator/dolo-patient-app/actions/runs/29228036084


## Combined Stages 6 and 7 handoff

Stages 6 and 7 are complete. The patient experience now includes foreground queue polling every 15 seconds, manual refresh, a visible sync/offline state, and a repository contract that can later be replaced by REST polling or WebSocket updates. Queue and token allocation remain a deterministic local prototype until the backend becomes authoritative.

The Patient App also adds persistent patient profile and family-member management, booking on behalf of a selected family member, appointment lifecycle timelines, completed-consultation review eligibility, verified local reviews, and an in-app notification centre. Notifications are created for booking, approaching turns, rescheduling, consultation completion and review submission. A Help & Support screen reserves complaint and chat integration without adding a provider yet.

All Stage 6 and 7 state remains behind PatientRepository. No maps, payment, SMS, real push provider, Doctor App or Admin App was introduced.

Validated build and artifact: https://github.com/bobysirji-creator/dolo-patient-app/actions/runs/29230262592

Artifact name: dolo-patient-debug-apk

Recommended next stage: Stage 8 provider abstractions and environment configuration for maps, payments, SMS and push notifications. Integrate providers one at a time, starting with a backend-authoritative OTP and appointment API before handling real patient data.


## Stage 8 handoff

Stage 8 is complete as a provider-ready foundation. New maps, payment, SMS and push interfaces define the operations the future backend and Android providers must implement. Their current disabled implementations return explicit unavailable results, which prevents accidental navigation, charges, outbound messages or device registration before configuration exists.

The Integration Readiness screen is available from Help & Support. It shows every provider as disabled and explains that no API keys, payment details, location data or device tokens are stored. IntegrationRegistry is the single lightweight source for this readiness state.

No real provider, SDK, credential or paid service has been connected. Real OTP and appointment authority should live behind an HTTPS backend rather than embedding service secrets in the APK.

Validated build: https://github.com/bobysirji-creator/dolo-patient-app/actions/runs/29232911280

Artifact name: dolo-patient-debug-apk

Recommended next stage: Stage 9 release hardening. Focus on accessibility labels and touch targets, offline and error-state consistency, UI and repository test expansion, performance checks, versioning, privacy copy and a release-candidate checklist before connecting live providers.


## Stage 9 release-candidate handoff

Stage 9 is complete. The Patient App is version 0.9.0-rc1 (version code 9). Accessibility semantics and minimum touch targets cover the primary controls. Live Queue now distinguishes current, stale and offline data and offers retry without discarding the last local snapshot.

ReleaseReadiness centralizes polling/staleness rules and safe local text behavior, with focused unit tests. Android backup and cleartext HTTP traffic are disabled. GitHub Actions now runs lint, unit tests and APK assembly, cancels stale builds, generates a SHA-256 checksum and uploads both files in `dolo-patient-debug-apk`.

The README, prototype privacy notice and release checklist document the demo scope and release blockers. Maps, payments, SMS and push notifications remain disabled. No production backend, real OTP, server-authoritative appointment/token/queue data, signing pipeline or real medical data is included.

Recommended next action: install the final successful artifact on a physical Android device and complete `docs/release-checklist.md`. After acceptance, freeze this Patient App release candidate. The next ecosystem project should be the dedicated Doctor App in its own repository/project, followed later by the Admin App; do not add either to this Patient App module.

## Stage 10 polish pass 1

The Patient App remains open for iterative polish and is now version 0.9.0-rc2 (version code 10). The bottom navigation has been reduced to Home, Appointments and Book. All three destinations are interactive, Appointment History is the Appointments destination, and the central Book action opens doctor categories. The inactive notification bell was removed from the shared page header; the active notification button remains only on Home.

Stage 10 remains in progress for further physical-device UI feedback. Maps, payments, SMS and push notifications remain disabled.

## Stage 10 polish pass 2

Version 0.9.0-rc3 (version code 11) centers Book between Home and Appointments. Home now renders a separate live queue card for every active appointment and identifies both the doctor and booked patient, so self and family-member bookings remain clear.

Queue estimates now include the consultation currently in progress. For example, token 10 while token 9 is in consultation starts at an estimated 12 minutes. Each active appointment stores the current token start time and exposes a one-second countdown that is preserved across routine queue refreshes and app restarts. Stage 10 remains open for further physical-device UI feedback; external integrations remain disabled.
## Stage 10 polish pass 3

Version 0.9.0-rc4 (version code 12) expands the offline test catalogue to 12 specialties and 24 doctors. Every specialty has two matching doctors and an optimized generated 3D illustration. Category artwork uses a small Compose floating animation without introducing a new runtime dependency.

Cards and primary actions now use stronger elevation and shadows. Doctor tickets use a softer blue surface, the patient's token uses a focused coral accent, and Home identifies the signed-in patient as `Name (City)`. Catalogue integrity is covered by unit tests. External integrations remain disabled and Stage 10 remains open for physical-device feedback.

## Stage 16A handoff - public hosted API connection

Version 0.10.0-stage16a (version code 13) starts the Patient App integration phase without risking its accepted offline workflow. The app now calls the hosted Render prototype over HTTPS for health, capabilities, and public clinic discovery. The status is visible through Home > Help & Support > Integration readiness and can be retried after a Render cold start or offline condition.

The implementation deliberately sends no login, phone, patient, family, appointment, queue, payment, location, or device-token data. LocalPatientRepository remains authoritative for all existing app behavior. Hosted clinics are displayed only as a read-only preview and do not silently enter the local booking catalogue.

External SMS, push, maps, and payment providers remain disabled. Backend authentication is still disabled, so authenticated bookings and cross-app queue synchronization are not part of Stage 16A.

Validation required:

1. Push the committed changes and confirm GitHub Actions lint, unit tests, and debug APK assembly pass.
2. Install version code 13 over the existing Patient App and confirm existing login, profile, favourites, family, and appointments remain intact.
3. Open Integration readiness with internet enabled. Confirm connected status, backend version 0.1.0-stage15.6, Stage 15.6, database connected, and a hosted clinic count.
4. Disable connectivity, retry, and confirm an offline message appears while local doctor browsing and appointment history still work.
5. Re-enable connectivity and confirm retry returns to connected.

Recommended next stage: Stage 16B controlled prototype identity. Add backend-issued short-lived access tokens and secure Android token storage before connecting booking or queue writes. Do not enable real OTP/SMS or collect real patient data in that stage.

## Stage 16A signing correction

Version `0.10.1-stage16a` (version code 14) fixes repeatable Patient APK upgrades. The package-conflict installation error was caused by GitHub-hosted runners generating a different default debug certificate for each build.

Main-branch and manual CI builds now require four encrypted Patient-repository secrets, reconstruct the private PKCS#12 only in runner temporary storage, sign the debug APK, verify it with `apksigner`, and compare the APK certificate digest with the keystore certificate. The artifact is `dolo-patient-stable-debug-apk` and includes the APK, APK checksum, and signing-certificate SHA-256. Pull requests receive no signing secrets and publish no installable APK.

A separate Patient certificate was generated outside all Git repositories at `C:\Users\Poly\Documents\codex\private\dolo-patient-prototype-signing`. It must be backed up securely. No private value is committed.

Because the previously installed Patient APK uses an unrecoverable temporary certificate, the first stable build requires one uninstall and loses old local demo data. Once the stable build is installed, all future builds using this certificate can update in place and retain local data.

## Stage 16B controlled prototype identity

Version `0.11.0-stage16b` (version code 15) requests tokens only for the fixed seeded `patient-demo` identity. The entered mobile number is retained locally and never sent to the backend. Access and rotating refresh tokens are encrypted with AES/GCM under Android Keystore; SharedPreferences stores only ciphertext and IV. Token networking runs off the UI thread, logout clears local token material immediately, and a Render/offline failure preserves the established local demo workflow with a visible `Local fallback` label.

Real SMS/OTP, arbitrary accounts, authenticated booking writes, server queue synchronization, maps, payments and push remain disabled. Local build tools are unavailable on this PC, so GitHub Actions must validate lint, unit tests and APK assembly. Follow `docs/stage16b-device-test.md` after the Platform API deploy is healthy.


### Stage 16B acceptance

Accepted on 20 July 2026. GitHub Actions passed, the stable APK updated the existing installation without data loss, hosted identity and Android Keystore session restoration passed after full app closure, Integration Readiness matched the deployed Stage 16.2 API, offline login visibly fell back to local mode, and reconnecting restored hosted prototype mode. Stage 16C is the next recommended phase.

## Stage 16C implementation checkpoint

Patient App `0.12.0-stage16c` (version code 16) adds an explicit Hosted Prototype Sync screen under Support > Integration readiness. It reuses the Stage 16B Keystore token pair, refreshes expired access tokens, boots the seeded dummy profile/clinic sessions, sends idempotent authoritative booking writes, reads server appointment history, and polls the owner-scoped live queue every 15 seconds while visible.

This does not migrate or upload existing local profiles, family members, appointments, favourites or reviews. The ordinary local workflow remains the fallback. Hosted snapshots are fetched again from the server and are not presented as an offline source of truth. Local Android tooling remains unavailable on this low-resource PC, so GitHub Actions is the build verifier.

### Stage 16C acceptance

Accepted on 20 July 2026. GitHub Actions passed and the stable APK upgraded the existing Patient App successfully. All eleven physical-device checks passed: local-data preservation, hosted login and readiness, seeded prototype discovery, authoritative booking/token allocation, idempotent duplicate protection, screen and full-restart history restoration, 15-second refresh stability, offline safety, and reconnection recovery.

The Platform API validation correction in commit `4035cfb` permits the deterministic canonical PostgreSQL UUID used by the seeded Prototype Patient; all 81 API tests passed. No Patient APK change was required for that server-side correction.

Recommended next phase: Stage 16D Doctor App hosted integration using seeded identities and the existing protected appointment, clinic-fee admission and queue-command boundaries. This will allow end-to-end Patient live-queue movement without enabling real identities, payments, SMS, maps or push providers.

## Stage 17B cross-app Doctor visibility correction

Patient App `0.12.1-stage16c` (version code 17) consumes the existing public hosted clinic discovery in the normal Doctor list without replacing the local test catalogue. The seeded hosted Doctor appears as a clearly labelled hosted card only while the Platform API publishes an active VERIFIED clinic; selecting it opens the separate authoritative hosted flow. Admin PENDING, REJECTED or inactive states remove that hosted card after refresh.

The hosted synchronization adapter now preserves a previous snapshot for ordinary offline failures but clears it for the explicit `DOCTOR_UNAVAILABLE` contract and shows `This doctor is temporarily unavailable for appointments.` Local profiles, family members, favourites, reviews and appointments are unchanged. JVM tests cover public discovery removal/restoration, error parsing, unavailable snapshot clearing and offline snapshot retention. GitHub Actions remains the Android compiler/lint verifier.

Accepted on 21 July 2026. GitHub Actions passed, Render deployed the corrected API, and paired Admin/Patient physical-device testing confirmed VERIFIED visibility plus immediate removal for PENDING and inactive states. A fresh hosted login restored the authoritative session, all local Patient data remained untouched, and Dr. Ananya Mehta was returned to active VERIFIED after testing.

## Stage 18B implementation checkpoint

Patient App `0.13.0-stage18b` (version code 18) extends Hosted Prototype Sync with the authoritative active communication feed. It reads Doctor announcements for the selected clinic and Admin broadcasts for all Patients, labels the source/type, and refreshes through the existing 15-second visible-screen loop.

Expired, future or draft messages are filtered by the server. The app does not upload or replace local profile, family, favourites, reviews or appointment data. SMS, Push, Maps and Payments remain disabled. GitHub Actions is the authoritative Android build gate because this PC has no local JDK/SDK.


## Stage 18B acceptance

Patient App `0.13.0-stage18b` passed GitHub Actions, stable in-place APK upgrade and all Stage 18B cross-app physical-device checks on 21 July 2026. Doctor announcements and Admin broadcasts appeared and disappeared correctly with published/draft state, automatic refresh, restart restoration and offline recovery; all local Patient data remained intact. External providers remain disabled.

## Stage 19C implementation checkpoint

Patient App `0.14.0-stage19c` (version code 19) adds a dedicated hosted Doctor profile page. It renders the approved registration number, qualification, experience, about text, clinic and clinic-direct consultation fee returned by Platform API public discovery. Hosted search also matches approved registration, qualification and about metadata.

The UI never reads pending/rejected profile revisions or Admin review notes. Selecting Book continues to the already accepted authoritative Hosted Prototype Sync flow. Missing Doctor data fails safely with a refresh action, and all local Patient data remains unchanged. GitHub Actions is the Android compiler/lint gate because this PC has no local JDK/SDK.

## Stage 19C acceptance

Patient App `0.14.0-stage19c` passed GitHub Actions, stable in-place upgrade and hosted physical-device checks on 21 July 2026. Approved Doctor details, search, Admin PENDING/VERIFIED/DISABLED propagation, fresh hosted login/session recovery and local-data isolation all passed. Pending/rejected review data remained private; Maps, Payments, SMS and Push remained disabled.

## Stage 21B implementation checkpoint

Patient App `0.15.0-stage21b` (version code 20) adds a selector for the fixed server-seeded SELF and FAMILY profiles inside Hosted Prototype Sync. Booking sends only the selected dummy profile ID. Retry keys now include both session and profile, allowing independent tokens for two household members while retaining the legacy SELF key after an in-place upgrade.

No local profile, local family member, age, phone or appointment is uploaded or merged. Real hosted family creation remains disabled. GitHub Actions is the Android compiler/lint/test gate on this PC; follow `docs/stage21b-device-test.md` after Platform API Stage 21A is live.

## Stage 21B acceptance

Patient App `0.15.0-stage21b`, Platform API `0.12.0-stage21a` and the existing Doctor hosted queue passed the complete physical-device checklist on 22 July 2026. SELF and FAMILY bookings, independent tokens, duplicate protection, Doctor visibility, history/live queue, restart restoration, offline safety and local-data isolation all passed. Maps, Payments, SMS and Push remained disabled.

## Stage 22A implementation checkpoint

Patient App `0.16.0-stage22a` (version code 21) adds the protected one-time missed-appointment reschedule flow to Hosted Prototype Sync. Only an authoritative ABSENT appointment with unused eligibility shows the action. The screen reads the server's separate reschedule horizon, lets the Patient choose an eligible candidate, sends an appointment/target-scoped idempotency key, then reloads authoritative history and live state.

The server remains authoritative for ownership, deadline, clinic, capacity and one-time enforcement. The original and replacement lineage are displayed, and no local profile, family member, appointment, favourite or review is uploaded or replaced. GitHub Actions is the Android compiler/lint/unit/APK gate on this PC. Platform API Stage 22A must deploy before following `docs/stage22a-device-test.md`.
## Stage 22A acceptance

Patient App `0.16.0-stage22a` and Platform API `0.13.0-stage22a` passed the full Stage 22A cross-app device checklist on 22 July 2026. One-time ABSENT rescheduling, bounded targets, duplicate protection, history/lineage, current-day-only ordinary booking independence, restart/offline recovery and local-data isolation passed. An additional observation retest is planned for 23 July without blocking this checkpoint.

## Stage 23A implementation checkpoint

Patient App `0.17.0-stage23a` (version code 22) now displays the authoritative clinic consultation-fee record on every hosted history card. PENDING explains that no receipt exists yet; PAID shows the clinic-recorded INR amount and receipt reference; WAIVED shows its receipt reference. Every confirmed state explicitly says it is not an online DO-LO payment.

The API response and UI omit payment method, gateway data and provider details. Receipt state remains attached to the original or replacement appointment after queue completion, absence, rescheduling, refresh and restart. Local profile, family, favourites, reviews and appointments remain untouched. GitHub Actions is the Android build gate; follow `docs/stage23a-device-test.md` only after API `0.14.0-stage23a` is live.
## Stage 23A acceptance

Patient App `0.17.0-stage23a`, Doctor App `0.20.0-stage23a` and Platform API `0.14.0-stage23a` passed the complete clinic-receipt checklist on 22 July 2026. Existing current-day SELF/FAMILY records verified PENDING, PAID and WAIVED presentation, matching receipt references, consultation/history retention, restart/offline safety and local-data isolation. A next-clinic-day observation retest is planned for 23 July without blocking acceptance.

## Stage 24A implementation checkpoint

Patient App `0.18.0-stage24a` (version code 23) moves active authoritative hosted appointments onto the normal Home screen. Hosted mode refreshes every 15 seconds while Home is visible and presents Doctor/patient identity, date/session, patient token, current token, patients ahead, estimated wait, countdown state and clinic-fee status. The latest active Doctor announcement or Admin broadcast is also surfaced.

Hosted and local test appointments remain explicitly separated. Tapping a hosted card or View all opens the existing full hosted booking/history workspace. Ordinary offline failures retain the last hosted snapshot; no local profile, family, favourite, review, notification or appointment is uploaded or replaced. GitHub Actions remains the Android compiler/lint/test gate.
## Stage 24A device-feedback correction

The initial Stage 24A device run passed Home loading, hosted appointment identity/token/queue data, automatic refresh, navigation, completion/history, offline recovery, local fallback and disabled-provider checks. It exposed one presentation flaw: Home selected only the first communication, so an Admin broadcast could hide an active Doctor update.

Patient App `0.18.1-stage24a` (version code 24) now renders a bounded three-card Home feed that prioritizes one Doctor update and one Admin broadcast when both exist, then fills any remaining position from the server order. The full Hosted Prototype Sync feed is unchanged. Regression coverage verifies simultaneous Doctor and Admin messages.
## Stage 24A acceptance

Patient App `0.18.1-stage24a` passed the final bounded Home communication retest on 22 July 2026. Active Doctor announcement and Admin broadcast appeared together, completing the Stage 24A physical-device checklist.

## Stages 25A-25B implementation checkpoint

Patient App `0.19.0-stage25b` (version code 25) consumes the new owner-scoped hosted review endpoints. Only COMPLETED hosted appointments expose a 1-5 rating and optional 500-character comment. A successful submission is reloaded from the server and displayed with PENDING moderation state; a stable appointment-scoped idempotency key protects retries.

The hosted review flow does not upload or merge local profile, family, favourites, notifications, appointments or local reviews. Doctor visibility and Admin moderation are reserved for Stages 25C-25D. GitHub Actions remains the Android compiler/lint/unit/APK gate; follow `docs/stage25ab-device-test.md` only after API `0.15.0-stage25ab` is deployed.
## Stages 25A-25B acceptance

Patient App 0.19.0-stage25b passed the complete Stage 25AB device checklist on 23 July 2026. Hosted completion gating, SELF/FAMILY association, pending persistence, duplicate protection, restart/offline recovery, non-public pending state and all local-data isolation checks passed.

## Stage 25D implementation checkpoint

Patient App 0.20.0-stage25d (version code 26) consumes only the public published-review count and one-decimal average returned for each clinic. Hosted Doctor cards and profiles show the aggregate or an explicit no-published-reviews state. Patient names and comments are never exposed by discovery; PENDING, HIDDEN and REJECTED reviews do not contribute.

Build only after API 0.16.0-stage25cd is deployed. GitHub Actions remains the Android compiler/lint/test gate on this PC. Use docs/stage25cd-device-test.md with the paired Admin and Doctor APKs.

## Stage 25D acceptance

Patient App `0.20.0-stage25d` passed GitHub Actions, stable in-place upgrade and the complete Stage 25C-25D checklist on 23 July 2026. Published-only count/average, hidden/rejected exclusion, zero-review presentation, privacy boundaries, restart/offline safety and local-data preservation passed.

## Stage 26B implementation checkpoint

Patient App `0.21.0-stage26b` (version code 27) activates the existing Help & Support placeholder for the hosted seeded account. The Patient selects a fixed category, enters a bounded subject and message, and submits through an idempotent protected endpoint. The same screen displays authoritative OPEN, IN_PROGRESS, RESOLVED and CLOSED history plus the bounded DO-LO Admin response.

No local profile, family member, favourite, notification, appointment or review is uploaded or replaced. Build only after API `0.17.0-stage26ab` is deployed, then follow `docs/stage26ab-device-test.md` with Admin App `0.7.0-stage26a`. External chat, attachments, SMS, Push, Maps and Payments remain disabled.
## Stages 26A-26B acceptance

Patient App `0.21.0-stage26b`, Admin App `0.7.0-stage26a` and Platform API `0.17.0-stage26ab` passed the complete support-request device checklist on 23 July 2026. Submission bounds, authoritative history, Admin state/response propagation, restart/offline recovery, role denial and local-data isolation passed.

## Stage 27B implementation checkpoint

Patient App `0.22.0-stage27b` (version code 28) consumes the owner-scoped in-app notification feed from Platform API `0.18.0-stage27ab`. Hosted appointment and queue updates appear in the existing Notifications screen with Patient/family identity and token. The Home bell badge includes unread hosted events, and opening the screen advances the monotonic server read cursor through the newest displayed event while local notifications remain independent.

GitHub Actions remains the Android compiler/lint/unit/APK gate on this PC. Deploy API migration 023 first, then install the stable Patient APK and follow `docs/stage27ab-device-test.md`. External Push, SMS, Maps and Payments remain disabled.

## Stages 27A-27B acceptance

Patient App `0.22.0-stage27b` and Platform API `0.18.0-stage27ab` passed GitHub Actions, Render deployment, stable APK upgrade and the complete notification checklist on 23 July 2026. Hosted SELF/FAMILY identity and token copy, Home badge, server-persisted read state, new Doctor event refresh, restart/offline recovery, non-duplication and all local-data isolation checks passed.

## Stages 31A-31B implementation checkpoint

Patient App `0.23.0-stage31b` (version code 29) adds a hosted communication-preference card backed by Platform API `0.22.0-stage31ab`. The seeded Patient can independently choose appointment/service updates, health information, promotions, in-app messages and English/Hindi. Saved values are server authoritative and retain versioned consent; unrelated local Patient data is neither uploaded nor replaced.

The UI and contract state two non-expandable policy boundaries: SMS is reserved only for OTP authentication, and future health-interest grouping may use only specialties of Doctors in the Patient's completed consultation history—never a diagnosis or disease inference. Patient Home now shows only Admin broadcasts. Doctor announcements are filtered by clinic ID and appear only on the matching hosted Doctor profile. GitHub Actions remains the Android compile/lint/test/APK gate; follow `docs/stage31ab-device-test.md` after the API deployment is live.
## Stages 31A-31B acceptance

Platform API `0.22.0-stage31ab` and Patient App `0.23.0-stage31b` passed GitHub Actions, Render deployment, stable APK upgrade and every `stage31ab-device-test.md` check on 23 July 2026. Server-owned communication choices, restart/offline recovery, local-data isolation, OTP-only SMS wording, specialty-history privacy, Doctor-profile-only announcements and Admin Home broadcasts are accepted.
## Stages 36A-36B implementation checkpoint

Patient App `0.24.0-stage36b` (version code 30) consumes Platform API `0.27.0-stage36ab`'s authenticated, consent-revalidated Patient campaign feed. Eligible messages appear under a dedicated `Targeted DO-LO messages` heading in Hosted Prototype Sync, with purpose, active dates and explicit in-app-only copy. General broadcasts remain separate, and targeted campaigns are deliberately absent from Patient Home.

The parser rejects non-authoritative or provider-backed responses. Ordinary offline failures preserve the last hosted snapshot and all local Patient data. Deploy the API before building/installing the stable Patient APK, then follow `docs/stage36ab-device-test.md`. SMS remains OTP-only and disabled; Push, Maps and Payments remain disabled.
## Stages 36A-36B acceptance

Platform API `0.27.0-stage36ab` and Patient App `0.24.0-stage36b` passed GitHub Actions, Render deployment, stable APK upgrade and every `stage36ab-device-test.md` check on 25 July 2026. Targeting, live consent changes, active-period/status exclusion, dedicated presentation, restart/offline recovery, role isolation and local-data preservation are accepted.
## Stage 40B implementation checkpoint

Patient App `0.25.0-stage40b` (version code 31) replaces the ambiguous mobile-login presentation with two explicit modes. `Production Patient account` consumes the API's authoritative readiness but remains disabled; `Seeded demo login` preserves the tested prototype path. The UI states that no real phone, profile or family data is uploaded, that the demo number is local-only, and that OTP `123456` sends no SMS.

The parser fails closed if the API unexpectedly reports real enrollment or an OTP provider as enabled. Existing secure hosted token restoration and offline local fallback are unchanged. Deploy Platform API `0.31.0-stage40ab` first, then let GitHub Actions build the stable APK and follow `docs/stage40ab-device-test.md`.

## Stages 40A-40B acceptance

Platform API `0.31.0-stage40ab` and Patient App `0.25.0-stage40b` passed GitHub Actions, Render deployment, stable APK upgrade and the complete Stage 40AB checklist on 26 July 2026. The explicit Production-versus-Demo login boundary, seeded OTP flow, restart/offline behavior, local-data isolation and disabled providers are accepted.

## Stage 41B implementation checkpoint

Patient App `0.26.0-stage41b` (version code 32) adds a Hosted DO-LO Identity card to Profile. It uses the secure hosted session to load the authenticated account's server-owned `DLO-PAT-NNNNNN` identifier, display name and role. The parser requires `SELF_ONLY_NO_PHONE`, rejects an unsafe contract, and never treats the ID as an authentication secret.

Local profile, family, favourites, appointments, reviews and notifications remain independent. Deploy Platform API `0.32.0-stage41ab` first, then use GitHub Actions to build the stable APK and follow `docs/stage41ab-device-test.md`.


## Stage 41AB acceptance

GitHub Actions, Render hosted verification, stable Patient APK upgrade and the complete physical-device checklist passed. The server-owned seeded Patient identity is accepted; production enrollment and issuance remain reserved.

## Stages 45A-45B implementation checkpoint

Patient App `0.27.0-stage45ab` (version code 33) begins the ecosystem UI modernization. The Patient app now uses one teal/navy Material 3 system, compact reusable components, a stable Home/Book/Appointments bottom bar, grouped account tools and short route transitions. The critical splash, login, Home, discovery, booking, confirmation, appointment and live-queue journeys were rebuilt around user tasks rather than prototype diagnostics.

All existing role, booking, queue, local persistence and hosted synchronization behavior remains in place. Simulation controls are collapsed under a testing label, while connection diagnostics remain available from Help & support. Use GitHub Actions as the Android compile/lint/unit/APK gate, install the stable artifact over the existing app and complete `docs/stage45ab-device-test.md`.

The complete and accepted modernization sequence is recorded in `docs/ui-modernization-roadmap.md`: Stage 46 modernizes Doctor, Stage 47 modernizes Admin and Stage 48 completes cross-app accessibility, responsive layout, dark theme and release polish.
## Patient Home UI implementation checkpoint

Patient App 0.30.0-home-ui (version code 38) recreates the approved Patient Home reference with modular Material 3 Compose components. Existing local and authoritative hosted queues, unread notifications, Admin broadcasts, favourites, booking, support and session behavior remain connected. The screen adds a dedicated All Queues route, swipe/dismiss broadcast carousel, responsive queue states, five-item fixed navigation and original optimized hero/broadcast assets.

This PC checkout has no Gradle wrapper, local Gradle or Java runtime. GitHub Actions must complete the compile/lint/test/APK gate before physical-device review. On device, verify OTP-to-Home navigation, drawer/top-bar alignment, all active queues, broadcast swipe/dismiss, favourite scrolling, five bottom actions, small-screen scrolling and hosted/offline recovery.
## Patient Home refinement checkpoint

Patient App 0.30.1-home-polish (version code 39) compacts and top-aligns the greeting card, removes the greeting icon, keeps the Patient name horizontal, narrows search, unboxes Near me and relocates the queue View all action into the section header. The Home drawer now includes a locally persisted app-level Dark Mode toggle, and the Home design uses semantic Material colors for both themes.

GitHub Actions remains the compile/lint/test/APK gate. On device, verify the full name at normal and larger font settings, the 112 dp greeting crop, search/Near me proportions, queue heading action and Dark Mode persistence after force-close and relaunch.

## Patient Home artwork and equal-card-height checkpoint

Patient App 0.30.2-home-card-fix (version code 40) restores full greeting-artwork visibility in both themes, uses a lighter terminal overlay in Dark Mode and ships a higher-contrast optimized hero asset. The two live-status cards now fill one shared intrinsic row height, preventing longer names or status text from producing mismatched card sizes.

Use GitHub Actions for the compile/lint/test/APK gate. On a physical device, check Light and Dark modes plus short and long Patient/Doctor names before accepting this checkpoint.

## Dark Mode contrast and current-queue order checkpoint

Patient App 0.30.3-dark-contrast (version code 41) swaps Queue Active and With Doctor in the current-token card and completes a semantic-color pass over the Patient UI. Authentication fields, OTP content, shared cards, bottom navigation and feature screens now select matching theme surfaces and foregrounds instead of retaining light-only white/navy combinations.

Use GitHub Actions for compilation and APK generation. On a physical device, verify the phone field before and after typing, disabled/loading controls, OTP entry, queue cards and representative discovery, booking, notification and diagnostic pages in both themes.

### Version 0.30.3 CI correction

The initial Dark Mode contrast commit failed Kotlin compilation because a theme color was read by a file-level Modifier. The shared page modifier is now a composable function, so `MaterialTheme.colorScheme.background` is evaluated only from composable screen contexts. UI behavior and data workflows are unchanged.

## Doctor Categories UI implementation checkpoint

Patient App `0.31.0-categories-ui` (version code 42) replaces the original demo category grid with a state-driven, searchable, accessible two-column Compose experience. Sixteen specialty categories expose stable IDs, aliases and Doctor counts. Selection passes category ID and visible name to the existing Doctor-list journey, while current local specialty naming remains compatible.

The screen uses `DoctorCategoryRepository` and a fake local implementation so a future REST catalogue can replace prototype data without changing UI components. Loading, empty, error, no-result and disabled states are explicit. SMS, Push, Maps and Payments remain disabled. Use GitHub Actions for the authoritative Android gate, then verify the reference-led layout and navigation on a physical device.

## Doctor Categories flat-layout refinement

Patient App 0.31.1-categories-flat (version code 43) removes the category hero and card containers, ships transparent category illustrations, and changes image scaling from crop to fit. The Home bottom-nav action now returns to the existing Home back-stack destination reliably.

GitHub Actions remains the compile/lint/unit/APK gate. On device, verify all category artwork is complete at the edges, search and category selection still work, unavailable states remain disabled, Light/Dark contrast is readable, and tapping Home returns directly to Patient Home.

## Doctor List UI implementation checkpoint

Patient App 0.32.0-doctor-list-ui (version code 44) replaces the original demo Doctor results page with the reference-led searchable and filterable Doctor List. Dynamic specialty headers, realistic portrait assets, responsive Doctor rows, availability, fees, favourites, local and hosted Profile routing, direct local booking, Maps placeholders, callback confirmation and the existing five-item bottom navigation are connected.

The four Cardiology demo Doctors use stable local IDs backed by DummyData, so Profile and walk-in booking remain functional. No Doctor Profile redesign or provider integration is included. GitHub Actions remains the Android compile/lint/unit/APK gate; physical-device visual comparison is still required.

## Doctor List search-row refinement

Patient App 0.32.1-doctor-list-search (version code 45) aligns the Doctor search and Near me controls at 56 dp and simplifies the visible placeholder to `Search`. Search logic, accessibility context, filters, booking and navigation are unchanged.
## Appointment Booking UI checkpoint

Patient App 0.33.0-booking-ui (version code 46) replaces the demo booking form with the reference-led walk-in booking screen. The state-driven flow supports the logged-in Patient and saved family members, compact clinics, available dates, independent Morning/Evening sessions, dynamic Patient details, fee breakdown, validation, retry/loading states and accessible responsive components.

The screen uses `AppointmentBookingRepository` with a fake implementation so REST booking options and appointment creation can be added without replacing the Compose UI. Successful confirmation still commits through the existing `PatientViewModel.book` path, preserving token allocation, history, persistence and live queue behavior. Provider integrations remain disabled and the existing Booking Confirmation screen is intentionally unchanged.

This checkout has no local Android toolchain. GitHub Actions is the compile/lint/unit/APK gate; then complete `docs/appointment-booking-device-test.md` on a physical phone.
## Appointment Booking polish checkpoint

Patient App 0.33.1-booking-polish (version code 47) widens and fixes the height of visitor, clinic and session cards, removes visitor-initial circles, softens selected-state contrast and aligns fee values. The booking total now represents only the DO-LO service charge payable during booking; the Doctor consultation fee remains clearly listed for direct payment at the clinic. Appointment creation and token workflow are unchanged.
### Appointment selection-outline correction

Patient App 0.33.2-selection-outline (version code 48) keeps one neutral 1 dp outline and the same subtle elevation for selected and unselected booking cards. Selection is now communicated by a light primary-container tint, teal text/tick and accessibility state, preventing a stronger border from consuming visible card space.
### Appointment selection-halo correction

Patient App 0.33.3-selection-halo (version code 49) replaces generic clickable indications on booking choices with radio-selection semantics and a deliberately disabled visual press/focus halo. The normal thin card border remains unchanged; selection continues through the light tint, tick and accessible selected state without adding a grey outer ring.
### Daylight selected-surface correction

Patient App 0.33.4-selection-surface (version code 50) replaces the translucent selected-card fill with an opaque color blended from the active theme surface and primary container. Selectable cards also use zero elevation. This removes the daylight-only dark inner edge while preserving the normal 1 dp outline, subtle selected tint, tick and Dark Mode appearance.

## Booking Confirmation UI checkpoint

Patient App 0.34.0-confirmation-ui (version code 51) replaces the demo token panel with a state-driven booking confirmation journey keyed by appointment ID. It presents the token, date, independent session, Doctor, clinic, visitor and current queue estimate, with loading, pending, retry and unavailable-estimate states.

Calendar uses the Android insert-event intent without permissions. Share excludes Patient identity and other sensitive local profile data. Save and Maps remain explicit placeholders; SMS, Push, Maps and Payment providers remain disabled. The existing persisted appointment, history and live queue data are unchanged.

This lightweight checkout has no Gradle wrapper, local JDK, Android SDK or emulator. GitHub Actions is the authoritative compile/lint/unit/APK gate; then complete `docs/booking-confirmation-device-test.md` on a physical phone.

## Stage 49B implementation checkpoint

Patient App 0.35.0-stage49b (version code 52) upgrades the existing fail-closed enrollment-readiness client to the complete accepted Stage 49A contract. The parser requires foundation version 49A, disabled production/profile/family enrollment, authentication-only OTP with its provider disabled, the server-owned location/phone-neutral DLO-PAT-NNNNNN policy, the reserved atomic allocator and the Terms, Privacy and Health Data consent categories.

Create an Account now retrieves and presents that authoritative preparation status without opening a registration form or sending Patient data. Missing network or any unsafe contract leaves account creation disabled. The existing seeded demo OTP, hosted token restoration, local fallback and all local Patient data remain unchanged. GitHub Actions is the Android build authority; then install the stable APK over the existing app and complete docs/stage49b-device-test.md.

## Stage 49B acceptance checkpoint

Patient App 0.35.0-stage49b passed GitHub Actions, stable in-place APK upgrade and every Stage 49B physical-device check on 3 August 2026. Authoritative disabled registration readiness, fail-closed offline recovery, seeded demo login, session restoration and local-data isolation are accepted. Production enrollment, real OTP/SMS delivery, public-ID allocation and all external providers remain disabled.

## Stage 50B implementation checkpoint

Patient App 0.36.0-stage50b (version code 53) adds a strict client for Platform API 0.37.0-stage50a's read-only activation requirements. Create an Account now accepts status only when the existing Stage 49A readiness contract is safe and all seven Stage 50A gates appear in canonical order as BLOCKED with NOT_APPROVED evidence. The screen translates every gate into patient-friendly wording and retains the future location/phone-neutral DLO-PAT-NNNNNN explanation.

A missing network, malformed response, changed gate, satisfied gate or Patient-input-accepting privacy marker fails closed. No form opens and no phone, profile, family or consent record is uploaded. Seeded OTP 123456, secure hosted sessions, offline fallback and all local Patient records are unchanged. GitHub Actions remains the Android build authority; install the stable APK and complete docs/stage50b-device-test.md.

## Stage 50B acceptance

Patient App 0.36.0-stage50b passed GitHub Actions, stable in-place APK upgrade and every Stage 50B physical-device check on 3 August 2026. All seven patient-friendly blocked prerequisites, authoritative refresh, offline fail-closed recovery, demo OTP login, hosted session restoration and local-data isolation are accepted. Production enrollment, real OTP/SMS, public-ID allocation and all external providers remain disabled.

## Stage 51B implementation checkpoint

Patient App 0.37.0-stage51b (version code 54) adds a strict client for Platform API 0.38.0-stage51a's read-only consent catalog. Create an Account now verifies Terms, Privacy and Health Data in canonical order and displays each as reserved and not published, alongside the seven already blocked activation prerequisites.

Any missing, reordered, published, collectable or otherwise unsafe catalog fails closed. The App sends no Patient input to the endpoint and exposes no consent checkbox or registration action. Seeded OTP 123456, secure hosted session restoration, local fallback and existing local Patient data remain unchanged. GitHub Actions is the Android build authority; install the stable APK over the existing app and complete docs/stage51b-device-test.md.
