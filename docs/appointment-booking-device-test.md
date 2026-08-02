# Appointment Booking device checklist

Use the stable APK produced for Patient App 0.33.0-booking-ui (version code 46).

## Installation and entry

- [ ] Install the stable APK over the accepted Patient App without losing profile, family, favourites, appointments or hosted data.
- [ ] Open a Doctor from a category and tap Book Now; the selected Doctor appears on Book Appointment.
- [ ] Back returns to the previous Doctor screen and the notification bell opens Notifications.

## Booking selections

- [ ] Self is selected initially and Patient Details shows the saved name/mobile number.
- [ ] Select each saved family member; only one option remains selected and Patient Details updates immediately.
- [ ] Select each clinic; cards scroll horizontally and contain no clinic photographs.
- [ ] Select available dates; unavailable dates cannot be selected and More opens its placeholder.
- [ ] Select Morning and Evening independently; timing, available tokens and reporting time remain readable.
- [ ] Confirm stays disabled if a required selection is missing and repeated taps cannot create duplicate appointments.

## Fee and completion

- [ ] Consultation fee, DO-LO service charge and total are correct; the info action explains the service charge.
- [ ] Confirm Booking shows progress, creates one appointment and opens the existing token confirmation screen.
- [ ] The correct Doctor, Patient/family member, date and session remain in confirmation, history and live queue.
- [ ] Force-close and relaunch; the booked appointment and selected family Patient remain safely persisted.

## Responsive and accessibility

- [ ] Test Light and Dark modes on a small and standard phone; all text remains readable.
- [ ] Test increased system font size; no essential text/control is clipped and horizontal groups still scroll.
- [ ] TalkBack announces visitor, clinic, date, session availability/selection, notification count and Confirm Booking.

Record screenshots of the populated screen in Light and Dark modes for comparison with `C:\Users\Poly\Documents\codex\2026-07-11\appointment.png`.