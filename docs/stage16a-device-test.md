# Stage 16A Physical-Device Test

Use the GitHub Actions `dolo-patient-stable-debug-apk` artifact. Android Studio is not required.

## Upgrade safety

1. Record any local demo information needed from the temporary-key app.
2. Uninstall that existing Patient App once; its local data will be erased because Android backup is disabled.
3. Install the first stable build, version `0.10.1-stage16a` (version code 14).
4. Recreate enough demo data to test retention.
5. For the next stable build, install over this app without uninstalling and confirm all local data remains available.

## Hosted connection

1. Connect the phone to the internet.
2. Open Home > Help & Support > Integration readiness.
3. Allow up to 30 seconds for a free-tier Render cold start.
4. Confirm the screen says Connected, version `0.1.0-stage15.6`, Stage `15.6`, and Database connected.
5. Confirm hosted clinics are listed, or the screen explicitly says none are published.
6. Press Check connection and confirm the app remains responsive.

## Offline fallback

1. Turn off Wi-Fi and mobile data.
2. Press Check connection and confirm a safe offline/timeout message appears.
3. Return to Home and confirm local doctor search, appointment history, booking demo, and queue demo still work.
4. Restore internet, return to Integration readiness, and retry successfully.

## Safety expectations

- Login and OTP remain local demo behavior.
- No local patient, family, appointment, or queue data is uploaded.
- Maps, payments, SMS, and push notifications remain Disabled.
- Hosted booking and queue synchronization are not expected until Stage 16B/16C.
