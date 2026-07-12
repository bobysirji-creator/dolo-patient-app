# DO-LO Android Ecosystem Roadmap

## Current scope

Only the dedicated Patient App is in development. Doctor App and Admin App are intentionally deferred.

## Stages

- [x] Stage 1 — Lean Android project, Compose theme, navigation skeleton, placeholder screens, reusable components, dummy models, documentation
- [x] Stage 2 — Local state architecture, validation, fake OTP contract, persisted session and authentication repository
- [ ] Stage 3 — REST client, secure token storage, environments, backend contract tests
- [ ] Stage 4 — Doctor search, filters, profiles, clinics and availability
- [ ] Stage 5 — Walk-in booking, concurrency-safe token allocation and appointment history
- [ ] Stage 6 — Live queue tracking, estimates and foreground/background refresh strategy
- [ ] Stage 7 — Favourites, reviews, missed-appointment rescheduling
- [ ] Stage 8 — Provider integrations: maps, payments, SMS and push notifications
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
