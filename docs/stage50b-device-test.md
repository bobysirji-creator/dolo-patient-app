# Stage 50B device checklist

Prerequisites:

- Platform API 0.37.0-stage50a is live and its hosted checklist is accepted.
- Patient App GitHub Actions is green.
- Install the new stable APK over the existing Patient App.

## Upgrade and local-data safety

- [x] Stable APK updates the existing app without a package conflict.
- [x] Existing profile, family, favourites, appointments, theme and hosted session remain unchanged.
- [x] Restart restores the existing signed-in session normally.

## Authoritative activation transparency

1. Log out to the mobile-number Login screen.
2. Tap **Create an Account** while online.

- [x] A short checking state appears.
- [x] Production registration is clearly shown as not open.
- [x] Exactly seven safety requirements appear:
  - Managed OTP provider
  - Distributed abuse protection
  - Versioned Terms, Privacy and Health Data consent
  - Account recovery and duplicate-account policy
  - Data retention, correction and deletion policy
  - India production security review
  - Atomic enrollment transaction review
- [x] Future DO-LO ID format is `DLO-PAT-NNNNNN`.
- [x] No registration form opens and no phone, profile, family or consent data is requested or uploaded.
- [x] No SMS, Push, Maps or Payments provider action occurs.

## Fail-closed recovery

1. Turn off Wi-Fi and mobile data.
2. Reopen Login and tap **Create an Account**.

- [x] Status verification fails safely and account creation remains disabled.
- [x] Restore connectivity and tap **Create an Account** again.
- [x] The seven authoritative blocked requirements recover without reinstalling.

## Existing login regression

- [x] A valid 10-digit demo number opens OTP verification.
- [x] Demo OTP `123456` signs in and sends no SMS.
- [x] Hosted prototype identity/data load after login.
- [x] Logout, full restart, session restoration and offline local-data safety remain unchanged.

Production enrollment, real OTP delivery, public-ID allocation and all external providers remain disabled in Stage 50B.
