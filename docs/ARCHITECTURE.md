# Crystalview Architecture

## Purpose

Crystalview is the single driver-intelligence system reconstructed from the historical CrystalClearview, DriveMetrix, and ShiftAssist designs, with Market Mirror and Decision Friction downstream. Capture Fabric is the evidence boundary, not the whole product.

## Runtime boundary

Capture is the truth boundary. Accessibility is the primary live sensor. Screenshots are corroborating/forensic evidence. Parsing and inference occur only after raw evidence is preserved.

## System layers

1. **Capture Fabric** — live observation of supported driver apps: package identity, accessibility events, raw evidence, screenshots when warranted, watchdog/health.
2. **CrystalClearview** — canonical observation and driver ledger foundation: evidence identity, canonical offers, provenance, local persistence/export, shift/session records, and capture diagnostics.
3. **DriveMetrix** — longitudinal driver memory: offers, decisions, trips, pickup/dropoff, pay/tips/incentives, miles/time, location, city/day, merchants, destinations, dwell, arrival context, and outcomes.
4. **Market Mirror** — downstream marketplace-state inference: demand/supply pressure, merchant friction, destination quality, position value, platform urgency, eligibility, driver friction, Promise Gap, Why-Me, Position Value, transition modeling, Opportunity Surface.
5. **Decision Friction** — evidence-aware decision arbitration, uncertainty/divergence handling, historical calibration, and shadow evaluation. Historical alias: Blackadder.
6. **ShiftAssist** — driver-facing operational presentation: current facts, historical context, decision guidance, and arrival assistance.

## Historical capability spine

The system must preserve the original driver workflow rather than collapsing it into offer grading:

`Shift -> Context -> Offer -> Decision -> Trip -> Outcome -> Memory -> Calibration`

Historical Crystal/ShiftAssist capabilities that remain architectural requirements include:

- shift/session reconstruction;
- online time, distance, earnings, net/hour, and net/mile accounting;
- anchor/staging-zone memory and dead-zone behavior;
- zone transitions and escape/recovery value;
- merchant memory and friction;
- apartment/destination memory and arrival artifacts;
- weather and other legitimate context;
- offer observations and evidence;
- decision records and reason codes;
- pickup/dropoff lifecycle;
- actual time, miles, payout, tips, wait/drop pain, and issue outcomes;
- prediction-vs-outcome review and calibration.

These are not new GigU-derived features. External research may improve their implementation, but does not redefine ownership.

## Canonical data relationship

`offer_event -> offer_frame -> offer_decision -> pickup_event -> dropoff_event -> trip_outcome -> historical_memory`

Context attaches to the same canonical records:

`shift_session + location + zone + merchant + destination + weather + legitimate physical context`

A missing relationship remains unresolved/missing evidence. It is never silently fabricated.

## Evidence hierarchy

`RAW_EVENT -> ACCESSIBILITY_FRAME -> SCREENSHOT -> STRUCTURED_FIELD -> OFFER_EPISODE -> OUTCOME`

Higher-level interpretations never overwrite lower-level evidence. Parser failure is not observation failure.

## Canonical runtime path

`Driver App -> Capture Fabric -> Evidence -> Observation Window/Frame -> Platform Adapter -> OfferEpisode -> Canonical Offer -> CrystalClearview/DriveMetrix -> Market Mirror -> Decision Friction -> ShiftAssist -> Driver Decision -> Trip/Outcome -> DriveMetrix`

## Ownership rules

- Capture Fabric owns observation, never decisions.
- CrystalClearview owns canonical observation records and the ledger foundation.
- DriveMetrix owns longitudinal memory and outcome reconciliation.
- Market Mirror owns marketplace inference, never observation truth.
- Decision Friction owns arbitration, never raw evidence.
- ShiftAssist owns presentation, never platform control.
- No layer silently rewrites another layer's truth.

## Core safety rule

No credential harvesting, authenticated customer scraping, platform-state manipulation, spoofing, or autonomous accept/reject/click behavior in the capture foundation. The driver remains the decision authority.
