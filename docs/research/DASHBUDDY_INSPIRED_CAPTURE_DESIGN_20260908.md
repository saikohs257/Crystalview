# DashBuddy-Inspired Capture Design — Crystalview

**Date:** 2026-09-08  
**Status:** Design adopted for Capture Fabric V1

## 1. What we are taking from DashBuddy

DashBuddy demonstrates a useful architectural separation for Android screen telemetry:

- normalize raw platform events before domain interpretation;
- deduplicate at the event/pipeline boundary;
- treat sensitive-data suppression as a structural gate;
- separate classification from persistence and other side effects;
- expose a common capture boundary instead of teaching every classifier how to save evidence;
- retain stable structural/content fingerprints for replay and regression analysis.

These are architectural lessons, not code to copy.

## 2. Our version: Evidence Spine

Crystalview will use a simpler capture pipeline optimized specifically for driver offers:

```text
Android Source
    |
    v
Normalize
    |
    v
Evidence Gate -----> Suppression Record
    |
    v
Episode Correlator
    |
    +----> Evidence Store
    |
    v
Offer Interpreter
    |
    v
Canonical Offer
    |
    v
DriveMetrix / Market Mirror / Decision Friction
```

The key difference is that **evidence is first-class**. A parser failure does not erase the observation.

## 3. Why our pipeline differs

DashBuddy's generic event/state-machine architecture is broader than we need. Crystalview is centered on a much narrower question:

> What did the driver actually see, when did it appear, how did the screen change, and what offer episode did those observations belong to?

Therefore:

1. `Episode Correlator` is a first-class stage rather than a generic classifier concern.
2. Raw accessibility evidence is retained before interpretation.
3. Screenshots are corroborating evidence, invoked selectively rather than treated as the primary stream.
4. Platform adapters translate observations into a shared offer model.
5. Longitudinal history is handled by DriveMetrix downstream.
6. Market Mirror consumes canonical observations; it does not contaminate capture with prediction logic.

## 4. Original contracts

### CaptureEnvelope

Every observation carries:

- `captureId`
- `sessionId`
- `capturedAtMs`
- `sourcePackage`
- `eventType`
- `structuralHash`
- `contentHash`
- `evidenceKind`
- `parserVersion`
- `schemaVersion`

### AccessibilityEvidence

Stores the minimum useful representation of the visible accessibility tree:

- node class
- resource/view identifier when available
- visible text
- content description
- bounds
- parent/child relationship or hierarchy path
- enabled/clickable/selected state

Sensitive content is excluded by the evidence gate.

### OfferEpisode

An episode groups multiple observations that represent one offer lifecycle:

```text
episodeId
  -> firstSeen
  -> lastSeen
  -> platform
  -> evidenceIds[]
  -> canonicalOffer?
  -> lifecycleState
```

Lifecycle states are intentionally small:

`CANDIDATE -> PRESENTED -> CHANGED -> ACCEPTED|DECLINED|EXPIRED|UNKNOWN`

No assumption is made that every visible offer produces a terminal state.

## 5. Deduplication: two fingerprints

We keep two independent fingerprints:

- **structuralHash:** UI topology/layout identity; useful for suppressing repeated identical frames.
- **contentHash:** normalized visible content; useful for detecting meaningful offer changes.

A repeated frame can therefore be ignored without losing a later content transition.

## 6. Sensitive-data boundary

The gate runs before interpretation whenever a known sensitive surface can be identified.

If suppression occurs:

- no raw text is persisted;
- no screenshot is requested;
- no offer parser runs;
- only a metadata suppression event may be retained.

The gate is not a keyword classifier pretending to be privacy protection. It is a maintained policy boundary with explicit tests.

## 7. Capture subscriber model

Platform capture code should not know about DriveMetrix, Market Mirror, or Decision Friction.

Instead:

```text
Capture Fabric
      |
      +--> Evidence Store
      +--> Offer Episode Index
      +--> Replay Export
      +--> Canonical Offer Stream
```

Downstream consumers subscribe to derived data.

This prevents intelligence modules from creating capture-side effects or silently changing what is recorded.

## 8. What we will build

### P0 — Capture truth

- Android accessibility source
- package filtering
- normalized node evidence
- structural/content hashes
- sensitive gate
- event/session IDs
- append-only local evidence records

### P1 — Offer episodes

- event correlation
- lifecycle transitions
- Uber/DoorDash platform adapters
- canonical offer extraction
- screenshot corroboration triggers

### P2 — Replay

- deterministic fixture format
- replay parser/classifier
- regression tests
- capture-quality metrics

### P3 — Intelligence

- DriveMetrix historical joins
- Market Mirror state inference
- Decision Friction arbitration
- ShiftAssist presentation

## 9. Explicit non-goals

This layer will not:

- reverse-engineer private platform APIs;
- modify platform state;
- auto-accept or auto-decline offers;
- scrape authenticated customer-facing pages;
- make predictions during capture;
- require network access to preserve evidence.

## 10. Acceptance criterion

The capture system is successful before the parser is successful if it can answer, from local evidence alone:

> What appeared on screen, when it appeared, what changed, what was suppressed, and which observations belonged to the same offer episode?

That is the Crystalview adaptation of the strongest lesson from DashBuddy: **make the pipeline deterministic and replayable first; make intelligence a downstream consumer.**
