# Stage 18B Patient communications device test

1. Install the new stable Patient APK over the existing app; confirm local profile, family, favourites and appointments remain intact.
2. Log in online with the seeded Patient mobile and OTP 123456, then open Hosted Prototype Sync.
3. Confirm the active Doctor announcement created in the Doctor App appears under `Updates for you` with its correct type, dates, title and message.
4. Confirm the active Admin broadcast appears separately as `DO-LO broadcast`.
5. Set the Doctor announcement to Draft; refresh Patient and confirm it disappears while the Admin broadcast remains.
6. Set the Admin broadcast to Draft; refresh Patient and confirm it disappears.
7. Publish both again and confirm automatic refresh displays both without duplicate cards.
8. Force-close/restart while online and confirm hosted login/feed restore; local data remains unchanged.
9. Test offline Refresh, confirm the previous hosted snapshot and all local data remain safe, then reconnect and refresh.
10. Confirm SMS, Push, Maps and Payments remain disabled.
