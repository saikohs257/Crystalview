# DashBuddy-Inspired Capture Design — Crystalview

**Date:** 2026-09-08  
**Status:** Design adopted for Capture Fabric V1  
**Principle:** borrow architectural lessons, build original implementation

## 1. What we are taking from DashBuddy

DashBuddy demonstrates useful separation for Android screen telemetry:

- normalize raw platform events before domain interpretation;
- deduplicate at the event/pipeline boundary;
- treat sensitive-data suppression as a structural gate;
- separate classification from persistence and other side effects;
- expose a common capture boundary instead of teaching every classifier how to save evidence;
- retain stable structural/content fingerprints for replay and regression analysis.

These are architectural lessons, not code to copy. The reference pipeline is `Source → Transform → Dedup → Sensitive Gate → Classify → Emit`, with capture attached at the classification boundary. fileciteturn27file0L8-L25

## 2. Enhanced Crystalview architecture: Evidence Spine

We keep the useful discipline but redesign the center around offer evidence rather than a generic app state machine:

```text
Android Source
     |
     v
Normalize ---------------------> Raw Event Journal
     |
     v
Sensitive / Policy Gate -------> Suppression Record
     |
     v
Frame Identity
     |
     +---- structuralHash
     +---- contentHash
     +---- screenSignature
     |
     v
Episode Correlator
     |
     +---------------------> Evidence Store
     |                         |
     |                         +--> Accessibility Evidence
     |                         +--> Screenshot Evidence
     |                         +--> Event Evidence
     |
     v
Platform Adapter
     |
     v
Offer Interpreter
     |
     +---- confidence
     +---- parser version
     +---- field provenance
     |
     v
Canonical Offer
     |
     +--> DriveMetrix
     +--> Market Mirror
     +--> Decision Friction
     +--> ShiftAssist
```

**Critical rule:** interpretation can fail without destroying the underlying observation.

## 3. Enhancement #1 — Raw Event Journal

The capture fabric must retain a bounded, append-oriented raw event journal before normalization is trusted.

Each journal entry contains:

- event ID;
- session ID;
- timestamp;
- source package;
- Android event type;
- window/package metadata;
- normalized event summary;
- evidence availability flags;
- parser/classifier status.

The journal is bounded in live operation but can promote events into durable evidence when they participate in an offer episode or satisfy a capture trigger.

This creates a failure-recovery path:

```text
raw event captured
      |
parser crashes/fails
      |
observation survives
      |
parser repaired later
      |
replay/reinterpretation
```

## 4. Enhancement #2 — Frame Identity instead of simple dedup

We do not treat deduplication as deletion of information.

Three identities are tracked:

### Structural identity
UI topology/layout identity. Useful for determining whether the visible surface fundamentally changed.

### Content identity
Normalized visible content identity. Useful for detecting changed payout, mileage, destination, timer, or other offer fields.

### Episode identity
A temporal correlation identity linking multiple observations to the same offer lifecycle.

Therefore:

```text
same structure + same content
    = repeated observation

same structure + changed content
    = meaningful update

changed structure + compatible offer identity
    = same episode, new surface

ambiguous relationship
    = preserve as UNKNOWN rather than force a merge
```

No evidence is physically discarded merely because it is a duplicate. Live dedup controls processing pressure; durable promotion remains policy-driven.

## 5. Enhancement #3 — Evidence confidence is multidimensional

A single parser confidence number is insufficient.

Canonical interpretation should track at least:

- `captureConfidence` — did we successfully observe the source?
- `structureConfidence` — how confidently do we identify the UI surface?
- `fieldConfidence` — how confidently were individual fields extracted?
- `episodeConfidence` — how confidently does this observation belong to an existing episode?
- `terminalConfidence` — how confidently do we know the lifecycle outcome?

This prevents a strong screen match from being mistaken for strong payout extraction or strong lifecycle knowledge.

## 6. Enhancement #4 — Field-level provenance

Every canonical offer field should be traceable to evidence.

Example:

```text
CanonicalOffer
  payout = $8.75
      <- evidenceId 8f21
      <- nodePath /root/.../TextView[4]
      <- parser uber.offer.v3
      <- confidence 0.97

  miles = 4.8
      <- evidenceId 8f21
      <- nodePath /root/.../TextView[5]
      <- parser uber.offer.v3
      <- confidence 0.94
```

This gives Market Mirror and Decision Friction an evidence lineage rather than an opaque parsed object.

## 7. Enhancement #5 — Event-driven screenshot corroboration

Accessibility remains the primary sensor. Screenshots are corroborating evidence.

Request a screenshot when one of these occurs:

- strong new-offer candidate;
- meaningful content transition;
- parser disagreement;
- low field confidence on a consequential field;
- new/unknown UI signature;
- episode boundary ambiguity;
- explicit forensic/debug capture request.

Screenshot requests must be rate-limited and failures must become explicit evidence metadata rather than silent loss.

The system must never require screenshots for ordinary capture correctness.

## 8. Enhancement #6 — Platform adapters, not platform-specific capture engines

Uber and DoorDash share the same capture fabric.

Only the interpretation layer varies:

```text
                 Capture Fabric
                      |
             +--------+--------+
             |                 |
        Uber Adapter      DoorDash Adapter
             |                 |
             +--------+--------+
                      |
               Canonical Offer
```

Adapters own:

- package identity;
- screen signatures;
- platform-specific field extraction;
- offer-state interpretation;
- platform parser version.

They do **not** own persistence, session management, screenshot storage, or downstream intelligence.

## 9. Enhancement #7 — OfferEpisode becomes the primary correlation object

An episode groups evidence representing one observed offer lifecycle:

```text
episodeId
platform
firstSeenAt
lastSeenAt
status
observationIds[]
canonicalOfferVersions[]
acceptedAt?
declinedAt?
expiredAt?
unknownReason?
```

Lifecycle states remain intentionally conservative:

`CANDIDATE → PRESENTED → CHANGED → ACCEPTED | DECLINED | EXPIRED | UNKNOWN`

A state transition requires evidence. If evidence is insufficient, the system records uncertainty instead of inventing a terminal outcome.

## 10. Enhancement #8 — Capture health becomes measurable

The fabric should expose a health record independent of offer interpretation:

- accessibility service alive;
- events received/sec;
- events normalized/sec;
- events suppressed;
- events promoted;
- parser failures;
- screenshot successes/failures;
- episode correlation rate;
- evidence-store write failures;
- last successful capture timestamp;
- backlog depth.

This separates **"we saw nothing"** from **"the recorder was broken."**

## 11. Enhancement #9 — Replay is a first-class consumer

Every durable evidence record should be replayable without Android.

Replay must be able to answer:

1. What was captured?
2. What did the normalizer produce?
3. What did the platform adapter infer?
4. What parser version produced the result?
5. What would a newer parser produce?
6. Where did the interpretation diverge?

This allows historical DriveMetrix data to improve as parsers improve without requiring the driver to encounter the same offer again.

## 12. Enhanced contracts

### CaptureEnvelope

- `captureId`
- `sessionId`
- `capturedAtMs`
- `sourcePackage`
- `eventType`
- `structuralHash`
- `contentHash`
- `screenSignature`
- `evidenceKind`
- `schemaVersion`
- `captureFabricVersion`

### AccessibilityEvidence

Minimum useful representation:

- node class;
- resource/view identifier when available;
- visible text;
- content description;
- bounds;
- hierarchy path;
- enabled/clickable/selected state;
- window/package metadata.

Sensitive content is excluded by the evidence gate.

### ScreenshotEvidence

- screenshot ID;
- capture ID;
- timestamp;
- display/window metadata;
- image reference;
- capture result;
- failure reason when unavailable;
- trigger reason;
- retention class.

### CanonicalOffer

Every field should carry provenance and confidence. The canonical object is derived data, not the authoritative raw observation.

## 13. Sensitive-data boundary

The gate runs before interpretation whenever a known sensitive surface can be identified.

If suppression occurs:

- no sensitive raw text is persisted;
- no screenshot is requested;
- no offer parser runs;
- only metadata required to prove suppression may be retained.

The gate is a maintained policy boundary, not a keyword classifier pretending to provide privacy protection.

## 14. Downstream isolation

Capture Fabric has no knowledge of prediction or decision policy.

```text
                    Evidence Spine
                         |
          +--------------+--------------+
          |              |              |
      DriveMetrix    Market Mirror   Decision Friction
          |              |              |
          +--------------+--------------+
                         |
                    ShiftAssist
```

Market Mirror can infer hidden marketplace state. Decision Friction can arbitrate decisions. ShiftAssist can present assistance. None can alter what Capture Fabric records.

## 15. What we are deliberately NOT copying

We do not import another project's implementation, dependency structure, naming scheme, or assumptions about its state machine.

We also do not make the capture layer responsible for:

- prediction;
- offer acceptance/decline;
- platform API reverse engineering;
- authenticated customer-page scraping;
- network interception;
- autonomous platform interaction.

## 16. Build sequence

### P0 — Truth

- accessibility source;
- package filtering;
- session IDs;
- raw event journal;
- normalized accessibility evidence;
- structural/content fingerprints;
- policy gate;
- append-only local persistence.

### P1 — Correlation

- frame identity;
- OfferEpisode correlator;
- platform adapters;
- field-level provenance;
- confidence vectors;
- screenshot trigger engine;
- capture health metrics.

### P2 — Replay

- deterministic fixture format;
- evidence replay;
- parser version comparison;
- regression corpus;
- capture-quality scoring.

### P3 — Intelligence

- DriveMetrix joins;
- Market Mirror state inference;
- Decision Friction arbitration;
- ShiftAssist presentation.

## 17. Acceptance tests

The enhanced fabric is considered successful when it can demonstrate all of the following offline:

- repeated identical frames are recognized without corrupting the evidence timeline;
- changed offer content creates a meaningful transition;
- multiple observations can be correlated into one episode;
- ambiguous observations remain UNKNOWN;
- parser failure does not destroy evidence;
- field values can be traced to source evidence;
- screenshot failure is explicit;
- sensitive surfaces produce suppression metadata without content leakage;
- the same evidence can be replayed under two parser versions;
- capture health distinguishes recorder failure from genuine absence of offers.

## 18. Final architectural rule

DashBuddy gives us the **pipeline discipline**.

Crystalview adds the **evidence spine, episode model, provenance, replayability, and longitudinal memory boundary** required for our problem.

The resulting system is intentionally boring at the bottom and increasingly intelligent as information moves upward:

```text
CAPTURE → EVIDENCE → CORRELATION → INTERPRETATION → MEMORY → INFERENCE → DECISION
```

That is the enhanced Crystalview version.