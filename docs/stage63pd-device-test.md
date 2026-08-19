# Stage 63P-D device checklist

Use only controlled-pilot accounts. Do not enter or import old local demo family or Assistant records.

## Admin invitation copy

- Create a new Patient or Doctor invitation.
- Tap **Copy code**; confirm the button changes to **Copied**.
- Paste into a private message or notes and confirm all 32 case-sensitive characters match.

## Patient family profile

- Sign in as the controlled-pilot Patient and open a hosted clinic booking page.
- Tap **Add family member**, enter a name and choose spouse, child, parent, or other.
- Save and confirm the new member immediately appears under **Who is visiting?**.
- Book for that member; confirm the actual family name appears in Patient appointments and Doctor appointments.
- Restart the Patient App and confirm the family member remains available from the server.

## Doctor-owned Assistant

- Sign in as the controlled-pilot Doctor and open the hosted clinic.
- Create an Assistant invitation, selecting queue and/or clinic-fee permissions.
- Tap **Copy code** and verify the pasted 32-character code matches.
- In the Doctor App, sign out and select **Controlled pilot > Assistant > Activate invite**.
- Activate with the copied code and a new private credential; record the issued `DLO-AST` ID privately.
- Confirm the Assistant sees only the assigned clinic and only the granted actions.
- Sign out, then sign in with the `DLO-AST` ID and credential; restart and verify session restoration.
- As Doctor, disable one permission and refresh the Assistant; confirm the forbidden action is blocked by the API.

## Regression

