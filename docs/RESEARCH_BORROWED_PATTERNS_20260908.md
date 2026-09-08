# Crystal — External Research / Borrow Plan

Date: 2026-09-08
Branch: `capture-fabric-v1`

## Naming change

The former `Blackadder` decision-arbitration concept is renamed **Decision Friction**.

`Blackadder` should be treated as a historical alias only. New code, documentation, schemas, and UI should use `DecisionFriction` / `decision_friction`.

## Research objective

Look for proven, small patterns Crystal can borrow without turning Crystal into a generic automation framework. Priority is capture truth, offline persistence, OCR/screenshot corroboration, service survivability, replayability, and explicit user-controlled assistance.

## High-value external patterns

### 1. Android accessibility abstraction — Krosxx/Android-Auto-Api

Repository: https://github.com/Krosxx/Android-Auto-Api

Borrow:
- a thin abstraction around AccessibilityService lifecycle and page/update callbacks;
- centralized service ownership rather than scattering node traversal logic;
- optional gesture capability as a separate capability from observation.

Do not borrow:
- generic automation behavior. Crystal's accessibility layer is an observation sensor first.

Source: GitHub search and repository documentation reviewed 2026-09-08.

### 2. Offline-first single-source-of-truth — Linda5823/android-offline-first-client

Repository: https://github.com/Linda5823/android-offline-first-client

Borrow:
- Room as the durable local source of truth;
- repository/domain separation;
- UI reading from local state rather than directly from transient capture callbacks;
- synchronization as a separate concern.

Crystal adaptation:
- raw evidence remains immutable/auditable;
- canonical offers and DriveMetrix records are derived projections;
- network sync must never be required for capture correctness.

Source: GitHub repository documentation reviewed 2026-09-08.

### 3. Accessibility + foreground-service survivability — sumerchoudhary78/adb-watchdog

Repository: https://github.com/sumerchoudhary78/adb-watchdog

Borrow:
- explicit foreground-service ownership;
- watchdog/status thinking;
- recovery/restart paths;
- visible operational state rather than assuming the service is alive.

Crystal adaptation:
- CaptureWatchdog should report last event, last successful parse, last evidence write, screenshot status, service uptime, and current platform.
- Recovery should be conservative: restore capture, never fabricate a missing offer.

Source: GitHub repository documentation reviewed 2026-09-08.

### 4. OEM-kill resilience — sumerchoudhary78/tailscale-watchdog-android

Repository: https://github.com/sumerchoudhary78/tailscale-watchdog-android

Borrow:
- foreground-service plus boot/package-replace recovery;
- explicit acknowledgement that aggressive OEM process killing is a real reliability boundary;
- operational setup checklist.

Crystal adaptation:
- add a Capture Health screen with service state, battery-optimization state, accessibility enabled state, and last-capture age;
- treat service death as a telemetry event.

Source: GitHub repository documentation reviewed 2026-09-08.

### 5. On-device OCR — notune/TextGrab

Repository: https://github.com/notune/TextGrab

Borrow:
- fully on-device OCR architecture;
- ML Kit text recognition as a local fallback for screenshots;
- no-network OCR path.

Crystal adaptation:
- accessibility text is primary;
- OCR is invoked only for corroboration, low-confidence frames, or parser disagreement;
- OCR output becomes evidence linked to the screenshot, not an unquestioned replacement for accessibility text.

Source: GitHub repository documentation reviewed 2026-09-08.

### 6. Explicit user-triggered overlay — FaHadiiii/AssistKit

Repository: https://github.com/FaHadiiii/AssistKit

Borrow:
- persistent foreground ownership;
- compact overlay architecture;
- user-triggered assistance;
- separation of service runtime from UI presentation.

Crystal adaptation:
- eventual ShiftAssist UI can be a small advisory overlay;
- Decision Friction may explain a recommendation without controlling the delivery app;
- no automatic accept/decline in the capture-first phase.

Source: GitHub repository documentation reviewed 2026-09-08.

## Research conclusion

The strongest borrow set is not a single project. It is a combination:

`Accessibility abstraction + event evidence + Room single-source-of-truth + foreground/watchdog health + event-driven screenshot/OCR + user-triggered overlay`

This matches Crystal's existing capture-first architecture without requiring a large automation framework.

## Proposed Crystal upgrades

Priority 1 — capture truth:
1. Add a richer accessibility evidence model: node text/content-description, class, resource ID, bounds, event type, package, timestamp, and capture/session IDs.
2. Persist raw evidence durably before parser inference.
3. Persist OfferEpisode state, not only its current in-memory representation.
4. Add replay fixtures from captured evidence.

Priority 2 — corroboration:
5. Add screenshot cooldown/rate limiting and explicit failure states.
6. Add ML Kit OCR as a deferred/event-driven fallback.
7. Compare accessibility parse vs OCR parse and preserve disagreement as evidence.

Priority 3 — operational reliability:
8. Expand CaptureWatchdog into a real health model.
9. Add boot/package-replace recovery where platform policy permits.
10. Add a user-visible Capture Health screen.

Priority 4 — durable driver intelligence:
11. Introduce Room after the evidence contract stabilizes.
12. Project canonical offers into DriveMetrix.
13. Keep Market Mirror and Decision Friction downstream from observation.

## Borrowing rule

Borrow patterns, not application behavior. Preserve Crystal's evidence boundary, local-first operation, user control, and platform-policy boundary. External projects are inspiration and implementation references; their code should only be copied when license terms permit and attribution/notice requirements are satisfied.

## Candidate reference set

- Krosxx/Android-Auto-Api — accessibility abstraction
- Linda5823/android-offline-first-client — offline-first data architecture
- sumerchoudhary78/adb-watchdog — service watchdog
- sumerchoudhary78/tailscale-watchdog-android — OEM resilience
- notune/TextGrab — local OCR
- FaHadiiii/AssistKit — user-triggered overlay/service separation
