# Crystal / Drive / ShiftAssist Capability Recovery Crosswalk

Date: 2026-09-08
Branch: `capture-fabric-v1`

## Purpose

Restore the historical capability model before adding new product features. This document distinguishes capabilities recovered from the earlier CrystalClearview / DriveMetrix / ShiftAssist design from capabilities newly strengthened by the current Capture Fabric work.

Status vocabulary:

- **PRESERVED** — represented in the current architecture/specification.
- **IMPLEMENTED** — present in current repository/runtime source.
- **PARTIAL** — some infrastructure exists, but the historical capability is not complete.
- **MISSING** — historical requirement is not yet represented sufficiently in runtime implementation.
- **SUPERSEDED** — ownership or implementation mechanism changed while preserving the underlying capability.
- **DEFERRED** — intentionally downstream/future; not a defect in the capture foundation.

## Crosswalk

| Historical capability | Intended role | Current status | Correct home |
|---|---|---|---|
| Delivery ledger | Daily earnings, distance, time, net economics | PARTIAL | CrystalClearview + DriveMetrix |
| Shift/session reconstruction | Turn raw activity into work sessions | MISSING | CrystalClearview / DriveMetrix |
| Online hours | Shift-level accounting | MISSING | DriveMetrix |
| Net/hour and net/mile | Driver economics | MISSING | DriveMetrix / downstream analytics |
| Offer capture | Observe offered work | IMPLEMENTED at foundation level | Capture Fabric -> CrystalClearview |
| Offer evidence/screenshots | Preserve what was shown | IMPLEMENTED | Capture Fabric |
| Offer lifecycle | Correlate repeated observations | PARTIAL | Capture Fabric / CrystalClearview |
| Decision record | Preserve accept/decline choice and reason | MISSING | DriveMetrix |
| Pickup/dropoff lifecycle | Connect offer to actual trip | MISSING | DriveMetrix |
| Actual payout/tips/incentives | Record realized economics | PARTIAL via historical imports/spec | DriveMetrix |
| Actual miles/time | Record realized work | PARTIAL via historical imports/spec | DriveMetrix |
| Offer -> outcome reconciliation | Compare promise to realization | MISSING runtime; PRESERVED architecture | DriveMetrix |
| Prediction vs outcome review | Determine whether guidance was useful | DEFERRED | Decision Friction / DriveMetrix |
| Calibration/learning | Update downstream models from outcomes | DEFERRED | Decision Friction / Market Mirror |
| Anchor/staging zones | Remember productive operating locations | MISSING runtime | DriveMetrix / Market Mirror |
| Dead-zone behavior | Remember poor recovery areas | MISSING runtime | DriveMetrix / Market Mirror |
| Zone transitions | Model movement/escape value | MISSING runtime | Market Mirror |
| Merchant memory | Wait/friction history | MISSING runtime | DriveMetrix |
| Destination/apartment memory | Arrival/entry knowledge | MISSING runtime | DriveMetrix / ShiftAssist |
| Weather/physical context | Context attached to observations | MISSING runtime | DriveMetrix / Market Mirror |
| Arrival assistance | Driver-facing destination help | DEFERRED | ShiftAssist |
| Context engine | Combine offer + driver + place context | DEFERRED | Market Mirror / ShiftAssist |
| Decision engine | Evaluate candidate actions | DEFERRED | Decision Friction |
| Background enrichment | Add external/local context asynchronously | DEFERRED | Enrichment subsystem |
| Replay | Reprocess evidence and parsers | PARTIAL; fixtures next | Capture Fabric / CrystalClearview |
| Capture health/watchdog | Detect loss of observation | IMPLEMENTED foundation | Capture Fabric |
| Platform-specific adapters | Uber/DoorDash interpretation | MISSING | Capture Fabric downstream parser layer |
| OCR fallback | Screenshot interpretation/corroboration | DEFERRED | Evidence interpretation layer |

## Corrected system model

The historical system should be implemented as one loop, not as a collection of feature apps:

`SHIFT -> OBSERVE -> PRESERVE -> CORRELATE -> INTERPRET -> DECIDE -> TRIP -> OUTCOME -> MEMORY -> CALIBRATE -> ASSIST`

The current Capture Fabric owns only the observation/preservation portion of that loop. CrystalClearview and DriveMetrix restore the canonical ledger and longitudinal memory. Market Mirror and Decision Friction remain downstream inference/arbitration systems. ShiftAssist presents the result.

## Data ownership

### Capture Fabric

Owns raw events, accessibility frames, screenshots, capture IDs, evidence quality, parser inputs, and capture health. It must never depend on successful parsing for truth preservation.

### CrystalClearview

Owns canonical observation identity, offer records, provenance, local storage/export, and the canonical driver ledger foundation.

### DriveMetrix

Owns shift/session history, delivery lifecycle, outcome records, historical joins, economics, context memory, and unresolved relationships.

### Market Mirror

Consumes historical observations and outcomes to infer marketplace state and transition/position behavior. It never becomes the observation authority.

### Decision Friction

Consumes evidence, inference, uncertainty, divergence, and historical calibration to arbitrate. It remains shadow-first and does not control the platform.

### ShiftAssist

Presents already-observed/inferred/arbitrated information and arrival context. It does not capture or mutate platform state.

## What external research changed

External products such as GigU validate that offer capture, screenshots, trip history, economics, and outcome comparison are useful. They do not add those capabilities to the Crystal roadmap; those capabilities were already in the historical design.

Useful external implementation patterns may still strengthen the system:

- detection latency/trace instrumentation;
- event-driven screenshot escalation;
- durable episode persistence;
- parser/version disagreement records;
- explicit health/degradation states;
- replay fixtures;
- conservative evidence-preserving reconciliation.

These are implementation improvements, not replacements for the historical architecture.

## Engineering consequence

Do not build a second "GigU layer." Restore the missing historical ledger/outcome/context spine behind the Capture Fabric, then connect downstream inference and ShiftAssist to that spine.

## Source discipline

This crosswalk records the recovered historical architecture and the current repository state. Where the historical artifact did not establish a concrete implementation detail, this document marks the capability as missing/deferred rather than inventing a design claim.
