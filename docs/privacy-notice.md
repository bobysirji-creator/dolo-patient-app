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

The ordinary local app does not upload this information. Stage 16C adds a separate, clearly labelled hosted flow that uses only the fixed seeded `Prototype Patient`, doctor, clinic, appointment and queue records. It does not upload the entered phone number or any existing local profile, family member, appointment, favourite, review, location, payment or device-token data. Access and refresh tokens remain encrypted with Android Keystore.

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
