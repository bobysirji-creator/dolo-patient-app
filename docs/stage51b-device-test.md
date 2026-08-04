# Stage 51B device checklist

Use the stable signed APK from GitHub Actions. Install it over the accepted Stage 50B Patient App; do not uninstall first.

## Build and upgrade

- [ ] GitHub Actions compile, lint, unit tests and stable APK complete successfully.
- [ ] The stable APK installs over the existing app without a package conflict.
- [ ] Existing local profile, family members, favourites, appointments, history and theme remain intact.

## Reserved consent catalog

1. Keep the phone connected to the internet.
2. Open Login and tap Create an Account.
3. Confirm the notice still says production registration is not open.
4. Confirm all seven Stage 50A safety requirements remain visible.
5. Under Required consent documents, confirm exactly:
   - Terms: reserved, not published
   - Privacy: reserved, not published
   - Health Data: reserved, not published
6. Confirm the future ID remains DLO-PAT-NNNNNN.
7. Confirm the notice says documents are not published and consent cannot be submitted.
8. Confirm there is no registration form, legal acceptance checkbox, consent button or phone/profile/family upload.

## Fail-closed and regression checks

- [ ] Turn off Wi-Fi/mobile data, reopen Login and tap Create an Account.
- [ ] Registration status becomes unavailable and account creation remains disabled.
- [ ] Reconnect and retry; the authoritative reserved catalog returns.
- [ ] Demo mobile login and OTP 123456 still work.
- [ ] Hosted session restores after force-close/relaunch.
- [ ] Logout still clears the hosted session.
- [ ] Existing Home, discovery, booking, history, family and live-queue workflows remain functional.
- [ ] SMS, Push, Maps and Payments remain disabled.

Production enrollment, legal consent collection, real OTP delivery and public-ID allocation are not activated in Stage 51B.