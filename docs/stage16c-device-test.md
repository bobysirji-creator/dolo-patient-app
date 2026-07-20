# Stage 16C Physical-Device Test

Run only after Platform API `0.3.0-stage16c` is deployed and ready. Use dummy data only.

1. Download the stable signed `0.12.0-stage16c` APK from the successful Patient App Actions run and update the existing app.
2. Confirm the existing local profile, local appointments, favourites and reviews remain unchanged.
3. Log out, reconnect to the internet, and sign in with any valid-looking 10-digit demo number plus OTP `123456`. Home should show `Hosted prototype`.
4. Open Support > Integration readiness. Confirm Stage `16.3`, `AUTHORITATIVE_DUMMY_PATIENT_SYNC`, Patient sync `AUTHORITATIVE_DUMMY_ONLY`, database connected and SMS/Push/Maps/Payments disabled.
5. Tap **Open hosted prototype sync**. Confirm it shows `Prototype Patient`, Dr. Ananya Mehta, DO-LO Prototype Clinic, and enabled Morning/Evening sessions for available dates.
6. Book one enabled session. Confirm a non-zero token appears in **Server appointment history** with the correct date and session.
7. Tap the same session's booking button again. Confirm the same appointment/token remains; no duplicate token is created.
8. Leave and reopen the hosted screen. Confirm server history is restored. Fully close/relaunch the app and confirm it is restored again while online.
9. Keep the hosted screen open for at least 20 seconds. Confirm automatic refresh does not duplicate the appointment and the live fields remain stable. `Current token` may say `Not started` until a backend-connected Doctor App admits and advances the queue.
10. Disable connectivity and tap Refresh. Confirm a visible offline/error message, no local data loss, and the ordinary local Patient workflow remains usable.
11. Reconnect and refresh. Confirm the server history returns.

Do not enter real patient information. Do not share access/refresh tokens. Stage 16C does not process the doctor's consultation fee, platform payments, maps, SMS or push notifications.