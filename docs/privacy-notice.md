# DO-LO Patient App Privacy Notice

Last updated: 20 July 2026

## Current release-candidate scope

DO-LO Patient is currently an offline-first demonstration app. Stage 16B reads public health, capability, and clinic-discovery data and can create a session for one seeded dummy identity from the hosted DO-LO prototype, but it is not connected to a production healthcare service and must not be used for emergencies, diagnosis, prescriptions, or storage of sensitive medical records.

## Data currently stored on the device

The prototype can store the following in Android private app storage:

- the demo mobile number used for the local session;
- patient name, city and optional family-member details;
- selected doctors and favourites;
- appointment date, session and locally generated token;
- local queue progress, reviews and in-app notifications.

The prototype does not upload this information to DO-LO servers. Stage 16B sends only the fixed key `patient-demo`, demo OTP `123456`, and a generic device label to obtain prototype tokens. The entered phone number, profile, family member, appointment, queue, location, payment, and device-token data are not included. Access and refresh tokens are encrypted with Android Keystore before local storage.

## External services

Maps, payments, SMS and push-notification providers are disabled. The app does not currently:

- request precise device location;
- create or verify a payment;
- send an SMS or real OTP;
- register a push-notification device token;
- share patient data with a clinic or doctor system.

Any future provider must be documented, configured outside source control, and reviewed before it is enabled.

## Device security defaults

- Android backup is disabled.
- Cleartext HTTP traffic is disabled.
- No provider credentials or API keys are included in the repository or APK.
- The stable debug APK has a repeatable prototype signature for upgrades, but it is not a Play Store production release.

## User controls

Users can clear the local session with Logout. During prototype testing, uninstalling the app clears its private local data. A production version must add explicit account deletion and data-export controls before collecting server-side personal data.

## Children and family members

Family-member profiles are local prototype conveniences. A production release must define guardian consent, age rules and jurisdiction-specific requirements before collecting information about children.

## Emergency warning

DO-LO does not provide emergency services. In an emergency, contact the appropriate local emergency service or visit the nearest emergency department.

## Changes before public release

This notice must be reviewed with the chosen backend, hosting region, healthcare workflow, retention rules, support contact, legal entity and third-party providers before Play Store publication. This document is a development notice, not final legal advice.
