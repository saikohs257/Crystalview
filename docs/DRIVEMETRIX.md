# DriveMetrix

DriveMetrix is the longitudinal driver-memory layer inside Crystalview.

It is not a second application. It turns captured offers and completed deliveries into durable history that can answer questions the live marketplace layer cannot answer from a single screen.

## Memory domains

- Offer observations and offer episodes.
- Accepted/declined outcomes when legitimately recorded.
- Pickup and dropoff events.
- Trip UUIDs and timestamps when available from legitimate exports or user-provided records.
- Pay, tips, fare, incentives, tolls, fees, miles, and duration.
- City/day aggregates and physical context.
- Merchant, destination, dwell, and arrival context where legitimately observed.

## Core relationship

`Offer -> Decision -> Trip -> Outcome -> Historical memory`

Market Mirror may consume DriveMetrix history to estimate expected outcomes. DriveMetrix itself remains a record of what happened and where the evidence came from.

## Evidence discipline

Historical joins must preserve provenance. A missing join is represented as missing evidence rather than silently inferred as a match.

## Implementation sequence

1. Define canonical local schema.
2. Import/reconcile existing delivery ledgers.
3. Join captured offer episodes to known trips using explicit identifiers where available and conservative temporal/context joins otherwise.
4. Preserve unresolved joins for later adjudication.
5. Expose historical features to Market Mirror without mutating the observation records.
