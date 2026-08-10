# Stage 60C Patient Firebase client checklist

Run this checklist only after Patient App GitHub Actions passes for `0.43.0-stage60c` and the stable APK is installed over the existing app.

## Upgrade and existing-data safety

- [ ] Install the stable APK over the existing Patient App without uninstalling it.
- [ ] Confirm the existing login/session, local profile, family members, favourites, appointments and history remain intact.
- [ ] Confirm ordinary hosted booking, queue refresh and nearby clinic navigation still work.

## Firebase client registration and consent

- [ ] Sign in to the hosted prototype and open **Hosted Prototype Sync**.
- [ ] Under **Communication preferences**, wait up to 20 seconds and confirm **Firebase device registration ready** appears.
- [ ] Enable **Push notifications**, tap **Save preferences**, and grant Android notification permission when asked.
- [ ] Confirm the screen reports that Android notification permission was granted.
- [ ] Refresh connected data and confirm the Push preference remains enabled.
- [ ] Open Android **App info > Notifications** and confirm the **Appointment updates** channel exists and is enabled.

## Fail-closed boundaries

- [ ] Deny notification permission once if practical and confirm the App explains that it can be enabled later without breaking login, booking or local data.
- [ ] Confirm no raw Firebase token, Firebase project identifier or credential is displayed anywhere in the App.
- [ ] Confirm the App still states that server delivery is disabled pending its managed backend credential.
- [ ] Confirm SMS remains OTP-only and no promotional SMS control appears.

Stage 60C proves the protected Firebase Android configuration, device registration, permission UX, privacy-safe notification channel and bounded appointment route handling. It does not prove Render-to-FCM delivery. A dedicated backend service account, managed Render secret, device-endpoint API and sender worker remain the next provider activation checkpoint.
