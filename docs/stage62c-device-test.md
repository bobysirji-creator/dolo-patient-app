# Stage 62C Patient device test

Build: Patient App `0.46.0-stage62c` (version code 67)

## Install and data safety

- [ ] GitHub Actions is green and the stable debug APK is downloaded from that run.
- [ ] Install the APK over the accepted Patient App without uninstalling it.
- [ ] Existing login/session, profile, family members, favourites, appointment history and dark-mode choice remain present.

## Primary navigation

- [ ] Home displays exactly three bottom destinations in this order: Home, Book, Appointments.
- [ ] Book is centered; Appointments is on the right.
- [ ] No History or Profile item appears in the bottom bar.
- [ ] Home opens Home without duplicating screens in the back stack.
- [ ] Book opens Doctor Categories from Home, Categories, Doctor List and Booking screens.
- [ ] Appointments opens the existing appointment/history screen.
- [ ] Android Back behaves normally after moving between all three destinations.
- [ ] Profile/family, favourites and support remain available from the Home menu; appointment history remains reachable through Appointments.

## Accessibility and responsive behavior

- [ ] Each bottom item is comfortably tappable and announces its label and selected state with TalkBack.
- [ ] At the largest practical Android font size, all three labels remain understandable and the main content remains usable by scrolling.
- [ ] On the smallest available phone, the bottom navigation stays above Android system navigation controls.
- [ ] Light and Dark modes keep selected/unselected items and screen text readable.
- [ ] Moving between screens uses a brief fade without horizontal sliding or distracting motion.

## Regression and boundaries

- [ ] Login and restart/session restoration still pass.
- [ ] Doctor discovery, booking, confirmation and appointment history still pass.
- [ ] Live queue refresh and Push notification opening still pass.
- [ ] Offline/error handling does not delete or upload local profile, family, favourites or appointment data.
- [ ] Maps continues to hand external navigation to the device map app; provider/payment boundaries are unchanged.

Record any failed box with phone model, Android version, Light/Dark mode, font size and exact navigation path.