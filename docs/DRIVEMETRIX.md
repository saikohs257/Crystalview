# DriveMetrix

DriveMetrix is the longitudinal driver-memory and outcome-reconciliation layer inside Crystalview.

It is not a second application. It turns captured observations and completed deliveries into durable history that the live marketplace layer cannot derive from a single screen.

## Memory domains

- Shift/session history.
- Offer observations and offer episodes.
- Decision records and reason codes when legitimately recorded.
- Accepted/declined outcomes when legitimately recorded.
- Pickup and dropoff events.
- Trip UUIDs and timestamps when available from legitimate exports or user-provided records.
- Pay, tips, fare, incentives, tolls, fees, miles, and duration.
- City/day aggregates and physical context.
- Zone, anchor, staging, and dead-zone observations.
- Merchant, destination/apartment, dwell, and arrival context where legitimately observed.

## Canonical lifecycle

`Offer -> Decision -> Trip -> Outcome -> Historical memory`

The offer and outcome are separate records. DriveMetrix reconciles them; it does not rewrite the original offer observation when later information arrives.

## Outcome reconciliation

Where evidence permits, retain the relationship between:

- offered payout, distance, time, destination, and offer evidence;
- driver's decision;
- accepted trip and pickup/dropoff;
- actual duration and miles;
- actual fare, tips, incentives, tolls, and fees;
- wait/drop pain and issue tags;
- final realized economics;
- whether the prior decision/inference was supported by the outcome.

A failed or uncertain join is an explicit unresolved relationship, never a fabricated match.

## Historical learning boundary

DriveMetrix is the record of what happened and where the evidence came from. Market Mirror and Decision Friction may consume DriveMetrix history to estimate or arbitrate, but they must not mutate observation records.

Prediction-vs-outcome review belongs downstream of the record and must preserve the evidence used for both sides of the comparison.

## Implementation sequence

1. Define canonical local schema.
2. Import/reconcile existing delivery ledgers.
3. Persist shift/session records.
4. Project canonical offers and evidence references into the ledger.
5. Record decisions, pickup/dropoff, and outcomes.
6. Join captured offer episodes to known trips using explicit identifiers where available and conservative temporal/context joins otherwise.
7. Preserve unresolved joins for later adjudication.
8. Restore zone/anchor/dead-zone, merchant, destination/apartment, and legitimate context memory.
9. Expose historical features to Market Mirror and Decision Friction without mutating observation truth.
