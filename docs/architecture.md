# Patient App Architecture

Stage 1 deliberately uses a single Android application module to reduce sync time and memory use.

Package boundaries:

- `data/model`: API-neutral domain-shaped data classes
- `data`: temporary in-memory Stage 1 fixtures
- `ui/theme`: DO-LO visual tokens
- `ui/components`: reusable Compose controls
- `ui/screens`: placeholder feature screens
- `ui/DoloPatientApp.kt`: navigation graph

Planned evolution:

1. Add `core/network` interfaces and DTO mapping only when the backend contract exists.
2. Add repositories for auth, doctors, appointments, queue, favourites and reviews.
3. Add ViewModels and immutable UI state per feature.
4. Keep OTP, maps, payment and notifications behind provider-neutral interfaces.
5. Never trust client-generated token numbers; allocation must be atomic on the server.
6. Use server events or a controlled polling policy for live queue updates; do not embed provider SDKs prematurely.

Doctor and Admin will remain separate Android projects/apps when development starts. Shared schemas and design decisions can be copied or extracted later only when duplication becomes costly.

