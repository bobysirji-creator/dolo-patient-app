# Stable Patient prototype APK signing

## Why this is required

Android accepts an in-place update only when the installed app and replacement APK have the same application ID and signing certificate. Earlier GitHub-hosted Patient builds used a temporary debug key created on each fresh runner. Their certificates changed, so Android reported **App not installed as package conflicts with an existing package**.

Version `0.10.1-stage16a` introduces one persistent Patient prototype certificate supplied through encrypted GitHub Actions repository secrets. It is not a production or Play Store signing key.

## One-time GitHub setup

Open the Patient repository [Actions secrets page](https://github.com/bobysirji-creator/dolo-patient-app/settings/secrets/actions). Choose **New repository secret** four times.

The private values were generated outside every Git repository in:

`C:\Users\Poly\Documents\codex\private\dolo-patient-prototype-signing\individual-github-secrets`

For each file below, use the filename without `.txt` as the secret name and copy the complete file content as its value:

1. `DOLO_SIGNING_KEYSTORE_BASE64.txt`
2. `DOLO_SIGNING_STORE_PASSWORD.txt`
3. `DOLO_SIGNING_KEY_ALIAS.txt`
4. `DOLO_SIGNING_KEY_PASSWORD.txt`

Do not paste any value into chat, source code, workflow logs, issues, or commits. Add all four secrets before pushing the signing commit.

## Private backup

Back up the entire private signing folder securely in at least one offline location. GitHub will not reveal secret values after they are saved. Losing the PKCS#12 file or passwords prevents future APKs from updating installations signed by this certificate.

Do not rotate this prototype key for routine builds. A different certificate again requires uninstalling the existing app unless a future Play App Signing migration supports an authorized signing-key upgrade.

## CI behavior

For pushes to `main` and manual workflow runs:

- all four secrets are mandatory;
- the private PKCS#12 is reconstructed only in the runner temporary directory;
- Gradle signs the debug APK with the stable Patient prototype certificate;
- `apksigner` verifies the APK;
- CI compares the APK certificate SHA-256 digest with the restored keystore certificate;
- the artifact is named `dolo-patient-stable-debug-apk`;
- the artifact includes the APK, APK checksum, and `signing-certificate.sha256`.

Pull-request builds receive no signing material and publish no installable artifact.

## First stable installation

The Patient APK currently installed from an older workflow has an unrecoverable temporary certificate. It cannot be upgraded directly to the first stable-signed APK.

1. Record any demo information you want to recreate. Android backup is disabled.
2. Uninstall the existing DO-LO Patient app once. This erases its local prototype data.
3. Install `dolo-patient-stable-debug-apk` version `0.10.1-stage16a`.
4. Recreate the required demo profile, family members, favourites, and appointments.
5. Keep the app installed.
6. For every later stable build, install over the existing app and confirm local data remains intact.

Production release signing, Play App Signing, key custody, rotation, and recovery remain separate future work.
