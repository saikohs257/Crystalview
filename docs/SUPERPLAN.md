# Crystalview Superplan

## Purpose

Build one driver intelligence system from the recovered CrystalClearview, DriveMetrix, Market Mirror, Decision Friction, and ShiftAssist concepts. Capture Fabric is the evidence foundation; it is not the product boundary.

## Canonical flow

`Shift -> Capture -> Evidence -> Parse -> OfferEpisode -> CanonicalOffer -> Decision -> Trip -> Outcome -> DriveMetrix -> MarketMirror -> DecisionFriction -> ShiftAssist`

The loop closes when actual outcomes return to DriveMetrix for review and calibration.

## System layers

### 1. Capture Fabric
Primary live sensor for supported driver apps. Capture raw accessibility evidence, package identity, event metadata, observation windows, screenshots when warranted, and capture health/watchdog state. Never make capture truth depend on parser success.

### 2. CrystalClearview
Canonical observation and ledger foundation. Owns evidence identity, canonical offer records, provenance, local persistence/export, shift/session records, and capture diagnostics.

### 3. DriveMetrix
Longitudinal memory and reconciliation layer. Join offers to decisions, trips, pickup/dropoff, pay, tips, incentives, miles, duration, location, city/day, merchant/destination context, and outcomes. Preserve unresolved joins rather than inventing matches.

### 4. Market Mirror
Infer marketplace state from observable consequences. Candidate dimensions include demand pressure, supply pressure, merchant friction, destination quality, position value, platform urgency, eligibility, and driver friction. Support Promise Gap, Why-Me, Position Value, transition modeling, and Opportunity Surface.

### 5. Decision Friction
Evidence-aware decision arbitration using uncertainty, divergence, evidence class, and historical calibration. Shadow-first. No autonomous platform action.

### 6. ShiftAssist
Driver-facing operational assistance. Present current offer facts, relevant DriveMetrix history, Market Mirror state, Decision Friction output, and legitimate arrival context. The driver remains the decision authority.

## Recovered historical capability set

The following are requirements recovered from the earlier Crystal/ShiftAssist design, not features invented from external products:

- shift/session reconstruction;
- online time, distance, earnings, net/hour, and net/mile;
- anchor/staging-zone memory;
- dead-zone behavior and zone transitions;
- merchant profiles and friction history;
- apartment/destination memory and arrival artifacts;
- weather/physical context where legitimately available;
- offer evidence and offer lifecycle;
- decision records and reason codes;
- pickup/dropoff lifecycle;
- actual time, miles, payout, tips, wait/drop pain, and issue outcomes;
- prediction-vs-outcome review;
- calibration/learning from actual outcomes;
- background enrichment kept outside the truth spine.

## Canonical record graph

`shift_session -> offer_event -> offer_frame -> offer_decision -> pickup_event -> dropoff_event -> trip_outcome -> historical_memory`

Context records attach without replacing evidence:

`location + zone + merchant + destination + weather + legitimate physical context`

## Build order

### Phase A — restore the truth spine
1. Keep the Android project canonical in this repository.
2. Integrate Capture Fabric V1 source.
3. Validate screenshot capability and API-gated handling.
4. Strengthen accessibility evidence and raw-event persistence.
5. Add durable observation-window/frame/episode persistence.
6. Add deterministic replay fixtures.

### Phase B — restore the original driver ledger
7. Define the canonical CrystalClearview/DriveMetrix local schema.
8. Import and reconcile existing delivery ledgers.
9. Implement shift/session reconstruction.
10. Implement offer -> decision -> trip -> outcome lifecycle.
11. Implement conservative offer-to-trip/outcome joins and unresolved-join storage.
12. Restore zone/anchor/dead-zone, merchant, destination/apartment, and legitimate context records.

### Phase C — platform interpretation
13. Implement platform-specific Uber and DoorDash adapters only after raw capture is proven.
14. Add field-level provenance, confidence, parser versioning, and disagreement records.
15. Keep OCR/screenshot interpretation downstream of original evidence.

### Phase D — intelligence
16. Port Market Mirror as a pure downstream inference subsystem.
17. Port Decision Friction as shadow-first arbitration.
18. Restore prediction-vs-outcome review and calibration.
19. Add ShiftAssist presentation and arrival assistance.
20. Keep enrichment asynchronous and outside the canonical observation spine.

### Phase E — proof
21. Add Gradle wrapper and GitHub Actions Android build/test pipeline.
22. Build the debug APK in CI.
23. Add parser and replay tests.
24. Validate package identifiers and capture behavior on the actual phone.
25. Only then expand live advisory behavior.

## Non-negotiables

- Evidence before intelligence.
- Parser failure is not observation failure.
- Observation, inference, arbitration, and presentation remain separate.
- Higher-level interpretation never overwrites lower-level evidence.
- Network access is not required for capture correctness.
- No credential harvesting, CAPTCHA bypass, mass automated requests, authenticated customer scraping, impersonation, or manipulation of platform state.
- No autonomous accept/reject/click behavior in the capture foundation.
- Every derived inference carries provenance and evidence class.

## External-research rule

GigU and other external products are validation/inspiration sources only. Borrow implementation patterns where useful; do not replace the recovered Crystal/Drive/ShiftAssist capability model with a competitor's product model.

## Current status

The repository now explicitly restores the historical capability spine. Capture Fabric V1 is the current implementation focus. The higher layers are architectural requirements and remain unclaimed until implemented and tested.
