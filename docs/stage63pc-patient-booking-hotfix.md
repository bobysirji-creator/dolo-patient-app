# Stage 63P-C Patient clinic booking hotfix

## Purpose

The controlled-pilot Patient App must book the verified clinic selected in hosted Doctor discovery. The Doctor-profile booking action must never open prototype recovery or payment simulation controls.

## Deployment order

1. Deploy Platform API `0.60.1-stage63pc` and confirm `/ready` is ready.
2. Build and install Patient App `0.49.1-stage63pc` (version code 71).
3. Sign in with the controlled-pilot Patient account.

## Device checklist

- [ ] The newly approved clinic appears in hosted Doctor discovery.
- [ ] Open its Doctor profile and tap **Book Appointment**.
- [ ] A clinic-specific booking page appears with the selected Doctor, clinic, Patient/family choices, and available sessions.
- [ ] No **Secure test connection** or prototype recovery/payment card appears in this flow.
- [ ] Select a Patient and an enabled session, then book once.
- [ ] The appointment receives an authoritative token and appears under hosted appointment history/live status.
- [ ] Reopening the Doctor profile and booking page retains correct clinic isolation.
- [ ] A closed/full/unavailable session cannot be booked and existing local Patient data remains unchanged.