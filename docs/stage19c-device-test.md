# Stage 19C Patient approved-profile device test

Prerequisite: Platform API `0.10.0-stage19c` is Live and Dr. Ananya Mehta is active and VERIFIED.

1. Confirm Patient GitHub Actions is green and install the stable APK over the existing Patient App.
2. Confirm login/session restoration and all existing local profile, family, favourites, reviews and appointment data remain intact.
3. Open Categories or Search Doctors, refresh, and open Dr. Ananya Mehta under Hosted doctors.
4. Confirm the page shows the Doctor name, specialty, Admin-approved label, registration, qualification, experience, about, clinic/city and clinic-direct consultation fee.
5. Search using a word from the approved qualification/about or the registration number and confirm the hosted Doctor is found.
6. Tap Book hosted appointment and confirm the existing Hosted Prototype Sync booking screen opens and authoritative booking still works.
7. In Doctor App submit a distinctive profile change. Before Admin approval, refresh Patient discovery and confirm the pending value is absent.
8. Reject that revision in Admin App. Refresh Patient discovery and confirm the rejected value remains absent and the previous approved profile remains.
9. Submit another distinctive change and approve it in Admin App. Refresh Patient discovery and confirm only the newly approved values appear.
10. Temporarily set the Doctor PENDING or inactive in Admin App. Refresh Patient discovery and confirm the hosted Doctor disappears or the open profile fails safely without exposing stale proposed data. Restore VERIFIED and active afterward.
11. Disable network, retry refresh, and confirm the app does not overwrite or upload local Patient data. Reconnect and confirm recovery.
12. Confirm Maps, Payments, SMS and Push remain disabled.