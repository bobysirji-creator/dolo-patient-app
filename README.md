# DO-LO Patient App

DO-LO Patient is a lightweight Kotlin and Jetpack Compose Android prototype for booking walk-in doctor consultations, receiving a token and following a live queue from home.

Current integration build: **0.14.0-stage19c** (version code 19).

## What works

- mobile-number login with the demo OTP and a controlled hosted dummy identity, with local fallback;
- 12 illustrated doctor categories, 24 test doctors, search, details and persistent favourites;
- walk-in booking with selectable date, morning/evening session and family member;
- local token allocation, appointment history and lifecycle timeline;
- foreground live-queue refresh, waiting estimate, stale/offline states and retry;
- one-time missed-appointment rescheduling;
- verified local reviews and in-app notifications;
- accessible labels and minimum touch targets;
- public connection checks plus securely stored, backend-issued prototype access and refresh tokens.

Maps, payments, SMS and push-notification providers remain deliberately disabled. Stage 16C sends only the fixed `patient-demo` identity and demo OTP for authentication, then uses a separate seeded dummy profile/clinic flow for authoritative booking and queue reads. The typed mobile number and all existing local profiles, family members, bookings, favourites and reviews are not uploaded.

## Demo login

1. Enter any valid 10-digit mobile number.
2. Enter OTP `123456`.
3. Use the app with demonstration data only.

## Low-resource installation

You do not need to run Android Studio and Codex together.

1. Open the latest successful run under [GitHub Actions](https://github.com/bobysirji-creator/dolo-patient-app/actions).
2. Download the `dolo-patient-stable-debug-apk` artifact.
3. Extract the ZIP and compare the APK with its `.sha256` file.
4. Transfer the APK to the Android phone, allow installation from that trusted source, and install.
5. Disable that installation permission again after testing.

The stable-debug APK uses a persistent prototype certificate for repeatable device upgrades. It is still only for controlled physical-device testing, not Play Store publication. The first stable installation requires uninstalling an older temporary-key build once; later stable APKs can update in place.

## Optional local build

Only if you later choose to build locally, use JDK 17 and Android SDK 35:

```powershell
.\gradlew.bat --no-daemon :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

The project keeps Gradle memory conservative and does not require an emulator. A physical Android device is preferred.

## Architecture

The repository is a single Patient App module. Compose screens use ViewModels and provider-neutral repository/service contracts. `platform/PlatformApi.kt` provides a lightweight, read-only HTTPS boundary for the hosted prototype. Local implementations preserve the demonstrated booking flow while leaving authenticated REST boundaries for OTP, appointments, tokens, live queue, maps, payments, SMS and push providers.

The Doctor App and platform API are separate projects. The future Admin App will also remain separate; none is coupled to this Patient App build.

## Safety and documentation

- This prototype is not for emergencies, diagnosis, prescriptions or real medical records.
- Review [Privacy Notice](docs/privacy-notice.md).
- Follow the [Release Checklist](docs/release-checklist.md).
- See the [Roadmap](docs/roadmap.md) and [Handoff Summary](docs/handoff-summary.md).

## Stage 16C authoritative dummy flow

Support > Integration readiness now opens a separate hosted prototype screen. It uses only seeded dummy records and the protected Platform API for server-authoritative booking, token history and live-queue polling. Existing local Patient data is not migrated or uploaded, and the local workflow remains available when the prototype service is offline. See `docs/stage16c-device-test.md`.

## Stage 18B hosted communication feed

Hosted Prototype Sync now displays active Doctor announcements and DO-LO Admin broadcasts. The feed is server-authoritative and in-app only; local Patient data remains isolated and external providers remain disabled.

## Stage 19C approved hosted Doctor profiles

Hosted Doctor cards now open a dedicated profile page showing only Admin-approved registration, qualification, experience, about, clinic and consultation-fee details. Search includes the approved metadata, while booking continues through the accepted authoritative Hosted Prototype Sync flow. Pending/rejected Doctor edits and Admin review notes are not consumed or displayed. Existing local Patient data is untouched.

## Stage 21B seeded family booking

Hosted Prototype Sync now lets the user select either the fixed `Prototype Patient` or `Prototype Family Member` before booking. Each profile uses an independent retry key, while legacy SELF retry keys remain compatible so an in-place upgrade cannot silently duplicate a prior booking. These are server-seeded dummy profiles only; the existing local profile and family list remain private and unchanged.
