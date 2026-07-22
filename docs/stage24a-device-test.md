# Stage 24A physical-device checklist

Prerequisites: Patient hosted login is valid and Platform API `0.14.0-stage23a` remains healthy.

- [x] GitHub Actions passes and the stable Patient APK updates the installed app without uninstalling.
- [x] Existing local profile, family, favourites, reviews, notifications and appointments remain unchanged.
- [x] After hosted login, Home automatically shows a distinct `Hosted appointments` section without opening Hosted Prototype Sync first.
- [x] An active SELF or FAMILY appointment shows the correct Doctor, patient, date, session and token.
- [x] The matching current token, patients ahead, estimated wait and countdown state appear when live data is available.
- [x] From the Doctor App, admit or advance that appointment; keep Patient Home open and confirm it refreshes within approximately 15 seconds.
- [x] Tap the hosted card or `View all`; confirm the complete hosted booking, receipt, history and reschedule workspace opens.
- [ ] Publish or reuse both an active Doctor announcement and Admin broadcast; confirm one card for each appears on Home.
- [x] Complete the appointment and confirm it leaves the active Home section but remains in hosted history with its receipt.
- [x] Disable connectivity, refresh and confirm the visible last hosted snapshot/local data are retained with an actionable error; reconnect successfully.
- [x] Log in through local fallback and confirm the hosted Home section is not shown.
- [x] Confirm Maps, Payments, SMS and Push remain disabled.

The next-day Stage 23A observation may be completed during this checklist using fresh seeded dummy appointments.

Initial device run on 22 July 2026 passed every item except simultaneous Doctor/Admin Home visibility. Version 0.18.1-stage24a corrects the bounded Home feed; retest only the unchecked item plus upgrade/local-data safety.
