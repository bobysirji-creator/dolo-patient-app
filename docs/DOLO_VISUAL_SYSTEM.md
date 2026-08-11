# DO-LO visual system baseline

Status: accepted on physical Android hardware in Patient App `0.48.0-stage62e` on 11 August 2026.

This file is the canonical visual reference for future Patient, Doctor, Admin and web-console modernization. It records design tokens and interaction rules, not application business logic.

## Day palette

- Primary/action blue: `#1D73E8`
- Page background: `#F4F8FF`
- Main surface: `#FFFFFF`
- Alternate surface: `#EAF2FF`
- Primary container: `#DCE9FF`
- Main text/navy: `#061A3A`
- Muted text: `#5A6B85`
- Secondary/cyan-blue: `#008DBA`
- Decorative cyan: `#38C6E8`
- Day cards and inactive fields are flat and do not use decorative blue outlines or heavy shadows.
- Focus, selection and error states must remain clearly visible.

## Night palette

- Page background: `#030817`
- Main surface: `#081428`
- Alternate/layered surface: `#10233E`
- Primary blue: `#78A9FF`
- Primary container: `#174584`
- Main text: `#E8F0FF`
- Muted text: `#BAC8DE`
- Secondary cyan: `#55D5F4`
- Outline: `#526B91`; subtle outline: `#203552`
- Night cards use restrained layer separation and elevation; text never relies on low-contrast green-on-dark combinations.

## Typography and shape rules

- System sans-serif; compact, readable weights and line heights.
- Headings are bold or extra-bold; body copy is regular; action labels are bold.
- Shape scale: 8, 12, 18, 24 and 30 dp.
- Primary actions use the theme primary blue and the paired on-primary text color.
- Persistent navigation uses Material 3 selected indicators, readable labels and safe navigation-bar insets.

## Interaction and accessibility rules

- Preserve at least 48 dp interactive targets.
- Use explicit selected, disabled, loading, error and offline states.
- Never encode status by color alone.
- Support small phones, large text, Day/Night modes and safe system insets.
- Prefer short fades and reduced motion over directional screen movement.
- Reuse shared components before adding screen-specific colors, shadows or navigation.

## Adoption rule

Doctor, Admin and future web interfaces may adapt information density to their role, but must inherit these core colors, typography hierarchy, shape scale, Day/Night contrast and interaction semantics. Adoption must not change permissions, queue logic, payments, persistence or hosted authority.