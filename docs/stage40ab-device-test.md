# Stages 40A-40B fail-closed Patient identity checklist

Prerequisites: Platform API `0.31.0-stage40ab` must pass GitHub Actions and deploy live before Patient App `0.25.0-stage40b` (version code 31) is built and installed.

## Hosted API

- [x] `/health` reports `0.31.0-stage40ab`; `/ready` reports ready with no blockers.
- [x] Capabilities report stage `40.2`, transport `FAIL_CLOSED_PATIENT_IDENTITY_FOUNDATION`, Patient enrollment `PROVIDER_GATED_DISABLED`, and real Patient data acceptance `DISABLED`.
- [x] Enrollment readiness shows production enrollment, OTP provider, profile enrollment and family enrollment as disabled; OTP is authentication-only and DO-LO ID issuance is reserved.
- [x] The production OTP challenge returns HTTP 503 `REAL_PATIENT_ENROLLMENT_DISABLED`; no SMS is sent.

## Patient login and regression

- [x] Install the stable APK over the existing Patient App; local profile, family, favourites, appointments, reviews and notifications remain safe.
- [x] Log out and confirm separate `Production Patient account` and `Seeded demo login` cards.
- [x] Production account creation is disabled and the card says no phone/profile/family details are uploaded.
- [x] The seeded demo accepts a valid 10-digit demo number and states that it is retained locally while the server uses `patient-demo`.
- [x] The OTP screen says no SMS was sent; OTP `123456` completes hosted seeded login.
- [x] Home confirms hosted prototype mode and existing hosted discovery, booking, history and queue flows still work.
- [x] Completely close/relaunch; the hosted seeded session restores.
- [x] Log out; the session clears and the identity-mode screen returns.
- [x] Offline readiness failure never enables production registration; demo local fallback remains recoverable.
- [x] Reconnect; authoritative disabled readiness returns.
- [x] No SMS, Push, Maps, Payments or media provider activity occurs.

Record the API Actions run, Render deployment, Patient Actions run, stable APK upgrade and observations before accepting Stage 40AB.
