- [ ] Stage 9- [x] Stage 9# DO-LO Android Ecosystem Roadmap

## Current scope

Only the dedicated Patient App is in development. Doctor App and Admin App are intentionally deferred.

## Stages

- [x] Stage 1 — Lean Android project, Compose theme, navigation skeleton, placeholder screens, reusable components, dummy models, documentation
- [x] Stage 2 — Local state architecture, validation, fake OTP contract, persisted session and authentication repository
- [x] Stage 3 — API-ready contracts, offline repository, persisted appointments, generated tokens and repository tests
- [x] Stage 4 — Doctor search, filters, profiles, clinics and favourites
- [x] Stage 5 — Live queue simulation, lifecycle states, waiting estimates and one-time rescheduling
- [x] Stage 6 — Live queue tracking, estimates and foreground/background refresh strategy
- [x] Stage 7 — Favourites, reviews, missed-appointment rescheduling
- [x] Stage 8 — Provider-ready maps, payments, SMS and push-notification foundations
- [ ] Stage 9 — accessibility, offline/error states, tests, performance and release hardening
- [ ] Future — Create separate Doctor App, then separate Admin App, reusing contracts/design tokens without coupling app builds

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
- [x] Stage 9 — accessibility, offline/error states, tests, performance and release hardening

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


## Stage 4 — Patient discovery and engagement (complete)

- Doctor search by name, specialty, and clinic is connected to PatientViewModel.
- Category filtering and empty search states are implemented.
- Doctor details provide clinic, session, fee, rating, and booking entry point.
- Favourite doctors persist locally and appear on Home and the dedicated Favourites screen.
- Appointment History reads persisted bookings.
- GitHub Actions tests and debug APK assembly passed in run 29201170208.


## Stage 5 — Live queue and appointment lifecycle (complete)

- Queue snapshot persists current token for each appointment.
- Patients-ahead and estimated waiting time use a 12-minute configurable prototype average.
- Live Queue screen supports refresh and deterministic demo advancement.
- Appointment lifecycle includes BOOKED, WAITING, IN_CONSULTATION, COMPLETED and MISSED states.
- Missed appointments are eligible for one reschedule within 10 days; successful reschedule is stored for the next day.
- Legacy Stage 3/4 appointment records remain readable.
- Queue calculations and codec compatibility have unit tests.
- GitHub Actions tests and debug APK assembly passed in run 29226964239.


## Stages 6 and 7 — Queue synchronization and patient engagement (complete)

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


## Stage 8 — Provider integration foundations (complete)

- Provider-neutral contracts exist for maps/navigation, payment order creation and verification, SMS OTP/reminders, and push registration/queue alerts.
- Disabled provider implementations fail safely and never create navigation links, payment orders, messages or device registrations.
- A central integration registry reports provider mode and readiness without storing credentials.
- The Patient App exposes an Integration Readiness screen from Help & Support.
- All external providers default to DISABLED; no API keys, payment data, precise location or device tokens are committed.
- Pure unit tests verify capability defaults and disabled-provider behavior.
- No third-party SDK or additional build weight was added.
- GitHub Actions unit tests and debug APK assembly passed in run 29232911280.


## Stage 9 — Patient release hardening (complete)

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
