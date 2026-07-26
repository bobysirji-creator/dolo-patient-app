# DO-LO UI Modernization Roadmap — Stages 45A to 48

This roadmap is the source of truth for replacing the functional prototype presentation with a coherent, production-oriented experience. Business rules, hosted contracts, role permissions, data isolation and provider boundaries remain unchanged unless a later stage explicitly changes them.

## Product-wide design principles

- Task first: show the action a person came to perform before reports, diagnostics or settings.
- Three-level navigation: primary destinations, contextual actions, then account/settings tools.
- Compact rhythm: 18 dp page margins, 12-16 dp section gaps and cards sized to their content.
- Accessible controls: minimum 44 dp touch targets, readable contrast, useful content descriptions and no color-only state.
- Consistent language: plain user-facing terms; stage numbers and technical transport language stay in diagnostics/documentation.
- Predictable state: loading, empty, offline, failure and success states use the same visual vocabulary.
- Safe modernization: no API, billing, queue, authentication or local-storage contract is silently changed by UI work.

## Stage 45A — Patient design foundation and navigation

- New teal/navy healthcare palette, typography scale, spacing, shapes and elevation.
- Reusable headers, cards, buttons, badges, metrics, search and quick actions.
- Stable three-item bottom navigation: Home, Book, Appointments.
- Account menu for profile, favourites, support and logout.
- Short, consistent animated route transitions.
- Prototype controls removed from primary navigation and placed behind diagnostics/test labels.

## Stage 45B — Patient critical journeys

- Modern splash and mobile sign-in.
- Compact Home with search, updates, every active appointment and live queue summary.
- Specialty discovery and doctor lists with clearer information hierarchy.
- Step-based Patient/date/session booking.
- Focused token confirmation and live-queue screens.
- Upcoming, past and all appointment groupings.
- Polished profile/family, support, notifications and connected-care language.

## Stage 46A — Doctor information architecture

- Group the Doctor app into Today, Appointments, Clinic and More.
- Put queue control and currently consulting Patient first.
- Separate Doctor-only management from Assistant-permitted work.
- Replace long mixed pages with summaries and drill-down screens.

## Stage 46B — Doctor operational workflows

- Redesign online/offline appointment intake, clinic-fee confirmation and receipt printing.
- Session-aware queue, late-Patient recovery and archive history flows.
- Clinic/schedule/weekday-off, assistant permission and announcement editors.
- Reports, backup/recovery and notifications with clearer navigation.

## Stage 47A — Admin information architecture

- Dashboard for platform health, required reviews, broadcasts and billing actions.
- Separate People, Communications, Billing and Operations areas.
- Persistent filters and explicit result counts for Doctor/Patient search.
- Mobile layouts that can later map cleanly to a richer web Admin console.

## Stage 47B — Admin operational workflows

- Modern Doctor/Patient search and audience preview.
- Safer verification, enable/disable and profile-review actions.
- Targeted Patient and Doctor messaging with consent/purpose boundaries.
- Policy versioning, per-online-booking accrual, trial periods and invoice navigation.

## Stage 48 — Ecosystem polish and release readiness

- Shared wording, state colors, icons and accessibility across all three apps.
- Dark-theme completion where appropriate, large-text and small/large-screen checks.
- Motion reduced for accessibility and kept brief elsewhere.
- Performance pass, screenshot regression set and end-to-end navigation audit.
- Final device acceptance, production asset replacement and release-candidate documentation.

## Stage 45A-45B status

Implemented in Patient App `0.27.0-stage45ab` (version code 33). GitHub Actions and physical-device acceptance remain the final gates.
