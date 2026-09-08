# Crystalview Engineering Progress — 2026-09-08

Branch: `capture-fabric-v1`

## Implemented in this pass

### Build gate
- GitHub Actions Android workflow added.
- JDK 17 configured.
- Gradle 8.7 configured through `gradle/actions/setup-gradle`.
- Debug APK is configured as a workflow artifact.
- JVM tests run before APK assembly.

### Canonical ledger foundation
Added pure Kotlin records for:
- `ShiftSession`
- `OfferObservation`
- `DecisionRecord`
- `TripOutcome`
- `OutcomeReconciliation`
- `ContextRecord`
- `ShiftMetrics`

Added `LedgerRepository` with an in-memory implementation so the domain can be exercised before committing to Room.

### Offer → Outcome reconciliation
Added `OutcomeReconciler` with conservative behavior:
- explicit `offerId` is a deterministic high-confidence join;
- a missing offer is rejected rather than guessed;
- a mismatched offer identifier remains unresolved;
- original offer and outcome records are never rewritten;
- reconciliation stores deltas and evidence references.

### Verification
Added deterministic JVM tests for the three reconciliation paths above.

## Not yet claimed

This pass does **not** establish:
- successful CI execution;
- APK build success;
- real-device capture;
- Room persistence;
- platform-specific offer extraction;
- temporal/economic fuzzy matching;
- complete shift reconstruction;
- production historical joins.

## Next code gate

1. Let CI expose compile/API failures.
2. Fix Android build failures.
3. Add durable Capture Fabric observation-window state and replay fixtures.
4. Connect canonical capture output to `OfferObservation` without losing raw evidence.
5. Add Room only after the domain/replay path is deterministic.
6. Implement shift/online-time reconstruction and offer lifecycle records.
7. Implement stronger outcome reconciliation only after real trip evidence exists.
