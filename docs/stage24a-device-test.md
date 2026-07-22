# Stage 24A physical-device checklist

Prerequisites: Patient hosted login is valid and Platform API `0.14.0-stage23a` remains healthy.

- [ ] GitHub Actions passes and the stable Patient APK updates the installed app without uninstalling.
- [ ] Existing local profile, family, favourites, reviews, notifications and appointments remain unchanged.
- [ ] After hosted login, Home automatically shows a distinct `Hosted appointments` section without opening Hosted Prototype Sync first.
- [ ] An active SELF or FAMILY appointment shows the correct Doctor, patient, date, session and token.
- [ ] The matching current token, patients ahead, estimated wait and countdown state appear when live data is available.
- [ ] From the Doctor App, admit or advance that appointment; keep Patient Home open and confirm it refreshes within approximately 15 seconds.
- [ ] Tap the hosted card or `View all`; confirm the complete hosted booking, receipt, history and reschedule workspace opens.
- [ ] Publish or reuse an active Doctor announcement/Admin broadcast and confirm the latest update appears on Home.
- [ ] Complete the appointment and confirm it leaves the active Home section but remains in hosted history with its receipt.
- [ ] Disable connectivity, refresh and confirm the visible last hosted snapshot/local data are retained with an actionable error; reconnect successfully.
- [ ] Log in through local fallback and confirm the hosted Home section is not shown.
- [ ] Confirm Maps, Payments, SMS and Push remain disabled.

The next-day Stage 23A observation may be completed during this checklist using fresh seeded dummy appointments.