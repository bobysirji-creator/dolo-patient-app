# Patient Home design QA

source visual truth path: C:\Users\Poly\Documents\codex\2026-07-11\home.jpeg
implementation screenshot path: unavailable
viewport: intended Android portrait phones from 320 dp through standard and large widths
source pixels: 900 x 1600
implementation pixels: unavailable
CSS size and density normalization: not applicable to this native Compose project
state: normal Patient Home plus preview-only loading, empty, paused, multiple-queue, broadcast and error states

## Full-view comparison evidence

The source reference was opened and inspected. The native implementation could not be rendered or captured in this lightweight checkout because no Java runtime, Gradle installation, Gradle wrapper, Android SDK or emulator is available. Code inspection is not accepted as visual comparison evidence.

## Focused region comparison evidence

Blocked for the same reason. The top bar, greeting hero, token cards, Admin Broadcast, favourite Doctor row and bottom navigation require an APK/device screenshot before a visual comparison can be made.

## Findings

- [P1] Native implementation screenshot unavailable
  - Location: Patient Home screen.
  - Evidence: source reference is available, but there is no rendered Android artifact in this environment.
  - Impact: typography, actual wrapping, density, image crop and small-screen layout cannot be visually certified.
  - Fix: run GitHub Actions, install the stable debug APK on a physical phone, capture the Home screen at the normal populated state and compare it with the reference.

## Open Questions

- Final Doctor portrait photography remains intentionally represented by existing local specialty artwork until approved portrait assets are supplied.

## Implementation Checklist

- Run GitHub Actions compile, lint, unit-test and APK jobs.
- Install the stable debug APK over the accepted Patient App.
- Capture normal, no-queue and multiple-queue Home states on a physical device.
- Compare the normal capture with the reference at matching crop and density.
- Fix any P0, P1 or P2 visual mismatch before marking this report passed.

## Follow-up Polish

- Replace explicit Doctor image placeholders with approved portraits.

final result: blocked
# Doctor Categories design QA

source visual truth path: C:\Users\Poly\Documents\codex\2026-07-11\categories.jpeg
implementation screenshot path: unavailable
viewport: intended Android portrait phones from 320 dp through standard and large widths
state: normal categories plus preview-only loading, error, no-result, empty and disabled states

## Full-view comparison evidence

The source reference was opened and inspected. The Compose implementation cannot be rendered in this lightweight checkout because the repository has no Gradle wrapper and this PC environment has no configured Android SDK/runtime. Code inspection is not visual comparison evidence.

## Findings

- [P1] Native implementation screenshot unavailable
  - Location: Doctor Categories screen.
  - Impact: exact typography, card density, image crop and small-screen behavior cannot be visually certified here.
  - Fix: run GitHub Actions, install the stable APK, capture the populated Categories screen on a physical phone and compare it with the reference.

## Implementation checklist

- GitHub Actions compile, lint, unit-test and APK jobs pass.
- Search by specialty and health-need aliases filters live.
- Clear search, Back, Notifications and all five bottom destinations work.
- Category selection opens Doctor List with the correct category ID/name.
- Light/Dark themes and 320 dp layout remain readable without clipping.

final result: blocked
