# Stage 49B device checklist

Prerequisites:

- Platform API 0.36.2-stage49a is healthy on Render.
- Patient App GitHub Actions is green.
- Install the new stable APK over the existing Patient App.

## Upgrade and data safety

- [x] The APK updates the existing app without a package conflict.
- [x] Existing local profile, family, favourites, appointments and theme remain unchanged.
- [x] An existing hosted seeded session restores normally after restart.

## Authoritative registration readiness

1. Log out to the mobile-number login screen.
2. Tap **Create an Account**.

- [x] A short checking state appears while the hosted readiness contract loads.
- [x] The result clearly says production registration is not open.
- [x] The future ID format is DLO-PAT-NNNNNN.
- [x] The message says the ID will not contain the phone number or location.
- [x] Terms, Privacy and Health Data consent are identified as future requirements.
- [x] No registration form opens and no real Patient information is requested.
- [x] No SMS is sent and the production OTP endpoint is not called.

## Fail-closed recovery

1. Turn off mobile data and Wi-Fi.
2. Reopen the login screen and tap **Create an Account**.

- [x] Status verification fails safely and account creation remains disabled.
- [x] No cached status enables registration.
- [x] Restore connectivity and tap **Create an Account** again.
- [x] The authoritative disabled status recovers without reinstalling the app.

## Existing login regression

- [x] A valid 10-digit demo number still opens the OTP screen.
- [x] Demo OTP 123456 still signs in; no SMS is expected.
- [x] Hosted prototype confirmation, DO-LO identity and hosted data still load.
- [x] Logout, restart/session restoration and offline local-data safety remain unchanged.

Production enrollment, real OTP/SMS delivery, public-ID allocation and external providers are not activated in Stage 49B.