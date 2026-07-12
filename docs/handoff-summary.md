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

