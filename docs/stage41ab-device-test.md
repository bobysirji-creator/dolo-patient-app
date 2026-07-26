# Stages 41A-41B public DO-LO identity checklist

Prerequisites: Platform API `0.32.0-stage41ab` must pass GitHub Actions and deploy migration 035 before Patient App `0.26.0-stage41b` (version code 32) is built and installed.

## Hosted API

- [ ] Health, readiness and stage 41.2 capabilities pass.
- [ ] Identity card requires authentication and returns `SELF_ONLY_NO_PHONE`.
- [ ] The seeded Patient receives a stable `DLO-PAT-NNNNNN` ID with no phone or internal UUID.
- [ ] Production enrollment and production ID issuance remain disabled/reserved; providers remain disabled.

## Patient device

- [ ] Install over the existing app and confirm all local data is preserved.
- [ ] Hosted seeded login with OTP `123456` still works.
- [ ] Profile shows Hosted DO-LO Identity with the stable public ID, Patient name and role.
- [ ] The card says the ID is server-owned, seeded, and not a mobile number/internal UUID.
- [ ] Refresh returns the same identity and creates no new account.
- [ ] Complete close/relaunch restores the hosted session and same ID.
- [ ] Logout hides the authenticated identity; login again returns the same ID.
- [ ] Offline refresh is recoverable and does not alter local data; reconnect restores the card.
- [ ] Existing hosted discovery, booking, history, queue, family and communication workflows still work.
- [ ] No SMS, Push, Maps, Payments or media-provider activity occurs.

Record Actions, Render, APK upgrade and observations before accepting Stage 41AB.
