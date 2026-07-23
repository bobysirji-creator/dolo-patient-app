# Stages 26A-26B Patient device checklist

Prerequisites: API `0.17.0-stage26ab` is deployed and ready; Patient and Admin Actions are green; install both stable APKs over the existing apps and use the seeded hosted Patient session.

- [ ] Existing hosted login, booking, history, queue, reviews, profile/family and local favourites remain intact.
- [ ] Open **Help & Support** and confirm the support form and authoritative request history appear.
- [ ] Confirm the five categories are APPOINTMENT, DOCTOR, BILLING, APP and OTHER.
- [ ] Confirm short subject/message values cannot be submitted.
- [ ] Submit a valid APP request and confirm it appears once with OPEN status.
- [ ] Close and relaunch Patient App; confirm the request remains visible after refresh.
- [ ] In Admin App, mark the request IN_PROGRESS; refresh Patient Support and confirm the status.
- [ ] In Admin App, resolve it with a meaningful response; confirm Patient shows RESOLVED and the exact response.
- [ ] After Admin closes it, confirm Patient shows CLOSED.
- [ ] Disable connectivity and retry submission/refresh; confirm a safe offline error and no local Patient data loss.
- [ ] Reconnect and confirm the authoritative request history recovers without duplicates.
- [ ] Confirm Doctor and Assistant apps expose no Patient support request screen.
- [ ] SMS, Push, Maps, Payments, attachments and external chat remain disabled.