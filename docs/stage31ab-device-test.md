# Stages 31A-31B communication-preference device checklist

Prerequisites: deploy Platform API `0.22.0-stage31ab`, confirm API and Patient Actions are green, then install Patient App `0.23.0-stage31b` (version code 29) over the existing stable app.

- [x] Confirm `/health` reports `0.22.0-stage31ab`, `/ready` is ready, and capabilities report stage `31.2` with transport `CONSENTED_PATIENT_COMMUNICATION_PREFERENCES`.
- [x] Confirm capabilities report `patientCommunicationPreferences = SERVER_AUTHORITATIVE_CONSENTED`, `smsUsage = OTP_ONLY_PROVIDER_DISABLED`, and `healthSegmentationBasis = CONSULTED_DOCTOR_SPECIALTY_HISTORY`.
- [x] Update the existing Patient App and confirm the hosted session and all local profile, family, favourite, appointment, review and notification data remain safe.
- [x] Log in to the hosted seeded Patient and open Hosted Prototype Sync.
- [x] Confirm Communication preferences shows independent appointment/service, health-information, promotional and in-app switches plus English/Hindi language.
- [x] Change several choices, save, leave the screen, reopen it and confirm the authoritative values remain selected.
- [x] Completely close/relaunch the app and confirm the saved hosted choices persist after login/session restoration.
- [x] Confirm the screen clearly states that health grouping uses only specialties of Doctors previously consulted and does not infer or store a diagnosis/disease.
- [x] Confirm the screen clearly states that SMS is for OTP only and is never used for promotions.
- [x] Create or use an active Doctor announcement and confirm it is absent from Patient Home but visible on that Doctor's own hosted profile only.
- [x] If a second hosted Doctor is available, confirm the announcement does not appear on that other Doctor's profile.
- [x] Confirm an active Admin broadcast remains visible on Patient Home.
- [x] Confirm Doctor, Assistant and Admin sessions cannot read or change `/api/v1/patient/preferences`.
- [x] Disable connectivity while changing or refreshing preferences; confirm the last server snapshot and all local Patient data remain safe.
- [x] Reconnect, refresh and confirm authoritative preferences recover without duplication or silent reset.
- [x] Regression-check hosted discovery, SELF/FAMILY booking, history, live queue, notifications, reviews and support.
- [x] Confirm Push, Maps and Payments remain disabled and no promotional SMS delivery exists.