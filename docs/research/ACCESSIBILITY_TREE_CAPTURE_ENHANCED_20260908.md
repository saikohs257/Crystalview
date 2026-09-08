# Accessibility Tree Capture — Enhanced Reference #4

**Date:** 2026-09-08  
**Status:** FROZEN — Capture Fabric V1.1 reference layer  
**Purpose:** Define how Crystalview treats Android accessibility-tree observations as structured, time-bounded evidence.

## 1. Core conclusion

An Android accessibility tree is an **observation of an accessibility representation**, not a live copy of the application's internal View hierarchy and not truth about the business domain.

The capture layer therefore preserves the observed representation, its acquisition conditions, and its quality state without prematurely converting it into offer semantics.

```text
ANDROID
   |
Accessibility Event
   |
   v
Frame Acquisition
   |
   v
Observation Window
   |
   v
Accessibility Frame
   |
   +--> Frame Quality
   +--> Node Identity
   +--> Structural Identity
   +--> Content Identity
   |
   v
Durable Evidence
```

## 2. Acquisition must be explicit

`event.source`, active-window root, and available windows are complementary acquisition paths. A frame records **how it was obtained** rather than pretending all sources are equivalent.

Suggested acquisition states:

- `EVENT_SOURCE`
- `ACTIVE_WINDOW_ROOT`
- `WINDOW_ENUMERATION`
- `MULTI_SOURCE_RECONCILIATION`
- `FAILED`

A missing or stale event source is recorded as an acquisition condition, not silently converted into a normal frame.

## 3. Observation Window and stability

A frame is not captured merely because an event arrived. Events and frame acquisition operate on different clocks.

```text
EVENT CLOCK
high-frequency Android events
        |
        v
OBSERVATION WINDOW
        |
   sample / poll
        |
        v
FRAME CANDIDATE
        |
   compare with prior sample
        |
   +----+----+
   |         |
 changed    stable
   |         |
 repeat     emit
```

Stability should be determined from bounded repeated observations rather than a single arbitrary sleep.

The observation window should record:

- trigger timestamp;
- first observation timestamp;
- last sample timestamp;
- stability interval achieved;
- poll interval;
- maximum observation duration;
- timeout state;
- screenshot stability state when screenshot corroboration is attempted.

## 4. Accessibility Frame contract

The raw Android object should be converted into an immutable, app-owned representation.

```text
AccessibilityFrame
├── frameId
├── captureId
├── sessionId
├── observedAtMs
├── acquisitionMethod
├── sourcePackage
├── windowIdentity
├── displayIdentity?
├── rootBounds?
├── nodes[]
├── structuralHash
├── contentHash
├── screenSignature
└── quality
```

A node contains, when available:

- `nodeId`
- `parentId`
- sibling/order index
- class name
- resource/view identifier
- visible text
- content description
- bounds
- enabled/clickable/focusable/focused/selected/scrollable state
- supported actions
- hierarchy path

The representation is canonicalized for replay and comparison, but the original observation remains the evidentiary source.

## 5. Accessibility Representation, not View Hierarchy

The system must use the term **Accessibility Representation** because Android accessibility nodes can expose virtual/custom structures that do not map one-to-one with physical application `View` objects.

This prevents downstream code from assuming that a node is necessarily a concrete Android widget.

## 6. Node identity is layered

No single property is assumed to be permanent identity.

```text
NodeIdentity
|
+-- platformUniqueId?      strongest when useful
+-- resourceId?
+-- semantic signature
+-- hierarchy path
+-- bounds
+-- occurrence/order
```

Identity is used to compare observations, not to assert that a node represents the same business entity forever.

## 7. Frame Identity and Delta

A frame participates in the existing three-level identity model:

```text
Structural Identity → Content Identity → Episode Identity
```

Within content comparison, calculate a non-destructive frame delta:

```text
Frame A
   |
   v
Frame Delta
   |
   +-- added nodes
   +-- removed nodes
   +-- moved nodes
   +-- changed text
   +-- changed state
   +-- changed bounds
   |
   v
Frame B
```

A delta is an interpretation of two captured frames; it never replaces either source frame.

This allows the system to distinguish:

- unchanged surface;
- same surface with changed offer fields;
- new surface with compatible episode identity;
- ambiguous transition.

## 8. Frame quality is first-class evidence

Every frame should carry explicit quality dimensions rather than a single parser confidence value.

```text
FrameQuality
├── acquisition
├── completeness
├── stability
├── freshness
├── sourceConsistency
└── screenshotConsistency?
```

Suggested states:

- `COMPLETE_STABLE`
- `COMPLETE_UNSTABLE`
- `PARTIAL_STABLE`
- `PARTIAL_UNSTABLE`
- `STALE`
- `SOURCE_MISSING`
- `TIMEOUT`
- `FAILED`

A low-quality frame is retained with its quality state. It is not silently discarded or promoted to equivalent status with a strong frame.

## 9. Tree + screenshot evidence bundle

When screenshot capture is requested, the tree and image are associated through a common capture/bundle identity.

```text
                 SAME OBSERVATION WINDOW
                          |
              +-----------+-----------+
              |                       |
              v                       v
      Accessibility Frame       Screenshot Evidence
              |                       |
              +-----------+-----------+
                          |
                          v
                    Evidence Bundle
```

Tree stability and screenshot stability are separate properties. One can be successful while the other fails.

## 10. Field provenance

A parsed field must be traceable to the exact evidence that supported it.

```text
amount = 8.42
  <- frameId
  <- nodeId
  <- hierarchyPath
  <- bounds
  <- parserVersion
  <- confidence
```

If OCR also supports the value, the canonical field may reference both the accessibility node and the screenshot/OCR result.

The raw frame and screenshot are never overwritten by later parser interpretations.

## 11. Reconciliation and disagreement

When multiple acquisition paths or evidence sources disagree, the capture layer records the disagreement.

Examples:

```text
EVENT says window A
FRAME observes window B

TREE says "$8.42"
OCR says "$8.12"

FRAME 1 and FRAME 2 disagree on node identity
```

These become explicit evidence conditions for downstream arbitration. Capture does not silently select a winner.

## 12. Sensitive-data boundary

The evidence gate remains upstream of interpretation.

If a sensitive surface is identified:

- sensitive node text is not persisted;
- screenshots are not requested;
- OCR does not run;
- suppression metadata records that capture was intentionally blocked.

The accessibility-tree layer must preserve no more sensitive content than the policy permits.

## 13. Relationship to the Capture Fabric

Reference #4 complements the three earlier frozen references:

```text
#1 DashBuddy
    pipeline discipline

#2 AppRecording
    temporal transition model

#3 AutoAccounting
    screenshot/OCR evidence

#4 Accessibility Tree Capture
    structured accessibility representation
```

Combined architecture:

```text
Accessibility Event
        |
        v
    Event Buffer
        |
        v
Observation Window
        |
        +------------------+
        |                  |
        v                  v
Accessibility Frame   Screenshot?
        |                  |
        +--------+---------+
                 v
          Evidence Bundle
                 |
                 v
             Frame Delta
                 |
                 v
       Temporal Episode Engine
                 |
                 v
          Platform Adapter
                 |
                 v
         Canonical Offer
```

## 14. What we adopt

- structured accessibility-tree capture;
- explicit acquisition method;
- observation-window stabilization;
- immutable canonical frames;
- layered node identity;
- structural/content fingerprints;
- frame deltas without destructive deduplication;
- frame quality states;
- tree/screenshot evidence bundling;
- field-level provenance;
- explicit disagreement records;
- replayable canonical serialization.

## 15. What we reject

- flattening the tree into text as the primary representation;
- assuming `rootInActiveWindow` is always authoritative;
- treating a single event as a complete UI state;
- treating a node ID or coordinate as permanent business identity;
- silently replacing incomplete frames with later frames;
- making OCR the capture truth;
- hiding acquisition or stability failures;
- allowing downstream inference to mutate captured evidence.

## 16. Acceptance tests

The enhanced accessibility layer is considered frozen only when the design can support these offline tests:

1. An event with a missing source can still yield a frame through an alternate acquisition path or an explicit failure record.
2. A changing UI does not get labeled stable until the stability policy is satisfied or times out.
3. Two otherwise similar frames can produce a meaningful delta when payout/distance/text changes.
4. A partial or stale frame retains its quality state.
5. A parsed field can be traced to its source node and frame.
6. Tree/screenshot disagreement is retained rather than hidden.
7. The same frame can be replayed independently of Android.
8. Later parser/OCR versions can reinterpret evidence without modifying the original frame.

## 17. Frozen design rule

**Capture the accessibility representation exactly enough to reconstruct what was observable, record how and when it was obtained, measure its quality, and never confuse the representation with the business meaning inferred from it.**

**Status: FROZEN.**
