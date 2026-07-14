# DO-LO Patient App

DO-LO Patient is a lightweight Kotlin and Jetpack Compose Android prototype for booking walk-in doctor consultations, receiving a token and following a live queue from home.

Current release candidate: **0.9.0-rc2** (version code 10).

## What works

- mobile-number login with the local demo OTP;
- doctor categories, search, details and persistent favourites;
- walk-in booking with selectable date, morning/evening session and family member;
- local token allocation, appointment history and lifecycle timeline;
- foreground live-queue refresh, waiting estimate, stale/offline states and retry;
- one-time missed-appointment rescheduling;
- verified local reviews and in-app notifications;
- accessible labels and minimum touch targets.

Maps, payments, SMS and push-notification providers remain deliberately disabled. The app does not contact a production healthcare backend.

## Demo login

1. Enter any valid 10-digit mobile number.
2. Enter OTP `123456`.
3. Use the app with demonstration data only.

## Low-resource installation

You do not need to run Android Studio and Codex together.

1. Open the latest successful run under [GitHub Actions](https://github.com/bobysirji-creator/dolo-patient-app/actions).
2. Download the `dolo-patient-debug-apk` artifact.
3. Extract the ZIP and compare the APK with its `.sha256` file.
4. Transfer the APK to the Android phone, allow installation from that trusted source, and install.
5. Disable that installation permission again after testing.

The debug APK is for controlled physical-device testing, not Play Store publication.

## Optional local build

Only if you later choose to build locally, use JDK 17 and Android SDK 35:

```powershell
.\gradlew.bat --no-daemon :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

The project keeps Gradle memory conservative and does not require an emulator. A physical Android device is preferred.

## Architecture

The repository is a single Patient App module. Compose screens use ViewModels and provider-neutral repository/service contracts. Local implementations preserve the demonstrated flow while leaving boundaries for HTTPS REST APIs, authoritative OTP, appointments, tokens, live queue, maps, payments, SMS and push providers.

Doctor App and Admin App are intentionally separate future projects; they are not coupled to this build.

## Safety and documentation

- This prototype is not for emergencies, diagnosis, prescriptions or real medical records.
- Review [Privacy Notice](docs/privacy-notice.md).
- Follow the [Release Checklist](docs/release-checklist.md).
- See the [Roadmap](docs/roadmap.md) and [Handoff Summary](docs/handoff-summary.md).
