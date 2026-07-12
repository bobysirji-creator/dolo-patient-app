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

Stage 2: introduce feature ViewModels/repositories, proper phone validation, OTP request/verify interfaces, session persistence, and unit tests—using a fake provider until the backend/API contract is agreed.



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
