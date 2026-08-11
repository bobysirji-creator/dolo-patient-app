# DO-LO Android Ecosystem Roadmap

## Current scope

Only the dedicated Patient App is changed in this repository. The Doctor App and platform API live in separate repositories; the Admin App remains a later ecosystem project.

## Stages

- [x] Stage 1 â€” Lean Android project, Compose theme, navigation skeleton, placeholder screens, reusable components, dummy models, documentation
- [x] Stage 2 â€” Local state architecture, validation, fake OTP contract, persisted session and authentication repository
- [x] Stage 3 â€” API-ready contracts, offline repository, persisted appointments, generated tokens and repository tests
- [x] Stage 4 â€” Doctor search, filters, profiles, clinics and favourites
- [x] Stage 5 â€” Live queue simulation, lifecycle states, waiting estimates and one-time rescheduling
- [x] Stage 6 â€” Live queue tracking, estimates and foreground/background refresh strategy
- [x] Stage 7 â€” Favourites, reviews, missed-appointment rescheduling
- [x] Stage 8 â€” Provider-ready maps, payments, SMS and push-notification foundations
- [x] Stage 9 â€” accessibility, offline/error states, tests, performance and release hardening
- [ ] Stage 10 - iterative UI/UX polish from physical-device feedback
- [x] Stage 16A - Patient App public hosted-API connection, capability status and clinic-discovery preview with local fallback
- [x] Stage 16B - controlled prototype identity/authentication and Android Keystore-encrypted token storage (accepted on physical device)
- [x] Stage 16C - server-authoritative Patient booking, token and live-queue synchronization behind an explicit migration switch
- [x] Ecosystem - Dedicated Doctor App created in a separate repository
- [ ] Future - Create the separate Admin App, reusing contracts/design tokens without coupling app builds

## Stage 1 acceptance status

- Project structure: complete
- Seven requested screen destinations: complete
- Mobile-number login placeholder: complete
- Morning/evening walk-in session booking: complete
- Token confirmation placeholder: complete
- Build/device verification: pending on a machine with Android SDK and Gradle dependencies available



## Stage 2 acceptance status

- Mobile validation and normalized phone input: complete
- Fake OTP request/verify flow using 123456: complete
- Persisted offline patient session and logout: complete
- ViewModel and repository boundaries: complete
- Loading, error and retry-friendly UI states: complete
- Phone validation unit tests: complete
- Real backend and SMS provider: intentionally deferred
- Remote APK validation: pending current GitHub Actions run

## Stage 3 acceptance status

- Provider-neutral PatientApi and ApiResult contracts: complete
- Doctor and appointment repository boundaries: complete
- SharedPreferences-backed offline appointment persistence: complete
- Generated booking IDs and tokens: complete
- Active appointment restored after restart: complete
- Dynamic token/date/doctor/clinic confirmation data: complete
- Repository codec and token tests: complete
- Unit tests and debug APK build: passed
- Real backend and secure server access tokens: intentionally deferred


## Stage 4 â€” Patient discovery and engagement (complete)

- Doctor search by name, specialty, and clinic is connected to PatientViewModel.
- Category filtering and empty search states are implemented.
- Doctor details provide clinic, session, fee, rating, and booking entry point.
- Favourite doctors persist locally and appear on Home and the dedicated Favourites screen.
- Appointment History reads persisted bookings.
- GitHub Actions tests and debug APK assembly passed in run 29201170208.


## Stage 5 â€” Live queue and appointment lifecycle (complete)

- Queue snapshot persists current token for each appointment.
- Patients-ahead and estimated waiting time use a 12-minute configurable prototype average.
- Live Queue screen supports refresh and deterministic demo advancement.
- Appointment lifecycle includes BOOKED, WAITING, IN_CONSULTATION, COMPLETED and MISSED states.
- Missed appointments are eligible for one reschedule within 10 days; successful reschedule is stored for the next day.
- Legacy Stage 3/4 appointment records remain readable.
- Queue calculations and codec compatibility have unit tests.
- GitHub Actions tests and debug APK assembly passed in run 29226964239.


## Stages 6 and 7 â€” Queue synchronization and patient engagement (complete)

- Live queue refreshes automatically every 15 seconds while the screen is open and also supports manual refresh.
- Queue sync exposes fresh, syncing and offline demonstration states while preserving the provider-neutral repository boundary.
- Booking supports the patient profile or a saved family member, and selected patient names persist with appointments.
- Patient profile and family-member management persist locally.
- Appointment history includes a clear lifecycle timeline.
- Completed consultations can receive one verified local review; doctor details show verified review totals.
- In-app notifications are generated for booking, turn-approaching, rescheduling, completion and review events.
- Help and support placeholders reserve future complaint/chat integration.
- Legacy appointment records remain readable and Stage 6/7 codec behavior is covered by unit tests.
- GitHub Actions unit tests and debug APK assembly passed in run 29230262592.


## Stage 8 â€” Provider integration foundations (complete)

- Provider-neutral contracts exist for maps/navigation, payment order creation and verification, SMS OTP/reminders, and push registration/queue alerts.
- Disabled provider implementations fail safely and never create navigation links, payment orders, messages or device registrations.
- A central integration registry reports provider mode and readiness without storing credentials.
- The Patient App exposes an Integration Readiness screen from Help & Support.
- All external providers default to DISABLED; no API keys, payment data, precise location or device tokens are committed.
- Pure unit tests verify capability defaults and disabled-provider behavior.
- No third-party SDK or additional build weight was added.
- GitHub Actions unit tests and debug APK assembly passed in run 29232911280.


## Stage 9 â€” Patient release hardening (complete)

- Release candidate version is 0.9.0-rc1 (version code 9).
- Core controls have accessible descriptions, button roles and minimum 48 dp touch targets.
- Live Queue distinguishes current, stale and offline data and provides a retry action.
- Release-readiness policy centralizes refresh timing, stale detection, readable statuses and safe local text.
- Unit tests cover queue freshness and safe-text behavior.
- Android backup and cleartext HTTP traffic are disabled.
- GitHub Actions now runs Android lint, unit tests and debug assembly, cancels stale builds, and publishes an APK SHA-256 checksum.
- README, prototype privacy notice and release checklist document the controlled-test scope.
- Maps, payments, SMS and push notifications remain disabled.
- The Patient App roadmap is complete for the offline release candidate; production backend/provider work remains a separate future program.

## Stage 10 polish pass 2

- Bottom navigation order is Home, Book, Appointments, with Book centered.
- Home shows doctor and patient names for every active appointment, including family-member bookings.
- Each active appointment has its own token, live queue progress and navigation to the matching queue.
- Estimated wait includes the current 12-minute consultation; a live countdown updates every second without resetting on normal refresh.
- Regression tests cover the corrected token 10/current token 9 estimate and countdown.
## Stage 10 polish pass 3

- Expanded test catalogue to 12 illustrated specialties and 24 doctors, with at least two doctors per category.
- Added optimized 3D medical category artwork with a lightweight Compose floating animation.
- Added stronger elevation and shadow treatment to category, doctor, appointment, queue, information and action surfaces.
- Updated doctor tickets to a softer blue treatment and highlighted the patient's token in coral.
- Simplified the Home greeting to patient name and saved city.
- Added catalogue integrity tests for category artwork, specialty coverage and unique doctor IDs.

## Stage 16A - Safe Patient App hosted integration

- The Patient App connects to the Render prototype through HTTPS only.
- Public `/health`, `/api/v1/meta/capabilities`, and `/api/v1/clinics` responses are parsed through a lightweight platform boundary.
- Support > Integration Readiness shows connection state, deployed version/stage, database state, and hosted clinic discovery.
- Network work runs off the UI thread and exposes retry, timeout, offline, and cold-start messages.
- Existing local login, profile, doctors, appointments, token allocation, and queue state remain authoritative and continue working offline.
- No patient data, phone number, appointment, payment data, location, or device token is sent.
- External SMS, push, maps, and payment providers remain disabled.
- Unit tests cover hosted response parsing, malformed clinic filtering, and HTTPS enforcement.
- Public API code passed GitHub Actions in version 0.10.0-stage16a (version code 13).
- Stable Patient prototype signing and certificate verification are implemented in version 0.10.1-stage16a (version code 14); first-install and later-upgrade acceptance are pending.

## Stage 16C - Server-authoritative seeded prototype flow

- [x] protected bootstrap using the Keystore-backed renewable Bearer session
- [x] explicit separate hosted screen; no silent migration of local data
- [x] PostgreSQL-authoritative appointment and session token allocation
- [x] persistent idempotency key prevents duplicate retry allocation
- [x] server appointment history and 15-second live-queue polling
- [x] local booking, profile, family, favourites and reviews remain unchanged
- [x] GitHub Actions and physical-device checklist acceptance

Accepted on 20 July 2026. The stable APK upgraded in place; all eleven Stage 16C device checks passed, including hosted login, authoritative booking/token allocation, duplicate protection, history and session restoration, automatic refresh, offline safety, local-data preservation and reconnection recovery.

Recommended next phase: Stage 16D Doctor App hosted integration. Connect a seeded Doctor/Assistant prototype to the existing protected appointment, admission and queue-command APIs so Patient live-queue movement can be verified end to end. Keep the accepted Patient local workflow and all external providers unchanged.

## Stage 17B cross-app Doctor visibility correction

- [x] display the active VERIFIED seeded hosted Doctor in normal discovery
- [x] remove the hosted card when public clinic discovery omits the Doctor
- [x] route the hosted card to the separate authoritative booking flow
- [x] clear stale hosted Doctor data on `DOCTOR_UNAVAILABLE`
- [x] retain safe prior hosted data on ordinary offline failure
- [x] preserve all local Patient data and local test catalogue entries
- [x] GitHub Actions validation
- [x] physical-device Admin-to-Patient verification

## Stage 18B - Hosted Patient communication feed

- [x] fetch active bounded communications from the authoritative Platform API
- [x] distinguish DO-LO Admin broadcasts from Doctor availability, camp, offer and general announcements
- [x] refresh communication state with the existing visible-screen hosted polling
- [x] preserve all local profile, family, favourites, reviews and appointment data
- [x] GitHub Actions compile, lint and unit tests
- [x] stable APK in-place upgrade and cross-app physical-device acceptance

SMS and Push remain disabled; this stage renders in-app server messages only.

## Stage 19C - Approved hosted Doctor profile display

- [x] parse approved public Doctor registration, qualification, experience and about fields
- [x] add a dedicated hosted Doctor profile route and mobile-first screen
- [x] extend hosted search across approved profile metadata
- [x] keep authoritative booking on the accepted hosted sync route
- [x] show a safe unavailable state if Admin removes the Doctor during navigation
- [x] preserve local profile, family, favourites, reviews and appointments
- [x] add JSON/discovery regression tests
- [x] GitHub Actions compile, lint and unit tests
- [x] stable APK in-place upgrade
- [x] cross-app Admin approval/rejection physical-device checks

Maps, Payments, SMS and Push remain disabled.

## Stage 21B - seeded hosted family booking

- [x] parse backward-compatible SELF/FAMILY hosted profile list
- [x] mobile-safe seeded profile selector before appointment booking
- [x] profile-scoped idempotency keys with legacy SELF-key compatibility
- [x] family patient name retained in history and live queue
- [x] local profile and family list remain private and unchanged
- [x] JSON and retry-key unit coverage; stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] Patient/Doctor cross-app physical-device acceptance

## Stage 22A - hosted missed-appointment rescheduling

- [x] consume the protected one-time reschedule endpoint
- [x] display reschedule action only for eligible ABSENT appointments
- [x] use the server's independent reschedule horizon rather than ordinary future-booking dates
- [x] persist appointment/target-scoped retry keys
- [x] display original RESCHEDULED state and replacement lineage/name/token
- [x] preserve local profile, family, favourites, reviews and appointments
- [x] add policy/retry-key unit coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] Patient/Doctor cross-app physical-device acceptance
## Stage 23A - hosted clinic receipt visibility

- [x] parse clinic fee status, recorded amount and receipt reference from authoritative history
- [x] distinguish PENDING, PAID and WAIVED clinic records
- [x] clearly state that the consultation fee is handled directly at the clinic
- [x] retain receipt state after completion, absence, rescheduling and refresh
- [x] expose no clinic payment method or platform payment data
- [x] add history JSON and presentation regression coverage
- [x] preserve all local Patient data and disabled-provider boundaries
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] Patient/Doctor cross-app physical-device acceptance
## Stage 24A - authoritative hosted Patient Home

- [x] automatically refresh hosted data while an authenticated Patient is on Home
- [x] show active hosted SELF/FAMILY appointments separately from local test appointments
- [x] display Doctor, patient, date, session, token, current token and patients ahead
- [x] display the server-authoritative estimated wait and countdown state
- [x] surface the latest active Doctor/Admin in-app update
- [x] retain a clear route to the complete hosted booking/history workspace
- [x] keep local Patient data isolated and preserve hosted snapshots during ordinary offline failures
- [x] add deterministic hosted-home presentation coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] physical-device Home refresh and cross-app queue acceptance

No Platform API migration or deployment is required. Maps, Payments, SMS and Push remain disabled.
## Stages 25A-25B - hosted Patient reviews

- [x] completion-gated, owner-scoped review contract in PostgreSQL
- [x] one review per hosted appointment with idempotent retry handling
- [x] initial PENDING state and immutable Patient-facing moderation status
- [x] protected Patient submit and own-review list endpoints
- [x] Patient rating/comment form only on completed hosted appointments
- [x] pending moderation display after submission and restart
- [x] preserve every unrelated local Patient record
- [x] API check and 108-test local suite
- [x] API PostgreSQL integration in GitHub Actions and Render deployment
- [x] Patient GitHub Actions build and stable APK
- [x] physical-device completed-appointment review checklist

Doctor review visibility and Admin moderation actions are deliberately reserved for Stages 25C-25D. Maps, Payments, SMS and Push remain disabled.
## Stage 25D - published review summary

- [x] parse published-only count and one-decimal average from public clinic discovery
- [x] show the aggregate on hosted Doctor list/profile
- [x] expose no Patient identity or comment in public discovery
- [x] retain zero-review empty state
- [x] JSON regression coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] combined Admin/Doctor/Patient physical-device acceptance

Admin moderation and Doctor feed are delivered by the paired Stage 25C-25D repositories. Maps, Payments, SMS and Push remain disabled.

## Stage 26B - authoritative Patient support requests

- [x] replace the Support placeholder with a hosted request form
- [x] fixed APPOINTMENT, DOCTOR, BILLING, APP and OTHER categories
- [x] bounded subject/message validation and retry-safe idempotency
- [x] owner-scoped authoritative status and Admin-response history
- [x] OPEN, IN_PROGRESS, RESOLVED and CLOSED presentation
- [x] preserve all unrelated local Patient data
- [x] JSON contract coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] paired Patient/Admin physical-device acceptance

External chat, attachments, SMS, Push, Maps and Payments remain disabled.
## Stage 27B - authoritative hosted in-app notifications

- [x] load the Patient-owned hosted notification feed with existing authoritative sync
- [x] show hosted appointment/queue updates in the existing notification screen
- [x] display the matching SELF/FAMILY patient and token on each hosted card
- [x] include hosted unread state in the Home bell badge
- [x] mark through the newest displayed server cursor without changing local notifications
- [x] preserve hosted snapshot and all local data during ordinary offline failures
- [x] JSON contract coverage and stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] physical-device notification/read-cursor acceptance

External Android Push and SMS are not enabled. Maps and Payments also remain disabled.
## Stage 31B - hosted communication preferences

- [x] load server-owned communication choices for the hosted seeded Patient
- [x] independently edit appointment/service, health-information, promotional and in-app choices
- [x] choose preferred English or Hindi and save versioned consent
- [x] disclose that health grouping uses consulted Doctor specialty history only
- [x] disclose that no diagnosis or disease is inferred or stored
- [x] reserve SMS exclusively for OTP and expose no promotional SMS path
- [x] remove Doctor announcements from Patient Home
- [x] show an active Doctor announcement only on its matching Doctor/clinic profile
- [x] retain Admin broadcasts on Patient Home
- [x] add JSON, null-consent and Home-filter regression coverage
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade
- [x] API deployment and paired physical-device acceptance

Your local Patient profile, family, favourites, appointments, reviews and notifications remain isolated. Push, Maps and Payments remain disabled; the SMS provider also remains disabled until a later OTP-only integration.
## Stage 36B - consent-aware targeted Patient messages

- [x] load the authenticated Patient-only targeted campaign feed
- [x] require authoritative, in-app-only, provider-disabled response metadata
- [x] render matching messages in a dedicated Hosted Sync section
- [x] keep targeted messages separate from general broadcasts and off Patient Home
- [x] retain purpose and active-period context on each message card
- [x] preserve the last hosted snapshot and every local Patient record during ordinary offline failures
- [x] parser and external-provider rejection regression coverage
- [x] stable version increment
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] API deployment and stable APK in-place upgrade
- [x] paired Stage 36 physical-device acceptance

The server revalidates current consent and schedule on every feed read. SMS remains OTP-only and disabled; Push, Maps and Payments remain disabled.
## Stage 40B - explicit Patient identity modes

- [x] show Production Patient and Seeded demo as separate login choices
- [x] consume authoritative enrollment readiness over HTTPS
- [x] reject any contract that enables real enrollment or an OTP provider unexpectedly
- [x] keep production account creation visibly disabled
- [x] disclose that no real phone, profile or family data is uploaded
- [x] disclose that the demo number stays local and the server uses `patient-demo`
- [x] disclose that fixed demo OTP `123456` sends no SMS
- [x] preserve hosted seeded session and local fallback behavior
- [x] stable version increment and JSON boundary coverage
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] API deployment and stable APK in-place upgrade
- [x] Stage 40AB physical-device acceptance

Real Patient registration, DO-LO ID issuance and external OTP delivery remain reserved for a later provider-controlled stage.

## Stage 41B - hosted public DO-LO identity card

- [x] authenticated self-only identity fetch with automatic token refresh
- [x] strict public-ID and privacy-contract validation
- [x] Hosted DO-LO Identity card in Patient Profile
- [x] stable ID display separate from local phone/profile data
- [x] explicit seeded-prototype and server-owned wording
- [x] refresh/recovery action without account creation
- [x] preserve local fallback and every unrelated local record
- [x] stable version increment and JSON regression coverage
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] API deployment and stable APK in-place upgrade
- [x] Stage 41AB physical-device acceptance

The public ID is not an authentication secret and cannot be edited. Real Patient registration and production DO-LO ID issuance remain disabled/reserved.

## Stages 45A-45B - Patient experience modernization

- [x] shared modern Patient design tokens and reusable components
- [x] compact three-destination primary navigation and grouped account tools
- [x] smooth route transitions without changing destination contracts
- [x] redesigned splash, login, Home, discovery, booking, confirmation, appointments and live queue
- [x] supporting profile, support, diagnostics and connected-care language cleanup
- [x] prototype simulation controls collapsed outside the normal queue journey
- [x] architecture regression test and full Stages 45-48 modernization roadmap
- [ ] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and Stage 45AB physical-device acceptance

See `docs/ui-modernization-roadmap.md` and `docs/stage45ab-device-test.md`. No API contract, provider status, billing rule or local-data boundary changed in this UI stage.

## Booking Confirmation UI refinement

- [x] appointment-ID confirmation route
- [x] validated token and booking detail presentation
- [x] loading, pending, error and no-estimate states
- [x] Calendar, share, Save placeholder and Maps callback
- [x] responsive and accessible layouts plus previews
- [x] pure model/formatting unit tests
- [ ] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and physical-device visual acceptance

Live Queue redesign and external provider activation remain outside this screen checkpoint.

## Stage 49B - Patient enrollment-readiness client

- [x] consume the complete accepted Stage 49A readiness contract over HTTPS
- [x] require foundation version 49A and fail closed on missing or changed fields
- [x] require production, profile and family enrollment to remain disabled
- [x] require OTP authentication-only mode and disabled OTP provider
- [x] require the location-neutral, phone-neutral, server-owned public-ID policy
- [x] require Terms, Privacy and Health Data consent categories
- [x] present authoritative account-preparation status from Create an Account
- [x] keep registration unavailable when the network or contract is unsafe
- [x] submit no phone, profile, family or consent data to enrollment endpoints
- [x] preserve seeded demo login, secure token restoration and local fallback
- [x] parser, unsafe-policy, consent and presentation regression coverage
- [x] stable version increment to 0.35.0-stage49b (version code 52)
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade and Stage 49B physical-device acceptance

Real Patient enrollment, OTP delivery, production DO-LO ID issuance and all external providers remain disabled. The Stage 49A allocator is not called by the Patient App.

## Stage 50B - Patient activation-requirements client

- [x] consume Stage 50A requirements through a read-only HTTPS GET
- [x] strictly require foundation version 50A and authoritative no-input metadata
- [x] require all seven known gates in canonical order as BLOCKED and NOT_APPROVED
- [x] reject any satisfied, missing, unknown or unsafe activation contract
- [x] present all seven prerequisites with patient-friendly labels
- [x] retain future location/phone-neutral DLO-PAT-NNNNNN explanation
- [x] keep account creation unavailable and submit no Patient data
- [x] preserve Stage 49B readiness, demo OTP, session restoration and local data
- [x] JSON, fail-closed status and gate-label regression coverage
- [x] stable version increment to 0.36.0-stage50b (version code 53)
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade and Stage 50B physical-device acceptance

Production enrollment, real OTP/SMS delivery, public-ID allocation and every external provider remain disabled. The Patient App never calls an activation, enrollment or public-ID allocation transaction.

## Stage 51B - reserved consent-catalog Patient client

- [x] consume the accepted Stage 51A catalog using a read-only HTTPS GET
- [x] require authoritative no-input metadata and providers disabled
- [x] require foundation version 51A and the versioned-legal-consents gate blocked
- [x] require Terms, Privacy and Health Data in canonical order
- [x] require every document to remain reserved, unpublished and non-collecting
- [x] fail closed on missing, reordered, published, collectable or unsafe content
- [x] show the three document statuses under Create an Account
- [x] keep the seven Stage 50A blocked gates visible
- [x] submit no phone, profile, family or consent data
- [x] preserve seeded demo login, session restoration and local data
- [x] parser, unsafe-catalog and presentation regression coverage
- [x] stable version increment to 0.37.0-stage51b (version code 54)
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [x] stable APK in-place upgrade and Stage 51B physical-device acceptance

Production enrollment, legal acceptance, real OTP/SMS, public-ID allocation and every external provider remain disabled. Reserved catalog entries are not legal documents.

## Stages 52-64 - production-capable ecosystem roadmap

The cross-app production sequence, dependencies and completion rules are maintained in docs/ecosystem-roadmap-52-64.md. App-specific implementation and acceptance checkpoints will be added here as each stage reaches this repository.

## Stage 52B-P - prototype legal-document viewer

- [x] verify a strict hosted test-only document contract and SHA-256 content
- [x] list Terms, Privacy and Health Data previews from account creation
- [x] show responsive accessible document viewer with permanent test banner
- [x] simulate acknowledgement in memory only
- [x] keep registration and production consent disabled
- [x] preserve local Patient data and existing login/booking workflows
- [ ] GitHub Actions lint, unit tests and stable APK build
- [ ] physical-device checklist

Stage 52B-F remains deferred until legally approved content and approval metadata are available.

## Stage 53B-P - seeded recovery simulation client

- [x] expose only three predefined dummy recovery scenarios
- [x] submit enum-only simulation requests with a stable idempotency key
- [x] reject account-changing, phone-bearing or internal-user payloads
- [x] show authoritative case status and append-only audit events
- [x] keep real recovery, mobile change, account merge and production enrollment disabled
- [x] preserve seeded login, hosted booking and all local Patient data
- [x] parser and unsafe-payload regression coverage
- [x] stable version increment to 0.39.0-stage53bp (version code 56)
- [ ] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and Stage 53B-P physical-device acceptance

Stage 53B-F remains policy-, legal-, privacy- and security-gated. This prototype cannot alter an account.
## Stage 59C-P - Patient synthetic payment presentation

- [x] present captured, zero-charge, failed, expired and refunded test outcomes
- [x] accept only authoritative synthetic INR responses with no real-money marker
- [x] reuse a persisted per-scenario idempotency key
- [x] show synthetic reference, status, test amount, eligibility and refund state
- [x] collect no card, UPI, bank, Patient, family or appointment information
- [x] preserve local data and keep provider, appointment and billing-ledger mutation disabled
- [x] parser and unsafe-response regression coverage
- [x] corrective version increment to 0.40.1-stage59cp (version code 58)
- [ ] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and Stage 59C-P physical-device acceptance

Real payment processing remains deferred behind every production Stage 59 provider, merchant, accounting, security, legal and release-policy dependency.
## Stage 60A - nearby clinics and external navigation

- [x] request approximate foreground location only after Near me is tapped
- [x] send bounded coordinates to the hosted nearby endpoint without local persistence
- [x] show only authoritative hosted clinics in nearby mode
- [x] sort and display approximate straight-line distance
- [x] hand navigation to Google Maps or another compatible device app/browser
- [x] require no Google Maps SDK or API key
- [x] preserve denial, location-off, offline and no-coordinate fallback paths
- [x] add parser and API-free navigation URL tests
- [x] stable version increment to 0.41.0-stage60a (version code 59)
- [ ] GitHub Actions lint, unit tests and stable APK
- [x] stable in-place upgrade and Stage 60A physical-device acceptance

The App requests no background location and performs no location tracking. Road distance, traffic and embedded turn-by-turn navigation are not claimed.

### Stage 60A empty-nearby recovery correction

- [x] preserve the ordinary authoritative hosted-clinic catalog separately from nearby results
- [x] show a specific no-clinics-within-50-km state
- [x] provide Show all hosted clinics so navigation remains testable outside Mumbai
## Stage 60B - Patient Push consent UI

- [x] add an independent Push notifications switch to hosted communication preferences
- [x] persist and reload explicit Push consent through the authoritative API
- [x] clearly label provider delivery as unavailable
- [x] keep SMS OTP-only and preserve existing in-app notifications
- [x] version 0.42.0-stage60b (version code 61)
- [ ] GitHub Actions stable APK verification
- [ ] in-place upgrade and Stage 60B physical-device acceptance

No Firebase/Push SDK, Android notification permission, device token or provider credential is added in this provider-disabled foundation.

## Stage 60C - Firebase Patient client foundation

- [x] verify the supplied Firebase Android configuration belongs to `com.dolo.patient`
- [x] restore `google-services.json` from a protected GitHub Actions secret and keep it outside Git
- [x] add the current Google services plugin, Firebase BoM and main Cloud Messaging module
- [x] add Android 13+ notification permission and contextual consent request
- [x] create the privacy-safe Appointment updates notification channel
- [x] handle foreground Firebase messages with fixed generic lock-screen content
- [x] allow only bounded `/appointments/{id}` routes into the authenticated queue screen
- [x] retain only a SHA-256 registration-token fingerprint locally; never log or display the raw token
- [x] add registration/policy tests and corrective version 0.43.1-stage60c (version code 63)
- [x] show a prominent denial notice confirming login, booking, queue tracking and local data remain available
- [ ] GitHub Actions lint, unit tests and stable APK
- [ ] stable in-place upgrade and Stage 60C physical-device acceptance

Render-to-FCM sending remains disabled. The next checkpoint requires a dedicated least-privilege Firebase service account stored only in Render, an authenticated device-endpoint API, encrypted server-side token storage, logout/device revocation and the Stage 60B outbox sender worker.

- Stage 60D: real authenticated FCM endpoint registration and delivery - implementation complete; protected Render activation and physical-device checklist pending.


## Stage 61B-P - authoritative cache rehearsal

- [x] Android Keystore-encrypted hosted read cache with 24-hour maximum
- [x] visible live, cached-fresh and cached-stale status
- [x] one retry only for transient idempotent commands
- [x] HTTP 409 refresh-required recovery without overwrite
- [x] hosted cache purge on logout; Doctor cache also purges on role change
- [x] local prototype data remains separate and is never uploaded
- [x] version 0.45.0-stage61bp
- [ ] GitHub Actions and stable APK physical-device acceptance

## Stage 61B-P physical-device acceptance

On 11 August 2026, the complete Stage 61B-P device checklist passed across Patient, Doctor and Admin apps. Encrypted hosted-cache fallback, freshness labels, one bounded retry, conflict refresh, logout/role cache isolation and local-data separation are accepted for the controlled prototype.
## Stage 62C - Patient navigation and shared accessibility polish

- [x] standardize primary navigation to Home, Book and Appointments in that order
- [x] remove History and Profile from the persistent bottom navigation while retaining them through existing secondary navigation
- [x] use Material 3 navigation items with stable selected semantics and minimum touch-target behavior
- [x] remove directional screen-slide transitions and retain short, non-directional fades
- [x] preserve dark theme, safe system-bar handling and all verified Patient data/hosted workflows
- [x] add pure navigation and accessibility-policy regression coverage
- [x] version 0.46.0-stage62c (version code 67)
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and Stage 62C physical-device acceptance

Stage 62C changes presentation and navigation only. Login, booking, token allocation, live queue, hosted authority, local persistence, Push, Maps and payment boundaries are unchanged.
## Stage 62D - Patient reference theme and navigation consistency

- [x] translate `theme_refrence_day.jpeg` into a white/ice-blue, cobalt and cyan Material palette
- [x] translate `theme_refrence_night.jpeg` into layered deep-navy surfaces with accessible blue/cyan accents
- [x] apply the palette through shared theme tokens so existing cards, buttons, inputs, badges and text inherit it
- [x] replace duplicate bottom-navigation rendering with one shared Material 3 component
- [x] keep Home, Book and Appointments in the same order on every participating screen
- [x] correct Appointments Home navigation to pop back to the existing Home destination
- [x] preserve theme persistence, hosted/local data and all verified booking/queue/provider behavior
- [x] version 0.47.0-stage62d (version code 68)
- [x] GitHub Actions compile, lint, unit tests and stable APK
- [ ] stable APK in-place upgrade and Stage 62D physical-device acceptance

The supplied JPEGs are visual references only and are not embedded in the APK. No API, authentication, appointment, queue, persistence or provider contract changed.
## Stage 62E - Patient visual consistency corrections

- [x] remove the Home hero illustration and replace it with a white circular, gender-aware Patient avatar
- [x] add backward-compatible persisted Male/Female avatar selection under Profile & family
- [x] add the shared Home, Book and Appointments navigation to local and hosted Doctor detail pages
- [x] make Doctor Details headings explicitly visible against the active page background
- [x] use blue primary-container styling for Favourite Doctor actions and Appointments filters
- [x] remove daylight card/field outlines and decorative elevation while retaining layered Night surfaces
- [x] lighten the daylight primary blue while preserving 4.5:1 white-text contrast
- [x] preserve existing login, profile, family, appointment, queue and hosted data
- [x] version 0.48.0-stage62e (version code 69)
- [x] GitHub Actions compile, lint, unit tests and stable APK (run 31502904923)
- [x] stable APK in-place upgrade and Stage 62E physical-device acceptance

Existing profiles without a stored avatar gender load safely as Male and can be changed in Profile & family. No hosted schema or Patient data upload was added.

## Stage 62E physical-device acceptance

On 11 August 2026, Patient App  .48.0-stage62e passed the complete physical-device checklist. The gender-aware avatar, Doctor Details navigation/title, theme-correct Favourite and Appointment controls, flat Day surfaces, lighter primary blue, Night contrast, workflow regressions and local-data safety are accepted.
