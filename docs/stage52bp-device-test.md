# Stage 52B-P device checklist

Use the stable Patient APK built after the API has deployed `0.40.0-stage52bp`.

## Upgrade and existing data

- [ ] Install the stable APK over the existing Patient App without uninstalling.
- [ ] Existing login, local profile, family, favourites, history and appointments remain intact.

## Test legal-document viewer

- [ ] Log out, open the Login screen and tap **Create an Account**.
- [ ] Registration remains blocked and the seven safety requirements remain visible.
- [ ] A **Test legal-document viewer** section lists exactly:
  - Prototype Terms of Use
  - Prototype Privacy Notice
  - Prototype Health Data Notice
- [ ] Open each document and verify the red test banner says **TEST DRAFT - NOT LEGALLY APPROVED**.
- [ ] Each viewer shows its `TEST-...` version, `en-IN`, draft-only status and readable sections.
- [ ] Long content scrolls on a small Android phone without clipping behind system navigation.
- [ ] Close returns to Login without losing the phone field or registration notice.
- [ ] **Simulate test acknowledgement** changes only that document to **Tested**.
- [ ] Closing and reopening during the same screen session retains the in-memory **Tested** label.
- [ ] Force-close/recreate the app and confirm the acknowledgement is not retained.

## Safety and accessibility

- [ ] No checkbox or control claims legal acceptance or production consent.
- [ ] No account is created and no production DO-LO ID is issued.
- [ ] No OTP SMS, payment, maps or push provider is invoked.
- [ ] Offline preview failure keeps registration disabled and preserves existing local data.
- [ ] Light and dark themes keep the banner, document text and buttons readable.
- [ ] TalkBack identifies document buttons, the viewer heading and the close button.

Stage 52B-F remains deferred until approved legal content, versions, effective dates and approval references are supplied.
