# Stage 23A physical-device checklist

Prerequisites: Platform API `0.14.0-stage23a` is live and the seeded Doctor is active/VERIFIED.

- [x] Stable Patient APK updates the existing app without uninstalling; all local and hosted data remains intact.
- [x] Hosted login/refresh shows Stage 23A and existing Stage 21/22 flows still work.
- [x] Use an existing current-day PENDING SELF or FAMILY hosted appointment (or book one if none exists); confirm its history card shows clinic fee PENDING and says the receipt will be generated after clinic confirmation.
- [x] In the Doctor App, confirm the clinic fee as PAID and generate the receipt; refresh Patient and confirm the INR clinic amount and matching receipt reference appear.
- [x] Use the second existing current-day PENDING dummy appointment and select `Waive fee and admit`; confirm Patient shows WAIVED and its receipt reference without a paid amount.
- [x] Confirm PAID/WAIVED cards explicitly state `Not an online DO-LO payment.`
- [x] Complete one consultation and confirm its receipt remains visible in Patient history.
- [x] Mark another confirmed appointment ABSENT, reschedule it once, and confirm the original receipt remains attached to the original while the replacement starts PENDING.
- [x] Confirm no clinic payment method, gateway reference or provider detail appears in Patient App.
- [x] Fully close/relaunch Patient and Doctor apps; receipt/history states reload from the server.
- [x] While offline, refresh fails visibly and previously loaded hosted/local data remains unchanged; reconnect successfully.
- [x] Confirm local profile, family, favourites, reviews and appointments remain private and unchanged.
- [x] Confirm Maps, Payments, SMS and Push remain disabled.

Use only seeded dummy records. Doctor consultation fees remain paid directly at the clinic.
Do not bypass duplicate-booking protection or confirm clinic fees for future appointments. If no current-day PENDING record remains, continue this transition check on the next clinic day.

Accepted on 22 July 2026. A fresh next-clinic-day observation retest is planned for 23 July and does not block Stage 24A.
