# Stage 62D Patient theme and navigation device test

Build: Patient App `0.47.0-stage62d` (version code 68)

## Install and persistence

- [x] GitHub Actions is green; install the stable APK over the existing Patient App.
- [ ] Existing hosted/local login, profile, family, favourites, appointments and queue data remain present.
- [ ] The previously selected Light or Dark mode remains selected after upgrade and app restart.

## Appointments navigation correction

- [ ] Open Appointments from Home and confirm its bottom navigation matches Home, Categories, Doctor List and Booking.
- [ ] The items appear in this exact order: Home, Book, Appointments.
- [ ] Appointments is visibly selected while on the Appointments screen.
- [ ] Tap Home and confirm the existing Home screen opens immediately.
- [ ] Tap Book and confirm Doctor Categories opens.
- [ ] Repeat Home navigation after filtering Upcoming, Past and All.
- [ ] Android Back does not reveal duplicate Home screens.

## Day reference theme

- [ ] Backgrounds are pale ice blue rather than teal/grey.
- [ ] Main cards and fields use clean white surfaces with soft blue borders/shadows.
- [ ] Primary buttons, selected navigation and important actions use cobalt blue.
- [ ] Secondary accents are cyan/blue and status colors remain understandable.
- [ ] Navy text remains readable on backgrounds, cards, fields and buttons.

## Night reference theme

- [ ] Background is deep navy rather than green/charcoal.
- [ ] Cards, dialogs, drawers, fields and bottom navigation use visibly separated navy layers.
- [ ] Primary actions and selected states use readable blue; secondary accents use cyan.
- [ ] Mobile number, OTP, appointment details, queue metrics and error/status messages remain readable.
- [ ] No illustration, card, field or dialog disappears into its background.

## Responsive and regression checks

- [ ] Check Login, OTP, Home, Categories, Doctor List, Booking, Confirmation, Appointments, Live Queue and Profile in both themes.
- [ ] Check the smallest available phone and large font size; content remains scrollable and bottom navigation stays above system controls.
- [ ] TalkBack announces Home, Book and Appointments with the correct selected state.
- [ ] Hosted login, booking, history, live refresh, restart restoration and Push notification opening still pass.
- [ ] Offline/error behavior preserves local data; Maps, payment and consultation-fee boundaries are unchanged.

Record any failure with phone model, Android version, theme, font size, screen name and screenshot.