# DO-LO Patient App Release Checklist

## Release candidate

- Version name: `0.9.0-rc3`
- Version code: `11`
- Build type: debug APK for controlled testing
- Maps, payments, SMS and push notifications: disabled

## Automated gates

The GitHub Actions build must pass all of these before the APK is shared:

- Android lint: `:app:lintDebug`
- Unit tests: `:app:testDebugUnitTest`
- APK assembly: `:app:assembleDebug`
- SHA-256 checksum generation
- Artifact upload containing the APK and checksum

## Physical-device acceptance

- [ ] Install or upgrade the APK on a physical Android device.
- [ ] Login accepts a valid 10-digit mobile number and demo OTP `123456`.
- [ ] Doctor search, categories, doctor details and favourites work.
- [ ] Booking preserves the selected date, session, patient and family member.
- [ ] Appointment and token details remain after closing and reopening the app.
- [ ] Live queue refreshes and shows current, stale and offline states correctly.
- [ ] Retry works after restoring connectivity.
- [ ] A missed appointment can be rescheduled once and keeps the updated date.
- [ ] Completion, review and in-app notification flows work.
- [ ] Logout clears the active local session as designed.

## Accessibility acceptance

- [ ] TalkBack announces the logo, navigation, search, token values and primary actions clearly.
- [ ] Controls remain usable at 200% font size.
- [ ] Text and controls have acceptable contrast in the light theme.
- [ ] Touch targets are at least 48 dp.
- [ ] Core flows work in portrait on a small phone without clipped actions.

## Privacy and security acceptance

- [ ] Android backup remains disabled.
- [ ] Cleartext HTTP traffic remains disabled.
- [ ] No API keys, provider credentials or real patient records are present.
- [ ] Disabled provider screens do not imply that a real payment, SMS, map or push action occurred.
- [ ] The privacy notice matches the actual build.

## Public-release blockers

Do not publish this release candidate to Google Play until all of these are resolved:

- production backend and authoritative authentication/OTP;
- server-owned appointment, token and live-queue state;
- approved provider integrations and policies;
- release signing and protected signing-key process;
- account deletion and data export;
- production privacy policy, retention rules and legal review;
- clinic/doctor operational validation;
- Doctor App and Admin App integration.

## Release rule

Only an APK from a successful final GitHub Actions run may be tested. Compare its SHA-256 checksum with the checksum file after download. This release candidate is not a production medical service and must not be used for emergencies.
