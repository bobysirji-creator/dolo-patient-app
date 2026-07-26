# Stage 45AB Patient UI Device Checklist

Install `dolo-patient-stable-debug.apk` over the current stable Patient App. Existing local data must remain present.

## Visual foundation

- [ ] Splash, login and all visited pages use the same teal/navy theme and readable typography.
- [ ] Cards, buttons and fields are compact, aligned and do not waste vertical space.
- [ ] Content remains above Android gesture/navigation controls on both test phones.
- [ ] Large text does not hide the primary action or truncate essential Patient/token information.

## Navigation

- [ ] Bottom navigation is Home, Book in the centre, and Appointments on the right.
- [ ] Each item opens the expected destination without duplicate screens piling up.
- [ ] Back returns predictably and route transitions are brief and smooth.
- [ ] Profile & family, Favourites, Help & support and Sign out are available from the Home account menu.
- [ ] Stage numbers and diagnostics are absent from the normal booking journey.

## Core Patient journeys

- [ ] Hosted login and local fallback login still work as before.
- [ ] Home shows every active SELF/FAMILY appointment with Doctor, Patient, token and queue progress.
- [ ] Search, specialty categories, doctor list, profile and favourite actions work.
- [ ] Booking clearly selects Patient, date and morning/evening session.
- [ ] Confirmation clearly shows the allotted token, Doctor, Patient, clinic and date.
- [ ] Upcoming/Past/All appointment filters show the correct records.
- [ ] Live queue refresh/countdown works; prototype simulation controls remain collapsed by default.
- [ ] Notifications, profile/family, support requests and connected-care features remain functional.

## Safety and persistence

- [ ] Restart restores the signed-in session and existing local records.
- [ ] Offline state preserves the last hosted snapshot and all local data.
- [ ] Reconnection refreshes hosted data normally.
- [ ] SMS, Push, Maps and Payments remain disabled.
- [ ] Existing booking, queue, reschedule, review and communication rules are unchanged.

Record device models, Android versions, APK SHA-256 and any failed item before accepting Stage 45AB.
