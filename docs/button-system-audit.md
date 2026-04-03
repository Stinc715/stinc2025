# Button System Audit

## Scope

This document records the remaining `.btn` duplication after the latest low-risk
desktop consistency cleanup. It is an audit and migration-prep document. This round
does not change high-risk button runtime styles.

## Current state

- Shared simple-page button base already lives in
  `frontend/desktop-consistency.css` for:
  - `clubs.html`
  - `join.html`
  - `venue overview.html`
- Shared simple-page `.btn.ghost` base also lives there for the same page group.
- Remaining duplication is concentrated in page-local button systems that are tied to
  page state, layout, or page-specific size semantics.

## Page groups

### 1. Already in the shared layer

| Page | Current status | Why it was safe |
| --- | --- | --- |
| `clubs.html` | Base `.btn` and `.btn.ghost` moved to shared desktop scope | Simple pill buttons, no loading or stateful variants, only page-local hero/layout styling remains. |
| `join.html` | Base `.btn` and `.btn.ghost` moved to shared desktop scope | Same pill-button family as `clubs.html`; only `.btn.soft` stays local. |
| `venue overview.html` | Base `.btn` and `.btn.ghost` moved to shared desktop scope | Same pill-button family as `join.html`; only `.btn.soft` stays local. |

### 2. Medium-risk local button systems that should stay local for now

| Page | Local button traits | Why not extract now |
| --- | --- | --- |
| `payment.html` | Small utility buttons, `hidden` and `disabled` states, page-local action row | Shares `desktop-page--payment` body scope with `reset-password.html`, so blind extraction would couple unrelated pages. |
| `reset-password.html` | Form CTA buttons with `flex: 1`, fixed `46px` height, primary/light pairing | Uses the same body scope as `payment.html` but a different control size and action layout. |
| `onboarding.css` / `onboarding.html` | Flow-specific `.btn.primary`, `.btn.ghost`, `:hover`, `[disabled]` rules | Tied to onboarding step flow, action emphasis, and module-level page semantics rather than a generic desktop shell. |

### 3. High-risk complex pages that should not be touched without a dedicated audit

| Page | Coupling that makes extraction risky |
| --- | --- |
| `club bookings.html` | `.approve`, `.checkin`, `.cancel`, `.ghost`, `.small`, disabled, hover, and active states are all bundled into one booking-action system. |
| `club-admin.html` | Base buttons are tied to `focus-visible`, disabled, `.is-disabled`, anchor-button treatment, compact variants, and flex item action rows. |
| `club-info.html` | Button rules include disabled, danger, small, and image-action variants tied to upload and media flows. |
| `club.html` | CTA buttons are part of the public club page action system and coexist with multiple color variants and page-specific action placement. |
| `home.html` | Base buttons share a public-shell action language, plus `search-controls .btn` overrides and focus/active handling. |
| `user.html` | Shared desktop button base is already in CSS, but page-local button layout still depends on `security-actions .btn` and booking-action width rules. |

## Pages that are not current duplication hotspots

These pages are not the next button-extraction candidates:

- `club chat.html`
- `club home.html`
- `login.html`

Reason:

- they already rely more on shared shell styling than on large local `.btn` duplicates
- their remaining button behavior is mostly page-scope override work, not a repeated
  local base worth extracting blindly

## Why the remaining systems stay local

Common reasons extraction is currently unsafe:

- disabled, loading, selected, or active-state semantics live beside the base rule
- button size is part of a page-specific layout contract
- variants such as `danger`, `compact`, `small`, `approve`, or `checkin` are part of
  one page-local action language
- multiple pages still share a body scope but use different button semantics

## What should not happen in a future round

- do not batch-merge all remaining `.btn` rules into one shared base
- do not treat `theme.css` and page-local button systems as interchangeable
- do not move page-local disabled/loading/active states into shared CSS without a page
  audit

## Recommended next audit before any further extraction

Do this first:

1. map each candidate page's button variants and states
2. identify whether width, height, flex behavior, or visibility are layout-critical
3. separate "visual base" from "business state" before moving anything

## Recommended next pilot page

If a future round wants one more button extraction pilot, the safest remaining target is
`payment.html`, but only after these preconditions are met:

1. give `payment.html` a page-specific scope that does not also affect `reset-password.html`
2. keep `hidden` and `disabled` behavior local
3. verify no visual baseline movement before extracting any base rule

Until those preconditions exist, the current button duplication is an intentional
stability tradeoff, not cleanup debt that should be removed blindly.
