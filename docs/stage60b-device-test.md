# Stage 60B Patient device test

Prerequisites: Platform API `0.54.0-stage60b` is deployed and healthy. Push provider delivery remains disabled.

1. Build Patient App `0.42.0-stage60b` (version code 61) through GitHub Actions.
2. Install the stable APK over the existing Patient App.
3. Confirm existing login/session, local profile, family, favourites, appointments, reviews and notifications remain safe.
4. Sign in to the hosted prototype and open Hosted Prototype Sync > Communication preferences.
5. Confirm a separate `Push notifications` switch is visible.
6. Enable it, save, leave the screen, return and confirm it remains enabled.
7. Disable it, save and confirm the disabled state also persists.
8. Confirm the screen clearly says Android system delivery is unavailable until a Push provider is connected.
9. With Push consent enabled, create a hosted booking or cause a queue update. Confirm the existing in-app notification still works.
10. Confirm no Android system Push notification is delivered and no notification permission is requested.
11. Disable connectivity while saving; confirm the last server snapshot and all local Patient data remain safe. Reconnect and refresh.
12. Regression-check hosted discovery, nearby clinics, external navigation, booking, history, live queue and in-app notifications.

Acceptance means consent and fail-closed behavior work. It does not mean real Push delivery is active.

