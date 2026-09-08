# GitHub Capture Research Findings

**Date:** 2026-09-08  
**Project:** CrystalClearview / DriveMetrix / Market Mirror / ShiftAssist  
**Repository:** `saikohs257/Crystalview`  
**Working branch:** `capture-fabric-v1`

## 1. Purpose

This document records the external GitHub research performed while designing Crystal's Capture Fabric. The goal was not to copy another application, but to identify proven implementation patterns for Android AccessibilityService capture, screen classification, deduplication, replayable evidence, privacy gating, and delivery-driver decision support.

The findings below are separated into **observed source patterns** and **Crystal design conclusions**. They should be treated as research evidence, not as proof that every referenced implementation is correct or appropriate for Crystal.

---

## 2. Executive finding

The strongest finding is that several independent projects have converged on an architecture very close to the one Crystal needs:

```text
Raw platform event
        ↓
Transform / normalize
        ↓
Deduplicate
        ↓
Privacy / sensitive gate
        ↓
Classify
        ↓
Capture / evidence subscriber
        ↓
Emit typed domain event
```

This validates the direction of Capture Fabric while also identifying several upgrades to our current implementation:

1. Separate **structural identity** from **content identity**.
2. Normalize the accessibility tree into a reusable `UiNode` model.
3. Make capture a subscriber to the pipeline rather than embedding persistence inside classifiers/parsers.
4. Put privacy gating before classification.
5. Treat platform recognition and offer parsing as separate concerns.
6. Make captured evidence replayable.
7. Use screenshots/OCR as corroborating evidence when accessibility evidence is incomplete, rather than making screenshots the primary sensor.
8. Preserve the strict boundary between observable offer-surface measurement and attempts to infer or reproduce platform internals.

---

# 3. DashBuddy — strongest architectural reference

**Repository:** `sjtrotter/DashBuddy`  
**Description:** DoorDash driver decision-support application.  
**Observed status:** Kotlin project with active development visible in September 2026 metadata and substantial issue history.

## 3.1 Accessibility-based screen observation

DashBuddy documents reading the contractor's own device screen through Android's `AccessibilityService` API and identifying platform screens through a chain of `ScreenMatcher` implementations. Its design is explicitly centered on observing information already visible to the driver rather than modifying the DoorDash application.

This is directly relevant to Crystal's intended capture path.

## 3.2 ScreenMatcher architecture

DashBuddy separates recognition into matcher components instead of building one monolithic parser.

Conceptually:

```text
ScreenMatcher
    ├── identify platform/screen
    └── hand recognized surface to downstream logic
```

This supports a Crystal separation of:

```text
PlatformMatcher
        ↓
Screen / Offer Classifier
        ↓
Offer Parser
```

The matcher answers **"what am I looking at?"** while the parser answers **"what fields are present?"**.

## 3.3 Canonical pipeline architecture

DashBuddy's ADR-0004 describes a six-stage canonical pipeline:

```text
Source → Transform → Dedup → Sensitive Gate → Classify → Emit
```

Capture subscribes between classification and emission rather than being implemented as a side effect inside individual factories.

The ADR identifies problems caused by organically grown pipelines having separate capture, persistence, deduplication, quota, and classification behavior. The proposed architecture centralizes those concerns.

### Crystal implication

This strongly supports making `CaptureFabric` a shared observation substrate consumed by Uber, DoorDash, DriveMetrix, Market Mirror, and later ShiftAssist rather than creating separate capture implementations for each subsystem.

## 3.4 Structural hash vs content hash

DashBuddy's capture event model distinguishes:

- `structuralHash` — structural identity of the UI tree.
- `contentHash` — identity of the actual displayed content.

This is important because a delivery offer can remain on the same UI layout while its timer, payout, or other values change.

### Crystal upgrade

Replace a single generic offer signature with at least:

```text
structuralHash
contentHash
```

Potential future identity layers:

```text
platform
screenClass
structuralHash
contentHash
offerIdentity
episodeIdentity
```

The hashes should remain evidence identifiers, not be mistaken for semantic offer IDs.

## 3.5 Sensitive gate before classification

DashBuddy's ADR argues that sensitive screens should be blocked structurally before they reach classifiers. Its documented pattern is:

```text
Source
  ↓
Transform
  ↓
Dedup
  ↓
Sensitive Gate
  ↓
Classify
```

The stated rationale is that classification itself can expose the sensitive UI tree to downstream code. A sensitive screen should therefore not be merely classified as `SENSITIVE` after raw content has already passed through the classifier.

### Crystal implication

Crystal should have a privacy gate before offer parsing/classification where feasible:

```text
Accessibility event
    ↓
Package gate
    ↓
Sensitive gate
    ↓
Structural/content identity
    ↓
Offer classification
```

The gate should produce metadata indicating that content was intentionally suppressed, without persisting the sensitive payload.

## 3.6 Capture subscriber pattern

DashBuddy's architecture moves snapshot/log persistence out of individual pipeline factories and into a unified capture service/subscriber.

### Crystal implication

`OfferParser`, `UberMatcher`, and `DoorDashMatcher` should not own file/database persistence.

They should emit typed evidence or classification results into Capture Fabric.

Persistence belongs to the capture/evidence layer.

## 3.7 Replay-oriented evidence

DashBuddy's capture envelopes include pipeline identity/version metadata and structured capture information suitable for later analysis/regression work.

### Crystal implication

Every important observation should carry enough metadata to answer:

- what produced this observation?
- when?
- from which package?
- under which parser/capture schema version?
- what evidence was available?
- what was inferred later?

---

# 4. AppRecording — replay-oriented AccessibilityService capture

**Repository:** `srknskr/apprecording`  
**Description:** Research prototype for replay-oriented Android user-interaction recording and automated app testing.

## 4.1 Relevant pattern

The project records accessibility-derived interaction/screen information in a form intended for replay and analysis.

The useful conceptual fields include information such as:

- package
- timestamp
- visible text
- class
- resource ID
- hierarchy path
- coordinates/bounds
- UI hierarchy
- interaction context

## 4.2 Crystal implication: richer raw evidence

Our initial raw event model was too thin:

```text
package
EventType
timestamp
text
```

A stronger normalized evidence model should preserve selected accessibility-node attributes:

```text
CaptureEvent
├── captureId
├── timestamp
├── packageName
├── eventType
├── windowId
├── sourceNode
│   ├── className
│   ├── resourceId
│   ├── text
│   ├── contentDescription
│   ├── bounds
│   ├── clickable
│   └── hierarchyPath
├── structuralHash
├── contentHash
└── schemaVersion
```

This should be implemented carefully: capture only fields needed for reconstruction and analysis, and enforce the privacy gate before sensitive content is persisted.

---

# 5. AutoAccounting — screenshot implementation reference

**Repository:** `AutoAccountingOrg/AutoAccounting`  
**Relevant file:** `OcrTools.kt`.

## 5.1 Observed implementation

The project contains a Kotlin implementation using Android's AccessibilityService screenshot facility. The implementation:

1. checks Android version support;
2. obtains the active accessibility service;
3. calls `takeScreenshot`;
4. converts the returned hardware buffer into a `Bitmap`;
5. saves the bitmap;
6. closes the hardware buffer;
7. records failure when screenshot capture fails.

This is concrete evidence that the screenshot API path we are using is a practical Android implementation pattern, although Crystal still needs device-level validation.

## 5.2 Crystal implication

The existing Crystal screenshot path is directionally correct, but should be hardened around:

- lifecycle/cancellation;
- screenshot cooldown/rate limiting;
- hardware-buffer cleanup;
- failure reason recording;
- device/API compatibility;
- secure-window failure;
- evidence linkage between screenshot and accessibility event.

Screenshot capture should remain event-driven rather than continuous.

Recommended linkage:

```text
accessibility captureId
        │
        ├── raw event evidence
        ├── parsed observation
        └── optional screenshot evidence
```

---

# 6. DashBuddy legal/operational framing

DashBuddy's legal documentation is not legal advice and should not be treated as a legal conclusion for Crystal. It is nevertheless useful as a project-design reference.

## 6.1 Observable surface framing

The project deliberately frames its analytical work as empirical measurement of the visible offer surface rather than reverse engineering of platform internals.

It explicitly distinguishes:

```text
Observable:
what the driver sees

from

Internal:
how the platform generates that information
```

### Crystal / Market Mirror implication

This is an important governance boundary.

Market Mirror can study:

- offer characteristics;
- timing;
- payout;
- mileage;
- displayed duration;
- visible destination information;
- observed acceptance/decline outcomes;
- marketplace conditions experienced by the driver;
- statistical relationships among observable variables.

It should not require:

- decompiling platform binaries;
- intercepting platform traffic;
- modifying platform applications;
- bypassing authentication/CAPTCHA;
- automating platform actions without explicit authorization;
- claiming to have recovered proprietary dispatch/routing/pricing internals merely from observational data.

The distinction is especially important for the language used in documentation and public-facing project descriptions.

---

# 7. Cross-project synthesis

The independent repositories produce a remarkably consistent design signal.

| Finding | Source | Crystal action |
|---|---|---|
| AccessibilityService is viable for screen observation | DashBuddy, AppRecording | Keep AccessibilityService primary |
| Screen recognition should be modular | DashBuddy | Build `ScreenMatcher`/platform adapters |
| Structural and content identity differ | DashBuddy | Add two hashes |
| Capture should be a shared subscriber | DashBuddy | Centralize evidence persistence |
| Sensitive data should be blocked early | DashBuddy | Add structural privacy gate |
| Accessibility trees can support replay | AppRecording | Expand normalized `UiNode` evidence |
| Screenshot API is practical | AutoAccounting | Harden event-driven screenshot path |
| Screenshot should complement other evidence | Synthesis | Keep accessibility primary |
| Visible offer-surface analysis is a safer framing | DashBuddy | Preserve Market Mirror observation boundary |

---

# 8. Revised Crystal Capture Fabric architecture

Based on the research, the preferred architecture is now:

```text
                    ANDROID
                       │
              AccessibilityService
                       │
                       ▼
               ┌───────────────┐
               │  RAW CAPTURE   │
               └───────┬───────┘
                       │
                       ▼
                PACKAGE GATE
                       │
                       ▼
               SENSITIVE GATE
                       │
                       ▼
             NORMALIZE UI TREE
                       │
              ┌────────┴────────┐
              ▼                 ▼
       STRUCTURAL HASH     CONTENT HASH
              │                 │
              └────────┬────────┘
                       ▼
                    DEDUP
                       │
                       ▼
               PLATFORM MATCHER
                 ┌─────┴─────┐
                 ▼           ▼
               UBER       DOORDASH
                 │           │
                 └─────┬─────┘
                       ▼
                OFFER CLASSIFIER
                       │
                       ▼
                  OFFER PARSER
                       │
              ┌────────┴────────┐
              ▼                 ▼
          CANONICAL         OPTIONAL
           OFFER           SCREENSHOT
              │                 │
              └────────┬────────┘
                       ▼
                EVIDENCE STORE
                       │
                       ▼
                  OFFER EPISODE
                       │
              ┌────────┼────────┐
              ▼        ▼        ▼
         DriveMetrix  Market   Blackadder
                     Mirror
              │        │        │
              └────────┼────────┘
                       ▼
                  ShiftAssist
```

The key architectural principle is that **capture remains upstream of intelligence**.

Market Mirror and Blackadder should consume observations rather than alter the capture path.

---

# 9. Revised data model

## 9.1 Raw evidence

```text
RawAccessibilityEvent
- captureId
- timestamp
- packageName
- eventType
- windowId
- normalizedUiNode
- structuralHash
- contentHash
- captureSchemaVersion
```

## 9.2 Screenshot evidence

```text
ScreenshotEvidence
- screenshotId
- captureId
- timestamp
- reason
- API/device metadata
- success/failure
- artifact reference
```

## 9.3 Canonical observation

```text
CanonicalOffer
- offerId
- episodeId
- platform
- capturedAt
- payout
- miles
- minutes
- dollarsPerMile
- dollarsPerHour
- parserConfidence
- parserVersion
- evidenceRefs
```

## 9.4 Episode

```text
OfferEpisode
- episodeId
- platform
- firstSeen
- lastSeen
- structuralIdentity
- observationCount
- terminalState
- evidenceRefs
```

This prevents one real offer from becoming dozens of independent offers merely because AccessibilityService emitted multiple events.

---

# 10. What this research does NOT establish

These repositories do not establish that:

- Crystal's current Android code compiles on a physical device;
- Uber or DoorDash expose identical accessibility trees on every Android version;
- screenshot capture succeeds on every device/window;
- a particular parser will work against the current production UI;
- AccessibilityService usage is automatically permitted by every distribution channel or platform policy;
- any legal interpretation in another project applies to Crystal;
- Market Mirror can recover proprietary platform algorithms.

Those remain validation questions.

---

# 11. Immediate implementation backlog

### P0 — Capture correctness

- [ ] Add `structuralHash` and `contentHash` as first-class evidence fields.
- [ ] Build normalized `UiNode` model.
- [ ] Separate package gate, privacy gate, dedup, matcher, classifier, parser.
- [ ] Preserve raw evidence before parsing.
- [ ] Link screenshots to capture IDs.
- [ ] Record screenshot failure reason.
- [ ] Add stronger offer-episode lifecycle persistence.

### P1 — Replayability

- [ ] Define capture envelope/schema version.
- [ ] Add deterministic replay fixtures.
- [ ] Add parser regression tests from captured fixtures.
- [ ] Add structural/content hash regression tests.

### P2 — Platform adapters

- [ ] Uber screen matcher.
- [ ] DoorDash screen matcher.
- [ ] Uber offer parser.
- [ ] DoorDash offer parser.
- [ ] Unknown-screen capture path.

### P3 — Intelligence integration

- [ ] Feed canonical observations into DriveMetrix.
- [ ] Feed longitudinal observations into Market Mirror.
- [ ] Keep Blackadder shadow-only until outcome evidence exists.
- [ ] Keep ShiftAssist downstream of the evidence layer.

---

# 12. Architectural conclusion

The research does **not** suggest adding a large number of new subsystems.

It suggests making the existing Capture Fabric more disciplined and more generic.

The preferred architecture is therefore:

```text
ONE capture fabric
       ↓
ONE evidence model
       ↓
platform-specific matchers/parsers
       ↓
ONE longitudinal driver ledger
       ↓
multiple downstream consumers
```

CrystalClearview remains the observation foundation.

DriveMetrix remains longitudinal driver memory.

Market Mirror becomes marketplace-state inference over observable evidence.

Blackadder remains decision-friction/arbitration.

ShiftAssist remains operational assistance.

The major lesson from the GitHub research is that **the capture layer should be boring, deterministic, replayable, privacy-aware, and difficult to fool. Intelligence belongs downstream.**

---

## Research references

- `sjtrotter/DashBuddy` — Accessibility-based DoorDash driver decision support; canonical pipeline and ScreenMatcher architecture; legal/operational framing.
- `srknskr/apprecording` — AccessibilityService recording and replay-oriented UI evidence.
- `AutoAccountingOrg/AutoAccounting` — concrete Android AccessibilityService screenshot implementation in `OcrTools.kt`.

Research evidence captured during the 2026-09-08 GitHub investigation includes DashBuddy's canonical pipeline ADR, DashBuddy's legal/architecture material, AppRecording repository metadata, and AutoAccounting's screenshot implementation. These sources are external references and are not Crystal components unless explicitly incorporated into the Crystal repository.
