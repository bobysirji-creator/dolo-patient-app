# Stage 50B device checklist

Prerequisites:

- Platform API 0.37.0-stage50a is live and its hosted checklist is accepted.
- Patient App GitHub Actions is green.
- Install the new stable APK over the existing Patient App.

## Upgrade and local-data safety

- [ ] Stable APK updates the existing app without a package conflict.
- [ ] Existing profile, family, favourites, appointments, theme and hosted session remain unchanged.
- [ ] Restart restores the existing signed-in session normally.

## Authoritative activation transparency

1. Log out to the mobile-number Login screen.
2. Tap **Create an Account** while online.

- [ ] A short checking state appears.
- [ ] Production registration is clearly shown as not open.
- [ ] Exactly seven safety requirements appear:
  - Managed OTP provider
  - Distributed abuse protection
  - Versioned Terms, Privacy and Health Data consent
  - Account recovery and duplicate-account policy
  - Data retention, correction and deletion policy
  - India production security review
  - Atomic enrollment transaction review
- [ ] Future DO-LO ID format is `DLO-PAT-NNNNNN`.
- [ ] No registration form opens and no phone, profile, family or consent data is requested or uploaded.
- [ ] No SMS, Push, Maps or Payments provider action occurs.

## Fail-closed recovery

1. Turn off Wi-Fi and mobile data.
2. Reopen Login and tap **Create an Account**.

- [ ] Status verification fails safely and account creation remains disabled.
- [ ] Restore connectivity and tap **Create an Account** again.
- [ ] The seven authoritative blocked requirements recover without reinstalling.

## Existing login regression

- [ ] A valid 10-digit demo number opens OTP verification.
- [ ] Demo OTP `123456` signs in and sends no SMS.
- [ ] Hosted prototype identity/data load after login.
- [ ] Logout, full restart, session restoration and offline local-data safety remain unchanged.

Production enrollment, real OTP delivery, public-ID allocation and all external providers remain disabled in Stage 50B.
