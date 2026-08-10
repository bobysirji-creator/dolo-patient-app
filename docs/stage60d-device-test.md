# Stage 60D Patient device checklist

Prerequisites: API Actions green, Render serves `0.55.2-stage60d` with `providers.push=true`, Patient Actions green, and the stable Patient APK installed over the existing app.

- [ ] Existing local profile, family members, favourites, appointments, reviews, theme and hosted session remain intact after the APK update.
- [ ] Hosted login, discovery, booking, history and live queue still work.
- [ ] In **Connected care > Communication preferences**, enable Appointment updates and Push notifications, save, and allow Android notifications.
- [ ] Refresh and confirm the preference reports `DEVICE_REGISTERED`; no raw Firebase token or project credential is displayed.
- [ ] Put the Patient App in the background. From the Doctor App, perform a queue action for this Patient appointment and wait up to one minute.
- [ ] A privacy-safe **DO-LO appointment update** system notification appears without a Patient name, Doctor name, token number, diagnosis or health detail.
- [ ] Tap the notification and confirm DO-LO opens the matching appointment/live-queue destination. An unknown or malformed route must open no privileged screen.
- [ ] Completely close the Patient App, trigger another eligible queue action, and confirm delivery still arrives.
- [ ] Turn Push notifications off and save. Trigger another action and confirm no new Push is delivered; login, booking, queue tracking and local data still work.
- [ ] Turn Push back on, save, and confirm delivery resumes after a new eligible event.
- [ ] Log out, trigger another eligible event, and confirm the logged-out installation receives no new Push. Sign in again and save Push consent to re-register.
- [ ] Denying Android notification permission explains that it can be enabled later and does not break login, booking, queue tracking or local data.
- [ ] Offline registration shows a recoverable error and does not delete local or previously synchronized data.

Stage 60D proves real Render-to-FCM delivery for the seeded hosted Patient only. Notification content remains generic and detailed state is fetched after authenticated app opening.