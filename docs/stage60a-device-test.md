# Stage 60A Patient device test

Use the stable Patient APK 0.41.0-stage60a after Platform API 0.53.0-stage60a is live.

## Upgrade and safety

- [ ] Install the stable APK over the existing Patient App.
- [ ] Existing login/session, profile, family members, favourites, appointments and history remain intact.
- [ ] Hosted login and normal Doctor discovery still work.

## Nearby discovery

- [ ] Open Doctor Categories, select All or a matching specialty, and tap Near me.
- [ ] Android asks only for approximate location access; no contacts, files or background-location permission is requested.
- [ ] Grant approximate location.
- [ ] Only authoritative hosted clinics appear in nearby mode; local demo Doctors are not mixed into these results.
- [ ] If the phone is within 50 km of the seeded Mumbai clinic, it appears with Distance sorting and an approximate distance.
- [ ] If the phone is outside that radius, a clear no-nearby-clinics result appears instead of invented Doctors.
- [ ] The screen states that the result is based on approximate straight-line distance, not road travel time.
- [ ] A clinic without verified coordinates cannot silently claim a distance.

## Permission and network recovery

- [ ] Deny location permission and confirm the app explains that city search remains available.
- [ ] Turn device location off and confirm the app asks you to enable it without crashing.
- [ ] Go offline and retry Near me; a bounded error appears and existing local Patient data remains safe.
- [ ] Reconnect and retry; hosted nearby results recover.

## External navigation

- [ ] Return to the ordinary All Doctors list if nearby results are empty, then tap the hosted clinic location/navigation action.
- [ ] Google Maps opens when installed; otherwise a compatible browser or map app opens.
- [ ] The destination coordinates point to the selected clinic.
- [ ] DO-LO does not display embedded turn-by-turn navigation and does not require a Maps API key.
- [ ] Return to DO-LO and confirm the current session and screen remain usable.

## Restart

- [ ] Fully close and relaunch the app.
- [ ] Hosted session restoration and all pre-existing local data still work.
- [ ] Location is requested again only when the Patient chooses Near me; no background tracking occurs.

Record phone model, Android version and any distance/navigation mismatch before accepting Stage 60A.
