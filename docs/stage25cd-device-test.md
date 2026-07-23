# Stages 25C-25D physical-device checklist

Prerequisites: Platform API 0.16.0-stage25cd is deployed, migration 020 is applied, and the Stage 25AB submitted review remains PENDING or a fresh COMPLETED appointment can submit one.

1. Confirm API GitHub Actions is green, including checksum migrations and PostgreSQL runtime verification.
2. Confirm Render reports version 0.16.0-stage25cd, ready status, stage 25.4, transport MODERATED_PUBLISHED_PATIENT_REVIEWS, and all external providers disabled.
3. Confirm Admin, Doctor and Patient Actions are green and each stable APK updates the existing installation without uninstalling.
4. In Patient App, submit a distinctive hosted review from a COMPLETED appointment. Confirm it says PENDING and does not affect the local profile, family, favourites, reviews, notifications or appointments.
5. In public Hosted doctors discovery, confirm that the PENDING review does not change the published count/average.
6. In Doctor App as Doctor, open Hosted staff queue and confirm the PENDING review is not visible.
7. In Doctor App as Assistant, confirm no Published Patient reviews section or other Doctor-only review action appears.
8. In Admin App, open Reviews and confirm the pending Patient review shows the correct patient, doctor, clinic, rating, comment and date.
9. Add an optional moderation note and choose Publish. Confirm the review disappears from the pending Admin queue.
10. Refresh Patient discovery. Confirm the published count/average updates, but no Patient name or comment appears publicly.
11. Refresh Doctor hosted staff. Confirm the published review appears read-only under the correct clinic.
12. Close/relaunch all three apps. Confirm the published state, Doctor feed and public aggregate remain correct.
13. Submit a second review when eligible and choose Hide or Reject. Confirm it leaves the pending queue but never appears to Doctor or public discovery.
14. Retry a moderation action during a slow response. Confirm no duplicate decision or duplicate public count is created.
15. Disable connectivity in each app, refresh, and confirm a clear error with previously loaded/local data remaining safe. Reconnect and recover.
16. Confirm billing, Doctor control, profile reviews, announcements, schedule, queue and clinic receipt workflows still work.
17. Confirm Maps, Payments, SMS and Push remain disabled.
