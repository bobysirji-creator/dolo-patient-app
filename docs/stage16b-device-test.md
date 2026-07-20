# Stage 16B Physical-Device Test — Accepted

Accepted on 20 July 2026: all ten checklist items and the user-reported eight grouped device checks passed. The stable APK updated in place and retained existing local data.

Use demonstration data only. Never paste access or refresh tokens into chat, screenshots, logs, or files.

1. Confirm the Platform API workflow is green and Render `/health` reports `0.2.0-stage16b` before installing the Patient APK.
2. Confirm the Patient workflow is green and download `dolo-patient-stable-debug-apk`.
3. Install version code 15 over the installed stable version code 14. Do not uninstall. Confirm the upgrade succeeds and existing local profile, family, favourites, appointments and history remain present.
4. Existing Stage 16A login should restore as `Identity: Local fallback`; this is expected because it has no hosted token.
5. Logout, keep internet enabled, login with any demo 10-digit number and OTP `123456`. Confirm Home shows `Identity: Hosted prototype`.
6. Close the app, remove it from Recents, reopen it and confirm login and `Hosted prototype` state restore.
7. Open Help > Integration readiness. Confirm version `0.2.0-stage16b`, Stage 16.2, database connected, and external SMS, Push, Maps and Payments disabled.
8. Logout. Disable internet, login again with the demo OTP, and confirm the local workflow still opens with `Identity: Local fallback` and existing local bookings remain usable.
9. Re-enable internet, logout/login once more, and confirm `Hosted prototype` returns.
10. Confirm no real OTP is sent and no local booking, family, profile or queue data appears in hosted requests.

Stage 16B acceptance does not authorize server booking writes. Those belong to Stage 16C.
