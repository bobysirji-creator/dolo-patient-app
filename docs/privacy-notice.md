# DO-LO Patient App Privacy Notice

Last updated: 13 July 2026

## Current release-candidate scope

DO-LO Patient is currently an offline-first demonstration app. Stage 16A reads public health, capability, and clinic-discovery data from the hosted DO-LO prototype, but it is not connected to a production healthcare service and must not be used for emergencies, diagnosis, prescriptions, or storage of sensitive medical records.

## Data currently stored on the device

The prototype can store the following in Android private app storage:

- the demo mobile number used for the local session;
- patient name, city and optional family-member details;
- selected doctors and favourites;
- appointment date, session and locally generated token;
- local queue progress, reviews and in-app notifications.

The prototype does not upload this information to DO-LO servers. Stage 16A sends only public GET requests for service status and clinic discovery; no phone number, profile, family member, appointment, queue, location, payment, or device-token data is included.

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
- The debug APK is for testing only and is not a signed production release.

## User controls

Users can clear the local session with Logout. During prototype testing, uninstalling the app clears its private local data. A production version must add explicit account deletion and data-export controls before collecting server-side personal data.

## Children and family members

Family-member profiles are local prototype conveniences. A production release must define guardian consent, age rules and jurisdiction-specific requirements before collecting information about children.

## Emergency warning

DO-LO does not provide emergency services. In an emergency, contact the appropriate local emergency service or visit the nearest emergency department.

## Changes before public release

This notice must be reviewed with the chosen backend, hosting region, healthcare workflow, retention rules, support contact, legal entity and third-party providers before Play Store publication. This document is a development notice, not final legal advice.
