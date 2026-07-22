# Stage 21B physical-device checklist

Prerequisites: Platform API `0.12.0-stage21a` is live and the seeded Doctor is active/VERIFIED.

- [ ] Stable Patient APK updates the existing app without uninstalling; local profile, family, favourites, appointments and hosted session remain intact.
- [ ] Hosted Prototype Sync displays both `Prototype Patient (self)` and `Prototype Family Member (family)`.
- [ ] Select SELF and book one enabled session; confirm the appointment shows `Prototype Patient`.
- [ ] Select FAMILY and book the same enabled session; confirm a separate token is allocated and shows `Prototype Family Member`.
- [ ] Re-tap each booking and confirm its original token is returned rather than allocating a duplicate.
- [ ] Doctor hosted appointments show both patient names; fee admission and queue actions remain independent.
- [ ] Patient history and live queue display both appointments with their respective names/tokens.
- [ ] Fully close/relaunch both apps; hosted session, selection fallback, history and live state reload safely.
- [ ] While offline, refresh/booking fails visibly and all existing local data remains unchanged; reconnect successfully.
- [ ] Confirm the local family list was neither uploaded nor replaced.
- [ ] Confirm Maps, Payments, SMS and Push remain disabled.

After acceptance, do not enter real family details into the hosted prototype; Stage 21B supports fixed dummy profiles only.
