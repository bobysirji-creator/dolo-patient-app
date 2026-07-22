# Patient App Architecture

Stage 1 deliberately uses a single Android application module to reduce sync time and memory use.

Package boundaries:

- `data/model`: API-neutral domain-shaped data classes
- `data`: temporary in-memory Stage 1 fixtures
- `ui/theme`: DO-LO visual tokens
- `ui/components`: reusable Compose controls
- `ui/screens`: placeholder feature screens
- `ui/DoloPatientApp.kt`: navigation graph
- `platform`: lightweight HTTPS transport, hosted DTO parsing and connection state

Planned evolution:

1. Add `core/network` interfaces and DTO mapping only when the backend contract exists.
2. Add repositories for auth, doctors, appointments, queue, favourites and reviews.
3. Add ViewModels and immutable UI state per feature.
4. Keep OTP, maps, payment and notifications behind provider-neutral interfaces.
5. Never trust client-generated token numbers; allocation must be atomic on the server.
6. Use server events or a controlled polling policy for live queue updates; do not embed provider SDKs prematurely.

Doctor is already a separate Android project and the platform API is a separate service repository. Admin will also remain a separate app. Shared schemas and design decisions can be extracted later only when duplication becomes costly.


## Stage 16A integration boundary

`HttpPlatformApi` performs public GET requests only. The configured base URL is compiled into BuildConfig, must use HTTPS, and contains no credential. It reads health, capabilities, and clinic discovery without sending patient input.

`PlatformConnectionViewModel` runs those calls on one background executor and reports connecting, connected, or offline state to the Integration Readiness screen. A hosted failure never replaces or clears local PatientRepository data.

The next boundary must add controlled prototype authentication before any write endpoint is called. Server appointments and queues must not be mixed with local appointments until an explicit migration and conflict policy exists.

## Stage 16B identity and token boundary

`HttpPrototypeAuthApi` sends only the fixed `patient-demo` key, demo OTP, and a generic device label over HTTPS on a background executor. The phone typed into the UI is not included. `AndroidKeystoreTokenStore` encrypts access and refresh tokens with AES/GCM using a non-exportable Android Keystore key; SharedPreferences contains only IV and ciphertext. Logout clears ciphertext before making a best-effort revocation request.

If the hosted service is offline or cold-starting, login deliberately falls back to the existing local demo session and the Home screen labels that state. Local profiles, appointments, queues, favourites and reviews remain authoritative throughout Stage 16B. No authenticated booking write is attempted.

## Stage 16C authoritative dummy synchronization boundary

Stage 16C adds a separate hosted adapter rather than replacing `LocalPatientRepository`. Only the fixed seeded Patient profile returned by the protected bootstrap can be booked through this adapter. The server owns token allocation, capacity, appointment history and queue estimates. A per-session idempotency key is persisted locally so network retries resolve to the original appointment. The Compose screen polls only while visible; failures are surfaced without converting a failed server write into a local booking.

## Stage 22A hosted reschedule boundary

The bootstrap distinguishes ordinary booking sessions from `rescheduleSessions` and publishes the bounded `rescheduleWindowDays`. `HostedReschedulePolicy` only presents enabled candidates inside the original appointment's server-configured deadline, but this is a usability filter rather than an authorization control. The protected API independently verifies Patient ownership, ABSENT state, one-time eligibility, same-clinic target, deadline, capacity and idempotency. Appointment/target retry keys remain in local preferences until the authoritative server accepts or consistently returns the command result.
## Stage 23A receipt presentation boundary

`HostedAppointmentJson` maps only the owner-scoped clinic fee status, amount and receipt number returned by appointment history. `HostedReceiptPresentation` renders PENDING, PAID and WAIVED without inferring a payment method. Missing fields remain backward-compatible as PENDING. The local Patient repository is not read or updated by this hosted projection, and no gateway/provider SDK is present.
## Stage 24A hosted Home projection

`HostedPatientSyncViewModel` remains the only owner of server snapshots. While a hosted Patient session is on Home, the navigation host triggers the existing bounded 15-second refresh; leaving Home cancels that loop. `HostedHomePresentation` deterministically filters terminal appointments, orders active records and associates each record with its owner-scoped live queue projection. Compose receives this read-only state but never writes it into `LocalPatientRepository`, so installed local data and hosted server data cannot silently merge.