# Admin-to-Patient Doctor Visibility Test

Use dummy data only. Restore the Doctor to VERIFIED and active after testing.

1. Deploy Platform API `0.6.1-stage17a`; confirm `/health` and `/ready` succeed.
2. Install Patient App `0.12.1-stage16c` (version code 17) over the existing stable app and confirm local data remains intact.
3. In Admin App, set Dr. Ananya Mehta to VERIFIED and active.
4. In Patient App, open Search Doctors or General Physician. Confirm a green `Hosted doctors` card for Dr. Ananya Mehta appears after refresh.
5. Open the hosted card and confirm the authoritative sessions load.
6. In Admin App, change verification to PENDING. Re-enter/refresh Patient discovery and confirm only the hosted Dr. Ananya card disappears; local test doctors remain.
7. Refresh Hosted Prototype Sync and confirm the exact friendly unavailable message appears with no stale Doctor/session card and no generic `Request failed` error.
8. Restore VERIFIED, refresh Patient discovery and confirm the hosted card and hosted synchronization return.
9. Disable the Doctor account while leaving VERIFIED selected. Repeat steps 6 and 7, then re-enable the account and confirm recovery.
10. Disable connectivity and refresh. Confirm an offline message without a crash or deletion of local profiles, family, favourites, reviews or appointments.
11. Confirm SMS, push, maps and payments remain disabled.
