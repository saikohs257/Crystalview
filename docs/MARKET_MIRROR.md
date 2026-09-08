# Market Mirror

Market Mirror is the marketplace-state inference layer inside Crystalview.

## Thesis

The platform's algorithm is hidden, but its consequences are observable.

Crystalview therefore treats the marketplace as a partially observable system:

`observations -> hidden-state inference -> expected state transition -> decision`

## State candidates

- demand pressure
- supply pressure
- merchant friction
- rejection / fulfillment pressure
- destination quality
- position value
- platform urgency
- eligibility
- driver friction

## Core constructs

### Promise Gap
Compare what an offer appears to promise at presentation time with what the observed downstream outcome actually delivers.

### Why-Me
Estimate which observable state variables could explain why a particular offer was presented to this driver at this time.

### Position Value
Estimate the expected value of remaining in or moving through a location/state, separately from the value of the current offer.

### Transition Model
Estimate likely marketplace-state movement after a decision or event without pretending that the platform's hidden policy is known.

### Opportunity Surface
Represent nearby or near-future opportunity conditions as a state-space object rather than a single scalar score.

## Evidence classes

Every inference is labeled as one of:

`DIRECT | DERIVED | CORRELATIONAL | HEURISTIC | SPECULATIVE`

## Boundary

Market Mirror consumes preserved observations and historical memory. It must not rewrite the evidence layer or silently convert inference into observed fact.

No credential harvesting, CAPTCHA bypass, mass automated requests, hidden authenticated customer scraping, impersonation, or manipulation of platform state is part of this system.
