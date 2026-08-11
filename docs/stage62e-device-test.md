# Stage 62E Patient visual-correction device test

Build: Patient App `0.48.0-stage62e` (version code 69)

## Upgrade and data safety

- [x] GitHub Actions is green and the stable APK artifact was produced.
- [x] The stable APK installs over the existing Patient App.
- [x] Existing login/session, profile, family, favourites, appointments, queue data and theme choice remain present.

## Home avatar

- [x] The old rectangular Home illustration is completely absent.
- [x] A Patient avatar appears inside a white circle at the upper right of the greeting card.
- [x] Existing profiles initially show the Male avatar.
- [x] Open Profile & family, select Female, save and confirm the female avatar appears on Home.
- [x] Restart the App and confirm the selected avatar remains.
- [x] Switch back to Male, save and confirm the male avatar returns.
- [x] Long Patient names remain on one line with ellipsis and do not overlap the avatar.

## Doctor details

- [x] Open a local Doctor Details page and confirm its title is readable in Day and Night themes.
- [x] Confirm Home, Book and Appointments bottom navigation is present and matches other screens.
- [x] Home returns to Home, Book opens Doctor Categories and Appointments opens appointment history.
- [x] Repeat the title and navigation checks on an authoritative hosted Doctor Profile.
- [x] The main Book appointment button still opens the correct existing booking/hosted flow.

## Theme consistency

- [x] Favourite Doctor Book Again buttons are blue-themed, not green, in Night mode.
- [x] Upcoming, Past and All selected filters are blue-themed in both modes.
- [x] Day cards and fields no longer show soft blue outlines or decorative shadows.
- [x] A focused Day input still has a clear active indicator.
- [x] Primary buttons use the lighter blue and white labels remain readable.
- [x] Night cards, fields and navigation remain visually separated and readable.

## Regression

- [x] Login, OTP, discovery, booking, confirmation, history and live queue still work.
- [x] Push notification opening, Maps handoff, restart restoration and offline local-data safety still pass.
- [x] No profile/family data is uploaded by the new local avatar preference.

Record failures with phone model, Android version, theme, screen and screenshot.