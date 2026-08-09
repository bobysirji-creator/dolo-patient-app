# Stage 59C-P combined device checklist

Prerequisites: Platform API GitHub Actions and Render are healthy on version 0.52.0-stage59bp; Patient and Admin stable APK workflows are green. Use only the seeded dummy accounts.

## Upgrade and safety

- [ ] Install Patient 0.40.1-stage59cp over the existing Patient App; local profile, family, favourites and appointments remain intact.
- [ ] Install Admin 0.19.1-stage59cp over the existing Admin App; the encrypted hosted session and existing workspaces remain intact.
- [ ] Confirm neither app asks for card, UPI, bank, payment-provider, Patient, family or appointment information.

## Patient test workspace

- [ ] Sign in with a valid seeded Patient mobile number and OTP 123456, then open Hosted prototype sync.
- [ ] Find Synthetic payment test lab and verify its permanent no-real-money warning.
- [ ] Run Captured: a DLO-PAY-SIM-NNNNNN reference, CAPTURED TEST ONLY, INR test amount and Booking eligible: Yes appear.
- [ ] Run Zero charge: amount is INR 0.00 and booking eligibility is Yes.
- [ ] Run Failed and Expired: booking eligibility is No.
- [ ] Run Refunded: status/refund show REFUNDED TEST ONLY; booking eligibility remains Yes because capture qualified before the later synthetic refund.
- [ ] Tap Captured again: the same synthetic reference returns, proving the stored idempotency key is reused.
- [ ] Confirm no appointment, token, local profile, family member, favourite or consultation-fee receipt changed.

## Admin test workspace

- [ ] Connect with the seeded Admin PIN and open Charges.
- [ ] Find Synthetic payment test lab and verify its permanent test-only warning.
- [ ] Repeat Captured, Zero charge, Failed, Expired and Refunded; each result matches the Patient presentation rules.
- [ ] Tap one scenario again: the same synthetic reference returns.
- [ ] Refresh financial data and confirm service-charge ledger lines, Doctor trials and invoices did not change because of these simulations.

## Recovery and isolation

- [ ] Disable connectivity and try a scenario: a bounded offline error appears and existing app data remains safe.
- [ ] Restore connectivity and retry successfully.
- [ ] Fully close and reopen both apps; sessions restore according to the existing behavior.
- [ ] Re-run a previous scenario and confirm its synthetic reference remains stable.
- [ ] Confirm no provider checkout, webhook, real receipt, real refund, payment notification or money movement occurs.

Acceptance requires every item above. Stage 59C-P validates cross-app presentation of synthetic outcomes only; it does not activate a payment gateway or satisfy production Stage 59 dependencies.
