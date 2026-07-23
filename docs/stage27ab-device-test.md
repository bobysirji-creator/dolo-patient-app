# Stages 27A-27B Patient notification device checklist

Prerequisites: API `0.18.0-stage27ab` is deployed and ready; API and Patient Actions are green; install Patient App `0.22.0-stage27b` over the existing stable app. Keep the Doctor App available to create authoritative queue changes.

- [ ] Existing hosted login, booking, family, history, queue, reviews, support and all local data remain intact.
- [ ] Confirm `/health` reports `0.18.0-stage27ab`, `/ready` is ready, and capabilities report stage `27.2` with `patientNotifications = SERVER_READ_CURSOR_IN_APP_ONLY`.
- [ ] Log in with the seeded hosted Patient and refresh Home; confirm the bell shows an unread badge when hosted appointment events are unread.
- [ ] Open Notifications and confirm hosted cards show a clear title, message, patient name and matching token number.
- [ ] Return Home after the notification refresh completes; confirm the unread badge clears.
- [ ] Fully close and relaunch the Patient App; confirm the already-read hosted cards remain read and the badge does not return without a new event.
- [ ] In Doctor App, perform one valid action on the seeded Patient appointment, such as admitting it, calling its token or completing it.
- [ ] Wait for the Patient Home automatic refresh (up to 15 seconds); confirm the bell badge returns.
- [ ] Open Notifications and confirm the newest card describes the matching Doctor action and correct SELF/FAMILY patient and token.
- [ ] If both SELF and FAMILY have events, confirm their notification identities and tokens are not mixed.
- [ ] Log out and sign in again online; confirm the server read state remains consistent.
- [ ] Disable connectivity and refresh; confirm the previous hosted snapshot/local data remain safe and no notification is falsely marked or duplicated.
- [ ] Reconnect and confirm authoritative notifications recover without duplication.
- [ ] Confirm there is no Android system push notification and no SMS; Push, SMS, Maps and Payments remain disabled.