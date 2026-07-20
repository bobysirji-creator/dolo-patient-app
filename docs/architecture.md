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
