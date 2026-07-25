# Stages 36A-36B device checklist

Prerequisites: Platform API `0.27.0-stage36ab` must pass GitHub Actions and deploy live before Patient App `0.24.0-stage36b` (version code 30) is built and installed over the current stable Patient App.

## Hosted API

- [ ] `/health` reports `0.27.0-stage36ab`; `/ready` reports ready with no blockers.
- [ ] Capabilities report stage `36.2`, transport `CONSENT_REVALIDATED_PATIENT_CAMPAIGN_FEED`, and targeted audience delivery `CONSENT_REVALIDATED_PATIENT_IN_APP_FEED`.
- [ ] SMS remains `OTP_ONLY_PROVIDER_DISABLED`; Push, Maps and Payments remain disabled.

## Active targeted message

- [ ] In Admin, create a Patient audience containing the seeded Patient, create a currently active compatible campaign, and approve it.
- [ ] Log out and sign in to Patient while online with a valid demo mobile number and OTP `123456`.
- [ ] Open Hosted Prototype Sync and refresh. The message appears under `Targeted DO-LO messages` with purpose, active period and `In-app only`.
- [ ] The targeted message does not appear on Patient Home and remains separate from `General DO-LO broadcasts`.
- [ ] A Doctor-targeted campaign never appears in the Patient feed.

## Consent and schedule revalidation

- [ ] Disable `In-app messages`; refresh and confirm all targeted messages disappear. Re-enable it and confirm an otherwise eligible message returns.
- [ ] Promotional content appears only while `DO-LO promotional messages` is enabled.
- [ ] Health-information content appears only while `Health information` is enabled.
- [ ] Service-update content appears only while `Appointment and service updates` is enabled.
- [ ] General informational/app-update content still requires `In-app messages`, but no additional purpose toggle.
- [ ] A future campaign is hidden until its start date; an expired or cancelled campaign is hidden.

## Safety and regression

- [ ] Closing/reopening the screen and completely restarting the app reload the authoritative targeted feed.
- [ ] While offline, refresh reports an error without deleting the last snapshot or changing local profile, family, favourites, appointments, reviews or notifications.
- [ ] After reconnecting, refresh recovers normally without duplicate campaign cards.
- [ ] Existing hosted login, doctor discovery, SELF/FAMILY booking, history, live queue, rescheduling, reviews, support, notifications and communication preferences still work.
- [ ] No promotional SMS, Android Push, Maps or Payment provider activity occurs.

Record GitHub Actions run links, Render deployment, stable APK upgrade and any observations before accepting Stage 36.