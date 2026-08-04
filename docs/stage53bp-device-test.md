# Stage 53B-P combined device checklist

Prerequisites: API GitHub Actions and PostgreSQL checks are green, Render runs `0.42.0-stage53bp`, and Patient/Admin stable APK workflows are green. Use only the seeded dummy accounts.

## Installation and safety

- [ ] Install Patient `0.39.0-stage53bp` over the existing Patient App; local profile, family, favourites and appointments remain intact.
- [ ] Install Admin `0.18.0-stage53bp` over the existing Admin App; encrypted hosted session and existing workspaces remain intact.
- [ ] No screen asks for a real phone number, identity document, recovery explanation or duplicate-account personal data.

## Patient simulation

- [ ] Log in online with the seeded Patient demo flow and open **Connected care**.
- [ ] **Account recovery test lab** clearly says dummy simulation only and no credential, ownership or account merge occurs.
- [ ] Create **Mobile-number change**; it appears as OPEN with one immutable event and `No account change` wording.
- [ ] Repeated taps do not create a duplicate active case.
- [ ] Existing hosted booking, queue, history and local-only data still work.

## Admin review

- [ ] Log in as seeded Admin (`admin-demo`, PIN `1234`) and open **Support**.
- [ ] The recovery case shows only `DLO-PAT-NNNNNN`, scenario, status, outcome and event count; no phone or internal UUID appears.
- [ ] Tap **Start test review**. Refresh Patient Connected care; status becomes UNDER REVIEW and event count increases.
- [ ] Tap **Acknowledge - no account change**. Patient refresh shows CLOSED and `NO ACCOUNT CHANGE TEST ONLY`.
- [ ] Create a **Duplicate account** simulation in Patient, then use **Escalate duplicate - test only** in Admin. It closes as `DUPLICATE ESCALATED TEST ONLY`; no records are merged.

## Persistence and negative checks

- [ ] Fully close and reopen both apps; hosted cases and immutable event counts return from the server.
- [ ] Offline refresh shows a safe error and does not alter local or hosted state.
- [ ] Logging out and back in restores the same server cases; no account credentials or ownership changed.
- [ ] General Patient support requests remain separate and functional.
- [ ] SMS, Push, Maps and Payments remain disabled.

Acceptance requires every item above. Stage 53B-F production recovery remains blocked even after this prototype checklist passes.
