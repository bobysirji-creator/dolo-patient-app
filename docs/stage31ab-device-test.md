# Stages 31A-31B communication-preference device checklist

Prerequisites: deploy Platform API `0.22.0-stage31ab`, confirm API and Patient Actions are green, then install Patient App `0.23.0-stage31b` (version code 29) over the existing stable app.

- [ ] Confirm `/health` reports `0.22.0-stage31ab`, `/ready` is ready, and capabilities report stage `31.2` with transport `CONSENTED_PATIENT_COMMUNICATION_PREFERENCES`.
- [ ] Confirm capabilities report `patientCommunicationPreferences = SERVER_AUTHORITATIVE_CONSENTED`, `smsUsage = OTP_ONLY_PROVIDER_DISABLED`, and `healthSegmentationBasis = CONSULTED_DOCTOR_SPECIALTY_HISTORY`.
- [ ] Update the existing Patient App and confirm the hosted session and all local profile, family, favourite, appointment, review and notification data remain safe.
- [ ] Log in to the hosted seeded Patient and open Hosted Prototype Sync.
- [ ] Confirm Communication preferences shows independent appointment/service, health-information, promotional and in-app switches plus English/Hindi language.
- [ ] Change several choices, save, leave the screen, reopen it and confirm the authoritative values remain selected.
- [ ] Completely close/relaunch the app and confirm the saved hosted choices persist after login/session restoration.
- [ ] Confirm the screen clearly states that health grouping uses only specialties of Doctors previously consulted and does not infer or store a diagnosis/disease.
- [ ] Confirm the screen clearly states that SMS is for OTP only and is never used for promotions.
- [ ] Create or use an active Doctor announcement and confirm it is absent from Patient Home but visible on that Doctor's own hosted profile only.
- [ ] If a second hosted Doctor is available, confirm the announcement does not appear on that other Doctor's profile.
- [ ] Confirm an active Admin broadcast remains visible on Patient Home.
- [ ] Confirm Doctor, Assistant and Admin sessions cannot read or change `/api/v1/patient/preferences`.
- [ ] Disable connectivity while changing or refreshing preferences; confirm the last server snapshot and all local Patient data remain safe.
- [ ] Reconnect, refresh and confirm authoritative preferences recover without duplication or silent reset.
- [ ] Regression-check hosted discovery, SELF/FAMILY booking, history, live queue, notifications, reviews and support.
- [ ] Confirm Push, Maps and Payments remain disabled and no promotional SMS delivery exists.