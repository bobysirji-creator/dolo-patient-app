# Stage 22A physical-device checklist

Prerequisites: Platform API `0.13.0-stage22a` is live, the seeded Doctor is active/VERIFIED, and an enabled replacement session exists inside the reschedule window.

- [x] Stable Patient APK updates the existing app without uninstalling; all local and hosted Stage 21 data remains intact.
- [x] Hosted login and refresh show Stage 22A without changing the fixed SELF/FAMILY profiles.
- [x] Book a hosted appointment, then use the Doctor App to confirm clinic fee/receipt and mark that appointment ABSENT.
- [x] Refresh Patient Hosted Prototype Sync; only the ABSENT unused appointment shows `Reschedule missed appointment`.
- [x] Open the action and confirm only enabled same-day/later candidates inside the configured reschedule window are offered.
- [x] Choose a replacement session and confirm the original becomes RESCHEDULED while one new BOOKED appointment receives a new token for the same patient name.
- [x] Retry/refresh repeatedly and confirm no duplicate replacement token is allocated.
- [x] Confirm the replacement cannot be rescheduled again, including after it is later marked ABSENT.
- [x] Set Doctor future booking to current-day-only and confirm an eligible later reschedule choice remains available but is not offered for ordinary booking.
- [x] Confirm Doctor hosted appointments show both the RESCHEDULED original and replacement lineage/status safely.
- [x] Fully close/relaunch Patient and Doctor apps; sessions, history, replacement and queue state restore correctly.
- [x] While offline, refresh/reschedule fails visibly and no local or previously loaded hosted data is changed; reconnect successfully.
- [x] Confirm local profile, family, favourites, reviews and appointments remain private and unchanged.
- [x] Confirm Maps, Payments, SMS and Push remain disabled.

Use only seeded dummy records. Do not enter real patient or family information.
Accepted on 22 July 2026. An additional observation retest is planned for 23 July 2026.
