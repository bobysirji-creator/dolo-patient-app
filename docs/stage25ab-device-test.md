# Stages 25A-25B physical-device checklist

Prerequisites: Platform API `0.15.0-stage25ab` is deployed, migration 019 is applied, Patient hosted login is valid, and at least one owned hosted appointment is COMPLETED.

- [x] API GitHub Actions passes, including PostgreSQL migration/runtime verification.
- [x] Render reports `0.15.0-stage25ab`, ready status, stage `25.2`, transport `PENDING_PATIENT_REVIEWS`, and all external providers disabled.
- [x] Patient GitHub Actions passes and the stable APK updates the installed app without uninstalling.
- [x] Existing local profile, family, favourites, local reviews, notifications and appointments remain unchanged.
- [x] A BOOKED, WAITING, IN_CONSULTATION, ABSENT or RESCHEDULED hosted appointment does not show the hosted review form.
- [x] A COMPLETED hosted appointment without a review shows rating choices 1-5, an optional bounded comment and `Submit hosted review`.
- [x] Submit a distinctive rating/comment and confirm the same appointment immediately shows `Submitted - pending Admin moderation`.
- [x] Refresh, leave/reopen the screen, fully close/relaunch the app and confirm the same hosted review remains visible.
- [x] Confirm the completed appointment cannot submit a second review; tapping/retrying during a slow response does not create duplicates.
- [x] If both SELF and FAMILY have completed appointments, confirm each can have one independent review with the correct patient/appointment association.
- [x] Disable connectivity before a submission; confirm a clear failure, no false success, and all previously loaded hosted/local data remains safe. Reconnect and retry successfully.
- [x] Confirm no pending review appears in public Patient Doctor discovery yet; Doctor visibility and Admin moderation are reserved for Stages 25C-25D.
- [x] Confirm Maps, Payments, SMS and Push remain disabled.

Accepted on 23 July 2026: every checklist item passed successfully.
