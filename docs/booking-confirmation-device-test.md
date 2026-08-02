# Booking Confirmation physical-device checklist

Build: Patient App 0.34.0-confirmation-ui (version code 51)
Reference: C:\Users\Poly\Documents\codex\2026-07-11\Booking_confirm.png

## Build and upgrade

- [ ] GitHub Actions is green for compile, lint, unit tests and stable APK.
- [ ] The stable APK installs over the accepted Patient App without data loss.
- [ ] Existing login, profile, family members, favourites, appointments and queue data remain present.

## Confirmed booking

- [ ] Complete a booking and confirm the page opens for the newly created appointment ID.
- [ ] Success heading, token, date, session, Doctor, clinic, Patient and estimate are correct.
- [ ] Token 1, a two-digit token and a three-digit token remain centred and readable.
- [ ] Morning and Evening sessions show independently and do not alter booking data.
- [ ] Doctor Profile, notification bell, Map placeholder, Appointments and Back to Home actions work.

## Quick actions and privacy

- [ ] Add to Calendar opens the Android event editor and requests no calendar permission.
- [ ] Share opens the Android share sheet and excludes Patient name, phone, family and medical data.
- [ ] Save shows the future-PDF placeholder and does not request storage permission.

## State, accessibility and layout

- [ ] Missing queue data shows a safe estimate-unavailable message rather than zero or a crash.
- [ ] Retry/View Appointments/Home are reachable in the failed state.
- [ ] 320 dp phone width has no horizontal clipping; quick actions stack when required.
- [ ] Increased font size preserves every action and important value.
- [ ] TalkBack announces the token, notification count, portrait and controls clearly.
- [ ] Light and Dark Mode maintain readable contrast.
- [ ] Compare a screenshot with the reference at the same viewport and note visual differences.