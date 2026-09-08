# Crystalview Superplan

## Purpose

Build one driver intelligence system from the recovered CrystalClearview, DriveMetrix, Market Mirror, Blackadder, and ShiftAssist concepts.

## Canonical flow

`Capture -> Evidence -> Parse -> OfferEpisode -> CanonicalOffer -> DriveMetrix -> MarketMirror -> Blackadder -> ShiftAssist`

## Layers

### 1. Capture Fabric
Primary live sensor for supported driver apps. Capture raw accessibility evidence, package identity, event metadata, and event timing. Attempt screenshots as corroborating/forensic evidence when warranted. Maintain capture health and watchdog state.

### 2. CrystalClearview
Canonical observation and application foundation. Owns evidence identity, canonical offer records, provenance, local storage, export, and capture diagnostics.

### 3. DriveMetrix
Longitudinal memory. Join offers to accepted trips, pickup/dropoff, pay, tips, incentives, miles, time, location, city/day, and outcome history. Preserve historical driver-specific context.

### 4. Market Mirror
Infer marketplace state from observable consequences. Maintain observation/inference separation. Candidate state dimensions include demand pressure, supply pressure, merchant friction, rejection/fulfillment pressure, destination quality, position value, platform urgency, eligibility, and driver friction. Support Promise Gap, Why-Me, Position Value, transition modeling, and Opportunity Surface.

### 5. Blackadder / Decision Friction
Arbitrate decisions using evidence class, uncertainty, divergence, and historical calibration. Shadow-first. No autonomous platform action.

### 6. ShiftAssist
Present useful operational assistance to the driver, including arrival context and decision guidance.

## Build order

1. Make Android project canonical in this repository.
2. Integrate Capture Fabric V1 source.
3. Fix screenshot capability declaration and validate API-gated screenshot handling.
4. Strengthen accessibility evidence model and raw-event persistence.
5. Implement platform-specific Uber and DoorDash parsing adapters.
6. Implement offer lifecycle/replay harness.
7. Establish canonical DriveMetrix ledger schema and joins.
8. Port Market Mirror inference as a pure downstream subsystem.
9. Port Blackadder shadow arbitration.
10. Add ShiftAssist presentation.
11. Add GitHub Actions Android build/test pipeline.

## Non-negotiables

- Evidence before intelligence.
- Parser failure is not observation failure.
- Observation, inference, and decision remain separate.
- No credential harvesting, CAPTCHA bypass, mass automated requests, authenticated customer scraping, impersonation, or manipulation of platform state.
- No autonomous accept/reject/click behavior in the capture foundation.
- Every derived inference carries provenance and evidence class.

## Current status

Repository initialized on 2026-09-08. Architecture is committed. Capture implementation exists as a recovered local source package and is being integrated into this canonical repository. Build proof must come from CI or a real Android build environment, not from static inspection alone.
